ADR — Identidad por Subject y Asignación Interna de Roles en B4RRHH

Estado: Propuesto

## Contexto
B4RRHH dispone de autenticación JWT y un modelo de autorización interno basado en roles, recursos, perfiles y políticas.
Actualmente, los roles se transportan en el JWT, lo cual mezcla identidad y autorización.

## Problema
Se necesita un modelo coherente que:
- Separe autenticación de autorización
- Permita operación en local sin IdP externo
- Evite usar el JWT como fuente de verdad de roles

## Decisión
B4RRHH utilizará:
- JWT como fuente de identidad (subject)
- Base de datos como fuente de roles

Se introduce la tabla:
authz.subject_role_assignment

## Modelo
Campos:
- subject_code
- role_code
- active
- assignment_origin
- created_at
- updated_at

Clave primaria:
(subject_code, role_code)

## Flujo
1. El frontend obtiene un JWT con subject
2. Backend autentica el token
3. Backend extrae subject
4. Backend resuelve roles desde BD
5. Backend evalúa permisos

## Consecuencias
Positivas:
- Separación clara de responsabilidades
- Preparado para futuro IdP
- Coherencia del modelo

Negativas:
- Nueva tabla y servicio
- Mayor complejidad inicial

## No objetivos
- No se introduce login con contraseña
- No se introduce dominio user
- No se integra IdP externo en esta fase

## Evolución futura
Integración con proveedor externo manteniendo autorización interna.

## Sobre subjeect_code
subject_code representa la identidad autenticada del actor y se trata como identificador opaco; no se normaliza por case y no se interpreta como business key.