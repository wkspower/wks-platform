import { IconUserCog } from '@tabler/icons-react'
import { useMenuContext } from 'menu/menuProvider'
import { useMemo } from 'react'
import { useSession } from 'SessionStoreContext'
import i18n from '../i18n/index'

const useFilteredMenu = () => {
  const keycloak = useSession()
  const { items: menuItems } = useMenuContext()
  const isPlantManager = keycloak?.realmAccess?.roles?.includes('cts_admin')
  // const isPlantManager = true

  const filterMenuByRole = (items) => {
    if (
      !keycloak?.idTokenParsed?.plants ||
      keycloak.idTokenParsed.plants.length === 0
    ) {
      return []
    }

    return items.map((item, index) => {
      if (item.type === 'group' && item.children) {
        const filteredChildren = item.children.filter((child) => {
          return child.id !== 'user-management'
        })

        return {
          ...item,
          children: filteredChildren,
        }
      }
      return item
    })
  }

  const userManagementRoute = {
    children: [
      {
        id: 'user-management',
        title: i18n.t('menu.userManage'),
        type: 'item',
        url: '/user-management',
        icon: IconUserCog,
        breadcrumbs: true,
      },
    ],
    id: 'utilities',
    title: '',
    type: 'group',
    url: '',
    icon: undefined,
    breadcrumbs: false,
  }

  const dashboardRoute = {
    children: [
      {
        id: 'dashboard',
        title: i18n.t('menu.dashboard'),
        type: 'item',
        url: '/dashboard',
        icon: IconUserCog,
        breadcrumbs: true,
      },
    ],
    id: 'utilities',
    title: '',
    type: 'group',
    url: '',
    icon: undefined,
    breadcrumbs: false,
  }

  const filteredMenu = useMemo(() => {
    const filteredMenuItem = filterMenuByRole(menuItems || [])
    const updatedMenu = isPlantManager
      ? [...filteredMenuItem, userManagementRoute]
      : filteredMenuItem

    return {
      items: updatedMenu,
    }
  }, [menuItems])

  return filteredMenu
}

export default useFilteredMenu
