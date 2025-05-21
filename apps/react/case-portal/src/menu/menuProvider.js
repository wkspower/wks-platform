import { createContext, useState, useEffect, useContext, useMemo } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from '../SessionStoreContext'
import { useSelector } from 'react-redux'
import plan from './plan'
import workspace from './workspace'
import { icons, mapScreen } from 'components/Utilities/menuRefractoring'
import i18n from 'i18n/index'

const MenuContext = createContext()

export function MenuProvider({ children }) {
  const staticMenu = [plan, workspace]
  const [menuItems, setMenuItems] = useState(staticMenu)

  const keycloak = useSession()
  const { verticalChange } = useSelector((s) => s.dataGridStore)
  const verticalId = localStorage.getItem('verticalId')
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  // const userMgmtItem = {
  //   id: 'user-management',
  //   title: i18n.t('menu.userManage'),
  //   type: 'item',
  //   url: '/user-management',
  //   icon: icons?.IconUserCog,
  //   breadcrumbs: true,
  // }
  // useEffect(() => {
  //   if (!keycloak?.token || !verticalId) return

  //   DataService.getScreenbyPlant(keycloak, verticalId, plantId)
  //     .then((res) => {
  //       // Map API response
  //       const dynamic = Array.isArray(res.data) ? res.data.map(mapScreen) : []

  //       // Our hardcoded user-management entry

  //       // Function to check existence
  //       const containsUserMgmt = (items) =>
  //         items.some(
  //           (item) =>
  //             item.id === 'user-management' ||
  //             (Array.isArray(item.children) && containsUserMgmt(item.children)),
  //         )

  //       // If API returned items…
  //       if (dynamic.length) {
  //         // Inject user-management if missing
  //         if (!containsUserMgmt(dynamic)) {
  //           dynamic[0].children.push(userMgmtItem)
  //         }
  //         setMenuItems(dynamic)
  //       } else {
  //         // No dynamic data: fall back to static + hardcoded
  //         const base = [...staticMenu]
  //         if (!containsUserMgmt(base)) {
  //           base.push(userMgmtItem)
  //         }
  //         setMenuItems(base)
  //       }
  //     })
  //     .catch((err) => {
  //       console.error('Menu API failed, using static menu', err)
  //       // Fallback with hardcoded if missing
  //       const base = [...staticMenu]
  //       if (!base.some((m) => m.id === 'user-management')) {
  //         base.push(userMgmtItem)
  //       }
  //       setMenuItems(base)
  //     })
  // }, [keycloak, plantId])
  useEffect(() => {
    if (!keycloak?.token || !verticalId) return

    DataService.getScreenbyPlant(keycloak, verticalId, plantId)
      .then((res) => {
        const dynamic = Array.isArray(res.data) ? res.data.map(mapScreen) : []
        console.log(dynamic)
        if (dynamic.length) {
          setMenuItems(dynamic)
          // setMenuItems(staticMenu)
        }
      })
      .catch((err) => {
        console.error('Menu API failed, using static menu', err)
        setMenuItems(staticMenu)
      })
  }, [keycloak, plantId, verticalId, verticalChange])
  // console.log(verticalChange)
  // console.log(verticalId)
  // console.log(plantId)
  const menuValue = useMemo(() => ({ items: menuItems }), [menuItems])
  // console.log(menuValue)
  return (
    <MenuContext.Provider value={menuValue}>{children}</MenuContext.Provider>
  )
}

export const useMenuContext = () => useContext(MenuContext)
