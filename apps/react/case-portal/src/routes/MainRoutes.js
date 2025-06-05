import { lazy } from 'react'
import Loadable from 'components/Loadable'
import MainLayout from 'layout/MainLayout'
import TurnaroundPlanTable from 'components/data-tables/TurnaroundPlanTable'
// import AssessmentForm from 'components/data-tables/AssesmentForm/AssessmentContext'
import ProductionvolumeData from 'components/data-tables/ProductionVoluemData'
import BusinessDemand from 'components/data-tables/BusinessDemand'
import ShutDown from 'components/data-tables/ShutDown'
import SlowDown from 'components/data-tables/Slowdown'

import { CaseStatus } from 'common/caseStatus'
import { CaseList } from 'views/caseList/caseList'
// import { RecordList } from 'views/record/recordList'
import { TaskList } from 'views/taskList/taskList'
import { CaseDefList } from 'views/management/caseDef/caseDefList/caseDefList'
import { ProcessDefList } from 'views/management/processDef/processDefList'
import { FormList } from 'views/management/form/formList'
import { RecordTypeList } from 'views/management/recordType/recordTypeList'
import { QueueList } from 'views/management/queue/queueList'
import FeedStockAvailability from 'components/data-tables/FeedStockavailability'
import NormalOpNormsScreen from 'components/data-tables/NormsScreens/NormalOpNorms'
import ShutdownNorms from 'components/data-tables/NormsScreens/ShutdownNorms'
import MaintenanceTable from 'components/data-tables/MaintenanceTable'
import ConsumptionNorms from 'components/data-tables/ConsumptionNorms'
import ProductionNorms from 'components/data-tables/ProductionNorms'
// import SelectivityData from 'components/data-tables/SelectivityData'
// import FiveTables from 'components/data-tables/AOPWorkFlow/ProductMixTable'
import ConfigurationTable from 'components/data-tables/ConfigurationTable/index'
// import UserManagement from 'components/user-management/UserManagementTable'
import UserForm from 'components/user-management/UserForm'
import UserManagementTable from 'components/user-management/UserManagementTable'
import AssessmentForm from 'components/data-tables/AssesmentForm/AssessmentContext'
import SlowdownNorms from 'components/data-tables/NormsScreens/SlowdownNorms'
import TextSubmitComponent from 'components/user-management/TextSubmitComponent'
import WorkFlowMerge from 'components/data-tables/AOPWorkFlow/WorkFlowMerge'
import AnnualAopCost from 'components/data-tables/Reports/AnnualAopCost'
import PlantsProductionSummary from 'components/data-tables/Reports/PlantsProductionData'
// import MonthwiseProduction from 'components/data-tables/Reports/MonthWiseProduction'
import ProductionVolumeDataBasis from 'components/data-tables/Reports/ProductionVolumeDataBasis'
import MonthwiseProduction from 'components/data-tables/Reports/MonthwiseProduction'
import MonthwiseRawMaterial from 'components/data-tables/Reports/MonthwiseRawMaterial'
import NormsHistorianBasis from 'components/data-tables/Reports/NormsHistorianBasis'
import BusinessDemandKendo from 'components/kendo-data-tables/BusinessDemandKendo'
import CrackerConfig from 'components/kendo-data-tables/KendoConfigCrackerInput'
import DecokingConfig from 'components/kendo-data-tables/KendoConfigCrackerActivities'
import CrackerConfigOutput from 'components/kendo-data-tables/KendoConfigCrackerOutput'

const ManagamentDefault = Loadable(lazy(() => import('../views/management')))
const DashboardDefault = Loadable(lazy(() => import('../views/dashboard')))

export const MainRoutes = (
  keycloak,
  authenticated,
  // recordsTypes,
  casesDefinitions,
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

      {
        path: 'production-norms-plan',
        children: [
          {
            path: 'business-demand',
            element: <BusinessDemand />,
          },
          {
            path: 'business-demand-kendo',
            element: <BusinessDemandKendo />,
          },
          {
            path: 'configuration',
            element: <ConfigurationTable />,
            // element: <SelectivityData />,
          },
          {
            path: 'spyro-menu',
            children: [
              {
            path: 'spyro-input',
                element: <CrackerConfig keycloak={keycloak} />,
              },
              {
                path: 'spyro-output',
                element: <CrackerConfigOutput />,
          },
          {
            path: 'decoking-activities',
            element: <DecokingConfig />,
              },
            ],
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
            path: 'normal-op-norms',
            element: <NormalOpNormsScreen />,
          },
          {
            path: 'shutdown-norms',
            element: <ShutdownNorms />,
          },
          {
            path: 'slowdown-norms',
            element: <SlowdownNorms />,
          },
          // {
          //   path: 'slowdown-norms',
          //   element: <SlowdownNorms />,
          // },

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
          { path: 'reliability', element: <TextSubmitComponent /> },
        ],
      },
      {
        path: 'reports',
        children: [
          { path: 'aop-annual-cost-report', element: <AnnualAopCost /> },
          {
            path: 'production-volume-basis',
            element: <ProductionVolumeDataBasis />,
          },
          {
            path: 'norms-historian-basis',
            element: <NormsHistorianBasis />,
          },
          {
            path: 'plants-production',
            element: <PlantsProductionSummary />,
          },
          {
            path: 'monthwise-production',
            element: <MonthwiseProduction />,
          },
          {
            path: 'monthwise-raw-material',
            element: <MonthwiseRawMaterial />,
          },
          { path: 'previous-fy-aop-result', element: <DashboardDefault /> },
          { path: 'previous-fy-aop-result', element: <DashboardDefault /> },
          { path: 'mat-bal-sheet', element: <DashboardDefault /> },
        ],
      },
      {
        path: 'workflow',
        element: <WorkFlowMerge />,
        // element: <FiveTables />,
      },
      {
        path: 'user-management',
        element: <UserManagementTable keycloak={keycloak} />,
      },
      {
        path: 'user-form',
        element: <UserForm keycloak={keycloak} />,
      },
      {
        path: 'assessment-form',
        element: <AssessmentForm />,
      },
    ],
  }

  casesDefinitions?.forEach((element) => {
    routes.children.push({
      path: 'case-list/' + element.id,
      element: <CaseList caseDefId={element.id} />,
    })
  })

  // recordsTypes.forEach((element) => {
  //   routes.children.push({
  //     path: 'record-list/' + element.id,
  //     element: <RecordList recordTypeId={element.id} />,
  //   })
  // })

  return routes
}
