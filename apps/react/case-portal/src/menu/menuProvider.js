import { createContext, useState, useEffect, useContext, useMemo } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from '../SessionStoreContext'
import { useSelector } from 'react-redux'
import plan from './plan'

import workspace from './workspace'
import { icons, mapScreen } from 'components/Utilities/menuRefractoring'
import i18n from 'i18n/index'
import planCracker from './planCracker'
// import { useNavigate } from '../../node_modules/react-router-dom/dist/index'

const MenuContext = createContext()
const USE_STATIC_MENU = true

export function MenuProvider({ children }) {
  const verticalName = JSON.parse(
    localStorage.getItem('selectedVertical'),
  )?.name

  const staticMenu = [plan, workspace]
  const staticMenuCracker = [planCracker]

  const menu2 = verticalName == 'Cracker' ? staticMenuCracker : staticMenu

  const [menuItems, setMenuItems] = useState(menu2)
  // const navigate = useNavigate()

  const keycloak = useSession()
  const { verticalChange } = useSelector((s) => s.dataGridStore)
  const verticalId = localStorage.getItem('verticalId')
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const userMgmtItem = {
    id: 'user-management',
    title: i18n.t('menu.userManage'),
    type: 'item',
    url: '/user-management',
    icon: icons?.IconUserCog,
    breadcrumbs: true,
  }
  useEffect(() => {
    if (USE_STATIC_MENU) {
      setMenuItems(menu2)
      return
    }

    if (!keycloak?.token || (!verticalId && !plantId)) return

    DataService.getScreenbyPlant(keycloak, verticalId, plantId)
      .then((res) => {
        // Map API response
        const dynamic = Array.isArray(res.data) ? res.data.map(mapScreen) : []

        // console.log(dynamic[0].children.length === 0, 'test-----')
        // console.log(dynamic.length && dynamic[0].children.length === 0)
        // Our hardcoded user-management entry
        // if (dynamic[0].children.length === 0) {
        //   navigate('/user-management')
        //   //   // optionally you can still inject the menu entry so the UI shows it:
        //   //   // dynamic[0].children.push(userMgmtItem)
        //   //   // setMenuItems(dynamic)
        //   //   // return
        // }
        // Function to check existence
        const containsUserMgmt = (items) =>
          items.some(
            (item) =>
              item.id === 'user-management' ||
              (Array.isArray(item.children) && containsUserMgmt(item.children)),
          )

        // If API returned items?
        if (dynamic.length) {
          // Inject user-management if missing
          if (!containsUserMgmt(dynamic)) {
            dynamic[0].children.push(userMgmtItem)
          }
          setMenuItems(dynamic)
        } else {
          const base = [...menu2]
          // if (!containsUserMgmt(base)) {
          //   base.push(userMgmtItem)
          // }
          setMenuItems(base)
        }
      })
      .catch((err) => {
        console.error('Menu API failed, using static menu', err)
        // Fallback with hardcoded if missing
        const base = [...menu2]
        if (!base.some((m) => m.id === 'user-management')) {
          base.push(userMgmtItem)
        }
        setMenuItems(base)
      })
  }, [keycloak, plantId, verticalId, verticalChange])
  // console.log(plantId)

  const menuValue = useMemo(() => ({ items: menuItems }), [menuItems])
  return (
    <MenuContext.Provider value={menuValue}>{children}</MenuContext.Provider>
  )
}

export const useMenuContext = () => useContext(MenuContext)
