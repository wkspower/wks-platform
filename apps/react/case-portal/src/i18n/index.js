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

import i18n from 'i18next'
import config from '../config'
import ptBR from './pt_br'
import enUS from './en_us'
import deDE from './de_de'
import {
  DEFAULT_LOCALE,
  STORAGE_KEY,
  SUPPORTED_LOCALES,
  matchLocale,
  resolveLocale,
} from './locales'

/**
 * localStorage is not always reachable — Safari private mode and some embedded
 * / third-party-cookie-blocked contexts throw on access rather than returning
 * null. This module runs before React (and therefore before any ErrorBoundary)
 * mounts, so an uncaught throw here is a blank white screen. Every access is
 * wrapped and a failure is treated as "no stored preference".
 */
const readStoredLocale = () => {
  try {
    return window.localStorage.getItem(STORAGE_KEY)
  } catch (err) {
    console.warn('Could not read the stored language preference', err)
    return null
  }
}

const writeStoredLocale = (code) => {
  try {
    window.localStorage.setItem(STORAGE_KEY, code)
  } catch (err) {
    console.warn('Could not persist the language preference', err)
  }
}

/**
 * Resolution order, most to least specific:
 *   1. the user's explicit stored choice
 *   2. the browser's preferred languages, in order (`navigator.languages` — not
 *      just `navigator.language`, and matched by prefix, so `de`, `de-AT` and
 *      `en-GB` all land somewhere sensible instead of silently defaulting)
 *   3. the deployment default in config.js
 *   4. the hard default
 */
const detectLocale = () => {
  const stored = matchLocale(readStoredLocale())
  if (stored) return stored

  const preferred = navigator.languages?.length
    ? navigator.languages
    : [navigator.language]
  const fromBrowser = preferred.map(matchLocale).find(Boolean)
  if (fromBrowser) return fromBrowser

  return resolveLocale(config.i18n)
}

i18n.init({
  resources: {
    'en-US': { translation: enUS },
    'pt-BR': { translation: ptBR },
    'de-DE': { translation: deDE },
  },
  lng: detectLocale(),
  fallbackLng: DEFAULT_LOCALE,
  supportedLngs: SUPPORTED_LOCALES.map((locale) => locale.code),
  // Keep i18n.language exactly as resolved. Our catalogs are keyed by full
  // BCP-47 tag, so i18next must not strip the region looking for a bare-language
  // bundle that does not exist. (This is also why `nonExplicitSupportedLngs` is
  // deliberately NOT set: it would resolve `en-US` down to `en` and every lookup
  // would fall through to the raw key. Normalising arbitrary browser tags onto a
  // supported one is matchLocale's job, before i18next ever sees them.)
  load: 'currentOnly',
  interpolation: {
    escapeValue: false,
  },
})

/**
 * Switch the active language. This is the only sanctioned way to do it —
 * calling `i18n.changeLanguage` directly skips persistence and the `lang`
 * attribute, and leaves the two out of sync with the UI.
 */
export const setLocale = (candidate) => {
  const code = resolveLocale(candidate)
  if (code === i18n.language) return Promise.resolve()

  writeStoredLocale(code)
  document.documentElement.lang = code
  return i18n.changeLanguage(code)
}

document.documentElement.lang = i18n.language

export default i18n
