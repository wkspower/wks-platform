import { useEffect, useState, lazy, Suspense } from 'react'
import { useTranslation } from 'react-i18next'
import { ThemeRoutes } from './routes'
import ThemeCustomization from './themes'
import { SessionStoreProvider } from './SessionStoreContext'
import { CaseService, RecordService, MenuEventService } from 'services'
import { getMenuItems } from './menu'
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
  const {
    i18n: { language },
  } = useTranslation()

  useEffect(() => {
    const { keycloak, initOptions } = sessionStore.bootstrap()
    // Held across the async init so the effect's cleanup can actually run it.
    // Previously the cleanup was returned from inside the .then() callback — which
    // React ignores — so the menu subscription leaked on unmount/HMR.
    let unsubscribe

    keycloak
      .init({ onLoad: 'login-required', ...initOptions })
      .then((authenticated) => {
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

  // Fetch the dynamic lists resiliently — a failing call should not blank the
  // whole navigation. Assembly is deliberately NOT done here: it lives in the
  // effect below so that switching language re-labels the nav without re-hitting
  // the backend.
  async function buildMenuItems(keycloak) {
    try {
      setRecordsTypes(await RecordService.getAllRecordTypes(keycloak))
    } catch (err) {
      console.error('Failed to load record types for the menu', err)
    }

    try {
      setCasesDefinitions(await CaseService.getCaseDefinitions(keycloak))
    } catch (err) {
      console.error('Failed to load case definitions for the menu', err)
    }
  }

  // Re-assemble whenever the fetched lists or the active language change. Safe to
  // run repeatedly: buildMenu() clones before mutating, so it never accumulates
  // state across builds.
  useEffect(() => {
    if (!authenticated) return

    setMenu(
      buildMenu({
        menuItems: getMenuItems().items,
        recordTypes: recordsTypes,
        caseDefinitions: casesDefinitions,
        isManager: accountStore.isManagerUser(keycloak),
      }),
    )
  }, [recordsTypes, casesDefinitions, keycloak, authenticated, language])

  return (
    keycloak &&
    authenticated && (
      <ThemeCustomization>
        <NotificationProvider>
          <Suspense fallback={<div>Loading...</div>}>
            <ScrollTop>
              <SessionStoreProvider value={{ keycloak, menu }}>
                {/* Per-view boundary: a crash in the routed content shows a
                    recoverable message while the app shell stays up.

                    Keyed on the active language so a switch remounts the routed
                    subtree. Several things only read the locale once and would
                    otherwise stay in the old language: form.io reads
                    options.language at construction, and views that formatted
                    dates or mapped status labels while fetching hold already
                    translated strings in state. Remounting makes them refetch and
                    re-render. The key sits INSIDE SessionStoreProvider so the
                    Keycloak instance and session are untouched. */}
                <ErrorBoundary
                  key={language}
                  title='This view failed to render'
                >
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
