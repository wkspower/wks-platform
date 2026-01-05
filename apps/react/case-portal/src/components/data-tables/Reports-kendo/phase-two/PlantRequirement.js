import React, { useEffect, useState } from 'react'
import KendoDataTables from 'components/kendo-data-tables/index'
import { Box, Typography, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'

const PlantRequirement = () => {
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
  const [rows, setRows] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Column definitions
  const columns = [
    {
      field: 'plantName',
      title: 'Process Plant',
      width: 150,
      minWidth:150,
      type: 'text',
      editable: false,
      hidden: false,
    },
    {
      field: 'plantCode',
      title: 'Plant Code',
      width: 120,
      minWidth:120,
      type: 'text',
      editable: false,
      hidden: true,
    },
    {
      field: 'cppUtilities',
      title: 'CPP Utilities',
      widthT: 120,
      minWidth:80,
      type: 'text',
      editable: false,
    },
    {
      field: 'cppUtiltiyIds',
      title: 'CPP Utility ID',
      widthT: 120,
      minWidth:80,
      type: 'text',
      editable: false,
    },
    {
      field: 'cppPlant',
      title: 'CPP Plant',
      widthT: 120,
      minWidth:80,
      type: 'text',
      editable: false,
    },
    {
      field: 'cppPlantId',
      title: 'CPP Plant ID',
      widthT: 100,
      minWidth:80,
      type: 'text',
      editable: false,
      hidden: true,
    },
    { field: 'uom', title: 'UOM', widthT: 60,minWidth:60, type: 'text', editable: false },
    {
      field: 'april',
      title: headerMap[4], // will be 'Apr-25' if AOP_YEAR is 2025-26
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'may',
      title: headerMap[5],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'june',
      title: headerMap[6],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'july',
      title: headerMap[7],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'aug',
      title: headerMap[8],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'sep',
      title: headerMap[9],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'oct',
      title: headerMap[10],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'nov',
      title: headerMap[11],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'dec',
      title: headerMap[12],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'jan',
      title: headerMap[1],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'feb',
      title: headerMap[2],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'march',
      title: headerMap[3],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'grandTotal',
      title: 'Grand Total',
      widthT: 120,
      type: 'number',
      format: valueFormat
    },
    // {
    //   field: 'remarks',
    //   title: 'Remarks',
    //   width: 250,
    //   type: 'textarea',
    //   editable: false,
    //   minWidth: 250,
    // },

  ]

  useEffect(() => {
    if(PLANT_ID && AOP_YEAR){
      fetchPlantRequirementData();
    }
  }, [PLANT_ID,AOP_YEAR])

  const fetchPlantRequirementData = async () => {
    setLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.getPlantRequirementData(
        keycloak,
        PLANT_ID,
        AOP_YEAR
      )
      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
     const formattedData = res?.map((item, index) => ({
        ...item,
        remarks:item.remarks || '',
        id: item?.id || index + 1,
      }))
      setRows(formattedData)

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
    saveBtn: false,
    allAction: true,
    downloadExcelBtnFromUI:true,
    ExcelName:`Plant Requirement - ${AOP_YEAR}`,
    // showImport:true,
    showTitleNameBusiness: true,
    showTitle:true,
    titleName: screenTitle?.title,
  }

  // Save handler with API call
  const saveChanges = async () => {
   
    setLoading(true)

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
    const payload = modifiedData;
    try {
      // Transform modifiedCells into the format expected by the API
      console.log('payload', payload)

      // Call the API to save changes
      // const response = await UtilityPlantApiServiceV2.savePlantRequirementData(
      //   keycloak,
      //   PLANT_ID,
      //   payload
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
      {/* <KendoDataTables */}
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title={permissions.showTitle ? permissions.titleName : ''}
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
        groupBy={['plantName']}
      />
    </Box>
  )
}

export default PlantRequirement
