/* eslint-disable no-undef */
import store from './index';

jest.mock('keycloak-js');

test('should be initialize realm with subdmain when using dns', () => {
  window.location.assign('http://marketshare.wkspower.local/');

  const { keycloak, realm, clientId } = store.bootstrap();

  expect(keycloak).not.toBeNull();
  expect(realm).toEqual('marketshare');
  expect(clientId).toEqual('wks-portal');
});

test('should be initialize realm default realm when using localhost', () => {
  window.location.assign('http://localhost:3001/');

  const { keycloak, realm, clientId } = store.bootstrap();

  expect(keycloak).not.toBeNull();
  expect(realm).toEqual('localhost');
  expect(clientId).toEqual('wks-portal');
});

test('should be initialize default realm when using app dns', () => {
  window.location.assign('http://app.wkspower.local/');

  const { keycloak, realm, clientId } = store.bootstrap();

  expect(keycloak).not.toBeNull();
  expect(realm).toEqual('app');
  expect(clientId).toEqual('wks-portal');
});
/* eslint-disable no-undef */
