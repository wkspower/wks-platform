// src/routes/PrivateRoute.js
import useFilteredMenu from 'hooks/useFilteredMenu'
import { Navigate } from 'react-router-dom'

const findFirstUrlFromMenu = (menu) => {
  for (const group of menu.items) {
    if (group.type === 'item' && group.url && group.url !== '/dashboard') {
      return group.url
    }

    if (!group.children) continue

    for (const child of group.children) {
      if (child.type === 'item' && child.url && child.url !== '/dashboard') {
        return child.url
      }

      if (child.children) {
        const firstItem = child.children.find(
          (c) => c.type === 'item' && c.url && c.url !== '/dashboard',
        )
        if (firstItem) {
          return firstItem.url
        }
      }
    }
  }

  return '/not-found'
}

// const isRouteIdAllowed = (menu, routeId) => {
//   for (const group of menu.items) {
//     if (!group.children) continue
//     for (const child of group.children || []) {
//       if (child.id === routeId) return true
//       if (child.children?.map((menu) => menu.id).includes(routeId)) return true
//     }
//   }
//   return false
// }

const isRouteIdAllowed = (menu, routeId) => {
  // console.log(12)
  for (const group of menu.items) {
    if (!group.children) continue
    const search = (items) => {
      for (const item of items || []) {
        if (item.id === routeId) return true
        if (item.children && search(item.children)) return true
      }
      return false
    }
    const result = search(group.children)
    if (result) return true
  }
  return false
}

const PrivateRoute = ({ children, routeId }) => {
  // console.log(13)
  const filteredMenu = useFilteredMenu()
  if (isRouteIdAllowed(filteredMenu, routeId)) {
    return children
  }
  const fallbackUrl = findFirstUrlFromMenu(filteredMenu)
  // const fallbackUrl = '/dashboard'

  // console.log('fallbackUrl', fallbackUrl)

  return <Navigate to={fallbackUrl} replace />
}

export default PrivateRoute
