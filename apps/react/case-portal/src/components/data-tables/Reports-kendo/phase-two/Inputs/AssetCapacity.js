import React, { useEffect, useState } from 'react'
import KendoDataTables from 'components/kendo-data-tables/index'
import { Box, Typography, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import NestedKendoTable from 'components/kendo-data-tables/NestedKendoTable/index'
import { Stack } from '../../../../../../node_modules/@mui/material/index'
import { InputApiService } from 'services/phase-two-services/inputApiService'
import { validateNestedRowDataWithRemarks } from 'components/Utilities/commonUtilityFunctions'

const dummyRowsData = [
  {
    assetId: '6e83b022-8950-434a-a80c-2db532ff6526',
    assetName: 'NMD-Power Plant-1',
    plantCode: '40NB',
    utilityDistributed: { name: 'POWERGEN', sapCode: '310027910' },
    utilityGenerated: { name: 'POWERGEN', sapCode: '310027910' },
    uom: 'MT',
    fixedMin: 0.0,
    fixedMax: 22.0,
    april: { min: 11.2, max: 22.0 },
    may: { min: 17.0, max: 22.0 },
    june: { max: 22.0 },
    july: { min: 18.0, max: 22.0 },
    aug: { max: 22.0 },
    sep: { min: 19.0, max: 22.0 },
    oct: { min: 18.0, max: 22.0 },
    nov: { min: 19.0, max: 22.0 },
    dec: { min: 17.0, max: 22.0 },
    jan: { min: 19.0, max: 22.0 },
    feb: { min: 18.0, max: 22.0 },
    march: { min: 19.0, max: 22.0 },
  },
]

const AssetCapacity = () => {
  const keycloak = useSession()
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
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
  const headerMap = generateHeaderNames(AOP_YEAR)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const valueFormat = ValueFormatterProduction()

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const columns = [
    { field: 'id', title: 'ID', hidden: true },
    {
      field: 'assetName',
      title: 'Asset Name',
      width: 120,
      minWidth: 120,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'plantCode',
      title: 'Plant Code',
      width: 80,
      minWidth: 80,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityDistributed.name',
      title: 'Utility Distributed',
      width: 100,
      minWidth: 100,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityDistributed.sapCode',
      title: 'Utility Distributed Code',
      width: 100,
      minWidth: 100,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.name',
      title: 'Utility Generated',
      width: 100,
      minWidth: 100,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.sapCode',
      title: 'Utility Generated Code',
      width: 100,
      minWidth: 100,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'uom',
      title: 'UOM',
      width: 80,
      minWidth: 80,
      type: 'text',
      editable: false,
    },
    {
      field: 'fixedMin',
      title: 'Fixed Min',
      width: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'fixedMax',
      title: 'Fixed Max',
      width: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    {
      title: headerMap[4],
      children: [
        {
          field: 'april.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'april.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[5],
      children: [
        {
          field: 'may.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'may.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[6],
      children: [
        {
          field: 'june.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'june.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[7],
      children: [
        {
          field: 'july.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'july.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[8],
      children: [
        {
          field: 'aug.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'aug.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[9],
      children: [
        {
          field: 'sep.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'sep.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[10],
      children: [
        {
          field: 'oct.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'oct.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[11],
      children: [
        {
          field: 'nov.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'nov.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[12],
      children: [
        {
          field: 'dec.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'dec.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[1],
      children: [
        {
          field: 'jan.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jan.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[2],
      children: [
        {
          field: 'feb.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'feb.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[3],
      children: [
        {
          field: 'march.min',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'march.max',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      field: 'remarks',
      title: 'Remarks',
      width: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]

  useEffect(() => {
    if (AOP_YEAR) {
      fetchAssetCapacityData(keycloak, AOP_YEAR)
      // setRows(dummyRowsData)
    }
  }, [AOP_YEAR])

  const fetchAssetCapacityData = async (keycloak, AOP_YEAR) => {
    setLoading(true)
    try {
      const res = await InputApiService.getAssetCapacity(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      let tempRes = res?.map((item, index) => {
        return {
          ...item,
          id: item.id || index + 1,
          remarks: item.remarks || '',
        }
      })
      setRows(tempRes)
      setOriginalRows(tempRes)
    } catch (error) {
      console.error('Error fetching asset capacity data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const permissions = {
    showAction: true,
    addButton: true,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showImport: true,
    downloadExcelBtnFromUI: true,
    ExcelName: `Asset Capacity - ${AOP_YEAR}`,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: screenTitle?.title,
  }

  const saveChanges = async () => {
    setLoading(true)
    console.log('modifiedCells', modifiedCells)
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length == 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    var rawData = Object.values(modifiedCells)
    const data = rawData.filter((row) => row.inEdit)
    if (data.length == 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    // Custom validation: If any row data is updated, remarks must be filled and different from original
    const fieldsToCheck = [
      'april.min',
      'april.max',
      'may.min',
      'may.max',
      'june.min',
      'june.max',
      'july.min',
      'july.max',
      'aug.min',
      'aug.max',
      'sep.min',
      'sep.max',
      'oct.min',
      'oct.max',
      'nov.min',
      'nov.max',
      'dec.min',
      'dec.max',
      'jan.min',
      'jan.max',
      'feb.min',
      'feb.max',
      'march.min',
      'march.max',
      'fixedMin',
      'fixedMax',
    ]
    const validationError = validateNestedRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'assetName',
    )

    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationError,
        severity: 'error',
      })
      setLoading(false)
      return
    }

    const payload = modifiedData

    try {
      console.log('payload', payload)

      const response = await InputApiService.saveAssetCapacity(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        payload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
    } catch (error) {
      console.error('Error saving asset capacity data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <NestedKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Asset Capacity Input'
        permissions={permissions}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
      />
    </Box>
  )
}

export default AssetCapacity
