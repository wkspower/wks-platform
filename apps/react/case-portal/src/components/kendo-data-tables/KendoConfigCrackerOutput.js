import { Box, Tab, Tabs, Backdrop, CircularProgress } from '@mui/material'
import { useCallback, useEffect, useState, useMemo } from 'react'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import KendoDataTables from './index'
import { DataService } from 'services/DataService'
import { validateFields } from 'utils/validationUtils'
import { useSession } from 'SessionStoreContext'
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
  const [modifiedCells, setModifiedCells] = useState({})

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

  const headerMap = useMemo(() => generateHeaderNames(AOP_YEAR), [])

  const rawTabsStatic = [
    'Total Feed',
    'Total Products',
    'Miscellaneous Parameters',
    'Constant',
    'Yield',
  ]
  const [tabs, setTabs] = useState(rawTabsStatic)
  const [availableTabs, setAvailableTabs] = useState([])
  const [tabIndex, setTabIndex] = useState(0)
  const [modes, setModes] = useState([])

  const [yieldRows, setYieldRows] = useState([])
  const [constantsRows, setConstantsRows] = useState([])
  const [feedRows, setFeedRows] = useState([])
  const [compositionRows, setCompositionRows] = useState([])
  const [hydrogenationRows, setHydrogenationRows] = useState([])

  // const allModes = ['5F', '4F', '4F+D']
  const [selectMode, setSelectMode] = useState('')

  const currentTabDisplay = useMemo(() => {
    const idLower = tabs[tabIndex]?.toLowerCase() || ''
    const info = availableTabs.find((t) => t.id.toLowerCase() === idLower)
    return info ? info.name : tabs[tabIndex] || 'Feed'
  }, [tabs, tabIndex, availableTabs])

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
      showModes: lowerVertName === 'cracker' && currentTabDisplay != 'Yield',
      saveWithRemark: true,
      saveBtn: true,
      allAction: lowerVertName === 'cracker',
      modes: modes,
      uploadExcelBtn: true,
      downloadExcelBtn: true,
    },
    isOldYear,
  )

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

  const fetchModes = useCallback(async () => {
    try {
      const resp = await OptimizerDataApiService.fetchModes(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        '1',
      )

      if (resp?.code === 200 && Array.isArray(resp.data)) {
        setModes(resp.data) // keep full objects
        setSelectMode(resp.data[0]?.name ?? '') // default select first mode by name
      } else {
        setModes([])
        setSelectMode('')
      }
    } catch (err) {
      console.error('Error fetching data:', err)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR])

  useEffect(() => {
    fetchModes()

    fetchTabsMatrix()
    fetchAvailableTabs()
    setTabIndex(0)
  }, [keycloak, fetchTabsMatrix, fetchAvailableTabs, fetchModes])

  const getRows = useCallback(
    (tabId) => {
      switch (tabId) {
        case 'Total Feed':
          return compositionRows
        case 'Total Products':
          return hydrogenationRows
        case 'Miscellaneous Parameters':
          return feedRows
        case 'Constant':
          return constantsRows
        case 'Yield':
          return yieldRows

        default:
          return []
      }
    },
    [feedRows, compositionRows, hydrogenationRows, constantsRows, yieldRows],
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
      case 'Constant':
        setConstantsRows(data)
        break
      case 'Yield':
        setYieldRows(data)
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

        const spyroVM = await DataService.getSpyroOutputData(
          keycloak,
          mode,
          currentTabDisplay,
        )
        let transformedData = []
        if (spyroVM?.data && Array.isArray(spyroVM.data)) {
          transformedData = spyroVM.data.map((item, index) => ({
            id: item.NormParameterFKID || `row_${index}`,
            remarks: item.remarks ?? item.Remarks ?? '',
            originalRemark: item.remarks ?? item.Remarks ?? '',
            ParticularsType: item.Type,

            ...item,
          }))
        }
        setRowsForTab(currentTabDisplay, transformedData)
      } catch (err) {
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

  const fetchCrackerRowsYield = useCallback(
    async (currentTabDisplay, mode) => {
      if (!currentTabDisplay) return
      try {
        setLoading(true)
        var spyroVMYield1 = []
        if (currentTabDisplay == 'Yield') {
          spyroVMYield1 = await DataService.getSpyroOutputDataYield(
            keycloak,
            mode,
            currentTabDisplay,
          )
        }
        let transformedData1 = (spyroVMYield1.data || []).map(
          (item, index) => ({
            ...item,
            id: index,
            isEditable: index !== spyroVMYield1?.data?.length - 1,
          }),
        )

        if (transformedData1.length > 0 && currentTabDisplay === 'Yield') {
          const numericColumns = [
            'fourFPropane',
            'fiveFC2C3',
            'fiveFEthane',
            'fiveFPropane',
            'fourFC2C3',
            'fourFDC2C3',
            'fourFDEthane',
            'fourFDPropane',
            'fourFEthane',
          ]

          const totalRow = {
            id: 'total_row',
            particulars: 'Total',
            isTotal: true, // Flag to identify total row
            editable: false, // Make total row non-editable
          }

          // Calculate totals for each numeric column
          numericColumns.forEach((column) => {
            totalRow[column] = transformedData1
              .reduce((sum, row) => {
                const value = parseFloat(row[column]) || 0
                return sum + value
              }, 0)
              .toFixed(2) // Round to 2 decimal places
          })

          // Add total row at the end
          // transformedData1.push(totalRow)
        }
        setRowsForTab(currentTabDisplay, transformedData1)
      } catch (err) {
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
      if (currentTabDisplay === 'Yield') {
        fetchCrackerRowsYield(currentTabDisplay, selectMode)
      } else {
        fetchCrackerRows(currentTabDisplay, selectMode)
      }
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
    fetchCrackerRowsYield,
    keycloak,
    currentTabDisplay,
    yearChanged,
  ])

  const saveChanges = useCallback(async () => {
    if (currentTabDisplay === 'Yield') {
      await saveSpyroDataYield(yieldRows)
      return
    }
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
    setLoading(true)

    try {
      const SpyroInputData = newRows.map((row) => ({
        normParameterFKID: row.normParameterFKID,
        remarks: row.remarks,
        Remarks: row.remarks,
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
      }))
      const response = await DataService.saveSpyroOutput(
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
        // Reload current tab
        const tabId = tabs[tabIndex]
        if (tabId) {
          if (currentTabDisplay === 'Yield') {
            fetchCrackerRowsYield(currentTabDisplay, selectMode)
          } else {
            fetchCrackerRows(currentTabDisplay, selectMode)
          }
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error saving data!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Error saving data!', error)
    } finally {
      setLoading(false)
    }
  }

  const saveSpyroDataYield = async (newRows) => {
    setLoading(true)

    try {
      const dataToSave = newRows.filter((row) => !row.isTotal)
      const SpyroOutputYield = newRows.map((row) => ({
        particulars: row.particulars,
        fourFPropane: row.fourFPropane || 0,
        fiveFC2C3: row.fiveFC2C3 || 0,
        fiveFEthane: row.fiveFEthane || 0,
        fiveFPropane: row.fiveFPropane || 0,
        fourFC2C3: row.fourFC2C3 || 0,
        fourFDC2C3: row.fourFDC2C3 || 0,
        fourFDEthane: row.fourFDEthane || 0,
        fourFDPropane: row.fourFDPropane || 0,
        fourFEthane: row.fourFEthane || 0,
      }))

      const response = await DataService.saveSpyroOutputYield(
        SpyroOutputYield,
        keycloak,
      )
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        // Reload current tab
        const tabId = tabs[tabIndex]
        if (tabId) {
          if (currentTabDisplay === 'Yield') {
            fetchCrackerRowsYield(currentTabDisplay, selectMode)
          } else {
            fetchCrackerRows(currentTabDisplay, selectMode)
          }
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error saving data!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Error saving data!', error)
    } finally {
      setLoading(false)
    }
  }

  const saveSpyroOutputExcelFile = async (rawFile) => {
    setLoading(true)

    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      const plantId = storedPlant ? JSON.parse(storedPlant)?.id : ''
      const mode = selectMode || '' // Optional

      let response

      if (currentTabDisplay === 'Yield') {
        response = await DataService.importSpyroOutputExcelYield(
          rawFile,
          keycloak,
          mode,
        )
      } else {
        response = await DataService.importSpyroOutputExcel(
          rawFile,
          keycloak,
          mode,
        )
      }

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        if (currentTabDisplay === 'Yield') {
          fetchCrackerRowsYield(currentTabDisplay, selectMode)
        } else {
          fetchCrackerRows(currentTabDisplay, selectMode)
        }
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
        link.setAttribute('download', 'Error File - Optimizer Output.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        if (currentTabDisplay === 'Yield') {
          fetchCrackerRowsYield(currentTabDisplay, selectMode)
        } else {
          fetchCrackerRows(currentTabDisplay, selectMode)
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error uploading Optimizer Output Excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = (rawFile) => {
    saveSpyroOutputExcelFile(rawFile)
  }

  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    const mode = selectMode // Can be empty � that's fine

    try {
      let response
      if (currentTabDisplay === 'Yield') {
        response = await DataService.exportSpyroOutputExcelYield(keycloak, mode)
      } else {
        response = await DataService.exportSpyroOutputExcel(keycloak, mode)
      }

      if (response?.code === 200) {
        // setSnackbarOpen(true)
        // setSnackbarData({
        //   message: 'Excel download completed successfully!',
        //   severity: 'success',
        // })
      } else {
        // setSnackbarOpen(true)
        // setSnackbarData({
        //   message: 'Failed to download Excel1.',
        //   severity: 'error',
        // })
      }
    } catch (error) {
      console.error('Error downloading Excel:', error)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    } finally {
      setSnackbarOpen(true)
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
            case 'Total Feed':
            case 'Total Products':
            case 'Miscellaneous Parameters':
            case 'Constant':
            case 'Yield':
              return (
                <Box key={currentTabDisplay}>
                  <KendoDataTables
                    rows={rows}
                    setRows={setRowsForCurrent}
                    fetchData={() =>
                      currentTabDisplay === 'Yield'
                        ? fetchCrackerRowsYield(currentTabDisplay, selectMode)
                        : fetchCrackerRows(currentTabDisplay, selectMode)
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

            default:
              return null
          }
        })()}
      </Box>
    </Box>
  )
}

export default CrackerConfig
