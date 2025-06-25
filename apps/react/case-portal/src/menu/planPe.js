import FolderOutlined from '@ant-design/icons/FolderOutlined'

import {
  IconArchive,
  IconFileCheck,
  IconFileInvoice,
  IconList,
  IconSquareAsterisk,
  IconDatabase,
  IconChartBar, // For analytics/data trend
  IconSettings, // For settings/reliability
  IconPower, // For shutdown-related items
  IconTrendingDown, // For slowdown
  IconTools, // For maintenance (TA Plan)
  IconShield, // For safety
  IconFunction, // For functions module
  IconReport, // For reports section
  IconFile, // General file icon
  IconFileText, // For textual reports
  IconCalendarCog,
  IconFilter,
  IconSwitch,
  IconBarrierBlock,
  IconChartHistogram,
  IconPackages,
  IconTrafficCone,
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

const planPe = {
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
        // {
        //   id: 'spyro-menu',
        //   title: i18n.t('menu.spyroMenu'),
        //   type: 'collapse',
        //   icon: icons.IconArchive,
        //   children: [
        //     {
        //       id: 'spyro-input',
        //       title: i18n.t('menu.spyroInput'),
        //       type: 'item',
        //       url: '/production-norms-plan/spyro-menu/spyro-input',
        //       icon: icons.IconFilter,
        //       breadcrumbs: true,
        //     },
        //     {
        //       id: 'spyro-output',
        //       title: i18n.t('menu.spyroOutput'),
        //       type: 'item',
        //       url: '/production-norms-plan/spyro-menu/spyro-output',
        //       icon: icons.IconFilter,
        //       breadcrumbs: true,
        //     },
        //     {
        //       id: 'decoking-activities',
        //       title: i18n.t('menu.decokingActivities'),
        //       type: 'item',
        //       url: '/production-norms-plan/spyro-menu/decoking-activities',
        //       icon: icons.IconFilter,
        //       breadcrumbs: true,
        //     },
        //   ],
        // },

        {
          id: 'shutdown-plan',
          title: i18n.t('menu.shutdownPlan'),
          type: 'item',
          url: '/production-norms-plan/shutdown-plan',
          icon: icons.IconSwitch,
          breadcrumbs: true,
        },
        {
          id: 'slowdown-plan',
          title: i18n.t('menu.slowdownPlan'),
          type: 'item',
          url: '/production-norms-plan/slowdown-plan',
          icon: icons.IconTrendingDown,
          breadcrumbs: true,
        },

        {
          id: 'production-volume-data',
          title: i18n.t('menu.productMCUVal'),
          type: 'item',
          url: '/production-norms-plan/production-volume-data',
          icon: icons.IconSettings,
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
        {
          id: 'feed-stock',
          title: i18n.t('menu.feedStock'),
          type: 'item',
          url: '/production-norms-plan/feed-stock-availability',
          icon: icons.IconPackages,
          breadcrumbs: true,
        },
      ],
    },
  ],
}

export default planPe
