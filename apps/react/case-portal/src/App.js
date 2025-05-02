import { useEffect, useState, lazy, Suspense } from 'react'
import { ThemeRoutes } from './routes'
import ThemeCustomization from './themes'
import { SessionStoreProvider } from './SessionStoreContext'
// import {
//   CaseService,
//   //  RecordService
// } from 'services'
// import menuItemsDefs from './menu'
import { RegisterInjectUserSession, RegisteOptions } from './plugins'
import { accountStore, sessionStore } from './store'
import './App.css'
import './extra-css.css'
import './data-grid-css.css'
import './jio-grid-style.css'
// import { useSelector } from 'react-redux'
import Layout from 'layout/FooterLayout/index'
import useMenuItems from 'menu/index'

const ScrollTop = lazy(() => import('./components/ScrollTop'))

const App = () => {
  // const dataGridStore = useSelector((state) => state.dataGridStore)
  // const { verticalChange } = dataGridStore
  const [keycloak, setKeycloak] = useState({})
  const [authenticated, setAuthenticated] = useState(null)
  // const [recordsTypes, setRecordsTypes] = useState([])
  // const [casesDefinitions, setCasesDefinitions] = useState([])
  const [menu, setMenu] = useState({ items: [] })
  const { items: menuItems } = useMenuItems()

  useEffect(() => {
    const { keycloak } = sessionStore.bootstrap()

    keycloak.init({ onLoad: 'login-required' }).then((authenticated) => {
      setKeycloak(keycloak)
      setAuthenticated(authenticated)
      buildMenuItems(menuItems)
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

  // useEffect(() => {
  //   console.log(keycloak)
  // }, [])

  async function buildMenuItems(menuItems) {
    // console.log(menuItems)
    const menu = { items: [...menuItems] }
    // …filter by roles, inject dynamic screens, etc…
    // console.log(menu)

    const isManagerUser =
      typeof keycloak.hasRealmRole === 'function'
        ? keycloak.hasRealmRole('manager')
        : false

    if (!isManagerUser) {
      delete menu.items[3]
    }

    //   return setMenu(menu)
    setMenu(menu)
  }

  return (
    keycloak &&
    authenticated && (
      <ThemeCustomization>
        <Layout>
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
        </Layout>
      </ThemeCustomization>
    )
  )
}
export default App
