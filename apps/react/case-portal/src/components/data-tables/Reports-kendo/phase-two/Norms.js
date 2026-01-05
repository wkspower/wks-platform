import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import KendoDataTables from 'components/kendo-data-tables/index'
import { min } from 'lodash'
import { nestedDummyRows } from './nestedDummyData'
import {
  flattenMonthObject,
  unflattenMonthObject,
} from 'components/Utilities/commonUtilityFunctions'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'
import NestedKendoTable from 'components/kendo-data-tables/NestedKendoTable/index'
import { Stack, Typography } from '../../../../../node_modules/@mui/material/index'

const Norms = () => {
  const keycloak = useSession()
  // State management

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
  const valueFormat = ValueFormatterProduction()

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Column definitions
  const nestedColumns = [
    //Generating Plant
    {
      field: 'generatingPlantName',
      title: 'Generating Plant',
      width: 150,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    //Utility
    {
      field: 'utilityName',
      title: 'Utility',
      width: 120,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    // Utility ID
    {
      field: 'utilityId',
      title: 'Utility ID',
      width: 120,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    //UOM
    {
      field: 'uom',
      title: 'UOM',
      width: 80,
      type: 'text',
      editable: false,
      minWidth: 80,
    },
    // Account
    {
      field: 'accountName',
      title: 'Account',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // Material
    {
      field: 'materialName',
      title: 'Material',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // SAP Code
    {
      field: 'materialId',
      title: 'SAP Code',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // Issuing Plant
    {
      field: 'issuingPlantName',
      title: 'Issuing Plant',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    {
      field: 'issuingUom',
      title: 'Issuing UOM',
      width: 80,
      type: 'text',
      editable: false,
      minWidth: 80,
    },
    // Apr
    {
      title: headerMap[4],
      children: [
        {
          field: 'apr.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'apr.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'apr.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'apr.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'apr.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'apr.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // May
    {
      title: headerMap[5],
      children: [
        {
          field: 'may.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'may.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'may.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'may.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'may.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'may.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Jun
    {
      title: headerMap[6],
      children: [
        {
          field: 'jun.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jun.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jun.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jun.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jun.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jun.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Jul
    {
      title: headerMap[7],
      children: [
        {
          field: 'jul.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jul.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jul.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jul.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jul.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jul.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Aug
    {
      title: headerMap[8],
      children: [
        {
          field: 'aug.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'aug.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'aug.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'aug.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'aug.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'aug.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Sep
    {
      title: headerMap[9],
      children: [
        {
          field: 'sep.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'sep.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'sep.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'sep.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'sep.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'sep.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Oct
    {
      title: headerMap[10],
      children: [
        {
          field: 'oct.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'oct.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'oct.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'oct.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'oct.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'oct.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Nov
    {
      title: headerMap[11],
      children: [
        {
          field: 'nov.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'nov.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'nov.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'nov.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'nov.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'nov.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    //Dec
    {
      title: headerMap[12],
      children: [
        {
          field: 'dec.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'dec.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'dec.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'dec.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'dec.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'dec.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    //Jan
    {
      title: headerMap[1],
      children: [
        {
          field: 'jan.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jan.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jan.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minWidth: 80,
        },
        {
          field: 'jan.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jan.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jan.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    //Feb
    {
      title: headerMap[2],
      children: [
        {
          field: 'feb.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'feb.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'feb.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minWidth: 80,
        },
        {
          field: 'feb.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'feb.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'feb.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    //Mar
    {
      title: headerMap[3],
      minWidth: 320,
      children: [
        {
          field: 'mar.qty',
          title: 'Qty',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'mar.generationUom',
          title: 'Generation UOM',
          width: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'mar.norms',
          title: 'Norms',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minWidth: 80,
        },
        {
          field: 'mar.quantity',
          title: 'Quantity',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'mar.amount',
          title: 'Amount',
          width: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'mar.price',
          title: 'Price',
          width: 80,
          minWidth: 80,
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

  const [rows, setRows] = useState([])
  const [calculationLoading, setCaculationLoading] = useState(false)

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchNormsData()
      setModifiedCells({})
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchNormsData = async () => {
    setLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.getNormBasedUtilityBudget(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (res?.data?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      let tempRes = res?.data?.map((item, index) => {
        return { ...item, id: item.id || index + 1,remarks: item.remarks || '' }
      })

      // setRows(flattenMonthObject(res?.data))
      setRows(tempRes)
    } catch (error) {
      console.error('Error fetching fixed consumption data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  // Permissions (adjust as needed)
  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showTitleNameBusiness: true,
    titleName: screenTitle?.title,
    showImport: true,
    showTitle: true,
    showCalculate: true,
    downloadExcelBtnFromUI:true,
    ExcelName:`Norms - ${AOP_YEAR}`
  }

  // Calculate Norms data via API
  const handleCalculate = async () => {
    setCaculationLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.calculateNormsData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (res) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Calculation completed successfully!',
          severity: 'success',
        })
        // Refresh the data after calculation
        await fetchNormsData()
      }
    } catch (error) {
      console.error('Error calculating norms data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error during calculation. Please try again.',
        severity: 'error',
      })
    } finally {
      setCaculationLoading(false)
    }
  }

  // Save handler with API call
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

    const payload = modifiedData
    const tempPayload=payload?.map((item)=>{
      const { normHeaderId,id,inEdit, ...rest } = item
      return {
        ...rest,
       normsHeaderFkId:normHeaderId
      }
    })

    try {
      // Transform modifiedCells into the format expected by the API

      console.log('payload', tempPayload)

      // Call the API to save changes
      // NOTE: Update this API call to expect nested format when ready
      const response = await UtilityPlantApiServiceV2.saveNormsData(
        keycloak,
        tempPayload, // Now sending nested format: { apr: { norms, quantity, ... } }
        AOP_YEAR,
      )

      // Update the local state with the saved data
      // setRows(updatedRows)
      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
    } catch (error) {
      console.error('Error saving plant requirement data:', error)
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
        open={!!loading || calculationLoading}
      >
        <Stack display="flex" flexDirection="column" alignItems="center" justifyContent="center">
        <CircularProgress color='inherit' />
        {calculationLoading && <Typography variant="h5" sx={{ mt: 2 }}>Your data is being processed. This may take a few moments—thank you for your patience.</Typography>}
        </Stack>
      </Backdrop>
      <NestedKendoTable
        columns={nestedColumns}
        rows={rows}
        setRows={setRows}
        handleCalculate={handleCalculate}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Norms'
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
        groupBy={['generatingPlantName', 'accountName']}
      />
    </Box>
  )
}

export default Norms
