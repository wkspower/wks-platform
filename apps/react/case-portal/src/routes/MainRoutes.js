import { lazy } from 'react'
import Loadable from 'components/Loadable'
import MainLayout from 'layout/MainLayout'
import { CaseStatus } from 'common/caseStatus'
import { CaseList } from 'views/caseList/caseList'
import { RecordList } from 'views/record/recordList'
import { TaskList } from 'views/taskList/taskList'
import { CaseDefList } from 'views/management/caseDef/caseDefList/caseDefList'
import { ProcessDefList } from 'views/management/processDef/processDefList'
import { FormList } from 'views/management/form/formList'
import { RecordTypeList } from 'views/management/recordType/recordTypeList'
import { QueueList } from 'views/management/queue/queueList'

const ManagamentDefault = Loadable(lazy(() => import('../views/management')))
const DashboardDefault = Loadable(lazy(() => import('../views/dashboard')))

export const MainRoutes = (
  keycloak,
  authenticated,
  recordsTypes,
  casesDefinitions,
) => {
  let routes = {
    path: '/',
    element: <MainLayout keycloak={keycloak} authenticated={authenticated} />,
    children: [
      {
        path: '/',
        element: <DashboardDefault />,
      },

      {
        path: 'home',
        element: <DashboardDefault />,
      },
      {
        path: 'case-list',
        children: [
          {
            path: 'cases',
            element: <CaseList />,
          },
          {
            path: 'wip-cases',
            element: <CaseList status={CaseStatus.WipCaseStatus.description} />,
          },
          {
            path: 'closed-cases',
            element: (
              <CaseList status={CaseStatus.ClosedCaseStatus.description} />
            ),
          },
          {
            path: 'archived-cases',
            element: (
              <CaseList status={CaseStatus.ArchivedCaseStatus.description} />
            ),
          },
        ],
      },
      {
        path: 'task-list',
        element: <TaskList />,
      },
      {
        path: 'system',
        children: [
          {
            path: 'look-and-feel',
            element: <ManagamentDefault />,
          },
        ],
      },
      {
        path: 'case-life-cycle',
        children: [
          {
            path: 'process-definition',
            element: <ProcessDefList />,
          },
          {
            path: 'case-definition',
            element: <CaseDefList />,
          },
          {
            path: 'record-type',
            element: <RecordTypeList />,
          },
          {
            path: 'form',
            element: <FormList />,
          },
          {
            path: 'queue',
            element: <QueueList />,
          },
        ],
      },
    ],
  }

  casesDefinitions.forEach((element) => {
    routes.children.push({
      path: 'case-list/' + element.id,
      element: <CaseList caseDefId={element.id} />,
    })
  })

  recordsTypes.forEach((element) => {
    routes.children.push({
      path: 'record-list/' + element.id,
      element: <RecordList recordTypeId={element.id} />,
    })
  })

  return routes
}
