import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '@mui/material'

const SaveConfirmationDialog = ({
  openSaveDialogeBox,
  closeSaveDialogeBox,
  saveConfirmation,
}) => {
  return (
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
  )
}

export default SaveConfirmationDialog