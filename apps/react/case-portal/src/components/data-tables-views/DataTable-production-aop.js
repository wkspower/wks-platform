import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

// import DataGridTable from 'components/data-tables/ASDataGrid'
import { DataGrid } from '@mui/x-data-grid'
import {
  Backdrop,
  Box,
  CircularProgress,
} from '../../../node_modules/@mui/material/index'

const ProductionAopView = () => {
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [columns, setColumns] = useState([])
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const formatValueToNoDecimals = (val) =>
    val && !isNaN(val) ? Math.round(val) : val

  const fetchData = async () => {
    // setLoading(true)
    try {
      var data = await DataService.getWorkflowDataProduction(keycloak, plantId)
      const formattedRows = data.results.map((row, id) => {
        const newRow = { id }
        Object.entries(row).forEach(([key, val]) => {
          if (!isNaN(val) && val !== '') {
            newRow[key] = formatValueToNoDecimals(val)
          } else {
            newRow[key] = val
          }
        })
        return newRow
      })
      setRows(formattedRows)
      const generateColumns = ({ headers, keys }) => {
        return headers.map((header, idx) => ({
          field: keys[idx],
          headerName: header,
          flex: 1,
          ...(idx === 0 && {
            renderHeader: (params) => <div>{params.colDef.headerName}</div>,
          }),
        }))
      }
      setColumns(generateColumns(data))
      // setLoading(false)
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
      setRows([])
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const defaultCustomHeight = { mainBox: '22vh', otherBox: '114%' }

  return (
    <Box
      sx={{
        height: '80px',
        width: '100%',
        padding: '0px 0px',
        margin: '0px 0px 0px',
        backgroundColor: '#F2F3F8',
        borderRadius: 0,
        borderBottom: 'none',
      }}
    >
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <DataGrid
        rows={rows || []}
        className='custom-data-grid'
        columns={columns.map((col) => ({
          ...col,
          filterable: false,
          sortable: true,
        }))}
        disableColumnMenu
        disableColumnFilter
        disableColumnSelector
        disableColumnSorting
        columnVisibilityModel={{
          maintenanceId: false,
          id: false,
          plantFkId: false,
          aopCaseId: false,
          aopType: false,
          aopYear: false,
          avgTph: false,
          NormParameterMonthlyTransactionId: false,
          aopStatus: false,
          idFromApi: false,
          period: false,
        }}
        rowHeight={35}
        slotProps={{
          toolbar: { setRows },
          loadingOverlay: {
            variant: 'linear-progress',
            norowsvariant: 'skeleton',
          },
        }}
        getRowClassName={(params) => {
          return params.row.Particulars || params.row.Particulars2
            ? 'no-border-row'
            : params.indexRelativeToCurrentPage % 2 === 0
              ? 'even-row'
              : 'even-row'
        }}
      />
    </Box>
  )
}

export default ProductionAopView
