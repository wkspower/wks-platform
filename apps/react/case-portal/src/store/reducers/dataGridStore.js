import { createSlice } from '@reduxjs/toolkit'

const initialState = {
  sitePlantChange: false,
  verticalChange: {},
  isBlocked: false,
  year: null,
  yearChanged: false,
  currentYear: null,
  oldYear: null,
  siteID: null,
  plantID: null,
}

const dataGridStore = createSlice({
  name: 'menu',
  initialState,
  reducers: {
    setSitePlantChange(state, action) {
      state.sitePlantChange = action.payload
    },
    setYearChange(state, action) {
      state.yearChanged = action.payload
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
    setAopYear(state, action) {
      state.year = action.payload
    },
    setCurrentYear(state, action) {
      state.currentYear = action.payload
    },
    setOldYear(state, action) {
      state.oldYear = action.payload
    },
    setSiteID(state, action) {
      state.siteID = action.payload
    },
    setPlantID(state, action) {
      state.plantID = action.payload
    },
  },
})

export default dataGridStore.reducer

export const {
  setSitePlantChange,
  setVerticalChange,
  setIsBlocked,
  setScreenTitle,
  setAopYear,
  setYearChange,
  setCurrentYear,
  setOldYear,
  setSiteID,
  setPlantID,
} = dataGridStore.actions
