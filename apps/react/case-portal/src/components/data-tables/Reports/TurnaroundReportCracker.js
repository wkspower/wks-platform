import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getShutdownConsumptionColDef from 'components/data-tables/CommonHeader/getShutdownConsumptionColDef'
import KendoDataTables from 'components/kendo-data-tables/index'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'

import { ShutdownNormsApiService } from 'services/shutdown-norms-api-service'

const TurnaroundReportCracker = () => {
  const [gradeId, setGradeId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [loading, setLoading] = useState(false)
  const menu = useSelector((state) => state.dataGridStore)
  const [shutdownMonths, setShutdownMonths] = useState([])
  const { yearChanged, oldYear, plantID } = menu
  const isOldYear = oldYear?.oldYear
  const [open1, setOpen1] = useState(false)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState([])
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [_plantID, set_PlantID] = useState('')
  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [rowModesModel, setRowModesModel] = useState({})
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,

    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [calculationObject, setCalculationObject] = useState([])
  const [grades, setGrades] = useState([])

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id

  const PLANT_NAME = plantObject?.name
  const SITE_NAME = siteObject?.name
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear

  const headerMap = generateHeaderNames(AOP_YEAR)

  const keycloak = useSession()

  useEffect(() => {
    fetchData()
  }, [
    oldYear,
    yearChanged,
    keycloak,
    selectedUnit,
    plantID,
    gradeId,
    lowerVertName,
  ])

  const isCellEditable = (params) => {
    return params.row.isEditable
  }

  const ShutdownConsumptionCrackerColumns = [
    {
      field: 'sapCode',
      headerName: 'SAP MAT Code',
      widthT: 130,
      editable: false,
    },
    {
      field: 'productName',
      headerName: 'Particulars',
      widthT: 130,
      editable: false,
    },
    { field: 'UOM', headerName: 'UOM', widthT: 60, editable: false },

    ...Array.from({ length: 12 }, (_, i) => {
      const monthIndex = (i + 4) % 12 || 12
      const monthField = new Date(2000, monthIndex - 1)
        .toLocaleString('en-US', { month: 'long' })
        .toLowerCase()

      return {
        field: monthField,

        type: 'number',
        format: '{0:0.000}',
        editable: false,
        isDisabled: true,
        monthNumber: monthIndex,
      }
    }),
  ]

  const enhancedColDefs = ShutdownConsumptionCrackerColumns.map((col) => {
    if (col.monthNumber) {
      const monthNum = col.monthNumber

      return {
        ...col,
        headerName: headerMap?.[monthNum] || col.field,
        editable: false,
      }
    }

    return col
  })

  const colDefs = enhancedColDefs
  const fetchData = async (gradeId) => {
    try {
      setLoading(true)
      setRows([])

      let data = []

      data = await ShutdownNormsApiService.shutdownConsumptionHistoryData(
        keycloak,
        gradeId,
        PLANT_ID,
        AOP_YEAR,
      )

      if (data?.code != 200) {
        setRows([])
        setLoading(false)
        return
      }

      let formattedData = []

      {
        const crackerArray = Array.isArray(data?.data?.mcuNormsValueDTOList)
          ? data.data.mcuNormsValueDTOList
          : Array.isArray(data?.data)
            ? data.data
            : []
        formattedData = crackerArray.map((item, index) => {
          const baseItem = {
            ...item,
            idFromApi: item.id,
            id: index,
            materialFkId: item?.materialFkId?.toLowerCase(),
            Particulars: item.normParameterTypeDisplayName || 'Type',
            isEditable: false,
            originalRemark: item.remarks,
          }
          return baseItem
        })
      }

      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      showCalculate: false,
      allAction: true,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      units: ['TPH', 'TPD'],
      saveWithRemark: false,
    },
    isOldYear,
  )

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        isCellEditable={isCellEditable}
        title='Turnaround'
        columns={colDefs}
        setRows={setRows}
        rows={rows}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        rowModesModel={rowModesModel}
        open1={open1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        groupBy='Particulars'
        permissions={adjustedPermissions}
      />
    </div>
  )
}

export default TurnaroundReportCracker
