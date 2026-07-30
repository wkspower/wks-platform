import i18n from '../i18n'
import { getMenuItems } from './index'

const findById = (groups, id) => {
  for (const group of groups) {
    if (group.id === id) return group
    const found = group.children ? findById(group.children, id) : undefined
    if (found) return found
  }
  return undefined
}

describe('getMenuItems', () => {
  afterEach(async () => {
    await i18n.changeLanguage('en-US')
  })

  it('returns the three menu groups', () => {
    expect(getMenuItems().items.map((group) => group.id)).toEqual([
      'externallinks',
      'utilities',
      'management',
    ])
  })

  it('re-translates titles after a language change', async () => {
    await i18n.changeLanguage('en-US')
    const english = getMenuItems().items
    expect(findById(english, 'task-list').title).toBe('Tasks')
    expect(findById(english, 'case-definition').title).toBe('Case Definitions')

    await i18n.changeLanguage('de-DE')
    const german = getMenuItems().items
    expect(findById(german, 'task-list').title).toBe('Aufgaben')
    expect(findById(german, 'case-definition').title).toBe('Falldefinitionen')
  })

  it('re-translates group captions after a language change', async () => {
    await i18n.changeLanguage('de-DE')
    const items = getMenuItems().items
    expect(findById(items, 'externallinks').caption).toBe('Externe Links')
    expect(findById(items, 'management').caption).toBe('Verwaltung')
  })

  // The regression this whole refactor exists for: the groups used to be
  // module-level singletons whose titles were translated at import time, so a
  // language switch could never reach the navigation.
  it('returns fresh objects on every call', () => {
    const first = getMenuItems()
    const second = getMenuItems()

    expect(first).not.toBe(second)
    expect(first.items[0]).not.toBe(second.items[0])

    first.items[1].title = 'mutated'
    expect(getMenuItems().items[1].title).not.toBe('mutated')
  })
})
