const configFactory = require('../webpack.config')

/**
 * Guards the cache-busting contract.
 *
 * Production bundles must carry a content hash. Without one every build emits
 * the same `build/<id>.js` names, and because those assets are served with a
 * long-lived cache, a browser holding the previous build keeps running it after
 * an image upgrade — a shipped fix then looks like it never landed.
 */
describe('webpack production output', () => {
  const production = configFactory({}, { mode: 'production' })

  it('content-hashes the entry bundle', () => {
    expect(production.output.filename).toContain('[contenthash]')
  })

  it('content-hashes split chunks too', () => {
    // splitChunks is on, so most of the app ships as chunks rather than the
    // entry bundle — hashing only `filename` would leave them cacheable-stale.
    expect(production.output.chunkFilename).toContain('[contenthash]')
  })

  it('clears stale hashed files between builds', () => {
    expect(production.output.clean).toBe(true)
  })
})

describe('webpack development output', () => {
  const development = configFactory({}, { mode: 'development' })

  it('keeps stable names so the dev server can hot update', () => {
    // Content hashes are incompatible with hot updates; dev is served from
    // memory and never cached, so it doesn't need them.
    expect(development.output.filename).not.toContain('[contenthash]')
  })
})

describe('webpack config shape', () => {
  it('is a factory, so mode can drive the output naming', () => {
    expect(typeof configFactory).toBe('function')
  })

  it('tolerates being called without argv', () => {
    // `webpack --mode=production` passes argv, but plain `require` of the
    // config (tooling, editors) may not.
    expect(() => configFactory({})).not.toThrow()
  })
})
