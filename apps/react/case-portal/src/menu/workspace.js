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
          id: 'contribution-report',
          title: i18n.t('menu.contributionReport'),
          type: 'item',
          url: '/reports/contribution-report',
          icon: icons.IconFile,
          breadcrumbs: true,
        },
        {
          id: 'previous-fy-aop-result',
          title: i18n.t('menu.previousFYAOPResult'),
          type: 'item',
          url: '/reports/previous-fy-aop-result',
          icon: icons.IconFileText,
          breadcrumbs: true,
        },
        {
          id: 'mat-bal-sheet',
          title: i18n.t('menu.matBalSheet'),
          type: 'item',
          url: '/reports/mat-bal-sheet',
          icon: icons.IconFileCheck,
          breadcrumbs: true,
        },
      ],
    },
    {
      id: 'workflow',
      title: i18n.t('menu.workflow'),
      type: 'item',
      url: '/workflow',
      icon: icons?.IconList,
      breadcrumbs: true,
    },
  ],
}

export default workspace
