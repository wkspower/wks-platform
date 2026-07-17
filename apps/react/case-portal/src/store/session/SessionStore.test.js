/* eslint-disable no-undef */
import store from './index'

jest.mock('keycloak-js')

test('should be initialize realm with subdmain when using dns', () => {
  window.location.assign('http://marketshare.wkspower.local/')

  const { keycloak, realm, clientId } = store.bootstrap()

  expect(keycloak).not.toBeNull()
  expect(realm).toEqual('marketshare')
  expect(clientId).toEqual('wks-portal')
})

test('should be initialize realm default realm when using localhost', () => {
  window.location.assign('http://localhost:3001/')

  const { keycloak, realm, clientId } = store.bootstrap()

  expect(keycloak).not.toBeNull()
  expect(realm).toEqual('localhost')
  expect(clientId).toEqual('wks-portal')
})

test('should be initialize default realm when using app dns', () => {
  window.location.assign('http://app.wkspower.local/')

  const { keycloak, realm, clientId } = store.bootstrap()

  expect(keycloak).not.toBeNull()
  expect(realm).toEqual('app')
  expect(clientId).toEqual('wks-portal')
})

// Config is evaluated at module load, so the runtime-override cases re-import
// the store with window.REALM already in place.
async function bootstrapFresh() {
  jest.resetModules()
  const { default: freshStore } = await import('./index')
  return freshStore.bootstrap()
}

test('explicit realm overrides hostname derivation (IP access)', async () => {
  window.location.assign('http://10.10.10.210:3001/')
  window.REALM = 'wks'

  const { keycloak, realm, clientId } = await bootstrapFresh()

  expect(keycloak).not.toBeNull()
  expect(realm).toEqual('wks')
  expect(clientId).toEqual('wks-portal')

  delete window.REALM
})

test('unresolved envsubst placeholder falls back to hostname derivation', async () => {
  window.location.assign('http://marketshare.wkspower.local/')
  window.REALM = '$__SERVER_REALM__'

  const { realm } = await bootstrapFresh()

  expect(realm).toEqual('marketshare')

  delete window.REALM
})
/* eslint-disable no-undef */
