import { useState } from 'react'
import { TextField, Button, Box } from '@mui/material'
import { DataService } from 'services/DataService'

const TextSubmitMUI = () => {
  const [text, setText] = useState('')

  const handleSubmit = async () => {
    try {
      if (!text.trim()) {
        console.log('Please enter a message!')
        return
      }
      const result = await DataService.saveText(text)
      console.log('Response:', result)
      // alert('Submitted successfully!')
    } catch (error) {
      console.error('Error submitting:', error)
      // alert('Something went wrong!')
    }
  }

  return (
    <Box sx={{ maxWidth: 500, mx: 'auto', mt: 4, p: 2 }}>
      <TextField
        label='Enter your message'
        multiline
        rows={5}
        fullWidth
        variant='outlined'
        value={text}
        onChange={(e) => setText(e.target.value)}
      />
      <Button
        variant='contained'
        color='primary'
        onClick={handleSubmit}
        sx={{ mt: 2 }}
        disabled={!text.trim()}
      >
        Submit
      </Button>
    </Box>
  )
}

export default TextSubmitMUI
