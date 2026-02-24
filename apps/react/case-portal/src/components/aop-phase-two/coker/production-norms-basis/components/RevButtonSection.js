import React, { useState } from 'react'
import {
  Box,
  Button,
  ButtonGroup,
} from '../../../../../../node_modules/@mui/material/index'
import RevConfirmDialog from './RevConfirmDialog'

const RevButtonSection = ({
  setSnackbarData,
  setSnackbarOpen,
  revisionUpdated,
  setRevisionUpdated,
}) => {
  const [openConfirmDialogRev, setOpenConfirmDialogRev] = useState(false)
  const [selectedRevNum, setSelectedRevNum] = useState(null)
  const [revision, setRevision] = useState('0')
  const [revisionDetails, setRevisionDetails] = useState(null)

  const handleOpenDialogRev = (num) => {
    setSelectedRevNum(num)
    setOpenConfirmDialogRev(true)
  }

  const handleCloseDialogRev = () => {
    setOpenConfirmDialogRev(false)
    setSelectedRevNum(null)
  }

  const handleConfirmLoadRev = () => {
    setOpenConfirmDialogRev(false)
    handleRevisionChange(selectedRevNum)
  }

  const handleRevisionChange = async (num) => {
    setRevision(num)
    console.log('revisionDetails:', revisionDetails)
    const payload = revisionDetails ? { ...revisionDetails } : {}
    payload.attributeValueVersion = num
    payload.attributeValue = num
    await updateRevision([payload])
  }

  const updateRevision = async (Payload) => {
    try {
      setRevisionUpdated(true)
      console.log('Payload', Payload)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Revision updated successfully!',
        severity: 'success',
        duration: 3000,
      })
    } catch (error) {
      console.error('Error updating data:', error)
    }
  }

  return (
    <Box>
      <ButtonGroup aria-label='revision group'>
        {['0', '1', '2', '3'].map((num) => {
          const selected = revision === num

          return (
            <Button
              key={num}
              onClick={() => handleOpenDialogRev(num)}
              variant={selected ? 'contained' : 'outlined'}
              size='small'
              sx={{
                textTransform: 'none',
                fontSize: '0.75rem',
                padding: '5px 10px',
                minWidth: '36px',
                mr: 0.5,
                ...(selected && {
                  bgcolor: '#0100cb',
                  color: '#fff',
                  borderColor: '#0100cb',
                  fontWeight: 'bold',
                }),
                ...(!selected && {
                  borderColor: '#000000ff',
                  color: '#000000ff',
                  fontWeight: 'bold',
                }),
              }}
            >
              {`Rev ${num}`}
            </Button>
          )
        })}
      </ButtonGroup>

      <RevConfirmDialog
        openConfirmDialogRev={openConfirmDialogRev}
        handleCloseDialogRev={handleCloseDialogRev}
        handleConfirmLoadRev={handleConfirmLoadRev}
      />
    </Box>
  )
}

export default RevButtonSection
