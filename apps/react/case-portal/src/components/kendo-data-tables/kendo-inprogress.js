import React, { useState } from 'react'
import { Grid, GridColumn as Column } from '@progress/kendo-react-grid'
import { filterBy } from '@progress/kendo-data-query'
import '@progress/kendo-theme-default/dist/all.css'
import PropTypes from 'prop-types'
import '../../kendo-data-grid.css'
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '../../../node_modules/@mui/material/index'
import Notification from 'components/Utilities/Notification'

const KendoDataTables = ({
  rows,
  setRows,
  columns,
  loading = false,
  pageSizes = [10, 20, 50],
  onRowChange,
  disableColor = false,
  permissions = {},
  setSnackbarOpen = () => {},
  snackbarData = { message: '', severity: 'info' },
  snackbarOpen = false,
  unsavedChangesRef = { current: { unsavedRows: {}, rowsBeforeChange: {} } },
  setRemarkDialogOpen = () => {},
  currentRemark = '',
  setCurrentRemark = () => {},
  currentRowId = null,
  // modifiedCells = [],
  remarkDialogOpen = false,
  handleCalculate = () => {},
  fetchData = () => {},
  handleUnitChange = () => {},
  deleteRowData = () => {},
  handleAddPlantSite = () => {},
  selectedUsers = [],
  allRedCell = [],
}) => {
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)

  const handleItemChange = (e) => {
    const updated = rows.map((r) =>
      r.id === e.dataItem.id ? { ...r, [e.field]: e.value } : r,
    )
    onRowChange?.(updated, e)
  }
  const rowRender = disableColor
    ? (trElement, props) => {
        const shouldDisable = props.dataItem.status === 'inactive'
        return React.cloneElement(trElement, {
          ...trElement.props,
          className: `${trElement.props.className || ''} ${shouldDisable ? 'disabled-row' : ''}`,
        })
      }
    : undefined

  const handleRemarkSave = () => {
    setRows((prevRows) => {
      let updatedRow = null

      const updatedRows = prevRows.map((row) => {
        if (row.id === currentRowId) {
          const keysToUpdate = ['aopRemarks', 'remarks', 'remark'].filter(
            (key) => key in row,
          )
          //          console.log(keysToUpdate)
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

  const handleAddRow = () => {
    if (isButtonDisabled) return
    setIsButtonDisabled(true)
    const newRowId = rows.length
      ? Math.max(...rows.map((row) => row.id)) + 1
      : 1
    const newRow = {
      id: newRowId,
      isNew: true,
      ...Object.fromEntries(initialColumns.map((col) => [col.field, ''])),
    }

    setRows((prevRows) => [newRow, ...prevRows])
    onAddRow?.(newRow)
    // setProduct('')
    // setRowModesModel((oldModel) => ({
    //   ...oldModel,
    //   [newRowId]: { mode: GridRowModes.Edit, fieldToFocus: 'discription' },
    // }))
    focusFirstField()
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }

  const handleDeleteClick = async (id, params) => {
    setParamsForDelete(params)
    setOpenDeleteDialogeBox(true)
  }

  return (
    <div style={{ position: 'relative' }}>
      {loading && (
        <div className='k-loading-mask'>
          <span className='k-loading-text'>Loading...</span>
          <div className='k-loading-image' />
          <div className='k-loading-color' />
        </div>
      )}

      <div className='kendo-data-grid'>
        <Grid
          data={filterBy(rows, filter)}
          filterable={true}
          sortable
          dataItemKey='id'
          pageable={{ pageSizes, buttonCount: 5 }}
          editField='inEdit'
          filter={filter}
          onFilterChange={(e) => setFilter(e.filter)}
          onItemChange={handleItemChange}
          // headerRowHeight={showHeader ? 35 : 0}
          rowRender={rowRender}
          resizable={true}
        >
          {columns.map(
            ({ field, title, width, cell, format, filterable = true }) => (
              <Column
                key={field}
                field={field}
                title={title}
                width={width}
                filterable={filterable}
                cell={cell}
                format={format}
              />
            ),
          )}
        </Grid>
      </div>
      {(permissions?.allActionOfBottomBtns ?? true) && (
        <Box
          sx={{
            marginTop: 2,
            display: 'flex',
            gap: 2,
          }}
        >
          {permissions?.addButton && (
            <Button
              variant='contained'
              className='btn-save'
              onClick={handleAddRow}
              disabled={isButtonDisabled}
            >
              Add Item
            </Button>
          )}

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
          {/* {permissions?.showCreateCasebutton && (
            <Button
              variant='contained'
              onClick={createCase}
              disabled={isCreatingCase || !showCreateCasebutton}
              className='btn-save'
            >
              {isCreatingCase ? 'Submitting…' : 'Submit'}
            </Button>
          )} */}

          {permissions?.approveBtn && (
            <Button
              variant='contained'
              className='btn-save'
              onClick={saveModalOpen}
              disabled={isButtonDisabled}
              // loading={loading}
              // loadingposition='start'
              {...(loading ? {} : {})}
            >
              Approve
            </Button>
          )}
          {permissions?.nextBtn && (
            <Button
              variant='contained'
              className='btn-save'
              onClick={() => {
                // Write any additional logic here before navigating.
                // console.log('Navigating to dashboard')
                // navigate('/user-form')
                handleAddPlantSite()
              }}
              disabled={isButtonDisabled}
              loading={loading} // Use the loading prop to trigger loading state
              loadingposition='start' // Use loadingPosition to control where the spinner appears
            >
              Next
            </Button>
          )}
          {showDeleteAll && (
            <Button
              variant='contained'
              className='btn-save'
              onClick={handleDeleteSelected}
              disabled={isButtonDisabled}
              loading={loading} // Use the loading prop to trigger loading state
              loadingposition='start' // Use loadingPosition to control where the spinner appears
            >
              Delete
            </Button>
          )}
        </Box>
      )}

      <Notification
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
      />

      <Dialog
        open={openDeleteDialogeBox}
        onClose={closeDeleteDialogeBox}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Delete ?'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure you want to delete this row?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialogeBox(false)}>Cancel</Button>
          <Button
            onClick={() => {
              deleteRowData(paramsForDelete)
              setOpenDeleteDialogeBox(false)
            }}
            autoFocus
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={openSaveDialogeBox}
        onClose={closeSaveDialogeBox}
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
          <Button onClick={closeSaveDialogeBox}>Cancel</Button>
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
    </div>
  )
}

KendoDataTables.propTypes = {
  rows: PropTypes.array.isRequired,
  disableColor: PropTypes.bool,
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      field: PropTypes.string.isRequired,
      title: PropTypes.string,
      width: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
      cell: PropTypes.func,
      filterable: PropTypes.bool,
    }),
  ).isRequired,
  loading: PropTypes.bool,
  pageSizes: PropTypes.arrayOf(PropTypes.number),
  onAddRow: PropTypes.func,
  onDeleteRow: PropTypes.func,
  onRowChange: PropTypes.func,
  toolbarButtons: PropTypes.arrayOf(
    PropTypes.shape({
      title: PropTypes.string.isRequired,
      onClick: PropTypes.func.isRequired,
      icon: PropTypes.string,
    }),
  ),
}

export default KendoDataTables
