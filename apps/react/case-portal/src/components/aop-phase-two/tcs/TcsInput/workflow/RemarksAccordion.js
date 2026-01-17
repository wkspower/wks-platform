import React, { useState } from 'react'
import {
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Box,
  Typography,
} from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { TextArea } from '@progress/kendo-react-inputs'

const RemarksAccordion = ({
  title = 'Add Remarks',
  placeholder = 'Enter your remarks here...',
  onSubmit,
  disabled = false,
  defaultExpanded = true,
  maxLength = 1000,
}) => {
  const [remark, setRemark] = useState('')
  const [expanded, setExpanded] = useState(defaultExpanded)

  const handleSubmit = () => {
    if (remark.trim()) {
      onSubmit(remark)
      setRemark('')
    }
  }

  const handleChange = (event) => {
    setRemark(event.target.value)
  }

  return (
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
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextArea
            rows={4}
            placeholder={placeholder}
            value={remark}
            onChange={handleChange}
            disabled={disabled}
            maxLength={maxLength}
            style={{
              width: '100%',
              fontSize: '0.875rem',
              minHeight: '100px',
              resize: 'vertical',
            }}
          />
          <Typography
            variant='caption'
            sx={{ color: '#666', fontSize: '0.75rem', textAlign: 'right' }}
          >
            {remark.length}/{maxLength} characters
          </Typography>
          <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button
              className='btn-submit'
              onClick={handleSubmit}
              disabled={disabled || !remark.trim()}
            >
              Submit
            </button>
          </Box>
        </Box>
      </AccordionDetails>
    </Accordion>
  )
}

export default RemarksAccordion
