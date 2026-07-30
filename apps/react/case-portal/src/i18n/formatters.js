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

import {
  de as dateFnsDe,
  enUS as dateFnsEnUS,
  ptBR as dateFnsPtBR,
} from 'date-fns/locale'
import i18n from './index'
import { localeEntry } from './locales'

// moment resolves locales from its own global registry, and a locale it has not
// loaded silently falls back to English rather than erroring. Importing the
// bundles here — once, next to the code that selects them — is what keeps
// "German relative dates" from quietly degrading to English in production.
import 'moment/locale/de'
import 'moment/locale/pt-br'

const DATE_FNS_LOCALES = {
  enUS: dateFnsEnUS,
  ptBR: dateFnsPtBR,
  de: dateFnsDe,
}

/**
 * Locale-aware formatting helpers, all keyed off the active i18next language so
 * callers never keep their own locale tables. Three date mechanisms coexist in
 * the portal (date-fns, moment, and native Intl); consolidating them is a
 * separate concern — these just make each of them follow the chosen language.
 */
export const getDateFnsLocale = () =>
  DATE_FNS_LOCALES[localeEntry(i18n.language).dateFns]

export const getMomentLocale = () => localeEntry(i18n.language).moment

/** BCP-47 tag for `Intl` / `toLocaleDateString` and friends. */
export const getIntlLocale = () => localeEntry(i18n.language).code

/** form.io's own language identifier for `options.language`. */
export const getFormioLocale = () => localeEntry(i18n.language).formio
