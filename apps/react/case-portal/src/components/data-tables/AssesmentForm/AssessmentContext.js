import { useState } from 'react'
import {
  Box,
  Grid,
  TextField,
  Select,
  InputLabel,
  FormControl,
  Button,
  Typography,
  IconButton,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '@mui/material'
import { MenuItem } from '@mui/material'
import { Delete } from '@mui/icons-material'

// Reusable Form Section Component
const FormSection = ({ title, children, onDelete }) => (
  <Box
    sx={{
      border: '1px solid #ddd',
      borderRadius: 2,
      p: 3,
      mb: 3,
      backgroundColor: 'white',
      position: 'relative',
    }}
  >
    {title && (
      <Typography variant='h6' sx={{ mb: 2 }}>
        {title}
      </Typography>
    )}
    {onDelete && (
      <IconButton
        sx={{ position: 'absolute', top: 8, right: 8 }}
        onClick={onDelete}
      >
        <Delete color='error' />
      </IconButton>
    )}
    {children}
  </Box>
)

const InputRow = ({ inputs }) => (
  <Grid container spacing={2} sx={{ mb: 2 }}>
    {inputs.map((input, index) => (
      <Grid item xs={12} md={4} key={index}>
        {input.type === 'select' ? (
          <FormControl fullWidth>
            <InputLabel>{input.label}</InputLabel>
            <Select required={input.required}>
              {input.options.map((option, i) => (
                <MenuItem value={option.value} key={i}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        ) : (
          <TextField
            fullWidth
            label={input.label}
            required={input.required}
            variant='outlined'
            type={input.type || 'text'}
          />
        )}
      </Grid>
    ))}
  </Grid>
)

const AssessmentForm = () => {
  // Existing states for other sections
  const [linkedQuestions, setLinkedQuestions] = useState([{}])
  const [requirements, setRequirements] = useState([{}])

  // State for additional General Information sections
  const [generalInfos, setGeneralInfos] = useState([{}])

  // Dialog state for confirming General Information import
  const [generalInfoDialogOpen, setGeneralInfoDialogOpen] = useState(false)

  // Functions for Linked Questions
  const addLinkedQuestion = () => setLinkedQuestions([...linkedQuestions, {}])
  const removeLinkedQuestion = (index) => {
    setLinkedQuestions(linkedQuestions.filter((_, i) => i !== index))
  }

  // Functions for Single Requirements
  const addRequirement = () => setRequirements([...requirements, {}])
  const removeRequirement = (index) => {
    setRequirements(requirements.filter((_, i) => i !== index))
  }

  // Functions for General Information import
  const handleGeneralInfoImportClick = () => {
    setGeneralInfoDialogOpen(true)
  }
  const handleGeneralInfoDialogClose = () => {
    setGeneralInfoDialogOpen(false)
  }
  const handleConfirmGeneralInfoImport = () => {
    // On confirm, add a new General Information component
    setGeneralInfos([...generalInfos, {}])
    setGeneralInfoDialogOpen(false)
  }
  const removeGeneralInfo = (index) => {
    setGeneralInfos(generalInfos.filter((_, i) => i !== index))
  }

  return (
    <Box sx={{ p: 4, backgroundColor: '#f5f5f5', minHeight: '100vh' }}>
      <Typography variant='h4' sx={{ mb: 4 }}>
        WKS Form
      </Typography>

      {/* Render all General Information Sections */}
      {generalInfos.map((_, index) => (
        <FormSection
          key={index}
          title={`General Information ${index + 1}`}
          onDelete={() => removeGeneralInfo(index)}
        >
          <InputRow
            inputs={[
              { label: 'First Name', required: true },
              { label: 'Last Name', required: true },
              { label: 'Email', required: true },
              { label: 'Phone', required: true },
              {
                label: 'Select Department',
                type: 'select',
                options: [
                  { value: 'dept1', label: 'Department 1' },
                  { value: 'dept2', label: 'Department 2' },
                ],
              },
              {
                label: 'Select Product',
                type: 'select',
                options: [
                  { value: 'prod1', label: 'Product 1' },
                  { value: 'prod2', label: 'Product 2' },
                ],
              },
            ]}
          />
        </FormSection>
      ))}

      {/* Single Requirement Section */}
      {requirements.map((_, index) => (
        <FormSection
          key={index}
          title='Single Requirement'
          onDelete={() => removeRequirement(index)}
        >
          <TextField
            fullWidth
            label='Detailed Description'
            multiline
            minRows={4}
            variant='outlined'
            sx={{ mb: 2 }}
          />
          <InputRow
            inputs={[
              {
                label: 'Technology',
                type: 'select',
                options: [
                  { value: 'hybrid', label: 'Hybrid' },
                  { value: 'ai', label: 'AI' },
                ],
              },
              {
                label: 'Sub Fields',
                type: 'select',
                options: [
                  { value: 'sub1', label: 'Sub Data 1' },
                  { value: 'sub2', label: 'Sub Data 2' },
                ],
              },
              {
                label: 'Requirement Level',
                type: 'select',
                options: [
                  { value: 'low', label: 'Low' },
                  { value: 'medium', label: 'Medium' },
                  { value: 'high', label: 'High' },
                ],
              },
            ]}
          />
        </FormSection>
      ))}

      {/* Linked Questions Section */}
      {/* <FormSection title='Linked Questions'> */}
      {linkedQuestions.map((_, index) => (
        <FormSection
          key={index}
          title='Linked Questions'
          onDelete={() => removeLinkedQuestion(index)}
        >
          <TextField
            fullWidth
            label={`Linked Question ${index + 1}`}
            multiline
            minRows={3}
            sx={{ mb: 2 }}
          />
          <InputRow
            inputs={[
              { label: 'Input Field 1', type: 'number' },
              { label: 'Input Field 2' },
              { label: 'Input Field 3' },
            ]}
          />
        </FormSection>
      ))}
      <Button variant='outlined' onClick={addLinkedQuestion}>
        Additional Requirements
      </Button>
      {/* </FormSection> */}

      {/* Action Buttons */}
      <Box sx={{ mt: 4, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
        <Button variant='contained' color='primary'>
          Save Draft
        </Button>
        <Button
          variant='contained'
          color='success'
          onClick={handleGeneralInfoImportClick}
        >
          Import Field
        </Button>
        <Button variant='contained' color='secondary' onClick={addRequirement}>
          Add Single Requirement
        </Button>
      </Box>

      {/* Import General Information Popup Dialog */}
      <Dialog
        open={generalInfoDialogOpen}
        onClose={handleGeneralInfoDialogClose}
      >
        <DialogTitle>Confirm Import</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Please confirm that you want to import the General Information
            component.
          </DialogContentText>
          {/* This Box is used as a preview of the General Information component */}
          <Box
            sx={{
              border: '1px solid #ddd',
              borderRadius: 2,
              p: 2,
              mt: 2,
              backgroundColor: 'white',
            }}
          >
            <Typography variant='subtitle1' sx={{ mb: 1 }}>
              Preview
            </Typography>
            <InputRow
              inputs={[
                { label: 'First Name', required: true },
                { label: 'Last Name', required: true },
                { label: 'Email', required: true },
                { label: 'Phone', required: true },
                {
                  label: 'Select Department',
                  type: 'select',
                  options: [
                    { value: 'dept1', label: 'Department 1' },
                    { value: 'dept2', label: 'Department 2' },
                  ],
                },
                {
                  label: 'Select Product',
                  type: 'select',
                  options: [
                    { value: 'prod1', label: 'Product 1' },
                    { value: 'prod2', label: 'Product 2' },
                  ],
                },
              ]}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleGeneralInfoDialogClose}>Cancel</Button>
          <Button variant='contained' onClick={handleConfirmGeneralInfoImport}>
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default AssessmentForm
