import { Box } from '@mui/material'

import { DataGrid } from '@mui/x-data-grid'

import { useEffect } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
} from '../../../node_modules/@mui/material/index'

const ReportDataGrid = ({
  rows,
  setRows,
  columns,
  height,
  treeData,
  getTreeDataPath,
  defaultGroupingExpansionDepth,
  columnGroupingModel,
  permissions,
  processRowUpdate = (row) => row,

  remarkDialogOpen = false,
  setRemarkDialogOpen = () => {},
  currentRemark = '',
  setCurrentRemark = () => {},
  currentRowId = null,
  unsavedChangesRef = { current: { unsavedRows: {}, rowsBeforeChange: {} } },
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
  const handleRemarkSave = () => {
    console.log(currentRemark)
    setRows((prevRows) => {
      let updatedRow = null

      const updatedRows = prevRows.map((row) => {
        if (row.id === currentRowId) {
          const keysToUpdate = [
            'aopRemarks',
            'remarks',
            'remark',
            'Remark',
          ].filter((key) => key in row)
          console.log(row)
          const keyToUpdate = keysToUpdate[0] || 'remark'
          //          console.log([keyToUpdate])
          updatedRow = { ...row, [keyToUpdate]: currentRemark }
          return updatedRow
        }
        return row
      })

      if (updatedRow) {
        unsavedChangesRef.current.unsavedRows[currentRowId] = updatedRow
      }

      return updatedRows
    })

    setRemarkDialogOpen(false)
  }

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
        processRowUpdate={processRowUpdate}
        disableColumnSeparator
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
          const classes = []
          // if () {
          //   return '' // No class for remark column
          // }
          if (params.row.isEditable === false) {
            return [
              ...classes,
              permissions?.noColor === true ? 'even-row' : 'odd-row',
            ].join(' ')
          }

          return [...classes, 'even-row'].join(' ')
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
          // '& .MuiDataGrid-cell--textRight': {
          //   textAlign: 'left',
          // },
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
      <Dialog
        open={!!remarkDialogOpen}
        onClose={() => setRemarkDialogOpen(false)}
      >
        <DialogTitle>Add Remark</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin='dense'
            id='remark'
            label='Remark'
            type='text'
            fullWidth
            variant='outlined'
            sx={{ width: '100%', minWidth: '600px' }}
            value={currentRemark || ''}
            // value={remark}
            onChange={(e) => setCurrentRemark(e.target.value)}
            multiline
            rows={8}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRemarkDialogOpen(false)}>Cancel</Button>
          {/* <Button onClick={handleCloseRemark}>Cancel</Button> */}
          <Button onClick={handleRemarkSave} disabled={!currentRemark?.trim()}>
            Add
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default ReportDataGrid
