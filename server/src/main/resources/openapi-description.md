## Conventions

- **Instants:** ISO-8601 UTC strings ending in `Z`, microsecond precision. Clients convert to the user's time zone.
- **Money:** `{"amount": "12.50", "currency": "USD"}` (schema `Money`). `amount` is a decimal string in the currency's
  minor units; do not do arithmetic on it as a floating-point number. Requests accept a string or a number and reject
  unknown currencies and amounts with more decimal places than the currency allows.
- **Errors:** `application/problem+json` (schema `Problem`) with a `code`. Branch on `code`; `detail` is English text
  for developers.

| `code` | Status | When |
|---|---|---|
| `INVALID_REQUEST` | 400 | Validation failure, unreadable body, malformed path variable. `errors[]` lists `{field, code, message}` |
| `NOT_FOUND` | 404 | Unknown resource or path |
| `INTERNAL_ERROR` | 500 | Unexpected failure |
| Business codes | 4xx | Listed on each operation that can return them |
| Other | 4xx | HTTP status name, e.g. `METHOD_NOT_ALLOWED` |

`errors[].code` is the violated constraint (`NotBlank`, `Positive`, ...) or `InvalidValue` when the body could not be read.
