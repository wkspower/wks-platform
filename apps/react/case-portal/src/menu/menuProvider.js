import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from '../SessionStoreContext'
import plan from './plan'

import { mapScreen } from 'components/Utilities/menuRefractoring'
import planCracker from './planCracker'
import workspace from './workspace'

const MenuContext = createContext()
const STATIC_MENU_DEFAULT = [plan, workspace]
const STATIC_MENU_CRACKER = [planCracker]
const USE_STATIC_MENU = true

export function MenuProvider({ children }) {
  // const navigate = useNavigate()

  const keycloak = useSession()

  const { verticalChange } = useSelector((state) => state.dataGridStore)
  const verticalName = verticalChange?.selectedVertical || ''
  const verticalId =
    verticalChange?.selectedVerticalId || localStorage.getItem('verticalId')
  const plant = JSON.parse(localStorage.getItem('selectedPlant') || '{}')
  const plantId = plant?.id

  const staticMenuForVertical = useMemo(() => {
    return verticalName === 'Cracker'
      ? STATIC_MENU_CRACKER
      : STATIC_MENU_DEFAULT
  }, [verticalName])
  const [menuItems, setMenuItems] = useState(staticMenuForVertical)
  const [permissions, setPermissions] = useState([])
  // const fetchMenuScreens = useCallback(
  //   async (currentToken, vId, pId, staticMenu) => {
  //     if (USE_STATIC_MENU) {
  //       return staticMenu
  //     }

  //     if (!currentToken || (!vId && !pId)) {
  //       return staticMenu
  //     }
  //     try {
  //       const res = await DataService.getScreenbyPlant(
  //         { token: currentToken },
  //         vId,
  //         pId,
  //       )
  //       console.log(res.data, 'res.data')
  //       const dynamic = Array.isArray(res.data) ? res.data.map(mapScreen) : []
  //       console.log(dynamic, 'dynamic')

  //       // console.log(dynamic[0].children.length === 0, 'test-----')
  //       // console.log(dynamic.length && dynamic[0].children.length === 0)
  //       // Our hardcoded user-management entry
  //       // if (dynamic[0].children.length === 0) {
  //       //   //   // optionally you can still inject the menu entry so the UI shows it:
  //       //   //   // dynamic[0].children.push(userMgmtItem)
  //       //   //   // setMenuItems(dynamic)
  //       //   //   // return
  //       // }
  //       // Function to check existence

  //       // If API returned items?
  //       if (dynamic.length) {
  //         // Inject user-management if missing
  //         return dynamic
  //         // if (!containsUserMgmt(base)) {
  //         //   base.push(userMgmtItem)
  //         // }
  //       }
  //       return staticMenu
  //     } catch (err) {
  //       console.error('Menu API failed, using static menu', err)
  //       // Fallback with hardcoded if missing
  //       return staticMenu
  //     }
  //   },
  //   [],
  // )

  // Cache to avoid redundant API calls
  const menuCache = new Map()

  const fetchMenuScreens = useCallback(
    async (currentToken, vId, pId, staticMenu) => {
      if (USE_STATIC_MENU || !currentToken || (!vId && !pId)) {
        return staticMenu
      }

      try {
        const res = await DataService.getScreenbyPlant(
          { token: currentToken },
          vId,
          pId,
        )

        if (!res?.data || !Array.isArray(res.data) || res.data.length === 0) {
          menuCache.set(cacheKey, staticMenu)
          return staticMenu
        }

        const parsedPermissions = res?.permission
          ? JSON.parse(res.permission)
          : []
        // console.log('parsedPermissions', parsedPermissions)
        setPermissions(parsedPermissions)
        const dynamic = res.data.map(mapScreen)

        const result =
          dynamic.length > 0 && dynamic[0]?.children?.length > 0
            ? dynamic
            : staticMenu

        return result
      } catch (err) {
        setPermissions([])
        return staticMenu
      }
    },
    [],
  )
  useEffect(() => {
    let cancelled = false
    async function updateMenu() {
      const token = keycloak?.token
      const items = await fetchMenuScreens(
        token,
        verticalId,
        plantId,
        staticMenuForVertical,
      )
      // console.log('items', items)
      if (!cancelled) {
        setMenuItems(items)
      }
    }
    updateMenu()
    return () => {
      cancelled = true
    }
  }, [
    keycloak?.token,
    verticalId,
    plantId,
    staticMenuForVertical,
    fetchMenuScreens,
  ])

  const menuValue = useMemo(
    () => ({ items: menuItems, permissions }),
    [menuItems, permissions],
  )
  return (
    <MenuContext.Provider value={menuValue}>{children}</MenuContext.Provider>
  )
}

export const useMenuContext = () => {
  const ctx = useContext(MenuContext)
  if (!ctx) {
    throw new Error('useMenuContext must be used within MenuProvider')
  }
  return ctx
}
// import { mapScreen } from 'components/Utilities/menuRefractoring'
//     <MenuContext.Provider value={menuValue}>{children}</MenuContext.Provider>
// export const useMenuContext = () => useContext(MenuContext)
