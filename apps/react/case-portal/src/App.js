import { useEffect, useState, lazy, Suspense } from 'react'
import { ThemeRoutes } from './routes'
import ThemeCustomization from './themes'
import { SessionStoreProvider } from './SessionStoreContext'
import { CaseService, RecordService, MenuEventService } from 'services'
import menuItemsDefs from './menu'
import { buildMenu } from './menu/menuBuilder'
import { RegisterInjectUserSession, RegisteOptions } from './plugins'
import { accountStore, sessionStore } from './store'
import RecordTypeChoice from './components/@formio/RecordTypeChoice'
import { Formio } from 'formiojs'
import ErrorBoundary from './components/ErrorBoundary/ErrorBoundary'
import { NotificationProvider } from './components/Notification/NotificationContext'
import './App.css'

const ScrollTop = lazy(() => import('./components/ScrollTop'))

const App = () => {
  const [keycloak, setKeycloak] = useState({})
  const [authenticated, setAuthenticated] = useState(null)
  const [recordsTypes, setRecordsTypes] = useState([])
  const [casesDefinitions, setCasesDefinitions] = useState([])
  const [menu, setMenu] = useState({ items: [] })

  useEffect(() => {
    const { keycloak } = sessionStore.bootstrap()
    // Held across the async init so the effect's cleanup can actually run it.
    // Previously the cleanup was returned from inside the .then() callback — which
    // React ignores — so the menu subscription leaked on unmount/HMR.
    let unsubscribe

    keycloak.init({ onLoad: 'login-required' }).then((authenticated) => {
      setKeycloak(keycloak)
      setAuthenticated(authenticated)
      buildMenuItems(keycloak)
      RegisterInjectUserSession(keycloak)
      RegisteOptions(keycloak)
      forceLogoutIfUserNoMinimalRoleForSystem(keycloak)
      registerExtensionModulesFormio()

      unsubscribe = MenuEventService.subscribeToMenuUpdates(() => {
        buildMenuItems(keycloak)
      })
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

    return () => {
      if (unsubscribe) unsubscribe()
    }
  }, [])

  function registerExtensionModulesFormio() {
    Formio.use(RecordTypeChoice)
  }

  async function forceLogoutIfUserNoMinimalRoleForSystem(keycloak) {
    if (!accountStore.hasAnyRole(keycloak)) {
      return keycloak.logout({ redirectUri: window.location.origin })
    }
  }

  async function buildMenuItems(keycloak) {
    // Fetch the dynamic lists resiliently — a failing call should not blank the
    // whole navigation — then hand the pure assembly off to buildMenu().
    let recordTypes = []
    try {
      recordTypes = await RecordService.getAllRecordTypes(keycloak)
      setRecordsTypes(recordTypes)
    } catch (err) {
      console.error('Failed to load record types for the menu', err)
    }

    let caseDefinitions = []
    try {
      caseDefinitions = await CaseService.getCaseDefinitions(keycloak)
      setCasesDefinitions(caseDefinitions)
    } catch (err) {
      console.error('Failed to load case definitions for the menu', err)
    }

    return setMenu(
      buildMenu({
        menuItems: menuItemsDefs.items,
        recordTypes,
        caseDefinitions,
        isManager: accountStore.isManagerUser(keycloak),
      }),
    )
  }

  return (
    keycloak &&
    authenticated && (
      <ThemeCustomization>
        <NotificationProvider>
          <Suspense fallback={<div>Loading...</div>}>
            <ScrollTop>
              <SessionStoreProvider value={{ keycloak, menu }}>
                {/* Per-view boundary: a crash in the routed content shows a
                    recoverable message while the app shell stays up. */}
                <ErrorBoundary title='This view failed to render'>
                  <ThemeRoutes
                    keycloak={keycloak}
                    authenticated={authenticated}
                    recordsTypes={recordsTypes}
                    casesDefinitions={casesDefinitions}
                  />
                </ErrorBoundary>
              </SessionStoreProvider>
            </ScrollTop>
          </Suspense>
        </NotificationProvider>
      </ThemeCustomization>
    )
  )
}

export default App
