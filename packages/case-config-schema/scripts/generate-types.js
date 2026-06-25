/*
 * Derivation seam (facet 2): generate TypeScript types FROM the canonical
 * schemas. This is the first proof that the Standard is a generative asset,
 * not just a validation target. Run: `yarn generate` (or `npm run generate`).
 *
 * The boundary is deliberately small. Future derivations (Java DTOs, example
 * fixtures, a `@wkspower/wks-components` schema-driven UI layer) plug in here
 * the same way — read schemas/, emit an artifact — without touching consumers.
 */

const fs = require('fs');
const path = require('path');
const { compile } = require('json-schema-to-typescript');

const SCHEMA_DIR = path.join(__dirname, '..', 'schemas');
const OUT = path.join(__dirname, '..', 'dist', 'types.d.ts');

const TARGETS = [
  ['CaseDefinition', 'case-definition.schema.json'],
  ['Form', 'form.schema.json'],
  ['RecordType', 'record-type.schema.json'],
  ['Queue', 'queue.schema.json'],
];

async function main() {
  fs.mkdirSync(path.dirname(OUT), { recursive: true });
  const parts = [
    '/* AUTO-GENERATED from @wkspower/case-config-schema. Do not edit by hand. */',
    '/* Regenerate with `npm run generate`. Source of truth: schemas/*.schema.json */',
    '',
  ];
  for (const [name, file] of TARGETS) {
    const schema = JSON.parse(fs.readFileSync(path.join(SCHEMA_DIR, file), 'utf8'));
    const ts = await compile(schema, name, {
      bannerComment: '',
      additionalProperties: true,
      declareExternallyReferenced: true,
    });
    parts.push(ts.trim(), '');
  }
  fs.writeFileSync(OUT, parts.join('\n'));
  console.log(`Generated ${OUT}`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
