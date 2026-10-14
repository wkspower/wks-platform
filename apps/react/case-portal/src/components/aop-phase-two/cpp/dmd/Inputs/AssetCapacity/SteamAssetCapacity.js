import { useEffect, useState, useRef, useMemo } from 'react'
import { Box } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { InputApiService } from 'components/aop-phase-two/services/cpp/jmd/inputApiService'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'

// Only users with this role can edit the Fixed Min / Fixed Max columns.
const CPP_MASTER_ROLE = 'cpp-master'

const SteamAssetCapacity = ({
  initialRows = [],
  onRefresh,
  externalLoading = false,
}) => {
  const keycloak = useSession()
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
    jmdSelectedPlants,
  } = dataGridStore
  const AOP_YEAR = year?.selectedYear
  const EXCEL_NAME = generateExcelName(dataGridStore, 'Steam_Asset_Capacity')
  const IS_DMD = siteObject?.name?.toLowerCase() === 'dmd'

  // Only cpp-master users may edit Fixed Min / Fixed Max.
  const isCppMasterRole = useMemo(
    () => keycloak?.realmAccess?.roles?.includes(CPP_MASTER_ROLE) || false,
    [keycloak?.realmAccess?.roles],
  )

  // Multi-plant list
  const PLANT_ID_LIST = plantObject?.id
  // useMemo(
  //   () => jmdSelectedPlants?.map((plant) => plant.id) || [],
  //   [jmdSelectedPlants],
  // )

  const headerMap = generateHeaderNames(AOP_YEAR)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const valueFormat = ValueFormatterPhaseTwo()

  useEffect(() => {
    setRows(initialRows)
    setOriginalRows(initialRows)
    setModifiedCells({})
  }, [initialRows])

  const combinedLoading = loading || externalLoading

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const MONTH_CONFIG = [
    { prefix: 'apr', headerIndex: 4 },
    { prefix: 'may', headerIndex: 5 },
    { prefix: 'jun', headerIndex: 6 },
    { prefix: 'jul', headerIndex: 7 },
    { prefix: 'aug', headerIndex: 8 },
    { prefix: 'sep', headerIndex: 9 },
    { prefix: 'oct', headerIndex: 10 },
    { prefix: 'nov', headerIndex: 11 },
    { prefix: 'dec', headerIndex: 12 },
    { prefix: 'jan', headerIndex: 1 },
    { prefix: 'feb', headerIndex: 2 },
    { prefix: 'mar', headerIndex: 3 },
  ]

  // Generate month column groups: each month gets Min + Max + Man. Load child columns
  const MONTH_COLUMNS = MONTH_CONFIG.map(({ prefix, headerIndex }) => ({
    title: headerMap[headerIndex],
    children: [
      {
        field: `${prefix}Min`,
        title: 'Min Capacity',
        widthT: 140,
        minWidth: 140,
        editable: true,
        type: 'number1',
        format: valueFormat,
        minValue: 'fixedMin',
        maxValue: 'fixedMax',
      },
      {
        field: `${prefix}Max`,
        title: 'Max Capacity',
        widthT: 140,
        minWidth: 140,
        editable: true,
        type: 'number1',
        format: valueFormat,
        minValue: 'fixedMin',
        maxValue: 'fixedMax',
      },
      {
        field: `${prefix}ManLoad`,
        title: 'Man. Load',
        widthT: 140,
        minWidth: 140,
        editable: true,
        type: 'select',
        hidden: IS_DMD,
        options: [
          { label: 1, value: 1 },
          { label: 0, value: 0 },
        ],
      },
    ],
  }))

  const columns = [
    { field: 'id', title: 'ID', hidden: true },
    {
      field: 'assetName',
      title: 'Asset Name',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'plantCode',
      title: 'Plant Code',
      widthT: 130,
      minWidth: 130,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityDistributed',
      title: 'Utility Distributed',
      widthT: 180,
      minWidth: 180,
      type: 'text',
      editable: false,
      // locked: true,
    },
    {
      field: 'distributedSapCode',
      title: 'Utility Distributed Code',
      widthT: 200,
      minWidth: 200,
      type: 'text',
      editable: false,
      // locked: true,
    },
    {
      field: 'utilityGenerated',
      title: 'Utility Generated',
      widthT: 180,
      minWidth: 180,
      type: 'text',
      editable: false,
      // locked: true,
    },
    {
      field: 'generatedUtilityCode',
      title: 'Utility Generated Code',
      widthT: 200,
      minWidth: 200,
      type: 'text',
      editable: false,
      // locked: true,
    },
    {
      field: 'uom',
      title: 'UOM',
      widthT: 100,
      minWidth: 100,
      type: 'text',
      editable: false,
    },
    {
      field: 'fixedMin',
      title: 'Fixed Min',
      widthT: 130,
      minWidth: 130,
      editable: isCppMasterRole,
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'fixedMax',
      title: 'Fixed Max',
      widthT: 130,
      minWidth: 130,
      editable: isCppMasterRole,
      type: 'number1',
      format: valueFormat,
    },
    ...MONTH_COLUMNS,
    {
      field: 'remarks',
      title: 'Remarks',
      widthT: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
      // locked: true,
      // lockPosition: 'right',
    },
  ]

  const debounceTimerRef = useRef(null)

  const customItemChange = (e, setRows) => {
    const { dataItem, field, value } = e

    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current)
    }

    // Flat field naming: e.g. 'aprMin', 'aprMax', 'mayMin', 'mayMax', ...
    const isMin = field.endsWith('Min')
    const isMax = field.endsWith('Max')

    if (!isMin && !isMax) {
      return true
    }

    const numValue = parseFloat(value)

    if (value === '' || isNaN(numValue)) {
      return true
    }

    // Derive the corresponding paired field (e.g. 'aprMin' <-> 'aprMax')
    const prefix = field.slice(0, -3) // e.g. 'apr'
    const correspondingField = isMin ? `${prefix}Max` : `${prefix}Min`
    const correspondingValue = dataItem[correspondingField]

    if (
      isMin &&
      correspondingValue !== undefined &&
      numValue > correspondingValue
    ) {
      const label = prefix.charAt(0).toUpperCase() + prefix.slice(1)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `${label} min capacity cannot be greater than ${label} max capacity`,
        severity: 'error',
      })
      return false
    }

    if (
      isMax &&
      correspondingValue !== undefined &&
      numValue < correspondingValue
    ) {
      const label = prefix.charAt(0).toUpperCase() + prefix.slice(1)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `${label} max capacity cannot be less than ${label} min capacity`,
        severity: 'error',
      })
      return false
    }

    return true
  }

  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showImport: false,
    showExport: false,
    ExcelName: EXCEL_NAME,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: screenTitle?.title,
  }

  const saveChanges = async () => {
    setLoading(true)
    console.log('modifiedCells', modifiedCells)
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const rawData = Object.values(modifiedCells)
    const data = rawData.filter((row) => row.inEdit)
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    // Custom validation: remarks must be filled when any monthly/fixed field changes
    const fieldsToCheck = [
      'aprMin',
      'aprMax',
      'mayMin',
      'mayMax',
      'junMin',
      'junMax',
      'julMin',
      'julMax',
      'augMin',
      'augMax',
      'sepMin',
      'sepMax',
      'octMin',
      'octMax',
      'novMin',
      'novMax',
      'decMin',
      'decMax',
      'janMin',
      'janMax',
      'febMin',
      'febMax',
      'marMin',
      'marMax',
      'fixedMin',
      'fixedMax',
    ]
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'assetName',
    )

    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationError,
        severity: 'error',
      })
      setLoading(false)
      return
    }

    // Backend expects AssetCapacityRequestDTO: { steamResponse: [...] }
    const payload = { steamResponse: modifiedData }

    try {
      console.log('payload', payload)

      const response = await InputApiService.saveAssetCapacities(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
        payload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
      await onRefresh?.()
    } catch (error) {
      console.error('Error saving steam asset capacity data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = async (file) => {
    if (!file) return

    setLoading(true)
    try {
      const response = await InputApiService.importAssetCapacityExcelUnified(
        file,
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
        'Steam',
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Excel file imported successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await onRefresh?.()
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
        link.setAttribute('download', `Error File - Steam Asset Capacity.xlsx`)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        await onRefresh?.()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error uploading Excel file:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Failed to import Excel file: ${error.message}`,
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      await InputApiService.exportAssetCapacityExcelUnified(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
        'Steam',
        EXCEL_NAME,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Steam Asset Capacity data:', error)
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
      <LoaderBackdrop open={!!combinedLoading} />
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Steam Asset Capacity Input'
        permissions={permissions}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        saveChanges={saveChanges}
        handleExcelUpload={handleExcelUpload}
        handleExport={handleExport}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        customItemChange={customItemChange}
        groupBy={['assetType']}
      />
    </Box>
  )
}

export default SteamAssetCapacity
