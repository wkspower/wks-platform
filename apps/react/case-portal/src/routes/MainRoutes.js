import { CaseStatus } from 'common/caseStatus'
import WorkFlowMerge from 'components/data-tables/AOPWorkFlow/kendo-WorkFlowMerge'
import AssessmentForm from 'components/data-tables/AssesmentForm/AssessmentContext'
import FeedStockAvailability from 'components/data-tables/FeedStockavailability'
import MonthwiseProduction from 'components/data-tables/Reports-kendo/kendo-MonthwiseProduction'
import MonthwiseRawMaterial from 'components/data-tables/Reports-kendo/kendo-MonthwiseRawMaterial'
import PlantsProductionSummary from 'components/data-tables/Reports-kendo/kendo-PlantsProductionData'
import ProductionVolumeDataBasis from 'components/data-tables/Reports-kendo/kendo-ProductionVolumeDataBasis'
import AnnualAopCost from 'components/data-tables/Reports/AnnualAopCost'
import NormsHistorianBasis from 'components/data-tables/Reports/NormsHistorianBasis'
import TurnaroundPlanTable from 'components/data-tables/TurnaroundPlanTable'
import BusinessDemand from 'components/kendo-data-tables/BusinessDemand'
import ConsumptionNorms from 'components/kendo-data-tables/ConsumptionNorms'
import DecokingConfig from 'components/kendo-data-tables/KendoConfigCrackerActivities'
import CrackerConfig from 'components/kendo-data-tables/KendoConfigCrackerInput'
import CrackerConfigOutput from 'components/kendo-data-tables/KendoConfigCrackerOutput'
import MaintenanceTable from 'components/kendo-data-tables/MaintenanceTable'
import NormalOpNormsScreen from 'components/kendo-data-tables/NormalOpNorms'
import ProductionNorms from 'components/kendo-data-tables/ProductionNorms'
import ProductionvolumeData from 'components/kendo-data-tables/ProductionVoluemData'
import ShutDown from 'components/kendo-data-tables/ShutDown'
import ShutdownNorms from 'components/kendo-data-tables/ShutdownNorms'
import SlowDown from 'components/kendo-data-tables/Slowdown'
import SlowdownNorms from 'components/kendo-data-tables/SlowdownNorms'
import Loadable from 'components/Loadable'
import TextSubmitComponent from 'components/user-management/TextSubmitComponent'
import UserForm from 'components/user-management/UserForm'
import UserManagementTable from 'components/user-management/UserManagementTable'

