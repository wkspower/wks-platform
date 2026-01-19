import React, { useState, useMemo } from 'react'
import {
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Box,
  Typography,
  Button,
  Divider,
  Chip,
} from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import HistoryIcon from '@mui/icons-material/History'
import HistoryDialog from './HistoryDialog'

const StatusAccordian = ({
  title = 'Status',
  data = [],
  defaultExpanded = true,
}) => {
  const [expanded, setExpanded] = useState(defaultExpanded)
  const [historyDialogOpen, setHistoryDialogOpen] = useState(false)

  // Get latest status (first item in data array)
  const latestStatus = useMemo(() => {
    if (data && data.length > 0) {
      return data[0]
    }
    return null
  }, [data])

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'approved':
        return 'success'
      case 'rejected':
        return 'error'
      case 'pending':
        return 'warning'
      default:
        return 'default'
    }
  }

  const handleViewHistory = () => {
    setHistoryDialogOpen(true)
  }

  const handleCloseHistory = () => {
    setHistoryDialogOpen(false)
  }

  return (
    <>
      <Accordion
        expanded={expanded}
        onChange={(e, isExpanded) => setExpanded(isExpanded)}
        sx={{
          mb: 2,
          backgroundColor: '#ffffff',
          border: '1px solid #e0e0e0',
          boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
          '&:before': {
            display: 'none',
          },
        }}
      >
        <AccordionSummary
          expandIcon={<ExpandMoreIcon />}
          sx={{
            backgroundColor: '#f9f9f9',
            borderBottom: expanded ? '1px solid #e0e0e0' : 'none',
            minHeight: '48px',
            '&.Mui-expanded': {
              minHeight: '48px',
            },
            '& .MuiAccordionSummary-content': {
              margin: '12px 0',
            },
            '& .MuiAccordionSummary-content.Mui-expanded': {
              margin: '12px 0',
            },
            '&:hover': {
              backgroundColor: '#f5f5f5',
            },
          }}
        >
          <Typography variant='subtitle1' sx={{ fontWeight: 600 }}>
            {title}
          </Typography>
        </AccordionSummary>
        <AccordionDetails
          sx={{
            p: 3,
            backgroundColor: '#ffffff',
          }}
        >
          {!latestStatus ? (
            <Box sx={{ textAlign: 'center', py: 2 }}>
              <Typography variant='body2' color='text.secondary'>
                No status available
              </Typography>
            </Box>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {/* Latest Status Header */}
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  pb: 1,
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Chip
                    label={latestStatus.status}
                    color={getStatusColor(latestStatus.status)}
                    size='small'
                    sx={{ fontWeight: 600 }}
                  />
                  <Typography variant='body2' color='text.secondary'>
                    by {latestStatus.submittedBy} on{' '}
                    {latestStatus.submittedDate}
                  </Typography>
                </Box>
                {data.length > 1 && (
                  <Button
                    variant='outlined'
                    size='small'
                    startIcon={<HistoryIcon />}
                    onClick={handleViewHistory}
                    sx={{
                      textTransform: 'none',
                      borderColor: '#1976d2',
                      color: '#1976d2',
                      '&:hover': {
                        borderColor: '#1565c0',
                        backgroundColor: '#e3f2fd',
                      },
                    }}
                  >
                    View History ({data.length})
                  </Button>
                )}
              </Box>

              <Divider />

              {/* Remark Section */}
              <Box>
                <Typography
                  variant='subtitle2'
                  sx={{ fontWeight: 600, mb: 1, color: '#424242' }}
                >
                  Remark:
                </Typography>
                <Box
                  sx={{
                    p: 2,
                    backgroundColor: '#f5f5f5',
                    borderRadius: 1,
                    border: '1px solid #e0e0e0',
                    maxHeight: '200px',
                    overflowY: 'auto',
                    '&::-webkit-scrollbar': {
                      width: '6px',
                    },
                    '&::-webkit-scrollbar-track': {
                      bgcolor: '#f0f0f0',
                    },
                    '&::-webkit-scrollbar-thumb': {
                      bgcolor: '#bdbdbd',
                      borderRadius: 1,
                    },
                  }}
                >
                  <Typography
                    variant='body2'
                    sx={{
                      whiteSpace: 'pre-wrap',
                      wordBreak: 'break-word',
                      lineHeight: 1.6,
                    }}
                  >
                    {latestStatus.remarks || 'No remarks provided'}
                  </Typography>
                </Box>
              </Box>
            </Box>
          )}
        </AccordionDetails>
      </Accordion>

      {/* History Dialog */}
      <HistoryDialog
        open={historyDialogOpen}
        onClose={handleCloseHistory}
        title={`Audit Trail - ${title}`}
        data={data}
      />
    </>
  )
}

export default StatusAccordian
