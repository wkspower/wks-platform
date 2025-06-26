import { lazy } from 'react'
import Loadable from 'components/Loadable'
import MainLayout from 'layout/MainLayout'
import TurnaroundPlanTable from 'components/data-tables/TurnaroundPlanTable'
// import AssessmentForm from 'components/data-tables/AssesmentForm/AssessmentContext'

import { CaseStatus } from 'common/caseStatus'
import { CaseList } from 'views/caseList/caseList'
// import { RecordList } from 'views/record/recordList'
import { TaskList } from 'views/taskList/taskList'
import { CaseDefList } from 'views/management/caseDef/caseDefList/caseDefList'
import { ProcessDefList } from 'views/management/processDef/processDefList'
import { FormList } from 'views/management/form/formList'
import { RecordTypeList } from 'views/management/recordType/recordTypeList'
import { QueueList } from 'views/management/queue/queueList'

// import SelectivityData from 'components/data-tables/SelectivityData'
// import FiveTables from 'components/data-tables/AOPWorkFlow/ProductMixTable'

// import UserManagement from 'components/user-management/UserManagementTable'
import UserForm from 'components/user-management/UserForm'
import UserManagementTable from 'components/user-management/UserManagementTable'
import AssessmentForm from 'components/data-tables/AssesmentForm/AssessmentContext'
import TextSubmitComponent from 'components/user-management/TextSubmitComponent'

import AnnualAopCost from 'components/data-tables/Reports/AnnualAopCost'

// import NormsHistorianBasis from 'components/data-tables/Reports/NormsHistorianBasis'

import FeedStockAvailability from 'components/data-tables/FeedStockavailability'

// import BusinessDemand from 'components/data-tables/BusinessDemand'
// import ConfigurationTable from 'components/data-tables/ConfigurationTable/index'
// import ShutDown from 'components/data-tables/ShutDown'
// import SlowDown from 'components/data-tables/Slowdown'
// import ProductionvolumeData from 'components/data-tables/ProductionVoluemData'
// import NormalOpNormsScreen from 'components/data-tables/NormsScreens/NormalOpNorms'
// import ShutdownNorms from 'components/data-tables/NormsScreens/ShutdownNorms'
// import MaintenanceTable from 'components/data-tables/MaintenanceTable'
// import ConsumptionNorms from 'components/data-tables/ConsumptionNorms'
// import ProductionNorms from 'components/data-tables/ProductionNorms'
// import SlowdownNorms from 'components/data-tables/NormsScreens/SlowdownNorms'
// import WorkFlowMerge from 'components/data-tables/AOPWorkFlow/WorkFlowMerge'
// import PlantsProductionSummary from 'components/data-tables/Reports/PlantsProductionData'
// import ProductionVolumeDataBasis from 'components/data-tables/Reports/ProductionVolumeDataBasis'
// import MonthwiseProduction from 'components/data-tables/Reports/MonthwiseProduction'
// import MonthwiseRawMaterial from 'components/data-tables/Reports/MonthwiseRawMaterial'

import BusinessDemand from 'components/kendo-data-tables/BusinessDemand'
import ConfigurationTable from 'components/kendo-data-tables/KendoConfigurationTable'
import ProductionvolumeData from 'components/kendo-data-tables/ProductionVoluemData'
import MaintenanceTable from 'components/kendo-data-tables/MaintenanceTable'
import ConsumptionNorms from 'components/kendo-data-tables/ConsumptionNorms'
import ProductionNorms from 'components/kendo-data-tables/ProductionNorms'
import NormalOpNormsScreen from 'components/kendo-data-tables/NormalOpNorms'
import ShutdownNorms from 'components/kendo-data-tables/ShutdownNorms'
import SlowdownNorms from 'components/kendo-data-tables/SlowdownNorms'
import ShutDown from 'components/kendo-data-tables/ShutDown'
import SlowDown from 'components/kendo-data-tables/Slowdown'
import WorkFlowMerge from 'components/data-tables/AOPWorkFlow/kendo-WorkFlowMerge'
import PlantsProductionSummary from 'components/data-tables/Reports-kendo/kendo-PlantsProductionData'
import ProductionVolumeDataBasis from 'components/data-tables/Reports-kendo/kendo-ProductionVolumeDataBasis'
import MonthwiseProduction from 'components/data-tables/Reports-kendo/kendo-MonthwiseProduction'
import MonthwiseRawMaterial from 'components/data-tables/Reports-kendo/kendo-MonthwiseRawMaterial'

import CrackerConfig from 'components/kendo-data-tables/KendoConfigCrackerInput'
import DecokingConfig from 'components/kendo-data-tables/KendoConfigCrackerActivities'
import CrackerConfigOutput from 'components/kendo-data-tables/KendoConfigCrackerOutput'
import NormsHistorianBasis from 'components/data-tables/Reports/NormsHistorianBasis'
import ProductionVolumeDataBasisPe from 'components/data-tables/Reports-kendo/kendo-ProductionVolumeDataBasisPe'
import NormsHistorianBasisPe from 'components/data-tables/Reports/NormsHistorianBasisPe'
import { Navigate } from '../../node_modules/react-router-dom/dist/index'
const ManagamentDefault = Loadable(lazy(() => import('../views/management')))
const DashboardDefault = Loadable(lazy(() => import('../views/dashboard')))

export const MainRoutes = (
  keycloak,
  authenticated,
  // recordsTypes,
  casesDefinitions,
) => {
  const verticalName = JSON.parse(
    localStorage.getItem('selectedVertical'),
  )?.name

  const defPage =
    verticalName == 'Cracker' ? <BusinessDemand /> : <BusinessDemand />
  const ProductionVolumeDataBasisElement =
    verticalName == 'PE' ? (
      <ProductionVolumeDataBasisPe />
    ) : (
      <ProductionVolumeDataBasis />
    )
  const NormsHistorianBasisElement =
    verticalName == 'PE' ? <NormsHistorianBasisPe /> : <NormsHistorianBasis />

  let routes = {
    path: '/',
    element: <MainLayout keycloak={keycloak} authenticated={authenticated} />,
    children: [
      {
        path: '/',
        element: <Navigate to='/production-norms-plan/business-demand' />,
        // element: defPage,
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
            element: defPage,
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
            element: ProductionVolumeDataBasisElement,
          },
          {
            path: 'norms-historian-basis',
            element: NormsHistorianBasisElement,
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
