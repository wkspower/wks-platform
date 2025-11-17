import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { Box, Typography } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

const REPORT_TYPE_FOR_ALL = 'NormsHistorian'

const NormsHistorianBasisAromatics2 = () => {
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  const [rawDataArr, setRawDataArr] = useState([])

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { yearChanged, oldYear, plantID, plantObject, year } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  const fetchAllGrids = useCallback(async () => {
    try {
      setLoading(true)
      const configData = await DataService.getConfigurationExecutionDetails(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (configData?.code !== 200) {
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
        setLoading(false)
        return
      }

      const apiResponse = await DataService.getProductionVolDataBasisPe(
        keycloak,
        REPORT_TYPE_FOR_ALL,
        StartDate,
        EndDate,
        'null',
        PLANT_ID,
        AOP_YEAR,
      )

      if (apiResponse?.code !== 200) {
        setLoading(false)
        return
      }

      setRawDataArr(Array.isArray(apiResponse.data) ? apiResponse.data : [])
    } catch (err) {
      console.error('Error fetching all grids:', err)
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR])

  useEffect(() => {
    fetchAllGrids()
  }, [fetchAllGrids, plantID, oldYear, yearChanged])

  const getGridTitleFromEntry = (entry = {}, fallbackIndex = 0) => {
    const rows = Array.isArray(entry.data) ? entry.data : []
    for (let i = 0; i < rows.length; i += 1) {
      const r = rows[i] || {}
      const val =
        r.GRID_TYPE ??
        r.Grid_Type ??
        r.grid_type ??
        r.gridType ??
        r.GridType ??
        r['Grid Type'] ??
        ''
      if (val !== undefined && val !== null && String(val).trim() !== '') {
        return String(val).trim()
      }
    }
    if (entry.title) return String(entry.title)
    if (entry.name) return String(entry.name)
    return `Grid ${fallbackIndex + 1}`
  }

  const panelMeta = useMemo(() => {
    return (rawDataArr || []).map((entry, idx) => ({
      title: getGridTitleFromEntry(entry, idx),
      hasData: Array.isArray(entry.data) && entry.data.length > 0,
    }))
  }, [rawDataArr])

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Box display='flex' flexDirection='column' gap={1}>
        {panelMeta.map((meta, idx) => (
          <ReportAccordionPanel
            key={`panel-${idx}`}
            index={idx}
            title={meta.title}
            loadEntry={() => rawDataArr[idx] || {}}
          />
        ))}
      </Box>
    </div>
  )
}

export default NormsHistorianBasisAromatics2

const ReportAccordionPanel = React.memo(function ReportAccordionPanel({
  index,
  title,
  loadEntry,
  READ_ONLY = false,
}) {
  const [expanded, setExpanded] = useState(false)

  const [colDefs, setColDefs] = useState([])
  const [rows, setRows] = useState([])
  const [loaded, setLoaded] = useState(false)

  const handleAccordionChange = (_evt, isExpanded) => {
    setExpanded(isExpanded)
    if (isExpanded && !loaded) {
      const entry = loadEntry() || {}
      setColDefs(entry.columns || [])
      setRows(entry.data || [])
      setLoaded(true)
    }
  }

  return (
    <CustomAccordion
      expanded={expanded}
      onChange={handleAccordionChange}
      disableGutters
    >
      <CustomAccordionSummary
        aria-controls={`panel-${index}-content`}
        id={`panel-${index}-header`}
      >
        <Typography className='accordian-title'>{title}</Typography>
      </CustomAccordionSummary>

      <CustomAccordionDetails>
        <Box sx={{ width: '100%', mt: 2 }}>
          {!loaded && <Typography sx={{ p: 1 }}>Loading...</Typography>}

          {loaded && (
            <KendoDataGrid
              setRows={setRows}
              columns={colDefs}
              rows={rows || []}
              permissions={{
                isHeight: rows?.length > 15,
                widthT: colDefs?.length > 15 ? '150px' : undefined,
              }}
            />
          )}
        </Box>
      </CustomAccordionDetails>
    </CustomAccordion>
  )
})
