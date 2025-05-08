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
} from '@tabler/icons-react'
import i18n from 'i18n/index'
// import i18n from '../i18n'

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
}

/**
 * Recursively maps raw menu items to the shape your app expects.
 * @param {Object} item - Raw menu item
 * @returns {Object} Mapped menu item
 */
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

// export function mapScreen(item = {}) {
//   return {
//     id: item.id || '',
//     title: i18n.t(item.title || ''),
//     type: item.type || 'item',
//     icon: icons[item.icon] || undefined,
//     url: item.url || '',
//     breadcrumbs: Boolean(item.breadcrumbs),
//     children: Array.isArray(item.children) ? item.children.map(mapScreen) : [],
//   }
// }
