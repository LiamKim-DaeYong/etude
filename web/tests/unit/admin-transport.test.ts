import assert from 'node:assert/strict';
import test from 'node:test';
import {
  AdminRequestError,
  createAdminTransport,
} from '../../apps/admin/src/lib/http/admin-transport.ts';

const path = '/admin/v1/example';
const csrf = { headerName: 'X-CSRF-TOKEN', value: 'example-token' };
const secret = 'private server diagnostic';

function parse(value: unknown) {
  if (
    typeof value === 'object' &&
    value !== null &&
    'ready' in value &&
    value.ready === true
  ) {
    return { ready: true };
  }
  throw new Error(secret);
}

function rejection(
  kind: AdminRequestError['kind'],
  expected: Partial<
    Pick<AdminRequestError, 'status' | 'code' | 'outcomeUnknown'>
  > = {},
) {
  return (error: unknown) => {
    assert.ok(error instanceof AdminRequestError);
    assert.equal(error.kind, kind);
    for (const [name, value] of Object.entries(expected)) {
      assert.equal(error[name as keyof typeof expected], value);
    }
    assert.ok(!error.message.includes(secret));
    assert.equal(error.cause, undefined);
    return true;
  };
}

test('GET fixes security options and passes only validated JSON to callers', async () => {
  let calls = 0;
  const client = createAdminTransport(async (input, init) => {
    calls++;
    assert.equal(input, path + '?query=%ED%95%9C%EA%B8%80');
    assert.equal(init?.method, 'GET');
    assert.equal(init?.credentials, 'same-origin');
    assert.equal(init?.mode, 'same-origin');
    assert.equal(init?.cache, 'no-store');
    assert.equal(init?.redirect, 'error');
    assert.equal(init?.referrerPolicy, 'no-referrer');
    const headers = new Headers(init.headers);
    assert.equal(
      headers.get('Accept'),
      'application/json, application/problem+json',
    );
    assert.equal(headers.get('Content-Type'), null);
    assert.equal(headers.get(csrf.headerName), null);
    assert.equal(init.body, undefined);
    return Response.json({ ready: true });
  });
  assert.deepEqual(
    await client.request({
      path: path + '?query=%ED%95%9C%EA%B8%80',
      parse,
      csrf,
    }),
    {
      ready: true,
    },
  );
  assert.equal(calls, 1);
});

test('external URLs, customer API and encoded path escapes never reach fetch', async () => {
  let calls = 0;
  const client = createAdminTransport(async () => {
    calls++;
    return Response.json({ ready: true });
  });
  for (const invalid of [
    'https://example.com/admin/v1/example',
    '//example.com/admin/v1/example',
    '/api/v1/example',
    '/admin/v10/example',
    '/admin/v1/../example',
    '/admin/v1/%2e%2e/example',
    '/admin/v1/%252e%252e/example',
    '/admin/v1/..;/../api/v1/example',
    '/admin/v1/%2e%2e%3b/example',
    '/admin/v1/%2fapi/example',
    '/admin/v1/%5capi/example',
    '/admin/v1/./example',
    '/admin/v1/example#fragment',
    '/admin/v1/ex ample',
    '/admin/v1/%0d%0aexample',
    '/admin/v1/%broken',
    '/admin/v1/\\example',
  ]) {
    await assert.rejects(
      client.request({ path: invalid, parse }),
      rejection('invalid-request'),
    );
  }
  assert.equal(calls, 0);
});

test('every mutation requires CSRF and sends JSON without changing decimal strings', async () => {
  let calls = 0;
  const body = { amount: '12345678901234567890.12', currency: 'USD' };
  const client = createAdminTransport(async (_input, init) => {
    calls++;
    assert.equal(new Headers(init?.headers).get('X-CSRF-TOKEN'), csrf.value);
    assert.equal(
      new Headers(init?.headers).get('Content-Type'),
      'application/json',
    );
    assert.equal(init?.body, JSON.stringify(body));
    return Response.json({ ready: true });
  });
  for (const method of ['POST', 'PUT', 'PATCH', 'DELETE'] as const) {
    await assert.rejects(
      client.request({ path, method, body, parse }),
      rejection('missing-csrf'),
    );
    await client.request({ path, method, body, parse, csrf });
  }
  assert.equal(calls, 4);
});

