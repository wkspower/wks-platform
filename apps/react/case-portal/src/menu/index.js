import createWorkspace from './workspace'
import createManagement from './management'
import createExternalLinks from './externalLinks'

/**
 * Assemble the static menu definitions in the active language.
 *
 * Call this on every rebuild rather than holding the result: the group builders
 * translate their titles, so a cached tree would keep the language it was first
 * built in. Each call returns fresh objects, which also keeps buildMenu() free to
 * treat them as its own.
 */
export const getMenuItems = () => ({
  items: [createExternalLinks(), createWorkspace(), createManagement()],
})

export default getMenuItems
