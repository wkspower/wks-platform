import { CmmnImportService } from './CmmnImportService'

/**
 * The response helpers take (keycloak, response) — passing only the response makes
 * every call fail with a bare "Cannot read properties of undefined (reading
 * 'status')", which looks like a server problem rather than a wiring mistake. These
 * pin the call shape.
 */
describe('CmmnImportService', () => {
  const keycloak = { token: 'a-token', isTokenExpired: () => false }
  const okResponse = (body) => ({
    ok: true,
    status: 200,
    json: async () => body,
    headers: { get: () => 'application/json' },
  })

  beforeEach(() => {
    global.fetch = jest.fn()
  })

  afterEach(() => {
    delete global.fetch
  })

  it('returns the parsed import result', async () => {
    const result = { dryRun: true, caseDefinition: { id: 'asyl-verfahren' } }
    global.fetch.mockResolvedValue(okResponse(result))

    await expect(
      CmmnImportService.import(keycloak, '<cmmn/>', { dryRun: true }),
    ).resolves.toEqual(result)
  })

  it('posts the model as XML with the bearer token', async () => {
    global.fetch.mockResolvedValue(okResponse({}))

    await CmmnImportService.import(keycloak, '<cmmn:definitions/>')

    const [url, options] = global.fetch.mock.calls[0]
    expect(url).toContain('/case-definition/import/cmmn')
    expect(options.method).toBe('POST')
    expect(options.headers['Content-Type']).toBe('application/xml')
    expect(options.headers.Authorization).toBe('Bearer a-token')
    // The raw model is sent as-is, not wrapped or re-encoded.
    expect(options.body).toBe('<cmmn:definitions/>')
  })

  it('defaults to a committing import and honours dryRun', async () => {
    global.fetch.mockResolvedValue(okResponse({}))

    await CmmnImportService.import(keycloak, '<cmmn/>')
    expect(global.fetch.mock.calls[0][0]).toContain('dryRun=false')

    await CmmnImportService.import(keycloak, '<cmmn/>', { dryRun: true })
    expect(global.fetch.mock.calls[1][0]).toContain('dryRun=true')
  })

  it('surfaces the engine explanation rather than a generic failure', async () => {
    global.fetch.mockResolvedValue({
      ok: false,
      status: 400,
      statusText: 'Bad Request',
      headers: { get: () => 'application/json' },
      json: async () => ({
        errorMessage:
          'That file is a rendered diagram (SVG), not a model. Export the model as CMMN 1.1 XML.',
      }),
    })

    await expect(CmmnImportService.import(keycloak, '<svg/>')).rejects.toThrow(
      /rendered diagram/,
    )
  })

  it('rejects a conflict with a usable message', async () => {
    global.fetch.mockResolvedValue({
      ok: false,
      status: 409,
      statusText: 'Conflict',
      headers: { get: () => 'application/json' },
      json: async () => ({
        errorMessage: 'A record with the same identifier already exists',
      }),
    })

    await expect(CmmnImportService.import(keycloak, '<cmmn/>')).rejects.toThrow(
      /already exists/,
    )
  })
})
