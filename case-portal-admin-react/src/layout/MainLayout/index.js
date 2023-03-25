import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Outlet } from 'react-router-dom';
import { Box, Toolbar } from '@mui/material';
import Breadcrumbs from 'components/@extended/Breadcrumbs';
import menuItemsDefs from 'menu-items';
import Drawer from './Drawer';
import Header from './Header';
import { openDrawer } from 'store/reducers/menu';
import Keycloak from 'keycloak-js';
import { SessionStoreProvider } from 'SessionStoreContext';
import { registerInjectUserSession } from 'plugins/InjectUserSession';
import Config from '../../config';

const MainLayout = () => {
    const [keycloak, setKeycloak] = useState();
    const [authenticated, setAuthenticated] = useState(null);
    const { drawerOpen } = useSelector((state) => state.menu);
    const [open, setOpen] = useState(drawerOpen);
    const [menu, setMenu] = useState({ items: [] });
    const dispatch = useDispatch();

    useEffect(() => {
        const keycloak = Keycloak({
            url: Config.LoginUrl,
            realm: 'wks-platform',
            clientId: 'wks-portal-admin'
        });

        keycloak.init({ onLoad: 'login-required' }).then((authenticaded) => {
            setKeycloak(keycloak);
            setAuthenticated(authenticaded);
            buildMenuItems(keycloak);
            registerInjectUserSession(keycloak);
        });

        keycloak.onAuthRefreshError = () => {
            keycloak.logout({ redirectUri: window.location.origin });
        };

        keycloak.onTokenExpired = () => {
            console.log('Token expired');

            keycloak
                .updateToken(70)
                .then((refreshed) => {
                    if (refreshed) {
                        console.info('Token refreshed: ' + refreshed);
                        registerInjectUserSession(keycloak);
                    } else {
                        console.info(
                            'Token not refreshed, valid for ' +
                                Math.round(
                                    keycloak.tokenParsed.exp +
                                        keycloak.timeSkew -
                                        new Date().getTime() / 1000
                                ) +
                                ' seconds'
                        );
                    }
                })
                .catch(() => {
                    console.error('Failed to refresh token');
                });
        };
    }, []);

    const handleDrawerToggle = () => {
        setOpen(!open);
        dispatch(openDrawer({ drawerOpen: !open }));
    };

    function buildMenuItems() {
        const menu = {
            items: [...menuItemsDefs.items]
        };
        return setMenu(menu);
    }

    return (
        keycloak &&
        authenticated && (
            <SessionStoreProvider value={{ keycloak, menu }}>
                <Box sx={{ display: 'flex', width: '100%' }}>
                    <Header
                        open={open}
                        handleDrawerToggle={handleDrawerToggle}
                        keycloak={keycloak}
                    />
                    <Drawer open={open} handleDrawerToggle={handleDrawerToggle} />
                    <Box component="main" sx={{ width: '100%', flexGrow: 1, p: { xs: 2, sm: 3 } }}>
                        <Toolbar />
                        <Breadcrumbs navigation={menu} divider={false} />
                        <Outlet />
                    </Box>
                </Box>
            </SessionStoreProvider>
        )
    );
};

export default MainLayout;
