import { Box, Tab, Tabs } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

import NormsHistorianBasisAromatics1 from './NormsHistorianBasisAromatics1'
import NormsHistorianBasisAromatics2 from './NormsHistorianBasisAromatics2'

const REPORT_TYPE_FOR_ALL = 'NormsHistorian'

const NormsHistorianBasisAromatics = () => {
  const keycloak = useSession()
  const [dataMap, setDataMap] = useState({})
  const [gridNames, setGridNames] = useState([])
  const [loading, setLoading] = useState(false)
  const [tabIndex, setTabIndex] = useState(0)
  const [rows, setRows] = useState([])
  const [columns, setcolumns] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    yearChanged,

    plantObject,

    year,
    verticalChange,
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const vertName = verticalChange?.selectedVertical

  const lowerVertName = vertName?.toLowerCase()

  const fetchAllGrids = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    if (lowerVertName === 'aromatics') return

    try {
      setLoading(true)

      const configData = await DataService.getConfigurationExecutionDetails(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (configData?.code !== 200) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        return
      }
      const StartDate = configData.data.find(
        (d) => d.Name === 'StartDate',
      )?.AttributeValue
      const EndDate = configData.data.find(
        (d) => d.Name === 'EndDate',
      )?.AttributeValue

      if (!StartDate || !EndDate) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        return
      }

      const apiResponse = await DataService.getProductionVolDataBasisPe(
        keycloak,
        REPORT_TYPE_FOR_ALL,
        StartDate,
        EndDate,
        null,
        PLANT_ID,
        AOP_YEAR,
      )

      if (apiResponse?.code !== 200) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        return
      }
    } catch (err) {
      console.error('Error fetching all grids (new shape):', err)
    } finally {
      setLoading(false)
    }
  }, [keycloak])

  useEffect(() => {
    setTabIndex(0)
    fetchAllGrids()
  }, [fetchAllGrids, PLANT_ID, AOP_YEAR, yearChanged])

  const defaultTabs = ['Tabs1']
  const activeTabs = defaultTabs

  let type = localStorage.getItem('type')

  if (type == 1) {
    return <NormsHistorianBasisAromatics1 />
  }

  if (type == 2) {
    return <NormsHistorianBasisAromatics2 />
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {activeTabs.length > 1 && (
        <Tabs
          value={tabIndex}
          onChange={(e, newIndex) => setTabIndex(newIndex)}
          variant='scrollable'
          scrollButtons='auto'
          textColor='primary'
          indicatorColor='primary'
        >
          {activeTabs.map((label, idx) => (
            <Tab
              key={idx}
              label={label}
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                fontSize: '0.75rem',
                padding: '9px',
                minHeight: '12px',
              }}
            />
          ))}
        </Tabs>
      )}

      <Box display='flex' flexDirection='column' gap={2}>
        {tabIndex === 0 && (
          <Box sx={{ width: '100%', margin: 0 }}>
            <KendoDataGrid
              rows={rows}
              columns={columns}
              permissions={{ isHeight: rows?.length > 15 }}
            />
          </Box>
        )}
      </Box>
    </div>
  )
}

export default NormsHistorianBasisAromatics
