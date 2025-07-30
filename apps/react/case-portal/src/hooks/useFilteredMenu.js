import { useMenuContext } from 'menu/menuProvider'
import { useMemo } from 'react'
import { useSession } from 'SessionStoreContext'

const useFilteredMenu = () => {
  const keycloak = useSession()
  const { items: menuItems } = useMenuContext()
  const isPlantManager = keycloak?.realmAccess?.roles?.includes('plant_manager')
  // const isPlantManager = true

  const filterMenuByRole = (items, hasPlantManagerRole) => {
    return items.map((item) => {
      if (item.type === 'group' && item.children) {
        const filteredChildren = item.children.filter((child) => {
          return !(child.id === 'user-management' && !hasPlantManagerRole)
        })

        return {
          ...item,
          children: filteredChildren,
        }
      }
      return item
    })
  }

  const filteredMenu = useMemo(() => {
    return {
      items: filterMenuByRole(menuItems || [], isPlantManager),
    }
  }, [menuItems, isPlantManager])

  return filteredMenu
}

export default useFilteredMenu
