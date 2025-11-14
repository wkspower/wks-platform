import React, { useEffect, useState, useRef } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { BusinessDemandDataApiService } from 'services/business-demand-data-api-service'
import KendoDataTables from './index'
import { validateFields } from 'utils/validationUtils'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { DataService } from 'services/DataService'
import PropaneDropdown from './Utilities-Kendo/PropaneDropdown'
const getMonthYearTitles = (selectedYear) => {
  const yearShort = String(selectedYear).slice(-2)
  const nextYearShort = String(Number(selectedYear) + 1).slice(-2)

  const monthNames = [
    'Apr',
    'May',
    'Jun',
    'Jul',
    'Aug',
    'Sep',
    'Oct',
    'Nov',
    'Dec',
    'Jan',
    'Feb',
    'Mar',
  ]
  return monthNames.map((month, idx) => {
    const yearLabel = idx < 9 ? yearShort : nextYearShort
    return `${month} ${yearLabel}`
  })
}

const PROPANE_MONTHS = [
  'apr',
  'may',
  'jun',
  'jul',
  'aug',
  'sep',
  'oct',
  'nov',
  'dec',
  'jan',
  'feb',
  'mar',
]

const PropaneBusiness = ({ permissions }) => {
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
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const SCREEN_NAME = screenTitle?.title

  const keycloak = useSession()
  const [rows, setRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const unsavedChangesRef = useRef({ unsavedRows: {}, rowsBeforeChange: {} })
  const monthTitles = getMonthYearTitles(
    Number(AOP_YEAR) || new Date().getFullYear(),
  )
  const yearRange =
    year?.selectedYearRange || `${AOP_YEAR}-${Number(AOP_YEAR) + 1}` // e.g. "2026-27"
  const headerMap = generateHeaderNames(yearRange)
  const dynamicYearMonthColumns = PROPANE_MONTHS.map((month, idx) => {
    const headerIdx = ((idx + 4 - 1) % 12) + 1
    return {
      field: month,
      title: headerMap[headerIdx],
      editable: true,
      type: 'propaneDropdown',
      editor: PropaneDropdown,
    }
  })

  // Numeric month columns (April=4, ..., Jan=1, etc.)
  //   const numericMonthColumns = PROPANE_MONTHS_NUMERIC.map(({ field, title }) => ({
  //     field,
  //     title,
  //     editable: true,
  //     width: 120,
  //     rightAlign: true,
  //     headerAlign: 'left',
  //     type: 'number',
  //   }))

  // Choose which columns to use:
  // To use dynamic year headers:
  const columns = [
    { field: 'productName', title: 'Particulars', editable: false },
    { field: 'uom', title: 'UOM', editable: false, widthT: 55 },
    { field: 'normType', title: 'Norm Type', editable: false, hidden: true },
    ...dynamicYearMonthColumns,
    { field: 'remarks', title: 'Remarks', editable: true },
  ]
  // Fetch data
  const fetchData = async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      const response = await BusinessDemandDataApiService.getBDssData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const formattedData = (response.data || []).map((item, index) => ({
        ...item,
        id: item.normParameterFKId || index,
        inEdit: false,
        originalRemark: item.remarks || '', // Store original
        remarks: item.remarks || '', // Editable field
        Particulars: 'Zone Selection',
      }))
      setRows(formattedData)
    } catch (error) {
      setRows([])
    }
    setLoading(false)
  }

  useEffect(() => {
    fetchData()
  }, [PLANT_ID, SITE_ID, VERTICAL_ID, AOP_YEAR, keycloak])

  const savePropaneBusiness = async () => {
    setLoading(true)
    try {
      const editedRows = Object.values(modifiedCells)
      if (editedRows.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      const requiredFields = ['remarks']
      const validationMessage = validateFields(editedRows, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }
      // Prepare payload like NormalOpNorms
      const businessData = editedRows.map((row) => ({
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
        uom: row.uom || '',
        auditYear: AOP_YEAR,
        normParameterFKId:
          row.normParameterFKId || row.normParameterId || row.id,
        remarks: row.remarks,
        id: row.id,
      }))

      if (businessData.length > 0) {
        const response = await BusinessDemandDataApiService.savepropanebusiness(
          PLANT_ID,
          AOP_YEAR,
          businessData,
          keycloak,
        )

        if (response?.code === 200) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Saved Successfully!',
            severity: 'success',
          })
          setModifiedCells({})
          fetchData()
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Save Failed!',
            severity: 'error',
          })
        }
      }
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving data!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }
  // Remark dialog logic
  const handleRemarkCellClick = (dataItem) => {
    setCurrentRemark(dataItem.remarks || '')
    setCurrentRowId(dataItem.id)
    setRemarkDialogOpen(true)
  }

  // Permissions logic (copy from BusinessDemand if needed)
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
      downloadExcelBtn: false,
      uploadExcelBtn: false,
    }
  }
  const adjustedPermissions = getAdjustedPermissions(
    {
      ...permissions,
      downloadExcelBtn: false,
      uploadExcelBtn: false,
      titleName: '',
    },
    isOldYear,
  )

  return (
    <div>
      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        setRows={setRows}
        columns={columns}
        rows={rows}
        title=''
        fetchData={fetchData}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        saveChanges={savePropaneBusiness}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        handleRemarkCellClick={handleRemarkCellClick}
        permissions={adjustedPermissions}
        groupBy='Particulars'
        // Add other props as needed
      />
    </div>
  )
}

export default PropaneBusiness
