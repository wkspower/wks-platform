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
  // IconFlow, // For workflow-related processes
  IconSettingsAutomation,
  IconUserCog,
} from '@tabler/icons-react'

import i18n from '../i18n'

const icons = {
  FolderOutlined,
  IconFileInvoice,
  IconFileCheck,
  IconArchive,
  IconSquareAsterisk,
  IconList,
  IconUserCog,
  IconSettingsAutomation,
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
  // IconFlow,
}

const workspace = {
  id: 'utilities',
  title: '',
  type: 'group',
  children: [
    // {
    //   id: 'case-list',
    //   title: i18n.t('menu.case'),
    //   type: 'collapse',
    //   icon: icons.FolderOutlined,
    //   children: [],
    // },

    // {
    //   id: 'functions',
    //   title: i18n.t('menu.functions'),
    //   type: 'collapse',
    //   icon: icons.IconFunction, // You can choose an appropriate icon
    //   children: [
    //     {
    //       id: 'safety',
    //       title: i18n.t('menu.safety'),
    //       type: 'item',
    //       url: '/functions/safety',
    //       icon: icons.IconShield,
    //       breadcrumbs: true,
    //     },
    //     {
    //       id: 'reliability',
    //       title: i18n.t('menu.reliability'),
    //       type: 'item',
    //       url: '/functions/reliability',
    //       icon: icons.IconSettings,
    //       breadcrumbs: true,
    //     },
    //   ],
    // },

    // {
    //   id: 'record-list',
    //   title: i18n.t('menu.record'),
    //   type: 'collapse',
    //   icon: icons.IconDatabase,
    //   children: [],
    // },
    // {
    //   id: 'task-list',
    //   title: i18n.t('menu.task'),
    //   type: 'item',
    //   url: '/task-list',
    //   icon: icons.IconList,
    //   breadcrumbs: true,
    // },
    {
      id: 'reports',
      title: i18n.t('menu.reports'),
      type: 'collapse',
      icon: icons.IconReport, // You can choose an appropriate icon
      children: [
        {
          id: 'aop-annual-cost-report',
          title: i18n.t('menu.annualAopCostReport'),
          type: 'item',
          url: '/reports/aop-annual-cost-report',
          icon: icons.IconFile,
          breadcrumbs: true,
        },
        {
          id: 'production-volume-basis',
          title: i18n.t('menu.productionVolumeDataBasis'),
          type: 'item',
          url: '/reports/production-volume-basis',
          icon: icons.IconFile,
          breadcrumbs: true,
        },
        {
          id: 'plants-production',
          title: i18n.t('menu.plantsProduction'),
          type: 'item',
          url: '/reports/plants-production',
          icon: icons.IconFileCheck,
          breadcrumbs: true,
        },
        {
          id: 'monthwise-production',
          title: i18n.t('menu.monthwise'),
          type: 'item',
          url: '/reports/monthwise-production',
          icon: icons.IconFileText,
          breadcrumbs: true,
        },
        //     {
        //       id: 'previous-fy-aop-result',
        //       title: i18n.t('menu.previousFYAOPResult'),
        //       type: 'item',
        //       url: '/reports/previous-fy-aop-result',
        //       icon: icons.IconFileText,
        //       breadcrumbs: true,
        //     },
        //     {
        //       id: 'mat-bal-sheet',
        //       title: i18n.t('menu.matBalSheet'),
        //       type: 'item',
        //   url: '/reports/mat-bal-sheet',
        //       icon: icons.IconFileCheck,
        //       breadcrumbs: true,
        //     },
      ],
    },
    {
      id: 'workflow',
      title: i18n.t('menu.workflow'),
      type: 'item',
      url: '/workflow',
      icon: icons?.IconSettingsAutomation,
      breadcrumbs: true,
    },
    {
      id: 'user-management',
      title: i18n.t('menu.userManage'),
      type: 'item',
      url: '/user-management',
      icon: icons?.IconUserCog,
      breadcrumbs: true,
    },
  ],
}

export default workspace
