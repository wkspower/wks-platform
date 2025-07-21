import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { SlowdownNormForMegServices } from 'services/SlowdownNormForMegServices'
import { useSession } from 'SessionStoreContext'
import KendoDataTables from './index'

const SlowdownNormForMeg = () => {
  const keycloak = useSession()
  const { year, plantID, oldYear } = useSelector((state) => state.dataGridStore)

  const isOldYear = oldYear?.oldYear
  const plantId = plantID?.plantId
  const selectedYear = year?.selectedYear

  const [isLoading, setIsLoading] = useState(false)
  const [notification, setNotification] = useState({
    open: false,
    message: '',
    severity: 'info',
  })

  const [tableRows, setTableRows] = useState([])
  const [columnDefinitions, setColumnDefinitions] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [calculationResults, setCalculationResults] = useState([])

  const showNotification = useCallback((message, severity = 'info') => {
    setNotification({
      open: true,
      message,
      severity,
    })
  }, [])

  const closeNotification = useCallback(() => {
    setNotification((prev) => ({ ...prev, open: false }))
  }, [])

  const saveSlowdownConfiguration = useCallback(
    async (payload) => {
      setIsLoading(true)
      try {
        const response =
          await SlowdownNormForMegServices.updateSlowdownNormsForMeg({
            keycloak,
            plantId,
            year: selectedYear,
            payload,
          })

        if (response?.code === 200) {
          showNotification('Saved Successfully!', 'success')
          setModifiedCells({})
          await fetchSlowdownNormsColumns()
        } else {
          showNotification('Data Save Failed!', 'error')
        }

        return response
      } catch (error) {
        console.error('Error saving data:', error)
        showNotification('Data Save Failed!', 'error')
        throw error
      } finally {
        setIsLoading(false)
      }
    },
    [keycloak, plantId, selectedYear, showNotification],
  )

  const handleSaveChanges = useCallback(async () => {
    try {
      const modifiedData = Object.values(modifiedCells)

      if (modifiedData.length === 0) {
        showNotification('No Records to Save!', 'info')
        return
      }

      const sanitizedData = modifiedData.map((item) => ({
        ...item,
        normParameterFKId: item.NormParameter_FK_Id,
        NormParameter_FK_Id: undefined,
        inEdit: undefined,
        particulars: undefined,
        id: undefined,
        aopYear: undefined,
        normParameterDisplayName: undefined,
        plantId: undefined,
        DisplayName: undefined,
        NormTypeName: undefined,
        srNo: undefined,
        isEditable: undefined,
        IsEditable: undefined,
        Particulars: undefined,
        uom: undefined,
        UOM: undefined,
      }))

      await saveSlowdownConfiguration(sanitizedData)
    } catch (error) {
      console.error('Error in handleSaveChanges:', error)
    }
  }, [modifiedCells, saveSlowdownConfiguration, showNotification])

  const handleCalculateData = useCallback(async () => {
    setIsLoading(true)
    try {
      const response =
        await SlowdownNormForMegServices.getSlowdownNormsCalculateForMeg({
          keycloak,
          plantId,
          year: selectedYear,
        })

      if (response) {
        showNotification('Data refreshed successfully!', 'success')
        await fetchSlowdownNormsColumns()

        await fetchSlowdownNormsData()
      } else {
        showNotification('Data Refresh Failed!', 'error')
      }

      return response
    } catch (error) {
      console.error('Error refreshing data:', error)
      showNotification('Data Refresh Failed!', 'error')
    } finally {
      setIsLoading(false)
    }
  }, [keycloak, plantId, selectedYear, showNotification])

  const fetchSlowdownNormsData = useCallback(async () => {
    setIsLoading(true)
    try {
      const { data } =
        await SlowdownNormForMegServices.getSlowdownNormsDataForMeg({
          keycloak,
          plantId,
          year: selectedYear,
        })

      const formattedRows =
        data?.resultList?.map((item, index) => {
          const parsedItem = Object.entries(item).reduce(
            (acc, [key, value]) => {
              if (
                typeof value === 'string' &&
                !isNaN(value) &&
                value.trim() !== ''
              ) {
                const parsedValue = parseFloat(value)
                acc[key] = isNaN(parsedValue) ? value : parsedValue
              } else {
                acc[key] = value
              }
              return acc
            },
            {},
          )

          return {
            ...parsedItem,
            id: index,
            particulars: item.DisplayName,
            Particulars: item?.NormTypeName,
            isEditable: item?.IsEditable,
          }
        }) || []
      setTableRows(formattedRows)
      setCalculationResults(data?.aopCalculation || [])
    } catch (error) {
      console.error('Error fetching slowdown norms data:', error)
      setTableRows([])
      setCalculationResults([])
    } finally {
      setIsLoading(false)
    }
  }, [keycloak, plantId, selectedYear])

  const fetchSlowdownNormsColumns = useCallback(async () => {
    setIsLoading(true)
    setColumnDefinitions([])

    try {
      const response =
        await SlowdownNormForMegServices.getSlowdownNormsColumnsForMeg({
          keycloak,
          plantId,
          year: selectedYear,
        })

      const hiddenColumns = [
        'srNo',
        'NormTypeName',
        'DisplayName',
        'NormParameter_FK_Id',
        'normParameterDisplayName',
        'aopYear',
        'plantId',
        'IsEditable',
      ]

      if (response?.code === 200 && Array.isArray(response.data)) {
        const dynamicColumns = response.data.map((column) => ({
          field: column.field,
          title: column.title,
          editable:
            column.field === 'particulars' ||
            column.field.toLowerCase() === 'uom'
              ? false
              : true,
          hidden: hiddenColumns.includes(column.field),
          ...(column.field !== 'particulars' &&
            column.field.toLowerCase() !== 'uom' && {
              format: '{0:#.###}',
              type: 'number',
            }),
        }))

        setColumnDefinitions(dynamicColumns)
        await fetchSlowdownNormsData()
      } else {
        setColumnDefinitions([])
        setTableRows([])
      }
    } catch (error) {
      console.error('Error fetching column definitions:', error)
      setColumnDefinitions([])
      setTableRows([])
    } finally {
      setIsLoading(false)
    }
  }, [keycloak, plantId, selectedYear, fetchSlowdownNormsData])

  useEffect(() => {
    if (keycloak && plantId && selectedYear) {
      fetchSlowdownNormsColumns()
    }
  }, [keycloak, plantId, selectedYear, fetchSlowdownNormsColumns])

  const tablePermissions = useMemo(() => {
    const isCurrentYear = isOldYear !== 1
    const hasCalculationResults = calculationResults.length > 0

    return {
      saveBtn: isCurrentYear,
      showCalculate: isCurrentYear,
      allAction: isCurrentYear,
      showCalculateVisibility: hasCalculationResults,
    }
  }, [isOldYear, tableRows.length, calculationResults.length])

  return (
    <div>
      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        setRows={setTableRows}
        columns={columnDefinitions}
        rows={tableRows}
        saveChanges={handleSaveChanges}
        handleCalculate={handleCalculateData}
        snackbarData={{
          message: notification.message,
          severity: notification.severity,
        }}
        snackbarOpen={notification.open}
        setSnackbarOpen={closeNotification}
        fetchData={fetchSlowdownNormsColumns}
        permissions={tablePermissions}
        groupBy='Particulars'
      />
    </div>
  )
}

export default SlowdownNormForMeg
