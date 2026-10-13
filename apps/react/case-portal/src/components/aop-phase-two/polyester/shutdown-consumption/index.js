import React, { useState, useEffect, useCallback, useMemo } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import {
  convertRowsByUnit,
  convertDataForSave,
} from '../production-target/utils'
import { ShutdownConsumptionApiService } from '../../services/polyester/shutdownConsumptionApiService'

const ShutdownConsumption = () => {
  const dispatch = useDispatch()
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    oldYear,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
    isReleased,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const PLANT_NAME = plantObject?.name
  const SITE_NAME = siteObject?.name
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const SCREEN_NAME = screenTitle?.title
  const IS_OLD_YEAR = oldYear?.oldYear
  const IS_RELEASED = isReleased

  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR, IS_RELEASED)

  const [rows, setRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [loading, setLoading] = useState(false)
  const [grades, setGrades] = useState([])
  const [shutdownMonths, setShutdownMonths] = useState([])
  const [selectedGradeId, setSelectedGradeId] = useState(null)

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const valueFormat = ValueFormatterProduction(5)
  const headerMap = generateHeaderNames(AOP_YEAR)

  const monthsConfig = [
    { field: 'april', key: 4, title: 'Apr' },
    { field: 'may', key: 5, title: 'May' },
    { field: 'june', key: 6, title: 'Jun' },
    { field: 'july', key: 7, title: 'Jul' },
    { field: 'august', key: 8, title: 'Aug' },
    { field: 'september', key: 9, title: 'Sep' },
    { field: 'october', key: 10, title: 'Oct' },
    { field: 'november', key: 11, title: 'Nov' },
    { field: 'december', key: 12, title: 'Dec' },
    { field: 'january', key: 1, title: 'Jan' },
    { field: 'february', key: 2, title: 'Feb' },
    { field: 'march', key: 3, title: 'Mar' },
  ]

  const columns = useMemo(() => {
    return [
    {
      field: 'idFromApi',
      title: 'ID',
      hidden: true,
      isVisible: false,
      minWidth: 100,
    },
    {
      field: 'aopCaseId',
      title: 'Case ID',
      hidden: true,
      isVisible: false,
      minWidth: 100,
    },
    {
      field: 'materialFKId',
      title: 'Particulars',
      editable: false,
      hidden: true,
      minWidth: 120,
    },
    {
      field: 'normParameterTypeDisplayName',
      title: 'Type',
      editable: false,
      hidden: true,
      minWidth: 120,
    },
    {
      field: 'sapCode',
      title: 'SAP MAT Code',
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    {
      field: 'productName',
      title: 'Particulars',
      editable: false,
      minWidth: 200,
    },
    {
      field: 'uom',
      title: 'UOM',
      widthT: 80,
      minWidth: 80,
      type: 'text',
      editable: false,
    },
    ...monthsConfig.map((m) => ({
      field: m.field,
      title: headerMap[m.key] || m.title,
      editable: shutdownMonths.some(month => Number(month) === m.key),
      type: 'number1',
      format: valueFormat,
      minWidth: 100,
      allowNegative: true
    })),
    {
      field: 'remarks',
      title: 'Remark',
      type: 'textarea',
      editable: true,
      minWidth: 160,
    },
  ]
  }, [headerMap, valueFormat, shutdownMonths])

  // Fetch shutdown consumption data
  const fetchActiveMonths = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      const response =
        await ShutdownConsumptionApiService.getShutdownMonths(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
        const monthsList = Array.isArray(response) ? response : response?.data || []
        setShutdownMonths(monthsList)
      } catch (error) {
        setShutdownMonths([])
        console.error('Error fetching data:', error)
      }
    }, [PLANT_ID, AOP_YEAR, keycloak])

  const fetchData = useCallback(
    async (gradeId) => {
      if (!PLANT_ID || !AOP_YEAR) return
      // if (!gradeId) return

      setLoading(true)
      setRows([])
      try {
        const response =
          await ShutdownConsumptionApiService.getShutdownConsumption(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
          )

        if (
          response?.code === 200 &&
          Array.isArray(response?.data?.mcuNormsValueDTOList)
        ) {
          const formattedData = response.data.mcuNormsValueDTOList.map(
            (item, index) => ({
              ...item,
              idFromApi: item.id || null,
              uom: item.UOM || item.uom || null,
              materialFKId: item.materialFKId || null,
              productName: item.productName || item.materialDisplayName || '',
              remarks: item.remarks?.trim() || '',
              originalRemark: item.remarks?.trim() || '',
              id: index,
              isEditable: item?.isEditable,
            }),
          )
          setRows(formattedData)
        } else {
          setRows([])
        }
      } catch (error) {
        console.error('Error fetching shutdown consumption data:', error)
        setRows([])
      } finally {
        setLoading(false)
      }
    },
    [keycloak, PLANT_ID, AOP_YEAR],
  )

  // Initial load: fetch grades and data
  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    setRows([])
    setShutdownMonths([])
    fetchData()
    fetchActiveMonths()
  }, [PLANT_ID, AOP_YEAR])

  // Re-fetch data when selectedGradeId or selectedUnit changes
  // useEffect(() => {
  //   setModifiedCells({})
  //   if (selectedGradeId) {
  //     fetchData(selectedGradeId)
  //   }
  // }, [selectedGradeId, selectedUnit, fetchData])

  const saveChanges = useCallback(async () => {
    try {
      const modifiedData = Object.values(modifiedCells)
      if (modifiedData.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      const months = monthsConfig.map((m) => m.field)
      const invalidRows = modifiedData.filter((row) => {
        for (const month of months) {
          const val = row[month]
          if (val === null || val === undefined || val === '') {
            return true
          }
        }
        const remarkVal = row.remarks || row.remark
        const origRemarkVal = row.originalRemark || ''
        if (
          !remarkVal ||
          !remarkVal.trim() ||
          remarkVal.trim() === origRemarkVal.trim()
        ) {
          return true
        }
        return false
      })

      if (invalidRows.length > 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message:
            'Please fill all fields in edited row and update the Remark!',
          severity: 'error',
        })
        return
      }

      const dataToSave = modifiedData.map((row) => ({
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
        financialYear: AOP_YEAR,
        plantFkId: PLANT_ID,
        siteFkId: siteObject?.id,
        materialFkId: row.materialFkId || row.normParametersFkId,
        verticalFkId: VERTICAL_ID,
        id: row.idFromApi || null,
        remark: row.remarks || null,
        remarks: row.remarks || null,
        gradeFkId: null,
      }))

      setLoading(true)
      const res = await ShutdownConsumptionApiService.saveShutdownConsumption(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        dataToSave,
      )

      if (res) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        dispatch(setIsBlocked(false))
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Save failed, please try again!',
          severity: 'error',
        })
      }
      setLoading(false)
    } catch (error) {
      console.error('Error saving shutdown consumption data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Save failed, please try again!',
        severity: 'error',
      })
      setLoading(false)
    }
  }, [
    modifiedCells,
    AOP_YEAR,
    PLANT_ID,
    siteObject,
    VERTICAL_ID,
    dispatch,
    fetchData,
  ])

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })
    try {
      const Excel_Name = `${VERTICAL_NAME}_${SITE_NAME}_${PLANT_NAME}_Shutdown_Consumption_Norms_${AOP_YEAR}`
      await ShutdownConsumptionApiService.exportShutdownConsumption(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        Excel_Name,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Excel downloaded successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting shutdown consumption excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    }
  }

  const handleExcelUpload = async (rawFile) => {
    setLoading(true)
    try {
      const response =
        await ShutdownConsumptionApiService.importShutdownConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          rawFile,
        )
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData()
      } else if (response?.code === 400 && response?.data) {
        const byteCharacters = atob(response.data)
        const byteNumbers = Array.from(byteCharacters, (char) =>
          char.charCodeAt(0),
        )
        const byteArray = new Uint8Array(byteNumbers)
        const blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'Error File - Shutdown Consumption.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error importing shutdown consumption excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleRemarkCellClick = useCallback(
    (row) => {
      if (READ_ONLY) return
      setCurrentRemark(row.remarks || '')
      setCurrentRowId(row.id)
      setRemarkDialogOpen(true)
    },
    [READ_ONLY],
  )

  // Dropdown configuration for grade selection
  const dropdownConfig = {
    options: grades,
    label: 'Grade',
    placeholder: 'Select Grade',
    valueKey: 'gradeId',
    labelKey: 'displayName',
  }

  const permissions = {
    showAction: false,
    addButton: false,
    deleteButton: false,
    editButton: false,
    saveBtn: true,
    showCalculate: false,
    allAction: true,
    showDropdown: false,
    showExport: true,
    showImport: true,
    showTitleNameBusiness: true,
    showTitle: true,
    showNote: true,
    note: '(in Additional consumption during Shutdown above Steady State Consumption)',
    titleName: 'Shutdown Consumption Norms',
  }

  return (
    <AdvanceKendoTable
      columns={columns}
      rows={rows}
      setRows={setRows}
      title={permissions.titleName}
      loading={loading}
      modifiedCells={modifiedCells}
      setModifiedCells={setModifiedCells}
      remarkDialogOpen={remarkDialogOpen}
      setRemarkDialogOpen={setRemarkDialogOpen}
      currentRemark={currentRemark}
      setCurrentRemark={setCurrentRemark}
      currentRowId={currentRowId}
      setCurrentRowId={setCurrentRowId}
      permissions={permissions}
      saveChanges={saveChanges}
      handleRemarkCellClick={handleRemarkCellClick}
      handleExport={handleExport}
      handleExcelUpload={handleExcelUpload}
      dropdownConfig={dropdownConfig}
      selectedDropdownValue={selectedGradeId || ''}
      setSelectedDropdownValue={setSelectedGradeId}
      snackbarOpen={snackbarOpen}
      setSnackbarOpen={setSnackbarOpen}
      snackbarData={snackbarData}
      setSnackbarData={setSnackbarData}
      groupBy={['normParameterTypeDisplayName']}
      customHeight={70}
      paginationConfig={{
        threshold: 100,
        buttonCount: 5,
        pageSizes: [10, 20, 50, 100],
        defaultPageSize: 100,
      }}
    />
  )
}

export default ShutdownConsumption
