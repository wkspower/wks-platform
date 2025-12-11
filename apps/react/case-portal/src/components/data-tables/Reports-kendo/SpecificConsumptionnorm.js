import React, { useEffect, useState } from 'react'
import { Box, Backdrop } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { MockSpecificConsumptionNormsAPI } from './MockSpecificConsumptionNormsAPI'
//F:\MNT\workspace\wks-platform\apps\react\case-portal\src\components\data-tables\Reports-kendo\MockSpecificConsumptionNormsAPI.js
// import { DataService } from 'services/DataService' // or your actual API service path
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import { DataService } from 'services/DataService'
import {
  CircularProgress,
  Typography,
} from '../../../../node_modules/@mui/material/index'

const specificConsumptionCategories = () => [
  {
    key: 'RawMaterial',
    title: 'Raw Material',
  },
  {
    key: 'ByProduct',
    title: 'By Product',
  },
  {
    key: 'CatChem',
    title: 'Catalysts & Chemicals',
  },
  {
    key: 'Utilities',
    title: 'Utilities',
  },
  {
    key: 'QualityParameters',
    title: 'Quality Parameters',
  },
  {
    key: 'OtherVariable',
    title: 'Other Variable',
  },
  {
    key: 'PackingConsumables',
    title: 'Packing & Consumables',
  },
]

// Categories that should have norms grid with their custom titles
const categoriesWithNorms = {
  RawMaterial: 'Raw Material',
  ByProducts: 'By Product',
  CatChem: 'Catalysts & Chemicals',
  UtilityConsumption: 'Utilities',
}

export default function SpecificConsumptionNorm() {
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
  const [normsData, setNormsData] = useState({})
  const [normColumns, setNormColumns] = useState([])
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const keycloak = useSession()
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [otherVariableRows, setOtherVariableRows] = useState([])
  const valueFormat = ValueFormatterConsumption()

  // Function to generate dynamic norm columns from backend data
  const generateNormColumns = (columnsFromBackend) => {
    if (!columnsFromBackend || columnsFromBackend.length === 0) {
      // Default columns if backend doesn't provide them
      return [
        { field: 'NormName', title: 'Particular', editable: false, flex: 1 },
        { field: 'UOM', title: 'UOM', editable: false, width: 100 },
      ]
    }
    return columnsFromBackend
  }

  const loadAll = async () => {
    setLoading(true)
    const out = {}
    const normsOut = {}
    let dynamicColumns = []

    await Promise.all(
      specificConsumptionCategories().map(async ({ key }) => {
        const { columns } = await MockSpecificConsumptionNormsAPI.getReport({
          category: key,
          AOP_YEAR,
          valueFormat,
        })
        const apiResp = await DataService.getSpecificConsumption(
          keycloak,
          key,
          PLANT_ID,
          AOP_YEAR,
        )
        let rows = []
        if (
          key === 'PackingConsumables' &&
          apiResp?.data?.packingConsumablesData
        ) {
          rows = apiResp.data.packingConsumablesData
        } else if (apiResp?.data?.plantProductionData) {
          rows = apiResp.data.plantProductionData
        }
        out[key] = { columns, rows }
      }),
    )

    await Promise.all(
      Object.keys(categoriesWithNorms).map(async (key) => {
        try {
          const normsResp = await DataService.getConsumptionNorms(
            keycloak,
            key,
            PLANT_ID,
            AOP_YEAR,
          )
          const normsRows = normsResp?.data?.data || []
          const allNormsColumns = normsResp?.data?.columns || []
          normsOut[key] = normsRows

          const columnToSkip = 'NormParameterTypeName'

          const normsColumns = allNormsColumns.filter(
            (col) => col.field !== columnToSkip,
          )

          if (normsColumns.length > 0 && dynamicColumns.length === 0) {
            dynamicColumns = normsColumns.map((col) => ({
              ...col,
              format: valueFormat,
            }))
          }
        } catch (error) {
          console.error(`Error loading norms for ${key}:`, error)
          normsOut[key] = []
        }
      }),
    )
    setReports(out)
    setNormsData(normsOut)
    setNormColumns(generateNormColumns(dynamicColumns))
    setLoading(false)
  }

  useEffect(() => {
    loadAll()
  }, [keycloak, AOP_YEAR, PLANT_ID, VERTICAL_ID])

  return (
    <Box sx={{ width: '100%' }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Typography component='div' className='grid-title' sx={{ mb: 0 }}>
        {'Specific Consumption Norms'}
      </Typography>

      {specificConsumptionCategories().map(({ key, title }) => {
        const rpt = reports[key] || {}
        return (
          <Box key={key} sx={{ mt: 0 }}>
            <KendoDataTablesReports
              columns={rpt.columns || []}
              rows={rpt.rows || []}
              title={title}
              setRows={() => {}}
              permissions={{
                textAlignment: 'center',
                showCalculate: false,
                showFinalSubmit: false,
                showTitle: true,
              }}
            />
          </Box>
        )
      })}

      {/* Section title for norms grids */}
      <Typography component='div' className='grid-title' sx={{ mt: 0, mb: 0 }}>
        {'Grade Wise Specific Consumption Norms'}
      </Typography>

      {/* Then, render all norms grids */}
      {Object.keys(categoriesWithNorms).map((key) => {
        const norms = normsData[key] || []
        const normsTitle = categoriesWithNorms[key]

        return (
          <Box key={`norms-${key}`} sx={{ mt: 0 }}>
            <KendoDataTablesReports
              columns={normColumns}
              rows={norms}
              title={normsTitle}
              setRows={() => {}}
              permissions={{
                textAlignment: 'center',
                showCalculate: false,
                showFinalSubmit: false,
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
