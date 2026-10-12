import React, { useState } from 'react'
import { Box } from '@mui/material'
import AopTabs from 'components/AopTabs'
import Quality from './quality'
import PackagingConsumables from './packaging-consumables'

const QualityPackagingNorms = () => {
  const [tabIndex, setTabIndex] = useState(0)
  const defaultTabs = ['Quality', 'Packaging & Consumables'] //, 'Other Cost'

  return (
    <Box>
      <AopTabs
        tabIndex={tabIndex}
        setTabIndex={setTabIndex}
        tabs={defaultTabs}
      />
      <Box sx={{ mt: 2 }}>
        {tabIndex === 0 && <Quality />}
        {tabIndex === 1 && <PackagingConsumables />}
      </Box>
    </Box>
  )
}

export default QualityPackagingNorms
