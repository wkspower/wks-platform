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
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const [calculationObject, setCalculationObject] = useState([])
  const [open1, setOpen1] = useState(false)
  const apiRef = useGridApiRef()
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
    try {
      const res = await DataService.getIntermediateValues(keycloak)
      if (res?.code == 200) {
        const formattedData = res?.data.map((item, index) => {
          const formattedItem = {
            ...item,
            isEditable: false,
            id: index,
            Particulars: item.NormTypeName,
          }
          return formattedItem
        })
        setRowsIntermediateValues(formattedData)
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
        // setAllRedCell(normalized)
        setAllRedCell([])
      }
    } catch (error) {
      console.error('Error fetching data:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const fetchAllData = async () => {
    setRows([])
    setRowsIntermediateValues([])
    setAllRedCell([])
    setLoading(true)
    try {
      await Promise.all([
        fetchData(),
        fetchDataIntermediateValues(),
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
      hidden: true,
    },
    {
      field: 'materialFkId',
      title: 'Particulars',
      width: 120,
      hidden: true,
    },
    {
      field: 'productName',
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
      format: '{0:#.#####}',
      type: 'number',
    },
    {
      field: 'may',
      title: headerMap[5],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
    },
    {
      field: 'june',
      title: headerMap[6],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
    },
    {
      field: 'july',
      title: headerMap[7],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
    },

    {
      field: 'august',
      title: headerMap[8],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
    },
    {
      field: 'september',
      title: headerMap[9],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
    },
    {
      field: 'october',
      title: headerMap[10],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
    },
    {
      field: 'november',
      title: headerMap[11],
      editable: true,

      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
    },
    {
      field: 'december',
      title: headerMap[12],
      editable: true,
      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
    },
    {
      field: 'january',
      title: headerMap[1],
      editable: true,
      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
    },
    {
      field: 'february',
      title: headerMap[2],
      editable: true,
      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
    },
    {
      field: 'march',
      title: headerMap[3],
      editable: true,
      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
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
      hidden: true,
    },
    {
      field: 'isEditable',
      title: 'isEditable',
      hidden: true,
    },
  ]

  const colDefsIntermediateValues = [
    {
      field: 'Particulars',
      title: 'Type',
      width: 110,
      groupable: true,
      editable: false,
      hidden: true,
    },

    {
      field: 'NormParameterFKId',
      title: 'Particulars',
      hidden: true,
    },

    {
      field: 'ProductName',
      title: 'Particulars',
      width: 120,
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
      format: '{0:#.#####}',
      type: 'number',
    },

    {
      field: 'May',
      title: headerMap[5],
      editable: false,
      width: 120,
      align: 'right',
      format: '{0:#.#####}',
      type: 'number',
    },
    {
      field: 'Jun',
      title: headerMap[6],
      editable: false,
      type: 'number',
      width: 120,
      align: 'right',
      format: '{0:#.#####}',
    },
    {
      field: 'Jul',
      title: headerMap[7],
      editable: false,
      type: 'number',
      width: 120,
      align: 'right',
      format: '{0:#.#####}',
    },

    {
      field: 'Aug',
      title: headerMap[8],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.#####}',
    },
    {
      field: 'Sep',
      title: headerMap[9],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.#####}',
    },
    {
      field: 'Oct',
      title: headerMap[10],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.#####}',
    },
    {
      field: 'Nov',
      title: headerMap[11],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.#####}',
    },
    {
      field: 'Dec',
      title: headerMap[12],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.#####}',
    },
    {
      field: 'Jan',
      title: headerMap[1],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.#####}',
    },
    {
      field: 'Feb',
      title: headerMap[2],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.#####}',
    },
    {
      field: 'Mar',
      title: headerMap[3],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.#####}',
    },
    {
      field: 'idFromApi',
      title: 'idFromApi',
      hidden: true,
    },
    {
      field: 'isEditable',
      title: 'isEditable',
      hidden: true,
    },
  ]

  const handleRemarkCellClick = (row) => {
    if (!row?.isEditable) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveChanges = React.useCallback(async () => {
    try {
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
  }, [modifiedCells])

  const saveNormalOperationNormsData = async (newRows) => {
    setLoading(true)
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
          setLoading(false)
          fetchData()
          fetchDataIntermediateValues()
          getNormTransactions()
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Normal Operations Norms not saved!`,
            severity: 'error',
          })
          setLoading(false)
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

  console.log('calculationObject', calculationObject)

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
      showCalculate: lowerVertName === 'meg' ? true : false,
      showCalculateVisibility:
        lowerVertName === 'meg' &&
        Object.keys(calculationObject || {}).length > 0
          ? true
          : false,
      downloadExcelBtn: lowerVertName == 'meg' ? true : false,
      uploadExcelBtn: lowerVertName == 'meg' ? true : false,
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

  const handleExcelUpload = (rawFile) => {
    saveExcelFile(rawFile)
  }
  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      await DataService.getNormalOpsNormsExcel(keycloak)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error!', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    } finally {
      // optional cleanup or logging
    }
  }

  const saveExcelFile = async (rawFile) => {
    setLoading(true)
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const response = await DataService.saveNormalOpsNormsExcel(
        rawFile,
        keycloak,
      )
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Configuration data Upload Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        setLoading(false)

        fetchAllData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving Configuration data:', error)
      setLoading(false)
    } finally {
      // fetchData()
      setLoading(false)
    }
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
        handleExcelUpload={handleExcelUpload}
        downloadExcelForConfiguration={downloadExcelForConfiguration}
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
