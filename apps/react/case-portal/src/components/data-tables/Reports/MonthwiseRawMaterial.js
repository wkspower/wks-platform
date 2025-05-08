import { Box } from '@mui/material'
// import DataGridTable from '../ASDataGrid'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid2'
import {
  Backdrop,
  CircularProgress,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import ProductionNorms from '../ProductionNorms'
import { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

const MonthwiseRawMaterial = () => {
  const keycloak = useSession()

  const formatValueToThreeDecimals = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(3) : ''
  }
  const columnDefs = [
    { field: 'id', headerName: 'ID' },
    {
      field: 'Particulars',
      headerName: 'Type',
      groupable: true,
      flex: 3,
      renderCell: (params) => <strong>{params.value}</strong>,
    },
    { field: 'particulars', headerName: 'Particulars', flex: 3 },
    {
      field: 'unit',
      headerName: 'Unit',
      editable: false,
      align: 'left',

      headerAlign: 'left',
    },
    {
      field: 'spec',
      headerName: 'Spec',
      editable: false,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'april',
      headerName: 'Apr-25',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'may',
      headerName: 'May-25',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'june',
      headerName: 'Jun-25',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'july',
      headerName: 'Jul-25',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'august',
      headerName: 'Aug-25',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'september',
      headerName: 'Sep-25',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'october',
      headerName: 'Oct-25',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'november',
      headerName: 'Nov-25',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'december',
      headerName: 'Dec-25',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'january',
      headerName: 'Jan-26',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'february',
      headerName: 'Feb-26',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'march',
      headerName: 'Mar-26',
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'total',
      headerName: 'Total',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },
  ]

  const defaultCustomHeight = { mainBox: '32vh', otherBox: '100%' }
  const defaultCustomHeightGrid2 = { mainBox: '40vh', otherBox: '100%' }

  //api call
  const [row, setRow] = useState()
  const [row2, setRow2] = useState()
  const [loading, setLoading] = useState(false)
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)
        var res = await DataService.getAnnualCostAopReport(
          keycloak,
          'quantity',
          'FY 2025-26 AOP',
        )
        var res2 = await DataService.getMonthwiseRawData(keycloak)

        // FY%202025-26%20AOP
        console.log(res)
        if (res2?.code == 200) {
          res2 = res2?.data?.consumptionSummary.map((item, index) => ({
            ...item,
            id: index,
          }))

          setRow2(res2)
        }
        if (res?.code == 200) {
          res = res?.data?.map((item, index) => ({
            ...item,
            id: index,
          }))

          const groupedRows = []
          const groups = new Map()
          let groupId = 0

          res.forEach((item) => {
            const groupKey = item.norm

            if (!groups.has(groupKey)) {
              groups.set(groupKey, [])
              groupedRows.push({
                id: groupId++,
                Particulars: groupKey,
                isGroupHeader: true,
              })
            }
            const formattedItem = {
              ...item,
              idFromApi: item.id,
              // originalRemark: item.remarks,
              id: groupId++,
            }

            groups.get(groupKey).push(formattedItem)
            groupedRows.push(formattedItem)
          })

          console.log(groupedRows)
          setRow(groupedRows)

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

    fetchData()
  }, [year, plantId])

  const dummyAPI1 = {
    status: 200,
    message: 'OK',
    data: {
      columns: [
        { field: 'id', headerName: 'ID' },
        {
          field: 'parameter',
          headerName: 'Parameters',
          editable: false,
          flex: 1,
          filterOperators: [{ label: 'contains', value: 'contains' }],
        },
        {
          field: 'april',
          headerName: 'Apr-25',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
        {
          field: 'may',
          headerName: 'May-25',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
        {
          field: 'june',
          headerName: 'Jun-25',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
        {
          field: 'july',
          headerName: 'Jul-25',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
        {
          field: 'aug',
          headerName: 'Aug-25',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
        {
          field: 'sep',
          headerName: 'Sep-25',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
        {
          field: 'oct',
          headerName: 'Oct-25',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
        {
          field: 'nov',
          headerName: 'Nov-25',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
        {
          field: 'dec',
          headerName: 'Dec-25',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
        {
          field: 'jan',
          headerName: 'Jan-26',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
        {
          field: 'feb',
          headerName: 'Feb-26',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
        {
          field: 'march',
          headerName: 'Mar-26',
          editable: false,
          align: 'right',
          headerAlign: 'left',
        },
      ],
      rows: [
        {
          id: '1',
          parameters: 'Budgeted Selectivity for current year',
          april: 92,
          may: 95,
          june: 94,
          july: 93,
          aug: 91,
          sep: 96,
          oct: 94,
          nov: 92,
          dec: 95,
          jan: 93,
          feb: 94,
          march: 96,
        },
        {
          id: '2',
          parameters: 'Actual selectivity for current year',
          april: 88,
          may: 90,
          june: 89,
          july: 87,
          aug: 91,
          sep: 90,
          oct: 89,
          nov: 88,
          dec: 90,
          jan: 89,
          feb: 90,
          march: 92,
        },
        {
          id: '3',
          parameters: 'Guaranteed Selectivity for budget year',
          april: 85,
          may: 87,
          june: 86,
          july: 84,
          aug: 88,
          sep: 87,
          oct: 86,
          nov: 85,
          dec: 87,
          jan: 86,
          feb: 87,
          march: 89,
        },
        {
          id: '4',
          parameters: 'Predicted Selectivity for budget year',
          april: 90,
          may: 92,
          june: 91,
          july: 89,
          aug: 93,
          sep: 92,
          oct: 91,
          nov: 90,
          dec: 92,
          jan: 91,
          feb: 92,
          march: 94,
        },
      ],
    },
  }
  const {
    data: { columns, rows },
  } = dummyAPI1

  return (
    <Box sx={{ height: 500, width: '100%' }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <ReportDataGrid
        rows={row2}
        columns={columns}
        permissions={{
          customHeight: defaultCustomHeight,
        }}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        {' '}
      </Typography>
      <ReportDataGrid
        rows={row}
        title='Monthwise Production Summary'
        columns={columnDefs}
        permissions={{
          customHeight: defaultCustomHeightGrid2,
          textAlignment: 'center',
        }}
      />
    </Box>
  )
}

export default MonthwiseRawMaterial
