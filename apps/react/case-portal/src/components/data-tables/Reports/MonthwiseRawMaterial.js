import { Box } from '@mui/material'
// import DataGridTable from '../ASDataGrid'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import {
  Backdrop,
  CircularProgress,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { renderTwoLineEllipsis } from 'components/Utilities/twoLineEllipsisRenderer'
import Notification from 'components/Utilities/Notification'

const MonthwiseRawMaterial = () => {
  const keycloak = useSession()
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [normRows, setNormRows] = useState({})

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const formatValueToThreeDecimals = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(3) : ''
  }
  const columnDefs = [
    { field: 'id', headerName: 'ID' },
    // {
    //   field: 'Particulars',
    //   headerName: 'Type',
    //   groupable: true,
    //   flex: 2,
    //   renderCell: (params) => (
    //     <div
    //       style={{
    //         whiteSpace: 'normal',
    //         wordBreak: 'break-word',
    //         lineHeight: 1.4,
    //       }}
    //     >
    //       <strong>{params.value}</strong>
    //     </div>
    //   ),
    // },
    {
      field: 'particulars',
      headerName: 'Particulars',
      flex: 2,
      renderCell: renderTwoLineEllipsis,
    },
    {
      field: 'unit',
      headerName: 'Unit',
      editable: false,
      align: 'left',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'spec',
      headerName: 'Spec',
      editable: false,
      align: 'left',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'april',
      headerName: headerMap[4],

      align: 'right',
      headerAlign: 'left',
      flex: 1,
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'may',
      headerName: headerMap[5],
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'june',
      headerName: headerMap[6],

      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'july',
      headerName: headerMap[7],

      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'august',
      headerName: headerMap[8],

      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'september',
      headerName: headerMap[9],

      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'october',
      headerName: headerMap[10],

      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'november',
      headerName: headerMap[11],

      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'december',
      headerName: headerMap[12],

      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'january',
      headerName: headerMap[1],

      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'february',
      headerName: headerMap[2],

      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'march',
      headerName: headerMap[3],

      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'total',
      headerName: 'Total',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
  ]

  const defaultCustomHeight = { mainBox: 'fit-content', otherBox: '100%' }
  const defaultCustomHeightGrid2 = { mainBox: '36vh', otherBox: '100%' }

  //api call
  const [row, setRow] = useState()
  const [row2, setRow2] = useState()
  const [loading, setLoading] = useState(false)
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const fetchData = async () => {
    try {
      setLoading(true)

      const label = `FY ${year} AOP`

      var res = await DataService.getAnnualCostAopReport(
        keycloak,
        'quantity',
        label,
      )
      var res2 = await DataService.getMonthwiseRawData(keycloak)

      // FY%202025-26%20AOP
      // console.log(res)
      if (res2?.code == 200) {
        res2 = res2?.data?.consumptionSummary.map((item, index) => ({
          ...item,
          id: index,
          isEditable: false,
        }))

        setRow2(res2)
      }
      if (res?.code == 200) {
        res = res?.data?.map((item, index) => ({
          ...item,
          id: index,
          isEditable: false,
        }))

        const formattedItems = res.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
        }))

        setRow(formattedItems)

        // Step 2: Group by `norms`
        const groupedByNorms = formattedItems.reduce((acc, item) => {
          const key = item?.norm
          if (!acc[key]) {
            acc[key] = []
          }
          acc[key].push(item)
          return acc
        }, {})

        // Add total row per group
        const groupedWithTotals = {}

        for (const [norm, items] of Object.entries(groupedByNorms)) {
          const totalRow = { particulars: 'Total', norm }

          for (const item of items) {
            for (const [key, value] of Object.entries(item)) {
              if (
                key !== 'particulars' &&
                key !== 'norm'
                // &&
                // typeof value === 'number'
              ) {
                totalRow[key] = (totalRow[key] || 0) + value
              }
            }
          }

          groupedWithTotals[norm] = [...items, totalRow]
        }

        // Set dynamic state
        setNormRows(groupedWithTotals)

        // const groupedRows = []
        // const groups = new Map()
        // let groupId = 0

        // res.forEach((item) => {
        //   const groupKey = item.norm

        // if (!groups.has(groupKey)) {
        //   groups.set(groupKey, [])
        //   groupedRows.push({
        //     id: groupId++,
        //     Particulars: groupKey,
        //     isGroupHeader: true,
        //   })
        // }

        // groups.get(groupKey).push(formattedItem)
        // groupedRows.push(formattedItem)
        // })

        // console.log(groupedRows)
        // setRow(groupedRows)

        // setRow(res)
        // setRow(res)
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

  const columns = [
    { field: 'id', headerName: 'ID' },
    {
      field: 'parameter',
      headerName: 'Parameters',
      editable: false,
      flex: 2,
    },
    {
      field: 'april',
      headerName: headerMap[4],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'may',
      headerName: headerMap[5],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'june',
      headerName: headerMap[6],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'july',
      headerName: headerMap[7],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'aug',
      headerName: headerMap[8],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'sep',
      headerName: headerMap[9],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'oct',
      headerName: headerMap[10],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'nov',
      headerName: headerMap[11],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'dec',
      headerName: headerMap[12],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'jan',
      headerName: headerMap[1],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'feb',
      headerName: headerMap[2],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
    {
      field: 'march',
      headerName: headerMap[3],
      editable: false,
      align: 'right',
      headerAlign: 'left',
      flex: 1,
    },
  ]

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

  return (
    <Box>
      <ReportDataGrid
        rows={row2}
        columns={columns}
        loading={loading}
        handleCalculate={handleCalculate}
        title='Month Wise Raw Data'
        permissions={{
          // customHeight: defaultCustomHeight,
          // showWorkFlowBtns: flase,
          showCalculate: true,
          allAction: true,
          showTitle: true,
        }}
      />

      {Object.entries(normRows).map(([normName, rows]) => (
        <div key={normName}>
          <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
            {normName}
          </Typography>
          <ReportDataGrid
            rows={rows}
            title='Monthwise Production Summary'
            columns={columnDefs}
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
