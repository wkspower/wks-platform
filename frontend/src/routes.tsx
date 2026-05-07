import { createBrowserRouter, Navigate, Outlet, useSearchParams } from 'react-router-dom';

import { NotFoundPage } from '@/components/errors/NotFoundPage';
import { AppShell } from '@/components/layout/AppShell';
import { LoginLayout } from '@/components/layout/LoginLayout';
import { RequireAuth } from '@/components/routing/RequireAuth';
import { RootRedirect } from '@/components/routing/RootRedirect';
import { AdminPage } from '@/pages/AdminPage';
import { CasesPage } from '@/pages/CasesPage';
import { DevConsolePage } from '@/pages/DevConsolePage';
import { FormPage } from '@/pages/FormPage';
import { LoginPage, safeReturnTo } from '@/pages/LoginPage';
import { TasksPage } from '@/pages/TasksPage';
import { useAuthStore } from '@/stores/authStore';

function LoginGate() {
  const status = useAuthStore((s) => s.status);
  const [params] = useSearchParams();
  if (status === 'authenticated') {
    // Honor `?returnTo` when an already-authenticated user hits /login,
    // so deep-link flows (e.g. copy/paste /login?returnTo=/admin) land
    // on the intended page rather than defaulting to /cases.
    return <Navigate to={safeReturnTo(params.get('returnTo'))} replace />;
  }
  return <Outlet />;
}

export const router = createBrowserRouter([
  { path: '/', element: <RootRedirect /> },
  {
    element: <LoginLayout />,
    children: [
      {
        element: <LoginGate />,
        children: [{ path: '/login', element: <LoginPage /> }],
      },
    ],
  },
  {
    element: (
      <RequireAuth>
        <AppShell />
      </RequireAuth>
    ),
    children: [
      { path: '/cases', element: <CasesPage /> },
      { path: '/cases/:caseId', element: <CasesPage /> },
      // Story 5.2 — single-page form renderer route.
      { path: '/cases/:caseId/forms/:formId', element: <FormPage /> },
      { path: '/tasks', element: <TasksPage /> },
      {
        path: '/admin',
        element: (
          <RequireAuth requiredRole="admin">
            <AdminPage />
          </RequireAuth>
        ),
      },
      {
        path: '/dev',
        element: (
          <RequireAuth requiredRole="developer">
            <DevConsolePage />
          </RequireAuth>
        ),
      },
      // Catch-all is nested under AppShell so authenticated 404s keep
      // the sidebar + topbar chrome (AC #20). Unauthenticated users
      // flow through RequireAuth → /login?returnTo=<path> before ever
      // reaching this route, so no separate unauthenticated 404 is
      // needed (they land on the login screen instead).
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
