import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import NestedKendoTable from 'components/kendo-data-tables/NestedKendoTable/index'
import { InputApiService } from 'services/phase-two-services/inputApiService'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import { Stack } from '../../../../../../node_modules/@mui/material/index'
import STGShutdownAndOperationalHr from './STGShutdownAndOperationalHr'

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
  const valueFormat = ValueFormatterProduction()

  const nestedColumns = [
    {
      field: 'assetName',
      title: 'Asset Name',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'assetType',
      title: 'Asset Type',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
      hidden: true,
    },
     {
      field: 'utilityDistributed.name',
      title: 'Utility Distributed',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityDistributed.sapCode',
      title: 'Distributed SAP Code',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.name',
      title: 'Utility Generated',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.sapCode',
      title: 'Generated SAP Code',
      width: 150,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'april.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'may.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'june.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'july.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'aug.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'sep.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'oct.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'nov.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'dec.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'jan.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'feb.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
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
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'march.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
  ]

  const [hoursRows, setHoursRows] = useState([])
  const hoursColumns = [
    {
      field: 'apr',
      title: headerMap[4] || 'Apr',
      width: 40,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'may',
      title: headerMap[5] || 'May',
      width: 40,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'jun',
      title: headerMap[6] || 'Jun',
      width: 40,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'jul',
      title: headerMap[7] || 'Jul',
      width: 40,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'aug',
      title: headerMap[8] || 'Aug',
      width: 40,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'sep',
      title: headerMap[9] || 'Sep',
      width: 40,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'oct',
      title: headerMap[10] || 'Oct',
      width: 40,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'nov',
      title: headerMap[11] || 'Nov',
      width: 40,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'dec',
      title: headerMap[12] || 'Dec',
      width: 40,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'jan',
      title: headerMap[1] || 'Jan',
      width: 40,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'feb',
      title: headerMap[2] || 'Feb',
      width: 40,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'mar',
      title: headerMap[3] || 'Mar',
      width: 40,
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

      // Merge rows with same assetName
      // const mergedMap = {}
      // res?.powerResponse?.forEach((row) => {
      //   const key = row.assetName
      //   if (!mergedMap[key]) {
      //     mergedMap[key] = { ...row }
      //   } else {
      //     // Merge utility properties
      //     if (row.utilityGenerated) {
      //       mergedMap[key].utilityGenerated = row.utilityGenerated
      //     }
      //     if (row.utilityDistributed) {
      //       mergedMap[key].utilityDistributed = row.utilityDistributed
      //     }
      //   }
      // })

      // const rowsWithIds = Object.values(mergedMap).map((row, index) => ({
      const rowsWithIds = res?.powerResponse?.map((row, index) => ({
        ...row,
        id: row.id || index + 1,
      }))

      setRows(rowsWithIds)
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
      fetchShutdownAndOperationalData();
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
          saveChanges={saveChanges}
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
