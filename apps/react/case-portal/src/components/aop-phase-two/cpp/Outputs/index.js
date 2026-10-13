import React from 'react'
import { Box } from '@mui/material'
import AopTabs from 'components/AopTabs'
import { useSelector } from 'react-redux'

import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import useConfigurationTabs from 'components/aop-phase-two/common/hooks/useConfigurationTabs'
import HeatRate from './heat-rate'
import SRMapping from './sr-mapping'
import QtyCostReport from './qty-cost-report'
import Summary from './Summary'
import AverageAssetLoading from './average-asset-loading'
import UtilityRate from './utility-rate'
import MonthlyCalculatedNorms from './monthly-calculated-norms'
import SpinningMarginReport from './spinning-margin-report'

const Outputs = () => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { siteObject, verticalObject } = dataGridStore

  const lowerVertName = verticalObject?.name?.toLowerCase()
  const lowerSiteName = siteObject?.name?.toLowerCase()
  const IS_CPP = lowerVertName === 'cpp'

  // Tab management via custom hook (type = 'OutputReport')
  const { filteredTabs, tabIndex, setTabIndex, loading } =
    useConfigurationTabs('OutputReport')

  // Get current tab display name
  const currentTabName = filteredTabs[tabIndex]?.name

  // Render tab content based on display name
  const renderTabContent = () => {
    if (!currentTabName) return null

    switch (currentTabName) {
      case 'Heat Rate':
        return <HeatRate />
      case 'SR Mapping':
        return <SRMapping />
      case 'Norm Cost Report':
        return <QtyCostReport />
      case 'Average Asset Loading':
        return <AverageAssetLoading />
      case 'Summary':
        return <Summary />
      case 'Utility Rate':
        return <UtilityRate />
      case 'Monthly Calculated Norms':
        return <MonthlyCalculatedNorms />
      case 'Spinning Margin Maintained':
        return <SpinningMarginReport />

      default:
        return null
    }
  }

  const renderBySite = () => {
    switch (lowerSiteName) {
      case 'jmd':
      case 'vmd':
      case 'hmd':
      case 'dmd':
      default:
        return (
          <>
            {/* Tabs - sticky below StepperNav */}
            {filteredTabs.length > 0 && (
              <Box
                sx={{
                  position: 'sticky',
                  top: -1,
                  zIndex: 10,
                  backgroundColor: '#ffffff',
                  borderBottom: 1,
                  borderColor: 'divider',
                  mb: 1,
                }}
              >
                <AopTabs
                  tabIndex={tabIndex}
                  setTabIndex={setTabIndex}
                  tabs={filteredTabs.map((tab) => tab.name)}
                />
              </Box>
            )}

            {/* Tab Content */}
            <Box>{renderTabContent()}</Box>
          </>
        )
    }
  }

  if (!IS_CPP) return null

  return (
    <Box sx={{ p: 0 }}>
      <LoaderBackdrop open={!!loading} />
      {renderBySite()}
    </Box>
  )
}

export default Outputs
