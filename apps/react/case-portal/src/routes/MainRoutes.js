import { lazy } from 'react'
import Loadable from 'components/Loadable'
import MainLayout from 'layout/MainLayout'
import TurnaroundPlanTable from 'components/data-tables/TurnaroundPlanTable'
// import AssessmentForm from 'components/data-tables/AssesmentForm/AssessmentContext'
import ProductionvolumeData from 'components/data-tables/ProductionVoluemData'
import BusinessDemand from 'components/data-tables/BusinessDemand'
import ShutDown from 'components/data-tables/ShutDown'
import SlowDown from 'components/data-tables/Slowdown'

// import { CaseStatus } from 'common/caseStatus'
// import { CaseList } from 'views/caseList/caseList'
// import { RecordList } from 'views/record/recordList'
// import { TaskList } from 'views/taskList/taskList'
// import { CaseDefList } from 'views/management/caseDef/caseDefList/caseDefList'
// import { ProcessDefList } from 'views/management/processDef/processDefList'
// import { FormList } from 'views/management/form/formList'
// import { RecordTypeList } from 'views/management/recordType/recordTypeList'
// import { QueueList } from 'views/management/queue/queueList'
import FeedStockAvailability from 'components/data-tables/FeedStockavailability'
import NormalOpNormsScreen from 'components/data-tables/NormsScreens/NormalOpNorms'
import ShutdownNorms from 'components/data-tables/NormsScreens/ShutdownNorms'
import MaintenanceTable from 'components/data-tables/MaintenanceTable'
import ConsumptionNorms from 'components/data-tables/ConsumptionNorms'
import ProductionNorms from 'components/data-tables/ProductionNorms'
// import SelectivityData from 'components/data-tables/SelectivityData'
import FiveTables from 'components/data-tables/ProductMixTable'
import ConfigurationTable from 'components/data-tables/ConfigurationTable/index'

const ManagamentDefault = Loadable(lazy(() => import('../views/management')))
const DashboardDefault = Loadable(lazy(() => import('../views/dashboard')))

export const MainRoutes = (
  keycloak,
  authenticated,
  // recordsTypes,
  // casesDefinitions,
) => {
  let routes = {
    path: '/',
    element: <MainLayout keycloak={keycloak} authenticated={authenticated} />,
    children: [
      {
        path: '/',
        element: <BusinessDemand />,
      },

      {
        path: 'home',
        element: <DashboardDefault />,
      },
      // {
      //   path: 'case-list',
      //   children: [
      //     {
      //       path: 'cases',
      //       element: <CaseList />,
      //     },
      //     {
      //       path: 'wip-cases',
      //       element: <CaseList status={CaseStatus.WipCaseStatus.description} />,
      //     },
      //     {
      //       path: 'closed-cases',
      //       element: (
      //         <CaseList status={CaseStatus.ClosedCaseStatus.description} />
      //       ),
      //     },
      //     {
      //       path: 'archived-cases',
      //       element: (
      //         <CaseList status={CaseStatus.ArchivedCaseStatus.description} />
      //       ),
      //     },
      //   ],
      // },
      // {
      //   path: 'task-list',
      //   element: <TaskList />,
      // },
      {
        path: 'system',
        children: [
          {
            path: 'look-and-feel',
            element: <ManagamentDefault />,
          },
        ],
      },

      // {
      //   path: 'case-life-cycle',
      //   children: [
      //     {
      //       path: 'process-definition',
      //       element: <ProcessDefList />,
      //     },
      //     {
      //       path: 'case-definition',
      //       element: <CaseDefList />,
      //     },
      //     {
      //       path: 'record-type',
      //       element: <RecordTypeList />,
      //     },
      //     {
      //       path: 'form',
      //       element: <FormList />,
      //     },
      //     {
      //       path: 'queue',
      //       element: <QueueList />,
      //     },
      //   ],
      // },

      {
        path: 'production-norms-plan',
        children: [
          {
            path: 'business-demand',
            element: <BusinessDemand />,
          },
          {
            path: 'production-volume-data',
            element: <ProductionvolumeData />,
          },
          {
            path: 'maintenance-details',
            element: <MaintenanceTable />,
          },
          {
            path: 'consumption-aop',
            element: <ConsumptionNorms />,
          },
          {
            path: 'production-aop',
            element: <ProductionNorms />,
          },
          {
            path: 'configuration',
            element: <ConfigurationTable />,
            // element: <SelectivityData />,
          },
          {
            path: 'normal-op-norms',
            element: <NormalOpNormsScreen />,
          },
          {
            path: 'shutdown-norms',
            element: <ShutdownNorms />,
          },

          {
            path: 'shutdown-plan',
            element: <ShutDown />,
          },
          {
            path: 'slowdown-plan',
            element: <SlowDown />,
          },
          {
            path: 'turnaround-plan',
            element: <TurnaroundPlanTable />,
          },
          {
            path: 'feed-stock-availability',
            element: <FeedStockAvailability />,
          },
        ],
      },
      {
        path: 'functions',
        children: [
          { path: 'safety', element: <DashboardDefault /> },
          { path: 'reliability', element: <DashboardDefault /> },
        ],
      },
      {
        path: 'reports',
        children: [
          { path: 'contribution-report', element: <DashboardDefault /> },
          { path: 'previous-fy-aop-result', element: <DashboardDefault /> },
          { path: 'previous-fy-aop-result', element: <DashboardDefault /> },
          { path: 'mat-bal-sheet', element: <DashboardDefault /> },
        ],
      },
      {
        path: 'workflow',
        element: <FiveTables />,
      },
    ],
  }

  // casesDefinitions.forEach((element) => {
  //   routes.children.push({
  //     path: 'case-list/' + element.id,
  //     element: <CaseList caseDefId={element.id} />,
  //   })
  // })

  // recordsTypes.forEach((element) => {
  //   routes.children.push({
  //     path: 'record-list/' + element.id,
  //     element: <RecordList recordTypeId={element.id} />,
  //   })
  // })

  return routes
}
