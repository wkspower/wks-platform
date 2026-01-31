import { useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import TabSection from '../../common/utilities/Tabs'
import ShutdownActivities from './ShutdownActivities'
import ShutdownHistoryConfig from './ShutdownHistoryConfig'

const ShutdownActivitiesContainer = () => {
  const [tabIndex, setTabIndex] = useState(0)
  const [loading, setLoading] = useState(false)

  const tablist = ['Shutdown/TA Activities', 'Shutdown History Config']

  const renderTab = () => {
    switch (tabIndex) {
      case 0:
        return <ShutdownActivities />
      case 1:
        return <ShutdownHistoryConfig />
      default:
        return null
    }
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* <Box>
        <TabSection
          tabIndex={tabIndex}
          setTabIndex={setTabIndex}
          tabs={tablist}
        />
      </Box> */}

      {/* Tab Content */}
      {/* <Box sx={{ mt: 2 }}>{renderTab()}</Box> */}
      <ShutdownActivities />
    </div>
  )
}

export default ShutdownActivitiesContainer
