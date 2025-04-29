// new-plan.js
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
import i18n from '../i18n'
import { useScreens } from './userscreen'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import { useSelector } from 'react-redux'
// import { useScreens } from './userscreen'
// import { DataService } from 'services/DataService'
// import { useEffect, useState } from 'react'
// import { useSession } from 'SessionStoreContext'
// import { useSelector } from 'react-redux'
// import { useScreens } from './userscreen'

// Mirror your original icon imports
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

function mapScreen(item = {}) {
  return {
    id: item.id || '',
    title: i18n.t(item.title || ''),
    type: item.type || 'item',
    icon: icons[item.icon] || undefined,
    url: item.url || '',
    breadcrumbs: Boolean(item.breadcrumbs),
    children: Array.isArray(item.children) ? item.children.map(mapScreen) : [],
  }
}

export function usePlanMenu() {
  // ==== REPLACED with a literal array ====

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const [screens, setScreens] = useState([])
  const keycloak = useSession()

  const verticalId = localStorage.getItem('verticalId')
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  // console.log(verticalId, verticalChange)
  useEffect(() => {
    if (!keycloak?.token || !verticalId) return

    const fetchScreens = async () => {
      try {
        const res = await DataService.getScreenbyPlant(
          keycloak,
          verticalId,
          plantId,
        )
        // const res = await DataService.getUserScreen(keycloak, verticalId)
        // console.log('[useScreens] API response:', res)
        setScreens(res.data)
      } catch (error) {
        console.error('Error fetching menu:', error)
      }
    }

    fetchScreens()
  }, [keycloak, verticalId, verticalChange])

  // console.log(screens)
  // const screens = [
  //   {
  //     id: 'utilities',
  //     title: '',
  //     type: 'group',
  //     children: [
  //       {
  //         id: 'production-norms-plan',
  //         title: 'menu.productionNormsPlan',
  //         type: 'collapse',
  //         icon: 'IconArchive',
  //         children: [
  //           {
  //             icon: 'IconChartBar',
  //             id: 'product-demand',
  //             title: 'menu.productDemand',
  //             type: 'item',
  //             url: '/production-norms-plan/business-demand',
  //             breadcrumbs: true,
  //           },
  //           {
  //             icon: 'IconSettings',
  //             id: 'product-mcu-val',
  //             title: 'menu.productMCUVal',
  //             type: 'item',
  //             url: '/production-norms-plan/production-volume-data',
  //             breadcrumbs: true,
  //           },
  //           {
  //             icon: 'IconSwitch',
  //             id: 'shutdown-plan',
  //             title: 'menu.shutdownPlan',
  //             type: 'item',
  //             url: '/production-norms-plan/shutdown-plan',
  //             breadcrumbs: true,
  //           },
  //           {
  //             icon: 'IconTrendingDown',
  //             id: 'slowdown-plan',
  //             title: 'menu.slowdownPlan',
  //             type: 'item',
  //             url: '/production-norms-plan/slowdown-plan',
  //             breadcrumbs: true,
  //           },
  //           {
  //             icon: 'IconTools',
  //             id: 'ta-plan',
  //             title: 'menu.taPlan',
  //             type: 'item',
  //             url: '/production-norms-plan/turnaround-plan',
  //             breadcrumbs: true,
  //           },
  //           {
  //             icon: 'IconCalendarCog',
  //             id: 'maintenance-details',
  //             title: 'menu.maintenanceDetails',
  //             type: 'item',
  //             url: '/production-norms-plan/maintenance-details',
  //             breadcrumbs: true,
  //           },
  //           {
  //             icon: 'IconDatabase',
  //             id: 'production-norms',
  //             title: 'menu.productionNorms',
  //             type: 'item',
  //             url: '/production-norms-plan/production-aop',
  //             breadcrumbs: true,
  //           },
  //           {
  //             icon: 'IconFilter',
  //             id: 'catalyst-selectivity',
  //             title: 'menu.catalystSelectivity',
  //             type: 'item',
  //             url: '/production-norms-plan/configuration',
  //             breadcrumbs: true,
  //           },
  //           {
  //             icon: 'IconBarrierBlock',
  //             id: 'normal-op-norms',
  //             title: 'menu.normalOpNorms',
  //             type: 'item',
  //             url: '/production-norms-plan/normal-op-norms',
  //             breadcrumbs: true,
  //           },
  //           {
  //             icon: 'IconPower',
  //             id: 'shutdown-norms',
  //             title: 'menu.shutdownNorms',
  //             type: 'item',
  //             url: '/production-norms-plan/shutdown-norms',
  //             breadcrumbs: true,
  //           },
  //           {
  //             icon: 'IconChartHistogram',
  //             id: 'consumption-norms',
  //             title: 'menu.consumptionNorms',
  //             type: 'item',
  //             url: '/production-norms-plan/consumption-aop',
  //             breadcrumbs: true,
  //           },
  //           {
  //             icon: 'IconPackages',
  //             id: 'feed-stock',
  //             title: 'menu.feedStock',
  //             type: 'item',
  //             url: '/production-norms-plan/feed-stock-availability',
  //             breadcrumbs: true,
  //           },
  //         ],
  //       },
  //     ],
  //   },
  // ]

  const utilities = screens.find((g) => g.id === 'utilities') || {}
  const collapseGroup = Array.isArray(utilities.children)
    ? utilities.children[0] || {}
    : {}

  // No more “icons.” prefix left, but if you did have it, you’d strip it here:
  const rawIcon = collapseGroup.icon || ''
  const cleanedIcon = rawIcon.startsWith('icons.')
    ? rawIcon.slice('icons.'.length)
    : rawIcon

  return {
    id: utilities.id || 'utilities',
    title: i18n.t(utilities.title || ''),
    type: utilities.type || 'group',
    children: [
      {
        id: collapseGroup.id || 'production-norms-plan',
        title: i18n.t(collapseGroup.title || ''),
        type: collapseGroup.type || 'collapse',
        icon: icons[cleanedIcon] || IconArchive,
        children: Array.isArray(collapseGroup.children)
          ? collapseGroup.children.map(mapScreen)
          : [],
      },
    ],
  }
  // } catch (error) {
  //     console.error('Error fetching menu:', error)
  //   }
  // }, [])
}
