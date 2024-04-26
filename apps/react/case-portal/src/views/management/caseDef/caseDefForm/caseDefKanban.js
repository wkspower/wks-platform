import FormControl from '@mui/material/FormControl'
import TextField from '@mui/material/TextField'
import React from 'react'

export const CaseKanbanForm = ({ caseDef, setCaseDef }) => {
  const handleInputChange = (event) => {
    setCaseDef({
      ...caseDef,
      kanbanConfig: {
        ...caseDef.kanbanConfig,
        [event.target.name]: event.target.value.split(','),
      },
    })
  }

  return (
    <React.Fragment>
      <FormControl key='ctrlTitle' sx={{ mt: 3 }}>
        <TextField
          label='Title'
          id='txtTitle'
          name='title'
          value={caseDef?.kanbanConfig?.title}
          onChange={handleInputChange}
        />
      </FormControl>

      <FormControl key='ctrlContent' sx={{ mt: 3 }}>
        <TextField
          label='Content'
          id='txtContent'
          name='content'
          value={caseDef?.kanbanConfig?.content}
          onChange={handleInputChange}
        />
      </FormControl>
    </React.Fragment>
  )
}
