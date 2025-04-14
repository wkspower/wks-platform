// components/FormValidation/ValidationErrorAlert.js
import { Alert, Typography } from '@mui/material'
import { useTranslation } from 'react-i18next'

/**
 * Component to display form validation errors
 */
const ValidationErrorAlert = ({ errors, open, onClose }) => {
  if (!open) return null

  const { t } = useTranslation()

  return (
    <Alert severity='error' sx={{ mb: 2 }} onClose={onClose}>
      <Typography variant='subtitle1' fontWeight='bold'>
        {t('pages.validation.pleaseCorrectErrors')}
      </Typography>
      {errors.length > 0 ? (
        <ul style={{ marginTop: '8px', paddingLeft: '20px' }}>
          {errors.map((error, index) => (
            <li key={index}>
              <strong>{error.field}:</strong> {error.message}
            </li>
          ))}
        </ul>
      ) : (
        <Typography>{t('pages.validation.requiredFieldsMissing')}</Typography>
      )}
    </Alert>
  )
}

export default ValidationErrorAlert
