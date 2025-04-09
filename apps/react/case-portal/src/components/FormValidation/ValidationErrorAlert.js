// components/FormValidation/ValidationErrorAlert.js
import React from 'react';
import { Alert, Typography } from '@mui/material';

/**
 * Component to display form validation errors
 */
const ValidationErrorAlert = ({ errors, open, onClose }) => {
  if (!open) return null;
  
  return (
    <Alert 
      severity="error" 
      sx={{ mb: 2 }}
      onClose={onClose}
    >
      <Typography variant="subtitle1" fontWeight="bold">
        Please correct the following errors in the form:
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
        <Typography>
          Required fields are missing or contain invalid values.
        </Typography>
      )}
    </Alert>
  );
};

export default ValidationErrorAlert;