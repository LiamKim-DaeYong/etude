export type AdminRequestErrorKind =
  | 'invalid-request'
  | 'missing-csrf'
  | 'cancelled'
  | 'timeout'
  | 'network'
  | 'invalid-response'
  | 'unauthenticated'
  | 'forbidden'
  | 'http';

const messages: Record<AdminRequestErrorKind, string> = {
  'invalid-request': '요청 설정을 확인해 주세요.',
  'missing-csrf': '요청 보호 정보를 확인하지 못했습니다.',
  cancelled: '요청을 취소했습니다.',
  timeout: '요청 시간이 초과되었습니다.',
  network: '서버에 연결하지 못했습니다.',
  'invalid-response': '서버 응답을 확인하지 못했습니다.',
  unauthenticated: '로그인이 필요합니다.',
  forbidden:
    '요청을 처리할 권한이 없거나 요청 보호 정보를 확인하지 못했습니다.',
  http: '요청을 처리하지 못했습니다.',
};

export class AdminRequestError extends Error {
  readonly kind: AdminRequestErrorKind;
  readonly status?: number;
  readonly code?: string;
  // A dispatched mutation may have completed even if its response was lost.
  readonly outcomeUnknown: boolean;

  constructor(
    kind: AdminRequestErrorKind,
    options: { status?: number; code?: string; outcomeUnknown?: boolean } = {},
  ) {
    super(
      options.outcomeUnknown
        ? '요청 결과를 확인하지 못했습니다. 처리 여부를 확인한 후 다시 시도해 주세요.'
        : messages[kind],
    );
    this.name = 'AdminRequestError';
    this.kind = kind;
    this.status = options.status;
    this.code = options.code;
    this.outcomeUnknown = options.outcomeUnknown ?? false;
  }
}

type RequestMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface AdminRequestOptions<T> {
  path: string;
  method?: RequestMethod;
  body?: unknown;
  /** Validate unknown JSON before returning application data. */
  parse: (value: unknown) => T;
  signal?: AbortSignal;
  timeoutMs?: number;
  /** Supplied by the future BE contract; never persisted by this module. */
  csrf?: { headerName: string; value: string };
}

function containsUnsafeCharacters(value: string) {
  return [...value].some((character) => {
    const code = character.charCodeAt(0);
    return code <= 32 || code === 127 || character === '\\';
  });
}

