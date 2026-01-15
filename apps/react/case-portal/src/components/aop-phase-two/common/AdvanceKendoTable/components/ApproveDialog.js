import React, { useState, useEffect, useMemo } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Checkbox,
  FormControlLabel,
  Box,
  Typography,
  Divider,
} from '@mui/material'

const ApproveDialog = ({ open, onClose, onApprove, entries = [] }) => {
  const [selectedLabels, setSelectedLabels] = useState([])
  const [selectAll, setSelectAll] = useState(false)

  // Get unique labels
  const uniqueLabels = useMemo(() => {
    const labels = new Set()
    entries.forEach((entry) => {
      const label = entry.label || entry.name || `Entry ${entry.id}`
      labels.add(label)
    })
    return Array.from(labels)
  }, [entries])

  useEffect(() => {
    if (open) {
      setSelectedLabels([])
      setSelectAll(false)
    }
  }, [open])

  const handleSelectAll = (event) => {
    const checked = event.target.checked
    setSelectAll(checked)
    if (checked) {
      setSelectedLabels([...uniqueLabels])
    } else {
      setSelectedLabels([])
    }
  }

  const handleSelectLabel = (label) => {
    setSelectedLabels((prev) => {
      if (prev.includes(label)) {
        const newSelected = prev.filter((l) => l !== label)
        setSelectAll(false)
        return newSelected
      } else {
        const newSelected = [...prev, label]
        if (newSelected.length === uniqueLabels.length) {
          setSelectAll(true)
        }
        return newSelected
      }
    })
  }

  const handleApprove = () => {
    if (selectedLabels.length > 0) {
      onApprove(selectedLabels)
      onClose()
    }
  }

  const handleCancel = () => {
    setSelectedLabels([])
    setSelectAll(false)
    onClose()
  }

  return (
    <Dialog
      open={open}
      onClose={handleCancel}
      maxWidth='md'
      fullWidth
      PaperProps={{
        sx: {
          minHeight: '400px',
          maxHeight: '80vh',
        },
      }}
    >
      <DialogTitle>
        <Typography variant='h6' component='div' fontWeight='600'>
          Approve Entries
        </Typography>
        <Typography variant='body2' color='text.secondary' sx={{ mt: 0.5 }}>
          Select the entries you want to approve
        </Typography>
      </DialogTitle>

      <Divider />

      <DialogContent sx={{ p: 3 }}>
        {/* Select All Checkbox */}
        <Box sx={{ mb: 2 }}>
          <FormControlLabel
            control={
              <Checkbox
                checked={selectAll}
                onChange={handleSelectAll}
                indeterminate={
                  selectedLabels.length > 0 &&
                  selectedLabels.length < uniqueLabels.length
                }
              />
            }
            label={
              <Typography variant='subtitle1' fontWeight='600'>
                Select All ({uniqueLabels.length} entries)
              </Typography>
            }
          />
        </Box>

        <Divider sx={{ mb: 2 }} />

        {/* Entries List */}
        <Box
          sx={{
            maxHeight: '400px',
            overflowY: 'auto',
            border: '1px solid #e0e0e0',
            borderRadius: 1,
            '&::-webkit-scrollbar': {
              width: '8px',
            },
            '&::-webkit-scrollbar-track': {
              bgcolor: '#f5f5f5',
            },
            '&::-webkit-scrollbar-thumb': {
              bgcolor: '#bdbdbd',
              borderRadius: 1,
              '&:hover': {
                bgcolor: '#9e9e9e',
              },
            },
          }}
        >
          {uniqueLabels.length === 0 ? (
            <Box
              sx={{
                textAlign: 'center',
                py: 4,
                px: 3,
              }}
            >
              <Typography variant='body2' color='text.secondary'>
                No entries available for approval
              </Typography>
            </Box>
          ) : (
            uniqueLabels.map((label, index) => (
              <Box
                key={label}
                sx={{
                  borderBottom:
                    index < uniqueLabels.length - 1
                      ? '1px solid #f0f0f0'
                      : 'none',
                }}
              >
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={selectedLabels.includes(label)}
                      onChange={() => handleSelectLabel(label)}
                      size='medium'
                    />
                  }
                  label={<Typography variant='body2'>{label}</Typography>}
                  sx={{
                    width: '100%',
                    m: 0,
                    px: 2,
                    py: 0.75,
                    '&:hover': {
                      bgcolor: 'action.hover',
                    },
                  }}
                />
              </Box>
            ))
          )}
        </Box>
      </DialogContent>

      <Divider />

      <DialogActions sx={{ p: 2, gap: 1 }}>
        <Typography
          variant='body2'
          color='text.secondary'
          sx={{ flex: 1, ml: 1 }}
        >
          {selectedLabels.length} of {uniqueLabels.length} selected
        </Typography>
        <Button onClick={handleCancel} variant='outlined' color='secondary'>
          Cancel
        </Button>
        <Button
          onClick={handleApprove}
          variant='contained'
          color='primary'
          disabled={selectedLabels.length === 0}
        >
          Approve ({selectedLabels.length})
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default ApproveDialog
