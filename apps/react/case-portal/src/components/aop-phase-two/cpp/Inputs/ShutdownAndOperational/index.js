import { useState } from 'react'
import { Box, Stack } from '@mui/material'
import HoursGrid from './HoursGrid'
import PowerGrid from './PowerGrid'
import STGGrid from './STGGrid'

const ShutdownAndOperational = () => {
  const [hoursRows, setHoursRows] = useState([])

  return (
    <Box>
      <Stack sx={{ mb: 2 }}>
        <HoursGrid onHoursRowsChange={setHoursRows} />
      </Stack>
      <Stack sx={{ mb: 2 }}>
        <PowerGrid hoursRows={hoursRows} />
      </Stack>
      <Stack>
        <STGGrid hoursRows={hoursRows} />
      </Stack>
    </Box>
  )
}

export default ShutdownAndOperational
