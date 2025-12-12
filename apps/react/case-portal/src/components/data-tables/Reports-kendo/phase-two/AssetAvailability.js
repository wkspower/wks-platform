import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import { dummyDataForAssetAvailability } from './nestedDummyData'
import { flattenMonthObject, unflattenMonthObject } from 'components/Utilities/commonUtilityFunctions'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'

const AssetAvailability = () => {
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
  
  // Column definitions
  const columns = [
    //Generating Plant
    {
      field: 'assetName',
      title: 'Asset Name',
      width: 150,
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
    // Apr
    {
      title: headerMap[4],
      children: [
        { field: 'apr_availability', title: 'Availability', width: 80, editable: true,type: 'boolean',format: valueFormat},
        { field: 'apr_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'apr_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'apr_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'apr_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    // May
    {
      title: headerMap[5],
      children: [
        { field: 'may_availability', title: 'Availability', width: 80, editable: true,type: 'boolean',format: valueFormat},
        { field: 'may_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'may_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'may_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'may_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    // Jun
    {
      title: headerMap[6],
      children: [
        { field: 'jun_availability', title: 'Availability', width: 80, editable: true,type: 'boolean'},
        { field: 'jun_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'jun_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'jun_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'jun_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    // Jul
    {
      title: headerMap[7],
      children: [
        { field: 'jul_availability', title: 'Availability', width: 80, editable: true,type: 'boolean'},
        { field: 'jul_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'jul_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'jul_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'jul_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    // Aug
    {
      title: headerMap[8],
      children: [
        { field: 'aug_availability', title: 'Availability', width: 80, editable: true,type: 'boolean'},
        { field: 'aug_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'aug_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'aug_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'aug_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    // Sep
    {
      title: headerMap[9],
      children: [
        { field: 'sep_availability', title: 'Availability', width: 80, editable: true,type: 'boolean'},
        { field: 'sep_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'sep_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'sep_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'sep_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    // Oct
    {
      title: headerMap[10],
      children: [
        { field: 'oct_availability', title: 'Availability', width: 80, editable: true,type: 'boolean'},
        { field: 'oct_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'oct_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'oct_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'oct_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    // Nov
    {
      title: headerMap[11],
      children: [
        { field: 'nov_availability', title: 'Availability', width: 80, editable: true,type: 'boolean'},
        { field: 'nov_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'nov_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'nov_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'nov_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    //Dec
    {
      title: headerMap[12],
      children: [
        { field: 'dec_availability', title: 'Availability', width: 80, editable: true,type: 'boolean'},
        { field: 'dec_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'dec_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'dec_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'dec_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    //Jan
    {
      title: headerMap[1],
      children: [
        { field: 'jan_availability', title: 'Availability', width: 80, editable: true,type: 'boolean'},
        { field: 'jan_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'jan_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'jan_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'jan_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    //Feb
    {
      title: headerMap[2],
      children: [
        { field: 'feb_availability', title: 'Availability', width: 80, editable: true,type: 'boolean'},
        { field: 'feb_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'feb_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'feb_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'feb_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    //mar
    {
      title: headerMap[3],
      minWidth: 320,
      children: [
        { field: 'mar_availability', title: 'Availability', width: 80, editable: true,type: 'boolean'},
        { field: 'mar_operationHours', title: 'Operation Hours', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'mar_priority', title: 'Priority', width: 80, minWidth: 80, editable: true, type: 'number1',format: valueFormat },
        { field: 'mar_minCapacity', title: 'Min Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
        { field: 'mar_maxCapacity', title: 'Max Capacity', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
  ]
  
  const [rows, setRows] = useState([])

  useEffect(() => {
    if (PLANT_ID) {
      setRows(flattenMonthObject(dummyDataForAssetAvailability))
    }
  }, [PLANT_ID,AOP_YEAR])

  const fetchPlantRequirementData = async () => {
    setLoading(true)
    try {        
      const res = await UtilityPlantApiServiceV2.getNormBasedUtilityBudget(
        keycloak,
        PLANT_ID,
        AOP_YEAR
      )
      
      if (res?.data?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      console.log('res', res)
      setRows(flattenMonthObject(res?.data)) 
      setSnackbarOpen(true)
      // setSnackbarData({
      //   message: 'Data fetched successfully!',
      //   severity: 'success',
      // })
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
    showExport: false,
    showImport: false,
    showTitle:true,
  }

  // Save handler with API call
  const saveChanges = async () => {
    setLoading(true)
    console.log('modifiedCells',modifiedCells)
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

    const payload = unflattenMonthObject(modifiedData)
    
    try {
      // Transform modifiedCells into the format expected by the API
     
      console.log('payload', payload)

      // Call the API to save changes
      // NOTE: Update this API call to expect nested format when ready
      // const response = await UtilityPlantApiServiceV2.savePlantRequirementData(
      //   keycloak,
      //   PLANT_ID,
      //   payload  // Now sending nested format: { apr: { norms, quantity, ... } }
      // )

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

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Asset Availability'
        permissions={permissions}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
      />
    </Box>
  )
}

export default AssetAvailability
