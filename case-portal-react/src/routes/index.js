import { useRoutes } from 'react-router-dom';

// project import
import LoginRoutes from './LoginRoutes';
import { MainRoutes } from './MainRoutes';

// ==============================|| ROUTING RENDER ||============================== //

export const ThemeRoutes = ({ keycloak, authenticated, recordsTypes, casesDefinitions }) => {
    return useRoutes([MainRoutes(keycloak, authenticated, recordsTypes, casesDefinitions), LoginRoutes]);
};
