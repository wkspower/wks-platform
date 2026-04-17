import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';

import { NotFoundPage } from '@/components/errors/NotFoundPage';
import { AppShell } from '@/components/layout/AppShell';
import { LoginLayout } from '@/components/layout/LoginLayout';
import { RequireAuth } from '@/components/routing/RequireAuth';
import { RootRedirect } from '@/components/routing/RootRedirect';
import { AdminPage } from '@/pages/AdminPage';
import { CasesPage } from '@/pages/CasesPage';
import { DevConsolePage } from '@/pages/DevConsolePage';
import { LoginPage } from '@/pages/LoginPage';
import { TasksPage } from '@/pages/TasksPage';
import { useAuthStore } from '@/stores/authStore';

function LoginGate() {
  const status = useAuthStore((s) => s.status);
  if (status === 'authenticated') return <Navigate to="/cases" replace />;
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
    ],
  },
  { path: '*', element: <NotFoundPage /> },
]);
