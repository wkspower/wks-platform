/*
 * WKS Platform - Open-Source Project
 *
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 *
 * WKS Platform is licensed under the MIT License.
 *
 * © 2021 WKS Power. All rights reserved.
 *
 * For licensing information, see the LICENSE file in the root directory of the project.
 */

/**
 * The single place a locale code is spelled out.
 *
 * Codes are BCP-47 so they can be handed straight to Intl and to
 * `document.documentElement.lang`. The third-party libraries in the portal each
 * use their own identifiers, so every locale carries the mapping with it —
 * consumers ask this registry rather than keeping their own tables.
 *
 * `label` is deliberately an autonym (the language's own name for itself) and is
 * never translated: someone looking for their language reads it in that
 * language, not in whichever one the UI happens to be showing.
 */
export const SUPPORTED_LOCALES = [
  {
    code: 'en-US',
    label: 'English',
    dateFns: 'enUS',
    moment: 'en',
    formio: 'en',
  },
  {
    code: 'pt-BR',
    label: 'Português (Brasil)',
    dateFns: 'ptBR',
    moment: 'pt-br',
    formio: 'pt',
  },
  {
    code: 'de-DE',
    label: 'Deutsch',
    dateFns: 'de',
    moment: 'de',
    formio: 'de',
  },
]

export const DEFAULT_LOCALE = 'en-US'

/** localStorage key holding the user's explicit choice. */
export const STORAGE_KEY = 'wks.locale'

/**
 * Match an arbitrary language tag onto a supported locale, or `null` if none
 * fits.
 *
 * Handles the three shapes that actually turn up: an exact supported code, a
 * bare language subtag (`de` → `de-DE`), and a region we don't ship but whose
 * language we do (`de-AT` → `de-DE`, `en-GB` → `en-US`). Matching is
 * case-insensitive because browsers and stored values are not consistent about
 * it.
 *
 * Returning `null` rather than the default is what lets callers walk a list of
 * candidates (`navigator.languages`) and tell "no preference matched" apart from
 * "the first preference happened to be English".
 */
export const matchLocale = (candidate) => {
  if (!candidate || typeof candidate !== 'string') return null

  const wanted = candidate.trim().toLowerCase()
  if (!wanted) return null

  const exact = SUPPORTED_LOCALES.find(
    (locale) => locale.code.toLowerCase() === wanted,
  )
  if (exact) return exact.code

  const language = wanted.split('-')[0]
  const byLanguage = SUPPORTED_LOCALES.find(
    (locale) => locale.code.toLowerCase().split('-')[0] === language,
  )

  return byLanguage ? byLanguage.code : null
}

/**
 * Like {@link matchLocale} but always yields a usable code. Never throws — this
 * runs during module init, where a throw is a blank screen.
 */
export const resolveLocale = (candidate) =>
  matchLocale(candidate) || DEFAULT_LOCALE

/** The registry entry for a code, always defined (falls back to the default). */
export const localeEntry = (code) => {
  const resolved = resolveLocale(code)
  return SUPPORTED_LOCALES.find((locale) => locale.code === resolved)
}
