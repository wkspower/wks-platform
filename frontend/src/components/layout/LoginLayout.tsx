import { Outlet } from 'react-router-dom';

export function LoginLayout() {
  return (
    <main
      id="main"
      tabIndex={-1}
      className="flex min-h-screen items-center justify-center bg-[var(--brand-navy)] p-[var(--space-6)] focus:outline-none"
    >
      <Outlet />
    </main>
  );
}
