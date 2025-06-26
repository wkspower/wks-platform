import { Box, Tab, Tabs, Backdrop, CircularProgress } from '@mui/material'
import { useCallback, useEffect, useState, useMemo } from 'react'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import KendoDataTables from './index'
import { DataService } from 'services/DataService'
import { validateFields } from 'utils/validationUtils'
import { useSession } from 'SessionStoreContext'

const CrackerConfig = () => {
  // Redux/context
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, oldYear } = dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const plantId = JSON.parse(localStorage.getItem('selectedPlant') || '{}')?.id

  // Snackbar/loading/remark
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Header map for columns
  const headerMap = useMemo(
    () => generateHeaderNames(localStorage.getItem('year')),
    [],
  )

  // ===== Dynamic-tabs state (static fallback) =====
  const rawTabsStatic = [
    'Miscellaneous Parameters',
    'Total Feed',
    'Total Products',
  ]
  const [tabs, setTabs] = useState(rawTabsStatic)
  const [availableTabs, setAvailableTabs] = useState([])
  const [tabIndex, setTabIndex] = useState(0)

  // ===== Separate row states per known tab =====
  // Initialize an individual state for each rawTabsStatic entry:
  const [feedRows, setFeedRows] = useState([])
  const [compositionRows, setCompositionRows] = useState([])
  const [hydrogenationRows, setHydrogenationRows] = useState([])
  // const [recoveryRows, setRecoveryRows] = useState([])
  // const [optimizing, setOptimizing] = useState([])
  // const [furnace, setFurnance] = useState([])

  // Mode selection
  const allModes = ['5F Operation', '4F Operation', '4F+D Operation']
  const [selectMode, setSelectMode] = useState(allModes[0])

  // Permissions helper
  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      showModes: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      allAction: false,
    }
  }
  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      showModes: lowerVertName === 'cracker',
      saveWithRemark: true,
      saveBtn: true,
      allAction: lowerVertName === 'cracker',
      modes: allModes,
    },
    isOldYear,
  )

  // NormParameterIdCell
  const NormParameterIdCell = (props) => <td>{props?.dataItem?.particulars}</td>

  // currentTab displayName
  const currentTabDisplay = useMemo(() => {
    const idLower = tabs[tabIndex]?.toLowerCase() || ''
    const info = availableTabs.find((t) => t.id.toLowerCase() === idLower)
    // console.log(info)
    return info ? info.displayName : tabs[tabIndex] || 'Feed'
  }, [tabs, tabIndex, availableTabs])

  // Columns: recalc when headerMap or currentTab changes
  const productionColumns = useMemo(() => {
    const configType =
      currentTabDisplay === 'Composition' ? 'cracker_composition' : 'cracker'
    return getEnhancedAOPColDefs({
      headerMap,
      handleRemarkCellClick,
      configType,
    })
  }, [headerMap, currentTabDisplay])

  // ===== Fetch dynamic-tabs from API =====
  const fetchTabsMatrix = useCallback(async () => {
    try {
      const resp = await DataService.getConfigurationTabsMatrix(
        keycloak,
        'output',
      )
      let tabsFromApi = []
      if (typeof resp.data === 'string') {
        try {
          tabsFromApi = JSON.parse(resp.data)
        } catch (e) {
          console.error('Failed parsing tabs JSON', e)
        }
      } else if (Array.isArray(resp.data)) {
        tabsFromApi = resp.data
      }
      if (Array.isArray(tabsFromApi) && tabsFromApi.length) {
        setTabs(tabsFromApi)
      } else {
        setTabs(rawTabsStatic)
      }
    } catch (err) {
      console.error('Error fetching cracker tabs matrix:', err)
      setTabs(rawTabsStatic)
    }
  }, [keycloak])

  const fetchAvailableTabs = useCallback(async () => {
    try {
      const resp = await DataService.getConfigurationAvailableTabs(keycloak)
      if (
        resp?.code === 200 &&
        Array.isArray(resp.data?.configurationTypeList)
      ) {
        setAvailableTabs(resp.data.configurationTypeList)
      } else {
        setAvailableTabs(
          rawTabsStatic.map((t) => ({
            id: t,
            displayName: t.charAt(0).toUpperCase() + t.slice(1),
          })),
        )
      }
    } catch (err) {
      console.error('Error fetching available tabs:', err)
      setAvailableTabs(
        rawTabsStatic.map((t) => ({
          id: t,
          displayName: t.charAt(0).toUpperCase() + t.slice(1),
        })),
      )
    }
  }, [keycloak])

  useEffect(() => {
    fetchTabsMatrix()
    fetchAvailableTabs()
    setTabIndex(0)
  }, [keycloak, fetchTabsMatrix, fetchAvailableTabs])

  // ===== Helper: getRows / setRowsForTab =====
  const getRows = useCallback(
    (tabId) => {
      switch (tabId) {
        case 'Miscellaneous Parameters':
          return feedRows
        case 'Total Feed':
          return compositionRows
        case 'Total Products':
          return hydrogenationRows

        default:
          return []
      }
    },
    [
      feedRows,
      compositionRows,
      hydrogenationRows,
      // recoveryRows,
      // furnace,
      // optimizing,
    ],
  )

  const setRowsForTab = useCallback((tabId, data) => {
    switch (tabId) {
      case 'Miscellaneous Parameters':
        setFeedRows(data)
        break
      case 'Total Feed':
        setCompositionRows(data)
        break
      case 'Total Products':
        setHydrogenationRows(data)
        break

      default:
        // no-op or log if unexpected
        console.warn('No state for tab:', tabId)
    }
  }, [])

  // ===== Fetch rows for a given tab =====
  const fetchCrackerRows = useCallback(
    async (currentTabDisplay, mode) => {
      if (!currentTabDisplay) return
      try {
        setLoading(true)
        // Use tabId directly for API
        const spyroVM = await DataService.getSpyroOutputData(
          keycloak,
          mode,
          currentTabDisplay,
        )
        let transformedData = []
        if (spyroVM?.data && Array.isArray(spyroVM.data)) {
          transformedData = spyroVM.data.map((item, index) => ({
            id: item.NormParameterFKID || `row_${index}`,
            particulars: item.Particulars,
            uom: item.UOM,
            remarks: item.Remarks,
            originalRemark: item.Remarks,
            ParticularsType: item.Type,
            jan: item.Jan,
            feb: item.Feb,
            march: item.Mar,
            april: item.Apr,
            may: item.May,
            june: item.Jun,
            july: item.Jul,
            aug: item.Aug,
            sep: item.Sep,
            oct: item.Oct,
            nov: item.Nov,
            dec: item.Dec,
            NormParameterFKID: item.NormParameterFKID,
            ...item,
          }))
        }
        setRowsForTab(currentTabDisplay, transformedData)
      } catch (err) {
        // console.warn(`Failed to load ${tabId} data:`, err)
        setSnackbarData({
          message: `Failed to load ${currentTabDisplay} data. Please try again.`,
          severity: 'error',
        })
        setSnackbarOpen(true)
        setRowsForTab(currentTabDisplay, [])
      } finally {
        setLoading(false)
      }
    },
    [keycloak, setRowsForTab, currentTabDisplay],
  )

  // When tabIndex, selectMode, plantId change, load that tab
  useEffect(() => {
    // const tabId = tabs[tabIndex]
    if (keycloak && plantId && currentTabDisplay) {
      fetchCrackerRows(currentTabDisplay, selectMode)
    } else {
      console.warn('Missing data for fetchCrackerRows:', {
        hasKeycloak: !!keycloak,
        hasPlantId: !!plantId,
        currentTabDisplay,
      })
    }
  }, [
    tabIndex,
    selectMode,
    plantId,
    tabs,
    fetchCrackerRows,
    keycloak,
    currentTabDisplay,
  ])

  // ===== Save logic unchanged except reload uses setRowsForTab =====
  const [modifiedCells, setModifiedCells] = useState({})
  const saveChanges = useCallback(async () => {
    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setLoading(false)
        return
      }
      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)
      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setLoading(false)
        return
      }
      console.log(data)
      const validationMessage = validateFields(data, ['particulars', 'remarks'])
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({ message: validationMessage, severity: 'error' })
        setLoading(false)
        return
      }
      await saveSpyroData(data)
    } catch (error) {
      console.error('Error saving changes:', error)
    }
  }, [modifiedCells])

  const saveSpyroData = async (newRows) => {
    setLoading(true)

    try {
      let plant = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) plant = JSON.parse(storedPlant).id
      let verticalId = localStorage.getItem('verticalId')
      const SpyroInputData = newRows.map((row) => ({
        VerticalFKId: verticalId,
        PlantFKId: plant,
        NormParameterFKID: row.NormParameterFKID ?? null,
        Particulars: row.particulars ?? row.Particulars ?? null,
        NormParameterTypeName: row.NormParameterTypeName ?? null,
        NormParameterTypeFKID: row.NormParameterTypeFKID ?? null,
        Type: row.ParticularsType ?? row.Type ?? null,
        UOM: row.uom ?? row.UOM ?? null,
        AuditYear: row.AuditYear ?? null,
        Remarks: row.remarks ?? row.Remarks ?? null,
        Jan: row.jan ?? row.Jan ?? null,
        Feb: row.feb ?? row.Feb ?? null,
        Mar: row.march ?? row.Mar ?? null,
        Apr: row.april ?? row.Apr ?? null,
        May: row.may ?? row.May ?? null,
        Jun: row.june ?? row.Jun ?? null,
        Jul: row.july ?? row.Jul ?? null,
        Aug: row.aug ?? row.Aug ?? null,
        Sep: row.sep ?? row.Sep ?? null,
        Oct: row.oct ?? row.Oct ?? null,
        Nov: row.nov ?? row.Nov ?? null,
        Dec: row.dec ?? row.Dec ?? null,
        id: row.idFromApi ?? row.id ?? null,
        inEdit: row.inEdit || false,
      }))
      const response = await DataService.saveSpyroOutput(
        SpyroInputData,
        keycloak,
      )
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Spyro Input data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        // Reload current tab
        const tabId = tabs[tabIndex]
        if (tabId) fetchCrackerRows(currentTabDisplay, selectMode)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error saving Spyro Input data!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Error saving Spyro Input data!', error)
    } finally {
      setLoading(false)
    }
  }

  // ===== Render =====
  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Tabs
        sx={{
          borderBottom: '0px solid #ccc',
          '.MuiTabs-indicator': { display: 'none' },
        }}
        textColor='primary'
        indicatorColor='primary'
        value={tabIndex}
        onChange={(e, newIndex) => {
          if (newIndex >= 0 && newIndex < tabs.length) {
            setTabIndex(newIndex)
          }
        }}
      >
        {tabs.map((tabId) => {
          const info = availableTabs.find(
            (t) => t.id.toLowerCase() === tabId.toLowerCase(),
          )
          const label = info?.displayName || tabId
          return (
            <Tab
              key={tabId}
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                textTransform: 'capitalize',
              }}
              label={label}
            />
          )
        })}
      </Tabs>

      <Box>
        {(() => {
          // const tabId = tabs[tabIndex]
          // console.log(tabId)
          const rows = getRows(currentTabDisplay)
          const setRowsForCurrent = useCallback(
            (newRows) => setRowsForTab(currentTabDisplay, newRows),
            [currentTabDisplay],
          )
          switch (currentTabDisplay) {
            case 'Miscellaneous Parameters':
            case 'Total Feed':
            case 'Total Products':
              return (
                <Box key={currentTabDisplay}>
                  <KendoDataTables
                    rows={rows}
                    setRows={setRowsForCurrent}
                    fetchData={() =>
                      fetchCrackerRows(currentTabDisplay, selectMode)
                    }
                    configType='cracker'
                    handleRemarkCellClick={handleRemarkCellClick}
                    NormParameterIdCell={NormParameterIdCell}
                    columns={productionColumns}
                    remarkDialogOpen={remarkDialogOpen}
                    setRemarkDialogOpen={setRemarkDialogOpen}
                    currentRemark={currentRemark}
                    setCurrentRemark={setCurrentRemark}
                    currentRowId={currentRowId}
                    permissions={adjustedPermissions}
                    selectMode={selectMode}
                    setSelectMode={setSelectMode}
                    saveChanges={saveChanges}
                    snackbarData={snackbarData}
                    snackbarOpen={snackbarOpen}
                    setSnackbarOpen={setSnackbarOpen}
                    setSnackbarData={setSnackbarData}
                    modifiedCells={modifiedCells}
                    setModifiedCells={setModifiedCells}
                  />
                </Box>
              )

            default:
              return null
          }
        })()}
      </Box>
    </Box>
  )
}

export default CrackerConfig
