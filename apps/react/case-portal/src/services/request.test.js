import { json, nop, ApiError } from './request'

function makeResponse({
  status = 200,
  ok,
  body = {},
  contentType = 'application/json',
  text,
}) {
  return {
    status,
    ok: ok !== undefined ? ok : status >= 200 && status < 300,
    statusText: `status ${status}`,
    headers: { get: () => contentType },
    json: async () => body,
    text: async () => (text !== undefined ? text : JSON.stringify(body)),
  }
}

function makeKeycloak({ expired = true } = {}) {
  return {
    isTokenExpired: () => expired,
    logout: jest.fn(),
  }
}

describe('request.json', () => {
  it('resolves parsed JSON on a 2xx response', async () => {
    const resp = makeResponse({ status: 200, body: { hello: 'world' } })
    await expect(json(makeKeycloak(), resp)).resolves.toEqual({
      hello: 'world',
    })
  })

  it('rejects with an ApiError carrying status and body on a 400', async () => {
    const resp = makeResponse({ status: 400, body: { message: 'bad input' } })
    await expect(json(makeKeycloak(), resp)).rejects.toMatchObject({
      name: 'ApiError',
      status: 400,
      body: { message: 'bad input' },
    })
  })

  it('rejects on a 500 (previously masqueraded as success)', async () => {
    const resp = makeResponse({
      status: 500,
      ok: false,
      contentType: 'text/plain',
      text: 'boom',
    })
    const err = await json(makeKeycloak(), resp).catch((e) => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(500)
    expect(err.body).toBe('boom')
  })

  it('rejects and triggers logout on 401 with an expired token', async () => {
    const keycloak = makeKeycloak({ expired: true })
    const resp = makeResponse({ status: 401, ok: false })
    await expect(json(keycloak, resp)).rejects.toMatchObject({ status: 401 })
    expect(keycloak.logout).toHaveBeenCalledTimes(1)
  })

  it('does not logout on 401 when the token is still valid', async () => {
    const keycloak = makeKeycloak({ expired: false })
    const resp = makeResponse({ status: 401, ok: false })
    await expect(json(keycloak, resp)).rejects.toMatchObject({ status: 401 })
    expect(keycloak.logout).not.toHaveBeenCalled()
  })
})

describe('request.nop', () => {
  it('resolves the raw Response on success', async () => {
    const resp = makeResponse({ status: 204, ok: true })
    await expect(nop(makeKeycloak(), resp)).resolves.toBe(resp)
  })

  it('rejects with an ApiError on a non-OK status', async () => {
    const resp = makeResponse({
      status: 409,
      ok: false,
      body: { message: 'conflict' },
    })
    await expect(nop(makeKeycloak(), resp)).rejects.toMatchObject({
      name: 'ApiError',
      status: 409,
    })
  })

  // The case engine's ErrorResponse names the field `errorMessage`, so reading
  // only `message` reduced every server explanation to the bare status text —
  // and a rejected BPMN deployment arrived at the modeler saying nothing useful.
  it('surfaces the engine ErrorResponse errorMessage in the error message', async () => {
    const resp = makeResponse({
      status: 400,
      ok: false,
      body: {
        error: 'Bad Request',
        errorMessage: 'ENGINE-12018 History Time To Live (TTL) cannot be null',
      },
    })

    await expect(nop(makeKeycloak(), resp)).rejects.toThrow(/ENGINE-12018/)
  })

  it('falls back to a plain-text error body', async () => {
    const resp = makeResponse({
      status: 500,
      ok: false,
      contentType: 'text/plain',
      text: 'boom',
    })

    await expect(nop(makeKeycloak(), resp)).rejects.toThrow(/boom/)
  })
})
