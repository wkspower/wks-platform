import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

import '@/index.css';

const rootElement = document.getElementById('root');
if (!rootElement) {
  // Story 1.3 fix for Story 1.1 deferred item: surface a visible
  // fallback instead of a blank screen when index.html is misconfigured
  // (the React tree never mounts in this case, so the fallback must be
  // plain DOM, not a React component).
  document.body.innerHTML =
    '<main style="min-height:100vh;display:grid;place-items:center;font-family:system-ui,sans-serif;color:#0B1437"><div style="text-align:center"><h1>WKS Platform failed to start</h1><p>Missing #root element in index.html. Please contact support.</p></div></main>';
  throw new Error('Missing #root element in index.html');
}

createRoot(rootElement).render(
  <StrictMode>
    <div />
  </StrictMode>,
);
