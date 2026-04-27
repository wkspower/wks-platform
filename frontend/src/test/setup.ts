import '@testing-library/jest-dom/vitest';

import { afterAll, afterEach, beforeAll } from 'vitest';

import { server } from './server';

// jsdom polyfills for Radix UI primitives (Select, Dialog, etc.) that probe Pointer Events APIs
// not implemented by jsdom. Without these, Radix throws "target.hasPointerCapture is not a
// function" / "Element.scrollIntoView is not a function" the moment a Radix combobox opens.
if (typeof window !== 'undefined' && !window.HTMLElement.prototype.hasPointerCapture) {
  window.HTMLElement.prototype.hasPointerCapture = () => false;
  window.HTMLElement.prototype.releasePointerCapture = () => {};
  window.HTMLElement.prototype.setPointerCapture = () => {};
}
if (typeof window !== 'undefined' && !window.HTMLElement.prototype.scrollIntoView) {
  window.HTMLElement.prototype.scrollIntoView = () => {};
}

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
