// CrackerConfig.jsx
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
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, oldYear } = dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const plantId = JSON.parse(localStorage.getItem('selectedPlant') || '{}')?.id
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
    // if (!row?.isEditable) return

    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  // const [allProducts, setAllProducts] = useState([])
  const headerMap = useMemo(
    () => generateHeaderNames(localStorage.getItem('year')),
    [],
  )

  const rawTabsStatic = [
    'Feed',
    'Composition',
    'Hydrogenation',
    'Recovery',
    'Furnace',
  ]
  const [tabs, setTabs] = useState(rawTabsStatic)
  const [availableTabs, setAvailableTabs] = useState([])
  const [tabIndex, setTabIndex] = useState(0)

  // Row states per tab

    // —— C2/C3 (existing) ——

    // —— Hexene Purge Gas ——

    // —— Import Propane ——

    // —— BPCL Kochi Propylene ——

    // —— FCC C3 ——

    // —— LDPE Off Gas ——

    // —— Additional Feed (Default Composition) ——





  const [feedRows, setFeedRows] = useState([])
  const [compositionRows, setCompositionRows] = useState([])
  const [hydrogenationRows, setHydrogenationRows] = useState([])
  const [recoveryRows, setRecoveryRows] = useState([])
  const [optimizing, setOptimizing] = useState([])
  const [furnace, setFurnance] = useState([])
  const allModes = ['5F', '4F', '4F+D']
  const [selectMode, setSelectMode] = useState(allModes[0])
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
  const NormParameterIdCell = (props) => <td>{props?.dataItem?.particulars}</td>
  const currentTabDisplay = useMemo(() => {
    const idLower = tabs[tabIndex]?.toLowerCase() || ''
    const info = availableTabs.find((t) => t.id.toLowerCase() === idLower)
    console.log(info)
    return info ? info.displayName : tabs[tabIndex] || 'Feed'
  }, [tabs, tabIndex, availableTabs])
  const productionColumns = useMemo(() => {
    const configType =
      currentTabDisplay === 'Composition' ? 'cracker_composition' : 'cracker'
    return getEnhancedAOPColDefs({
      headerMap,
      handleRemarkCellClick,
      configType,
    })
  }, [headerMap, currentTabDisplay])
  const fetchTabsMatrix = useCallback(async () => {
    try {
      const resp = await DataService.getConfigurationTabsMatrix(keycloak)
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
  const getRows = useCallback(
    (tabId) => {
      switch (tabId) {
        case 'Feed':
          return feedRows
        case 'Composition':
          return compositionRows
        case 'Hydrogenation':
          return hydrogenationRows
        case 'Recovery':
          return recoveryRows
        case 'Optimizing':
          return optimizing
        case 'Furnace':
          return furnace
        default:
          return []
      }
    },
    [
      feedRows,
      compositionRows,
      hydrogenationRows,
      recoveryRows,
      furnace,
      optimizing,
    ],
  )
  const setRowsForTab = useCallback((tabId, data) => {
    switch (tabId) {
      case 'Feed':
        setFeedRows(data)
        break
      case 'Composition':
        setCompositionRows(data)
        break
      case 'Hydrogenation':
        setHydrogenationRows(data)
        break
      case 'Recovery':
        setRecoveryRows(data)
        break
      case 'Furnace':
        setFurnance(data)
        break
      case 'Optimizing':
        setOptimizing(data)
        break
      default:
        console.warn('No state for tab:', tabId)
    }
  }, [])
  const fetchCrackerRows = useCallback(
    // Simulate network delay
    async (currentTabDisplay, mode) => {
      if (!currentTabDisplay) return
      try {
        setLoading(true)
        const spyroVM = await DataService.getSpyroInputData(
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

  // 5️⃣ Whenever the selected tab changes, reload that tab’s rows
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
    // console.log(props)
    // const productId = props.dataItem.normParameterFKId
    // const product = allProducts.find((p) => p.id === productId)
    // const displayName = product?.displayName || ''
    // console.log(displayName)

  // ===== Save logic unchanged except reload uses setRowsForTab =====
  const [modifiedCells, setModifiedCells] = useState({})
    // allProducts,
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
      const response = await DataService.saveSpyroInput(
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
    }
  }
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
          // margin: '-35px 0px -8px 0%',
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
          const rows = getRows(currentTabDisplay)
          const setRowsForCurrent = useCallback(
            (newRows) => setRowsForTab(currentTabDisplay, newRows),
            [currentTabDisplay],
          )
          switch (currentTabDisplay) {
            case 'Feed':
            case 'Hydrogenation':
            case 'Recovery':
            case 'Optimizing':
            case 'Furnace':
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

            case 'Composition':
              return (
                <Box key={currentTabDisplay}>
                  <KendoDataTables
                    rows={rows}
                    setRows={setRowsForCurrent}
                    fetchData={() =>
                      fetchCrackerRows(currentTabDisplay, selectMode)
                    }
                    configType='cracker_composition'
                    groupBy='ParticularsType'
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
