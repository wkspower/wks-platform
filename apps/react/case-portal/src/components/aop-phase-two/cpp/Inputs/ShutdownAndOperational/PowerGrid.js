import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { Stack } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { InputApiService } from 'components/aop-phase-two/services/cpp/inputApiService'
import { validateNestedRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import NestedKendoTable from 'components/aop-phase-two/common/NestedKendoTable/index'

const PowerGrid = ({ hoursRows = [] }) => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year, screenTitle } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = ValueFormatterProduction()

  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({ message: '', severity: 'info' })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const nestedColumns = [
    { field: 'assetName', title: 'Asset Name', widthT: 150, minWidth: 150, type: 'text', editable: false, locked: true },
    { field: 'assetType', title: 'Asset Type', widthT: 150, minWidth: 150, type: 'text', editable: false, locked: true, hidden: true },
    { field: 'utilityDistributed.name', title: 'Utility Distributed', widthT: 150, minWidth: 150, type: 'text', editable: false, locked: true },
    { field: 'utilityDistributed.sapCode', title: 'Distributed SAP Code', widthT: 150, minWidth: 150, type: 'text', editable: false, locked: true },
    { field: 'utilityGenerated.name', title: 'Utility Generated', widthT: 150, minWidth: 150, type: 'text', editable: false, locked: true },
    { field: 'utilityGenerated.sapCode', title: 'Generated SAP Code', widthT: 150, minWidth: 150, type: 'text', editable: false, locked: true },
    {
      title: headerMap[4],
      children: [
        { field: 'april.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'april.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    {
      title: headerMap[5],
      children: [
        { field: 'may.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'may.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    {
      title: headerMap[6],
      children: [
        { field: 'june.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'june.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    {
      title: headerMap[7],
      children: [
        { field: 'july.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'july.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    {
      title: headerMap[8],
      children: [
        { field: 'aug.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'aug.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    {
      title: headerMap[9],
      children: [
        { field: 'sep.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'sep.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    {
      title: headerMap[10],
      children: [
        { field: 'oct.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'oct.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    {
      title: headerMap[11],
      children: [
        { field: 'nov.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'nov.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    {
      title: headerMap[12],
      children: [
        { field: 'dec.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'dec.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    {
      title: headerMap[1],
      children: [
        { field: 'jan.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'jan.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    {
      title: headerMap[2],
      children: [
        { field: 'feb.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'feb.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    {
      title: headerMap[3],
      children: [
        { field: 'march.shutdownHrs', title: 'Shutdown Hrs', widthT: 120, minWidth: 120, editable: true, type: 'wholeNumber', format: valueFormat },
        { field: 'march.netOperationHrs', title: 'Operational Hrs', widthT: 120, minWidth: 120, editable: false, type: 'wholeNumber', format: valueFormat },
      ],
    },
    { field: 'remarks', title: 'Remarks', widthT: 250, type: 'textarea', editable: true, minWidth: 250 },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchData()
      setModifiedCells({})
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchData = async () => {
    setLoading(true)
    try {
      const res = await InputApiService.getOperationHoursData(keycloak, PLANT_ID, AOP_YEAR)

      if (!res || res?.powerResponse?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        setLoading(false)
        return
      }

      const rowsWithIds = res?.powerResponse
        ?.filter((row) => row.assetType !== 'Power_Dis')
        ?.map((row, index) => ({ ...row, id: row.id || index + 1 }))

      setRows(rowsWithIds)
      setOriginalRows(rowsWithIds)
    } catch (error) {
      console.error('Error fetching power grid data:', error)
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

  const saveChanges = async () => {
    setLoading(true)
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
      setLoading(false)
      return
    }

    const data = modifiedData.filter((row) => row.inEdit)
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
      setLoading(false)
      return
    }

    const fieldsToCheck = [
      'april.shutdownHrs', 'may.shutdownHrs', 'june.shutdownHrs', 'july.shutdownHrs',
      'aug.shutdownHrs', 'sep.shutdownHrs', 'oct.shutdownHrs', 'nov.shutdownHrs',
      'dec.shutdownHrs', 'jan.shutdownHrs', 'feb.shutdownHrs', 'march.shutdownHrs',
    ]
    const validationError = validateNestedRowDataWithRemarks(data, originalRows, fieldsToCheck, 'assetName')
    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({ message: validationError, severity: 'error' })
      setLoading(false)
      return
    }

    const payload = modifiedData.map(({ id, inEdit, ...rest }) => rest)
    try {
      await InputApiService.saveOperationHours(keycloak, PLANT_ID, AOP_YEAR, { powerResponse: payload })
      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({ message: `Successfully saved ${modifiedData.length} changes!`, severity: 'success' })
      fetchData()
    } catch (error) {
      console.error('Error saving power grid data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Failed to save changes. Please try again.', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = async (file) => {
    if (!file) return
    setLoading(true)
    try {
      const response = await InputApiService.savePowerResponseExcel(file, keycloak, PLANT_ID, AOP_YEAR)
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Excel file imported successfully!', severity: 'success' })
        setModifiedCells({})
        await fetchData()
      } else if (response?.code === 400 && response?.data) {
        const byteCharacters = atob(response.data)
        const byteArray = new Uint8Array(Array.from(byteCharacters, (c) => c.charCodeAt(0)))
        const blob = new Blob([byteArray], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'Error File - Shutdown and Operational.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Partial data saved. Error file downloaded.', severity: 'warning' })
        await fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Upload Failed!', severity: 'error' })
      }
    } catch (error) {
      console.error('Error uploading Excel file:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: `Failed to import Excel file: ${error.message}`, severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({ message: 'Excel download started!', severity: 'info' })
    try {
      await InputApiService.exportPowerResponseExcel(keycloak, PLANT_ID, AOP_YEAR)
      setSnackbarData({ message: 'Excel download completed successfully!', severity: 'success' })
    } catch (error) {
      console.error('Error exporting power response data:', error)
      setSnackbarData({ message: 'Excel download failed. Please try again.', severity: 'error' })
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  return (
    <Box>
      <Backdrop sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }} open={!!loading}>
        <CircularProgress color='inherit' />
      </Backdrop>
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
    </Box>
  )
}

export default PowerGrid
