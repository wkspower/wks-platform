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
          id: 'product-demand',
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
              icon: icons.IconFilter,
              breadcrumbs: true,
            },
            {
              id: 'spyro-output',
              title: i18n.t('menu.spyroOutput'),
              type: 'item',
              url: '/production-norms-plan/spyro-menu/spyro-output',
              icon: icons.IconFilter,
              breadcrumbs: true,
            },
            {
              id: 'decoking-activities',
              title: i18n.t('menu.decokingActivities'),
              type: 'item',
              url: '/production-norms-plan/spyro-menu/decoking-activities',
              icon: icons.IconFilter,
              breadcrumbs: true,
            },
          ],
        },

        {
          id: 'production-norms',
          title: i18n.t('menu.productionNorms'),
          type: 'item',
          url: '/production-norms-plan/production-aop',
          icon: icons.IconDatabase,
          breadcrumbs: true,
        },
      ],
    },
  ],
}

export default planCracker
