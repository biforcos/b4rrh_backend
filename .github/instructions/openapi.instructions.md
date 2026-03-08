---
applyTo: "src/main/resources/**/*.yaml,src/main/resources/**/*.yml,**/openapi/**/*.yaml,**/openapi/**/*.yml"
---

## OpenAPI rules

- OpenAPI is the source of truth for backend/frontend communication.
- Keep naming consistent, stable, and explicit.
- Use English names for schemas, fields, paths, and operationIds.
- Prefer resource-oriented endpoints.
- Include validation rules whenever known.
- Define response codes explicitly.
- Avoid vague "data" wrappers unless justified.
- Separate write models from read models when useful.
- Do not expose internal persistence structure in the contract.
- Do not expose internal numeric technical identifiers unless intentionally part of the API.
- If a field has business ambiguity, annotate with a description and flag it for user review.
- Never introduce payroll fields in personnel administration APIs unless explicitly requested.