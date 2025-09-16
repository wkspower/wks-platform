import { createSlice } from '@reduxjs/toolkit'

const getSelectedPlantStorage = localStorage.getItem('selectedPlant')
  ? JSON.parse(localStorage.getItem('selectedPlant'))
  : null

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

  // New objects (each with shape { id, name } or null)
  plantObject: null,
  siteObject: null,
  verticalObject: null,
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

    setPlantObject(state, action) {
      state.plantObject = action.payload
      if (action.payload && action.payload.id !== undefined) {
        state.plantID = action.payload.id
      }
    },
    setSiteObject(state, action) {
      state.siteObject = action.payload
      if (action.payload && action.payload.id !== undefined) {
        state.siteID = action.payload.id
      }
    },
    setVerticalObject(state, action) {
      state.verticalObject = action.payload
      if (action.payload && action.payload.id !== undefined) {
        state.verticalID = action.payload.id
      }
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

  setPlantObject,
  setSiteObject,
  setVerticalObject,
} = dataGridStore.actions
