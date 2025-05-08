import { Box } from '@mui/material'

import { DataGrid } from '@mui/x-data-grid'

import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import {
  Backdrop,
  Button,
  CircularProgress,
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
  handleCalculate = () => {},
  remarkDialogOpen = false,
  setRemarkDialogOpen = () => {},
  currentRemark = '',
  setCurrentRemark = () => {},
  currentRowId = null,
  unsavedChangesRef = { current: { unsavedRows: {}, rowsBeforeChange: {} } },
  loading,
  rowModesModel: rowModesModel,
  onRowModesModelChange = () => {},
}) => {
  const keycloak = useSession()
  // const [allProducts, setAllProducts] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  // const [loading, setLoading] = useState(false)

  useEffect(() => {
    // fetchData1()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])
  const handleRemarkSave = () => {
    // console.log(currentRemark)
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
          // console.log(row)
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
  const handleCalculateBtn = async () => {
    setIsButtonDisabled(true)
    handleCalculate()
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }

  return (
    <Box
      sx={{
        // height: height || permissions?.customHeight?.mainBox || '240px',
        height: 'auto',
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
      {(permissions?.allAction ?? true) && (
        <Box className='action-box'>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            {permissions?.showCalculate && (
              <Button
                variant='contained'
                onClick={handleCalculateBtn}
                disabled={isButtonDisabled}
                className='btn-save'
              >
                Calculate
              </Button>
            )}
          </Box>
        </Box>
      )}

      <DataGrid
        rows={rows || []}
        className='custom-data-grid'
        columns={columns?.map((col) => ({
          ...col,
          filterable: true,
          editable: (params) => {
            if (
              params.row.isEditable === false &&
              col.field !== 'Remark' &&
              col.field !== 'remarks'
            ) {
              return false
            }
            return true
          },
          cellClassName: (params) => {
            if (
              params.row.isEditable === false &&
              col.field !== 'Remark' &&
              col.field !== 'remarks'
            ) {
              return 'odd-cell'
            }

            return undefined
          },
          headerClassName: col.isDisabled ? 'disabled-header' : undefined,
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
        columnGroupHeaderHeight={20}
        experimentalFeatures={{ newEditingApi: true, columnGrouping: true }}
        columnGroupingModel={columnGroupingModel}
        treeData={treeData}
        getTreeDataPath={getTreeDataPath}
        defaultGroupingExpansionDepth={defaultGroupingExpansionDepth}
        rowModesModel={rowModesModel}
        onRowModesModelChange={onRowModesModelChange}
        handleCalculate={handleCalculate}
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
          '& .MuiDataGrid-cellEmpty': {
            display: 'none',
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
