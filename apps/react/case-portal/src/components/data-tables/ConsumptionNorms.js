import { DataService } from 'services/DataService'
import DataGridTable from './ASDataGrid'
import React, { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
import getEnhancedColDefs from './CommonHeader/consumptionHeader'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
import TextField from '@mui/material/TextField'
import { useDispatch } from 'react-redux'
import { setIsBlocked } from 'store/reducers/dataGridStore'

import Accordion from '@mui/material/Accordion'
import AccordionSummary from '@mui/material/AccordionSummary'
import AccordionDetails from '@mui/material/AccordionDetails'
import Typography from '@mui/material/Typography'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { Box } from '@mui/material'
import { Button } from '@mui/material'
//import './data-grid-css.css'
//import './extra-css.css'

import { styled } from '@mui/material/styles'
import MuiAccordion, { AccordionProps } from '@mui/material/Accordion'
import MuiAccordionSummary, {
  AccordionSummaryProps,
} from '@mui/material/AccordionSummary'
import MuiAccordionDetails from '@mui/material/AccordionDetails'

// Customized Accordion
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

// Customized Accordion Summary
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

// Customized Accordion Details
const CustomAccordionDetails = styled(MuiAccordionDetails)(() => ({
  padding: '0px 0px 12px',
  backgroundColor: '#F2F3F8',
}))

const NormalOpNormsScreen = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [summary, setSummary] = useState('')

  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [rowModesModel, setRowModesModel] = useState({})

  const [isAccordionExpanded, setIsAccordionExpanded] = useState(true)

  const handleAccordionChange = (event, isExpanded) => {
    setIsAccordionExpanded(isExpanded)
  }

  const [open1, setOpen1] = useState(false)

  const dataGridStore = useSelector((state) => state.dataGridStore)

  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const [loading, setLoading] = useState(false)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const dispatch = useDispatch()

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.aopRemarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // const getProductDisplayName = (id) => {
  //   if (!id) return
  //   const product = allProducts.find((p) => p.id === id)
  //   return product ? product.displayName : ''
  // }

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    const updatedFields = []

    for (const key in newRow) {
      if (
        Object.prototype.hasOwnProperty.call(newRow, key) &&
        newRow[key] !== oldRow[key]
      ) {
        updatedFields.push(key)
      }
    }
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    if (updatedFields.length > 0) {
      setModifiedCells((prevModifiedCells) => ({
        ...prevModifiedCells,
        [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
      }))
    }

    return newRow
  }, [])

  const saveEditedData = async (newRows) => {
    setLoading(true)
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      let siteID =
        JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

      let verticalId = localStorage.getItem('verticalId')

      const businessData = newRows.map((row) => ({
        april: row.april || null,
        may: row.may || null,
        june: row.june || null,
        july: row.july || null,
        aug: row.aug || null,
        sep: row.sep || null,
        oct: row.oct || null,
        nov: row.nov || null,
        dec: row.dec || null,
        jan: row.jan || null,
        feb: row.feb || null,
        march: row.march || null,
        aopRemarks: row.aopRemarks || null,
        aopYear: localStorage.getItem('year'),
        plantFkId: plantId,
        siteFkId: siteID,
        verticalFkId: verticalId,
        materialFkId: row.NormParametersId,
        id: row.idFromApi || null,
        aopCaseId: '2025-26-NormsAOP',
        aopStatus: 'Saved',
      }))
      const response = await DataService.saveAOPConsumptionNorm(
        plantId,
        businessData,
        keycloak,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Consumption AOP Saved Successfully!',
        severity: 'success',
      })
      //

      setLoading(false)
      setModifiedCells({})

      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
      fetchData()
      dispatch(setIsBlocked(false))

      return response
    } catch (error) {
      console.error('Error saving Consumption AOP!', error)
    } finally {
      //
      setLoading(false)
    }
  }

  const saveSummary = async () => {
    setLoading(true)
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      let year = localStorage.getItem('year')
      const response = await DataService.saveSummaryAOPConsumptionNorm(
        plantId,
        year,
        summary,
        keycloak,
      )

      if (response?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Summary Saved Successfully!',
          severity: 'success',
        })
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Summary Saved Failed!',
          severity: 'error',
        })
      }

      //

      setLoading(false)
      return response
    } catch (error) {
      console.error('Error saving Summary!', error)
    } finally {
      //
      setLoading(false)
    }
  }

  const saveChanges = React.useCallback(async () => {
    const rowsInEditMode = Object.keys(rowModesModel).filter(
      (id) => rowModesModel[id]?.mode === 'edit',
    )

    rowsInEditMode.forEach((id) => {
      apiRef.current.stopRowEditMode({ id })
    })
    setLoading(true)

    setTimeout(() => {
      const lowerVertName = JSON.parse(
        localStorage.getItem('selectedVertical'),
      )?.name?.toLowerCase()

      if (lowerVertName == 'meg') {
        // console.log('lowerVertName', lowerVertName)

        try {
          var data = Object.values(unsavedChangesRef.current.unsavedRows)
          if (data.length == 0) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: 'No Records to Save!',
              severity: 'info',
            })
            //
            setLoading(false)

            return
          }
          const requiredFields = ['aopRemarks']
          const validationMessage = validateFields(data, requiredFields)
          if (validationMessage) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: validationMessage,
              severity: 'error',
            })
            //
            setLoading(false)
            return
          }

          saveEditedData(data)
        } catch (error) {
          console.log('Error saving changes:', error)
          //
          setLoading(false)
        }
      }

      if (lowerVertName == 'pe') {
        try {
          setLoading(true)

          var editedData = Object.values(unsavedChangesRef.current.unsavedRows)
          var allRows = Array.from(apiRef.current.getRowModels().values())
          allRows = allRows.filter((row) => !row.isGroupHeader)
          const updatedRows = allRows.map(
            (row) => unsavedChangesRef.current.unsavedRows[row.id] || row,
          )

          //SKIP THIS IF saveBtn IS SET TO --> FALSE
          // if (updatedRows.length === 0) {
          //   setSnackbarOpen(true)
          //   setSnackbarData({
          //     message: 'No Records to Save!',
          //     severity: 'info',
          //   })
          //   setLoading(false)

          //   return
          // }

          const requiredFields = ['aopRemarks']

          const validationMessage = validateFields(editedData, requiredFields)
          if (validationMessage) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: validationMessage,
              severity: 'error',
            })

            setLoading(false)
            return
          }

          if (calculatebtnClicked == false) {
            // if (editedData.length === 0) {
            //   setSnackbarOpen(true)
            //   setSnackbarData({
            //     message: 'No Records to Save!',
            //     severity: 'info',
            //   })
            //   setLoading(false)

            //   setCalculatebtnClicked(false)
            //   return
            // }
            //UNCOMMNET THIS IF saveBtn IS SET TO --> TRUE
            saveEditedData(updatedRows)

            // setLoading(false)
            setCalculatebtnClicked(false)
            // saveEditedData(editedData)
          } else {
            saveEditedData(updatedRows)
          }
        } catch (error) {
          setLoading(false)
          console.log('Error saving changes:', error)
          setCalculatebtnClicked(false)
        }
      }
    }, 400)
  }, [apiRef, selectedUnit, rowModesModel, calculatebtnClicked])

  const fetchData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getConsumptionNormsData(keycloak)

      // const customOrder = [
      //   'Raw Material',
      //   'By Products',
      //   'Cat Chem',
      //   'Utility Consumption',
      //   'Configuration',
      // ]

      // const data = data1.sort(
      //   (a, b) =>
      //     customOrder.indexOf(a.normParameterTypeDisplayName) -
      //     customOrder.indexOf(b.normParameterTypeDisplayName),
      // )

      const groupedRows = []
      const groups = new Map()
      let groupId = 0

      data.forEach((item) => {
        const groupKey = item.normParameterTypeDisplayName

        if (!groups.has(groupKey)) {
          groups.set(groupKey, [])
          groupedRows.push({
            id: groupId++,
            Particulars: groupKey,
            isGroupHeader: true,
          })
        }
        const formattedItem = {
          ...item,
          idFromApi: item.id,
          NormParametersId: item.materialFkId.toLowerCase(),
          // originalRemark: item.aopRemarks,
          originalRemark: item.aopRemarks?.trim() || null,
          id: groupId++,
          isEditable: false,
        }

        groups.get(groupKey).push(formattedItem)
        groupedRows.push(formattedItem)
      })

      // setBDData(groupedRows)
      setRows(groupedRows)

      setLoading(false)
      setCalculatebtnClicked(false)
    } catch (error) {
      console.error('Error fetching data:', error)

      setLoading(false)
      setCalculatebtnClicked(false)
    }
  }
  const getAopSummary = async () => {
    setLoading(true)
    try {
      var res = await DataService.getAopSummary(keycloak)

      if (res?.code == 200) {
        setSummary(res?.data?.summary)
      } else {
        setSummary('')
      }

      setLoading(false)
      setCalculatebtnClicked(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
      setCalculatebtnClicked(false)
    }
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProductsAll(keycloak, 'All')
        const productList = data.map((product) => ({
          id: product.id.toLowerCase(),
          displayName: product.displayName,
          name: product.name,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching products:', error)
      }
    }

    getAllProducts()
    fetchData()
    getAopSummary()
  }, [
    sitePlantChange,
    oldYear,
    yearChanged,
    keycloak,
    selectedUnit,
    lowerVertName,
  ])

  const productionColumns = getEnhancedColDefs({
    allProducts,
    headerMap,
    handleRemarkCellClick,
  })

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  const handleCalculate = () => {
    if (lowerVertName == 'meg') {
      handleCalculateMeg()
    } else {
      handleCalculatePe()
    }
  }

  const handleCalculateMeg = async () => {
    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      const data = await DataService.handleCalculateonsumptionNorms(
        plantId,
        year,
        keycloak,
      )

      if (data || data == 0) {
        // dispatch(setIsBlocked(true))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
      }

      return data
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      console.error('Error!', error)
    }
  }

  const handleCalculatePe = async () => {
    try {
      setCalculatebtnClicked(true)

      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      var data = await DataService.handleCalculateConsumptionNorm1(
        plantId,
        year,
        keycloak,
      )

      if (data) {
        const groupedRows = []
        const groups = new Map()
        let groupId = 0

        data.forEach((item) => {
          const groupKey = item.normParameterTypeDisplayName

          if (!groups.has(groupKey)) {
            groups.set(groupKey, [])
            groupedRows.push({
              id: groupId++,
              Particulars: groupKey,
              isGroupHeader: true,
            })
          }
          const formattedItem = {
            ...item,
            idFromApi: item.id,
            NormParametersId: item.materialFkId.toLowerCase(),
            id: groupId++,
            aopRemarks: item?.aopRemarks?.trim() || null,
            originalRemark: item.aopRemarks?.trim() || null,
            UOM: item?.uom,
          }
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Data refreshed successfully!',
            severity: 'success',
          })

          groups.get(groupKey).push(formattedItem)
          groupedRows.push(formattedItem)
        })

        setRows(groupedRows)

        // setLoading(false)
        //IF saveBtn IS SET TO --> FALSE
        saveChanges()
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
      console.error('Error!', error)

      setLoading(false)
    }
  }
  const defaultCustomHeight = { mainBox: '55vh', otherBox: '112%' }

  const handleSave = () => {
    // console.log('Summary:', summary)
    saveSummary()
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
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      units: ['TPH', 'TPD'],
      saveWithRemark: true,
      saveBtn: false,
      showCalculate: true,
      showRefresh: false,
      noColor: false,
      ShowSummary: true,
      // customHeight2: true,
      customHeight: defaultCustomHeight,
    },
    isOldYear,
  )

  const isCellEditable = (params) => {
    console.log(params)
    return params.row.isEditable
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <div>
        {(lowerVertName === 'meg' || lowerVertName === 'pe') && (
          <CustomAccordion
            defaultExpanded
            disableGutters
            onChange={handleAccordionChange}
          >
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                Consumption AOP
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box
                sx={{
                  width: '100%',
                  padding: '0px ',
                  margin: '0px',
                  backgroundColor: '#F2F3F8',
                  borderRadius: 0,
                  borderBottom: 'none',
                }}
              >
                <DataGridTable
                  autoHeight={true}
                  modifiedCells={modifiedCells}
                  columns={productionColumns}
                  isCellEditable={isCellEditable}
                  rows={rows}
                  setRows={setRows}
                  getRowId={(row) => row.id}
                  title='Consumption AOP'
                  paginationOptions={[100, 200, 300]}
                  processRowUpdate={processRowUpdate}
                  rowModesModel={rowModesModel}
                  onRowModesModelChange={onRowModesModelChange}
                  saveChanges={saveChanges}
                  snackbarData={snackbarData}
                  snackbarOpen={snackbarOpen}
                  apiRef={apiRef}
                  // deleteId={deleteId}
                  open1={open1}
                  // setDeleteId={setDeleteId}
                  setOpen1={setOpen1}
                  setSnackbarOpen={setSnackbarOpen}
                  setSnackbarData={setSnackbarData}
                  // handleDeleteClick={handleDeleteClick}
                  handleCalculate={handleCalculate}
                  handleRemarkCellClick={handleRemarkCellClick}
                  fetchData={fetchData}
                  onProcessRowUpdateError={onProcessRowUpdateError}
                  handleUnitChange={handleUnitChange}
                  remarkDialogOpen={remarkDialogOpen}
                  setRemarkDialogOpen={setRemarkDialogOpen}
                  currentRemark={currentRemark}
                  setCurrentRemark={setCurrentRemark}
                  currentRowId={currentRowId}
                  unsavedChangesRef={unsavedChangesRef}
                  permissions={adjustedPermissions}

                  // permissions={{
                  //   showAction: false,
                  //   addButton: false,
                  //   deleteButton: false,
                  //   editButton: false,
                  //   showUnit: false,
                  //   units: ['TPH', 'TPD'],
                  //   saveWithRemark: true,
                  //   saveBtn: false,
                  //   showCalculate: true,
                  //   showRefresh: false,
                  //   noColor: true,
                  //   ShowSummary: true,
                  //   // customHeight2: true,
                  //   customHeight: defaultCustomHeight, // use default height
                  // }}
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        )}
      </div>
      <Typography
        component='div'
        sx={{ fontWeight: 'bold', ml: '5px', mt: '25px' }}
      >
        Consumption AOP Summary
      </Typography>

      <TextField
        label='Summary'
        multiline
        // minRows={isAccordionExpanded ? 4 : 20}
        minRows={4}
        fullWidth
        margin='normal'
        variant='outlined'
        disabled={isOldYear == 1}
        value={summary}
        onChange={(e) => setSummary(e.target.value)}
        sx={{
          '& .MuiInputBase-root': {
            backgroundColor: '#ffffff',
            borderRadius: '8px',
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
            padding: '8px',
          },
          '& label': {
            fontSize: '1rem',
            color: '#666',
            lineHeight: '1.2',
            transform: 'translate(14px, 12px) scale(1)',
          },
          '& .MuiInputLabel-shrink': {
            transform: 'translate(14px, -6px) scale(0.75)',
          },
          '& .MuiOutlinedInput-notchedOutline': {
            borderColor: '#ccc',
          },
          '&:hover .MuiOutlinedInput-notchedOutline': {
            borderColor: '#999',
          },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderColor: '#1976d2',
          },
          '& .MuiInputBase-input': {
            resize: 'vertical',
          },
        }}
      />
      {isOldYear !== 1 && (
        <Button variant='contained' className='btn-save' onClick={handleSave}>
          Save
        </Button>
      )}
    </div>
  )
}

export default NormalOpNormsScreen
