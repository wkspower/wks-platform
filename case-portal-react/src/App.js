import ScrollTop from 'components/ScrollTop';
import { useEffect, useState } from 'react';
import { ThemeRoutes } from 'routes';
import ThemeCustomization from 'themes';
import { SessionStoreProvider } from './SessionStoreContext';
import { CaseService, RecordService, BpmService } from 'services';
import menuItemsDefs from 'menu';
import { registerInjectUserSession } from 'plugins/InjectUserSession';
import { accountStore, sessionStore } from './store';
import './App.css';

const App = () => {
    const [keycloak, setKeycloak] = useState({});
    const [authenticated, setAuthenticated] = useState(null);
    const [recordsTypes, setRecordsTypes] = useState([]);
    const [casesDefinitions, setCasesDefinitions] = useState([]);
    const [bpmEngine, setBpmEngine] = useState(null);
    const [menu, setMenu] = useState({ items: [] });

    useEffect(() => {
        const { keycloak } = sessionStore.bootstrap();

        keycloak.init({ onLoad: 'login-required' }).then((authenticated) => {
            setKeycloak(keycloak);
            setAuthenticated(authenticated);
            buildMenuItems(keycloak);
            registerInjectUserSession(keycloak);
            forceLogoutIfUserNoMinimalRoleForSystem(keycloak);
        });

        keycloak.onAuthRefreshError = () => {
            window.location.reload();
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

    async function forceLogoutIfUserNoMinimalRoleForSystem(keycloak) {
        if (!accountStore.hasAnyRole(keycloak)) {
            console.log('auto logout because user dont have any permission');
            return keycloak.logout({ redirectUri: window.location.origin });
        }
    }

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

        if (!accountStore.isManagerUser(keycloak)) {
            delete menu.items[2];
        }

        BpmService.getAll(keycloak).then((data) => {
            if (data.length) {
                setBpmEngine(data[0].id);
            }
        });

        return setMenu(menu);
    }

    return (
        keycloak &&
        authenticated && (
            <ThemeCustomization>
                <ScrollTop>
                    <SessionStoreProvider value={{ keycloak, menu, bpmEngine }}>
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
