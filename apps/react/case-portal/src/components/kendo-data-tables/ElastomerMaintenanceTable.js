import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { MaintenanceDetailsApiService } from 'services/maintenance-details-api-service'
import { getRoleName } from 'services/role-service'
import { useSession } from 'SessionStoreContext'
import KendoDataTables from './index'
import { validateFields } from 'utils/validationUtils'
import { Box, Button, Tab, Tabs, Typography } from '@mui/material'

const ElastomerMaintenanceTable = () => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const keycloak = useSession()

  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)
  const vertName = verticalChange?.selectedVertical
  const SCREEN_NAME = screenTitle?.title
  const lowerVertName = vertName?.toLowerCase()
  const dataConfig = useMemo(
    () => ({
      serviceFn: (keycloak, PLANT_ID, AOP_YEAR) =>
        MaintenanceDetailsApiService.getMaintenanceData(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        ),
    }),
    [PLANT_ID, AOP_YEAR, lowerVertName],
  )

  const headerMap = generateHeaderNames(AOP_YEAR)

  const [rows, setRows] = useState([])
  const [slowdownRows, setSlowdownRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [deleteId, setDeleteId] = useState(null)
  const [open1, setOpen1] = useState(false)

  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [currentRowId1, setCurrentRowId1] = useState(null)
  const [tabIndex, setTabIndex] = useState(0)
  const defaultTabs = ['Net Production Hours', 'Slowdown History Config']

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId1(row.id)
    setRemarkDialogOpen(true)
  }

  const fetchData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setRows([])
    setLoading(true)
    try {
      const resp = await dataConfig.serviceFn(keycloak, PLANT_ID, AOP_YEAR)
      const raw = resp
      const monthFields = [
        'April',
        'May',
        'June',
        'July',
        'Aug',
        'Sep',
        'Oct',
        'Nov',
        'Dec',
        'Jan',
        'Feb',
        'Mar',
      ]

      const formatted = (raw || []).map((item, idx) => {
        const allMonthsTotal = monthFields.reduce((sum, month) => {
          const value = parseFloat(item[month]) || 0
          return sum + value
        }, 0)

        return {
          ...item,
          idFromApi: item.id,
          id: idx,
          isEditable: false,
          originalRemark: item.remarks,
          allMonthsTotal,
        }
      })

      setRows(formatted)
    } catch (err) {
      console.error('Error fetching data:', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, AOP_YEAR, keycloak, dataConfig])

  const slowdownFetchData = useCallback(async () => {
  if (!PLANT_ID || !AOP_YEAR) return
  setSlowdownRows([]) 
  setLoading(true)
  try {//
    const resp = await MaintenanceDetailsApiService.getSlowdownConfig(keycloak, PLANT_ID, AOP_YEAR)
    // Add isEditable: true to each row
    const formatted = (resp.data || []).map((item, idx) => ({
      ...item,
      monthly: item.month,
      originalRemark: item.remark,
      remarks: item.remark,  
      year: item.year,      
      isEditable: true,
      id: idx,
      idFromApi: item.id,
    }))
    setSlowdownRows(formatted) 
  } catch (err) {
    console.error('Error fetching data:', err)
    setSlowdownRows([]) 
  } finally {
    setLoading(false)
  }
}, [PLANT_ID, AOP_YEAR, keycloak])

