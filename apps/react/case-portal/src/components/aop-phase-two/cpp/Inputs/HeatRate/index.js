import React from 'react'
import { Box, Stack } from '../../../../../../node_modules/@mui/material/index'
import GTHeatRate from './GTHeatRate'
import STGHeatRate from './STGHeatRate'
import HRSGHeatRate from './HRSGHeatRate'

const index = () => {
  return (
    <Box>
      <Stack sx={{ mb: 2 }}>
        <GTHeatRate />
      </Stack>
      <Stack sx={{ mb: 2 }}>
        <STGHeatRate />
      </Stack>
      <Stack>
        <HRSGHeatRate />
      </Stack>
    </Box>
  )
}

export default index
