import { builtinModules } from 'node:module';
import js from '@eslint/js';
import { defineConfig, globalIgnores } from 'eslint/config';
import globals from 'globals';
import tseslint from 'typescript-eslint';
import hooks from 'eslint-plugin-react-hooks';
import refresh from 'eslint-plugin-react-refresh';

const browserSources = [
  'apps/*/src/**/*.{ts,tsx}',
  'packages/ui/src/**/*.{ts,tsx}',
  'apps/*/public/**/*.js',
];
const typedSources = [
  'apps/admin/src/**/*.{ts,tsx}',
  'packages/ui/src/**/*.{ts,tsx}',
];

function browserImports(extraPatterns = []) {
  return [
    'error',
    {
      paths: builtinModules.map((name) => ({
        name,
        message: 'Node.js modules do not belong in browser code.',
      })),
      patterns: [
        {
          group: ['node:*'],
          message: 'Node.js modules do not belong in browser code.',
        },
        ...extraPatterns,
      ],
    },
  ];
}

export default defineConfig([
  globalIgnores([
    '**/dist/**',
    '**/node_modules/**',
    'playwright-report/**',
    'test-results/**',
  ]),
  {
    files: ['**/*.{js,ts,tsx}'],
    extends: [js.configs.recommended],
    rules: {
      eqeqeq: ['error', 'always'],
      'no-eval': 'error',
      'no-implied-eval': 'error',
      'no-new-func': 'error',
    },
  },
  {
    files: ['**/*.{ts,tsx}'],
    extends: [tseslint.configs.recommended],
  },
  {
    files: browserSources,
    languageOptions: { globals: globals.browser },
    rules: {
      'no-console': ['error', { allow: ['warn', 'error'] }],
      'no-restricted-globals': [
        'error',
        ...[
          'process',
          'Buffer',
          '__dirname',
          '__filename',
          'require',
          'module',
          'global',
        ].map((name) => ({
          name,
          message: 'Node.js globals do not belong in browser code.',
        })),
      ],
      'no-restricted-imports': browserImports(),
    },
  },
  {
    files: ['**/*.config.{js,ts}', 'tests/**/*.{js,ts}'],
    languageOptions: { globals: globals.node },
  },
  {
    files: typedSources,
    extends: [tseslint.configs.recommendedTypeCheckedOnly],
    languageOptions: {
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
      },
    },
  },
  {
    files: ['apps/*/src/**/*.{ts,tsx}', 'packages/ui/src/**/*.{ts,tsx}'],
    plugins: { 'react-hooks': hooks },
    rules: hooks.configs.recommended.rules,
  },
  {
    files: ['apps/*/src/**/*.tsx', 'packages/ui/src/**/*.tsx'],
    extends: [refresh.configs.vite],
  },
  {
    files: ['packages/ui/src/**/*.{ts,tsx}'],
    rules: {
      'no-restricted-imports': browserImports([
        {
          group: [
            'react-router',
            'react-router/**',
            'react-router-dom',
            'react-router-dom/**',
            'next',
            'next/**',
            '**/apps/**',
            '@/*',
            '@etude/admin',
            '@etude/admin/**',
            '@etude/storefront',
            '@etude/storefront/**',
            '@etude/cs',
            '@etude/cs/**',
          ],
          message: 'Shared UI must not depend on an app or its router.',
        },
      ]),
    },
  },
  ...[
    ['button', ['buttonVariants']],
    ['badge', ['badgeVariants']],
    ['tabs', ['tabsListVariants']],
    ['toast', ['toast', 'createToastManager', 'useToastManager']],
  ].map(([component, allowExportNames]) => ({
    files: ['packages/ui/src/components/' + component + '.tsx'],
    rules: {
      'react-refresh/only-export-components': [
        'error',
        { allowConstantExport: true, allowExportNames },
      ],
    },
  })),
]);
