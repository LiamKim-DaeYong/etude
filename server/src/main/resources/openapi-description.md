## Conventions

- **Instants:** ISO-8601 UTC strings ending in `Z`, microsecond precision. Clients convert to the user's time zone.
- **Money:** `{"amount": "12.50", "currency": "USD"}` (schema `Money`). `amount` is a decimal string in the currency's
  minor units; do not do arithmetic on it as a floating-point number. Requests accept a string or a number and reject
  unknown currencies and amounts with more decimal places than the currency allows.
- **Sign-in (admin API):** a server-side session. Get a CSRF token from `GET /admin/v1/auth/csrf`, then sign in with
  `POST /admin/v1/auth/owner-login` (email) or `POST /admin/v1/auth/staff-login` (company code and username). The
  browser keeps only the session cookie; send requests from the same origin with credentials. Every state-changing
  request carries the CSRF token in the returned header. Fetch a new token after signing in. Every other
  `/admin/v1` operation needs a signed-in session.
- **Company data:** board and post operations (`/admin/v1/boards`, `/admin/v1/posts`) work only on the signed-in
  user's company. Requests never carry a company id; ids that belong to another company are reported as
  `404 NOT_FOUND`, the same as unknown ids. Sign-in, session and CSRF operations are outside any company, and product,
  inventory and order operations are not yet separated by company.
- **Paging:** paged lists take `page` (zero-based, default `0`) and `size` and return
  `{items, page, size, totalElements, totalPages}`. Each operation states its default and maximum `size`.
- **Errors:** `application/problem+json` (schema `Problem`) with a `code`. Branch on `code`; `detail` is English text
  for developers.

| `code` | Status | When |
|---|---|---|
| `INVALID_REQUEST` | 400 | Validation failure, unreadable body, malformed path variable. `errors[]` lists `{field, code, message}` |
| `AUTHENTICATION_REQUIRED` | 401 | Not signed in, or the session ended |
| `INVALID_CREDENTIALS` | 401 | Sign-in failed. The same code for an unknown user and a wrong password |
| `CSRF_TOKEN_INVALID` | 403 | A state-changing request without a valid CSRF token |
| `ACCESS_DENIED` | 403 | Signed in, but not allowed for this user |
| `NOT_FOUND` | 404 | Unknown resource or path |
| `INTERNAL_ERROR` | 500 | Unexpected failure |
| `SERVICE_UNAVAILABLE` | 503 | Busy, e.g. another request holds the same inventory too long. Retry after `Retry-After` seconds |
| Business codes | 4xx | Listed on each operation that can return them |
| Other | 4xx | HTTP status name, e.g. `METHOD_NOT_ALLOWED` |

`errors[].code` is the violated constraint (`NotBlank`, `Positive`, ...) or `InvalidValue` when the body could not be read.
