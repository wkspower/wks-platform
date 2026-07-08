import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Checkbox from '@mui/material/Checkbox'
import FormControlLabel from '@mui/material/FormControlLabel'
import IconButton from '@mui/material/IconButton'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import DeleteIcon from '@mui/icons-material/Delete'
import MainCard from 'components/MainCard'

// Note: acceptedFileTypes / maxSizeBytes are part of the requiredDocuments
// schema (@wkspower/case-config-schema 1.1) but are advisory and deferred
// from this UI slice. They remain untouched on entries that already carry them.

export const CaseDefFormDocuments = ({ caseDef, setCaseDef }) => {
  const requiredDocuments = caseDef.requiredDocuments || []

  const updateDocuments = (newDocuments) => {
    setCaseDef({ ...caseDef, requiredDocuments: newDocuments })
  }

  const handleAddDocument = () => {
    updateDocuments([
      ...requiredDocuments,
      {
        id: '',
        label: '',
        description: '',
        required: true,
      },
    ])
  }

  const handleFieldChange = (index, field, value) => {
    const newDocuments = requiredDocuments.map((doc, i) =>
      i === index ? { ...doc, [field]: value } : doc,
    )
    updateDocuments(newDocuments)
  }

  const handleRemoveDocument = (index) => {
    const newDocuments = requiredDocuments.filter((doc, i) => i !== index)
    updateDocuments(newDocuments)
  }

  return (
    <div style={{ width: '100%' }}>
      <Button id='basic-button' variant='contained' onClick={handleAddDocument}>
        New Document
      </Button>

      {requiredDocuments.length === 0 && (
        <Typography sx={{ mt: 2 }} component='div'>
          No required documents declared for this case type.
        </Typography>
      )}

      {requiredDocuments.map((doc, index) => (
        <MainCard key={index} sx={{ mt: 2 }} content={false}>
          <Box sx={{ p: 2, display: 'grid', gap: 2 }}>
            <TextField
              label='Id'
              value={doc.id || ''}
              onChange={(e) => handleFieldChange(index, 'id', e.target.value)}
            />
            <TextField
              label='Label'
              value={doc.label || ''}
              onChange={(e) =>
                handleFieldChange(index, 'label', e.target.value)
              }
            />
            <TextField
              label='Description'
              value={doc.description || ''}
              onChange={(e) =>
                handleFieldChange(index, 'description', e.target.value)
              }
            />
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}
            >
              <FormControlLabel
                control={
                  <Checkbox
                    checked={doc.required !== false}
                    onChange={(e) =>
                      handleFieldChange(index, 'required', e.target.checked)
                    }
                  />
                }
                label='Required'
              />
              <IconButton
                aria-label='delete'
                onClick={() => handleRemoveDocument(index)}
              >
                <DeleteIcon />
              </IconButton>
            </Box>
          </Box>
        </MainCard>
      ))}
    </div>
  )
}
