import path from 'node:path';

import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

/**
 * Story 14.5 — B1: ensure the runtime theme override link loads AFTER bundled CSS.
 *
 * Vite injects fingerprinted <link> tags into <head> during the production build.
 * Whatever was already in index.html ends up BEFORE those injected links, so the
 * SI-provided :root overrides would lose the cascade to tokens.css defaults.
 *
 * This plugin strips the theme link from wherever Vite placed it and re-appends it
 * just before </head>, guaranteeing last-loaded wins for equal-specificity :root rules.
 */
const themeLinkLast = {
  name: 'theme-link-last',
  transformIndexHtml(html: string): string {
    const themeLink = '<link rel="stylesheet" href="/api/theme.css">';
    const themeLinkSelfClosing = '<link rel="stylesheet" href="/api/theme.css" />';
    // Remove all occurrences (Vite may or may not normalise self-closing tags)
    html = html.split(themeLink).join('');
    html = html.split(themeLinkSelfClosing).join('');
    // Re-inject just before </head> so it is the last stylesheet
    return html.replace('</head>', `  ${themeLink}\n  </head>`);
  },
};

export default defineConfig({
  plugins: [react(), tailwindcss(), themeLinkLast],
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
