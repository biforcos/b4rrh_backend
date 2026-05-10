# ADR-047 — Lifecycle Workflow Participant Pattern (Hire / Terminate)

## Estado

Propuesto

---

## Contexto

ADR-007 y ADR-018 establecieron que los workflows de ciclo de vida (Hire, Terminate, Rehire) se implementan como servicios de orquestación en la capa de aplicación: un único `@Transactional` que coordina múltiples verticales y garantiza coherencia temporal.

Esa decisión sigue siendo correcta. El problema es la **implementación concreta** que ha emergido de ella.

`HireEmployeeService` actualmente:

- Tiene **10 dependencias inyectadas** en el constructor
- Ocupa **358 líneas**
- Ejecuta **8 sub-operaciones** secuenciales dentro del método `hire()`
- `HireEmployeeExceptionHandler` importa excepciones de **6 verticales distintos**

El patrón real que ha emergido es este:

```
HireEmployeeService → CreatePresenceUseCase
                    → CreateContractUseCase
                    → CreateWorkCenterUseCase
                    → CreateCostCenterDistributionUseCase
                    → CreateWorkingTimeUseCase
                    → CreateLaborClassificationUseCase
                    → NextEmployeeNumberPort
                    → WorkCenterCompanyValidator
                    → EmployeeTypeCatalogValidator
```

Cada nuevo vertical que necesita participar en el hire obliga a:

1. Añadir una dependencia al constructor de `HireEmployeeService`
2. Añadir la llamada dentro del método `hire()`
3. Añadir un `@ExceptionHandler` en `HireEmployeeExceptionHandler` para las excepciones de ese vertical

Esto viola Open/Closed: el servicio más crítico del sistema cambia con cada feature nueva.

El patrón se reproduce en `TerminateEmployeeService` y `RehireEmployeeService`.

---

## Problema

El diseño actual acopla el servicio orquestador con cada vertical que participa en el workflow. Añadir un nuevo vertical al hire no es una operación local — requiere modificar el servicio central.

Esto no es un problema de complejidad (el hire ES complejo por dominio), sino un problema de **dirección de las dependencias**.

---

## Decisión

Se introduce el **Lifecycle Workflow Participant Pattern**:

> En vez de que el servicio de lifecycle conozca cada vertical, cada vertical se registra como participante del workflow.

### 1. Puerto `HireParticipant`

```java
// employee/lifecycle/application/port/HireParticipant.java
public interface HireParticipant {
    int order();
    void participate(HireContext ctx);
}
```

`order()` garantiza la secuencia de ejecución. Los valores de orden son:

| Orden | Participante | Razón |
|-------|-------------|-------|
| 10 | EmployeeCoreParticipant | El employee debe existir antes que todo |
| 20 | PresenceParticipant | Require employee.id |
| 30 | WorkCenterParticipant | Require employee.id |
| 40 | CostCenterParticipant | Require employee.id (opcional) |
| 50 | ContractParticipant | Require employee.id |
| 60 | LaborClassificationParticipant | Require employee.id |
| 70 | WorkingTimeParticipant | Require employee.id |

### 2. Objeto de contexto `HireContext`

`HireContext` viaja por todos los participantes. Cada uno lee lo que necesita del comando y escribe su resultado:

```java
// employee/lifecycle/application/model/HireContext.java
public class HireContext {
    private final HireEmployeeCommand command;
    private final String employeeNumber;      // generado antes de los participantes
    private Employee employee;
    private Presence presence;
    private WorkCenter workCenter;
    private CostCenterDistribution costCenterDistribution;
    private Contract contract;
    private LaborClassification laborClassification;
    private WorkingTime workingTime;

    // getters y setters
    public HireEmployeeResult toResult() { ... }
}
```

### 3. `HireEmployeeService` tras el refactor

```java
@Service
public class HireEmployeeService implements HireEmployeeUseCase {

    private final List<HireParticipant> participants;
    private final NextEmployeeNumberPort nextEmployeeNumberPort;
    private final HireEmployeePreConditionValidator validator;

    public HireEmployeeService(
            List<HireParticipant> participants,
            NextEmployeeNumberPort nextEmployeeNumberPort,
            HireEmployeePreConditionValidator validator) {
        this.participants = participants.stream()
                .sorted(Comparator.comparingInt(HireParticipant::order))
                .toList();
        this.nextEmployeeNumberPort = nextEmployeeNumberPort;
        this.validator = validator;
    }

    @Override
    @Transactional
    public HireEmployeeResult hire(HireEmployeeCommand command) {
        validator.validate(command);
        String employeeNumber = nextEmployeeNumberPort.consumeNext(
                normalizeCode(command.ruleSystemCode()));
        HireContext ctx = new HireContext(command, employeeNumber);
        participants.forEach(p -> p.participate(ctx));
        return ctx.toResult();
    }
}
```

**Añadir un nuevo vertical al hire = crear un fichero `@Component` que implementa `HireParticipant`. `HireEmployeeService` nunca vuelve a cambiar.**

### 4. Extracción de `HireEmployeePreConditionValidator`

Las validaciones previas al hire (tipo de empleado, coherencia work center / company) se extraen a un servicio dedicado con sus propios tests unitarios:

```java
// employee/lifecycle/application/service/HireEmployeePreConditionValidator.java
@Component
public class HireEmployeePreConditionValidator {
    private final EmployeeTypeCatalogValidator employeeTypeCatalogValidator;
    private final WorkCenterCompanyValidator workCenterCompanyValidator;

    public void validate(HireEmployeeCommand command) { ... }
}
```

