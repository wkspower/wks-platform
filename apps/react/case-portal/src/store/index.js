import { configureStore } from '@reduxjs/toolkit';
import reducers from './reducers';
import accountStore from './account';
import sessionStore from './session';

const store = configureStore({
  reducer: reducers,
});

const { dispatch } = store;

export { store, dispatch, accountStore, sessionStore };
