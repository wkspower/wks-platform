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
    ['board-config', fx.validBoardConfig],
  ]) {
    const { valid, errors } = validate(type, doc);
    assert.ok(valid, `${type} should be valid: ${JSON.stringify(errors)}`);
  }
});

test('board-config requires caseDefinitionId', () => {
  const bad = { ...fx.validBoardConfig };
  delete bad.caseDefinitionId;
  assert.strictEqual(validate('board-config', bad).valid, false);
});

test('case-definition no longer carries kanbanConfig in fixtures', () => {
  // kanbanConfig was removed from the contract in 2.0 (board-config.schema.json).
  assert.strictEqual(fx.validCaseDefinition.kanbanConfig, undefined);
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

test('document requirement must carry id and label', () => {
  const bad = JSON.parse(JSON.stringify(fx.validCaseDefinition));
  delete bad.requiredDocuments[0].label;
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
  assert.strictEqual(SCHEMA_VERSION, '2.3');
});

test('a stage accepts milestones (schema 2.2)', () => {
  const { valid, errors } = validate('case-definition', {
    schemaVersion: '2.2',
    id: 'asyl-verfahren',
    name: 'Asylverfahren',
    formKey: 'asyl-verfahren-form',
    stages: [
      {
        id: '0',
        index: 0,
        name: 'Vorverfahren (NPOL/LPOL)',
        milestones: [
          { id: 'person-erfasst', name: 'Person erfasst' },
          {
            id: 'antrag-gestellt',
            name: 'Antrag gestellt',
            note: 'Sobald beide Tasks erledigt',
            sourceElementId: 'PlanItem_0fjixce',
          },
        ],
      },
    ],
  });
  assert.ok(valid, `expected valid, got ${JSON.stringify(errors)}`);
});

test('a milestone requires id and name', () => {
  const withoutName = validate('case-definition', {
    id: 'c',
    name: 'C',
    formKey: 'f',
    stages: [{ id: '0', index: 0, name: 'S', milestones: [{ id: 'm' }] }],
  });
  assert.ok(!withoutName.valid, 'a milestone with no name should be rejected');
});

// Milestones are additive: a pre-2.2 definition that declares none stays valid,
// which is what lets the version bump land without touching existing config.
test('a stage without milestones is still valid', () => {
  const { valid, errors } = validate('case-definition', {
    schemaVersion: '2.1',
    id: 'c',
    name: 'C',
    formKey: 'f',
    stages: [{ id: '0', index: 0, name: 'S' }],
  });
  assert.ok(valid, `expected valid, got ${JSON.stringify(errors)}`);
});
