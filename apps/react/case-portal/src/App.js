import { useEffect, useState, lazy, Suspense } from 'react'
import { ThemeRoutes } from './routes'
import ThemeCustomization from './themes'
import { SessionStoreProvider } from './SessionStoreContext'
import { CaseService, RecordService, MenuEventService } from 'services'
import menuItemsDefs from './menu'
import { RegisterInjectUserSession, RegisteOptions } from './plugins'
import { accountStore, sessionStore } from './store'
import RecordTypeChoice from './components/@formio/RecordTypeChoice'
import { Formio } from 'formiojs'
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

    keycloak.init({ onLoad: 'login-required' }).then((authenticated) => {
      setKeycloak(keycloak)
      setAuthenticated(authenticated)
      buildMenuItems(keycloak)
      RegisterInjectUserSession(keycloak)
      RegisteOptions(keycloak)
      forceLogoutIfUserNoMinimalRoleForSystem(keycloak)
      registerExtensionModulesFormio()

      const unsubscribe = MenuEventService.subscribeToMenuUpdates(() => {
        buildMenuItems(keycloak)
      })

      return () => {
        if (unsubscribe) unsubscribe()
      }
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

  function registerExtensionModulesFormio() {
    Formio.use(RecordTypeChoice)
  }

  async function forceLogoutIfUserNoMinimalRoleForSystem(keycloak) {
    if (!accountStore.hasAnyRole(keycloak)) {
      return keycloak.logout({ redirectUri: window.location.origin })
    }
  }

  function enableExternalLinkMenuItemIfRequired(menu, keycloak) {
    if (!accountStore.isManagerUser(keycloak)) {
      delete menu.items[2]
      return
    }

    if (!menu.items[2]?.children[0]?.children?.length) {
      delete menu.items[2]
    }
  }

  async function buildMenuItems(keycloak) {
    const menu = {
      items: [...menuItemsDefs.items],
    }

    if (menu.items[0].children) {
      const recordListMenu = menu.items[0].children.find(
        (menu) => menu.id === 'record-list',
      )
      if (recordListMenu) {
        recordListMenu.children = []
      }
    }

    await RecordService.getAllRecordTypes(keycloak).then((data) => {
      setRecordsTypes(data)

      data.forEach((element) => {
        menu.items[0].children
          .filter((menu) => menu.id === 'record-list')[0]
          .children.push({
            id: element.id,
            title: element.id,
            type: 'item',
            url: '/record-list/' + element.id,
            breadcrumbs: true,
          })
      })
    })

    if (menu.items[0].children) {
      const caseListMenu = menu.items[0].children.find(
        (menu) => menu.id === 'case-list',
      )
      if (caseListMenu) {
        caseListMenu.children = []
      }
    }

    await CaseService.getCaseDefinitions(keycloak).then((data) => {
      setCasesDefinitions(data)

      data.forEach((element) => {
        menu.items[0].children
          .filter((menu) => menu.id === 'case-list')[0]
          .children.push({
            id: element.id,
            title: element.name,
            type: 'item',
            url: '/case-list/' + element.id,
            breadcrumbs: true,
          })
      })
    })

    if (!accountStore.isManagerUser(keycloak)) {
      delete menu.items[1]
    }

    enableExternalLinkMenuItemIfRequired(menu, keycloak)

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
                recordsTypes={recordsTypes}
                casesDefinitions={casesDefinitions}
              />
            </SessionStoreProvider>
          </ScrollTop>
        </Suspense>
      </ThemeCustomization>
    )
  )
}

export default App
