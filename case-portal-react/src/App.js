// project import
import ScrollTop from 'components/ScrollTop';
import Keycloak from 'keycloak-js';
import { useEffect, useState } from 'react';
import { ThemeRoutes } from 'routes';
import ThemeCustomization from 'themes';
import './App.css';

// ==============================|| APP - THEME, ROUTER, LOCAL  ||============================== //

const App = () => {
    const [keycloak, setKeycloak] = useState();
    const [authenticated, setAuthenticated] = useState(null);
    const [recordsTypes, setRecordsTypes] = useState([]);

    useEffect(() => {
        const keycloak = Keycloak({
            url: process.env.REACT_APP_KEYCLOAK_URL,
            realm: 'wks-platform',
            clientId: 'wks-portal'
        });
        keycloak.init({ onLoad: 'login-required' }).then((authenticated) => {
            setKeycloak(keycloak);
            setAuthenticated(authenticated);
        });

        fetch(process.env.REACT_APP_API_URL + '/record-type/')
            .then((response) => response.json())
            .then((data) => {
                setRecordsTypes(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, []);

    return (
        keycloak &&
        authenticated && (
            <ThemeCustomization>
                <ScrollTop>
                    <ThemeRoutes keycloak={keycloak} authenticated={authenticated} recordsTypes={recordsTypes} />
                </ScrollTop>
            </ThemeCustomization>
        )
    );
};

export default App;