useEffect(() => {
  if (tabIndex === 1) {
    slowdownFetchData();
  }
}, [tabIndex, slowdownFetchData]);

  useEffect(() => {
    fetchData()
  }, [fetchData, oldYear, yearChanged, PLANT_ID, AOP_YEAR])

  // Helper to generate monthly fields
  const getMonthlyColumns = () => {
    const months = [
      { field: 'April', index: 4 },
      { field: 'May', index: 5 },
      { field: 'June', index: 6 },
      { field: 'July', index: 7 },
      { field: 'Aug', index: 8 },
      { field: 'Sep', index: 9 },
      { field: 'Oct', index: 10 },
      { field: 'Nov', index: 11 },
      { field: 'Dec', index: 12 },
      { field: 'Jan', index: 1 },
      { field: 'Feb', index: 2 },
      { field: 'Mar', index: 3 },
    ]

    return months.map(({ field, index }) => ({
      field,
      title: headerMap[index],
      type: 'number',
      format: '{0:n2}',
      editable: false,
      align: 'right',
      headerAlign: 'left',
    }))
  }

  // Shared editable field
  const isEditableField = {
    field: 'isEditable',
    title: 'isEditable',
    hidden: true,
  }

  // Base function to generate column set
  const generateColumns = (nameWidthT) => [
    {
      field: 'Name',
      title: 'Description',
      align: 'left',
      headerAlign: 'left',
      widthT: nameWidthT,
      editable: false,
      isEditable: false,
    },
    ...getMonthlyColumns(),
    isEditableField,
  ]

  const generateColumnsELASTOMER = (nameWidthT) => [
    {
      field: 'Name',
      title: 'Description',
      align: 'left',
      headerAlign: 'left',
      widthT: nameWidthT,
      editable: false,
    },
    ...getMonthlyColumns(),
    isEditableField,
    {
      field: 'allMonthsTotal',
      title: 'Total',
      type: 'number',
      format: '{0:00}',
      editable: false,
    },
  ]

  // Column sets
  const productionColumnsMEG = generateColumns(390)
  const productionColumnsPE = generateColumns(150)
  const productionColumnsPP = generateColumns(220)
  const productionColumnsNonMEG = generateColumns(200)
  const productionColumnsELASTOMER = generateColumnsELASTOMER(200)

  // Column selection
  let basecols

  switch (lowerVertName) {
    case 'meg':
      basecols = productionColumnsMEG
      break
    case 'pe':
      basecols = productionColumnsPE
      break
    case 'pp':
      basecols = productionColumnsPP
      break
    case 'elastomer':
      basecols = productionColumnsELASTOMER
      break
    default:
      basecols = productionColumnsNonMEG
      break
  }
   const slowdownColumns = [
  {
    field: 'monthly',
    title: 'Month',
    type: 'monthDropdown',
    editable: true,
    width: 200,
  },
  {
    field: 'year',
    title: 'Year',
    type: 'yeardropdown',
    editable: true,
    width: 200,
  },
  
  {
    field: 'remarks',
    title: 'Remark',
    editable: true,
    width: 200,
  },
  
  
]
const saveChanges = async () => {
    try {
      const data = Object.values(modifiedCells)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
     const requiredFields = ['remarks']
    const validationMessage = validateFields(data, requiredFields)
    if (validationMessage) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationMessage,
        severity: 'error',
      })
      return
    }

      const dataList = data.map((row) => {
        const obj = {
          month: row.monthly,
          year: row.year,
          aopYear: AOP_YEAR, 
          remark: row.remarks,
        }

        if (row.idFromApi) {
          obj.id = row.idFromApi
        }

        return obj
      })

      const res = await MaintenanceDetailsApiService.saveSlowdownConfig(
        PLANT_ID,
        AOP_YEAR,
        dataList,
        keycloak,
      )

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        slowdownFetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Failed!',
          severity: 'error',
        })
      }
    } catch (err) {
      console.error('Error while save', err)
      setSnackbarOpen(true)
      setSnackbarData({ message: err.message, severity: 'error' })
    } finally {
      setSnackbarOpen(true)
    }
  }

