/*
 * WKS Case Configuration Standard — validator entry point.
 *
 * Loads the canonical JSON Schemas and exposes a small, dependency-light
 * validation API. The schemas in ../schemas are the single source of truth;
 * this module is the JavaScript-side consumer (the portal uses it via the
 * `@wkspower/case-config-schema` package; the backend validates the same
 * files from its own JSON Schema validator).
 */

const Ajv2020 = require('ajv/dist/2020');

/** Current version of the WKS Case Configuration Standard. */
const SCHEMA_VERSION = '1.0';

/** type key -> schema filename (relative to schemas/) */
const SCHEMA_FILES = {
  'case-definition': 'case-definition.schema.json',
  form: 'form.schema.json',
  'record-type': 'record-type.schema.json',
  queue: 'queue.schema.json',
};

/*
 * Schemas are required (not fs-read) so this module bundles cleanly in a
 * browser (webpack) as well as Node — the portal imports it directly.
 */
const schemas = {
  'case-definition': require('../schemas/case-definition.schema.json'),
  form: require('../schemas/form.schema.json'),
  'record-type': require('../schemas/record-type.schema.json'),
  queue: require('../schemas/queue.schema.json'),
};

const ajv = new Ajv2020({ allErrors: true, strict: false });
const validators = Object.fromEntries(
  Object.entries(schemas).map(([type, schema]) => [type, ajv.compile(schema)]),
);

/**
 * Validate a config document against its schema.
 * @param {('case-definition'|'form'|'record-type'|'queue')} type
 * @param {object} doc
 * @returns {{ valid: boolean, errors: Array }}
 */
function validate(type, doc) {
  const v = validators[type];
  if (!v) {
    throw new Error(`Unknown config type "${type}". Known: ${Object.keys(validators).join(', ')}`);
  }
  const valid = v(doc);
  return { valid, errors: valid ? [] : v.errors || [] };
}

/** Get the compiled ajv validator for a type (errors on its `.errors`). */
function getValidator(type) {
  const v = validators[type];
  if (!v) {
    throw new Error(`Unknown config type "${type}".`);
  }
  return v;
}

module.exports = {
  SCHEMA_VERSION,
  SCHEMA_FILES,
  schemas,
  validate,
  getValidator,
};
