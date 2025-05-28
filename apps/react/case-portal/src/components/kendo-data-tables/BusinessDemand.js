import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { Box } from '@mui/material'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { styled } from '@mui/material/styles'
import Typography from '@mui/material/Typography'
import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { validateFields } from 'utils/validationUtils'
import getEnhancedColDefs from '../data-tables/CommonHeader/index'
import ProductionvolumeData from './ProductionVoluemData'
import KendoDataTables from './index'
import kendoGetEnhancedColDefs from 'components/data-tables/CommonHeader/kendoBusinessDemColDef'
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

const KendoBusinessDemand = ({ permissions }) => {
  const [modifiedCells, setModifiedCells] = React.useState({})

  const [rowModesModel, setRowModesModel] = useState({})
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  // const [updatedRows, setUpdatedRows] = useState()
  // const [rows2, setRows2] = useState()
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
          inEdit: false,
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

          // .filter((product) =>
          //   ['EO', 'EOE', 'MEG', 'CO2'].includes(product.displayName),
          // )
          // .map((product) => ({
          //   id: product.id,
          //   displayName: product.displayName,
          //   inEdit: false,
          // }))
        } else {
          productList = data.map((product) => ({
            id: product.id,
            displayName: product.displayName,
            // inEdit: false,
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

    getAllProducts()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  // useEffect(()=>{
  //   console.log('this is test for api call ')
  // })

  const handleRemarkCellClick = (dataItem) => {
    // if (!dataItem?.isEditable) return
    console.log('hiiiiiiii')
    setCurrentRemark(dataItem.remark || '')
    setCurrentRowId(dataItem.id)
    setRemarkDialogOpen(true)
  }

  const isCellEditable = (params) => {
    return params.row.isEditable
  }

  const colDefs = kendoGetEnhancedColDefs({
    allProducts,
    headerMap,
    handleRemarkCellClick,
  })
  // const colDefs = React.useMemo(() => {
  //   const defs = getEnhancedColDefs({
  //     allProducts,
  //     headerMap,
  //     handleRemarkCellClick,
  //   })
  //   console.log(' colDefs →', defs)
  //   return defs
  // }, [allProducts, headerMap, handleRemarkCellClick])

  const saveChanges = React.useCallback(async () => {
    // setLoading(true)
    // const rowsInEditMode = Object.keys(rowModesModel).filter(
    //   (id) => rowModesModel[id]?.mode === 'edit',
    // )

    // rowsInEditMode.forEach((id) => {
    //   apiRef.current.stopRowEditMode({ id })
    // })

    setTimeout(() => {
      try {
        if (Object.keys(modifiedCells).length === 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          setLoading(false)
          return
        }
        // console.log('modifiedCells', modifiedCells)
        let newRows = modifiedCells.filter((row) => row.isGroupHeader !== true)
        console.log(newRows)
        var data = Object.values(newRows)
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

        // const requiredFields = ['normParameterId', 'remark']

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
    }, 400)
  }, [apiRef, rowModesModel, modifiedCells])

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

      const response = await DataService.saveBusinessDemandData(
        plantId,
        businessData,
        keycloak,
      )

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Business Demand data Saved Successfully!',
        severity: 'success',
      })
      setModifiedCells({})

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
      // showStepper:false,
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
  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <div>
        <CustomAccordion defaultExpanded disableGutters>
          <CustomAccordionSummary
            aria-controls='meg-grid-content'
            id='meg-grid-header'
          >
            <Typography component='span' className='grid-title'>
              Production Volume Data (MT) (For reference to enter Business
              Demand Value )
            </Typography>
          </CustomAccordionSummary>
          <CustomAccordionDetails>
            <Box sx={{ width: '100%', margin: 0 }}>
              <ProductionvolumeData
                permissions={{
                  allAction: false,
                  showAction: false,
                  addButton: false,
                  deleteButton: false,
                  editButton: false,
                  showUnit: false,
                  saveWithRemark: false,
                  showCalculate: false,
                  saveBtn: false,
                  hideSummary: true,
                }}
              />
            </Box>
          </CustomAccordionDetails>
        </CustomAccordion>
      </div>

      <Typography component='div' className='grid-title'>
        Business Demand Data
      </Typography>

      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        setRows={setRows}
        // updatedRows={updatedRows}
        // setUpdatedRows={setUpdatedRows}
        columns={colDefs}
        rows={rows || []}
        isCellEditable={isCellEditable}
        title='Business Demand'
        // processRowUpdate={processRowUpdate}

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
        handleRemarkCellClick={handleRemarkCellClick}
        deleteRowData={deleteRowData}
        permissions={adjustedPermissions}
      />
    </div>
  )
}

export default KendoBusinessDemand
