import js from '@eslint/js';
import tsPlugin from '@typescript-eslint/eslint-plugin';
import tsParser from '@typescript-eslint/parser';
import importPlugin from 'eslint-plugin-import';
import reactPlugin from 'eslint-plugin-react';
import reactHooksPlugin from 'eslint-plugin-react-hooks';

// Story 1.3 AC #4: design-token discipline is enforced via two
// no-restricted-syntax rules instead of eslint-plugin-tailwindcss
// (the plugin is not yet compatible with Tailwind 4's CSS-first
// theme model). The rules are scoped narrowly so tests and the
// tokens source folder can still use literal hex.
const noLiteralJsxText = {
  selector: 'JSXText[value=/\\S/]',
  message:
    'Literal JSX text is banned — wrap user-visible strings in t(\'...\') so they land in the i18n bundle.',
};

const noRawHexInStyles = {
  selector: "Literal[value=/^#[0-9a-fA-F]{3,8}$/]",
  message:
    'Raw hex color literals are banned — reference a design token via Tailwind utilities (bg-primary) or arbitrary value (bg-[var(--token)]).',
};

const noRawPxInStyleProp = {
  selector:
    "JSXAttribute[name.name='style'] Property[key.name=/^(margin|padding|width|height|top|right|bottom|left|gap|fontSize|lineHeight|borderRadius|borderWidth)$/] Literal[value=/^[0-9]+px$/]",
  message:
    'Raw px values in style props are banned — use Tailwind utilities or reference --space-* / --radius-* tokens.',
};

export default [
  {
    ignores: ['dist/**', 'node_modules/**', 'coverage/**'],
  },
  js.configs.recommended,
  {
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        ecmaFeatures: { jsx: true },
      },
      globals: {
        // Browser
        window: 'readonly',
        document: 'readonly',
        console: 'readonly',
        fetch: 'readonly',
        Headers: 'readonly',
        Response: 'readonly',
        Request: 'readonly',
        FormData: 'readonly',
        URL: 'readonly',
        URLSearchParams: 'readonly',
        EventTarget: 'readonly',
        CustomEvent: 'readonly',
        Event: 'readonly',
        localStorage: 'readonly',
        sessionStorage: 'readonly',
        history: 'readonly',
        location: 'readonly',
        navigator: 'readonly',
        setTimeout: 'readonly',
        clearTimeout: 'readonly',
        setInterval: 'readonly',
        clearInterval: 'readonly',
        queueMicrotask: 'readonly',
        HTMLElement: 'readonly',
        HTMLInputElement: 'readonly',
        HTMLAnchorElement: 'readonly',
        HTMLButtonElement: 'readonly',
        HTMLDivElement: 'readonly',
        Node: 'readonly',
        // Node-ish (used by tests + ts files that read fs)
        __dirname: 'readonly',
        __filename: 'readonly',
        process: 'readonly',
        Buffer: 'readonly',
        // DOM lib type aliases (TS removes them at compile time, but
        // ESLint sees them at parse time as identifiers).
        RequestInit: 'readonly',
        ResponseInit: 'readonly',
        EventListener: 'readonly',
        EventListenerOrEventListenerObject: 'readonly',
        AddEventListenerOptions: 'readonly',
      },
    },
    plugins: {
      '@typescript-eslint': tsPlugin,
      react: reactPlugin,
      'react-hooks': reactHooksPlugin,
      import: importPlugin,
    },
    settings: {
      react: { version: 'detect' },
      'import/resolver': {
        typescript: { alwaysTryTypes: true },
      },
    },
    rules: {
      'no-console': 'error',
      // The base rule double-flags TS interface members and overload params.
      // The @typescript-eslint version understands them.
      'no-unused-vars': 'off',
      '@typescript-eslint/no-unused-vars': [
        'error',
        { argsIgnorePattern: '^_', varsIgnorePattern: '^_' },
      ],
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn',
      'import/order': [
        'error',
        {
          groups: ['builtin', 'external', 'internal', 'parent', 'sibling', 'index'],
          'newlines-between': 'always',
          alphabetize: { order: 'asc', caseInsensitive: true },
        },
      ],
      'react/jsx-uses-react': 'off',
      'react/react-in-jsx-scope': 'off',
      'no-restricted-syntax': [
        'error',
        noLiteralJsxText,
        noRawHexInStyles,
        noRawPxInStyleProp,
      ],
    },
  },
  {
    // Tests, the styles folder (token definitions), and font assets are
    // legitimate places to use literal hex / JSX text.
    files: [
      '**/__tests__/**',
      '**/*.test.{ts,tsx}',
      'src/test/**',
      'src/styles/**',
    ],
    rules: {
      'no-restricted-syntax': 'off',
      'no-console': 'off',
    },
  },
];
