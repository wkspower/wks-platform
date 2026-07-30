import {
  DEFAULT_LOCALE,
  SUPPORTED_LOCALES,
  localeEntry,
  matchLocale,
  resolveLocale,
} from './locales'

describe('matchLocale', () => {
  it('matches a supported code exactly', () => {
    expect(matchLocale('de-DE')).toBe('de-DE')
    expect(matchLocale('pt-BR')).toBe('pt-BR')
  })

  it('matches case-insensitively, because stored and browser values vary', () => {
    expect(matchLocale('DE-de')).toBe('de-DE')
    expect(matchLocale('pt-br')).toBe('pt-BR')
  })

  it('matches a bare language subtag', () => {
    expect(matchLocale('de')).toBe('de-DE')
    expect(matchLocale('en')).toBe('en-US')
  })

  it('matches a region we do not ship onto the language we do', () => {
    expect(matchLocale('de-AT')).toBe('de-DE')
    expect(matchLocale('en-GB')).toBe('en-US')
    expect(matchLocale('pt-PT')).toBe('pt-BR')
  })

  it('returns null for an unknown language so callers can keep looking', () => {
    expect(matchLocale('fr-FR')).toBeNull()
    expect(matchLocale('zz')).toBeNull()
  })

  it('returns null for junk input rather than throwing', () => {
    expect(matchLocale(undefined)).toBeNull()
    expect(matchLocale(null)).toBeNull()
    expect(matchLocale('')).toBeNull()
    expect(matchLocale('   ')).toBeNull()
    expect(matchLocale(42)).toBeNull()
    expect(matchLocale({})).toBeNull()
  })
})

describe('resolveLocale', () => {
  it('falls back to the default instead of returning null', () => {
    expect(resolveLocale('fr-FR')).toBe(DEFAULT_LOCALE)
    expect(resolveLocale(undefined)).toBe(DEFAULT_LOCALE)
  })

  it('still resolves what matchLocale would match', () => {
    expect(resolveLocale('de')).toBe('de-DE')
  })
})

describe('the registry itself', () => {
  it('exposes the default as a supported locale', () => {
    expect(SUPPORTED_LOCALES.map((l) => l.code)).toContain(DEFAULT_LOCALE)
  })

  it('gives every locale a library mapping, so no formatter lookup can miss', () => {
    SUPPORTED_LOCALES.forEach((locale) => {
      expect(locale.code).toMatch(/^[a-z]{2}-[A-Z]{2}$/)
      expect(locale.label).toBeTruthy()
      expect(locale.dateFns).toBeTruthy()
      expect(locale.moment).toBeTruthy()
      expect(locale.formio).toBeTruthy()
    })
  })

  it('localeEntry is always defined, even for junk', () => {
    expect(localeEntry('de-DE').label).toBe('Deutsch')
    expect(localeEntry('nonsense').code).toBe(DEFAULT_LOCALE)
    expect(localeEntry(undefined).code).toBe(DEFAULT_LOCALE)
  })
})
