import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { MaintenanceDetailsApiService } from 'services/maintenance-details-api-service'
import { getRoleName } from 'services/role-service'
import { useSession } from 'SessionStoreContext'
import KendoDataTables from './index'
import AopTabs from 'components/AopTabs'
import { Box } from '@mui/material'
import { DataService } from 'services/DataService'
//import ElastomerMaintenanceTable from './ElastomerMaintenanceTable'
const MaintenanceTable = () => {
  // State for tabs
  const [tabIndex, setTabIndex] = useState(0)
  const [tabs, setTabs] = useState([])
  const [lineDetails, setLineDetails] = useState([])
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

  const PLANT_NAME_NO_CASE = plantObject?.name?.toUpperCase()
  const SITE_NAME_NO_CASE = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME_NO_CASE = verticalObject?.name?.toUpperCase()
  const IS_PTA = verticalObject?.name?.toLowerCase() === 'pta'
  const EXCEL_EXPORT_TITLE = `${VERTICAL_NAME_NO_CASE}_${SITE_NAME_NO_CASE}_${PLANT_NAME_NO_CASE}`

  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const vertName = verticalChange?.selectedVertical
  const SCREEN_NAME = screenTitle?.title
  const lowerVertName = vertName?.toLowerCase()
  const isPPVerticalDTASite = verticalObject?.name?.toLowerCase() === 'pp' && siteObject?.name?.toLowerCase() === 'dta'
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

  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
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

  useEffect(() => {
    fetchData()
  }, [fetchData, oldYear, yearChanged, PLANT_ID, AOP_YEAR])

  // Fetch line details when component mounts or plantID/year changes
  const fetchLineDetails = async () => {
    if (!PLANT_ID || !AOP_YEAR) return

    try {
      const response = await DataService.getLineDetails(
        keycloak,
        PLANT_ID,
        AOP_YEAR
      )

      if (response?.code != 200) {
        setTabs([])
        return
      }
      if (response && Array.isArray(response?.data)) {
        setLineDetails(response.data)
        // Update tabs based on the response
        const lineTabs = response?.data.map(line => line.displayName)
        setTabs(lineTabs)
      }
    } catch (err) {
      console.error('Error fetching line details:', err)
      // Fallback to default tabs if API fails
      setTabs([])
    }
  }

  useEffect(() => {
    if(isPPVerticalDTASite){
      fetchLineDetails()
    }
  }, [PLANT_ID, keycloak, yearChanged])

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
  // Base function to generate column set
  const generateColumnsPEPP = (nameWidthT) => [
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

    {
      field: 'allMonthsTotal',
      title: 'Total Hrs',
      type: 'number',
      format: '{0:n2}',
      editable: false,
    },
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
  const productionColumnsPE = generateColumnsPEPP(150)
  const productionColumnsPP = generateColumnsPEPP(220)
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
    case 'pet':
      basecols = productionColumnsPP
      break
    default:
      basecols = productionColumnsNonMEG
      break
  }

  if (IS_PTA) {
    basecols = [
      ...basecols,
      {
        field: 'allMonthsTotal',
        title: 'Total',
        type: 'number',
        format: '{0:n2}',
        editable: false,
      },
    ]
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
          ExcelName: `${EXCEL_EXPORT_TITLE}_${SCREEN_NAME}`,
          showRefresh: false,
          showTitleNameBusiness: true,
          titleName: SCREEN_NAME,
        },
        isOldYear,
      ),
    [isOldYear, AOP_YEAR, PLANT_ID, SCREEN_NAME],
  )
  // if (lowerVertName == 'elastomer') {
  //   return <ElastomerMaintenanceTable />
  // }

  return (
    <>
      {/* LINE1-LINE6 Tabs - Only for PP VERTICAL | DTA SITE */}
      {isPPVerticalDTASite && (
        <Box display='flex' alignItems='center' sx={{ mb: 1, mt: 1 }}>
          <AopTabs
            tabIndex={tabIndex}
            setTabIndex={setTabIndex}
            tabs={tabs}
          />
        </Box>
      )}
      <div>
        <Backdrop
          open={loading}
          sx={{ color: '#fff', zIndex: (t) => t.zIndex.drawer + 1 }}
        >
          <CircularProgress color='inherit' />
        </Backdrop>

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
      </div>
    </>
  )
}
export default MaintenanceTable
