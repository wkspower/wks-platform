import { Box } from '@mui/material'

import { DataGrid } from '@mui/x-data-grid'

import { useEffect } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'

const ReportDataGrid = ({
  rows,
  columns,
  height,
  treeData,
  getTreeDataPath,
  defaultGroupingExpansionDepth,
  columnGroupingModel,
  permissions,
  // loading
}) => {
  const keycloak = useSession()
  // const [allProducts, setAllProducts] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  // const [loading, setLoading] = useState(false)

  useEffect(() => {
    // fetchData1()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])
  return (
    <Box
      sx={{
        height: height || permissions?.customHeight?.mainBox || '240px',
        width: '100%',
        padding: '0px 0px',
        margin: '0px 0px 0px',
        backgroundColor: '#F2F3F8',
        borderRadius: 0,
        borderBottom: 'none',
      }}
    >
      {/* <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={false}
      >
        <CircularProgress color='inherit' />
      </Backdrop> */}

      <DataGrid
        rows={rows || []}
        className='custom-data-grid'
        columns={columns?.map((col) => ({
          ...col,
          filterable: true,
          // sortable: true,
        }))}
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
        experimentalFeatures={{ newEditingApi: true, columnGrouping: true }}
        columnGroupingModel={columnGroupingModel}
        treeData={treeData}
        getTreeDataPath={getTreeDataPath}
        defaultGroupingExpansionDepth={defaultGroupingExpansionDepth}
        sx={{
          '& .MuiDataGrid-columnHeader': {
            justifyContent: 'left',
          },
          '& .MuiDataGrid-cell--textRight': {
            textAlign: 'left',
          },
          '& .MuiDataGrid-columnHeaderTitleContainer': {
            borderTop: '1px solid rgba(224,224,224,1)',
            justifyContent: `${permissions?.textAlignment}` || 'left',
          },
          '& .MuiDataGrid-columnHeader, .MuiDataGrid-columnGroupHeader': {
            justifyContent: `${permissions?.textAlignment}` || 'left',
            textAlign: `${permissions?.textAlignment}` || 'left',
            padding: '0 8px',
          },
          '& .MuiDataGrid-columnHeader:before, .MuiDataGrid-columnGroupHeader:before':
            {
              display: 'none', // hides the little red triangle corners
            },
          '& .MuiDataGrid-columnGroupHeader': {
            borderBottom: '1px solid rgba(224,224,224,1)',
          },
        }}
      />
    </Box>
  )
}

export default ReportDataGrid
