import PropTypes from 'prop-types'
import { useTranslation } from 'react-i18next'
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  AlertTitle,
  Box,
  Chip,
  List,
  ListItem,
  ListItemText,
  Stack,
  Typography,
} from '@mui/material'
import DownOutlined from '@ant-design/icons/DownOutlined'

/**
 * What an import would create, and what it could not carry over.
 *
 * The warnings are expanded by default and given as much room as the summary. CMMN
 * is a richer language than the platform's stage model, so every real import leaves
 * something behind; a preview that showed only the happy result would be claiming a
 * fidelity the import does not have. Reading these aloud is how the mapping's
 * assumptions get confirmed with whoever drew the model.
 */
export const CmmnImportPreview = ({ result }) => {
  const { t } = useTranslation()

  const stages = result?.caseDefinition?.stages ?? []
  const warnings = result?.warnings ?? []
  const processes = result?.processes ?? []

  const taskCount = processes.reduce(
    (total, process) => total + (process.taskNames?.length ?? 0),
    0,
  )
  const milestoneCount = stages.reduce(
    (total, stage) => total + (stage.milestones?.length ?? 0),
    0,
  )

  return (
    <Stack spacing={2}>
      <Box>
        <Typography variant='h5'>{result?.caseDefinition?.name}</Typography>
        <Stack direction='row' spacing={1} sx={{ mt: 1 }}>
          <Chip
            size='small'
            label={t('pages.cmmnImport.summary.stages', {
              count: stages.length,
            })}
          />
          <Chip
            size='small'
            label={t('pages.cmmnImport.summary.tasks', { count: taskCount })}
          />
          <Chip
            size='small'
            label={t('pages.cmmnImport.summary.milestones', {
              count: milestoneCount,
            })}
          />
        </Stack>
      </Box>

      <Box>
        {stages.map((stage) => {
          const stageProcesses = processes.filter(
            (process) => process.stageName === stage.name,
          )
          const tasks = stageProcesses
            .filter((process) => process.autoStart)
            .flatMap((process) => process.taskNames ?? [])
          const optional = stageProcesses
            .filter((process) => !process.autoStart)
            .flatMap((process) => process.taskNames ?? [])

          return (
            <Box key={stage.name} sx={{ mb: 1.5 }}>
              <Typography variant='subtitle1'>
                {stage.index + 1}. {stage.name}
              </Typography>

              <Stack
                direction='row'
                spacing={0.5}
                sx={{ flexWrap: 'wrap', gap: 0.5 }}
              >
                {tasks.map((task) => (
                  <Chip
                    key={task}
                    size='small'
                    variant='outlined'
                    label={task}
                  />
                ))}
                {optional.map((task) => (
                  <Chip
                    key={task}
                    size='small'
                    variant='outlined'
                    color='info'
                    label={`${task} (${t('pages.cmmnImport.optional')})`}
                  />
                ))}
                {(stage.milestones ?? []).map((milestone) => (
                  <Chip
                    key={milestone.id}
                    size='small'
                    color='success'
                    variant='outlined'
                    label={`◆ ${milestone.name}`}
                  />
                ))}
              </Stack>
            </Box>
          )
        })}
      </Box>

      {warnings.length > 0 && (
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<DownOutlined />}>
            <Alert severity='info' sx={{ width: '100%', py: 0 }}>
              <AlertTitle sx={{ mb: 0 }}>
                {t('pages.cmmnImport.warnings.title', {
                  count: warnings.length,
                })}
              </AlertTitle>
            </Alert>
          </AccordionSummary>
          <AccordionDetails>
            <Typography variant='caption' color='text.secondary'>
              {t('pages.cmmnImport.warnings.intro')}
            </Typography>
            <List dense>
              {warnings.map((warning, index) => (
                <ListItem key={`${warning.code}-${warning.elementId}-${index}`}>
                  <ListItemText
                    primary={warning.message}
                    secondary={warning.elementId}
                    primaryTypographyProps={{ variant: 'body2' }}
                    secondaryTypographyProps={{ variant: 'caption' }}
                  />
                </ListItem>
              ))}
            </List>
          </AccordionDetails>
        </Accordion>
      )}
    </Stack>
  )
}

CmmnImportPreview.propTypes = {
  result: PropTypes.shape({
    caseDefinition: PropTypes.object,
    processes: PropTypes.array,
    warnings: PropTypes.array,
  }),
}

export default CmmnImportPreview
