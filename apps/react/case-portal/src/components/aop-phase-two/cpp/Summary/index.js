import { useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import CppExecutionList from './CppExecutionList'
import MonthlyExecutionList from './MonthlyExecutionList'
import AssetStatusList from './AssetStatusList'

const Summary = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, siteObject, verticalObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const [loading, setLoading] = useState(false)
  const [selectedExecutionId, setSelectedExecutionId] = useState(null)
  const [selectedMonthData, setSelectedMonthData] = useState(null)

  // Handle view click from main execution list
  const handleExecutionViewClick = (dataItem) => {
    console.log('Execution view clicked:', dataItem)
    setSelectedExecutionId(dataItem.id)
    setSelectedMonthData(null) // Reset month selection when execution changes
  }

  // Handle view click from monthly execution list
  const handleMonthlyViewClick = (dataItem) => {
    console.log('Monthly view clicked:', dataItem)
    setSelectedMonthData(dataItem)
  }

  return (
    <Box sx={{ p: 2 }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Grid 1: CPP Execution List - Always visible */}
      <Box sx={{ mb: 3 }}>
        <CppExecutionList onViewClick={handleExecutionViewClick} />
      </Box>

      {/* Grid 2: Monthly Execution List - Shows when execution is selected */}
      {selectedExecutionId && (
        <Box sx={{ mb: 3 }}>
          <MonthlyExecutionList
            executionId={selectedExecutionId}
            onViewClick={handleMonthlyViewClick}
          />
        </Box>
      )}

      {/* Grid 3: Asset Status List - Shows when month is selected */}
      {selectedMonthData && (
        <Box sx={{ mb: 3 }}>
          <AssetStatusList
            executionId={selectedMonthData.parentExecutionFkId}
            month={selectedMonthData.month}
            financialYear={selectedMonthData.financialYear}
          />
        </Box>
      )}
    </Box>
  )
}

export default Summary
