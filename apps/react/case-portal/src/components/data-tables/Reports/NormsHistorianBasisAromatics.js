import React, { useCallback, useEffect, useRef, useState, useMemo } from 'react'
import { Box, Tab, Tabs, Typography, Button } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import ConsumptionNormsHistorianBasis from './ConsumptionNormsHistorianBasis'
import NormsHistorianBasisAromatics1 from './NormsHistorianBasisAromatics1'
import NormsHistorianBasisAromatics2 from './NormsHistorianBasisAromatics2'

const REPORT_TYPE_FOR_ALL = 'NormsHistorian'

const NormsHistorianBasisAromatics = () => {
  const keycloak = useSession()
  const [dataMap, setDataMap] = useState({}) // values will be processed rows + columns (light processing only)
  const [gridNames, setGridNames] = useState([])
  const [loading, setLoading] = useState(false)
  const [tabIndex, setTabIndex] = useState(0)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()

  const fetchAllGrids = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    timeoutIdsRef.current.forEach((t) => clearTimeout(t))
    timeoutIdsRef.current = []

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

      const gridsArray = Array.isArray(apiResponse.data)
        ? apiResponse.data
        : Array.isArray(apiResponse.data?.data)
          ? apiResponse.data.data
          : []

      if (!Array.isArray(gridsArray) || gridsArray.length === 0) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        return
      }

      const newMap = {}
      const normalizedNames = []

      for (const g of gridsArray) {
        const name = g.gridName
        if (!name) continue
        normalizedNames.push(name)
        newMap[name] = preprocessGrid(g) // preprocessed: stable refs, but values unchanged
      }

      if (isMountedRef.current) {
        setGridNames(normalizedNames)
        setDataMap(newMap)
      }
    } catch (err) {
      console.error('Error fetching all grids (new shape):', err)
    } finally {
      if (isMountedRef.current) setLoading(false)
    }
  }, [keycloak, preprocessGrid])

  useEffect(() => {
    setTabIndex(0)
    fetchAllGrids()
  }, [fetchAllGrids, PLANT_ID, oldYear, yearChanged])

  const PETabs = ['Steady State Norm Basis', 'Overall Consumption Norm Basis']
  const defaultTabs = ['Steady State Norm Basis']
  const activeTabs = lowerVertName === 'pe' ? PETabs : defaultTabs

  const fileName = `Norms Historian Basis.xlsx`

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
        {tabIndex === 0 &&
          gridList.map(({ name, rows, columns }) => (
            <div key={name}>
              <Typography component='span' className='grid-title'>
                {renderTitle(name)}
              </Typography>

              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rows}
                  columns={columns}
                  permissions={{ isHeight: rows?.length > 15 }}
                />
              </Box>
            </div>
          ))}
      </Box>
    </div>
  )
}

export default NormsHistorianBasisAromatics
