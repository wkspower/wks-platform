const MENU_UPDATE_EVENT = 'menu-update-event'

export const MenuEventService = {
  triggerMenuUpdate: () => {
    const event = new CustomEvent(MENU_UPDATE_EVENT)
    window.dispatchEvent(event)
  },

  subscribeToMenuUpdates: (callback) => {
    window.addEventListener(MENU_UPDATE_EVENT, callback)
    return () => window.removeEventListener(MENU_UPDATE_EVENT, callback)
  },
}
