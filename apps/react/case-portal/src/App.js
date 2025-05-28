import { useEffect, useState, lazy, Suspense } from 'react'
import { ThemeRoutes } from './routes'
import ThemeCustomization from './themes'
import { SessionStoreProvider } from './SessionStoreContext'
// import {
//   CaseService,
//   //  RecordService
// } from 'services'
import { RegisterInjectUserSession, RegisteOptions } from './plugins'
import { accountStore, sessionStore } from './store'
import './App.css'
import './extra-css.css'
import './data-grid-css.css'
import './jio-grid-style.css'
// import '@progress/kendo-theme-default/dist/all.css'
// import '@progress/kendo-theme-bootstrap/dist/all.css'
import '@progress/kendo-theme-fluent/dist/all.css'

// import { useSelector } from 'react-redux'
import Layout from 'layout/FooterLayout/index'
import { MenuProvider } from 'menu/menuProvider'

const ScrollTop = lazy(() => import('./components/ScrollTop'))

const App = () => {
  const [keycloak, setKeycloak] = useState({})
  const [authenticated, setAuthenticated] = useState(null)
  // const [recordsTypes, setRecordsTypes] = useState([])
  // const [casesDefinitions, setCasesDefinitions] = useState([])

  useEffect(() => {
    const { keycloak } = sessionStore.bootstrap()

    keycloak.init({ onLoad: 'login-required' }).then((authenticated) => {
      setKeycloak(keycloak)
      setAuthenticated(authenticated)
      // buildMenuItems(keycloak)
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

  return (
    keycloak &&
    authenticated && (
      <ThemeCustomization>
        <Layout>
          <Suspense fallback={<div>Loading...</div>}>
            <ScrollTop>
              <SessionStoreProvider value={{ keycloak }}>
                <MenuProvider>
                  <ThemeRoutes
                    keycloak={keycloak}
                    authenticated={authenticated}

                    // recordsTypes={recordsTypes}
                    // casesDefinitions={casesDefinitions}
                  />
                </MenuProvider>
              </SessionStoreProvider>
            </ScrollTop>
          </Suspense>
        </Layout>
      </ThemeCustomization>
    )
  )
}
export default App
