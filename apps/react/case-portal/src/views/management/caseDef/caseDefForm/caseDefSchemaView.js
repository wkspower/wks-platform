/*
 * WKS Platform - Open-Source Project
 *
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 *
 * WKS Platform is licensed under the MIT License.
 *
 * Copyright (c) 2021 WKS Power Limited. All rights reserved.
 *
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
import Alert from '@mui/material/Alert'
import AlertTitle from '@mui/material/AlertTitle'
import Box from '@mui/material/Box'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { SCHEMA_VERSION, validateCaseDefinition } from 'utils/caseConfigSchema'

/**
 * Read-only "Schema view": renders the live case definition against the WKS
 * Case Configuration Standard so authors can see and verify the underlying
 * config instead of treating the wizard as a black box. Visible + validated;
 * not an editor and not an enforcement gate.
 */
export const CaseDefSchemaView = ({ caseDef }) => {
  const { valid, errors } = validateCaseDefinition(caseDef)
  const json = JSON.stringify(caseDef, null, 2)

  return (
    <Box sx={{ display: 'grid', gap: 2 }}>
      <Typography variant='h5'>
        WKS Case Configuration Standard (v{SCHEMA_VERSION})
      </Typography>
      <Typography variant='body2' color='text.secondary'>
        This case definition is validated against the published configuration
        schema. Saving is not blocked by validation.
      </Typography>

      {valid ? (
        <Alert severity='success'>Conforms to the Standard.</Alert>
      ) : (
        <Alert severity='error'>
          <AlertTitle>
            {errors.length} issue{errors.length === 1 ? '' : 's'} found
          </AlertTitle>
          <ul style={{ margin: 0, paddingLeft: '1.2rem' }}>
            {errors.map((err, i) => (
              <li key={i}>
                <code>{err.instancePath || '/'}</code> {err.message}
              </li>
            ))}
          </ul>
        </Alert>
      )}

      <TextField
        label='Case definition (JSON)'
        value={json}
        multiline
        minRows={12}
        InputProps={{
          readOnly: true,
          sx: { fontFamily: 'monospace', fontSize: 13 },
        }}
        fullWidth
      />
    </Box>
  )
}
