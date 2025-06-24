// CrackerConfig.jsx
import { Box, Tab, Tabs } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import KendoDataTables from './index'
import { DataService } from 'services/DataService'
import { validateFields } from 'utils/validationUtils'
import { Backdrop, CircularProgress } from '@mui/material'

const CrackerConfig = ({ keycloak }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, oldYear } = dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
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

  const rawTabs = [
    'feed',
    'composition',
    'hydrogenation',
    'recovery',
    'optimizing',
    'furnace',
  ]

  // Tab index
  const [rawTabIndex, setRawTabIndex] = useState(0)

  // Row states per tab

    // —— C2/C3 (existing) ——

    // —— Hexene Purge Gas ——

    // —— Import Propane ——

    // —— BPCL Kochi Propylene ——

    // —— FCC C3 ——

    // —— LDPE Off Gas ——

    // —— Additional Feed (Default Composition) ——





  const [feedRowsDummy, setFeedRows] = useState([])
  const [compositionRowsDummy, setCompositionRows] = useState([])
  const [hydrogenationRowsDummy, setHydrogenationRows] = useState([])
  const [recoveryRowsDummy, setRecoveryRows] = useState([])
  const [optimizingRowsDummy, setOptimizingRows] = useState([])
  const [furnaceRowsDummy, setFurnaceRows] = useState([])
  const allModes = ['5F', '4F', '4F+D']
  const [selectMode, setSelectMode] = useState(allModes[0])
  const fetchCrackerRows = useCallback(
    // Simulate network delay
    async (tab, mode) => {
      try {
        setLoading(true)
        const spyroVM = await DataService.getSpyroInputData(keycloak, mode)
        if (spyroVM?.data && Array.isArray(spyroVM.data)) {
          const transformedData = spyroVM.data.map((item, index) => {
            const transformedItem = {
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
            }
            return transformedItem
          })
      switch (tab) {
            case 'feed':
              setFeedRows(transformedData)
              console.log('Feed rows set:', transformedData.length)
          break
            case 'composition':
              setCompositionRows(transformedData)
              console.log('Composition rows set:', transformedData.length)
          break
            case 'hydrogenation':
              setHydrogenationRows(transformedData)
              console.log('Hydrogenation rows set:', transformedData.length)
          break
            case 'recovery':
              setRecoveryRows(transformedData)
              console.log('Recovery rows set:', transformedData.length)
          break
            case 'optimizing':
              setOptimizingRows(transformedData)
              console.log('Optimizing rows set:', transformedData.length)
          break
            case 'furnace':
              setFurnaceRows(transformedData)
              console.log('Furnace rows set:', transformedData.length)
          break
        default:
              console.warn('Unknown tab:', tab)
          }
        } else {
          const emptyArray = []
          switch (tab) {
            case 'feed':
              setFeedRows(emptyArray)
          break
            case 'composition':
              setCompositionRows(emptyArray)
              break
            case 'hydrogenation':
              setHydrogenationRows(emptyArray)
              break
            case 'recovery':
              setRecoveryRows(emptyArray)
              break
            case 'optimizing':
              setOptimizingRows(emptyArray)
              break
            case 'furnace':
              setFurnaceRows(emptyArray)
              break
      }
        }
      } catch (err) {
        const emptyArray = []
        switch (tab) {
          case 'feed':
            setFeedRows(emptyArray)
            break
          case 'composition':
            setCompositionRows(emptyArray)
            break
          case 'hydrogenation':
            setHydrogenationRows(emptyArray)
            break
          case 'recovery':
            setRecoveryRows(emptyArray)
            break
          case 'optimizing':
            setOptimizingRows(emptyArray)
            break
          case 'furnace':
            setFurnaceRows(emptyArray)
            break
        }
        setSnackbarData({
          message: `Failed to load ${tab} data. Please try again.`,
          severity: 'error',
        })
        setSnackbarOpen(true)
      } finally {
        setLoading(false)
      }
    },
    [keycloak],
  )

  // 5️⃣ Whenever the selected tab changes, reload that tab’s rows
  useEffect(() => {
    const currentTab = rawTabs[rawTabIndex]
    console.log('🔄 useEffect triggered:', {
      currentTab,
      rawTabIndex,
      selectMode,
      plantId,
      hasKeycloak: !!keycloak,
    })
    if (keycloak && plantId && currentTab) {
      fetchCrackerRows(currentTab, selectMode)
    } else {
      console.warn('⚠️ Missing required data for API call:', {
        hasKeycloak: !!keycloak,
        hasPlantId: !!plantId,
        currentTab,
      })
    }
  }, [rawTabIndex, fetchCrackerRows, selectMode, plantId])
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
      showModes: lowerVertName === 'cracker' ? true : false,
      saveWithRemark: true,
      saveBtn: true,
      allAction: lowerVertName === 'cracker' ? true : false,
      modes: allModes,
    },
    isOldYear,
  )
  const NormParameterIdCell = (props) => {
    // console.log(props)
    // const productId = props.dataItem.normParameterFKId
    // const product = allProducts.find((p) => p.id === productId)
    // const displayName = product?.displayName || ''
    // console.log(displayName)
    return <td>{props?.dataItem?.particulars}</td>
  }

  const productionColumns = getEnhancedAOPColDefs({
    // allGradesReciepes,
    // allProducts,
    headerMap,
    handleRemarkCellClick,
    configType:
      rawTabs[rawTabIndex] === 'composition'
        ? 'cracker_composition'
        : 'cracker', // columnConfig,
  })
  const saveChanges = useCallback(async () => {
    try {
      console.log('modifiedCells', modifiedCells)
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      var rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      const requiredFields = ['particulars', 'remarks']
      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }
      saveSpyroData(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCells])
  const saveSpyroData = async (newRows) => {
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      let verticalId = localStorage.getItem('verticalId')
      const SpyroInputData = newRows.map((row) => ({
        VerticalFKId: verticalId,
        PlantFKId: plantId,
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
        fetchCrackerRows()
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
        value={rawTabIndex}
        onChange={(e, newIndex) => setRawTabIndex(newIndex)}
      >
        {rawTabs.map((tabId) => (
          <Tab
            key={tabId}
            sx={{
              border: '1px solid #ADD8E6',
              borderBottom: '1px solid #ADD8E6',
              textTransform: 'capitalize',
            }}
            label={tabId}
          />
        ))}
      </Tabs>

      <Box>
        {(() => {
          switch (rawTabs[rawTabIndex]) {
            case 'feed':
              return (
                <Box>
                  <KendoDataTables
                    key={rawTabs[rawTabIndex]}
                    rows={feedRowsDummy}
                    setRows={setFeedRows}
                    fetchData={() => fetchCrackerRows('feed')}
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

            case 'composition':
              return (
                <Box>
                  <KendoDataTables
                    key={rawTabs[rawTabIndex]}
                    rows={compositionRowsDummy}
                    setRows={setCompositionRows}
                    fetchData={() => fetchCrackerRows('composition')}
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

            case 'hydrogenation':
              return (
                <Box>
                  <KendoDataTables
                    key={rawTabs[rawTabIndex]}
                    rows={hydrogenationRowsDummy}
                    setRows={setHydrogenationRows}
                    fetchData={() => fetchCrackerRows('hydrogenation')}
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

            case 'recovery':
              return (
                <Box>
                  <KendoDataTables
                    key={rawTabs[rawTabIndex]}
                    rows={recoveryRowsDummy}
                    setRows={setRecoveryRows}
                    fetchData={() => fetchCrackerRows('recovery')}
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

            case 'optimizing':
              return (
                <Box>
                  <KendoDataTables
                    key={rawTabs[rawTabIndex]}
                    rows={optimizingRowsDummy}
                    setRows={setOptimizingRows}
                    fetchData={() => fetchCrackerRows('optimizing')}
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

            case 'furnace':
              return (
                <Box>
                  <KendoDataTables
                    key={rawTabs[rawTabIndex]}
                    rows={furnaceRowsDummy}
                    setRows={setFurnaceRows}
                    fetchData={() => fetchCrackerRows('furnace')}
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
