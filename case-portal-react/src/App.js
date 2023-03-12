import ScrollTop from 'components/ScrollTop';
import Keycloak from 'keycloak-js';
import { useEffect, useState } from 'react';
import { ThemeRoutes } from 'routes';
import ThemeCustomization from 'themes';
import { SessionStoreProvider } from './SessionStoreContext';
import { CaseService, RecordService } from 'services';
import menuItemsDefs from 'menu-items';
import './App.css';
import { registerInjectUserSession } from 'plugins/InjectUserSession';

const App = () => {
    const [keycloak, setKeycloak] = useState({});
    const [authenticated, setAuthenticated] = useState(null);
    const [recordsTypes, setRecordsTypes] = useState([]);
    const [casesDefinitions, setCasesDefinitions] = useState([]);
    const [menu, setMenu] = useState({ items: [] });

    useEffect(() => {
        const keycloak = Keycloak({
            url: process.env.REACT_APP_KEYCLOAK_URL,
            realm: 'wks-platform',
            clientId: 'wks-portal'
        });

        keycloak.init({ onLoad: 'login-required' }).then((authenticated) => {
            setKeycloak(keycloak);
            setAuthenticated(authenticated);
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

    async function buildMenuItems(keycloak) {
        const menu = {
            items: [...menuItemsDefs.items]
        };

        await RecordService.getAllRecordTypes(keycloak).then((data) => {
            setRecordsTypes(data);

            data.forEach((element) => {
                menu.items[1].children
                    .filter((menu) => menu.id === 'record-list')[0]
                    .children.push({
                        id: element.id,
                        title: element.id,
                        type: 'item',
                        url: '/record-list/' + element.id,
                        breadcrumbs: true
                    });
            });
        });

        await CaseService.getCaseDefinitions(keycloak).then((data) => {
            setCasesDefinitions(data);

            data.forEach((element) => {
                menu.items[1].children
                    .filter((menu) => menu.id === 'case-list')[0]
                    .children.push({
                        id: element.id,
                        title: element.name,
                        type: 'item',
                        url: '/case-list/' + element.id,
                        breadcrumbs: true
                    });
            });
        });

        return setMenu(menu);
    }

    return (
        keycloak &&
        authenticated && (
            <ThemeCustomization>
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
            </ThemeCustomization>
        )
    );
};

export default App;