const handleDeleteSlowdownConfig = async (row) => {
    if (!row.idFromApi) {
      setSlowdownRows((prev) => prev.filter((r) => r.id !== row.id))
      return
    }
    setLoading(true)
    try {
      const response = await MaintenanceDetailsApiService.deleteSlowdownConfig(
        row.idFromApi,
        keycloak,
        PLANT_ID,
        AOP_YEAR, 
      )
      if (response && response?.code === 200) {
        setSlowdownRows((prev) => prev.filter((r) => r.id !== row.id))
        setSnackbarData({
          message: 'Deleted Successfully!',
          severity: 'success',
        })
        setSnackbarOpen(true)
        slowdownFetchData()
      } else {
        throw new Error('Unexpected response from server')
      }
    } catch (error) {
      console.error('Delete error:', error)
      setSnackbarData({ message: 'Error deleting record!', severity: 'error' })
      setSnackbarOpen(true)
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
      allAction: false,
    }
  }

  const adjustedPermissions = useMemo(
    () =>
      getAdjustedPermissions(
        {
          showAction: false,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: false,
          saveWithRemark: false,
          saveBtn: dataConfig.isCracker,
          allAction: true,
          downloadExcelBtnFromUI: true,
          ExcelName: SCREEN_NAME,
          showRefresh: false,
          showTitleNameBusiness: true,
          titleName: SCREEN_NAME,
        },
        isOldYear,
      ),
    [isOldYear, AOP_YEAR, PLANT_ID, SCREEN_NAME],
  )
  //adjustedPermissionsslowdown
  const adjustedPermissionsslowdown = useMemo(
    () =>
      getAdjustedPermissions(
        {
          showAction: false,
          addButton: true,
          deleteButton: true,
          editButton: false,
          showUnit: false,
          saveWithRemark: false,
          saveBtn: true,
          allAction: true,
          downloadExcelBtnFromUI: true,
          ExcelName: SCREEN_NAME,
          showRefresh: false,
          showTitleNameBusiness: true,
          titleName:'Slowdown History Config',
        },
        isOldYear,
      ),
    [isOldYear, AOP_YEAR, PLANT_ID, SCREEN_NAME],
  )
  
  return (
    <>
      <div>
        <Backdrop
          open={loading}
          sx={{ color: '#fff', zIndex: (t) => t.zIndex.drawer + 1 }}
        >
          <CircularProgress color='inherit' />
        </Backdrop>
       {defaultTabs?.length > 1 && (
               <Tabs
                 value={tabIndex}
                 onChange={(e, newIndex) => setTabIndex(newIndex)}
                 variant='scrollable'
                 scrollButtons='auto'
                 sx={{
                   borderBottom: '0px solid #ccc',
                   '.MuiTabs-indicator': { display: 'none' },
                   margin: '0px 0px 10px 0px',
                   minHeight: '28px',
                 }}
                 textColor='primary'
                 indicatorColor='primary'
               >
                 {defaultTabs.map((label, idx) => (
                   <Tab
                     key={idx}
                     label={label}
                     sx={{
                       border: '1px solid #ADD8E6',
                       borderBottom: '1px solid #ADD8E6',
                       fontSize: '0.75rem',
                       padding: '9px',
                       minHeight: '12px',
                     }}
                   />
                 ))}
               </Tabs>
             )}
         {tabIndex === 0 && (    
        <KendoDataTables
          columns={basecols}
          rows={rows}
          setRows={setRows}
          fetchData={fetchData}
          deleteId={deleteId}
          setDeleteId={setDeleteId}
          open1={open1}
          setOpen1={setOpen1}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          permissions={adjustedPermissions}
          currentRowId={currentRowId}
        />
         )}
         {tabIndex === 1 && (
        <KendoDataTables
          columns={slowdownColumns}
          rows={slowdownRows}
          setRows={setSlowdownRows}
          fetchData={slowdownFetchData} 
          deleteRowData={handleDeleteSlowdownConfig}
          saveChanges={saveChanges}
          deleteId={deleteId}
          setDeleteId={setDeleteId}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          open1={open1}
          setOpen1={setOpen1}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId1}
          handleRemarkCellClick={handleRemarkCellClick}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          permissions={adjustedPermissionsslowdown}
        />
         )}
      </div>
    </>
  )
}
export default ElastomerMaintenanceTable