test('invalid token headers, GET bodies, timeouts and unserializable bodies fail before fetch', async () => {
  let calls = 0;
  const client = createAdminTransport(async () => {
    calls++;
    return Response.json({ ready: true });
  });
  for (const token of [
    { headerName: 'Authorization', value: 'token' },
    { headerName: 'Content-Type', value: 'token' },
    { headerName: 'X-HTTP-Method-Override', value: 'DELETE' },
    { headerName: 'X-CSRF-TOKEN\r\nOther', value: 'token' },
    { headerName: 'X-CSRF-TOKEN', value: '' },
    { headerName: 'X-CSRF-TOKEN', value: 'one\r\ntwo' },
    { headerName: 'X-CSRF-TOKEN', value: '한글' },
    { headerName: 'X-CSRF-TOKEN', value: 'a'.repeat(4097) },
  ]) {
    await assert.rejects(
      client.request({ path, method: 'POST', parse, csrf: token }),
      rejection('invalid-request'),
    );
  }
  await assert.rejects(
    client.request({ path, body: {}, parse }),
    rejection('invalid-request'),
  );
  for (const timeoutMs of [0, -1, 1.5, NaN, Infinity, 120_001]) {
    await assert.rejects(
      client.request({ path, parse, timeoutMs }),
      rejection('invalid-request'),
    );
  }
  await assert.rejects(
    client.request({ path, method: 'POST', body: 1n, parse, csrf }),
    rejection('invalid-request'),
  );
  assert.equal(calls, 0);
});

test('HTTP status is authoritative and server diagnostics are discarded', async () => {
  for (const [status, kind] of [
    [401, 'unauthenticated'],
    [403, 'forbidden'],
    [409, 'http'],
    [503, 'http'],
  ] as const) {
    let calls = 0;
    const client = createAdminTransport(async () => {
      calls++;
      return Response.json(
        {
          status,
          code: 'EXAMPLE_CODE',
          detail: secret,
          title: secret,
          errors: [{ message: secret }],
        },
        {
          status,
          headers: { 'Content-Type': 'application/problem+json' },
        },
      );
    });
    await assert.rejects(
      client.request({ path, method: 'POST', csrf, parse }),
      rejection(kind, {
        status,
        code: 'EXAMPLE_CODE',
        outcomeUnknown: status >= 500,
      }),
    );
    assert.equal(calls, 1);
  }
});

test('malformed error JSON and mismatched Problem fields still preserve 401/403', async () => {
  for (const status of [401, 403]) {
    for (const body of [
      '<html>login</html>',
      '{',
      JSON.stringify({ status: 200, code: 'WRONG_STATUS' }),
      JSON.stringify({ status, code: secret }),
    ]) {
      const client = createAdminTransport(
        async () =>
          new Response(body, {
            status,
            headers: { 'Content-Type': 'application/problem+json' },
          }),
      );
      await assert.rejects(
        client.request({ path, parse }),
        rejection(status === 401 ? 'unauthenticated' : 'forbidden', {
          status,
          code: undefined,
        }),
      );
    }
  }
});

test('HTML, redirects, invalid JSON, and parser failures cannot become successful data', async () => {
  for (const response of [
    Response.error(),
    new Response('<html>login</html>', {
      headers: { 'Content-Type': 'text/html' },
    }),
    new Response(null, {
      status: 302,
      headers: { Location: 'https://example.com/login' },
    }),
    new Response('{', { headers: { 'Content-Type': 'application/json' } }),
    Response.json({ ready: false, detail: secret }),
    Response.json(
      { code: 'EXAMPLE_CODE' },
      { headers: { 'Content-Type': 'application/problem+json' } },
    ),
  ]) {
    const client = createAdminTransport(async () => response);
    await assert.rejects(
      client.request({ path, parse }),
      rejection('invalid-response'),
    );
  }
});

