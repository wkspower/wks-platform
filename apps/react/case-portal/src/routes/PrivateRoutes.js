// src/routes/PrivateRoute.js
import useFilteredMenu from 'hooks/useFilteredMenu'
import { Navigate } from 'react-router-dom'

const findFirstUrlFromMenu = (menu) => {
  for (const group of menu.items) {
    if (!group.children) continue
    for (const child of group.children) {
      if (child.type === 'item' && child.url) {
        return child.url
      } else if (child.children) {
        const firstItem = child.children.find((c) => c.type === 'item' && c.url)
        if (firstItem) return firstItem.url
      }
    }
  }
  return '/not-found'
}

const isRouteIdAllowed = (menu, routeId) => {
  console.log('menu.items', menu.items)
  // return true
  for (const group of menu.items) {
    if (!group.children) continue
    for (const child of group.children || []) {
      if (child.id === routeId) return true
      if (child.children?.map((menu) => menu.id).includes(routeId)) return true
    }
  }
  return false
}

const PrivateRoute = ({ children, routeId }) => {
  const filteredMenu = useFilteredMenu()
  if (isRouteIdAllowed(filteredMenu, routeId)) {
    return children
  }
  const fallbackUrl = findFirstUrlFromMenu(filteredMenu)
  return <Navigate to={fallbackUrl} replace />
}

export default PrivateRoute
