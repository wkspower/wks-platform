import FolderOutlined from '@ant-design/icons/FolderOutlined'
import {
  IconArchive,
  IconFileCheck,
  IconFileInvoice,
  IconList,
  IconSquareAsterisk,
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
  IconSettingsAutomation,
  IconUserCog,
} from '@tabler/icons-react'

import i18n from 'i18n/index'

// Icon mapping object
export const icons = {
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
  IconSettingsAutomation,
  IconUserCog,
}

export function mapScreen(item = {}) {
  const {
    id = '',
    title = '',
    type = 'item',
    icon,
    url = '',
    breadcrumbs,
    children = [],
  } = item

  return {
    id,
    title: i18n.t(title),
    type,
    icon: icons[icon] || undefined,
    url,
    breadcrumbs: !!breadcrumbs,
    children: Array.isArray(children) ? children.map(mapScreen) : [],
  }
}
