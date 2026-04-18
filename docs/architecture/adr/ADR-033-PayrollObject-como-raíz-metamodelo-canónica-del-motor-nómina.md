ADR — PayrollObject como raíz metamodelo canónica del motor de nómina
Estado

Propuesto

Contexto

El diseño del motor de nómina de B4RRHH está empezando a consolidarse alrededor de varios tipos de elementos configurables del dominio de payroll.

Inicialmente, la conversación se ha centrado en los conceptos de nómina, pero rápidamente han aparecido también otros candidatos naturales del mismo espacio funcional, como:

tablas
constantes
futuros objetos auxiliares o parametrizables del motor

Si el modelo parte directamente de payrollConcept como raíz, existe el riesgo de:

sobredimensionar el concepto de nómina para que absorba responsabilidades que no le pertenecen
acabar creando metamodelos paralelos inconsistentes para tablas, constantes y otros elementos
mezclar identidad común con semántica específica de un subtipo concreto

Por tanto, antes de profundizar en el modelado específico de los conceptos, es necesario fijar una raíz metamodelo común para todos los objetos configurables del motor.

Decisión

Se introduce PayrollObject como raíz metamodelo canónica del motor de nómina.

Todo elemento configurable del metamodelo de payroll deberá modelarse primero como un PayrollObject, con una identidad funcional común basada en business keys.

La business key canónica de PayrollObject será:

ruleSystemCode
objectTypeCode
objectCode

PayrollObject actuará como raíz común para distintos tipos de objeto del dominio payroll, incluyendo al menos:

CONCEPT
TABLE
CONSTANT

El atributo canónico de identidad del objeto será objectCode.

Cuando se trabaje dentro de un subtipo concreto, podrán usarse alias semánticos de contexto, por ejemplo:

conceptCode
tableCode
constantCode

Sin embargo, esos nombres no definen identidades alternativas ni nuevas business keys. Son únicamente proyecciones semánticas del mismo objectCode dentro del contexto de cada subtipo.

Consecuencias
Positivas
Se fija una raíz común clara para el metamodelo del motor de nómina.
Se evita construir un modelo demasiado centrado exclusivamente en conceptos.
Se facilita la incorporación futura de tablas, constantes y otros objetos parametrizables sin rediseñar la base del modelo.
Se mantiene coherencia con las reglas generales de B4RRHH, donde la identidad pública se expresa mediante business keys funcionales y no mediante IDs técnicos.
Se separa correctamente la identidad común del objeto de la semántica específica de cada subtipo.
Costes o limitaciones
Obliga a introducir una capa de abstracción adicional antes de modelar los subtipos concretos.
Requiere disciplina para no contaminar el modelo raíz con propiedades específicas de PayrollConcept u otros tipos.
Puede parecer más abstracto al inicio que arrancar directamente desde payrollConcept, aunque a medio plazo reduce deuda semántica.
No objetivos

Este ADR no define todavía:

las propiedades específicas de PayrollConcept
el modelo de versionado de los objetos de payroll
la estrategia de cálculo
la segmentación intrames
las reglas de cálculo ni su representación
la implementación física en base de datos o APIs

Este ADR solo fija la raíz metamodelo común y su identidad funcional.

Resumen ejecutivo

El motor de nómina de B4RRHH no se modelará partiendo directamente de conceptos aislados, sino desde una raíz metamodelo común llamada PayrollObject.

La identidad funcional canónica será:

ruleSystemCode
objectTypeCode
objectCode

Los subtipos como PayrollConcept, PayrollTable o PayrollConstant heredarán esa identidad común.

Los nombres como conceptCode o tableCode se consideran alias semánticos contextuales del objectCode, no nuevas business keys.