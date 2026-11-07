import assert from 'node:assert/strict';
import { fileURLToPath } from 'node:url';
import test from 'node:test';
import { ESLint } from 'eslint';

const eslint = new ESLint({
  cwd: fileURLToPath(new URL('../..', import.meta.url)),
});
const appFile = 'apps/admin/src/app/routes.ts';
const uiFile = 'packages/ui/src/lib/utils.ts';

async function messages(code, filePath) {
  const [result] = await eslint.lintText(code, { filePath });
  assert.equal(result.fatalErrorCount, 0, JSON.stringify(result.messages));
  return result.messages;
}

async function rejects(code, filePath, ruleId) {
  const actual = await messages(code, filePath);
  assert.ok(
    actual.some((message) => message.ruleId === ruleId),
    `Expected ${ruleId}: ${JSON.stringify(actual)}`,
  );
}

test('browser code rejects Node globals and prefixed or bare built-ins', async () => {
  await rejects('process.cwd();', appFile, 'no-restricted-globals');
  await rejects('Buffer.from("data");', uiFile, 'no-restricted-globals');
  for (const name of ['node:fs/promises', 'fs']) {
    await rejects(`import '${name}';`, appFile, 'no-restricted-imports');
  }
});

test('Node config and browser scripts have separate globals', async () => {
  assert.deepEqual(
    await messages('export default process.cwd();', 'eslint.config.js'),
    [],
  );
  assert.deepEqual(
    await messages(
      'window.localStorage.getItem("theme");',
      'apps/admin/public/theme.js',
    ),
    [],
  );
  await rejects('window.location.reload();', 'eslint.config.js', 'no-undef');
  await rejects(
    'process.cwd();',
    'apps/admin/public/theme.js',
    'no-restricted-globals',
  );
});

test('shared UI rejects app aliases, relative app paths and routers', async () => {
  for (const name of [
    '@etude/admin',
    '@etude/storefront/pages',
    '@etude/cs',
    '@/app/App',
    '../../../apps/admin/src/app/App',
    'react-router-dom',
    'react-router/dom',
    'next/navigation',
  ]) {
    await rejects(`import '${name}';`, uiFile, 'no-restricted-imports');
  }
  assert.deepEqual(
    await messages(
      'import { cn } from "cn"; export const label = cn("label");',
      uiFile,
    ),
    [],
  );
});

test('typed lint catches unhandled promises and accepts awaited work', async () => {
  await rejects(
    'Promise.resolve("unhandled");',
    appFile,
    '@typescript-eslint/no-floating-promises',
  );
  assert.deepEqual(
    await messages(
      'export async function run() { await Promise.resolve("handled"); }',
      appFile,
    ),
    [],
  );
});

test('React hooks still reject conditional calls', async () => {
  await rejects(
    'import { useState } from "react"; export function Demo({ visible }: { visible: boolean }) { if (visible) { useState(0); } return null; }',
    'apps/admin/src/app/App.tsx',
    'react-hooks/rules-of-hooks',
  );
});

test('Fast Refresh exceptions apply only to the matching official component', async () => {
  const example =
    'export const buttonVariants = () => "button"; export function Button() { return null; }';
  assert.deepEqual(
    await messages(example, 'packages/ui/src/components/button.tsx'),
    [],
  );
  await rejects(
    example,
    'apps/admin/src/app/App.tsx',
    'react-refresh/only-export-components',
  );
});

test('runtime source rejects eval and debug logging', async () => {
  await rejects('eval("1");', appFile, 'no-eval');
  await rejects('console.log("debug");', appFile, 'no-console');
});
