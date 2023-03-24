import React, { useContext } from 'react';

const SessionStoreContext = React.createContext({});
export const SessionStoreProvider = SessionStoreContext.Provider;
export const SessionStoreConsumer = SessionStoreContext.Consumer;

export const withStore = (WrappedComponent) => (props) =>
    (
        <SessionStoreConsumer>
            {(stores) => <WrappedComponent {...props} {...stores} />}
        </SessionStoreConsumer>
    );

export const useSession = () => {
    const attrs = useContext(SessionStoreContext);
    return attrs['keycloak'];
};

export const useMenu = () => {
    const attrs = useContext(SessionStoreContext);
    return attrs['menu'];
};

export const useBpmEngine = () => {
    const attrs = useContext(SessionStoreContext);
    return attrs['bpmEngine'];
};

export default SessionStoreContext;
