import i18n from '../i18n'

// assets
import {
  IconForms,
  IconDatabase,
  IconPencil,
  IconSchema,
} from '@tabler/icons-react'

// icons
const icons = {
  IconForms,
  IconDatabase,
  IconPencil,
  IconSchema,
}

const management = {
  id: 'management',
  title: 'Management',
  caption: 'Management',
  type: 'group',
  children: [
    {
      id: 'system',
      title: i18n.t('menu.system'),
      type: 'collapse',
      icon: icons.IconBuilding,
      children: [
        {
          id: 'look-and-feel',
          title: 'Look And Feel',
          type: 'item',
          url: '/system/look-and-feel',
          icon: icons.IconPalette,
          breadcrumbs: true,
        },
      ],
    },
    {
      id: 'casesAndProcessesManagement',
      title: i18n.t('menu.casebuilder'),
      type: 'collapse',
      icon: icons.IconSettingsAutomation,
      children: [
        {
          id: 'process-definition',
          title: i18n.t('menu.processes'),
          type: 'item',
          url: '/case-life-cycle/process-definition',
          breadcrumbs: true,
          icon: icons.IconSchema,
        },
        {
          id: 'case-definition',
          title: i18n.t('menu.caseDefinitions'),
          type: 'item',
          url: '/case-life-cycle/case-definition',
          breadcrumbs: true,
          icon: icons.IconPencil,
        },
        {
          id: 'record-type',
          title: i18n.t('menu.recordTypes'),
          type: 'item',
          url: '/case-life-cycle/record-type',
          breadcrumbs: true,
          icon: icons.IconDatabase,
        },
        {
          id: 'form',
          title: i18n.t('menu.forms'),
          type: 'item',
          url: '/case-life-cycle/form',
          breadcrumbs: true,
          icon: icons.IconForms,
        },
        {
          id: 'queue',
          title: i18n.t('menu.queues'),
          type: 'item',
          url: '/case-life-cycle/queue',
          breadcrumbs: true,
          icon: icons.IconForms,
        },
      ],
    },
  ],
}

export default management
