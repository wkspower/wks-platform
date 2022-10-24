import { lazy } from 'react';

// project import
import Loadable from 'components/Loadable';
import MainLayout from 'layout/MainLayout';

import { CaseStatus } from 'common/caseStatus';
import { CaseList } from 'views/caseList/caseList';
import { RecordList } from 'views/record/recordList';
import { TaskList } from 'views/taskList/taskList';

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
    ]
};

fetch('http://localhost:8081/record-type/')
    .then((response) => response.json())
    .then((data) => {
        data.forEach((element) => {
            MainRoutes.children.push({
                path: 'record-list/' + element.id,
                element: <RecordList recordTypeId={element.id} />
            });
        });
    })
    .catch((err) => {
        console.log(err.message);
    });

export default MainRoutes;
