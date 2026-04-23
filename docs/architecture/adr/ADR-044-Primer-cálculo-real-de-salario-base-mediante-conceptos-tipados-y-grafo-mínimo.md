ADR — Primer cálculo real de salario base mediante conceptos tipados y grafo mínimo
Estado

Propuesto

Contexto

En iteraciones recientes se ha construido un slice funcional que permite:

activar conceptos por convenio
vincular tablas mediante binding
resolver filas temporales por convenio/categoría/fecha
integrar conceptos reales en el lanzador de nómina

Ese trabajo ha sido útil para validar:

activation
binding
payroll_table_row
resolución temporal
integración con el launch

Sin embargo, la implementación actual de conceptos como BASE_SALARY o PLUS_CONVENIO se ha materializado mediante servicios concretos por concepto. Ese enfoque ha servido como spike técnico, pero no representa la arquitectura objetivo del motor de nómina.

El bundle de diseño ya fijaba una línea distinta: los conceptos de nómina deben resolverse por tipología de cálculo y por sources tipados, no por servicios hardcodeados por concepto. En particular, el bundle ya contempla tipologías como DIRECT_AMOUNT, RATE_BY_QUANTITY, PERCENTAGE y AGGREGATE, y además permite que un operando se resuelva a partir de otro CONCEPT. El ejemplo conceptual del salario base ya estaba descrito como una composición de cantidad y precio.

Se necesita, por tanto, una reconducción controlada:

aprovechar las piezas ya validadas
dejar de codificar conceptos de negocio “a mano”
empezar a ejecutar un mini grafo real
Problema

Se quiere obtener una primera nómina no fake con un salario base mínimo pero real, disparado desde el convenio y resuelto mediante conceptos configurados en base de datos.

Ese primer caso debe ser lo bastante simple para ser implementable en pocas iteraciones, pero lo bastante correcto como para validar la arquitectura del motor.

Decisión
1. Separar explícitamente concepto de negocio y concepto técnico

Se distinguen dos familias de conceptos:

Conceptos de negocio

Son los que representan líneas reales de nómina y pueden persistirse como resultado final.

Ejemplo:

101 - SALARIO_BASE
Conceptos técnicos

Son nodos auxiliares de cálculo, reutilizables, y no tienen por qué persistirse como líneas finales de nómina.

Ejemplos:

D01 - DIAS_PRESENCIA
P01 - PRECIO_DIA_TEORICO

Los conceptos técnicos podrán ser reutilizados por varios conceptos de negocio futuros.

2. El salario base piloto se modela como RATE_BY_QUANTITY

El primer concepto real de negocio será:

101 - SALARIO_BASE

Su tipología será:

RATE_BY_QUANTITY

Semántica:

quantity = CONCEPT(D01)
rate = CONCEPT(P01)

Resultado:

101 = P01 × D01

Esto sigue la línea ya fijada en el bundle para salario base como combinación de cantidad y precio.

3. D01 - DIAS_PRESENCIA se introduce como concepto técnico temporalmente simplificado

Se define:

D01 - DIAS_PRESENCIA

Tipología inicial:

DIRECT_AMOUNT

Valor inicial:

30

Esta simplificación es deliberada.
No se calcularán todavía días reales de presencia ni segmentación.

Objetivo de esta iteración:

validar la dependencia técnica
validar la ejecución por grafo
no bloquear el avance por la falta de cálculo temporal detallado

En iteraciones futuras, D01 podrá evolucionar para resolverse por segmento o por ventana real de presencia sin cambiar la estructura del concepto 101.

4. P01 - PRECIO_DIA_TEORICO se introduce como concepto técnico valorizado por tabla binded

Se define:

P01 - PRECIO_DIA_TEORICO

Tipología inicial:

DIRECT_AMOUNT resuelto por source TABLE
o, equivalentemente, un nodo técnico cuyo valor proviene de lookup a tabla binded

Valor:

daily_value de payroll_table_row

Lookup por:

convenio aplicable
categoría aplicable
fecha efectiva

El binding del convenio apuntará a la tabla salarial correspondiente, y P01 se resolverá leyendo daily_value de la fila vigente.

Esto aprovecha directamente la estructura ya existente en payroll_table_row, que ya contiene daily_value. No se requiere derivar el precio diario a partir del mensual. Eso permite un primer caso limpio y escalable.

5. El convenio dispara el concepto final de negocio, no los nodos técnicos como líneas finales

Para el primer caso:

