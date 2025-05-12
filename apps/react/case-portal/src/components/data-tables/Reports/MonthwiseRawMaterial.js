import { Box } from '@mui/material'
// import DataGridTable from '../ASDataGrid'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid2'
import {
  Backdrop,
  CircularProgress,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import ProductionNorms from '../ProductionNorms'
import { useEffect, useMemo, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'

const MonthwiseRawMaterial = () => {
  const keycloak = useSession()
  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const formatValueToThreeDecimals = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(3) : ''
  }
  const columnDefs = [
    { field: 'id', headerName: 'ID' },
    {
      field: 'Particulars',
      headerName: 'Type',
      groupable: true,
      flex: 2,
      renderCell: (params) => (
        <div
          style={{
            whiteSpace: 'normal',
            wordBreak: 'break-word',
            lineHeight: 1.4,
          }}
        >
          <strong>{params.value}</strong>
        </div>
      ),
    },
    { field: 'particulars', headerName: 'Particulars', flex: 3 },
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
  useEffect(() => {
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

          // console.log(groupedRows)
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
          flex: 2,
          filterOperators: [{ label: 'contains', value: 'contains' }],
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
    <Box>
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
          // textAlignment: 'center',
        }}
      />
    </Box>
  )
}

export default MonthwiseRawMaterial
