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
} from '@tabler/icons-react'

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
          url: '/production-norms-plan/product-demand',
          icon: icons.IconChartBar,
          breadcrumbs: true,
        },
        {
          id: 'product-mcu-val',
          title: i18n.t('menu.productMCUVal'),
          type: 'item',
          url: '/production-norms-plan/product-mcu-val',
          icon: icons.IconSettings,
          breadcrumbs: true,
        },
        {
          id: 'shutdown-plan',
          title: i18n.t('menu.shutdownPlan'),
          type: 'item',
          url: '/production-norms-plan/shutdown-plan',
          icon: icons.IconPower,
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
          id: 'ta-plan',
          title: i18n.t('menu.taPlan'),
          type: 'item',
          url: '/production-norms-plan/ta-plan',
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
          url: '/production-norms-plan/production-norms',
          icon: icons.IconDatabase,
          breadcrumbs: true,
        },
        {
          id: 'catalyst-selectivity',
          title: i18n.t('menu.catalystSelectivity'),
          type: 'item',
          url: '/production-norms-plan/catalyst-selectivity',
          icon: icons.IconDatabase,
          breadcrumbs: true,
        },
        {
          id: 'normal-op-norms',
          title: i18n.t('menu.normalOpNorms'),
          type: 'item',
          url: '/production-norms-plan/normal-op-norms',
          icon: icons.IconDatabase,
          breadcrumbs: true,
        },
        {
          id: 'shutdown-norms',
          title: i18n.t('menu.shutdownNorms'),
          type: 'item',
          url: '/production-norms-plan/shutdown-norms',
          icon: icons.IconDatabase,
          breadcrumbs: true,
        },

        {
          id: 'consumption-norms',
          title: i18n.t('menu.consumptionNorms'),
          type: 'item',
          url: '/production-norms-plan/consumption-norms',
          icon: icons.IconDatabase,
          breadcrumbs: true,
        },
        {
          id: 'feed-stock',
          title: i18n.t('menu.feedStock'),
          type: 'item',
          url: '/production-norms-plan/feed-stock',
          icon: icons.IconDatabase,
          breadcrumbs: true,
        },
      ],
    },
  ],
}

export default plan
