import React, { useState, useEffect, useMemo } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Grid,
  MenuItem,
  Typography,
} from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import { InputApiService } from 'components/aop-phase-two/services/cpp/jmd/inputApiService'
import { SaveIcon as SaveImageIcon } from 'assets/images/icons'

const INITIAL_FORM_DATA = {
  cppPlant: '',
  procurementPlant: '',
  name: '',
  displayName: '',
  sapCode: '',
  uom: 'MW',
}

const INITIAL_FORM_ERROR = {
  cppPlant: { isError: false, errorMessage: '' },
  procurementPlant: { isError: false, errorMessage: '' },
  name: { isError: false, errorMessage: '' },
  displayName: { isError: false, errorMessage: '' },
  sapCode: { isError: false, errorMessage: '' },
  uom: { isError: false, errorMessage: '' },
}

// Fields that are required for form submission
const REQUIRED_FIELDS = ['cppPlant', 'procurementPlant', 'name', 'uom']

// Human-readable labels for each field
const FIELD_LABELS = {
  cppPlant: 'CPP Plant',
  procurementPlant: 'Procurement Plant',
  name: 'Name',
  displayName: 'Display Name',
  sapCode: 'SAP Code',
  uom: 'UOM',
}

const AddSourceDialog = ({ open, onClose, onSuccess, editRowData = null }) => {
  const keycloak = useSession()
  const isEditMode = !!editRowData

  const [formData, setFormData] = useState(INITIAL_FORM_DATA)
  const [formError, setFormError] = useState(INITIAL_FORM_ERROR)
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const [procurementPlantOptions, setProcurementPlantOptions] = useState([])

  // Read plant list, year, and role flags from the global store
  const { jmdSelectedPlants, year, oldYear, isReleased, plantObject } =
    useSelector((state) => state.dataGridStore)

  const IS_OLD_YEAR = oldYear?.oldYear
  const IS_RELEASED = isReleased
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR, IS_RELEASED)
  const AOP_YEAR = year?.selectedYear

  const cppPlantOptions = useMemo(() => {
    const options = ([plantObject] || []).map((plant) => ({
      value: plant.id,
      label: plant.name || plant.id,
    }))
    if (isEditMode && editRowData?.cppPlantFkId) {
      if (!options.find((o) => o.value === editRowData.cppPlantFkId)) {
        options.push({
          value: editRowData.cppPlantFkId,
          label: editRowData.plantName || editRowData.cppPlantFkId,
        })
      }
    }
    return options
  }, [plantObject, isEditMode, editRowData])

  // Fetch procurement plants whenever cppPlant changes
  useEffect(() => {
    const fetchProcurementPlants = async () => {
      if (!formData.cppPlant) {
        setProcurementPlantOptions([])
        return
      }
      try {
        const response = await InputApiService.getImportProcurementPlants(
          keycloak,
          formData.cppPlant,
        )
        if (response && response.data) {
          const apiOptions = response.data.map((plant) => ({
            value: plant.procurementPlantId,
            label: plant.name || plant.procurementPlantId,
          }))

          // In edit mode, ensure the existing selection is present
          if (isEditMode && editRowData?.importPlantFkId) {
            if (
              !apiOptions.find((o) => o.value === editRowData.importPlantFkId)
            ) {
              apiOptions.push({
                value: editRowData.importPlantFkId,
                label:
                  editRowData.procurementPlant || editRowData.importPlantFkId,
              })
            }
          }
          setProcurementPlantOptions(apiOptions)
        }
      } catch (error) {
        console.error('Error fetching procurement plants:', error)
      }
    }

    fetchProcurementPlants()
  }, [formData.cppPlant, keycloak, isEditMode, editRowData])

  // Reset / pre-populate form whenever the dialog opens
  useEffect(() => {
    if (open) {
      if (editRowData) {
        // Edit mode — pre-fill with the row's existing source fields
        setFormData({
          cppPlant: editRowData.cppPlantFkId || '',
          procurementPlant: editRowData.importPlantFkId || '',
          name: editRowData.materialName || '',
          displayName: editRowData.materialDisplayName || '',
          sapCode: editRowData.materialCode || '',
          uom: editRowData.uom || '',
        })
      } else {
        // Add mode — blank form
        setFormData(INITIAL_FORM_DATA)
      }
      setFormError(INITIAL_FORM_ERROR)
      setIsButtonDisabled(false)
    }
  }, [open, editRowData])

  const handleFieldChange = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
    // Clear error for the changed field
    if (formError[field]?.isError) {
      setFormError((prev) => ({
        ...prev,
        [field]: { isError: false, errorMessage: '' },
      }))
    }
  }

  const validateForm = () => {
    let isValid = true
    const newErrors = { ...INITIAL_FORM_ERROR }

    REQUIRED_FIELDS.forEach((field) => {
      const value = formData[field]
      if (!value || value.toString().trim() === '') {
        newErrors[field] = {
          isError: true,
          errorMessage: `${FIELD_LABELS[field]} is required`,
        }
        isValid = false
      }
    })

    setFormError(newErrors)
    return isValid
  }

  // ── API handler ─────────────────────────────────────────────────────────

  const handleSave = async () => {
    if (!validateForm()) return

    setIsButtonDisabled(true)
    try {
      const sourceData = {
        cppPlant: formData.cppPlant,
        procurementPlant: formData.procurementPlant,
        name: formData.name,
        displayName: formData.displayName,
        sapCode: formData.sapCode,
        uom: formData.uom,
      }

      if (isEditMode) {
        // Update existing source
        await InputApiService.updateSource(
          keycloak,
          editRowData.normParameterFkId,
          {
            procurementPlant: formData.procurementPlant,
            name: formData.name,
            displayName: formData.displayName,
            sapCode: formData.sapCode,
            uom: formData.uom,
          },
        )
      } else {
        // Add new source
        await InputApiService.addSource(keycloak, {
          ...sourceData,
          aopYear: AOP_YEAR,
        })
      }

      onClose()
      if (onSuccess) onSuccess()
    } catch (error) {
      console.error(
        isEditMode ? 'Error updating source:' : 'Error adding source:',
        error,
      )
    } finally {
      setIsButtonDisabled(false)
    }
  }

  const handleCancel = () => {
    onClose()
  }

  return (
    <Dialog open={open} onClose={handleCancel} maxWidth='sm' fullWidth>
      <DialogTitle sx={{ borderBottom: '1px solid #ccc' }}>
        <Typography variant='h6' className='dialog-title'>
          {isEditMode ? 'Edit Source' : 'Add Source'}
        </Typography>
      </DialogTitle>
      <DialogContent dividers>
        <Box sx={{ mt: 2 }}>
          <Grid container spacing={2}>
            {/* CPP Plant — dropdown */}
            <Grid item xs={6}>
              <TextField
                select
                fullWidth
                label={FIELD_LABELS.cppPlant}
                value={formData.cppPlant}
                onChange={(e) => handleFieldChange('cppPlant', e.target.value)}
                error={formError.cppPlant.isError}
                helperText={formError.cppPlant.errorMessage}
                required
                size='small'
                disabled={isEditMode}
              >
                {cppPlantOptions.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>

            {/* Procurement Plant — dropdown */}
            <Grid item xs={6}>
              <TextField
                select
                fullWidth
                label={FIELD_LABELS.procurementPlant}
                value={formData.procurementPlant}
                onChange={(e) =>
                  handleFieldChange('procurementPlant', e.target.value)
                }
                error={formError.procurementPlant.isError}
                helperText={formError.procurementPlant.errorMessage}
                required
                size='small'
                disabled={isEditMode}
              >
                {procurementPlantOptions.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>

            {/* Name */}
            <Grid item xs={6}>
              <TextField
                fullWidth
                label={FIELD_LABELS.name}
                value={formData.name}
                onChange={(e) => handleFieldChange('name', e.target.value)}
                error={formError.name.isError}
                helperText={formError.name.errorMessage}
                required
                size='small'
                autoFocus
              />
            </Grid>

            {/* Display Name */}
            <Grid item xs={6}>
              <TextField
                fullWidth
                label={FIELD_LABELS.displayName}
                value={formData.displayName}
                onChange={(e) =>
                  handleFieldChange('displayName', e.target.value)
                }
                error={formError.displayName.isError}
                helperText={formError.displayName.errorMessage}
                size='small'
              />
            </Grid>

            {/* SAP Code */}
            <Grid item xs={6}>
              <TextField
                fullWidth
                label={FIELD_LABELS.sapCode}
                value={formData.sapCode}
                onChange={(e) => handleFieldChange('sapCode', e.target.value)}
                error={formError.sapCode.isError}
                helperText={formError.sapCode.errorMessage}
                size='small'
              />
            </Grid>

            {/* UOM */}
            <Grid item xs={6}>
              <TextField
                fullWidth
                label={FIELD_LABELS.uom}
                value={formData.uom}
                onChange={(e) => handleFieldChange('uom', e.target.value)}
                error={formError.uom.isError}
                helperText={formError.uom.errorMessage}
                required
                size='small'
              />
            </Grid>
          </Grid>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button
          onClick={handleCancel}
          color='secondary'
          disabled={isButtonDisabled}
          className='btn-no'
        >
          Cancel
        </Button>
        <Button
          onClick={handleSave}
          variant='contained'
          disabled={isButtonDisabled || READ_ONLY}
          className='btn-save'
          startIcon={
            <Box component='img' src={SaveImageIcon} className='w16-icon' />
          }
        >
          {isButtonDisabled
            ? isEditMode
              ? 'Updating...'
              : 'Saving...'
            : isEditMode
              ? 'Update'
              : 'Save'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AddSourceDialog
