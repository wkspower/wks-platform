import { createBrowserRouter, Navigate, Outlet, useLocation } from 'react-router-dom';

import { AppShell } from '@/components/chrome/AppShell';
import { Spinner } from '@/components/ui/Spinner';
import { AdminPage } from '@/pages/AdminPage';
import { CasesPage } from '@/pages/CasesPage';
import { CaseDetailPage } from '@/pages/CaseDetailPage';
import { DashboardPage } from '@/pages/DashboardPage';
import { FormPage } from '@/pages/FormPage';
import { LicenseStatusPage } from '@/pages/LicenseStatusPage';
import { LoginPage } from '@/pages/LoginPage';
import { MappingInspectorPage } from '@/pages/MappingInspectorPage';
import { NotFoundPage } from '@/pages/NotFoundPage';
import { TasksPage } from '@/pages/TasksPage';
import { useAuthStore } from '@/stores/authStore';

function RootRedirect() {
  const status = useAuthStore((s) => s.status);
  if (status === 'pending') {
    return (
      <div className="grid min-h-screen place-items-center">
        <Spinner className="size-6" />
      </div>
    );
  }
  return <Navigate to={status === 'authenticated' ? '/cases' : '/login'} replace />;
}

function RequireAuth({ requiredRole }: { requiredRole?: string }) {
  const status = useAuthStore((s) => s.status);
  const user = useAuthStore((s) => s.user);
  const location = useLocation();
  if (status === 'pending') {
    return (
      <div className="grid min-h-screen place-items-center">
        <Spinner className="size-6" />
      </div>
    );
  }
  if (status !== 'authenticated' || !user) {
    const returnTo = encodeURIComponent(location.pathname + location.search);
    return <Navigate to={`/login?returnTo=${returnTo}`} replace />;
  }
  if (requiredRole && !user.roles.includes(requiredRole)) {
    return <Navigate to="/cases" replace />;
  }
  return <Outlet />;
}

export const router = createBrowserRouter([
  { path: '/', element: <RootRedirect /> },
  { path: '/login', element: <LoginPage /> },
  {
    element: <RequireAuth />,
    children: [
      {
        element: <AppShell />,
        children: [
          { path: '/dashboard', element: <DashboardPage /> },
          { path: '/cases', element: <CasesPage /> },
          { path: '/cases/:caseId', element: <CasesPage /> },
          { path: '/cases/:caseId/full', element: <CaseDetailPage /> },
          { path: '/cases/:caseId/forms/:formId', element: <FormPage /> },
          { path: '/tasks', element: <TasksPage /> },
        ],
      },
    ],
  },
  {
    element: <RequireAuth requiredRole="admin" />,
    children: [
      {
        element: <AppShell />,
        children: [
          { path: '/admin', element: <AdminPage /> },
          { path: '/admin/license', element: <LicenseStatusPage /> },
          { path: '/admin/mapping-inspector/:caseTypeId', element: <MappingInspectorPage /> },
        ],
      },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
]);