### 5. Exception handlers descentralizados

Con el patrón participant, cada vertical puede manejar sus propias excepciones mediante un `@RestControllerAdvice` scoped a `HireEmployeeController`:

```java
// workcenter/infrastructure/web/WorkCenterHireExceptionHandler.java
@RestControllerAdvice(assignableTypes = HireEmployeeController.class)
@Order(10)
public class WorkCenterHireExceptionHandler {
    @ExceptionHandler(WorkCenterCatalogValueInvalidException.class)
    public ResponseEntity<HireEmployeeErrorResponse> handle(...) { ... }
}
```

`HireEmployeeExceptionHandler` queda únicamente con las excepciones propias del lifecycle (autonumeración, validaciones transversales).

### 6. El mismo patrón para TERMINATION

```java
// employee/lifecycle/application/port/TerminationParticipant.java
public interface TerminationParticipant {
    int order();
    void participate(TerminationContext ctx);
}
```

`TerminationContext` lleva el `Employee` existente, la fecha de terminación, el motivo, y acumula los resultados del cierre de cada vertical (presence.endDate, contract.endDate, etc.).

El orden en termination es inverso a hire en semántica (cerrar desde fuera hacia dentro):

| Orden | Participante |
|-------|-------------|
| 10 | WorkingTimeTerminationParticipant |
| 20 | WorkCenterTerminationParticipant |
| 30 | CostCenterTerminationParticipant |
| 40 | ContractTerminationParticipant |
| 50 | LaborClassificationTerminationParticipant |
| 60 | PresenceTerminationParticipant | ← cierra presence al final |

Presence es la última en cerrarse porque es el eje del lifecycle (ADR-018): cerrarla primero implicaría que el empleado está "fuera" mientras aún tiene contratos abiertos.

---

## Invariantes que NO cambian

- **La transacción sigue siendo única.** Todos los participantes se ejecutan dentro del `@Transactional` del servicio orquestador. Si cualquier participante lanza, Spring hace rollback de todo. El comportamiento transaccional es idéntico al actual.
- **No se introducen domain events asíncronos.** Los eventos síncronos de Spring (`@EventListener`) son una alternativa válida al participant port, pero añaden indirección sin ventaja real en este contexto. Se descarta.
- **No se introducen sagas ni consistencia eventual.** El hire necesita devolver el número de matrícula generado en la misma petición HTTP. La atomicidad no es negociable.
- **El orden sigue siendo explícito y auditable.** `order()` como entero en la interfaz es deliberadamente simple — no hay grafos de dependencias ni resolución dinámica. Si el orden cambia, se ve en el diff.

---

## Consecuencias positivas

- `HireEmployeeService` y `TerminateEmployeeService` se vuelven estables — no cambian al añadir nuevos verticales
- Cada vertical es dueño de su lógica de participación en el workflow (cohesión)
- Los tests de cada participante son unitarios y pequeños
- `HireEmployeeExceptionHandler` pierde el conocimiento de verticales externos
- La misma base sirve para Rehire sin duplicar la orquestación

## Consecuencias negativas / riesgos

- El orden de participación es implícito al leer el código del servicio — hay que consultar las implementaciones para ver la secuencia completa. **Mitigación:** documentar el orden en `HireContext` con un comment de referencia.
- Spring inyecta `List<HireParticipant>` en el orden que descubre los beans, por lo que el `order()` explícito es crítico — un test de contexto debe verificar el orden en cada release.
- La migración de `HireEmployeeService` al patrón debe hacerse gradualmente (un participante cada vez) para no introducir regresiones.

---

## Alternativas descartadas

### Mantener el orquestador actual y aceptar el crecimiento

Descartado:

- 10 dependencias hoy, 15 en 12 meses
- Cada feature de lifecycle toca el servicio más crítico del sistema
- No hay punto de estabilización natural

### Domain events asíncronos (ApplicationEventPublisher + @TransactionalEventListener)

Descartado:

- Añade indirección sin ventaja en un contexto single-service
- El orden de los handlers es menos explícito
- El debugging es más complejo
- Para lograr el mismo orden de operaciones se necesita `@Order` igualmente

### Saga pattern (compensating transactions)

Descartado:

- Consistencia eventual no es apropiada para hire
- El número de matrícula debe estar disponible sincrónicamente
- Añade infraestructura de saga que no existe ni se necesita

### Employee como agregado monolítico

Descartado desde ADR-002 y ADR-007. La arquitectura vertical se mantiene.

---

## Relación con ADRs anteriores

| ADR | Relación |
|-----|----------|
| ADR-007 | Este ADR evoluciona la implementación de los workflows definidos allí. Los workflows siguen siendo orquestados y transaccionales. |
| ADR-018 | La sección "Orquestación interna" de ADR-018 queda reemplazada por este patrón. El modelo de datos, la API y las validaciones no cambian. |

---

## Resumen

El Lifecycle Workflow Participant Pattern invierte la dependencia entre el orquestador y los verticales:

- Antes: el servicio conoce cada vertical
- Después: cada vertical se registra en el servicio

La transacción, el orden de ejecución y el contrato de API permanecen inalterados. Lo que cambia es que añadir un nuevo vertical al hire o al terminate se convierte en una operación local (crear un fichero), no en una modificación del servicio central.
