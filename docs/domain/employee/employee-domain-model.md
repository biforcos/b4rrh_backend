# B4RRHH – Employee Domain Model

This document defines the conceptual **HR Core Domain Model** for the B4RRHH project.

Purpose:
- provide a stable reference for Copilot and developers
- ensure every new vertical follows the same architectural pattern
- prevent ad-hoc domain modelling

Architecture assumptions:
- Java 21
- Spring Boot
- Hexagonal Architecture
- Contract-first APIs (OpenAPI)
- Flyway migrations
- PostgreSQL

Global rule:
Public APIs MUST always use **functional business keys**, never technical IDs.

Employee identity business key:

    ruleSystemCode + employeeTypeCode + employeeNumber

Example:

    ESP + EMP + 0001

------------------------------------------------------------
BOUNDED CONTEXT: employee
------------------------------------------------------------

The employee bounded context represents the **core HR identity and lifecycle of a worker**.

Verticals inside this context:

    employee
    presence
    contact
    address
    cost_center
    contract
    assignment
    compensation
    work_schedule
    document
    absence

------------------------------------------------------------
1. employee.employee
------------------------------------------------------------

Purpose:
Defines the **identity of the employee** in the HR system.

Business Key:

    ruleSystemCode
    employeeTypeCode
    employeeNumber

Example:

    ESP / EMP / 0001

Typical attributes:

    firstName
    lastName
    birthDate
    hireDate
    nationality

Notes:

- employeeTypeCode comes from catalog: EMPLOYEE_TYPE
- technical ID may exist internally but MUST NOT appear in canonical APIs

------------------------------------------------------------
2. employee.presence
------------------------------------------------------------

Purpose:
Represents **periods of employment or presence in the company**.

Characteristics:

    historized
    prevents overlapping intervals
    lifecycle action = close

Identity:

    employee + presenceNumber

Expanded API identity:

    ruleSystemCode + employeeTypeCode + employeeNumber + presenceNumber

Attributes:

    startDate
    endDate
    entryReasonCode
    exitReasonCode
    companyCode

Catalog dependencies:

    EMPLOYEE_ENTRY_REASON
    EMPLOYEE_EXIT_REASON

Public API shape:

    POST   /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/presences
    GET    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/presences
    GET    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/presences/{presenceNumber}
    POST   /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/presences/{presenceNumber}/close

Notes:

- `presenceNumber` is the public identity of the presence resource
- technical `presenceId` must not appear in canonical APIs
- `close` is a domain action, not generic CRUD semantics

------------------------------------------------------------
3. employee.contact
------------------------------------------------------------

Purpose:
Stores **current communication channels** for the employee.

Characteristics:

    NOT historized
    one contact per type

Identity:

    employee + contactTypeCode

Expanded API identity:

    ruleSystemCode + employeeTypeCode + employeeNumber + contactTypeCode

Examples:

    EMAIL
    MOBILE

Attributes:

    contactValue

Catalog dependency:

    EMPLOYEE_CONTACT_TYPE

------------------------------------------------------------
4. employee.address
------------------------------------------------------------

Purpose:
Stores employee addresses.

Characteristics:

    historized
    multiple addresses allowed

Identity:

    employee + addressNumber

Attributes:

    addressTypeCode
    street
    city
    postalCode
    countryCode
    validFrom
    validTo

Catalog dependency:

    EMPLOYEE_ADDRESS_TYPE

------------------------------------------------------------
5. employee.contract
------------------------------------------------------------

Purpose:
Represents the contractual relationship between employee and company.

Characteristics:

    historized

Identity:

    employee + contractNumber

Attributes:

    contractTypeCode
    workingTimeCode
    startDate
    endDate
    companyCode

Catalog dependencies:

    CONTRACT_TYPE
    WORKING_TIME_TYPE

------------------------------------------------------------
6. employee.assignment
------------------------------------------------------------

Purpose:
Represents the **organizational assignment** of an employee.

Characteristics:

    historized

Identity:

    employee + assignmentNumber

Attributes:

    departmentCode
    jobCode
    managerEmployeeNumber
    costCenterCode
    locationCode

------------------------------------------------------------
7. employee.compensation
------------------------------------------------------------

Purpose:
Defines the **base compensation conditions** of the employee.

Characteristics:

    historized

Identity:

    employee + compensationNumber

Attributes:

    salaryAmount
    salaryTypeCode
    currencyCode
    bonusEligibility

------------------------------------------------------------
8. employee.work_schedule
------------------------------------------------------------

Purpose:
Defines the **working schedule**.

Characteristics:

    historized

Identity:

    employee + scheduleNumber

Attributes:

    scheduleTypeCode
    hoursPerWeek
    shiftCode

------------------------------------------------------------
9. employee.document
------------------------------------------------------------

Purpose:
Stores employee official documents.

Characteristics:

    historized

Identity:

    employee + documentNumber

Attributes:

    documentTypeCode
    documentIdentifier
    issuingCountry
    expirationDate

Catalog dependency:

    EMPLOYEE_DOCUMENT_TYPE

------------------------------------------------------------
10. employee.absence
------------------------------------------------------------

Purpose:
Represents absences such as vacations or sick leave.

Characteristics:

    historized

Identity:

    employee + absenceNumber

Attributes:

    absenceTypeCode
    startDate
    endDate
    approvalStatus

------------------------------------------------------------
Design Principles
------------------------------------------------------------

1. Every vertical must be implemented as:

    employee.<vertical>
        application
        domain
        infrastructure

2. APIs must never expose technical identifiers.

3. Catalog values must always be validated using:

    rulesystem.rule_entity
    rulesystem.rule_entity_type

4. Vertical identity must always be:

    employee + verticalKey

Examples:

    employee + contactTypeCode
    employee + presenceNumber
    employee + contractNumber

5. Historized verticals must implement:

    start_date
    end_date
    overlap validation

------------------------------------------------------------
Goal of this document
------------------------------------------------------------

This document acts as:

    - domain map
    - architectural guardrail
    - Copilot design reference

When implementing a new vertical:

    Follow the structure and principles defined in this document.
