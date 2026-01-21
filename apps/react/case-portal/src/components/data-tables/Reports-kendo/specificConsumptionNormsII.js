import React, { useEffect, useState } from 'react'
import { Box, Backdrop } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { MockSpecificConsumptionNormsIIAPI } from './MockSpecificConsumptionNormsIIAPI'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import { SpecificConsumptionService } from 'services/SpecificConsumptionService'
import {
  CircularProgress,
  Typography,
} from '../../../../node_modules/@mui/material/index'

const specificConsumptionCategories = () => [
  {
    key: 'RAWMATERIAL',
    title: 'Raw Material',
  },
  {
    key: 'BYPRODUCT',
    title: 'By Product',
  },
  {
    key: 'CatChem',
    title: 'Catalysts & Chemicals',
  },
  {
    key: 'ProductionCostCalculations',
    title: 'Production Cost Calculations',
  },
  {
    key: 'OtherVariableCost',
    title: 'Other Variable Cost',
  },
  {
    key: 'ProductMixAndProduction',
    title: 'Product Mix And Production',
  },
]

export default function SpecificConsumptionNormsII() {
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
  const [loading, setLoading] = useState(false)
  const [reports, setReports] = useState({})
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const keycloak = useSession()
  const [modifiedCells, setModifiedCells] = useState({})
  // Create separate state for each grid
  const [gridStates, setGridStates] = useState({})

  const valueFormat = ValueFormatterConsumption()

  // Initialize grid states
  useEffect(() => {
    const initialStates = {}
    specificConsumptionCategories().forEach(({ key }) => {
      initialStates[key] = {
        remarkDialogOpen: false,
        currentRemark: '',
        currentRowId: null,
        modifiedCells: {},
      }
    })
    setGridStates(initialStates)
  }, [])

  // Helper functions to update individual grid state
  const updateGridState = (key, updates) => {
    setGridStates((prev) => ({
      ...prev,
      [key]: {
        ...prev[key],
        ...updates,
      },
    }))
  }

  const handleRemarkCellClick = (key, row) => {
    updateGridState(key, {
      currentRemark: row.remarks || '',
      currentRowId: row.id,
      remarkDialogOpen: true,
    })
  }

  const updateCategoryRows = (key, updatedRows) => {
    setReports((prev) => ({
      ...prev,
      [key]: {
        ...prev[key],
        rows: updatedRows,
      },
    }))
  }

  // Handle modified cells update and sync with rows
  const handleModifiedCellsUpdate = (key, modifiedCells) => {
    updateGridState(key, { modifiedCells })

    // Update the rows in reports to reflect the changes
    setReports((prev) => {
      const currentRows = prev[key]?.rows || []
      const updatedRows = currentRows.map((row) => {
        const modifiedRow = modifiedCells[row.id]
        return modifiedRow ? { ...row, ...modifiedRow } : row
      })

      return {
        ...prev,
        [key]: {
          ...prev[key],
          rows: updatedRows,
        },
      }
    })
  }

  const saveChanges = async (key) => {
    try {
      setLoading(true)
      const modifiedCells = gridStates[key]?.modifiedCells || {}
      const data = Object.values(modifiedCells)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      // Get the original rows to merge with modified data
      const currentRows = reports[key]?.rows || []

      // Map data with proper field handling
      const payload = data.map((item) => {
        // Find the original row to get all fields
        const originalRow = currentRows.find((row) => row.id === item.id) || {}

        return {
          sno: item.sno ?? originalRow.sno,
          id: item.id ?? originalRow.id,
          material: item.material ?? originalRow.material,
          price: item.price ?? originalRow.price,
          uom: item.uom ?? item.unit ?? originalRow.uom ?? originalRow.unit,
          design: item.design ?? originalRow.design,
          designRsMt: item.designRsMt ?? originalRow.designRsMt,
          bestAchivedActual:
            item.bestAchivedActual ?? originalRow.bestAchivedActual,
          bestAchivedActualRsMT:
            item.bestAchivedActualRsMT ?? originalRow.bestAchivedActualRsMT,
          globalBenchmark: item.globalBenchmark ?? originalRow.globalBenchmark,
          globalBenchmarkRsMT:
            item.globalBenchmarkRsMT ?? originalRow.globalBenchmarkRsMT,
          budgetPrevYear: item.budgetPrevYear ?? originalRow.budgetPrevYear,
          budgetPrevYearRsMT:
            item.budgetPrevYearRsMT ?? originalRow.budgetPrevYearRsMT,
          actualPrevYear: item.actualPrevYear ?? originalRow.actualPrevYear,
          actualPrevYearRsMT:
            item.actualPrevYearRsMT ?? originalRow.actualPrevYearRsMT,
          proposedBudget: item.proposedBudget ?? originalRow.proposedBudget,
          proposedBudgetRsMT:
            item.proposedBudgetRsMT ?? originalRow.proposedBudgetRsMT,
          plantFkId: item.plantFkId ?? originalRow.plantFkId ?? PLANT_ID,
          aopYear: item.aopYear ?? originalRow.aopYear ?? AOP_YEAR,
          remarks: item.remarks ?? originalRow.remarks ?? '',
        }
      })

      console.log('Saving payload:', payload) // Debug log

      // Save to API
      const response =
        await SpecificConsumptionService.saveSpecificConsumptionII(
          keycloak,
          payload,
          PLANT_ID,
          AOP_YEAR,
        )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        updateGridState(key, { modifiedCells: {} })
        await loadAll()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Save failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Save error:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: error?.message || 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const loadAll = async () => {
    setLoading(true)
    try {
      const out = {}
      await Promise.all(
        specificConsumptionCategories().map(async ({ key }) => {
          try {
            const { columns } =
              await MockSpecificConsumptionNormsIIAPI.getReport({
                category: key,
                AOP_YEAR,
                valueFormat,
              })
            const apiResp =
              await SpecificConsumptionService.getSpecificConsumptionII(
                keycloak,
                key,
                PLANT_ID,
                AOP_YEAR,
              )
            const rows = apiResp?.data?.plantProductionData || []
            out[key] = { columns, rows }
          } catch (error) {
            console.error(`Error loading data for ${key}:`, error)
            out[key] = { columns: [], rows: [] }
          }
        }),
      )
      setReports(out)
    } catch (error) {
      console.error('Error in loadAll:', error)
      setSnackbarData({
        message: 'Error loading data. Please try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (keycloak && PLANT_ID && AOP_YEAR) {
      loadAll()
    }
  }, [keycloak, AOP_YEAR, PLANT_ID, VERTICAL_ID])

  return (
    <Box sx={{ width: '100%' }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Typography component='div' className='grid-title' sx={{ mb: 0 }}>
        {`Specific Consumption Norms (T-17)`}
      </Typography>

      {specificConsumptionCategories().map(({ key, title }) => {
        const rpt = reports[key] || {}
        const gridState = gridStates[key] || {
          remarkDialogOpen: false,
          currentRemark: '',
          currentRowId: null,
          modifiedCells: {},
        }

        return (
          <Box key={key} sx={{ mt: 0 }}>
            <KendoDataTablesReports
              columns={rpt.columns || []}
              rows={rpt.rows || []}
              title={title}
              setRows={(updaterFn) => {
                // Handle both direct values and updater functions
                if (typeof updaterFn === 'function') {
                  const currentRows = rpt.rows || []
                  const newRows = updaterFn(currentRows)
                  updateCategoryRows(key, newRows)
                } else {
                  updateCategoryRows(key, updaterFn)
                }
              }}
              // Pass individual grid state
              remarkDialogOpen={gridState.remarkDialogOpen}
              setRemarkDialogOpen={(value) =>
                updateGridState(key, { remarkDialogOpen: value })
              }
              currentRemark={gridState.currentRemark}
              setCurrentRemark={(value) =>
                updateGridState(key, { currentRemark: value })
              }
              currentRowId={gridState.currentRowId}
              setCurrentRowId={(value) =>
                updateGridState(key, { currentRowId: value })
              }
              modifiedCells={gridState.modifiedCells}
              setModifiedCells={(value) =>
                handleModifiedCellsUpdate(key, value)
              }
              loading={loading}
              handleRemarkCellClick={(row) => handleRemarkCellClick(key, row)}
              // Pass save function for this specific category
              saveChanges={() => saveChanges(key)}
              permissions={{
                customHeight: { mainBox: '32vh', otherBox: '100%' },
                textAlignment: 'center',
                remarksEditable: true,
                showCalculate: false,
                saveBtnForRemark: true,
                saveBtn: true,
                showWorkFlowBtns: true,
                showTitle: true,
              }}
            />
          </Box>
        )
      })}

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}
