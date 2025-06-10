import { Box, Tab, Tabs } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import KendoDataTables from './index'
import { DataService } from 'services/DataService'
import { validateFields } from 'utils/validationUtils'

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
  const [rawTabIndex, setRawTabIndex] = useState(0)

  const [feedRowsDummy, setFeedRows] = useState([])
  const [compositionRowsDummy, setCompositionRows] = useState([])
  const [hydrogenationRowsDummy, setHydrogenationRows] = useState([])
  const [recoveryRowsDummy, setRecoveryRows] = useState([])
  const [optimizingRowsDummy, setOptimizingRows] = useState([])
  const [furnaceRowsDummy, setFurnaceRows] = useState([])
  const allModes = ['5F', '4F', '4F+D']
  const [selectMode, setSelectMode] = useState(allModes[0])
  const fetchCrackerRows = useCallback(
    async (tab, mode) => {
      try {
        const spyroVM = await DataService.getSpyroInputData(keycloak, mode)

        // setTimeout(() => {
        switch (tab) {
          case 'feed':
            setFeedRows(spyroVM.data || [])
            break
          case 'composition':
            setCompositionRows(spyroVM.data)
            break
          case 'hydrogenation':
            setHydrogenationRows(spyroVM.data)
            break
          case 'recovery':
            setRecoveryRows(spyroVM.data)
            break
          case 'optimizing':
            setOptimizingRows(spyroVM.data)
            break
          case 'furnace':
            setFurnaceRows(spyroVM.data)
            break
          default:
            break
        }
        // }, 500)
        // console.log(spyroVM)
      } catch (err) {
        console.error('Error loading Spyro‑Input data:', err)
        // setFeedRows(spyroVM.data)
      }
    },
    [keycloak],
  )
  useEffect(() => {
    const currentTab = rawTabs[rawTabIndex]
    fetchCrackerRows(currentTab, selectMode)
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
      addButton: true,
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
    // setLoading(true)
    // const rowsInEditMode = Object.keys(rowModesModel).filter(
    //   (id) => rowModesModel[id]?.mode === 'edit',
    // )
    // rowsInEditMode.forEach((id) => {
    //   apiRef.current.stopRowEditMode({ id })
    // })
    // setTimeout(() => {
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
      // var data = Object.values(unsavedChangesRef.current.unsavedRows)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      // const requiredFields = ['NormParameterTypeFKID', 'Remarks']

      // const validationMessage = validateFields(data, requiredFields)

      // if (validationMessage) {
      //   setSnackbarOpen(true)
      //   setSnackbarData({
      //     message: validationMessage,
      //     severity: 'error',
      //   })
      //   setLoading(false)
      //   return
      // }
      saveBusinessDemandData(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
    // }, 400)
  }, [modifiedCells])
  const saveBusinessDemandData = async (newRows) => {
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
        Particulars: row.Particulars ?? null,
        NormParameterTypeName: row.NormParameterTypeName ?? null,
        NormParameterTypeFKID: row.NormParameterTypeFKID ?? null,
        Type: row.Type ?? null,
        UOM: row.UOM ?? null,
        AuditYear: row.AuditYear ?? null,
        Remarks: row.Remarks ?? null,
        Jan: row.Jan ?? null,
        Feb: row.Feb ?? null,
        Mar: row.Mar ?? null,
        Apr: row.Apr ?? null,
        May: row.May ?? null,
        Jun: row.Jun ?? null,
        Jul: row.Jul ?? null,
        Aug: row.Aug ?? null,
        Sep: row.Sep ?? null,
        Oct: row.Oct ?? null,
        Nov: row.Nov ?? null,
        Dec: row.Dec ?? null,
        id: row.idFromApi ?? null,
        inEdit: row.inEdit || false,
      }))

      const response = await DataService.saveSpyroInput(
        SpyroInputData,
        keycloak,
      )
      // console.log()
      if (response?.status === 200) {
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
    } finally {
      // fetchData()
    }
  }
  return (
    <Box>
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
                    loading={loading}
                    modifiedCells={modifiedCells}
                    setModifiedCells={setModifiedCells}
                  />
                </Box>
              )

            case 'composition':
              return (
                <Box>
                  <KendoDataTables
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
                    loading={loading}
                    modifiedCells={modifiedCells}
                    setModifiedCells={setModifiedCells}
                  />
                </Box>
              )

            case 'hydrogenation':
              return (
                <Box>
                  <KendoDataTables
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
                    loading={loading}
                    modifiedCells={modifiedCells}
                    setModifiedCells={setModifiedCells}
                  />
                </Box>
              )

            case 'recovery':
              return (
                <Box>
                  <KendoDataTables
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
                    loading={loading}
                    modifiedCells={modifiedCells}
                    setModifiedCells={setModifiedCells}
                  />
                </Box>
              )

            case 'optimizing':
              return (
                <Box>
                  <KendoDataTables
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
                    loading={loading}
                    modifiedCells={modifiedCells}
                    setModifiedCells={setModifiedCells}
                  />
                </Box>
              )

            case 'furnace':
              return (
                <Box>
                  <KendoDataTables
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
                    loading={loading}
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