import MainLayout from 'layout/MainLayout'
import { lazy } from 'react'
import { CaseList } from 'views/caseList/caseList'
import { CaseDefList } from 'views/management/caseDef/caseDefList/caseDefList'
import { FormList } from 'views/management/form/formList'
import { ProcessDefList } from 'views/management/processDef/processDefList'
import { QueueList } from 'views/management/queue/queueList'
import { RecordTypeList } from 'views/management/recordType/recordTypeList'
import { TaskList } from 'views/taskList/taskList'
// import ProductionVolumeDataBasisPe from 'components/data-tables/Reports-kendo/kendo-ProductionVolumeDataBasisPe'
import ProductionVolumeDataBasisPe from 'components/data-tables/Reports-kendo/ProductionVolumeDataBasisPe'
import NormsHistorianBasisPe from 'components/data-tables/Reports/NormsHistorianBasisPe'
import ConfigurationTable from 'components/kendo-data-tables/KendoConfigurationTable'
import { Navigate } from '../../node_modules/react-router-dom/dist/index'
import PrivateRoute from './PrivateRoutes'
import AopBudget from 'components/kendo-data-tables/AopBudget'
import MonthlyTemplatePlants from 'components/kendo-data-tables/MonthlyTemplatePlants'
import AopSummary from 'components/kendo-data-tables/AopSummary'
import PlantTeam from 'components/kendo-data-tables/PlantTeam'
import RelPerf from 'components/kendo-data-tables/RelPerf'
import PlantSafetyPerformanceTarget from 'components/kendo-data-tables/PlantSafetyPerformanceTarget'

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
        element: <Navigate to='/production-norms-plan/configuration' />,
      },

      {
        path: 'home',
        element: (
          <PrivateRoute routeId='home'>
            <DashboardDefault />
          </PrivateRoute>
        ),
      },

      {
        path: 'case-list',
        children: [
          {
            path: 'cases',
            element: (
              <PrivateRoute routeId='case-list'>
                <CaseList />
              </PrivateRoute>
            ),
          },
          {
            path: 'wip-cases',
            element: (
              <PrivateRoute routeId='case-list'>
                <CaseList status={CaseStatus.WipCaseStatus.description} />
              </PrivateRoute>
            ),
          },
          {
            path: 'closed-cases',
            element: (
              <PrivateRoute routeId='case-list'>
                <CaseList status={CaseStatus.ClosedCaseStatus.description} />
              </PrivateRoute>
            ),
          },
          {
            path: 'archived-cases',
            element: (
              <PrivateRoute routeId='case-list'>
                <CaseList status={CaseStatus.ArchivedCaseStatus.description} />
              </PrivateRoute>
            ),
          },
        ],
      },
      {
        path: 'task-list',
        element: (
          <PrivateRoute routeId='task-list'>
            <TaskList />
          </PrivateRoute>
        ),
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
            element: (
              <PrivateRoute routeId='process-definition'>
                <ProcessDefList />
              </PrivateRoute>
            ),
          },
          {
            path: 'case-definition',
            element: (
              <PrivateRoute routeId='case-definition'>
                <CaseDefList />
              </PrivateRoute>
            ),
          },
          {
            path: 'record-type',
            element: (
              <PrivateRoute routeId='record-type'>
                <RecordTypeList />
              </PrivateRoute>
            ),
          },
          {
            path: 'form',
            element: (
              <PrivateRoute routeId='form'>
                <FormList />
              </PrivateRoute>
            ),
          },
          {
            path: 'queue',
            element: (
              <PrivateRoute routeId='queue'>
                <QueueList />
              </PrivateRoute>
            ),
          },
        ],
      },

      {
        path: 'production-norms-plan',
        children: [
          {
            path: 'business-demand',
            element: (
              <PrivateRoute routeId='business-demand'>
                <BusinessDemand />
              </PrivateRoute>
            ),
          },
          {
            path: 'configuration',
            element: (
              <PrivateRoute routeId='configuration'>
                <ConfigurationTable />
                {/* <Configuration /> */}
              </PrivateRoute>
            ),
            // element: <SelectivityData />,
          },
          {
            path: 'spyro-menu',
            children: [
              {
                path: 'spyro-input',
                element: (
                  <PrivateRoute routeId='spyro-input'>
                    <CrackerConfig keycloak={keycloak} />
                  </PrivateRoute>
                ),
              },
              {
                path: 'spyro-output',
                element: (
                  <PrivateRoute routeId='spyro-output'>
                    <CrackerConfigOutput />
                  </PrivateRoute>
                ),
              },
              {
                path: 'decoking-activities',
                element: (
                  <PrivateRoute routeId='decoking-activities'>
                    <DecokingConfig />
                  </PrivateRoute>
                ),
              },
            ],
          },
          {
            path: 'production-volume-data',
            element: (
              <PrivateRoute routeId='production-volume-data'>
                <ProductionvolumeData />
              </PrivateRoute>
            ),
          },
          {
            path: 'maintenance-details',
            element: (
              <PrivateRoute routeId='maintenance-details'>
                <MaintenanceTable />
              </PrivateRoute>
            ),
          },
          {
            path: 'consumption-aop',
            element: (
              <PrivateRoute routeId='consumption-aop'>
                <ConsumptionNorms />
              </PrivateRoute>
            ),
          },
          {
            path: 'production-aop',
            element: (
              <PrivateRoute routeId='production-aop'>
                <ProductionNorms />
              </PrivateRoute>
            ),
          },
          {
            path: 'normal-op-norms',
            element: (
              <PrivateRoute routeId='normal-op-norms'>
                <NormalOpNormsScreen />
              </PrivateRoute>
            ),
          },
          {
            path: 'shutdown-norms',
            element: (
              <PrivateRoute routeId='shutdown-norms'>
                <ShutdownNorms />
              </PrivateRoute>
            ),
          },
          {
            path: 'slowdown-norms',
            element: (
              <PrivateRoute routeId='slowdown-norms'>
                <SlowdownNorms />
              </PrivateRoute>
            ),
          },
          // {
          //   path: 'slowdown-norms',
          //   element: <SlowdownNorms />,
          // },

          {
            path: 'shutdown-plan',
            element: (
              <PrivateRoute routeId='shutdown-plan'>
                <ShutDown />
              </PrivateRoute>
            ),
          },
          {
            path: 'slowdown-plan',
            element: (
              <PrivateRoute routeId='slowdown-plan'>
                <SlowDown />
              </PrivateRoute>
            ),
          },
          {
            path: 'turnaround-plan',
            element: (
              <PrivateRoute routeId='turnaround-plan'>
                <TurnaroundPlanTable />
              </PrivateRoute>
            ),
          },
          {
            path: 'feed-stock-availability',
            element: (
              <PrivateRoute routeId='feed-stock-availability'>
                <FeedStockAvailability />
              </PrivateRoute>
            ),
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
          {
            path: 'aop-annual-cost-report',
            element: (
              <PrivateRoute routeId='aop-annual-cost-report'>
                <AnnualAopCost />
              </PrivateRoute>
            ),
          },
          {
            path: 'production-volume-basis',
            element: (
              <PrivateRoute routeId='production-volume-basis'>
                {ProductionVolumeDataBasisElement}
              </PrivateRoute>
            ),
          },
          {
            path: 'norms-historian-basis',
            element: (
              <PrivateRoute routeId='norms-historian-basis'>
                {NormsHistorianBasisElement}
              </PrivateRoute>
            ),
          },
          {
            path: 'plants-production',
            element: (
              <PrivateRoute routeId='plants-production'>
                <PlantsProductionSummary />
              </PrivateRoute>
            ),
          },
          {
            path: 'monthwise-production',
            element: (
              <PrivateRoute routeId='monthwise-production'>
                <MonthwiseProduction />
              </PrivateRoute>
            ),
          },
          {
            path: 'monthwise-raw-material',
            element: <MonthwiseRawMaterial />,
          },
          { path: 'previous-fy-aop-result', element: <DashboardDefault /> },
          {
            path: 'mat-bal-sheet',
            element: (
              <PrivateRoute routeId='mat-bal-sheet'>
                <DashboardDefault />
              </PrivateRoute>
            ),
          },
        ],
      },

      {
        path: 'functional-aop',
        children: [
          {
            path: 'aop-budget',
            element: (
              <PrivateRoute routeId='aop-budget'>
                <AopBudget />
              </PrivateRoute>
            ),
          },
        ],
      },

      // {
      //   path: 'functional-aop',
      //   children: [
      //     {
      //       path: 'aop-budget',
      //       element: (
      //         <PrivateRoute routeId='aop-budget'>
      //           <AopBudget />
      //         </PrivateRoute>
      //       ),
      //     },
      //     {
      //       path: 'monthly-template-plants',
      //       element: (
      //         <PrivateRoute routeId='monthly-template-plants'>
      //           <MonthlyTemplatePlants />
      //         </PrivateRoute>
      //       ),
      //     },
      //     {
      //       path: 'aop-summary',
      //       element: (
      //         <PrivateRoute routeId='aop-summary'>
      //           <AopSummary />
      //         </PrivateRoute>
      //       ),
      //     },
      //   ],
      // },

      // {
      //   path: 'manufacturing-division',
      //   children: [
      //     {
      //       path: 'plant-team',
      //       element: (
      //         <PrivateRoute routeId='plant-team'>
      //           <PlantTeam />
      //         </PrivateRoute>
      //       ),
      //     },
      //     {
      //       path: 'plant-safety-performance-target',
      //       element: (
      //         <PrivateRoute routeId='plant-safety-performance-target'>
      //           <PlantSafetyPerformanceTarget />
      //         </PrivateRoute>
      //       ),
      //     },
      //   ],
      // },

      {
        path: 'plant-team',
        element: (
          <PrivateRoute routeId='plant-team'>
            <PlantTeam />
          </PrivateRoute>
        ),
      },
      {
        path: 'reliability-performance',
        element: (
          <PrivateRoute routeId='reliability-performance'>
            <RelPerf />
          </PrivateRoute>
        ),
      },
      {
        path: 'plant-safety-performance-target',
        element: (
          <PrivateRoute routeId='plant-safety-performance-target'>
            <PlantSafetyPerformanceTarget />
          </PrivateRoute>
        ),
      },

      {
        path: 'workflow',
        element: (
          <PrivateRoute routeId='workflow'>
            <WorkFlowMerge />
          </PrivateRoute>
        ),
        // element: <FiveTables />,
      },
      {
        path: 'user-management',
        element: (
          <PrivateRoute routeId='user-management'>
            <UserManagementTable keycloak={keycloak} />
          </PrivateRoute>
        ),
      },

      {
        path: 'user-form',
        element: <UserForm keycloak={keycloak} />,
      },
      {
        path: 'assessment-form',
        element: (
          <PrivateRoute routeId='assessment-form'>
            <AssessmentForm />
          </PrivateRoute>
        ),
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
