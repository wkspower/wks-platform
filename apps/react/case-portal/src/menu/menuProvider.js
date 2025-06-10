import { createContext, useState, useEffect, useContext, useMemo } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from '../SessionStoreContext'
import { useSelector } from 'react-redux'
import plan from './plan'
import workspace from './workspace'
import { mapScreen } from 'components/Utilities/menuRefractoring'
// import i18n from 'i18n/index'
// import { useNavigate } from '../../node_modules/react-router-dom/dist/index'

const MenuContext = createContext()
const USE_STATIC_MENU = true

export function MenuProvider({ children }) {
  const staticMenu = [plan, workspace]
  const [menuItems, setMenuItems] = useState(staticMenu)
  // const navigate = useNavigate()

  const keycloak = useSession()
  const { verticalChange } = useSelector((s) => s.dataGridStore)
  const verticalId = localStorage.getItem('verticalId')
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id

  useEffect(() => {
    if (USE_STATIC_MENU) {
      setMenuItems(staticMenu)
      return
    }

    if (!keycloak?.token || (!verticalId && !plantId)) return

    DataService.getScreenbyPlant(keycloak, verticalId, plantId)
      .then((res) => {
        // Map API response
        const dynamic = Array.isArray(res.data) ? res.data.map(mapScreen) : []
        if (dynamic.length) {
          setMenuItems(dynamic)
        } else {
          const base = [...staticMenu]
          setMenuItems(base)
        }
      })
      .catch((err) => {
        console.error('Menu API failed, using static menu', err)
        // Fallback with hardcoded if missing
        const base = [...staticMenu]
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
