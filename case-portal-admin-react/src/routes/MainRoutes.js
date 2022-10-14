import { lazy } from 'react';

// project import
import Loadable from 'components/Loadable';
import MainLayout from 'layout/MainLayout';

import { BpmEngineList } from 'views/bpmEngine/bpmEngineList/bpmEngineList';
import { CaseDefList } from 'views/caseDef/caseDefList/caseDefList';
import { FormList } from 'views/form/formList';

// render - dashboard
const DashboardDefault = Loadable(lazy(() => import('pages/dashboard')));

// ==============================|| MAIN ROUTING ||============================== //

const MainRoutes = {
    path: '/',
    element: <MainLayout />,
    children: [
        {
            path: '/',
            element: <DashboardDefault />
        },

        {
            path: 'home',
            element: <DashboardDefault />
        },

        {
            path: 'system',
            children: [
                {
                    path: 'look-and-feel',
                    element: <DashboardDefault />
                },
                {
                    path: 'email',
                    element: <DashboardDefault />
                },
                {
                    path: 'notification',
                    element: <DashboardDefault />
                },
                {
                    path: 'integration',
                    element: <DashboardDefault />
                },
                {
                    path: 'webhook',
                    element: <DashboardDefault />
                },
                {
                    path: 'environment',
                    element: <DashboardDefault />
                },
                {
                    path: 'job',
                    element: <DashboardDefault />
                },
                {
                    path: 'log',
                    element: <DashboardDefault />
                },
                {
                    path: 'monitoring',
                    element: <DashboardDefault />
                },
                {
                    path: 'languages',
                    element: <DashboardDefault />
                }
            ]
        },

        {
            path: 'settings',
            children: [
                {
                    path: 'company-settings',
                    element: <DashboardDefault />
                },
                {
                    path: 'multi-tenancy',
                    element: <DashboardDefault />
                },
                {
                    path: 'identity',
                    element: <DashboardDefault />
                },
                {
                    path: 'security',
                    element: <DashboardDefault />
                },
                {
                    path: 'privacy-settings',
                    element: <DashboardDefault />
                },
                {
                    path: 'user-engagement',
                    element: <DashboardDefault />
                }
            ]
        },
        {
            path: 'case-life-cycle',
            children: [
                {
                    path: 'case-definition',
                    element: <CaseDefList />
                },
                {
                    path: 'data-domain',
                    element: <DashboardDefault />
                },
                {
                    path: 'process-engine',
                    element: <BpmEngineList />
                },
                {
                    path: 'task-definition',
                    element: <DashboardDefault />
                },
                {
                    path: 'form',
                    element: <FormList />
                },
                {
                    path: 'export',
                    element: <DashboardDefault />
                }
            ]
        }
    ]
};

export default MainRoutes;
