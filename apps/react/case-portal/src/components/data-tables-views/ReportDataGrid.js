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
  DialogContentText,
  DialogTitle,
  Stack,
  TextField,
  Typography,
} from '../../../node_modules/@mui/material/index'
import AuditTrail from 'components/data-tables/AOPWorkFlow/AuditTrail'
// import Notification from 'components/Utilities/Notification'

const ReportDataGrid = ({
  title = '',
  rows,
  setRows,
  columns,
  modifiedCells = {},
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
  saveWorkflowData = () => {},
  saveRemarkData = () => {},
  createCase = () => {},
  isCreatingCase = false,
  showCreateCasebutton = false,
  openAuditPopup = false,
  handleAuditOpen = () => {},
  handleAuditClose = () => {},
  handleRejectClick = () => {},
  openRejectDialog = false,
  handleRejectCancel = () => {},
  handleSubmit = () => {},
  taskId = '',
  // role = '',
  businessKey = '',
  text = '',
  setText = () => {},
}) => {
  const keycloak = useSession()
  // const [allProducts, setAllProducts] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  // const [loading, setLoading] = useState(false)

  useEffect(() => {}, [
    sitePlantChange,
    oldYear,
    yearChanged,
    keycloak,
    lowerVertName,
  ])
  const handleRemarkSave = () => {
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
  const saveModalOpen = async () => {
    setIsButtonDisabled(true)
    setOpenSaveDialogeBox(true)
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }
  const saveConfirmation = async () => {
    permissions?.saveBtnForRemark ? saveRemarkData() : saveWorkflowData()
    setOpenSaveDialogeBox(false)
  }

  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: 100,
  })
  const lastColumnField = columns[columns.length - 1]?.field
  return (
    <Box
      sx={{
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
        <Box
          className='action-box2'
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            width: '100%', // make sure container is full width
            p: 1,
          }}
        >
          {/* LEFT: Title � this flexGrow:1 makes it push buttons right */}
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              flexGrow: 1, // ? key to take up all left space
            }}
          >
            {permissions?.showTitle && (
              <Typography
                component='div'
                className='grid-title'
                style={{ whiteSpace: 'pre-line' }}
              >
                {title?.replace(/\\n/g, '\n')}
              </Typography>
            )}
          </Box>

          {/* RIGHT: Buttons */}
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

            {/* {permissions?.showWorkFlowBtns && (
              <Stack direction='row' spacing={1} alignItems='center'>
                {taskId && (
                  <Button
                    variant='contained'
                    onClick={handleRejectClick}
                    disabled={isButtonDisabled}
                  >
                    Accept
                  </Button>
                )}
                <Button variant='outlined' onClick={handleAuditOpen}>
                  Audit Trail
                </Button>
              </Stack>
            )} */}
          </Box>
        </Box>
      )}

      <DataGrid
        autoHeight={true}
        rows={rows || []}
        className='custom-data-grid'
        columns={columns.map((col) => ({
          ...col,
          cellClassName: (params) => {
            const modsForRow = modifiedCells[params.row.id] || []
            if (modsForRow.includes(params.field)) {
              return 'red-cell'
            }

            if (col.isDisabled && !params.row.Particulars) {
              return 'disabled-cell'
            }

            if (
              permissions?.remarksEditable &&
              params.row.isEditable === false &&
              col.field !== lastColumnField
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
        getRowClassName={(params) => {
          const classes = []

          if (permissions?.isOldYear == 1) {
            classes.push('odd-row-disabled')
          }

          if (params.row.Particulars || params.row.Particulars2) {
            classes.push('no-border-row')
          }

          if (
            params.row.isEditable === false &&
            !permissions?.remarksEditable
          ) {
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
        rowModesModel={rowModesModel}
        onRowModesModelChange={onRowModesModelChange}
        // paginationModel={{ pageSize: 100, page: 0 }}
        pageSizeOptions={[]}
        paginationModel={paginationModel}
        onPaginationModelChange={(model) => setPaginationModel(model)}
        pagination
        hideFooter={rows?.length <= 100}
        // handleCalculate={handleCalculate}
        sx={{
          '& .pinned-row': {
            position: 'sticky',
            bottom: 0,
            bgcolor: 'background.paper', // keep it opaque
            fontWeight: 'bold',
            zIndex: 1, // sit above normal rows
          },
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
          // '& .MuiDataGrid-cellEmpty': {
          //   display: 'none',
          // },
        }}
      />
      <Box
        sx={{
          marginTop: 2,
          display: 'flex',
          gap: 2,
        }}
      >
        {permissions?.saveBtn && (
          <Button
            variant='contained'
            className='btn-save'
            onClick={saveModalOpen}
            disabled={isButtonDisabled}
            // loading={loading}
            // loadingposition='start'
            {...(loading ? {} : {})}
          >
            Save
          </Button>
        )}
        {permissions?.showCreateCasebutton && (
          <Button
            variant='contained'
            onClick={createCase}
            disabled={isCreatingCase || !showCreateCasebutton}
            className='btn-save'
          >
            {isCreatingCase ? 'Submitting…' : 'Submit'}
          </Button>
        )}
      </Box>
      {/* Reject Dialog (Comments) */}
      <Dialog open={openRejectDialog} onClose={handleRejectCancel}>
        <DialogTitle>Please provide remarks on the changes?</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin='dense'
            label='Remark'
            type='text'
            fullWidth
            multiline
            rows={8}
            sx={{ width: '100%', minWidth: '600px' }}
            value={text}
            onChange={(e) => setText(e.target.value)}
            variant='outlined'
          />
        </DialogContent>
        <DialogActions sx={{ justifyContent: 'flex-end' }}>
          <Button onClick={handleRejectCancel} color='primary'>
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            color='primary'
            variant='contained'
            disabled={!text?.trim()}
          >
            Submit
          </Button>
        </DialogActions>
      </Dialog>

      {/* Audit Trail Dialog */}
      <Dialog
        open={openAuditPopup}
        onClose={handleAuditClose}
        maxWidth='lg'
        fullWidth
      >
        {/* <Notification
              open={snackbarOpen}
              message={snackbarData.message}
              severity={snackbarData.severity}
              onClose={() => setSnackbarOpen(false)}
            /> */}
        <DialogTitle>Audit Trail</DialogTitle>
        <DialogContent dividers>
          <AuditTrail keycloak={keycloak} businessKey={businessKey} />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleAuditClose}>Close</Button>
        </DialogActions>
      </Dialog>
      <Dialog
        open={openSaveDialogeBox}
        onClose={() => setOpenSaveDialogeBox(false)}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Save ?'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure you want to save these changes?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenSaveDialogeBox(false)}>Cancel</Button>
          <Button onClick={saveConfirmation} autoFocus>
            Save
          </Button>
        </DialogActions>
      </Dialog>
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
