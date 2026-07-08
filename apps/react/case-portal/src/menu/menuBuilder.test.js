import { buildMenu, findSubmenu } from './menuBuilder'

// Mirror of the real menu shape (menu/index.js): an external-links group, a
// workspace ('utilities') group holding the record-list/case-list submenus, and
// a management group.
function menuDefs() {
  return [
    { id: 'externallinks', title: '', type: 'group', children: [] },
    {
      id: 'utilities',
      title: '',
      type: 'group',
      children: [
        { id: 'workspace', title: 'Workspace', type: 'item' },
        { id: 'case-list', title: 'Cases', type: 'collapse', children: [] },
        { id: 'record-list', title: 'Records', type: 'collapse', children: [] },
      ],
    },
    { id: 'management', title: 'Management', type: 'group', children: [] },
  ]
}

describe('findSubmenu', () => {
  it('finds a nested submenu by id across groups', () => {
    const found = findSubmenu(menuDefs(), 'case-list')
    expect(found?.id).toBe('case-list')
  })

  it('returns null when the submenu is absent', () => {
    expect(findSubmenu(menuDefs(), 'does-not-exist')).toBeNull()
  })

  it('tolerates groups without children (sparse/holey arrays)', () => {
    const items = [undefined, { id: 'x' }, ...menuDefs()]
    expect(findSubmenu(items, 'record-list')?.id).toBe('record-list')
  })
})

describe('buildMenu', () => {
  it('populates case-list and record-list from the dynamic lists', () => {
    const { items } = buildMenu({
      menuItems: menuDefs(),
      recordTypes: [{ id: 'customer' }],
      caseDefinitions: [{ id: 'support', name: 'Support' }],
      isManager: true,
    })
    const caseList = findSubmenu(items, 'case-list')
    const recordList = findSubmenu(items, 'record-list')
    expect(caseList.children).toHaveLength(1)
    expect(caseList.children[0]).toMatchObject({
      id: 'support',
      title: 'Support',
      url: '/case-list/support',
    })
    expect(recordList.children[0]).toMatchObject({
      id: 'customer',
      url: '/record-list/customer',
    })
  })

  it('drops the management group for non-managers, keeping the array dense', () => {
    const { items } = buildMenu({ menuItems: menuDefs(), isManager: false })
    expect(items.some((g) => g.id === 'management')).toBe(false)
    expect(items.every(Boolean)).toBe(true) // no holes
  })

  it('keeps the management group for managers', () => {
    const { items } = buildMenu({ menuItems: menuDefs(), isManager: true })
    expect(items.some((g) => g.id === 'management')).toBe(true)
  })

  it('drops the external-links group when it has no children', () => {
    const { items } = buildMenu({ menuItems: menuDefs(), isManager: true })
    expect(items.some((g) => g.id === 'externallinks')).toBe(false)
  })

  it('does not mutate the source definitions or accumulate duplicates across builds', () => {
    const defs = menuDefs()
    buildMenu({
      menuItems: defs,
      caseDefinitions: [{ id: 'a', name: 'A' }],
      isManager: true,
    })
    const second = buildMenu({
      menuItems: defs,
      caseDefinitions: [{ id: 'a', name: 'A' }],
      isManager: true,
    })
    // Source case-list children stay empty; each build yields exactly one entry.
    expect(findSubmenu(defs, 'case-list').children).toHaveLength(0)
    expect(findSubmenu(second.items, 'case-list').children).toHaveLength(1)
  })

  it('is resilient to non-array dynamic inputs', () => {
    const { items } = buildMenu({
      menuItems: menuDefs(),
      recordTypes: null,
      caseDefinitions: undefined,
      isManager: true,
    })
    expect(findSubmenu(items, 'case-list').children).toHaveLength(0)
    expect(findSubmenu(items, 'record-list').children).toHaveLength(0)
  })
})
