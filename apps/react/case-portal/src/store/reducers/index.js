// third-party
import { combineReducers } from 'redux'

// project import
import menu from './menu'
import dataGridStore from './dataGridStore'

// ==============================|| COMBINE REDUCERS ||============================== //

const reducers = combineReducers({ menu, dataGridStore })

export default reducers
