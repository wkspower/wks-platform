import React, { useState } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Divider,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Box,
} from '@mui/material'

const RemarkCell = ({ text, maxLength = 100 }) => {
  const [expanded, setExpanded] = useState(false)
  const isLong = text && text.length > maxLength

  if (!text || text === '-') {
    return (
      <Typography variant='body2' sx={{ color: 'text.secondary' }}>
        -
      </Typography>
    )
  }

  return (
    <Box>
      <Typography
        variant='body2'
        sx={{
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-word',
          pr: 1,
        }}
      >
        {expanded || !isLong ? text : `${text.substring(0, maxLength)}...`}
      </Typography>
      {isLong && (
        <Button
          size='small'
          onClick={() => setExpanded(!expanded)}
          sx={{
            textTransform: 'none',
            minWidth: 'auto',
            p: 0,
            mt: 0.5,
            fontSize: '0.75rem',
            color: '#1976d2',
            '&:hover': {
              backgroundColor: 'transparent',
              textDecoration: 'underline',
            },
          }}
        >
          {expanded ? 'View Less' : 'View More'}
        </Button>
      )}
    </Box>
  )
}

const HistoryDialog = ({
  open,
  onClose,
  title = 'History',
  data = [],
  columns = [
    { field: 'submittedDate', header: 'Date', width: '20%' },
    { field: 'status', header: 'Action', width: '15%', isChip: true },
    { field: 'submittedBy', header: 'Submitted By', width: '20%' },
    { field: 'remarks', header: 'Remarks', width: '45%' },
  ],
}) => {
  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'approved':
        return 'success'
      case 'rejected':
        return 'error'
      case 'pending':
        return 'warning'
      case 'submitted':
        return 'info'
      default:
        return 'default'
    }
  }

  return (
    <Dialog
      open={open}
      onClose={onClose}
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
          {title}
        </Typography>
        <Typography variant='body2' color='text.secondary' sx={{ mt: 0.5 }}>
          Complete audit trail of all changes
        </Typography>
      </DialogTitle>

      <Divider />

      <DialogContent sx={{ p: 0 }}>
        {data.length === 0 ? (
          <Typography
            variant='body2'
            color='text.secondary'
            sx={{ textAlign: 'center', py: 4 }}
          >
            No history available
          </Typography>
        ) : (
          <TableContainer
            component={Paper}
            elevation={0}
            sx={{
              ml: 2,
              mr: 2,
              maxHeight: '500px',
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
            <Table stickyHeader>
              <TableHead>
                <TableRow>
                  {columns.map((col) => (
                    <TableCell
                      key={col.field}
                      sx={{
                        fontWeight: 600,
                        bgcolor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                        width: col.width || 'auto',
                      }}
                    >
                      {col.header}
                    </TableCell>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {data.map((item, index) => (
                  <TableRow
                    key={item.id || index}
                    sx={{
                      '&:hover': {
                        bgcolor: 'action.hover',
                      },
                      bgcolor: index === 0 ? '#f0f7ff' : 'inherit',
                    }}
                  >
                    {columns.map((col) => (
                      <TableCell key={col.field}>
                        {col.isChip ? (
                          <Chip
                            label={item[col.field]}
                            color={getStatusColor(item[col.field])}
                            size='small'
                            sx={{ fontWeight: 500 }}
                          />
                        ) : col.field === 'remarks' ? (
                          <RemarkCell text={item[col.field]} />
                        ) : (
                          <Typography
                            variant='body2'
                            sx={{
                              whiteSpace: 'pre-wrap',
                              wordBreak: 'break-word',
                              overflowY: 'auto',
                              pr: 1,
                            }}
                          >
                            {item[col.field] || '-'}
                          </Typography>
                        )}
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </DialogContent>

      <Divider />

      <DialogActions sx={{ p: 2 }}>
        <Typography
          variant='body2'
          color='text.secondary'
          sx={{ flex: 1, ml: 1 }}
        >
          Total Records: {data.length}
        </Typography>
        <Button onClick={onClose} variant='contained' color='primary'>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default HistoryDialog
