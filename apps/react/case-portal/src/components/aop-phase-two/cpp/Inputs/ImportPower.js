import { useEffect, useState, useCallback } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { InputApiService } from 'components/aop-phase-two/services/cpp/inputApiService'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'

const ImportPower = () => {
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

  // Column definitions
  const columns = [
    { field: 'id', title: 'ID', hidden: true },
    {
      field: 'plantName',
      title: 'Plant',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    {
      field: 'sourceName',
      title: 'Utility/Material',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    {
      field: 'sapCode',
      title: 'SAP Code',
      width: 120,
      minWidth: 120,
      type: 'text',
      editable: false,
    },
    {
      field: 'materialCode',
      title: 'Material Code',
      width: 120,
      minWidth: 120,
      type: 'text',
      editable: false,
      hidden: true,
    },
    {
      field: 'uom',
      title: 'UOM',
      width: 80,
      minWidth: 80,
      type: 'text',
      editable: false,
    },
    {
      field: 'april',
      title: headerMap[4],
      editable: true,
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
      editable: true,
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
      editable: true,
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
      editable: true,
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
      editable: true,
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
      editable: true,
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
      editable: true,
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
      editable: true,
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
      editable: true,
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
      editable: true,
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
      editable: true,
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
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'remarks',
      title: 'Remarks',
      width: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchImportConsumptionData(keycloak, PLANT_ID, AOP_YEAR)
      // setRows(dummyRowsData);
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchImportConsumptionData = async (keycloak, PLANT_ID, AOP_YEAR) => {
    setLoading(true)
    try {
      const res = await InputApiService.getImportPowerCapacity(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (res?.length === 0) {
        setRows([])
        setOriginalRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      let tempRes = res.map((item, index) => {
        const transformed = {
          sourceId: item?.sourceId,
          sourceName: item?.sourceName,
          plantName: item?.plantName,
          sapCode: item?.sapCode,
          materialCode: item?.materialCode,
          uom: item?.uom,
          april: item?.april,
          may: item?.may,
          june: item?.june,
          july: item?.july,
          aug: item?.august,
          sept: item?.september,
          oct: item?.october,
          nov: item?.november,
          dec: item?.december,
          jan: item?.january,
          feb: item?.february,
          mar: item?.march,
          remarks: item?.remarks,
          id: item?.sourceId || index + 1,
        }
        return transformed
      })

      // Calculate totals for each month
      const totalRow = {
        sourceId: '',
        sourceName: '',
        plantName: 'Total',
        sapCode: '',
        materialCode: '',
        uom: tempRes.length > 0 ? tempRes[0].uom : 'Unit',
        april: tempRes.reduce(
          (sum, row) => sum + (parseFloat(row.april) || 0),
          0,
        ),
        may: tempRes.reduce((sum, row) => sum + (parseFloat(row.may) || 0), 0),
        june: tempRes.reduce(
          (sum, row) => sum + (parseFloat(row.june) || 0),
          0,
        ),
        july: tempRes.reduce(
          (sum, row) => sum + (parseFloat(row.july) || 0),
          0,
        ),
        aug: tempRes.reduce((sum, row) => sum + (parseFloat(row.aug) || 0), 0),
        sept: tempRes.reduce(
          (sum, row) => sum + (parseFloat(row.sept) || 0),
          0,
        ),
        oct: tempRes.reduce((sum, row) => sum + (parseFloat(row.oct) || 0), 0),
        nov: tempRes.reduce((sum, row) => sum + (parseFloat(row.nov) || 0), 0),
        dec: tempRes.reduce((sum, row) => sum + (parseFloat(row.dec) || 0), 0),
        jan: tempRes.reduce((sum, row) => sum + (parseFloat(row.jan) || 0), 0),
        feb: tempRes.reduce((sum, row) => sum + (parseFloat(row.feb) || 0), 0),
        mar: tempRes.reduce((sum, row) => sum + (parseFloat(row.mar) || 0), 0),
        remarks: '',
        id: 'TOTAL_ROW',
        isTotal: true, // Flag to identify total row
        isEditable: false,
      }

      // Add total row at the end
      tempRes.push(totalRow)

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

  // Custom item change handler to recalculate totals in real-time
  const customItemChange = useCallback((event, setRowsFunc) => {
    const { field } = event

    // Only recalculate totals for month fields
    const monthFields = [
      'april',
      'may',
      'june',
      'july',
      'aug',
      'sept',
      'oct',
      'nov',
      'dec',
      'jan',
      'feb',
      'mar',
    ]

    if (!monthFields.includes(field)) {
      return
    }

    // Update rows with recalculated totals
    setRowsFunc((currentRows) => {
      // Filter out the total row to calculate new totals
      const dataRows = currentRows.filter((row) => !row.isTotal)

      // Recalculate totals
      const totalRow = {
        sourceId: '',
        sourceName: '',
        plantName: 'Total',
        sapCode: '',
        materialCode: '',
        uom: dataRows.length > 0 ? dataRows[0].uom : 'Unit',
        april: dataRows.reduce(
          (sum, row) => sum + (parseFloat(row.april) || 0),
          0,
        ),
        may: dataRows.reduce((sum, row) => sum + (parseFloat(row.may) || 0), 0),
        june: dataRows.reduce(
          (sum, row) => sum + (parseFloat(row.june) || 0),
          0,
        ),
        july: dataRows.reduce(
          (sum, row) => sum + (parseFloat(row.july) || 0),
          0,
        ),
        aug: dataRows.reduce((sum, row) => sum + (parseFloat(row.aug) || 0), 0),
        sept: dataRows.reduce(
          (sum, row) => sum + (parseFloat(row.sept) || 0),
          0,
        ),
        oct: dataRows.reduce((sum, row) => sum + (parseFloat(row.oct) || 0), 0),
        nov: dataRows.reduce((sum, row) => sum + (parseFloat(row.nov) || 0), 0),
        dec: dataRows.reduce((sum, row) => sum + (parseFloat(row.dec) || 0), 0),
        jan: dataRows.reduce((sum, row) => sum + (parseFloat(row.jan) || 0), 0),
        feb: dataRows.reduce((sum, row) => sum + (parseFloat(row.feb) || 0), 0),
        mar: dataRows.reduce((sum, row) => sum + (parseFloat(row.mar) || 0), 0),
        remarks: '',
        id: 'TOTAL_ROW',
        isTotal: true,
        isEditable: false,
      }

      // Return updated rows with recalculated total
      return [...dataRows, totalRow]
    })
  }, [])

  // Permissions (adjust as needed)
  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showImport: true,
    showExport: true,
    ExcelName: `Import Power Capacity - ${AOP_YEAR}`,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: screenTitle?.title,
  }

  const saveChanges = async () => {
    setLoading(true)
    console.log('modifiedCells', modifiedCells)
    const modifiedData = Object.values(modifiedCells)
    console.log('modifiedData', modifiedData)
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
    console.log('rawData', rawData)
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
      'april',
      'may',
      'june',
      'july',
      'aug',
      'sept',
      'oct',
      'nov',
      'dec',
      'jan',
      'feb',
      'mar',
    ]
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'sourceName',
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

    const payload = modifiedData?.map(
      ({
        id,
        inEdit,
        sourceId,
        sourceName,
        plantName,
        sapCode,
        materialCode,
        uom,
        aug,
        sept,
        oct,
        nov,
        dec,
        jan,
        feb,
        mar,
        ...rest
      }) => ({
        sourceId,
        sourceName,
        plantName,
        sapCode,
        materialCode,
        uom,
        august: aug,
        september: sept,
        october: oct,
        november: nov,
        december: dec,
        january: jan,
        february: feb,
        march: mar,
        ...rest,
      }),
    )

    try {
      console.log('payload', payload)

      const response = await InputApiService.saveImportPowerCapacity(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        payload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} rows changes!`,
        severity: 'success',
      })
      fetchImportConsumptionData(keycloak, PLANT_ID, AOP_YEAR)
    } catch (error) {
      console.error('Error saving import consumption data:', error)
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
      const response = await InputApiService.saveImportPowerCapacityExcel(
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
        await fetchImportConsumptionData(keycloak, PLANT_ID, AOP_YEAR)
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
        link.setAttribute('download', `Error File - Import Power.xlsx`)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        await fetchImportConsumptionData(keycloak, PLANT_ID, AOP_YEAR)
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
      await InputApiService.exportImportPowerCapacityExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
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
        title='Purchase Power Input'
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
        //groupBy="plant"
      />
    </Box>
  )
}

export default ImportPower
