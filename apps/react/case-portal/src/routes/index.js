import { useRoutes } from 'react-router-dom'
import { MainRoutes } from './MainRoutes'

export const ThemeRoutes = ({
  keycloak,
  authenticated,
  recordsTypes,
  casesDefinitions,
}) => {
  return useRoutes([
    MainRoutes(keycloak, authenticated, recordsTypes, casesDefinitions),
  ])
}
