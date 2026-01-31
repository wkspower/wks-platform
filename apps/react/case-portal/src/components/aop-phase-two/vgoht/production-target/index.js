import React, { useState } from 'react'
import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import DesignCapacityGrid from './DesignCapacityGrid'
import MaxAchievedCapacityGrid from './MaxAchievedCapacityGrid'
import ProposedOperatingCapacityGrid from './ProposedOperatingCapacityGrid'
import PercentageSummaryGrid from './PercentageSummaryGrid'

const ProductionTarget = () => {
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Stack spacing={2} mt={2}>
        {/* Grid 1: Design Capacity - Editable with dropdown */}
        <DesignCapacityGrid
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          loading={loading}
          setLoading={setLoading}
        />

        {/* Grid 2: Max Achieved Capacity - Read-only with dropdown */}
        <MaxAchievedCapacityGrid
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          loading={loading}
          setLoading={setLoading}
        />

        {/* Grid 3: Proposed Operating Capacity - Editable with dropdown */}
        <ProposedOperatingCapacityGrid
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          loading={loading}
          setLoading={setLoading}
        />

        {/* Grid 4: Percentage Summary - Read-only, no dropdown */}
        <PercentageSummaryGrid
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          loading={loading}
          setLoading={setLoading}
        />
      </Stack>
    </Box>
  )
}

export default ProductionTarget
