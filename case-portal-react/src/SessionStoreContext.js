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
    return useContext(SessionStoreContext);
};

export default SessionStoreContext;
