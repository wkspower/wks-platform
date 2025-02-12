import FolderOutlined from '@ant-design/icons/FolderOutlined'

import {
  IconArchive,
  IconFileCheck,
  IconFileInvoice,
  IconList,
  IconSquareAsterisk,
  IconDatabase,
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

    {
      id: 'case-list',
      title: i18n.t('menu.case'),
      type: 'collapse',
      icon: icons.FolderOutlined,
      children: [],
    },

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
          icon: icons.IconList,
          breadcrumbs: true,
        },
        {
          id: 'product-mcu-val',
          title: i18n.t('menu.productMCUVal'),
          type: 'item',
          url: '/production-norms-plan/product-mcu-val',
          icon: icons.IconList,
          breadcrumbs: true,
        },
        {
          id: 'shutdown-plan',
          title: i18n.t('menu.shutdownPlan'),
          type: 'item',
          url: '/production-norms-plan/shutdown-plan',
          icon: icons.IconList,
          breadcrumbs: true,
        },
        {
          id: 'slowdown-plan',
          title: i18n.t('menu.slowdownPlan'),
          type: 'item',
          url: '/production-norms-plan/slowdown-plan',
          icon: icons.IconList,
          breadcrumbs: true,
        },
        {
          id: 'ta-plan',
          title: i18n.t('menu.taPlan'),
          type: 'item',
          url: '/production-norms-plan/ta-plan',
          icon: icons.IconList,
          breadcrumbs: true,
        },
        {
          id: 'feed-stock',
          title: i18n.t('menu.feedStock'),
          type: 'item',
          url: '/production-norms-plan/feed-stock',
          icon: icons.IconList,
          breadcrumbs: true,
        },
      ],
    },

    {
      id: 'functions',
      title: i18n.t('menu.functions'),
      type: 'collapse',
      icon: icons.IconFunction, // You can choose an appropriate icon
      children: [
        {
          id: 'safety',
          title: i18n.t('menu.safety'),
          type: 'item',
          url: '/functions/safety',
          icon: icons.IconShield,
          breadcrumbs: true,
        },
        {
          id: 'reliability',
          title: i18n.t('menu.reliability'),
          type: 'item',
          url: '/functions/reliability',
          icon: icons.IconSettings,
          breadcrumbs: true,
        },
      ],
    },

    {
      id: 'record-list',
      title: i18n.t('menu.record'),
      type: 'collapse',
      icon: icons.IconDatabase,
      children: [],
    },
    {
      id: 'task-list',
      title: i18n.t('menu.task'),
      type: 'item',
      url: '/task-list',
      icon: icons.IconList,
      breadcrumbs: true,
    },
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
      icon: icons.IconFlow,
      breadcrumbs: true,
    },
  ],
}

export default workspace
