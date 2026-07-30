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

import { StorageService } from 'plugins/storage'
import { getFormioLocale } from '../../i18n/formatters'

/**
 * form.io ships only an English translation table. A per-language map handed to
 * `options.i18n` is merged over that built-in `en`, so only the keys we care
 * about need authoring — anything omitted falls back to English rather than
 * rendering a raw key.
 *
 * These cover the chrome form.io renders itself: button labels and the
 * client-side validation messages. Field labels come from the form's own JSON
 * schema and are shown as authored.
 */
const FORMIO_BUNDLE = {
  de: {
    required: '{{field}} ist ein Pflichtfeld',
    pattern: '{{field}} entspricht nicht dem erwarteten Format',
    minLength: '{{field}} muss mindestens {{length}} Zeichen lang sein',
    maxLength: '{{field}} darf höchstens {{length}} Zeichen lang sein',
    min: '{{field}} darf nicht kleiner als {{min}} sein',
    max: '{{field}} darf nicht größer als {{max}} sein',
    invalid_email: '{{field}} muss eine gültige E-Mail-Adresse sein',
    invalid_date: '{{field}} ist kein gültiges Datum',
    invalid_regex: '{{field}} entspricht nicht dem erwarteten Muster',
    unique: '{{field}} muss eindeutig sein',
    submit: 'Absenden',
    cancel: 'Abbrechen',
    next: 'Weiter',
    previous: 'Zurück',
    complete: 'Abschließen',
    error: 'Bitte korrigieren Sie die markierten Felder',
    browse: 'Durchsuchen',
    dropFilesToAttach: 'Dateien hierher ziehen oder',
    removeFile: 'Datei entfernen',
  },
  pt: {
    required: '{{field}} é obrigatório',
    pattern: '{{field}} não corresponde ao formato esperado',
    minLength: '{{field}} deve ter ao menos {{length}} caracteres',
    maxLength: '{{field}} deve ter no máximo {{length}} caracteres',
    min: '{{field}} não pode ser menor que {{min}}',
    max: '{{field}} não pode ser maior que {{max}}',
    invalid_email: '{{field}} deve ser um e-mail válido',
    invalid_date: '{{field}} não é uma data válida',
    invalid_regex: '{{field}} não corresponde ao padrão esperado',
    unique: '{{field}} deve ser único',
    submit: 'Enviar',
    cancel: 'Cancelar',
    next: 'Próximo',
    previous: 'Anterior',
    complete: 'Concluir',
    error: 'Por favor, corrija os campos destacados',
    browse: 'Procurar',
    dropFilesToAttach: 'Arraste arquivos aqui ou',
    removeFile: 'Remover arquivo',
  },
}

/**
 * Build the options object for a form.io `<Form>` / `<FormBuilder>`.
 *
 * Every render site goes through this so the storage plugin and the active
 * language are wired consistently. `extra` is spread last, so a call site can
 * still override anything (`readOnly`, `noNewEdit`, validation flags, …).
 *
 * Note that form.io reads `language` and `i18n` when the webform is
 * *constructed*, so changing the language does not retranslate a mounted form —
 * App.js remounts the routed subtree on a language change, which is what makes
 * this take effect.
 */
export const getFormioOptions = (extra = {}) => ({
  fileService: new StorageService(),
  language: getFormioLocale(),
  i18n: FORMIO_BUNDLE,
  ...extra,
})

export default getFormioOptions
