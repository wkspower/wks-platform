import FolderOutlined from '@ant-design/icons/FolderOutlined'

import {
  IconArchive,
  IconBarrierBlock, // For textual reports
  IconCalendarCog,
  IconChartBar,
  IconChartHistogram,
  IconDatabase, // For reports section
  IconFile,
  IconFileCheck,
  IconFileInvoice, // General file icon
  IconFileText,
  IconFilter, // For safety
  IconFunction,
  IconList,
  IconPackages, // For settings/reliability
  IconPower, // For functions module
  IconReport, // For analytics/data trend
  IconSettings, // For maintenance (TA Plan)
  IconShield,
  IconSquareAsterisk,
  IconSwitch, // For slowdown
  IconTools,
  IconTrafficCone, // For shutdown-related items
  IconTrendingDown, // For slowdown
} from '@tabler/icons-react'
// import { useSelector } from 'react-redux'
import i18n from '../i18n'

const icons = {
  FolderOutlined,
  IconFileInvoice,
  IconFileCheck,
  IconArchive,
  IconSquareAsterisk,
  IconList,
  IconDatabase,
  IconChartBar,
  IconSettings,
  IconPower,
  IconTrendingDown,
  IconTools,
  IconShield,
  IconFunction,
  IconReport,
  IconFile,
  IconFileText,
  IconCalendarCog,
  IconFilter,
  IconSwitch,
  IconBarrierBlock,
  IconChartHistogram,
  IconPackages,
  IconTrafficCone,
}

const planCracker = {
  id: 'utilities',
  title: '',
  type: 'group',
  children: [
    {
      id: 'production-norms-plan',
      title: i18n.t('menu.productionNormsPlan'),
      type: 'collapse',
      icon: icons.IconArchive,
      children: [
        {
          id: 'configuration',
          title: i18n.t('menu.catalystSelectivity'),
          type: 'item',
          url: '/production-norms-plan/configuration',
          icon: icons.IconFilter,
          breadcrumbs: true,
        },
        {
          id: 'business-demand',
          title: i18n.t('menu.productDemand'),
          type: 'item',
          url: '/production-norms-plan/business-demand',
          icon: icons.IconChartBar,
          breadcrumbs: true,
        },
        {
          id: 'spyro-menu',
          title: i18n.t('menu.spyroMenu'),
          type: 'collapse',
          icon: icons.IconArchive,
          children: [
            {
              id: 'spyro-input',
              title: i18n.t('menu.spyroInput'),
              type: 'item',
              url: '/production-norms-plan/spyro-menu/spyro-input',
              icon: icons.IconDatabase,
              breadcrumbs: true,
            },
            {
              id: 'spyro-output',
              title: i18n.t('menu.spyroOutput'),
              type: 'item',
              url: '/production-norms-plan/spyro-menu/spyro-output',
              icon: icons.IconChartBar,
              breadcrumbs: true,
            },
          ],
        },

        {
          id: 'decoking-activities',
          title: i18n.t('menu.decokingActivities'),
          type: 'item',
          url: '/production-norms-plan/spyro-menu/decoking-activities',
          icon: icons.IconPower,
          breadcrumbs: true,
        },
        {
          id: 'maintenance-details',
          title: i18n.t('menu.maintenanceDetails'),
          type: 'item',
          url: '/production-norms-plan/maintenance-details',
          icon: icons.IconCalendarCog,
          breadcrumbs: true,
        },
        {
          id: 'production-aop',
          title: i18n.t('menu.productionNorms'),
          type: 'item',
          url: '/production-norms-plan/production-aop',
          icon: icons.IconDatabase,
          breadcrumbs: true,
        },
        {
          id: 'normal-op-norms',
          title: i18n.t('menu.normalOpNorms'),
          type: 'item',
          url: '/production-norms-plan/normal-op-norms',
          icon: icons.IconBarrierBlock,
          breadcrumbs: true,
        },
        {
          id: 'shutdown-norms',
          title: i18n.t('menu.shutdownNorms'),
          type: 'item',
          url: '/production-norms-plan/shutdown-norms',
          icon: icons.IconPower,
          breadcrumbs: true,
        },
        {
          id: 'slowdown-norms',
          title: i18n.t('menu.slowdownNorms'),
          type: 'item',
          url: '/production-norms-plan/slowdown-norms',
          icon: icons.IconTrafficCone,
          breadcrumbs: true,
        },

        {
          id: 'consumption-aop',
          title: i18n.t('menu.consumptionNorms'),
          type: 'item',
          url: '/production-norms-plan/consumption-aop',
          icon: icons.IconChartHistogram,
          breadcrumbs: true,
        },
      ],
    },
    {
      id: 'reports',
      title: i18n.t('menu.reports'),
      type: 'collapse',
      icon: icons.IconReport, // You can choose an appropriate icon
      children: [
        //  {
        //    id: 'aop-annual-cost-report',
        //    title: i18n.t('menu.annualAopCostReport'),
        //    type: 'item',
        //    url: '/reports/aop-annual-cost-report',
        //    icon: icons.IconFile,
        //    breadcrumbs: true,
        //  },
        //  {
        //    id: 'production-volume-basis',
        //    title: i18n.t('menu.productionVolumeDataBasis'),
        //    type: 'item',
        //    url: '/reports/production-volume-basis',
        //    icon: icons.IconFileText,
        //    breadcrumbs: true,
        //  },
        {
          id: 'norms-historian-basis',
          title: i18n.t('menu.normsHistorianBasis'),
          type: 'item',
          url: '/reports/norms-historian-basis',
          icon: icons.IconReport,
          breadcrumbs: true,
        },

        {
          id: 'best-achieved-basis',
          title: i18n.t('menu.bestAchievedBasis'),
          type: 'item',
          url: '/reports/best-achieved-basis',
          icon: icons.IconReport,
          breadcrumbs: true,
        },
      ],
    },
  ],
}

export default planCracker
