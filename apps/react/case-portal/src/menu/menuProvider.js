import { createContext, useState, useEffect, useContext, useMemo } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from '../SessionStoreContext'
import { useSelector } from 'react-redux'
import plan from './plan'
import workspace from './workspace'
import { mapScreen } from 'components/Utilities/menuRefractoring'

const MenuContext = createContext()

export function MenuProvider({ children }) {
  const staticMenu = [plan, workspace]
  const [menuItems, setMenuItems] = useState(staticMenu)

  const keycloak = useSession()
  const { verticalChange } = useSelector((s) => s.dataGridStore)
  const verticalId = localStorage.getItem('verticalId')
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id

  useEffect(() => {
    if (!keycloak?.token || !verticalId) return

    DataService.getScreenbyPlant(keycloak, verticalId, plantId)
      .then((res) => {
       
        const dynamic = Array.isArray(res.data) ? res.data.map(mapScreen) : []
       
        if (dynamic.length) {
          setMenuItems(dynamic)
          // setMenuItems(staticMenu)
        }
      })
      .catch((err) => {
        console.error('Menu API failed, using static menu', err)
        setMenuItems(staticMenu)
      })
  }, [keycloak, verticalChange, verticalId, plantId])
  const menuValue = useMemo(() => ({ items: menuItems }), [menuItems])

  return (
    <MenuContext.Provider value={menuValue}>{children}</MenuContext.Provider>
  )
}

export const useMenuContext = () => useContext(MenuContext)
