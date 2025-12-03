import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { SlowdownNormForMegServices } from 'services/SlowdownNormForMegServices'
import { useSession } from 'SessionStoreContext'
import KendoDataTables from './index'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'

const SlowdownNormForMeg = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical

  const lowerVertName = vertName?.toLowerCase()

  const SCREEN_NAME = screenTitle?.title

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
  const valueFormat = ValueFormatterConsumption()
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
            PLANT_ID,
            year: AOP_YEAR,
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
    [keycloak, PLANT_ID, AOP_YEAR, showNotification],
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
          PLANT_ID,
          year: AOP_YEAR,
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
  }, [keycloak, PLANT_ID, AOP_YEAR, showNotification])

  const fetchSlowdownNormsData = useCallback(async () => {
    setIsLoading(true)
    try {
      const { data } =
        await SlowdownNormForMegServices.getSlowdownNormsDataForMeg({
          keycloak,
          PLANT_ID,
          year: AOP_YEAR,
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
  }, [keycloak, PLANT_ID, AOP_YEAR])

  const fetchSlowdownNormsColumns = useCallback(async () => {
    setIsLoading(true)
    setColumnDefinitions([])

    try {
      const response =
        await SlowdownNormForMegServices.getSlowdownNormsColumnsForMeg({
          keycloak,
          PLANT_ID,
          year: AOP_YEAR,
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
          widthT: column.field.toLowerCase() === 'uom' ? 90 : 150,

          editable:
            column.field === 'particulars' ||
            column.field.toLowerCase() === 'uom'
              ? false
              : true,

          hidden: hiddenColumns.includes(column.field),
          ...(column.field !== 'particulars' &&
            column.field.toLowerCase() !== 'uom' && {
              format: valueFormat,
              type: 'negativeNumber',
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
  }, [keycloak, PLANT_ID, AOP_YEAR, fetchSlowdownNormsData])

  useEffect(() => {
    if (keycloak && PLANT_ID && AOP_YEAR) {
      fetchSlowdownNormsColumns()
    }
  }, [keycloak, PLANT_ID, AOP_YEAR, fetchSlowdownNormsColumns])

  const tablePermissions = useMemo(() => {
    const isCurrentYear = isOldYear !== 1
    const hasCalculationResults = calculationResults.length > 0

    return {
      saveBtn: isCurrentYear,
      showCalculate: isCurrentYear,
      allAction: isCurrentYear,
      showCalculateVisibility: hasCalculationResults,
      downloadExcelBtnFromUI: true,
      ExcelName: `${lowerVertName}_${SCREEN_NAME}`,
      showTitleNameBusiness: true,
      titleName: `${SCREEN_NAME}`,
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
