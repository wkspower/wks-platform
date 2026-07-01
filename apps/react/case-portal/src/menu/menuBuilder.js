/**
 * Pure assembly of the navigation menu from its static definition plus the
 * dynamic record-type and case-definition lists. Kept free of React/services so
 * it can be unit-tested and so the array-shape hardening lives in one place.
 *
 * The static groups (menu/index.js) are module singletons, so this clones them
 * before mutating children — otherwise repeated builds accumulate duplicates.
 */

// Find a child submenu (e.g. 'record-list', 'case-list') within any group,
// without assuming a fixed index. Returns null when absent.
export function findSubmenu(items, submenuId) {
  for (const group of items) {
    const match = group?.children?.find((child) => child.id === submenuId)
    if (match) return match
  }
  return null
}

// Clone groups AND their direct children (the submenu objects), so replacing a
// submenu's `children` array never mutates the shared menu-definition singletons.
// Grandchildren aren't touched (their arrays are replaced wholesale), and icon
// component references are intentionally copied by reference, not deep-cloned.
function cloneItems(items) {
  return items.map((group) =>
    group
      ? {
          ...group,
          children: group.children
            ? group.children.map((child) => (child ? { ...child } : child))
            : undefined,
        }
      : group,
  )
}

/**
 * @param {object} params
 * @param {Array}  params.menuItems      the static menu definition items
 * @param {Array}  params.recordTypes    record types to list under 'record-list'
 * @param {Array}  params.caseDefinitions case definitions to list under 'case-list'
 * @param {boolean} params.isManager     whether the management group is shown
 * @returns {{ items: Array }}
 */
export function buildMenu({
  menuItems = [],
  recordTypes = [],
  caseDefinitions = [],
  isManager = false,
} = {}) {
  const items = cloneItems(menuItems)

  const recordListMenu = findSubmenu(items, 'record-list')
  if (recordListMenu) {
    recordListMenu.children = (
      Array.isArray(recordTypes) ? recordTypes : []
    ).map((element) => ({
      id: element.id,
      title: element.id,
      type: 'item',
      url: '/record-list/' + element.id,
      breadcrumbs: true,
    }))
  }

  const caseListMenu = findSubmenu(items, 'case-list')
  if (caseListMenu) {
    caseListMenu.children = (
      Array.isArray(caseDefinitions) ? caseDefinitions : []
    ).map((element) => ({
      id: element.id,
      title: element.name,
      type: 'item',
      url: '/case-list/' + element.id,
      breadcrumbs: true,
    }))
  }

  // Keep the array dense (filter, not delete-by-index, which leaves holes that
  // break downstream .map/.filter over menu.items).
  let result = items
  if (!isManager) {
    result = result.filter((group) => group?.id !== 'management')
  }
  result = result.filter(
    (group) => group?.id !== 'externallinks' || group?.children?.length,
  )

  return { items: result }
}
