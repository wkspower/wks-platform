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
}

const plan = {
  id: 'utilities',
  title: '',
  type: 'group',
  children: [
    {
      id: 'production-norms-plan',
      title: i18n.t('menu.productionNormsPlan'),
      type: 'collapse',
      icon: icons.IconArchive, // You can choose an appropriate icon
      children: [
        {
          id: 'product-demand',
          title: i18n.t('menu.productDemand'),
          type: 'item',
          url: '/production-norms-plan/business-demand',
          icon: icons.IconChartBar,
          breadcrumbs: true,
        },
        {
          id: 'product-mcu-val',
          title: i18n.t('menu.productMCUVal'),
          type: 'item',
          url: '/production-norms-plan/production-volume-data',
          icon: icons.IconSettings,
          breadcrumbs: true,
        },
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

        // verticalChange !== 'MEG' &&
        {
          id: 'ta-plan',
          title: i18n.t('menu.taPlan'),
          type: 'item',
          url: '/production-norms-plan/turnaround-plan',
          icon: icons.IconTools,
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
          id: 'production-norms',
          title: i18n.t('menu.productionNorms'),
          type: 'item',
          url: '/production-norms-plan/production-aop',
          icon: icons.IconDatabase,
          breadcrumbs: true,
        },
        {
          id: 'catalyst-selectivity',
          title: i18n.t('menu.catalystSelectivity'),
          type: 'item',
          url: '/production-norms-plan/configuration',
          icon: icons.IconFilter,
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
          id: 'consumption-norms',
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

export default plan
