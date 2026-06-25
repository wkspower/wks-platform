const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const path = require('path');

const { validate, SCHEMA_VERSION } = require('../src');
const fx = require('./fixtures');

test('happy-path fixtures validate', () => {
  for (const [type, doc] of [
    ['case-definition', fx.validCaseDefinition],
    ['form', fx.validForm],
    ['queue', fx.validQueue],
    ['record-type', fx.validRecordType],
  ]) {
    const { valid, errors } = validate(type, doc);
    assert.ok(valid, `${type} should be valid: ${JSON.stringify(errors)}`);
  }
});

test('case-definition requires formKey', () => {
  const bad = { ...fx.validCaseDefinition };
  delete bad.formKey;
  assert.strictEqual(validate('case-definition', bad).valid, false);
});

test('case-hook action vocabulary is pinned', () => {
  const bad = JSON.parse(JSON.stringify(fx.validCaseDefinition));
  bad.caseHooks[0].actions[0].actionType = 'CASE_DO_SOMETHING_ELSE';
  assert.strictEqual(validate('case-definition', bad).valid, false);
});

test('stage-update action must carry newStage', () => {
  const bad = JSON.parse(JSON.stringify(fx.validCaseDefinition));
  delete bad.caseHooks[0].actions[0].newStage;
  assert.strictEqual(validate('case-definition', bad).valid, false);
});

test('form requires a structure', () => {
  const bad = { ...fx.validForm };
  delete bad.structure;
  assert.strictEqual(validate('form', bad).valid, false);
});

// Anti-drift: the shipped seed data must conform to the published Standard.
test('seed configs conform to the Standard', () => {
  const seedPath = path.join(
    __dirname,
    '..',
    '..',
    '..',
    'apps',
    'java',
    'services',
    'demo-data-loader',
    'data',
    'mongodb',
    'mongo-base-collections.json',
  );
  const seed = JSON.parse(fs.readFileSync(seedPath, 'utf8'));
  const checks = [
    ['case-definition', seed.caseDefinition || []],
    ['form', seed.form || []],
    ['queue', seed.queue || []],
  ];
  for (const [type, entries] of checks) {
    entries.forEach((entry, i) => {
      const { valid, errors } = validate(type, entry);
      assert.ok(valid, `seed ${type}[${i}] (${entry.id || entry.key}) invalid: ${JSON.stringify(errors)}`);
    });
  }
});

test('SCHEMA_VERSION is exported', () => {
  assert.strictEqual(SCHEMA_VERSION, '1.0');
});
