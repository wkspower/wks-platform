// // import dashboard from './dashboard'
// import workspace from './workspace'
// // import management from './management'
// import plan from './plan'

// const menuItems = {
//   items: [plan, workspace],
//   // items: [dashboard, plan, workspace, management],
// }

// export default menuItems
// src/menu/menuItems.js
// Remove this line: import plan from './plan'
import { usePlanMenu } from './new-plan'
import workspace from './workspace'
// import { usePlanMenu } from './plan'

export default function useMenuItems() {
  const plan = usePlanMenu()
  // console.log(plan)
  return {
    items: [plan, workspace],
  }
}
