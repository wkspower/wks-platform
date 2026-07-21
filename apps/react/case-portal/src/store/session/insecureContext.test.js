/* eslint-disable no-undef */
import { insecureContextInitOptions } from './insecureContext'

const originalDescriptor = Object.getOwnPropertyDescriptor(
  window,
  'isSecureContext',
)

function setSecureContext(value) {
  Object.defineProperty(window, 'isSecureContext', {
    value,
    configurable: true,
  })
}

afterEach(() => {
  if (originalDescriptor) {
    Object.defineProperty(window, 'isSecureContext', originalDescriptor)
  } else {
    delete window.isSecureContext
  }
  delete crypto.randomUUID
  jest.restoreAllMocks()
})

test('secure context: no init overrides, crypto untouched', () => {
  setSecureContext(true)

  expect(insecureContextInitOptions()).toEqual({})
  expect(crypto.randomUUID).toBeUndefined()
})

test('insecure context: disables PKCE and warns', () => {
  setSecureContext(false)
  const warn = jest.spyOn(console, 'warn').mockImplementation(() => {})

  expect(insecureContextInitOptions()).toEqual({ pkceMethod: false })
  expect(warn).toHaveBeenCalled()
})

test('insecure context: polyfills a valid RFC 4122 v4 randomUUID', () => {
  setSecureContext(false)
  jest.spyOn(console, 'warn').mockImplementation(() => {})

  insecureContextInitOptions()

  expect(typeof crypto.randomUUID).toBe('function')
  const uuid = crypto.randomUUID()
  expect(uuid).toMatch(
    /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/,
  )
  expect(crypto.randomUUID()).not.toEqual(uuid)
})

test('insecure context: an existing native randomUUID is not replaced', () => {
  setSecureContext(false)
  jest.spyOn(console, 'warn').mockImplementation(() => {})
  const native = () => 'native'
  crypto.randomUUID = native

  insecureContextInitOptions()

  expect(crypto.randomUUID).toBe(native)
})