test('204 is accepted only through the caller parser', async () => {
  const client = createAdminTransport(
    async () => new Response(null, { status: 204 }),
  );
  assert.equal(
    await client.request({
      path,
      parse: (value) => {
        assert.equal(value, undefined);
        return 'no-content';
      },
    }),
    'no-content',
  );
});

test('unread response bodies are cancelled after media type rejection', async () => {
  let discarded = false;
  const client = createAdminTransport(
    async () =>
      new Response(
        new ReadableStream({
          cancel() {
            discarded = true;
          },
        }),
        { headers: { 'Content-Type': 'text/html' } },
      ),
  );
  await assert.rejects(
    client.request({ path, parse }),
    rejection('invalid-response'),
  );
  assert.equal(discarded, true);
});

test('an observed 403 remains forbidden when its body times out', async () => {
  const client = createAdminTransport(
    async (_input, init) =>
      new Response(
        new ReadableStream({
          start(controller) {
            init?.signal?.addEventListener(
              'abort',
              () => controller.error(new Error(secret)),
              { once: true },
            );
          },
        }),
        {
          status: 403,
          headers: { 'Content-Type': 'application/problem+json' },
        },
      ),
  );
  await assert.rejects(
    client.request({ path, method: 'POST', csrf, parse, timeoutMs: 20 }),
    rejection('forbidden', { status: 403, outcomeUnknown: false }),
  );
});

test('network failure never retries a mutation and marks its result unknown', async () => {
  let calls = 0;
  const client = createAdminTransport(async () => {
    calls++;
    throw new Error(secret);
  });
  await assert.rejects(
    client.request({ path, method: 'POST', csrf, parse }),
    rejection('network', { outcomeUnknown: true }),
  );
  assert.equal(calls, 1);
  await assert.rejects(
    client.request({ path, parse }),
    rejection('network', { outcomeUnknown: false }),
  );
});

test('pre-cancelled requests never dispatch and late responses cannot revive cancelled work', async () => {
  let calls = 0;
  const cancelled = new AbortController();
  cancelled.abort(secret);
  const client = createAdminTransport(async () => {
    calls++;
    return Response.json({ ready: true });
  });
  await assert.rejects(
    client.request({ path, parse, signal: cancelled.signal }),
    rejection('cancelled', { outcomeUnknown: false }),
  );
  assert.equal(calls, 0);

  const late = new AbortController();
  const lateClient = createAdminTransport(async () => {
    late.abort(secret);
    return Response.json({ ready: true });
  });
  await assert.rejects(
    lateClient.request({
      path,
      parse,
      method: 'POST',
      csrf,
      signal: late.signal,
    }),
    rejection('cancelled', { outcomeUnknown: true }),
  );
});

test('timeout covers response body reading and does not retry', async () => {
  let calls = 0;
  const client = createAdminTransport(async (_input, init) => {
    calls++;
    return new Response(
      new ReadableStream({
        start(controller) {
          init?.signal?.addEventListener(
            'abort',
            () => controller.error(new Error(secret)),
            { once: true },
          );
        },
      }),
      { headers: { 'Content-Type': 'application/json' } },
    );
  });
  await assert.rejects(
    client.request({ path, parse, timeoutMs: 20, method: 'POST', csrf }),
    rejection('timeout', { outcomeUnknown: true }),
  );
  assert.equal(calls, 1);
});

test('cancellation during parsing is distinguished from invalid data', async () => {
  const controller = new AbortController();
  const client = createAdminTransport(async () =>
    Response.json({ ready: true }),
  );
  await assert.rejects(
    client.request({
      path,
      signal: controller.signal,
      parse: () => {
        controller.abort(secret);
        return { ready: true };
      },
    }),
    rejection('cancelled'),
  );
});
