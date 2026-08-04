import enUS from './en_us'
import ptBR from './pt_br'
import deDE from './de_de'

const CATALOGS = [
  ['en_us', enUS],
  ['pt_br', ptBR],
  ['de_de', deDE],
]

const flatten = (node, prefix = '') =>
  Object.entries(node).flatMap(([key, value]) =>
    value && typeof value === 'object'
      ? flatten(value, `${prefix}${key}.`)
      : [`${prefix}${key}`],
  )

const resolve = (catalog, path) =>
  path.split('.').reduce((node, segment) => node?.[segment], catalog)

/**
 * Keys the components actually ask for.
 *
 * Listed explicitly because catalog parity cannot catch a misplaced block: if the
 * same key lands under the wrong parent in all three catalogs they stay perfectly
 * in step with each other while every lookup still misses. That is exactly what
 * happened once — the whole cmmnImport block was nested under `pages.caseform`, all
 * three files agreed, and the UI rendered the raw key.
 */
const REQUIRED_PATHS = [
  'menu.workspace',
  'menu.case',
  'menu.task',
  'menu.record',
  'menu.management',
  'menu.casebuilder',
  'menu.language',
  'menu.logout',
  'menu.profile',
  'general.case.status.wip',
  'pages.caseform.tabs.details',
  'pages.caseform.tabs.diagram',
  'pages.caseform.diagram.none',
  'pages.caseform.diagram.currentStage',
  'pages.caseform.diagram.achieved',
  'pages.cmmnImport.action',
  'pages.cmmnImport.chooseDiagram',
  'pages.cmmnImport.diagramHint',
  'pages.cmmnImport.diagramRejected',
  'pages.cmmnImport.title',
  'pages.cmmnImport.chooseFile',
  'pages.cmmnImport.hint',
  'pages.cmmnImport.import',
  'pages.cmmnImport.cancel',
  'pages.cmmnImport.done',
  'pages.cmmnImport.optional',
  'pages.cmmnImport.imported',
  'pages.cmmnImport.summary.stages',
  'pages.cmmnImport.summary.tasks',
  'pages.cmmnImport.summary.milestones',
  'pages.cmmnImport.warnings.title',
  'pages.cmmnImport.warnings.intro',
]

describe('translation catalogs', () => {
  it.each(CATALOGS)(
    '%s resolves every key the UI asks for',
    (name, catalog) => {
      const missing = REQUIRED_PATHS.filter(
        (path) => typeof resolve(catalog, path) !== 'string',
      )
      expect(missing).toEqual([])
    },
  )

  it('all catalogs carry exactly the same keys', () => {
    const [reference, ...others] = CATALOGS.map(([name, catalog]) => [
      name,
      flatten(catalog).sort(),
    ])

    others.forEach(([name, keys]) => {
      expect({ [name]: keys }).toEqual({ [name]: reference[1] })
    })
  })

  it('leaves no value blank', () => {
    CATALOGS.forEach(([name, catalog]) => {
      const blank = flatten(catalog).filter(
        (path) => !String(resolve(catalog, path)).trim(),
      )
      expect({ [name]: blank }).toEqual({ [name]: [] })
    })
  })
})
