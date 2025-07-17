import { createSlice } from '@reduxjs/toolkit'

const getSelectedPlantStorage = localStorage.getItem('selectedPlant')
  ? JSON.parse(localStorage.getItem('selectedPlant'))
  : null
const getStoragePlantId = () => {
  const plantId = localStorage.getItem('selectedPlant')
  return plantId ? JSON.parse(plantId).id : ''
}
const getStorageSiteId = () => {
  const siteId = localStorage.getItem('selectedSite')
  return siteId ? JSON.parse(siteId).id : ''
}

const getStorageVerticalId = () => {
  const verticalId = localStorage.getItem('selectedVertical')
  return verticalId ? JSON.parse(verticalId).id : ''
}

const getStorageYear = () => {
  const year = localStorage.getItem('year')
  return year ? year : ''
}

const initialState = {
  sitePlantChange: false,
  verticalChange: {},
  isBlocked: false,
  year: { selectedYear: getStorageYear() },
  yearChanged: false,
  currentYear: null,
  oldYear: null,
  siteID: null,
  plantID: getSelectedPlantStorage,
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
