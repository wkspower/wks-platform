import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { validateFields } from 'utils/validationUtils'
import KendoDataTables from './index'
import { PackagingConsumablesApiService } from 'services/packaging-consumables-api-service'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import { getRoleName } from 'services/role-service'

const PackagingConsumables = (permissions) => {
  const [gradeId, setGradeId] = useState(null)
  const [gradeName, setGradeName] = useState('')
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [loading, setLoading] = useState(false)
  const menu = useSelector((state) => state.dataGridStore)
  const [shutdownMonths, setShutdownMonths] = useState([])
  const { yearChanged, oldYear, plantID } = menu
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const [open1, setOpen1] = useState(false)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    screenTitle,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [selectedYear, setSelectedYear] = useState('Budget') // Default to Budget
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const PLANT_NAME = plantObject?.name
  const SITE_NAME = siteObject?.name
  const VERTICAL_NAME = verticalObject?.name
  const SCREEN_NAME = screenTitle?.title
  const headerMap = generateHeaderNames(AOP_YEAR)
  const keycloak = useSession()
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  // Calculate year based on selected year dropdown
  const getYearForAPI = () => {
    if (!AOP_YEAR) return null
    
    if (selectedYear === 'Actual') {
      // For Actual, use previous year
      const yearParts = AOP_YEAR.split('-')
      const startYear = parseInt(yearParts[0])
      const endYear = parseInt(yearParts[1])
      return `${startYear - 1}-${endYear - 1}`
    }
    // For Budget, use current year
    return AOP_YEAR
  }

  const API_YEAR = getYearForAPI()

  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCells)

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
        setLoading(false)
        return
      }

      savepackagingdata(data)
    } catch (error) {
      setLoading(false)
    }
  }, [apiRef, modifiedCells])

  const isCellEditable = (params) => {
    return params.row.isEditable
  }
  const valueFormat = ValueFormatterConsumption()

  const monthColumns = [
    { 
      field: 'apr',
      title: headerMap[4],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
    { 
      field: 'may',
      title: headerMap[5],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
    { 
      field: 'jun',
      title: headerMap[6],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
    { 
      field: 'jul',
      title: headerMap[7],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
    { 
      field: 'aug',
      title: headerMap[8],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
    { 
      field: 'sep',
      title: headerMap[9],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
    { 
      field: 'oct',
      title: headerMap[10],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
    { 
      field: 'nov',
      title: headerMap[11],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
    { 
      field: 'dec',
      title: headerMap[12],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
    { 
      field: 'jan',
      title: headerMap[1],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
    { 
      field: 'feb',
      title: headerMap[2],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
    { 
      field: 'mar',
      title: headerMap[3],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number' 
    },
  ];

  const colDefs = [
    { 
      field: 'NormParameter_FK_Id',
      title: 'Particulars',
      widthT: 160,
      hidden: true,
    },
    { 
      field: 'productName',
      title: 'Particulars',
      widthT: 120,
    },
    { 
      field: 'UOM',
      title: 'UOM',
      widthT: 60,
      editable: false,
    },
    ...monthColumns,
    { 
      field: 'remarks',
      title: 'Remark',
      widthT: 100,
      editable: true,
    },
    { 
      field: 'idFromApi',
      title: 'idFromApi',
      hidden: true,
    },
  ]

  const handleRemarkCellClick = (row) => {
    if (!row?.isEditable || READ_ONLY) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const savepackagingdata = async (rows) => {
    setLoading(true)
    
    try {
      const payload = rows.map((row) => ({
        apr: row.apr || null,
        may: row.may || null,
        jun: row.jun || null,
        jul: row.jul || null,
        aug: row.aug || null,
        sep: row.sep || null,
        oct: row.oct || null,
        nov: row.nov || null,
        dec: row.dec || null,
        jan: row.jan || null,
        feb: row.feb || null,
        mar: row.mar || null,
        UOM: row.UOM,
        remarks: row.remarks,
        auditYear: API_YEAR, 
        normParameterFKId: row.normParameterFKId,
        id: null,
      }))
      
      if (payload.length > 0) {
        const response = await PackagingConsumablesApiService.savePackagingData(
          PLANT_ID,
          payload,
          keycloak,
          API_YEAR,
        )
        setSnackbarOpen(true)
        setSnackbarData({
          message: `Saved Successfully!`,
          severity: 'success',
        })
        setModifiedCells({})
        setLoading(false)
        return response
      } else {
        setSnackbarOpen(true)
        setLoading(false)
        setSnackbarData({
          message: `Data not saved!`,
          severity: 'error',
        })
      }
    } catch (error) {
      console.error(`Error saving Data`, error)
      setLoading(false)
    } finally {
      fetchData()
      setLoading(false)
    }
  }

  const fetchData = async () => {
    if (!PLANT_ID || !AOP_YEAR || !API_YEAR) return

    try {
      setLoading(true)
      setRows([])
      let data = []

      if (lowerVertName === 'pe' || lowerVertName === 'pp') {
        data = await PackagingConsumablesApiService.getPackagingData(
          keycloak,
          PLANT_ID,
          API_YEAR,
        )
      } 
      
      if (data?.code != 200) {
        setRows([])
        setLoading(false)
        return
      }
      
      let formattedData = []
      formattedData = data?.data?.configurationDTOList?.map((item, index) => {
        const baseItem = {
          ...item,
          idFromApi: item.normParameterFKId,
          id: index,
          remarks: item?.remarks?.trim() || null,
          originalRemark: item?.remarks?.trim(),
          materialFkId: item?.normParameterFKId?.toLowerCase(),
          Particulars: item.productName || 'Particulars',
          isEditable: item.isEditable,
        }
        return baseItem
      })

      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  // Handle year change from dropdown
  const handleYearChange = (year) => {
    setSelectedYear(year)
  }

  useEffect(() => {
    fetchData()
  }, [yearChanged, PLANT_ID, AOP_YEAR, selectedYear]) // selectedYear added as dependency

  // Reset to Budget when plant or year changes
  useEffect(() => {
    setSelectedYear('Budget');
  }, [AOP_YEAR, PLANT_ID]);

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      downloadExcelBtn: false,
      uploadExcelBtn: false,
      editButton: false,
      showPackagingYear: false, // Disable year dropdown for old year
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      allAction: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      addButton: false,
      deleteButton: false,
      showPackagingYear: true, // Enable packaging year dropdown
      saveBtn: true,
      customHeight: permissions?.customHeight,
      showAction: false,
      editButton: false,
      saveWithRemark: true,
      showCheckbox: false,
      marginBottom: true,
      allAction: true,
      downloadExcelBtn: false,
      downloadExcelBtnFromUI: true,
      showNoteWhileDeleting: false,
      showTitleNameBusiness: true,
      titleName: `${SCREEN_NAME}`,
      uploadExcelBtn: false,
      packagingYears: ['Budget', 'Actual'], // Year dropdown options
      ExcelName: `${selectedYear}_${PLANT_NAME}_${SCREEN_NAME}`,
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
      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        isCellEditable={isCellEditable}
        title='Packaging Consumables Norms'
        columns={colDefs}
        setRows={setRows}
        rows={rows}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        open1={open1}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        handleRemarkCellClick={handleRemarkCellClick}
        handleYearChange={handleYearChange}
        permissions={adjustedPermissions}
        plantID={plantID}
        selectedPackagingYear={selectedYear}
      />
    </div>
  )
}

export default PackagingConsumables