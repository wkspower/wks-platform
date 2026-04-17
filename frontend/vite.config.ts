import path from 'node:path';

import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    // Never inline font files as data URLs — keep them as separate
    // fingerprinted assets so the browser can cache them independently
    // and the CI font-count assertion (Story 1.3 AC #13) sees them.
    assetsInlineLimit: (filePath) => (filePath.endsWith('.woff2') ? false : undefined),
  },
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
