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
  const { verticalChange, sitePlantChange } = dataGridStore
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
    if (keycloak) {
      buildMenuItems(keycloak)
    }
  }, [verticalChange, sitePlantChange, keycloak])

  async function buildMenuItems(keycloak) {
    const menu = {
      items: [...menuItemsDefs.items],
    }

    // Get the selected vertical
    const selectedVertical = verticalChange?.verticalChange?.selectedVertical

    const alternateMapping = {
      PE: {
        'product-demand': 'Business Demand',
        'product-mcu-val': 'Production Volume Data',
        'shutdown-plan': 'Shutdown Activities',
        'slowdown-plan': 'Slowdown Activities',
        'ta-plan': 'Turnaround Activities',
        'production-norms': 'Production AOP',
      },
      MEG: {
        'product-demand': 'Business Demand',
        'product-mcu-val': 'Production Volume Data',
        'shutdown-plan': 'Shutdown Activities',
        'slowdown-plan': 'Slowdown Activities',
        'ta-plan': ' Turnaround Activities', // but we are hiding in meg
        'production-norms': 'Production AOP',
      },
      // ... add more vertical mappings if needed.
    }

    // eslint-disable-next-line no-constant-condition
    if (true) {
      // If vertical is MEG, hide the ta-plan item.
      menu.items = menu.items.map((item) => {
        if (item.id === 'utilities') {
          return {
            ...item,
            children: item.children.map((group) => {
              if (group.id === 'production-norms-plan') {
                return {
                  ...group,
                  children: group.children.filter(
                    (child) => child.id !== 'ta-plan',
                  ),
                }
              }
              return group
            }),
          }
        }
        return item
      })
    } else if (selectedVertical && alternateMapping[selectedVertical]) {
      // For non-MEG verticals with a defined alternate mapping, update the titles.
      const mapping = alternateMapping[selectedVertical]
      menu.items = menu.items.map((item) => {
        if (item.id === 'utilities') {
          return {
            ...item,
            children: item.children.map((group) => {
              if (group.id === 'production-norms-plan') {
                return {
                  ...group,
                  children: group.children.map((child) => {
                    // If an alternate title exists for this child's id, update the title.
                    if (mapping[child.id]) {
                      return { ...child, title: mapping[child.id] }
                    }
                    return child
                  }),
                }
              }
              return group
            }),
          }
        }
        return item
      })
    }

    // Additional modifications (e.g., manager check)
    if (!accountStore.isManagerUser(keycloak)) {
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