el convenio activa 101 - SALARIO_BASE

El motor, al resolver 101, podrá descubrir y ejecutar sus dependencias:

D01
P01

Pero el resultado persistido en la nómina será, en esta iteración:

101 - SALARIO_BASE

Los conceptos técnicos podrán existir:

como nodos de ejecución
como trazabilidad futura
como snapshot si más adelante interesa

Pero no se consideran todavía líneas finales de nómina.

6. La ejecución se hará mediante un grafo mínimo, no mediante servicios concretos por concepto de negocio

Se abandona como dirección arquitectónica final la idea de:

CalculateBaseSalaryService
CalculateAgreementPlusService
etc.

Esos servicios se reinterpretan como spikes o resolvedores transitorios útiles para validar piezas del pipeline.

La dirección correcta pasa a ser:

resolver conceptos por tipología
resolver operandos por source
permitir que un concepto dependa de otro CONCEPT

Para la primera iteración no se construirá un motor genérico completo, pero sí un mini dispatcher suficiente para ejecutar:

DIRECT_AMOUNT
RATE_BY_QUANTITY

y para resolver operandos tipo:

CONCEPT
TABLE
7. El lanzador de nómina deberá enchufarse al mini grafo real

El endpoint de cálculo / lanzador ya existente dejará de inyectar un concepto fake para este caso y pasará a:

identificar que el convenio dispara 101 - SALARIO_BASE
resolver el mini grafo:
D01
P01
101
persistir 101 como línea final real de nómina

El camino fake podrá mantenerse temporalmente como fallback o modo alternativo, pero el caso piloto de salario base debe pasar a ejecutarse ya con grafo mínimo real.

Consecuencias
Positivas
Se reconduce el diseño hacia el motor real sin tirar piezas útiles.
Se valida el uso real de:
trigger por convenio
binding de tabla
source CONCEPT
source TABLE
tipología RATE_BY_QUANTITY
Se evita seguir creando servicios por concepto como arquitectura final.
Se prepara una base escalable:
mañana D01 podrá calcularse por segmento
mañana P02 podrá depender de P01 y J01
mañana se podrán introducir coeficientes de jornada sin reescribir 101
Negativas / Costes
Lo ya implementado como cálculo directo de BASE_SALARY y PLUS_CONVENIO pasa a ser transitorio.
Hay que introducir un primer wiring real de dependencias entre conceptos.
El launch tendrá que dejar de pensar en “conceptos hardcodeados” y empezar a ejecutar un mini plan de cálculo.
No objetivos de esta iteración

No se pretende todavía:

calcular días reales de presencia
aplicar coeficiente real de jornada
introducir segmentación temporal
modelar dependencias arbitrarias complejas
persistir todos los conceptos técnicos como líneas de nómina
construir un motor genérico completo con todas las tipologías posibles
resolver plus convenio dentro de este mismo salto
Diseño mínimo resultante
Conceptos iniciales
101 - SALARIO_BASE
tipo: RATE_BY_QUANTITY
quantity source: CONCEPT(D01)
rate source: CONCEPT(P01)
D01 - DIAS_PRESENCIA
tipo: DIRECT_AMOUNT
valor inicial: 30
P01 - PRECIO_DIA_TEORICO
tipo: DIRECT_AMOUNT / valor resuelto por TABLE
source: tabla binded por convenio
campo usado: daily_value
Resultado final
línea final persistida: 101 - SALARIO_BASE
Estrategia de implementación
Paso 1

Sembrar en base de datos:

101
D01
P01
sus tipologías
sus relaciones/dependencias
el binding de tabla para P01
Paso 2

Crear el wiring mínimo del mini grafo:

resolver 101
descubrir dependencias
resolver D01
resolver P01
calcular 101
Paso 3

Enchufar ese mini cálculo al lanzador de nómina existente

Paso 4

Persistir 101 como concepto final real en la tabla de resultados

Nota de transición

Los servicios actuales tipo CalculateBaseSalaryService o CalculateAgreementPlusService no se consideran el diseño final del motor. Se mantienen únicamente como apoyo temporal o como material de transición mientras el primer camino real basado en tipologías y dependencias queda operativo.

Decisión práctica inmediata

La siguiente iteración no se enfocará en añadir más conceptos directos.
Se enfocará en conseguir que el lanzador calcule un único concepto real final (101 - SALARIO_BASE) mediante:

trigger de convenio
mini grafo
conceptos técnicos
tipologías mínimas
lookup a tabla binded