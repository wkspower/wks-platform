// import Tooltip from '@mui/material/Tooltip'
// import { truncateRemarks } from 'utils/remarksUtils'
import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
// import NumericInputOnly from 'utils/NumericInputOnly'
//import DataGridTable from '../ASDataGrid'
import KendoDataTables from './index'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
import { useDispatch } from 'react-redux'
import { setIsBlocked } from 'store/reducers/dataGridStore'
// import TextField from '@mui/material/TextField'

import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { Box, Typography } from '../../../node_modules/@mui/material/index'
import { styled } from '@mui/material/styles'

const CustomAccordion = styled((props) => (
  <MuiAccordion disableGutters elevation={0} square {...props} />
))(() => ({
  position: 'unset',
  border: 'none',
  boxShadow: 'none',
  margin: '0px',
  '&:before': {
    display: 'none',
  },
}))
const CustomAccordionSummary = styled((props) => (
  <MuiAccordionSummary expandIcon={<ExpandMoreIcon />} {...props} />
))(() => ({
  backgroundColor: '#fff',
  padding: '0px 12px',
  minHeight: '40px',
  '& .MuiAccordionSummary-content': {
    margin: '8px 0',
  },
}))
const CustomAccordionDetails = styled(MuiAccordionDetails)(() => ({
  padding: '0px 0px 12px',
  backgroundColor: '#F2F3F8',
}))

