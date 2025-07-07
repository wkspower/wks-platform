import { Box } from '@mui/material'
// import DataGridTable from '../ASDataGrid'
// import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import {
  Backdrop,
  CircularProgress,
  // Tooltip,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
// import { renderTwoLineEllipsis } from 'components/Utilities/twoLineEllipsisRenderer'
import Notification from 'components/Utilities/Notification'
// import KendoDataTables from 'components/kendo-data-tables/index'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { validateFields } from 'utils/validationUtils'

const MonthwiseRawMaterial = () => {
  const keycloak = useSession()
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [normRows, setNormRows] = useState({})
  const [rows, setRows] = useState()

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  // const formatValueToThreeDecimals = (params) => {
  //   return params === 0 ? 0 : params ? parseFloat(params).toFixed(0) : ''
  // }
  // const formatValueToThreeDecimals4 = (params) => {
  //   return params === 0 ? 0 : params ? parseFloat(params).toFixed(4) : ''
  // }
  // const formatValueToThreeDecimals2 = (params) => {
  //   return params === 0 ? 0 : params ? parseFloat(params).toFixed(2) : ''
  // }
  const columnDefs = [
    { field: 'id', headerName: 'ID', editable: false },

    {
      field: 'material',
      headerName: 'Particulars',
      flex: 2,
      editable: false,
    },
    {
      field: 'UOM',
      headerName: 'UOM',
      editable: false,
      align: 'left',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'spec',
      headerName: 'Spec',
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    {
      field: 'april',
      headerName: headerMap[4],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    {
      field: 'may',
      headerName: headerMap[5],
      align: 'right',
      headerAlign: 'left',
      editable: false,
      type:'number'
    },
    {
      field: 'june',
      headerName: headerMap[6],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      type:'number'
    },
    {
      field: 'july',
      headerName: headerMap[7],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      type:'number'
    },
    {
      field: 'aug',
      headerName: headerMap[8],

      align: 'right',
      headerAlign: 'left',
      editable: false,
      type:'number'
    },
    {
      field: 'sep',
      headerName: headerMap[9],

      align: 'right',
      headerAlign: 'left',
      editable: false,
      type:'number'
    },
    {
      field: 'oct',
      headerName: headerMap[10],

      align: 'right',
      headerAlign: 'left',
      editable: false,
      type:'number'
    },
    {
      field: 'nov',
      headerName: headerMap[11],

      align: 'right',
      headerAlign: 'left',
      editable: false,
      type:'number'
    },
    {
      field: 'dec',
      headerName: headerMap[12],

      align: 'right',
      headerAlign: 'left',
      editable: false,
      type:'number'
    },
    {
      field: 'jan',
      headerName: headerMap[1],

      align: 'right',
      headerAlign: 'left',
      editable: false,
      type:'number'
    },
    {
      field: 'feb',
      headerName: headerMap[2],

      align: 'right',
      headerAlign: 'left',
      editable: false,
      type:'number'
    },
    {
      field: 'march',
      headerName: headerMap[3],

      align: 'right',
      headerAlign: 'left',
      editable: false,
      type:'number'
    },
    {
      field: 'total',
      headerName: 'Total',
      align: 'right',
      editable: false,
      type:'number'
    },
  ]
  const columns = [
    { field: 'id', headerName: 'ID' },
    {
      field: 'material',
      headerName: 'Parameters',
      editable: false,
      flex: 2,
      type:'number'
    },
    {
      field: 'april',
      headerName: headerMap[4],
      editable: false,
      align: 'right',
      headerAlign: 'left',

      flex: 1,
      type:'number'
    },
    {
      field: 'may',
      headerName: headerMap[5],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    {
      field: 'june',
      headerName: headerMap[6],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    {
      field: 'july',
      headerName: headerMap[7],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    {
      field: 'aug',
      headerName: headerMap[8],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,

      type:'number'
    },
    {
      field: 'sep',
      headerName: headerMap[9],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    {
      field: 'oct',
      headerName: headerMap[10],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    {
      field: 'nov',
      headerName: headerMap[11],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    {
      field: 'dec',
      headerName: headerMap[12],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    {
      field: 'jan',
      headerName: headerMap[1],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    {
      field: 'feb',
      headerName: headerMap[2],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    {
      field: 'march',
      headerName: headerMap[3],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
      type:'number'
    },
    // {
    //   field: 'Remark',
    //   headerName: 'Remark',
    //   editable: false,
    //   align: 'left',
    //   headerAlign: 'left',
    //   flex: 2,
    // },
  ]

  const [row, setRow] = useState()
  const [row2, setRow2] = useState()
  const [loading, setLoading] = useState(false)
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const [modifiedCells, setModifiedCells] = React.useState({})

  const fetchData = async () => {
    try {
      setLoading(true)
      var res = await DataService.getMonthwiseRawData(keycloak, 'NormQuantity')
      var res2 = await DataService.getMonthwiseRawData(keycloak, 'Selectivity')

      if (res2?.code == 200) {
        res2 = res2?.data?.consumptionSummary.map((item, index) => ({
          ...item,
          id: index,
          idFromApi: item.id,
          isEditable: true,
          originalRemark: item.Remark || '',
        }))
        // console.log("data is ",res2);
        setRow2(res2)
      }

      if (res?.code == 200) {
        res = res?.data?.consumptionSummary.map((item, index) => ({
          ...item,
          id: index,
          //idFromApi: item.id,
          originalRemark: item.Remark || '',
        }))

        const formattedItems = res.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
        }))

        setRow(formattedItems)

        // Group by normType and add Total row to each group
        const groupedByNorms = formattedItems.reduce((acc, item) => {
          const key = item?.normType
          if (!acc[key]) {
            acc[key] = []
          }
          acc[key].push(item)
          return acc
        }, {})

        // // Add Total row per group
        // Object.keys(groupedByNorms).forEach((key) => {
        //   const group = groupedByNorms[key]

        //   // List of columns to total
        //   const monthColumns = [
        //     'april',
        //     'may',
        //     'june',
        //     'july',
        //     'aug',
        //     'sep',
        //     'oct',
        //     'nov',
        //     'dec',
        //     'jan',
        //     'feb',
        //     'march',
        //     'total',
        //   ]

        //   const totalRow = {
        //     id: `total-${key}`,
        //     material: 'Total',
        //     normType: key,
        //     spec: '',
        //     UOM: '',
        //     isEditable: false,
        //   }

        //   // Sum values for each month column
        //   monthColumns.forEach((col) => {
        //     totalRow[col] = group.reduce(
        //       (sum, item) => sum + (Number(item[col]) || 0),
        //       0,
        //     )
        //   })

        //   // Add to group
        //   group.push(totalRow)
        // })

        setNormRows(groupedByNorms)
      } else {
        setRow([])
      }
    } catch (err) {
      console.log(err)
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => {
    fetchData()
  }, [year, plantId])

  const handleCalculate = () => {
    handleCalculateMonthwiseAndTurnaround()
  }
  const handleCalculateMonthwiseAndTurnaround = async () => {
    try {
      setLoading(true)

      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var plantId = plantId
      const res = await DataService.handleCalculatePlantConsumptionData(
        plantId,
        year,
        keycloak,
      )

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refreshed Successfully!',
          severity: 'success',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refreshed Faild!',
          severity: 'error',
        })
      }
      return res
    } catch (error) {
      console.error('Error!', error)
    } finally {
      setLoading(false)
    }
  }

  const [currentRowId, setCurrentRowId] = useState(null)
  const [currentRemark, setCurrentRemark] = useState('')
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.Remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveChanges = async () => {
    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      var data = Object.values(modifiedCells)
      //  console.log('Modified cells before save:', modifiedCells);
      const year = localStorage.getItem('year') // e.g. "2025-26"

      let prevYear = ''
      if (year && year.includes('-')) {
        const [start, end] = year.split('-').map(Number)
        prevYear = `${start - 1}-${(start - 1 + 1).toString().slice(-2)}`
      }
      // console.log('row data', data)
      const rowsToUpdate = data.map((row) => ({
        april: row.april ?? null,
        may: row.may ?? null,
        june: row.june ?? null,
        july: row.july ?? null,
        aug: row.aug ?? null,
        sep: row.sep ?? null,
        oct: row.oct ?? null,
        nov: row.nov ?? null,
        dec: row.dec ?? null,
        jan: row.jan ?? null,
        feb: row.feb ?? null,
        march: row.march ?? null,
        remark: row.Remark ?? null,
        id: row.idFromApi, // support for both camelCase and PascalCase
      }))
      const requiredFields = ['Remark']

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
      const res = await DataService.postMonthwiseRawData(
        keycloak,
        rowsToUpdate,
        plantId,
      )

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Failed!',
          severity: 'error',
        })
      }
    } catch (err) {
      console.error('Error while save', err)
      setSnackbarOpen(true)
      setSnackbarData({ message: err.message, severity: 'error' })
    } finally {
      setSnackbarOpen(true)
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

      <KendoDataTablesReports
        rows={row2}
        columns={columns}
        setRows={setRow2}
        loading={loading}
        handleCalculate={handleCalculate}
        title='Monthwise Consumption (T-18)'
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        permissions={{
          // customHeight: defaultCustomHeight,
          // showWorkFlowBtns: flase,
          showCalculate: false,
          allAction: true,
          showTitle: true,
          // saveBtn: true,
          saveBtn: false,
          textAlignment: 'center',
          remarksEditable: true,
        }}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        saveChanges={saveChanges}
        handleRemarkCellClick={handleRemarkCellClick}
      />

      {Object.entries(normRows).map(([normName, rows]) => (
        <div key={normName}>
          <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
            {normName}
          </Typography>
          <KendoDataTablesReports
            rows={rows}
            setRows={setRows}
            title='Monthwise Production Summary'
            columns={columnDefs}
            handleRemarkCellClick={handleRemarkCellClick}
          />
        </div>
      ))}

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}

export default MonthwiseRawMaterial
