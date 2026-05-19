import path from 'node:path';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': path.resolve(import.meta.dirname, './src') },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    css: false,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'lcov'],
      include: ['src/**/*.{ts,tsx}'],
      exclude: [
        'src/**/*.d.ts',
        'src/main.tsx',
        'src/routes.tsx',
        'src/vite-env.d.ts',
        'src/test/**',
        'src/**/*.test.{ts,tsx}',
      ],
      // Coverage ratchet: raise these only upward, never down.
      // Baseline (2026-05-19): 4 test files covering api/client, authStore,
      // LoginPage, CasesTable. Numbers are set just below the actual measured
      // values so new test additions ratchet up; never lower these.
      thresholds: {
        lines: 12,
        statements: 12,
        functions: 25,
        branches: 45,
      },
    },
  },
});