function adminPath(path: string) {
  if (
    !path.startsWith('/admin/v1/') ||
    path.includes('#') ||
    containsUnsafeCharacters(path)
  ) {
    throw new AdminRequestError('invalid-request');
  }

  try {
    // Reject encoded separators and double encoding before URL normalization.
    for (const segment of path.split('?')[0]!.split('/')) {
      const decoded = decodeURIComponent(segment);
      if (
        decoded === '.' ||
        decoded === '..' ||
        /[/%?#;]/.test(decoded) ||
        containsUnsafeCharacters(decoded)
      ) {
        throw new Error();
      }
    }
    const url = new URL(path, 'https://admin.invalid');
    if (
      url.origin !== 'https://admin.invalid' ||
      !url.pathname.startsWith('/admin/v1/')
    ) {
      throw new Error();
    }
    return url.pathname + url.search;
  } catch {
    throw new AdminRequestError('invalid-request');
  }
}

function requestHeaders(
  mutation: boolean,
  csrf: AdminRequestOptions<unknown>['csrf'],
) {
  const headers = new Headers({
    Accept: 'application/json, application/problem+json',
  });
  if (mutation) {
    if (!csrf) throw new AdminRequestError('missing-csrf');
    // Restrict configuration to standard CSRF headers, never request overrides.
    if (
      !/^X-(?:CSRF|XSRF)-TOKEN$/i.test(csrf.headerName) ||
      !csrf.value ||
      csrf.value.length > 4096 ||
      !/^[\x21-\x7e]+$/.test(csrf.value)
    ) {
      throw new AdminRequestError('invalid-request');
    }
    headers.set(csrf.headerName, csrf.value);
  }
  return headers;
}

function problemCode(value: unknown, status: number): string | undefined {
  if (
    typeof value === 'object' &&
    value !== null &&
    'status' in value &&
    value.status === status &&
    'code' in value &&
    typeof value.code === 'string' &&
    /^[A-Z][A-Z0-9_]{0,99}$/.test(value.code)
  ) {
    return value.code;
  }
}

function httpError(status: number, code?: string, mutation = false) {
  return new AdminRequestError(
    status === 401 ? 'unauthenticated' : status === 403 ? 'forbidden' : 'http',
    { status, code, outcomeUnknown: mutation && status >= 500 },
  );
}

/** No retries, authentication decisions, endpoint assumptions, or stored data. */
export function createAdminTransport(
  fetchRequest: typeof fetch = (input, init) => globalThis.fetch(input, init),
) {
  return {
    async request<T>(options: AdminRequestOptions<T>): Promise<T> {
      const path = adminPath(options.path);
      const method = options.method ?? 'GET';
      const mutation = method !== 'GET';
      const timeoutMs = options.timeoutMs ?? 15_000;
      if (
        !['GET', 'POST', 'PUT', 'PATCH', 'DELETE'].includes(method) ||
        !Number.isInteger(timeoutMs) ||
        timeoutMs < 1 ||
        timeoutMs > 120_000 ||
        (!mutation && options.body !== undefined)
      ) {
        throw new AdminRequestError('invalid-request');
      }
      const headers = requestHeaders(mutation, options.csrf);
      let body: string | undefined;
      if (options.body !== undefined) {
        try {
          body = JSON.stringify(options.body);
          if (body === undefined) throw new Error();
        } catch {
          throw new AdminRequestError('invalid-request');
        }
        headers.set('Content-Type', 'application/json');
      }

      const deadline = new AbortController();
      const signal = options.signal
        ? AbortSignal.any([options.signal, deadline.signal])
        : deadline.signal;
      const timer = setTimeout(() => deadline.abort(), timeoutMs);
      let dispatched = false;
      let response: Response | undefined;
      try {
        signal.throwIfAborted();
        dispatched = true;
        response = await fetchRequest(path, {
          method,
          headers,
          body,
          signal,
          credentials: 'same-origin',
          mode: 'same-origin',
          cache: 'no-store',
          redirect: 'error',
          referrerPolicy: 'no-referrer',
        });
        signal.throwIfAborted();
        if (
          response.redirected ||
          response.type === 'opaqueredirect' ||
          response.status < 200 ||
          (response.status >= 300 && response.status < 400)
        ) {
          throw new AdminRequestError('invalid-response');
        }

        const mediaType = response.headers
          .get('Content-Type')
          ?.split(';')[0]
          ?.trim()
          .toLowerCase();
        if (!response.ok) {
          let code: string | undefined;
          if (mediaType === 'application/problem+json') {
            try {
              code = problemCode(await response.json(), response.status);
            } catch {
              // HTTP status remains authoritative even with a malformed body.
            }
          }
          signal.throwIfAborted();
          throw httpError(response.status, code, mutation);
        }

        if (response.status !== 204 && mediaType !== 'application/json') {
          throw new AdminRequestError('invalid-response');
        }
        try {
          const value: unknown =
            response.status === 204 ? undefined : await response.json();
          signal.throwIfAborted();
          const result = options.parse(value);
          signal.throwIfAborted();
          return result;
        } catch {
          throw new AdminRequestError('invalid-response');
        }
      } catch (error) {
        const outcomeUnknown = mutation && dispatched;
        // Preserve an observed HTTP rejection even if cancellation races its body.
        if (response && !response.ok && response.status >= 400) {
          if (
            error instanceof AdminRequestError &&
            error.kind !== 'invalid-response'
          ) {
            throw error;
          }
          throw httpError(response.status, undefined, mutation);
        }
        if (signal.aborted) {
          throw new AdminRequestError(
            options.signal?.aborted ? 'cancelled' : 'timeout',
            { outcomeUnknown },
          );
        }
        if (error instanceof AdminRequestError) {
          throw new AdminRequestError(error.kind, {
            status: response?.status,
            outcomeUnknown,
          });
        }
        throw new AdminRequestError('network', { outcomeUnknown });
      } finally {
        clearTimeout(timer);
        // Discard rejected HTML/redirect bodies without retaining a connection.
        if (response?.body && !response.bodyUsed) {
          void response.body.cancel().catch(() => undefined);
        }
      }
    },
  };
}
