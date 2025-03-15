// types
import { createSlice } from '@reduxjs/toolkit'

// initial state
const initialState = {
  openItem: ['dashboard'],
  openComponent: 'buttons',
  drawerOpen: true,
  componentDrawerOpen: true,
  // sitePlantChange: false,
  // verticalChange: {},
}

// ==============================|| SLICE - MENU ||============================== //

const menu = createSlice({
  name: 'menu',
  initialState,
  reducers: {
    activeItem(state, action) {
      state.openItem = action.payload.openItem
    },

    activeComponent(state, action) {
      state.openComponent = action.payload.openComponent
    },

    openDrawer(state, action) {
      state.drawerOpen = action.payload.drawerOpen
    },

    openComponentDrawer(state, action) {
      state.componentDrawerOpen = action.payload.componentDrawerOpen
    },
    // setSitePlantChange(state, action) {
    //   state.sitePlantChange = action.payload
    // },
    // setVerticalChange(state, action) {
    //   state.verticalChange = action.payload
    // },
  },
})

export default menu.reducer

export const {
  activeItem,
  activeComponent,
  openDrawer,
  openComponentDrawer,
  // setSitePlantChange,
  // setVerticalChange,
} = menu.actions
