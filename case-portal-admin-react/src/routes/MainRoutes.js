import { lazy } from 'react';

// project imports
import MainLayout from 'layout/MainLayout';
import Loadable from 'ui-component/Loadable';
import { CaseDefList } from 'views/caseDef/caseDefList/caseDefList';
import { EventTypeList } from 'views/eventType/eventTypeList';
import { FormList } from 'views/form/formList';

// dashboard routing
const DashboardDefault = Loadable(lazy(() => import('views/dashboard/Default')));

// utilities routing
// const UtilsTypography = Loadable(lazy(() => import('views/utilities/Typography')));
// const UtilsColor = Loadable(lazy(() => import('views/utilities/Color')));
// const UtilsShadow = Loadable(lazy(() => import('views/utilities/Shadow')));
// const UtilsMaterialIcons = Loadable(lazy(() => import('views/utilities/MaterialIcons')));
// const UtilsTablerIcons = Loadable(lazy(() => import('views/utilities/TablerIcons')));

// sample page routing
// const SamplePage = Loadable(lazy(() => import('views/sample-page')));

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
            path: 'dashboard',
            children: [
                {
                    path: 'default',
                    element: <DashboardDefault />
                }
            ]
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
                    path: 'indentity',
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
            path: 'case-definition',
            element: <CaseDefList />
        },
        {
            path: 'event-type-definition',
            element: <EventTypeList />
        },
        {
            path: 'listener-type-definition',
            element: <DashboardDefault />
        },
        {
            path: 'data-domain',
            element: <DashboardDefault />
        },
        {
            path: 'process-engine',
            element: <DashboardDefault />
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
};

export default MainRoutes;
