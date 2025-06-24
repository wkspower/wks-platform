import { Box, CircularProgress, Backdrop } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import KendoDataTables from './index'
import { DataService } from 'services/DataService'
import { validateFields } from 'utils/validationUtils'
import { useSession } from 'SessionStoreContext'

// Helper: get plant ID from localStorage
function getSelectedPlantId() {
  try {
    const stored = localStorage.getItem('selectedPlant')
    return stored ? JSON.parse(stored).id : ''
  } catch {
    return ''
  }
  }

  // Pre-filled composition data with random values
function getStoredVerticalId() {
  return localStorage.getItem('verticalId') || ''
}

// Helper: show snackbar (you might replace with a context/hook)
function useSnackbar() {
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [open, setOpen] = useState(false)
  const show = (message, severity = 'info') => {
    setSnackbarData({ message, severity })
    setOpen(true)
  }
  return {
    snackbarData,
    snackbarOpen: open,
    setSnackbarOpen: setOpen,
    showSnackbar: show,
  }
}

function transformSpyroItem(item, index) {
  return {
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
    idFromApi: item.id,
    ...item,
  }
}

// Helper: sort by type using rank map
const typeRank = {
  'Total Feed': 1,
  'Total Products': 2,
  'Miscellaneous Parameters': 3,
}
// function sortByType(rows) {


  // Fetch composition data once on mount
//   return rows.slice().sort((a, b) => {
//     const rA = typeRank[a.ParticularsType] || 99
//     const rB = typeRank[b.ParticularsType] || 99
//     return rA - rB
//   })


// Helper: prepare payload for saving one row
function prepareSavePayload(row) {
    return {
    VerticalFKId: getStoredVerticalId(),
    PlantFKId: getSelectedPlantId(),
    NormParameterFKID: row.NormParameterFKID ?? null,
    Particulars: row.particulars ?? row.Particulars ?? null,
    NormParameterTypeName: row.NormParameterTypeName ?? null,
    NormParameterTypeFKID: row.NormParameterTypeFKID ?? null,
    Type: row.Type ?? null,
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
    }
  }

const CrackerConfigOutput = () => {
  const { verticalChange, oldYear } = useSelector((s) => s.dataGridStore)
  const lowerVert = (verticalChange?.selectedVertical || '').toLowerCase()
  const isOldYear = oldYear?.oldYear === 1
  const keycloak = useSession()
  const { snackbarData, snackbarOpen, setSnackbarOpen, showSnackbar } =
    useSnackbar()
  const [loading, setLoading] = useState(false)
  const [compositionRows, setCompositionRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [selectMode, setSelectMode] = useState('5F') // default
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const basePermissions = {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
    showModes: lowerVert === 'cracker',
      saveWithRemark: true,
      saveBtn: true,
    allAction: lowerVert === 'cracker',
      modes: ['5F', '4F', '4F+D'],
  }
  const permissions = isOldYear
    ? {
        ...basePermissions,
        showAction: false,
        addButton: false,
        saveBtn: false /* etc */,
  }
    : basePermissions

  const productionColumns = getEnhancedAOPColDefs({
    headerMap,
    handleRemarkCellClick,
    configType: 'cracker_composition',
  })
  function handleRemarkCellClick(row) {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const fetchCrackerRows = useCallback(
    async (mode) => {
      setLoading(true)
      try {
        const resp = await DataService.getSpyroOutputData(keycloak, mode)
        console.log(resp)
        if (resp?.data && Array.isArray(resp.data)) {
          const transformed = resp.data.map(transformSpyroItem)
          setCompositionRows(transformed)
        } else {
          setCompositionRows([])
          console.warn('No data from API')
        }
      } catch (err) {
        console.error('Fetch error', err)
        setCompositionRows([])
        showSnackbar('Failed to load data', 'error')
      } finally {
        setLoading(false)
      }
    },
    [keycloak],
  )
  useEffect(() => {
    fetchCrackerRows(selectMode)
  }, [selectMode])

  // Save changes handler
  const saveChanges = useCallback(async () => {
    if (!Object.keys(modifiedCells).length) {
      showSnackbar('No Records to Save!', 'info')
      return
  }
    const rowsToSave = Object.values(modifiedCells).filter((r) => r.inEdit)
    if (!rowsToSave.length) {
      showSnackbar('No Records to Save!', 'info')
      return
    }
    const msg = validateFields(rowsToSave, ['NormParameterTypeFKID', 'remarks'])
    if (msg) {
      showSnackbar(msg, 'error')
      return
    }

    setLoading(true)
    try {
      const payload = rowsToSave.map(prepareSavePayload)
      const resp = await DataService.saveSpyroOutput(payload, keycloak)
      if (resp?.code === 200 || resp?.success) {
        showSnackbar('Data saved successfully!', 'success')
        await fetchCrackerRows(selectMode)
        setModifiedCells({})
      } else {
        showSnackbar(resp?.message || 'Failed to save', 'error')
      }
    } catch (err) {
      console.error('Save error', err)
      showSnackbar('Network error while saving', 'error')
    } finally {
      setLoading(false)
    }
  }, [modifiedCells, keycloak, fetchCrackerRows, selectMode, showSnackbar])
  const NormParameterIdCell = (props) => {
    return <td>{props.dataItem.particulars}</td>
  }
  return (
    <Box>
      <KendoDataTables
        rows={compositionRows}
        setRows={setCompositionRows}
        fetchData={() => fetchCrackerRows(selectMode)}
        configType='cracker_composition'
        groupBy='ParticularsType'
        handleRemarkCellClick={handleRemarkCellClick}
        ParticularsCell={NormParameterIdCell} // or rename in DataTable accordingly
        columns={productionColumns}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        permissions={permissions}
        typeRank={typeRank}
        selectMode={selectMode}
        setSelectMode={setSelectMode}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        saveChanges={saveChanges}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
      />
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 9999 }}
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
    </Box>
  )
}

export default CrackerConfigOutput
// import { Box } from '@mui/material'
// import { generateHeaderNames } from 'components/Utilities/generateHeaders'
// import KendoDataTables from './index'
// import { CircularProgress, Backdrop } from '@mui/material'
