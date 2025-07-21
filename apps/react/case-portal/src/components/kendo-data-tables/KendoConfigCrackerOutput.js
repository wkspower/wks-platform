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
  const { verticalChange, oldYear, plantID, yearChanged } = dataGridStore
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
    'Total Feed',
    'Total Products',
    'Miscellaneous Parameters',
    'Constant',
    'Yield',
  ]
  const [tabs, setTabs] = useState(rawTabsStatic)
  const [availableTabs, setAvailableTabs] = useState([])
  const [tabIndex, setTabIndex] = useState(0)

  // ===== Separate row states per known tab =====
  // Initialize an individual state for each rawTabsStatic entry:
  const [yieldRows, setYieldRows] = useState([])
  const [constantsRows, setConstantsRows] = useState([])
  const [feedRows, setFeedRows] = useState([])
  const [compositionRows, setCompositionRows] = useState([])
  const [hydrogenationRows, setHydrogenationRows] = useState([])
  // const [recoveryRows, setRecoveryRows] = useState([])
  // const [optimizing, setOptimizing] = useState([])
  // const [furnace, setFurnance] = useState([])

  // Mode selection
  // const allModes = ['5F Operation', '4F Operation', '4F+D Operation']
  const allModes = ['5F', '4F', '4F+D']
  const [selectMode, setSelectMode] = useState(allModes[0])

  // NormParameterIdCell
  const NormParameterIdCell = (props) => <td>{props?.dataItem?.particulars}</td>

  // currentTab displayName
  const currentTabDisplay = useMemo(() => {
    const idLower = tabs[tabIndex]?.toLowerCase() || ''
    const info = availableTabs.find((t) => t.id.toLowerCase() === idLower)
    // console.log(info)
    return info ? info.displayName : tabs[tabIndex] || 'Feed'
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
      modes: allModes,
      uploadExcelBtn: true,
      downloadExcelBtn: true,
    },
    isOldYear,
  )

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
    [
      feedRows,
      compositionRows,
      hydrogenationRows,
      constantsRows,
      yieldRows,
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
        let transformedData1 = []
        if (spyroVMYield1 && Array.isArray(spyroVMYield1.data)) {
          const rowMap = {}

          spyroVMYield1.data.forEach((item, i) => {
            const comp = item.displayName
            const col = `${item.operation}_${item.type}`

            if (!rowMap[comp]) {
              rowMap[comp] = {
                id: `row_${i}`,
                particulars: comp,
                uom: item.uom,
                remarks: item.remarks,
                NormParameterFKID: item.normParameterId,
                '5F_C2C3': null,
                '5F_Propane': null,
                '5F_Ethane': null,
                '4F_C2C3': null,
                '4F_Propane': null,
                '4F_Ethane': null,
                '4FD_C2C3': null,
                '4FD_Propane': null,
                '4FD_Ethane': null,
              }
            }

            rowMap[comp][col] = item.attributeValue
          })

          transformedData1 = Object.values(rowMap)
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

  const [modifiedCells, setModifiedCells] = useState({})

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
        Jan: row.jan || null,
        Feb: row.feb || null,
        Mar: row.march || null,
        Apr: row.april || null,
        May: row.may || null,
        Jun: row.june || null,
        Jul: row.july || null,
        Aug: row.aug || null,
        Sep: row.sep || null,
        Oct: row.oct || null,
        Nov: row.nov || null,
        Dec: row.dec || null,
        id: row.idFromApi || null,
        inEdit: row.inEdit || false,
      }))
      const response = await DataService.saveSpyroOutput(
        SpyroInputData,
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

  const saveSpyroDataYield = async (newRows) => {
    setLoading(true)

    try {
      const SpyroOutputYield = newRows.map((row) => ({
        particulars: row.particulars,
        '5F_C2C3': row['5F_C2C3'],
        '5F_Propane': row['5F_Propane'],
        '5F_Ethane': row['5F_Ethane'],
        '4F_C2C3': row['4F_C2C3'],
        '4F_Propane': row['4F_Propane'],
        '4F_Ethane': row['4F_Ethane'],
        '4FD_C2C3': row['4FD_C2C3'],
        '4FD_Propane': row['4FD_Propane'],
        '4FD_Ethane': row['4FD_Ethane'],
      }))

      // console.log('SpyroOutputYield', SpyroOutputYield)

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
//----
  const saveSpyroOutputExcelFile = async (rawFile) => {
  setLoading(true);

  try {
    const storedPlant = localStorage.getItem('selectedPlant');
    const plantId = storedPlant ? JSON.parse(storedPlant)?.id : '';
    const mode = selectMode || ''; // Optional

    let response;

    // Spyro Output Excel Import API call
    response = await DataService.importSpyroOutputExcel(rawFile, keycloak, mode);

    if (response?.code === 200) {
      setSnackbarOpen(true);
      setSnackbarData({
        message: 'Uploaded Successfully!',
        severity: 'success',
      });
      setModifiedCells({});
      fetchAllData?.();
    } else if (response?.code === 400 && response?.data) {
      const byteCharacters = atob(response.data);
      const byteNumbers = Array.from(byteCharacters, char => char.charCodeAt(0));
      const byteArray = new Uint8Array(byteNumbers);

      const blob = new Blob([byteArray], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      });

      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'Error File - Spyro Output.xlsx');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      setSnackbarOpen(true);
      setSnackbarData({
        message: 'Partial data saved. Error file downloaded.',
        severity: 'warning',
      });
    } else {
      setSnackbarOpen(true);
      setSnackbarData({
        message: 'Upload Failed!',
        severity: 'error',
      });
    }

    return response;
  } catch (error) {
    console.error('Error uploading Spyro Output Excel:', error);
    setSnackbarOpen(true);
    setSnackbarData({
      message: 'Unexpected error occurred!',
      severity: 'error',
    });
  } finally {
    setLoading(false);
  }
};

  const handleExcelUpload = (rawFile) => {
    saveSpyroOutputExcelFile(rawFile);
  };

  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true);
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    });
  
    const mode = selectMode;          // Can be empty — that's fine
  
    try {
      const response = await DataService.exportSpyroOutputExcel(
        keycloak,
        mode
      );
  
      if (response?.code === 200) {
        setSnackbarData({
          message: 'Excel download completed successfully!',
          severity: 'success',
        });
      } else {
        setSnackbarData({
          message: 'Failed to download Excel.',
          severity: 'error',
        });
      }
    } catch (error) {
      console.error('Error downloading Excel:', error);
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      });
    } finally {
      setSnackbarOpen(true);
    }
  };

//----
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
                padding: '9px',
                minHeight: '10px',
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
                    handleExcelUpload={handleExcelUpload}
                    downloadExcelForConfiguration={downloadExcelForConfiguration}
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
