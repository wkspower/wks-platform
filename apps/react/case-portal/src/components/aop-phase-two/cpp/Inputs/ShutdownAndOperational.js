import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { InputApiService } from 'components/aop-phase-two/services/cpp/inputApiService'
import STGShutdownAndOperationalHr from './STGShutdownAndOperationalHr'
import { validateNestedRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { Stack } from '../../../../../node_modules/@mui/material/index'
import NestedKendoTable from 'components/aop-phase-two/common/NestedKendoTable/index'

const generateMonthHours = (aopYear) => {
  if (!aopYear) return {}

  const [startYear, endYear] = aopYear.split('-').map((y) => parseInt(y))
  const fullStartYear = startYear < 100 ? 2000 + startYear : startYear
  const fullEndYear = endYear < 100 ? 2000 + endYear : endYear

  const getDaysInMonth = (month, year) => {
    return new Date(year, month, 0).getDate()
  }

  const hourRows = {
    apr: getDaysInMonth(4, fullStartYear) * 24,
    may: getDaysInMonth(5, fullStartYear) * 24,
    jun: getDaysInMonth(6, fullStartYear) * 24,
    jul: getDaysInMonth(7, fullStartYear) * 24,
    aug: getDaysInMonth(8, fullStartYear) * 24,
    sep: getDaysInMonth(9, fullStartYear) * 24,
    oct: getDaysInMonth(10, fullStartYear) * 24,
    nov: getDaysInMonth(11, fullStartYear) * 24,
    dec: getDaysInMonth(12, fullStartYear) * 24,
    jan: getDaysInMonth(1, fullEndYear) * 24,
    feb: getDaysInMonth(2, fullEndYear) * 24,
    mar: getDaysInMonth(3, fullEndYear) * 24,
  }

  return hourRows
}

const ShutdownAndOperational = () => {
  const keycloak = useSession()
  const [modifiedCells, setModifiedCells] = useState({})
  const [modifiedCellsHours, setModifiedCellsHours] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, siteObject, verticalObject, year, screenTitle } =
    dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const valueFormat = ValueFormatterProduction()

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const nestedColumns = [
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
      field: 'assetType',
      title: 'Asset Type',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
      hidden: true,
    },
    {
      field: 'utilityDistributed.name',
      title: 'Utility Distributed',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityDistributed.sapCode',
      title: 'Distributed SAP Code',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.name',
      title: 'Utility Generated',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.sapCode',
      title: 'Generated SAP Code',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      title: headerMap[4],
      children: [
        {
          field: 'april.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'april.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[5],
      children: [
        {
          field: 'may.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'may.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[6],
      children: [
        {
          field: 'june.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'june.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[7],
      children: [
        {
          field: 'july.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'july.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[8],
      children: [
        {
          field: 'aug.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'aug.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[9],
      children: [
        {
          field: 'sep.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'sep.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[10],
      children: [
        {
          field: 'oct.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'oct.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[11],
      children: [
        {
          field: 'nov.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'nov.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[12],
      children: [
        {
          field: 'dec.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'dec.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[1],
      children: [
        {
          field: 'jan.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'jan.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[2],
      children: [
        {
          field: 'feb.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'feb.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[3],
      children: [
        {
          field: 'march.shutdownHrs',
          title: 'Shutdown Hrs',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'march.netOperationHrs',
          title: 'Operational Hrs',
          widthT: 120,
          minWidth: 120,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      field: 'remarks',
      title: 'Remarks',
      widthT: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]

  const [hoursRows, setHoursRows] = useState([])
  const hoursColumns = [
    {
      field: 'apr',
      title: headerMap[4] || 'Apr',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'may',
      title: headerMap[5] || 'May',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'jun',
      title: headerMap[6] || 'Jun',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'jul',
      title: headerMap[7] || 'Jul',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'aug',
      title: headerMap[8] || 'Aug',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'sep',
      title: headerMap[9] || 'Sep',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'oct',
      title: headerMap[10] || 'Oct',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'nov',
      title: headerMap[11] || 'Nov',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'dec',
      title: headerMap[12] || 'Dec',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'jan',
      title: headerMap[1] || 'Jan',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'feb',
      title: headerMap[2] || 'Feb',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'mar',
      title: headerMap[3] || 'Mar',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchShutdownAndOperationalData()
      setModifiedCells({})
    }
  }, [PLANT_ID, AOP_YEAR])

  useEffect(() => {
    if (AOP_YEAR) {
      const hoursData = generateMonthHours(AOP_YEAR)
      setHoursRows([hoursData])
    }
  }, [AOP_YEAR])

  const fetchShutdownAndOperationalData = async () => {
    setLoading(true)
    try {
      const res = await InputApiService.getOperationHoursData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (!res || res?.powerResponse?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      const rowsWithIds = res?.powerResponse
        ?.filter((row) => row.assetType !== 'Power_Dis')
        ?.map((row, index) => ({
          ...row,
          id: row.id || index + 1,
        }))

      setRows(rowsWithIds)
      setOriginalRows(rowsWithIds)
    } catch (error) {
      console.error('Error fetching shutdown and operational data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showImport: true,
    showExport: true,
    ExcelName: `Shutdown and Operational - ${AOP_YEAR}`,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: screenTitle?.title,
  }

  const hoursPermissions = {
    showAction: false,
    addButton: false,
    deleteButton: false,
    editButton: false,
    saveBtn: false,
    allAction: true,
    showTitleNameBusiness: false,
    showTitle: true,
    titleName: 'Total available hours',
  }

  const saveChanges = async () => {
    setLoading(true)
    console.log('modifiedCells', modifiedCells)
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length == 0) {
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
    if (data.length == 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    // Custom validation: If any row data is updated, remarks must be filled and different from original
    const fieldsToCheck = [
      'april.shutdownHrs',
      'may.shutdownHrs',
      'june.shutdownHrs',
      'july.shutdownHrs',
      'aug.shutdownHrs',
      'sep.shutdownHrs',
      'oct.shutdownHrs',
      'nov.shutdownHrs',
      'dec.shutdownHrs',
      'jan.shutdownHrs',
      'feb.shutdownHrs',
      'march.shutdownHrs',
    ]
    const validationError = validateNestedRowDataWithRemarks(
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

    const payload = modifiedData.map(({ id, inEdit, ...rest }) => rest)
    const tempPayload = {
      powerResponse: payload,
    }
    try {
      console.log('payload', payload)

      const response = await InputApiService.saveOperationHours(
        keycloak,
        AOP_YEAR,
        tempPayload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
      fetchShutdownAndOperationalData()
    } catch (error) {
      console.error('Error saving operational hours data:', error)
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
      const response = await InputApiService.savePowerResponseExcel(
        file,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Excel file imported successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await fetchShutdownAndOperationalData()
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
        link.setAttribute(
          'download',
          `Error File - Shutdown and Operational.xlsx`,
        )
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        await fetchShutdownAndOperationalData()
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
      await InputApiService.exportPowerResponseExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Power Response data:', error)
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
      <Stack sx={{ mb: 2 }}>
        <AdvanceKendoTable
          columns={hoursColumns}
          rows={hoursRows}
          setRows={setHoursRows}
          title={hoursPermissions?.titleName}
          permissions={hoursPermissions}
          modifiedCells={modifiedCellsHours}
          setModifiedCells={setModifiedCellsHours}
        />
      </Stack>
      <Stack>
        <NestedKendoTable
          columns={nestedColumns}
          rows={rows}
          setRows={setRows}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          title='Shutdown and Operational Input (Hours)'
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
          hoursRows={hoursRows}
          groupBy={['assetType']}
        />
      </Stack>

      <Stack sx={{ mt: 2 }}>
        <STGShutdownAndOperationalHr hoursRows={hoursRows} />
      </Stack>
    </Box>
  )
}

export default ShutdownAndOperational
