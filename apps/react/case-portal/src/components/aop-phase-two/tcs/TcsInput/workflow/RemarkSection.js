import { useState } from 'react'
import { Box, Typography, IconButton, Tooltip } from '@mui/material'
import HistoryIcon from '@mui/icons-material/History'
import { TextArea } from '@progress/kendo-react-inputs'

const RemarkSection = ({
  title = 'Remark',
  placeholder = 'Enter your remarks here...',
  onSubmit,
  onShowHistory,
  disabled = false,
  maxLength = 1000,
}) => {
  const [remark, setRemark] = useState('')

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
    <div>
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
        {/* Label */}
        <Typography sx={{ whiteSpace: 'nowrap', mt: 1 }} variant='h5'>
          {title} :
        </Typography>

        {/* Textarea */}
        <Box sx={{ flex: 1 }}>
          <TextArea
            rows={2}
            placeholder={placeholder}
            value={remark}
            onChange={handleChange}
            disabled={disabled}
            maxLength={maxLength}
            style={{
              width: '80%',
              fontSize: '0.875rem',
              minHeight: '40px',
              resize: 'vertical',
            }}
          />
        </Box>

        {/* Submit button and History icon */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <button
            className='btn-submit'
            onClick={handleSubmit}
            disabled={disabled || !remark.trim()}
          >
            Submit
          </button>
          <Tooltip title='View History'>
            <IconButton onClick={onShowHistory} color='primary'>
              <HistoryIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>
    </div>
  )
}

export default RemarkSection
