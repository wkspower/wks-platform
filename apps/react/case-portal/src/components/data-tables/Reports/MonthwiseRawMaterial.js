import { Box } from '@mui/material'
// import DataGridTable from '../ASDataGrid'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
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
  const columns1 = [
    { field: 'id', headerName: 'ID' },
    {
      field: 'aopCaseId',
      headerName: 'Case ID',
      minWidth: 120,
      editable: false,
    },
    { field: 'aopType', headerName: 'Type', minWidth: 80 },
    { field: 'aopYear', headerName: 'Year', minWidth: 80 },
    { field: 'plantFkId', headerName: 'Plant ID', minWidth: 80 },
    {
      field: 'parameters',
      headerName: 'Particulars',
      editable: false,
      minWidth: 125,
      filterOperators: [{ label: 'contains', value: 'contains' }],
    },
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
    // { field: 'averageTPH', headerName: 'Total', editable: false },
    // { field: 'isEditable', headerName: 'isEditable' },
    { field: 'aopStatus', headerName: 'aopStatus', editable: false },
    { field: 'remark', headerName: 'Remark', editable: true },
  ]
  const rows1 = [
    {
      id: 1,
      aopCaseId: 'CASE-2001',
      aopType: 'RawMaterial',
      aopYear: '2025-26',
      plantFkId: 'PLANT-A',
      parameters: 'EOE Feedstock',
      unit: 'MT',
      spec: '≥99.5%',
      april: 1200,
      may: 1150,
      june: 1180,
      july: 1220,
      aug: 1190,
      sep: 1210,
      oct: 1230,
      nov: 1175,
      dec: 1240,
      jan: 1250,
      feb: 1225,
      march: 1215,
      aopStatus: 'Open',
      remark: 'Check purity',
    },
    {
      id: 2,
      aopCaseId: 'CASE-2002',
      aopType: 'RawMaterial',
      aopYear: '2025-26',
      plantFkId: 'PLANT-B',
      parameters: 'MEG Feedstock',
      unit: 'MT',
      spec: '≥99.0%',
      april: 900,
      may: 950,
      june: 920,
      july: 940,
      aug: 930,
      sep: 910,
      oct: 925,
      nov: 935,
      dec: 945,
      jan: 955,
      feb: 965,
      march: 975,
      aopStatus: 'Closed',
      remark: '',
    },
    {
      id: 3,
      aopCaseId: 'CASE-2003',
      aopType: 'RawMaterial',
      aopYear: '2025-26',
      plantFkId: 'PLANT-C',
      parameters: 'EO Feedstock',
      unit: 'MT',
      spec: '≥98.5%',
      april: 800,
      may: 820,
      june: 810,
      july: 830,
      aug: 840,
      sep: 850,
      oct: 860,
      nov: 870,
      dec: 880,
      jan: 890,
      feb: 900,
      march: 910,
      aopStatus: 'Open',
      remark: 'Low stock in June',
    },
  ]

  const defaultCustomHeight = { mainBox: '38vh', otherBox: '100%' }

  // //api call
  // const [rows, setRows] = useState()
  // const [loading, setLoading] = useState(false)
  // const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  // const year = localStorage.getItem('year')
  // useEffect(() => {
  //   const fetchData = async () => {
  //     try {
  //       setLoading(true)
  //       var res = await DataService.getMonthWiseSummary(keycloak)

  //       console.log(res)
  //       if (res?.code == 200) {
  //         res = res?.data?.data.map((item, index) => ({
  //           ...item,
  //           id: index,
  //         }))

  //         setRow(res)
  //       } else {
  //         setRow([])
  //       }
  //     } catch (err) {
  //       console.log(err)
  //     } finally {
  //       setLoading(false)
  //     }
  //   }
  //   fetchData()
  // }, [year, plantId])

  const dummyAPI1 = {
    status: 200,
    message: 'OK',
    data: {
      columns: [
        { field: 'id', headerName: 'ID' },
        {
          field: 'parameters',
          headerName: 'Parameters',
          editable: false,
          minWidth: 225,
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
      {/* <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop> */}

      <ReportDataGrid
        rows={rows}
        columns={columns}
        permissions={{
          customHeight: defaultCustomHeight,
        }}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        {' '}
        Raw Materials
      </Typography>
      <ReportDataGrid
        rows={rows1}
        title='Monthwise Production Summary'
        columns={columns1}
        permissions={{
          customHeight: defaultCustomHeight,
          textAlignment: 'center',
        }}
        // treeData
        // getTreeDataPath={(row) => row.path}
        // defaultGroupingExpansionDepth={1} // expand only first level by default
        // disableSelectionOnClick
        // columnGroupingModel={columnGroupingModel}
        // experimentalFeatures
      />
    </Box>
  )
}

export default MonthwiseRawMaterial
