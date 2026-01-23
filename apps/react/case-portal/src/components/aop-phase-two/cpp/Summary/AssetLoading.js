import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { InputApiService } from 'components/aop-phase-two/services/cpp/inputApiService'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { UtilityPlantApiServiceV2 } from 'components/aop-phase-two/services/cpp/utilityPlantApiServiceV2'

const AssetLoading = () => {
  const keycloak = useSession()
  // State management
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
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
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const valueFormat = ValueFormatterPhaseTwo()

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Static dummy data
  const staticData = [
    {
      id: 1,
      uom: 'MW',
      utility: 'Easy Proc',
      plant: 'NMD - Easy Proc',
      april: 25.0,
      may: 25.0,
      june: 33.0,
      july: 33.0,
      aug: 33.0,
      sept: 33.0,
      oct: 33.0,
      nov: 33.0,
      dec: 33.0,
      jan: 33.0,
      feb: 33.0,
      mar: 33.0,
      remarks: '',
    },
    {
      id: 2,
      uom: 'MW',
      utility: 'POWERGEN',
      plant: 'NMD - Power Plant 1',
      april: null,
      may: 12.81,
      june: 7.63,
      july: 8.14,
      aug: 9.45,
      sept: 10.79,
      oct: 9.89,
      nov: 7.76,
      dec: 9.94,
      jan: 7.94,
      feb: 10.63,
      mar: 11.03,
      remarks: '',
    },
    {
      id: 3,
      uom: 'MW',
      utility: 'POWERGEN',
      plant: 'NMD - Power Plant 2',
      april: 11.2,
      may: 11.6,
      june: null,
      july: null,
      aug: 6.93,
      sept: null,
      oct: null,
      nov: null,
      dec: null,
      jan: null,
      feb: null,
      mar: null,
      remarks: '',
    },
    {
      id: 4,
      uom: 'MW',
      utility: 'POWERGEN',
      plant: 'NMD - Power Plant 3',
      april: 12.57,
      may: null,
      june: 6.41,
      july: null,
      aug: 7.9,
      sept: 9.19,
      oct: 8.29,
      nov: 6.77,
      dec: 8.39,
      jan: 6.39,
      feb: 9.04,
      mar: 9.44,
      remarks: '',
    },
    {
      id: 5,
      uom: 'MW',
      utility: 'POWERGEN',
      plant: 'NMD - STG Power Plant',
      april: 14.21,
      may: 14.21,
      june: 12.24,
      july: 12.3,
      aug: 11.92,
      sept: 11.84,
      oct: 11.92,
      nov: 12.24,
      dec: 11.92,
      jan: 11.92,
      feb: 11.67,
      mar: 11.62,
      remarks: '',
    },
  ]

  // Column definitions
  const columns = [
    { field: 'id', title: 'ID', hidden: true },

    {
      field: 'plant',
      title: 'Plant',
      width: 200,
      minWidth: 180,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utility',
      title: 'Utility',
      width: 150,
      minWidth: 120,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'uom',
      title: 'UOM',
      widthT: 60,
      minWidth: 80,
      type: 'text',
      editable: false,
      locked: true,
    },

    {
      field: 'april',
      title: headerMap[4],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'may',
      title: headerMap[5],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'june',
      title: headerMap[6],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'july',
      title: headerMap[7],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'aug',
      title: headerMap[8],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'sept',
      title: headerMap[9],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'oct',
      title: headerMap[10],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'nov',
      title: headerMap[11],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'dec',
      title: headerMap[12],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'jan',
      title: headerMap[1],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'feb',
      title: headerMap[2],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'mar',
      title: headerMap[3],
      editable: false,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    // {
    //   field: 'remarks',
    //   title: 'Remarks',
    //   width: 250,
    //   type: 'textarea',
    //   editable: false,
    //   minWidth: 250,
    // },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      // Load static data for now
      setRows(staticData)
      setOriginalRows(staticData)
      // fetchAssetLoadingData(keycloak, PLANT_ID, AOP_YEAR)
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchAssetLoadingData = async (keycloak, PLANT_ID, AOP_YEAR) => {
    setLoading(true)
    try {
      // const res = await UtilityPlantApiServiceV2.getImportPowerData(
      //   keycloak,
      //   PLANT_ID,
      //   AOP_YEAR,
      // )
      if (res?.length === 0) {
        setRows([])
        setOriginalRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      let tempRes = res.map((item, index) => {
        const transformed = {
          id: item?.id || index + 1,
          ...item,
        }
        return transformed
      })
      console.log('tempRes', tempRes)
      setRows(tempRes)
      setOriginalRows(tempRes)
    } catch (error) {
      console.error('Error fetching import consumption data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  // Permissions (adjust as needed)
  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: false,
    allAction: true,
    showImport: false,
    showExport: false,
    ExcelName: `Asset Loading - ${AOP_YEAR}`,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: screenTitle?.title,
  }

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      await InputApiService.exportImportPowerExcel(keycloak, PLANT_ID, AOP_YEAR)
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Import Power data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Asset Loading'
        permissions={permissions}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        handleExport={handleExport}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        //groupBy="plant"
      />
    </Box>
  )
}

export default AssetLoading
