import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import ASDataGrid from './ASDataGrid'
import getEnhancedColDefs from './CommonHeader/index'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
import SimpleDataTable from 'components/data-tables-views/SimpleDataTable'
import { Box } from '@mui/material'

import Accordion from '@mui/material/Accordion'
import AccordionSummary from '@mui/material/AccordionSummary'
import AccordionDetails from '@mui/material/AccordionDetails'
import Typography from '@mui/material/Typography'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'

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

const BusinessDemand = ({ permissions }) => {
  const [rowModesModel, setRowModesModel] = useState({})
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [rows2, setRows2] = useState()

  // console.log('yearyear', year)

  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const fetchData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getBDData(keycloak)

      if (lowerVertName !== 'pe') {
        data = data.sort((a, b) =>
          b.normParameterTypeDisplayName.localeCompare(
            a.normParameterTypeDisplayName,
          ),
        )
      }

      // console.log(sortedData)

      const groupedRows = []
      const groups = new Map()
      let groupId = 0

      // console.log('lowerVertName', lowerVertName)

      data.forEach((item) => {
        const formattedItem = {
          ...item,
          idFromApi: item.id,
          id: groupId++,
          originalRemark: item.remark,
        }

        // if (lowerVertName !== 'pe') {
        const groupKey = item.normParameterTypeDisplayName

        if (!groups.has(groupKey)) {
          groups.set(groupKey, [])
          groupedRows.push({
            id: groupId++,
            Particulars: groupKey,
            isGroupHeader: true,
          })
        }

        groups.get(groupKey).push(formattedItem)
        // }

        groupedRows.push(formattedItem)
      })

      setRows(groupedRows)
      setLoading(false) // Hide loading
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
      setLoading(false) // Hide loading
    }
  }

  // console.log(verticalChange)
  useEffect(() => {
    // console.log('oldYear', oldYear?.oldYear)
    const getAllProducts = async () => {
      try {
        var data = []
        if (lowerVertName == 'meg')
          data = await DataService.getAllProductsAll(
            keycloak,
            'BusinessDemandMEG',
          )
        else {
          data = await DataService.getAllProductsAll(keycloak, 'Production')
        }
        var productList = []
        if (lowerVertName === 'meg') {
          productList = data
            .filter((product) =>
              ['EO', 'EOE', 'MEG', 'CO2'].includes(product.displayName),
            )
            .map((product) => ({
              id: product.id,
              displayName: product.displayName,
            }))
        } else {
          productList = data.map((product) => ({
            id: product.id,
            displayName: product.displayName,
          }))
        }

        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }

    fetchData()
    // fetchData2()
    getAllProducts()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  // useEffect(()=>{
  //   console.log('this is test for api call ')
  // })

  const handleRemarkCellClick = (row) => {
    // console.log(row, newRow)
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const isCellEditable = (params) => {
    return !params.row.Particulars
  }

  const colDefs = getEnhancedColDefs({
    allProducts,
    headerMap,
    handleRemarkCellClick,
  })

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    // Keep track of original values before editing
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    return newRow
  }, [])

  const saveChanges = React.useCallback(async () => {
    setLoading(true)
    const rowsInEditMode = Object.keys(rowModesModel).filter(
      (id) => rowModesModel[id]?.mode === 'edit',
    )

    rowsInEditMode.forEach((id) => {
      apiRef.current.stopRowEditMode({ id })
    })

    setTimeout(() => {
      try {
        var data = Object.values(unsavedChangesRef.current.unsavedRows)
        if (data.length == 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          setLoading(false)
          return
        }

        const requiredFields = ['normParameterId', 'remark']

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
        saveBusinessDemandData(data)
      } catch (error) {
        console.log('Error saving changes:', error)
      }
    }, 400)
  }, [apiRef, rowModesModel])

  const saveBusinessDemandData = async (newRows) => {
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      let siteId = ''
      const storedSite = localStorage.getItem('selectedSiteId')
      if (storedSite) {
        const parsedSite = JSON.parse(storedSite)
        siteId = parsedSite.id
      }

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
        remark: row.remark || null,
        avgTph: row.avgTph || null,
        year: localStorage.getItem('year'),
        plantId: plantId,
        siteFKId: siteId,
        verticalFKId: verticalId,
        normParameterId: row.normParameterId,
        id: row.idFromApi || null,
      }))

      // if (businessData.length > 0) {
      const response = await DataService.saveBusinessDemandData(
        plantId,
        businessData,
        keycloak,
      )

      // console.log(response)

      // if (response.status == 200) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Business Demand data Saved Successfully!',
        severity: 'success',
      })
      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
      fetchData()
      return response
    } catch (error) {
      console.error('Error saving Business Demand data!', error)
    } finally {
      // fetchData()
    }
  }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

  const deleteRowData = async (paramsForDelete) => {
    try {
      const { idFromApi, id } = paramsForDelete.row
      const deleteId = id

      if (!idFromApi) {
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
      }

      if (idFromApi) {
        await DataService.deleteBusinessDemandData(idFromApi, keycloak)
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        fetchData()
      }
    } catch (error) {
      console.error('Error deleting Record!', error)
    }
  }

  const defaultCustomHeight = { mainBox: '50vh', otherBox: '112%' }

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
    }
  }
  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? false,
      addButton: permissions?.addButton ?? false,
      deleteButton: permissions?.deleteButton ?? false,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? false,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      saveBtn: permissions?.saveBtn ?? true,
      units: ['TPH', 'TPD'],
      customHeight: permissions?.customHeight || defaultCustomHeight,
    },
    isOldYear,
  )

  // permissions={adjustedPermissions}

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Typography component='div' className='grid-title'>
        Business Demand Data
      </Typography>

      <ASDataGrid
        setRows={setRows}
        columns={colDefs}
        rows={rows || []}
        isCellEditable={isCellEditable}
        title='Business Demand'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        rowModesModel={rowModesModel}
        onRowModesModelChange={onRowModesModelChange}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        apiRef={apiRef}
        deleteId={deleteId}
        setDeleteId={setDeleteId}
        setOpen1={setOpen1}
        open1={open1}
        fetchData={fetchData}
        onProcessRowUpdateError={onProcessRowUpdateError}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        unsavedChangesRef={unsavedChangesRef}
        handleRemarkCellClick={handleRemarkCellClick}
        deleteRowData={deleteRowData}
        permissions={adjustedPermissions}
        // permissions={{
        //   showAction: permissions?.showAction ?? false,
        //   addButton: permissions?.addButton ?? false,
        //   deleteButton: permissions?.deleteButton ?? false,
        //   editButton: permissions?.editButton ?? false,
        //   showUnit: permissions?.showUnit ?? false,
        //   saveWithRemark: permissions?.saveWithRemark ?? true,
        //   saveBtn: permissions?.saveBtn ?? true,
        //   units: ['TPH', 'TPD'],
        //   customHeight: permissions?.customHeight || defaultCustomHeight,
        // }}
      />

      <div>
        {(lowerVertName === 'meg' || lowerVertName === 'pe') && (
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                Production Volume Data (MT)
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <SimpleDataTable />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        )}
      </div>
    </div>
  )
}

export default BusinessDemand