const NormalOpNormsScreen = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [allProducts, setAllProducts] = useState([])
  const [allRedCell, setAllRedCell] = useState([])
  // const [bdData, setBDData] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const [calculationObject, setCalculationObject] = useState([])

  // const { sitePlantChange } = menu
  const [open1, setOpen1] = useState(false)
  // const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  // const [rowModesModel, setRowModesModel] = useState({})
  const [rows, setRows] = useState()
  const [rowsIntermediateValues, setRowsIntermediateValues] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [loading, setLoading] = useState(false)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const dispatch = useDispatch()
  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const keycloak = useSession()

  const fetchData = async () => {
    try {
      setLoading(true)

      // Fetch data from API
      const response = await DataService.getNormalOperationNormsData(keycloak)

      if (response?.code !== 200) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error fetching data. Please try again.',
          severity: 'error',
        })
        setLoading(false)
        return
      }

      setCalculationObject(response?.data?.aopCalculation)

      const formattedData = response?.data?.mcuNormsValueDTOList.map(
        (item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.remarks,
          Particulars: item.normParameterTypeDisplayName,
        }),
      )

      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
      setLoading(false)
    }
  }

  const fetchDataIntermediateValues = async () => {
    // setLoading(true)
    try {
      const res = await DataService.getIntermediateValues(keycloak)
      if (res?.code == 200) {
        const formattedData = res?.data.map((item, index) => {
          const formattedItem = {
            ...item,
            isEditable: false,
            id: index,
          }
          return formattedItem
        })
        // console.log(formattedData)
        setRowsIntermediateValues(formattedData)
        // setLoading(false)
      }
    } catch (error) {
      setLoading(false)
      console.error('Error fetching data:', error)
    }
  }

  const getNormTransactions = async () => {
    try {
      const res = await DataService.getNormTransactions(keycloak)
      if (res?.code == 200) {
        const normalized = res?.data.map((obj) => ({
          ...obj,
          normParameterFKId: obj.normParameterFKId.toUpperCase(),
        }))
        setAllRedCell(normalized)
      }
    } catch (error) {
      console.error('Error fetching data:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const getAllProducts = async () => {
    try {
      const data = await DataService.getAllProducts(keycloak, null)
      const productList = data.map((product) => ({
        id: product.id,
        displayName: product.displayName,
      }))
      setAllProducts(productList)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const fetchAllData = async () => {
    setRows([])
    setRowsIntermediateValues([])
    setAllRedCell([])
    setAllProducts([])
    setLoading(true)
    try {
      await Promise.all([
        fetchData(),
        fetchDataIntermediateValues(),
        getAllProducts(),
        getNormTransactions(),
      ])
    } catch (error) {
      console.error('Error during data fetching:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAllData()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const colDefs = [
    {
      field: 'Particulars',
      title: 'Type',
      width: 110,
      groupable: true,
      editable: false,
    },
    {
      field: 'materialFkId',
      title: 'Particulars',
      width: 120,
    },

    {
      field: 'UOM',
      title: 'UOM / MT',
      width: 100,
      editable: false,
    },

    {
      field: 'april',
      title: headerMap[4],
      editable: true,
      width: 120,
      align: 'right',
      format: '{0:n5}',
    },
    {
      field: 'may',
      title: headerMap[5],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:n5}',
    },
    {
      field: 'june',
      title: headerMap[6],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:n5}',
    },
    {
      field: 'july',
      title: headerMap[7],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:n5}',
    },

    {
      field: 'august',
      title: headerMap[8],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:n5}',
    },
    {
      field: 'september',
      title: headerMap[9],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:n5}',
    },
    {
      field: 'october',
      title: headerMap[10],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:n5}',
    },
    {
      field: 'november',
      title: headerMap[11],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:n5}',
    },
    {
      field: 'december',
      title: headerMap[12],
      editable: true,
      width: 120,
      align: 'right',
      format: '{0:n5}',
    },
    {
      field: 'january',
      title: headerMap[1],
      editable: true,
      width: 120,
      align: 'right',
      format: '{0:n5}',
    },
    {
      field: 'february',
      title: headerMap[2],
      editable: true,
      width: 120,
      align: 'right',
      format: '{0:n5}',
    },
    {
      field: 'march',
      title: headerMap[3],
      editable: true,
      width: 120,
      align: 'right',
      format: '{0:n5}',
    },
    {
      field: 'remarks',
      title: 'Remark',
      width: 125,
      editable: true,
    },

    {
      field: 'idFromApi',
      title: 'idFromApi',
    },
    {
      field: 'isEditable',
      title: 'isEditable',
    },
  ]

  const colDefsIntermediateValues = [
    {
      field: 'NormTypeName',
      title: 'Particulars',
      width: 160,
    },
    {
      field: 'UOM',
      title: 'UOM',
      width: 80,
      editable: false,
    },
    {
      field: 'Apr',
      title: headerMap[4],
      editable: false,
      width: 120,
      align: 'right',
      format: '{0:n5}',
    },

    {
      field: 'May',
      title: headerMap[5],
      editable: false,
      //renderEditCell: NumericInputOnly,
      width: 120,
      align: 'right',
      format: '{0:n5}',
      //valueFormatter: formatValueToFiveDecimals,
    },
    {
      field: 'Jun',
      title: headerMap[6],
      editable: false,
      //renderEditCell: NumericInputOnly,
      width: 120,
      align: 'right',
      format: '{0:n5}',
      //valueFormatter: formatValueToFiveDecimals,
    },
    {
      field: 'Jul',
      title: headerMap[7],
      editable: false,
      //renderEditCell: NumericInputOnly,
      width: 120,
      align: 'right',
      format: '{0:n5}',
      //valueFormatter: formatValueToFiveDecimals,
    },

    {
      field: 'Aug',
      title: headerMap[8],
      editable: false,
      //renderEditCell: NumericInputOnly,
      width: 120,
      align: 'right',
      format: '{0:n5}',
      //valueFormatter: formatValueToFiveDecimals,
    },
    {
      field: 'Sep',
      title: headerMap[9],
      editable: false,
      //renderEditCell: NumericInputOnly,
      width: 120,
      align: 'right',
      format: '{0:n5}',
      //valueFormatter: formatValueToFiveDecimals,
    },
    {
      field: 'Oct',
      title: headerMap[10],
      editable: false,
      //renderEditCell: NumericInputOnly,
      width: 120,
      align: 'right',
      format: '{0:n5}',
      //valueFormatter: formatValueToFiveDecimals,
    },
    {
      field: 'Nov',
      title: headerMap[11],
      editable: false,
      //renderEditCell: NumericInputOnly,
      width: 120,
      align: 'right',
      format: '{0:n5}',
      //valueFormatter: formatValueToFiveDecimals,
    },
    {
      field: 'Dec',
      title: headerMap[12],
      editable: false,
      //renderEditCell: NumericInputOnly,
      width: 120,
      align: 'right',
      format: '{0:n5}',
      //valueFormatter: formatValueToFiveDecimals,
    },
    {
      field: 'Jan',
      title: headerMap[1],
      editable: false,
      //renderEditCell: NumericInputOnly,
      width: 120,
      align: 'right',
      format: '{0:n5}',
      //valueFormatter: formatValueToFiveDecimals,
    },
    {
      field: 'Feb',
      title: headerMap[2],
      editable: false,
      //renderEditCell: NumericInputOnly,
      width: 120,
      align: 'right',
      format: '{0:n5}',
      //valueFormatter: formatValueToFiveDecimals,
    },
    {
      field: 'Mar',
      title: headerMap[3],
      editable: false,
      //renderEditCell: NumericInputOnly,
      width: 120,
      align: 'right',
      format: '{0:n5}',
      //valueFormatter: formatValueToFiveDecimals,
    },
    {
      field: 'idFromApi',
      title: 'idFromApi',
    },
    {
      field: 'isEditable',
      title: 'isEditable',
    },
  ]

  const handleRemarkCellClick = (row) => {
    if (!row?.isEditable) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // const processRowUpdate = React.useCallback((newRow, oldRow) => {
  //   const rowId = newRow.id
  //   const updatedFields = []
  //   for (const key in newRow) {
  //     if (
  //       Object.prototype.hasOwnProperty.call(newRow, key) &&
  //       newRow[key] !== oldRow[key]
  //     ) {
  //       updatedFields.push(key)
  //     }
  //   }

  //   unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

  //   if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
  //     unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
  //   }

  //   setRows((prevRows) =>
  //     prevRows.map((row) =>
  //       row.id === newRow.id ? { ...newRow, isNew: false } : row,
  //     ),
  //   )

  //   if (updatedFields.length > 0) {
  //     setModifiedCells((prevModifiedCells) => ({
  //       ...prevModifiedCells,
  //       [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
  //     }))
  //   }

  //   return newRow
  // }, [])

  const saveChanges = React.useCallback(async () => {
    // const rowsInEditMode = Object.keys(rowModesModel).filter(
    //   (id) => rowModesModel[id]?.mode === 'edit',
    // )

    // rowsInEditMode.forEach((id) => {
    //   apiRef.current.stopRowEditMode({ id })
    // })

    setTimeout(() => {
      try {
        console.log('modifiedCells', modifiedCells)
        // let newRows = modifiedCells.filter((row) => row.isGroupHeader !== true)
        // console.log(newRows)
        var data = Object.values(modifiedCells)
        if (data.length == 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          return
        }

        const requiredFields = ['materialFkId', 'remarks']
        const validationMessage = validateFields(data, requiredFields)
        if (validationMessage) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: validationMessage,
            severity: 'error',
          })
          return
        }

        saveNormalOperationNormsData(data)
      } catch (error) {
        /* empty */
        console.log(error)
      }
    }, 400)
  }, [modifiedCells])

  const saveNormalOperationNormsData = async (newRows) => {
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const businessData = newRows.map((row) => ({
        april: row.april || null,
        may: row.may || null,
        june: row.june || null,
        july: row.july || null,
        august: row.august || null,
        september: row.september || null,
        october: row.october || null,
        november: row.november || null,
        december: row.december || null,
        january: row.january || null,
        february: row.february || null,
        march: row.march || null,
        remark: row.remarks,
        remarks: row.remarks,
        financialYear: localStorage.getItem('year'),
        plantId: plantId,
        normParameterId: row.normParameterId,
        id: row.idFromApi || null,
        materialFkId: row.materialFkId || null,
        mcuVersion: row.mcuVersion || null,
        plantFkId: row.plantFkId || null,
        siteFkId: row.siteFkId || null,
        verticalFkId: row.verticalFkId || null,
        unit: row.unit || null,
        normParameterTypeId: row.normParameterTypeId || null,
      }))
      if (businessData.length > 0) {
        const response = await DataService.saveNormalOperationNormsData(
          plantId,
          businessData,
          keycloak,
        )

        // if (response.status === 200) {
        if (response) {
          dispatch(setIsBlocked(false))
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Normal Operations Norms Saved Successfully!`,
            severity: 'success',
          })
          setModifiedCells({})
          unsavedChangesRef.current = {
            unsavedRows: {},
            rowsBeforeChange: {},
          }
          fetchData()
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Normal Operations Norms not saved!`,
            severity: 'error',
          })
        }
        return response
      }
    } catch (error) {
      console.error(`Error saving Normal Operations Norms`, error)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }

  // const onProcessRowUpdateError = React.useCallback((error) => {
  //   console.log(error)
  // }, [])

  // const onRowModesModelChange = (newRowModesModel) => {
  //   setRowModesModel(newRowModesModel)
  // }

  const isCellEditable = (params) => {
    return params.row.isEditable
  }

  const handleCalculate = async () => {
    setLoading(true)
    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var plantId = plantId
      const data = await DataService.handleCalculateNormalOpsNorms34(
        plantId,
        year,
        keycloak,
      )

      if (data == 0 || data) {
        // dispatch(setIsBlocked(true))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        fetchData()
        fetchDataIntermediateValues()
        getNormTransactions()
        setLoading(false)
      } else {
        setSnackbarOpen(true)

        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
        setLoading(false)
      }

      return data
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      setLoading(false)
      console.error('Error!', error)
    }
  }

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      showCalculate: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      allAction: true,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: true,
      saveBtn: true,
      showCalculate:
        lowerVertName === 'meg' &&
        Object.keys(calculationObject || {}).length > 0
          ? true
          : false,
    },
    isOldYear,
  )

  const getAdjustedPermissionsIV = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      showCalculate: false,
    }
  }

  const adjustedPermissionsIV = getAdjustedPermissionsIV(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      showCalculate: false,
      allAction: true,
    },
    isOldYear,
  )
  const NormParameterIdCell = (props) => {
    const productId = props.dataItem.materialFkId
    const product = allProducts.find((p) => p.id === productId)
    const displayName = product?.displayName || ''
    // console.log(displayName)
    return <td>{displayName}</td>
  }
  const NormParameterIdCellForImmidiate = (props) => {
    const productId = props.dataItem.NormParameterFKId
    const product = allProducts.find((p) => p.id === productId)
    const displayName = product?.displayName || ''
    // console.log(displayName)
    return <td>{displayName}</td>
  }
  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <KendoDataTables
        NormParameterIdCell={NormParameterIdCell}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Normal Operations Norms'
        columns={colDefs}
        setRows={setRows}
        rows={rows}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        // processRowUpdate={processRowUpdate}
        // rowModesModel={rowModesModel}
        // onRowModesModelChange={onRowModesModelChange}
        saveChanges={saveChanges}
        isCellEditable={isCellEditable}
        snackbarData={snackbarData}
        handleCalculate={handleCalculate}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        open1={open1}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        // onProcessRowUpdateError={onProcessRowUpdateError}
        fetchData={fetchData}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        unsavedChangesRef={unsavedChangesRef}
        handleRemarkCellClick={handleRemarkCellClick}
        permissions={adjustedPermissions}
        allRedCell={allRedCell}
        groupBy='Particulars'
      />

      {lowerVertName === 'meg' && (
        <Box sx={{ width: '100%', marginTop: 1 }}>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                Intermediate Values
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataTables
                  NormParameterIdCellForImmidiate={
                    NormParameterIdCellForImmidiate
                  }
                  title='Intermediate Values'
                  columns={colDefsIntermediateValues}
                  setRows={setRowsIntermediateValues}
                  rows={rowsIntermediateValues}
                  paginationOptions={[100, 200, 300]}
                  permissions={adjustedPermissionsIV}
                  groupBy='NormTypeName'
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </Box>
      )}
    </div>
  )
}

export default NormalOpNormsScreen
