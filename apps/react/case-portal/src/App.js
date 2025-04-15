import { useEffect, useState, lazy, Suspense } from 'react'
import { ThemeRoutes } from './routes'
import ThemeCustomization from './themes'
import { SessionStoreProvider } from './SessionStoreContext'
// import { CaseService, RecordService } from 'services'
import menuItemsDefs from './menu'
import { RegisterInjectUserSession, RegisteOptions } from './plugins'
import { accountStore, sessionStore } from './store'
import './App.css'
import { useSelector } from 'react-redux'

const ScrollTop = lazy(() => import('./components/ScrollTop'))

const App = () => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const [keycloak, setKeycloak] = useState({})
  const [authenticated, setAuthenticated] = useState(null)
  // const [recordsTypes, setRecordsTypes] = useState([])
  // const [casesDefinitions, setCasesDefinitions] = useState([])
  const [menu, setMenu] = useState({ items: [] })

  useEffect(() => {
    const { keycloak } = sessionStore.bootstrap()

    keycloak.init({ onLoad: 'login-required' }).then((authenticated) => {
      setKeycloak(keycloak)
      setAuthenticated(authenticated)
      buildMenuItems(keycloak)
      RegisterInjectUserSession(keycloak)
      RegisteOptions(keycloak)
      forceLogoutIfUserNoMinimalRoleForSystem(keycloak)
    })

    keycloak.onAuthRefreshError = () => {
      window.location.reload()
    }

    keycloak.onTokenExpired = () => {
      keycloak
        .updateToken(70)
        .then((refreshed) => {
          if (refreshed) {
            console.info('Token refreshed: ' + refreshed)
            RegisterInjectUserSession(keycloak)
            RegisteOptions(keycloak)
          } else {
            console.info(
              'Token not refreshed, valid for ' +
                Math.round(
                  keycloak.tokenParsed.exp +
                    keycloak.timeSkew -
                    new Date().getTime() / 1000,
                ) +
                ' seconds',
            )
          }
        })
        .catch(() => {
          console.error('Failed to refresh token')
        })
    }
  }, [])

  async function forceLogoutIfUserNoMinimalRoleForSystem(keycloak) {
    if (!accountStore.hasAnyRole(keycloak)) {
      return keycloak.logout({ redirectUri: window.location.origin })
    }
  }

  useEffect(() => {
    if (keycloak && verticalChange) {
      buildMenuItems(keycloak)
    }
    // console.log(verticalChange)
  }, [verticalChange, keycloak])

  async function buildMenuItems(keycloak) {
    let allowedLinked = []
    const verticals = keycloak?.idTokenParsed?.verticals
    const selectedVertical = localStorage.getItem('verticalId').toLowerCase()

    // console.log('keycloak', verticals)
    if (verticals) {
      try {
        allowedLinked = JSON.parse(verticals)
      } catch (error) {
        console.error('Error parsing verticals JSON:', error)
        allowedLinked = []
      }
    } else {
      // console.log('No verticals found in idTokenParsed')
    }

    const allowedVerticalsMapping = allowedLinked.reduce((acc, obj) => {
      // Convert each key to lowercase for consistent matching.
      const key = Object.keys(obj)[0].toLowerCase()
      return { ...acc, [key]: obj[Object.keys(obj)[0]] }
    }, {})

    // console.log(allowedVerticalsMapping)
    // console.log(selectedVertical)

    // verticalChange?.selectedVertical?.toLowerCase()
    const allowedChildIds =
      (selectedVertical && allowedVerticalsMapping[selectedVertical]) || []
    // console.log(allowedChildIds)
    // Build the menu based on allowed verticals
    const menu = {
      items: [...menuItemsDefs.items],
    }
    menu.items = menu.items.filter((item) => item.id !== 'ta-plan')
    menu.items = menu.items.map((item) => {
      if (item.children && Array.isArray(item.children)) {
        item.children = item.children.filter((child) => child.id !== 'ta-plan')
        // If you have nested groups inside children, you can further map and filter
        item.children = item.children.map((group) => {
          if (group.children && Array.isArray(group.children)) {
            group.children = group.children.filter(
              (child) => child.id !== 'ta-plan',
            )
          }
          return group
        })
      }
      return item
    })
    menu.items = menu.items.map((item) => {
      if (item.id === 'utilities') {
        return {
          ...item,
          children: item.children.map((group) => {
            if (group.id === 'production-norms-plan') {
              return {
                ...group,
                children: group.children.filter((child) =>
                  allowedChildIds.length > 0
                    ? allowedChildIds.includes(child.id.toLowerCase())
                    : true,
                ),
              }
            }
            return group
          }),
        }
      }
      return item
    })
    // console.log(menu)
    // Safely determine if the user is a manager.
    // If keycloak.hasRealmRole is not a function, default to false.
    const isManagerUser =
      typeof keycloak.hasRealmRole === 'function'
        ? keycloak.hasRealmRole('manager')
        : false

    if (!isManagerUser) {
      delete menu.items[3]
    }

    return setMenu(menu)
  }

  return (
    keycloak &&
    authenticated && (
      <ThemeCustomization>
        <Suspense fallback={<div>Loading...</div>}>
          <ScrollTop>
            <SessionStoreProvider value={{ keycloak, menu }}>
              <ThemeRoutes
                keycloak={keycloak}
                authenticated={authenticated}
                // recordsTypes={recordsTypes}
                // casesDefinitions={casesDefinitions}
              />
            </SessionStoreProvider>
          </ScrollTop>
        </Suspense>
      </ThemeCustomization>
    )
  )
}

export default App
