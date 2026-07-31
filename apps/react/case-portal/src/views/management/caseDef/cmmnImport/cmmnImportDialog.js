import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  LinearProgress,
  Stack,
  Typography,
} from '@mui/material'

import { useSession } from 'SessionStoreContext'
import { CmmnImportService, MenuEventService } from 'services'
import CmmnImportPreview from './cmmnImportPreview'

/**
 * Import a CMMN model as a case definition.
 *
 * Two steps on purpose. Picking a file runs a dry run and shows exactly what would
 * be created — including everything the importer could not map faithfully — and only
 * then is anything written. CMMN says more than the platform's stage model can hold,
 * so an import always leaves something behind; showing what, before committing, is
 * what makes the result reviewable rather than merely plausible.
 */
export const CmmnImportDialog = ({ open, handleClose }) => {
  const { t } = useTranslation()
  const keycloak = useSession()

  const [fileName, setFileName] = useState(null)
  // The model text is held so the commit re-sends exactly what was previewed —
  // re-reading the file could pick up an edit made in between.
  const [cmmnXml, setCmmnXml] = useState(null)
  const [preview, setPreview] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)
  const [imported, setImported] = useState(false)

  const reset = () => {
    setFileName(null)
    setCmmnXml(null)
    setPreview(null)
    setError(null)
    setImported(false)
    setBusy(false)
  }

  const close = () => {
    reset()
    handleClose()
  }

  const handleFileSelected = async (event) => {
    const file = event.target.files?.[0]
    // Clear the input so re-picking the same file after an error still fires.
    event.target.value = ''
    if (!file) return

    setFileName(file.name)
    setPreview(null)
    setError(null)
    setImported(false)
    setBusy(true)

    try {
      const text = await file.text()
      setCmmnXml(text)
      setPreview(
        await CmmnImportService.import(keycloak, text, { dryRun: true }),
      )
    } catch (err) {
      // The engine's message names the fix (e.g. "that is a rendered diagram —
      // export CMMN 1.1 XML"), so it is shown rather than a generic failure.
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const handleImport = async () => {
    setBusy(true)
    setError(null)

    try {
      await CmmnImportService.import(keycloak, cmmnXml, { dryRun: false })
      setImported(true)
      MenuEventService.triggerMenuUpdate()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open={open} onClose={close} fullWidth maxWidth='md'>
      <DialogTitle>{t('pages.cmmnImport.title')}</DialogTitle>

      <DialogContent dividers>
        <Stack spacing={2}>
          <Box>
            <Button variant='outlined' component='label' disabled={busy}>
              {t('pages.cmmnImport.chooseFile')}
              <input
                type='file'
                hidden
                accept='.cmmn,.xml,application/xml,text/xml'
                onChange={handleFileSelected}
              />
            </Button>
            {fileName && (
              <Chip
                label={fileName}
                size='small'
                sx={{ ml: 1 }}
                variant='outlined'
              />
            )}
          </Box>

          <Typography variant='caption' color='text.secondary'>
            {t('pages.cmmnImport.hint')}
          </Typography>

          {busy && <LinearProgress />}

          {error && <Alert severity='error'>{error}</Alert>}

          {imported && (
            <Alert severity='success'>
              {t('pages.cmmnImport.imported', {
                name: preview?.caseDefinition?.name,
              })}
            </Alert>
          )}

          {preview && !imported && (
            <>
              <Divider />
              <CmmnImportPreview result={preview} />
            </>
          )}
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={close}>
          {imported ? t('pages.cmmnImport.done') : t('pages.cmmnImport.cancel')}
        </Button>
        {preview && !imported && (
          <Button variant='contained' onClick={handleImport} disabled={busy}>
            {t('pages.cmmnImport.import')}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  )
}

export default CmmnImportDialog
