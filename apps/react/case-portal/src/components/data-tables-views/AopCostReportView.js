import { Box } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { DataGrid } from '@mui/x-data-grid'

import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'

const AopCostReportView = ({ rows, cols, height }) => {
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [loading, setLoading] = useState(false)

  // const fetchData1 = async () => {
  //   try {
  //     setLoading(true)
  //     const data = await DataService.getAnnualCostAopReport(keycloak)
  //     if (data?.code === 200) {
  //       const rowsWithId = data.data.map((item, index) => ({
  //         ...item,
  //         id: index,
  //       }))
  //       setRows(rowsWithId)
  //       setLoading(false)
  //       console.error('Error fetching data')
  //     } else {
  //       setLoading(false)
  //     }

  //     setLoading(false)
  //   } catch (error) {
  //     console.error('Error fetching data:', error)
  //     setLoading(false)
  //   }
  // }

  useEffect(() => {
    // fetchData1()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  return (
    <Box
      sx={{
        height: height || '240px',
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
        columns={cols.map((col) => ({
          ...col,
          filterable: true,
          // sortable: true,
        }))}
        // disableColumnMenu
        // disableColumnFilter
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

export default AopCostReportView
