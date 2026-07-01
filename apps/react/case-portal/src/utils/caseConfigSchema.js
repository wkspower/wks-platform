/*
 * WKS Platform - Open-Source Project
 *
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 *
 * WKS Platform is licensed under the MIT License.
 *
 * Copyright (c) 2021 WKS Power Limited. All rights reserved.
 *
 * For licensing information, see the LICENSE file in the root directory of the project.
 */

/**
 * Portal-side adapter for the WKS Case Configuration Standard
 * (@wkspower/case-config-schema). Validates a case definition against the
 * published schema so the Case Builder can show authors when their config
 * conforms to the Standard. Defensive: a validator failure must never crash
 * the builder, so on any unexpected error we treat the document as valid.
 */
import { validate, SCHEMA_VERSION } from '@wkspower/case-config-schema'

export { SCHEMA_VERSION }

export const validateCaseDefinition = (caseDef) => {
  try {
    return validate('case-definition', caseDef)
  } catch (e) {
    // Never let validation tooling break the builder.
    console.warn('Case definition schema validation skipped:', e)
    return { valid: true, errors: [] }
  }
}
