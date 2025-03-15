import { createSlice } from '@reduxjs/toolkit'

const initialState = {
  sitePlantChange: false,
  verticalChange: {},
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
  },
})

export default dataGridStore.reducer

export const { setSitePlantChange, setVerticalChange } = dataGridStore.actions
