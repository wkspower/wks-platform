import React from 'react'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '../../../../../../node_modules/@mui/material/index'

const RevConfirmDialog = ({
  openConfirmDialogRev,
  handleCloseDialogRev,
  handleConfirmLoadRev,
}) => {
  return (
    <Dialog
      open={openConfirmDialogRev}
      onClose={handleCloseDialogRev}
      aria-labelledby='alert-dialog-title'
      aria-describedby='alert-dialog-description'
      disableScrollLock
    >
      <DialogTitle id='alert-dialog-title'>{'Change?'}</DialogTitle>
      <DialogContent>
        <DialogContentText
          id='alert-dialog-description'
          sx={{ color: 'text.primary' }}
        >
          Are you sure you want to change the Revision?
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleCloseDialogRev}>Cancel</Button>
        <Button onClick={handleConfirmLoadRev} autoFocus>
          Change
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default RevConfirmDialog
