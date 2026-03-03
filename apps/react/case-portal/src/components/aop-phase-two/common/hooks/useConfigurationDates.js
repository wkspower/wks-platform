import { useState, useEffect, useCallback, useRef } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { InputApiService } from 'components/aop-phase-two/services/cpp/inputApiService'

/**
 * Custom hook to fetch and manage configuration dates (startDate and endDate)
 * from the AOP Design Basis configuration
 *
 * @returns {Object} - { startDate, endDate, loading, error }
 */
const useConfigurationDates = () => {
  const hasExecutedRef = useRef(false)
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  const [startDate, setStartDate] = useState(null)
  const [endDate, setEndDate] = useState(null)
  const [configurationExecutionDetails, setConfigurationExecutionDetails] =
    useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  // Fetch configuration execution details
  const getConfigurationExecutionDetails = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await InputApiService.getConfigurationExecutionDetails(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const details = response?.data || []
      if (details.length === 0) {
        console.warn(
          'getConfigurationExecutionDetails returned an empty array:',
          response,
        )
        setError('AOP Design Basis not configured')
        setLoading(false)
        return
      }
      setConfigurationExecutionDetails(details)
    } catch (err) {
      console.error('Error fetching getConfigurationExecutionDetails:', err)
      setError('Failed to fetch configuration details')
    } finally {
      setLoading(false)
    }
  }

  // Compute and set dates based on configuration details
  const computeAndSetDates = useCallback(() => {
    if (!configurationExecutionDetails.length) return

    const getDateValue = (name) => {
      const item = configurationExecutionDetails.find(
        (item) => item.Name === name,
      )
      return item?.AttributeValue ? new Date(item.AttributeValue) : null
    }

    setStartDate(getDateValue('StartDate'))
    setEndDate(getDateValue('EndDate'))
  }, [configurationExecutionDetails])

  // Initialize on mount and when PLANT_ID/AOP_YEAR changes
  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    hasExecutedRef.current = false
    getConfigurationExecutionDetails()
  }, [PLANT_ID, AOP_YEAR])

  // Compute dates when configuration details change
  useEffect(() => {
    computeAndSetDates()
  }, [computeAndSetDates])

  return {
    startDate,
    endDate,
    loading,
    error,
  }
}

export default useConfigurationDates
