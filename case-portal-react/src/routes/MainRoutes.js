import { lazy } from 'react';

// project import
import Loadable from 'components/Loadable';
import MainLayout from 'layout/MainLayout';

import { CaseList } from 'views/caseList/caseList';
import { TaskList } from 'views/taskList/taskList';
import { CaseStatus } from 'common/caseStatus';

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
            path: 'case-list',
            children: [
                {
                    path: 'cases',
                    element: <CaseList />
                },
                {
                    path: 'wip-cases',
                    element: <CaseList status={CaseStatus.WipCaseStatus.description} />
                },
                {
                    path: 'closed-cases',
                    element: <CaseList status={CaseStatus.ClosedCaseStatus.description} />
                },
                {
                    path: 'archived-cases',
                    element: <CaseList status={CaseStatus.ArchivedCaseStatus.description} />
                }
            ]
        },
        {
            path: 'task-list',
            element: <TaskList />
        }
        // {
        //     path: 'utils',
        //     children: [
        //         {
        //             path: 'util-typography',
        //             element: <UtilsTypography />
        //         }
        //     ]
        // },
        // {
        //     path: 'utils',
        //     children: [
        //         {
        //             path: 'util-color',
        //             element: <UtilsColor />
        //         }
        //     ]
        // },
        // {
        //     path: 'utils',
        //     children: [
        //         {
        //             path: 'util-shadow',
        //             element: <UtilsShadow />
        //         }
        //     ]
        // },
        // {
        //     path: 'icons',
        //     children: [
        //         {
        //             path: 'tabler-icons',
        //             element: <UtilsTablerIcons />
        //         }
        //     ]
        // },
        // {
        //     path: 'icons',
        //     children: [
        //         {
        //             path: 'material-icons',
        //             element: <UtilsMaterialIcons />
        //         }
        //     ]
        // },
        // {
        //     path: 'sample-page',
        //     element: <SamplePage />
        // }
    ]
};

export default MainRoutes;
