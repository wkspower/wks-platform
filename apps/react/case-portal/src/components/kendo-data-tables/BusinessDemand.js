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
import kendoGetEnhancedColDefs from 'components/data-tables/CommonHeader/kendoBusinessDemColDef'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { validateFields } from 'utils/validationUtils'
import KendoDataTables from './index'
import ProductionvolumeData from './ProductionVoluemData'

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

const BusinessDemand = ({ permissions }) => {
  const [modifiedCells, setModifiedCells] = React.useState({})

  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear, plantID } =
    dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
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

  const [_plantID, set_PlantID] = useState('')
  useEffect(() => {
    if (plantID?.plantId) {
      set_PlantID(plantID?.plantId)
    }
  }, [plantID])

  const fetchData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getBDData(keycloak)

      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: index,
        originalRemark: item.remark,
        inEdit: false,
        Particulars: item.normParameterTypeDisplayName,
        expanded: false,
      }))

      setRows(formattedData)

      setLoading(false)
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [plantID, oldYear, yearChanged, keycloak])

  const handleRemarkCellClick = (dataItem) => {
    // if (!dataItem?.isEditable) return
    setCurrentRemark(dataItem.remark || '')
    setCurrentRowId(dataItem.id)
    setRemarkDialogOpen(true)
  }

  const colDefs = kendoGetEnhancedColDefs({
    headerMap,
  })

  const saveChanges = React.useCallback(async () => {
    setLoading(true)

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
        inEdit: row.inEdit || false,
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
      allAction: permissions?.allAction ?? true,
      units: ['TPH', 'TPD'],
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

      {lowerVertName !== 'cracker' && (
        <>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                Production Volume Data (MT) (This is a reference for entering
                the Business Demand value)
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <ProductionvolumeData
                  permissions={{
                    allAction: true,
                    showAction: false,
                    addButton: false,
                    deleteButton: false,
                    editButton: false,
                    showUnit: true,
                    saveWithRemark: false,
                    showCalculate: false,
                    saveBtn: false,
                    hideSummary: true,
                    hideUploadExcel: true,
                    hideDownloadExcel: true,
                  }}
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </>
      )}

      <Typography component='div' className='grid-title'>
        Business Demand Data
      </Typography>

      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        setRows={setRows}
        columns={colDefs}
        rows={rows || []}
        title='Business Demand'
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
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        handleRemarkCellClick={handleRemarkCellClick}
        deleteRowData={deleteRowData}
        permissions={adjustedPermissions}
        groupBy='Particulars'
      />
    </div>
  )
}
export default BusinessDemand
