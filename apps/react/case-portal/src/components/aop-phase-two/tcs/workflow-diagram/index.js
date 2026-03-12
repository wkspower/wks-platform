import { Box } from '@mui/material'
import { Stack } from '../../../../../node_modules/@mui/material/index'
import ProcessViewer from './ProcessViewer'

const WorkflowDiagram = () => {
  return (
    <Box sx={{ height: '100%', width: '100%' }}>
      <Stack>
        <ProcessViewer />
      </Stack>
    </Box>
  )
}

export default WorkflowDiagram
