import { createSlice } from '@reduxjs/toolkit'

const initialState = {
  sitePlantChange: false,
  verticalChange: {},
  isBlocked: false,
}

const dataGridStore = createSlice({
  name: 'menu',
  initialState,
  reducers: {
    setSitePlantChange(state, action) {
      state.sitePlantChange = action.payload
    },
    setVerticalChange(state, action) {
      state.verticalChange = action.payload
    },
    setIsBlocked(state, action) {
      state.isBlocked = action.payload
    },
    setScreenTitle(state, action) {
      state.screenTitle = action.payload
    },
  },
})

export default dataGridStore.reducer

export const {
  setSitePlantChange,
  setVerticalChange,
  setIsBlocked,
  setScreenTitle,
} = dataGridStore.actions
