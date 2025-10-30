import { Backdrop, Box, CircularProgress, Tab, Tabs } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { validateFields } from 'utils/validationUtils'
import KendoDataTables from './index'
import { OptimizerDataApiService } from 'services/optimizer-api-service'

const CrackerConfig = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    oldYear,
    plantID,
    yearChanged,
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
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const headerMap = useMemo(
    () => generateHeaderNames(localStorage.getItem('year')),
    [],
  )

  const rawTabsStatic = [
    'Feed',
    'Optimizing',
    'Composition',
    'Hydrogenation',
    'Recovery',
    'Furnace',
    'Constant',
  ]
  const [tabs, setTabs] = useState(rawTabsStatic)
  const [availableTabs, setAvailableTabs] = useState([])
  const [tabIndex, setTabIndex] = useState(0)

  const [feedRows, setFeedRows] = useState([])
  const [compositionRows, setCompositionRows] = useState([])
  const [hydrogenationRows, setHydrogenationRows] = useState([])
  const [recoveryRows, setRecoveryRows] = useState([])
  const [optimizing, setOptimizing] = useState([])
  const [furnace, setFurnance] = useState([])
  const allModes = ['5F', '4F', '4F+D']
  const [selectMode, setSelectMode] = useState(allModes[0])
  const [constantsRows, setConstantsRows] = useState([])

  const currentTabDisplay = useMemo(() => {
    const idLower = tabs[tabIndex]?.toLowerCase() || ''
    const info = availableTabs.find((t) => t.id.toLowerCase() === idLower)
    return info ? info.name : tabs[tabIndex] || 'Feed'
  }, [tabs, tabIndex, availableTabs])

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
      uploadExcelBtn: false,
      downloadExcelBtn: false,
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
      uploadExcelBtn: currentTabDisplay == 'Constant' ? false : true,
      downloadExcelBtn: currentTabDisplay == 'Constant' ? false : true,
    },
    isOldYear,
  )

  const productionColumns = useMemo(() => {
    const configType =
      currentTabDisplay === 'Composition'
        ? 'cracker_composition'
        : currentTabDisplay === 'Constant'
          ? 'cracker_constants'
          : currentTabDisplay === 'Yield'
            ? 'cracker_yield'
            : 'cracker'

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

  const fetchModes = useCallback(async () => {
    try {
      const resp = await OptimizerDataApiService.fetchModes(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        '1',
      )
      if (resp?.code === 200 && Array.isArray(resp.data)) {
        setModes(resp.data)
      } else {
        setModes([])
      }
    } catch (err) {
      console.error('Error fetching data:', err)
    }
  }, [keycloak])

  useEffect(() => {
    fetchModes()
    fetchTabsMatrix()
    fetchAvailableTabs()
    setTabIndex(0)
  }, [keycloak, fetchTabsMatrix, fetchAvailableTabs, fetchModes])

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
        case 'Constant':
          return constantsRows
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
      constantsRows,
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

      case 'Constant':
        setConstantsRows(data)
        break
      default:
        console.warn('No state for tab:', tabId)
    }
  }, [])

  const fetchCrackerRows = useCallback(
    async (currentTabDisplay, mode) => {
      if (!currentTabDisplay) return
      try {
        setLoading(true)
        let transformedData = []
        let transformedData1 = []
        var spyroVM1 = []
        if (currentTabDisplay == 'Constant') {
          spyroVM1 = await DataService.getSpyroInputData(
            keycloak,
            mode,
            currentTabDisplay,
          )

          if (spyroVM1?.data && Array.isArray(spyroVM1.data)) {
            transformedData1 = spyroVM1.data.map((item, index) => ({
              id: item.NormParameterFKID || `row_${index}`,
              particulars: item.Particulars,
              uom: item.UOM,
              remarks: item.remarks ?? item.Remarks ?? '',
              originalRemark: item.remarks ?? item.Remarks ?? '',
              ParticularsType: item.NormParameterTypeName,

              april:
                item.Apr && item.Apr.trim() !== '' ? Number(item.Apr) : null,
              NormParameterFKID: item.NormParameterFKID,
              ...item,
            }))
          }
          setRowsForTab(currentTabDisplay, transformedData1)
          return
        }

        const spyroVM = await DataService.getSpyroInputData(
          keycloak,
          mode,
          currentTabDisplay,
        )
        setTimeout(() => {
          if (spyroVM?.data && Array.isArray(spyroVM.data)) {
            transformedData = spyroVM.data.map((item, index) => ({
              id: item.NormParameterFKID || `row_${index}`,
              particulars: item.Particulars,
              uom: item.UOM,
              remarks: item.remarks ?? item.Remarks ?? '',
              originalRemark: item.remarks ?? item.Remarks ?? '',
              ParticularsType: item.normParameterTypeName,

              NormParameterFKID: item.NormParameterFKID,

              ...item,
            }))
          }
          setRowsForTab(currentTabDisplay, transformedData)
        }, 500)
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

  useEffect(() => {
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
    plantID,
    tabs,
    fetchCrackerRows,
    keycloak,
    currentTabDisplay,
    yearChanged,
  ])

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
    console.log('newRows', newRows)

    setLoading(true)
    try {
      const SpyroInputData = newRows.map((row) => ({
        normParameterFKID: row.normParameterFKID ?? null,
        Remarks: row.remarks ?? row.Remarks ?? null,
        remarks: row.remarks ?? row.Remarks ?? null,
        jan: row.jan || null,
        feb: row.feb || null,
        mar: row.mar || null,
        apr: row.apr || null,
        may: row.may || null,
        jun: row.jun || null,
        jul: row.jul || null,
        aug: row.aug || null,
        sep: row.sep || null,
        oct: row.oct || null,
        nov: row.nov || null,
        dec: row.dec || null,
        id: null,
      }))
      const response = await DataService.saveSpyroInput(
        SpyroInputData,
        keycloak,
        plantId,
      )
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})

        const tabId = tabs[tabIndex]
        if (tabId) fetchCrackerRows(currentTabDisplay, selectMode)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error saving Optimizer data!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Error saving Optimizer Input data!', error)
    } finally {
      setLoading(false)
    }
  }

  const saveSpyroInputExcelFile = async (rawFile) => {
    setLoading(true)
    try {
      const mode = selectMode || ''
      let response

      response = await DataService.importSpyroInputExcel(
        rawFile,
        keycloak,
        mode,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })

        fetchCrackerRows(currentTabDisplay, selectMode)
      } else if (response?.code === 400 && response?.data) {
        const byteCharacters = atob(response.data)
        const byteNumbers = Array.from(byteCharacters, (char) =>
          char.charCodeAt(0),
        )
        const byteArray = new Uint8Array(byteNumbers)

        const blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        })

        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'Error File - Optimizer Input.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
      } else {
        // setSnackbarOpen(true)
      }

      return response
    } catch (error) {
      console.error('Error uploading Optimizer Input Excel:', error)
    } finally {
      setLoading(false)
      fetchCrackerRows(currentTabDisplay, selectMode)
    }
  }
  const handleExcelUpload = (rawFile) => {
    saveSpyroInputExcelFile(rawFile)
  }

  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    const mode = selectMode

    try {
      const response = await DataService.exportSpyroInputExcel(keycloak, mode)

      if (response?.code === 200) {
        setSnackbarOpen(true)

        setSnackbarData({
          message: 'Excel download completed successfully!',
          severity: 'success',
        })
      } else {
        setSnackbarOpen(true)

        setSnackbarData({
          message: 'Failed to download Excel.',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error downloading Excel:', error)
      setSnackbarOpen(true)

      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    } finally {
      setSnackbarOpen(false)
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
      <Box sx={{ overflowX: 'auto', width: '100%' }}>
        <Tabs
          sx={{
            borderBottom: '0px solid #ccc',
            '.MuiTabs-indicator': { display: 'none' },
            margin: '0px 0px 0px 0px',
            minHeight: '28px',
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
                  fontSize: '0.75rem',
                  padding: '9px',
                  minHeight: '12px',
                }}
                label={label}
              />
            )
          })}
        </Tabs>
      </Box>
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
            case 'Constant':
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
                    handleExcelUpload={handleExcelUpload}
                    downloadExcelForConfiguration={
                      downloadExcelForConfiguration
                    }
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
                    handleExcelUpload={handleExcelUpload}
                    downloadExcelForConfiguration={
                      downloadExcelForConfiguration
                    }
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
