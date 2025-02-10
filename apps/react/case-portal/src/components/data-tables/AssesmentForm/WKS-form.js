import { useState } from 'react'
import {
  Box,
  Grid,
  TextField,
  MenuItem,
  Typography,
  Button,
  FormControlLabel,
  Checkbox,
  Radio,
  IconButton,
  Divider,
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import ReactQuill from 'react-quill'
import 'react-quill/dist/quill.snow.css' // Import React-Quill styles

const WKSForm = () => {
  const [options, setOptions] = useState([
    { value: '', correct: false },
    { value: '', correct: false },
    { value: '', correct: false },
  ])
  const [question, setQuestion] = useState('') // State for React-Quill

  const handleOptionChange = (index, field, value) => {
    const updatedOptions = [...options]
    updatedOptions[index][field] = value
    setOptions(updatedOptions)
  }

  const addOption = () => {
    setOptions([...options, { value: '', correct: false }])
  }

  const removeOption = (index) => {
    setOptions(options.filter((_, i) => i !== index))
  }

  const markCorrectOption = (index) => {
    setOptions(
      options.map((option, i) => ({
        ...option,
        correct: i === index,
      })),
    )
  }

  return (
    <Box
      p={2}
      maxWidth='1200px'
      height='100%'
      mx='auto'
      bgcolor='white'
      borderRadius={2}
      boxShadow={1}
    >
      {/* Title */}
      {/* <Typography variant='h5' fontWeight='bold' mb={2}>
          WKS Form
          </Typography> */}
      {/* Input Fields */}
      <Grid container spacing={2} mb={2}>
        <Grid item xs={6}>
          <TextField
            fullWidth
            select
            label='Question Type'
            size='small'
            defaultValue='Single choice (Radio button)'
          >
            <MenuItem value='Single choice (Radio button)'>
              First Product
            </MenuItem>
            <MenuItem value='Multiple choice (Checkbox)'>
              Second Product
            </MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={6}>
          <TextField
            fullWidth
            select
            label='Difficulty Level'
            size='small'
            defaultValue='Low'
          >
            <MenuItem value='Low'>Low</MenuItem>
            <MenuItem value='Medium'>Medium</MenuItem>
            <MenuItem value='High'>High</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={6}>
          <TextField
            fullWidth
            select
            label='Require File Submission'
            size='small'
            defaultValue='No'
          >
            <MenuItem value='Yes'>Yes</MenuItem>
            <MenuItem value='No'>No</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={6} display='flex' alignItems='center'>
          <Typography variant='subtitle1' fontWeight='bold' mr={1}>
            Technology Set
          </Typography>
          <Box>
            <FormControlLabel control={<Checkbox />} label='TC' />
            <FormControlLabel control={<Checkbox />} label='MS' />
            <FormControlLabel control={<Checkbox />} label='PS' />
            <FormControlLabel control={<Checkbox />} label='AT' />
          </Box>
        </Grid>
      </Grid>

      {/* Question Input */}
      <Box mb={2}>
        <Typography variant='subtitle1' fontWeight='bold' mb={1}>
          Requirements
        </Typography>
        <ReactQuill
          theme='snow'
          value={question}
          onChange={setQuestion}
          placeholder='Write your question here'
        />
      </Box>

      {/* Options Section */}
      <Box>
        <Typography variant='subtitle1' fontWeight='bold' mb={1}>
          Options
        </Typography>
        {options.map((option, index) => (
          <Box
            key={index}
            display='flex'
            alignItems='center'
            gap={1}
            mb={1}
            p={1}
            bgcolor='grey.100'
            borderRadius={2}
          >
            <TextField
              fullWidth
              size='small'
              placeholder={`Option ${String.fromCharCode(65 + index)}`}
              value={option.value}
              onChange={(e) =>
                handleOptionChange(index, 'value', e.target.value)
              }
            />
            <Radio
              checked={option.correct}
              onChange={() => markCorrectOption(index)}
              color='primary'
            />
            <IconButton color='error' onClick={() => removeOption(index)}>
              <DeleteIcon />
            </IconButton>
          </Box>
        ))}
        <Button
          onClick={addOption}
          variant='outlined'
          size='small'
          color='primary'
        >
          + Add Option
        </Button>
      </Box>

      {/* Actions */}
      <Divider sx={{ my: 2 }} />
      <Box display='flex' justifyContent='flex-end' gap={1}>
        <Button variant='outlined'>Cancel</Button>
        <Button variant='contained' color='primary'>
          Create
        </Button>
      </Box>
    </Box>
  )
}

export default WKSForm
