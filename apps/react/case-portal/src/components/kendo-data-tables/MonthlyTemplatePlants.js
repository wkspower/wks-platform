import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { Backdrop, Box, CircularProgress } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import { useSelector } from 'react-redux'
export default function MonthlyTemplatePlants() {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
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
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const thisYear = AOP_YEAR
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const unsavedChangesRef = useRef({ unsavedRows: {}, rowsBeforeChange: {} })
  const isOldYear = false
  const columns = useMemo(
    () => [
      {
        field: 'CB_MaintenanceCostType',
        title: 'Maintenance Cost Type',
        width: 120,
        editable: false,
        type: 'number1',
      },
      {
        field: 'CB_MaterialCost',
        title: 'Material Cost',
        width: 120,
        editable: true,
        type: 'number1',
      },

      {
        title: 'Consumption Budget',
        children: [
          {
            field: 'CB_ServiceCost',
            title: 'Service Cost',
            width: 120,
            editable: true,
            type: 'number1',
          },
        ],
      },

      {
        field: 'CB_TotalCost',
        title: 'Total Cost',
        width: 120,
        editable: true,
        type: 'number1',
      },
      {
        field: 'CB_Months',
        title: 'Months',
        width: 120,
        editable: false,
        type: 'number1',
      },

      {
        field: 'CB_MaintenanceCostType',
        title: 'Maintenance Cost Type',
        width: 120,
        editable: true,
        type: 'number1',
      },
      {
        title: 'Procurment Budget',
        children: [
          {
            field: 'PB_MaterialCost',
            title: 'Material Cost',
            width: 120,
            editable: true,
            type: 'number1',
          },
        ],
      },

      {
        field: 'PB_Months',
        title: 'Months',
        width: 120,
        editable: false,
        type: 'number1',
      },
    ],
    [lowerVertName],
  )

  const fetchData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      var res = await DataService.getMonthWiseSummary(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      res = {
        code: 200,
        data: {
          data: [
            /* === ROUTINE (12 rows) Apr-25 -> Mar-26 === */
            {
              Id: 1,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1200,
              CB_MaterialCost: 300,
              CB_ServiceCost: 150,
              CB_TotalCost: 1650,
              CB_Months: 'Apr-25',
              PB_MaintenanceCost: 800,
              PB_MaterialCost: 200,
              PB_Months: 'Apr-25',
              Remark: '',
            },
            {
              Id: 2,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1100,
              CB_MaterialCost: 250,
              CB_ServiceCost: 120,
              CB_TotalCost: 1470,
              CB_Months: 'May-25',
              PB_MaintenanceCost: 780,
              PB_MaterialCost: 190,
              PB_Months: 'May-25',
              Remark: '',
            },
            {
              Id: 3,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1300,
              CB_MaterialCost: 400,
              CB_ServiceCost: 200,
              CB_TotalCost: 1900,
              CB_Months: 'Jun-25',
              PB_MaintenanceCost: 820,
              PB_MaterialCost: 240,
              PB_Months: 'Jun-25',
              Remark: '',
            },
            {
              Id: 4,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1250,
              CB_MaterialCost: 320,
              CB_ServiceCost: 180,
              CB_TotalCost: 1750,
              CB_Months: 'Jul-25',
              PB_MaintenanceCost: 810,
              PB_MaterialCost: 210,
              PB_Months: 'Jul-25',
              Remark: '',
            },
            {
              Id: 5,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1150,
              CB_MaterialCost: 280,
              CB_ServiceCost: 160,
              CB_TotalCost: 1590,
              CB_Months: 'Aug-25',
              PB_MaintenanceCost: 790,
              PB_MaterialCost: 205,
              PB_Months: 'Aug-25',
              Remark: '',
            },
            {
              Id: 6,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1400,
              CB_MaterialCost: 360,
              CB_ServiceCost: 190,
              CB_TotalCost: 1950,
              CB_Months: 'Sep-25',
              PB_MaintenanceCost: 840,
              PB_MaterialCost: 230,
              PB_Months: 'Sep-25',
              Remark: '',
            },
            {
              Id: 7,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1180,
              CB_MaterialCost: 310,
              CB_ServiceCost: 170,
              CB_TotalCost: 1660,
              CB_Months: 'Oct-25',
              PB_MaintenanceCost: 805,
              PB_MaterialCost: 215,
              PB_Months: 'Oct-25',
              Remark: '',
            },
            {
              Id: 8,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1220,
              CB_MaterialCost: 300,
              CB_ServiceCost: 160,
              CB_TotalCost: 1680,
              CB_Months: 'Nov-25',
              PB_MaintenanceCost: 825,
              PB_MaterialCost: 220,
              PB_Months: 'Nov-25',
              Remark: '',
            },
            {
              Id: 9,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1350,
              CB_MaterialCost: 380,
              CB_ServiceCost: 200,
              CB_TotalCost: 1930,
              CB_Months: 'Dec-25',
              PB_MaintenanceCost: 860,
              PB_MaterialCost: 240,
              PB_Months: 'Dec-25',
              Remark: '',
            },
            {
              Id: 10,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1280,
              CB_MaterialCost: 340,
              CB_ServiceCost: 180,
              CB_TotalCost: 1800,
              CB_Months: 'Jan-26',
              PB_MaintenanceCost: 840,
              PB_MaterialCost: 230,
              PB_Months: 'Jan-26',
              Remark: '',
            },
            {
              Id: 11,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1190,
              CB_MaterialCost: 290,
              CB_ServiceCost: 150,
              CB_TotalCost: 1630,
              CB_Months: 'Feb-26',
              PB_MaintenanceCost: 795,
              PB_MaterialCost: 210,
              PB_Months: 'Feb-26',
              Remark: '',
            },
            {
              Id: 12,
              CB_MaintenanceCostType: 'Routine',
              CB_MaintenanceCost: 1320,
              CB_MaterialCost: 370,
              CB_ServiceCost: 210,
              CB_TotalCost: 1900,
              CB_Months: 'Mar-26',
              PB_MaintenanceCost: 850,
              PB_MaterialCost: 225,
              PB_Months: 'Mar-26',
              Remark: '',
            },
            /* === ONE TIME (12 rows) Apr-25 -> Mar-26 === */
            {
              Id: 13,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 6000,
              CB_MaterialCost: 800,
              CB_ServiceCost: 400,
              CB_TotalCost: 7200,
              CB_Months: 'Apr-25',
              PB_MaintenanceCost: 2500,
              PB_MaterialCost: 500,
              PB_Months: 'Apr-25',
              Remark: '',
            },
            {
              Id: 14,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 7200,
              CB_MaterialCost: 950,
              CB_ServiceCost: 450,
              CB_TotalCost: 8600,
              CB_Months: 'May-25',
              PB_MaintenanceCost: 2700,
              PB_MaterialCost: 520,
              PB_Months: 'May-25',
              Remark: '',
            },
            {
              Id: 15,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 5000,
              CB_MaterialCost: 700,
              CB_ServiceCost: 350,
              CB_TotalCost: 6050,
              CB_Months: 'Jun-25',
              PB_MaintenanceCost: 2200,
              PB_MaterialCost: 480,
              PB_Months: 'Jun-25',
              Remark: '',
            },
            {
              Id: 16,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 8100,
              CB_MaterialCost: 1200,
              CB_ServiceCost: 600,
              CB_TotalCost: 9900,
              CB_Months: 'Jul-25',
              PB_MaintenanceCost: 2800,
              PB_MaterialCost: 600,
              PB_Months: 'Jul-25',
              Remark: '',
            },
            {
              Id: 17,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 5500,
              CB_MaterialCost: 650,
              CB_ServiceCost: 300,
              CB_TotalCost: 6450,
              CB_Months: 'Aug-25',
              PB_MaintenanceCost: 2300,
              PB_MaterialCost: 470,
              PB_Months: 'Aug-25',
              Remark: '',
            },
            {
              Id: 18,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 6800,
              CB_MaterialCost: 900,
              CB_ServiceCost: 420,
              CB_TotalCost: 8120,
              CB_Months: 'Sep-25',
              PB_MaintenanceCost: 2600,
              PB_MaterialCost: 520,
              PB_Months: 'Sep-25',
              Remark: '',
            },
            {
              Id: 19,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 7400,
              CB_MaterialCost: 980,
              CB_ServiceCost: 460,
              CB_TotalCost: 8840,
              CB_Months: 'Oct-25',
              PB_MaintenanceCost: 2750,
              PB_MaterialCost: 560,
              PB_Months: 'Oct-25',
              Remark: '',
            },
            {
              Id: 20,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 6600,
              CB_MaterialCost: 870,
              CB_ServiceCost: 390,
              CB_TotalCost: 7860,
              CB_Months: 'Nov-25',
              PB_MaintenanceCost: 2550,
              PB_MaterialCost: 540,
              PB_Months: 'Nov-25',
              Remark: '',
            },
            {
              Id: 21,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 7000,
              CB_MaterialCost: 920,
              CB_ServiceCost: 430,
              CB_TotalCost: 8350,
              CB_Months: 'Dec-25',
              PB_MaintenanceCost: 2650,
              PB_MaterialCost: 580,
              PB_Months: 'Dec-25',
              Remark: '',
            },
            {
              Id: 22,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 8200,
              CB_MaterialCost: 1100,
              CB_ServiceCost: 520,
              CB_TotalCost: 9820,
              CB_Months: 'Jan-26',
              PB_MaintenanceCost: 2900,
              PB_MaterialCost: 650,
              PB_Months: 'Jan-26',
              Remark: '',
            },
            {
              Id: 23,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 5900,
              CB_MaterialCost: 720,
              CB_ServiceCost: 360,
              CB_TotalCost: 6980,
              CB_Months: 'Feb-26',
              PB_MaintenanceCost: 2350,
              PB_MaterialCost: 500,
              PB_Months: 'Feb-26',
              Remark: '',
            },
            {
              Id: 24,
              CB_MaintenanceCostType: 'One time',
              CB_MaintenanceCost: 7600,
              CB_MaterialCost: 1000,
              CB_ServiceCost: 480,
              CB_TotalCost: 9080,
              CB_Months: 'Mar-26',
              PB_MaintenanceCost: 2850,
              PB_MaterialCost: 640,
              PB_Months: 'Mar-26',
              Remark: '',
            },
            /* === SHUTDOWN (12 rows) Apr-25 -> Mar-26 === */
            {
              Id: 25,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 2500,
              CB_MaterialCost: 500,
              CB_ServiceCost: 200,
              CB_TotalCost: 3200,
              CB_Months: 'Apr-25',
              PB_MaintenanceCost: 1200,
              PB_MaterialCost: 300,
              PB_Months: 'Apr-25',
              Remark: '',
            },
            {
              Id: 26,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 3000,
              CB_MaterialCost: 600,
              CB_ServiceCost: 300,
              CB_TotalCost: 3900,
              CB_Months: 'May-25',
              PB_MaintenanceCost: 1400,
              PB_MaterialCost: 350,
              PB_Months: 'May-25',
              Remark: '',
            },
            {
              Id: 27,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 2700,
              CB_MaterialCost: 450,
              CB_ServiceCost: 220,
              CB_TotalCost: 3370,
              CB_Months: 'Jun-25',
              PB_MaintenanceCost: 1300,
              PB_MaterialCost: 320,
              PB_Months: 'Jun-25',
              Remark: '',
            },
            {
              Id: 28,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 2600,
              CB_MaterialCost: 480,
              CB_ServiceCost: 260,
              CB_TotalCost: 3340,
              CB_Months: 'Jul-25',
              PB_MaintenanceCost: 1350,
              PB_MaterialCost: 330,
              PB_Months: 'Jul-25',
              Remark: '',
            },
            {
              Id: 29,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 2400,
              CB_MaterialCost: 420,
              CB_ServiceCost: 210,
              CB_TotalCost: 3030,
              CB_Months: 'Aug-25',
              PB_MaintenanceCost: 1250,
              PB_MaterialCost: 300,
              PB_Months: 'Aug-25',
              Remark: '',
            },
            {
              Id: 30,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 2800,
              CB_MaterialCost: 520,
              CB_ServiceCost: 240,
              CB_TotalCost: 3560,
              CB_Months: 'Sep-25',
              PB_MaintenanceCost: 1450,
              PB_MaterialCost: 360,
              PB_Months: 'Sep-25',
              Remark: '',
            },
            {
              Id: 31,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 2950,
              CB_MaterialCost: 530,
              CB_ServiceCost: 270,
              CB_TotalCost: 3750,
              CB_Months: 'Oct-25',
              PB_MaintenanceCost: 1500,
              PB_MaterialCost: 380,
              PB_Months: 'Oct-25',
              Remark: '',
            },
            {
              Id: 32,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 2650,
              CB_MaterialCost: 470,
              CB_ServiceCost: 250,
              CB_TotalCost: 3370,
              CB_Months: 'Nov-25',
              PB_MaintenanceCost: 1380,
              PB_MaterialCost: 340,
              PB_Months: 'Nov-25',
              Remark: '',
            },
            {
              Id: 33,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 3100,
              CB_MaterialCost: 560,
              CB_ServiceCost: 290,
              CB_TotalCost: 3950,
              CB_Months: 'Dec-25',
              PB_MaintenanceCost: 1600,
              PB_MaterialCost: 420,
              PB_Months: 'Dec-25',
              Remark: '',
            },
            {
              Id: 34,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 3050,
              CB_MaterialCost: 550,
              CB_ServiceCost: 280,
              CB_TotalCost: 3880,
              CB_Months: 'Jan-26',
              PB_MaintenanceCost: 1550,
              PB_MaterialCost: 400,
              PB_Months: 'Jan-26',
              Remark: '',
            },
            {
              Id: 35,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 2300,
              CB_MaterialCost: 410,
              CB_ServiceCost: 190,
              CB_TotalCost: 2900,
              CB_Months: 'Feb-26',
              PB_MaintenanceCost: 1200,
              PB_MaterialCost: 300,
              PB_Months: 'Feb-26',
              Remark: '',
            },
            {
              Id: 36,
              CB_MaintenanceCostType: 'Shutdown',
              CB_MaintenanceCost: 2850,
              CB_MaterialCost: 510,
              CB_ServiceCost: 260,
              CB_TotalCost: 3620,
              CB_Months: 'Mar-26',
              PB_MaintenanceCost: 1480,
              PB_MaterialCost: 380,
              PB_Months: 'Mar-26',
              Remark: '',
            },
          ],
        },
      }
      if (res?.code === 200) {
        const mapped = res?.data?.data.map((item, index) => ({
          ...item,
          id: index,
          isEditable: true,
          originalRemark: item.Remark,
        }))
        setRows(mapped)
      } else {
        setRows([])
      }
    } catch (err) {
      console.error('fetchData error', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [keycloak])

  useEffect(() => {
    fetchData()
  }, [fetchData, AOP_YEAR, PLANT_ID])

  const saveChanges = useCallback(async () => {
    try {
      setLoading(true)
      const data = Object.values(modifiedCells)
      if (!data.length) {
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setSnackbarOpen(true)
        return
      }
      const rowsToUpdate = data.map((row) => ({
        id: row.Id,
        remark: row.Remark,
        opHrsActual: row?.OpHrsActual,
      }))
      const res = await DataService.saveMonthwiseProduction(
        keycloak,
        rowsToUpdate,
        PLANT_ID,
        AOP_YEAR,
      )
      if (res?.code === 200) {
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        unsavedChangesRef.current = { unsavedRows: {}, rowsBeforeChange: {} }
      } else {
        setSnackbarData({ message: 'Data Saved Failed!', severity: 'error' })
      }
    } catch (err) {
      console.error('Error while save', err)
      setSnackbarData({
        message: err?.message || 'Save failed',
        severity: 'error',
      })
    } finally {
      setSnackbarOpen(true)
      setLoading(false)
    }
  }, [modifiedCells, keycloak, PLANT_ID])

  const handleRemarkCellClick = useCallback((row) => {
    setCurrentRemark(row.Remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }, [])

  return (
    // <Box>
    //   <Backdrop
    //     sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
    //     open={!!loading}
    //   >
    //     <CircularProgress color='inherit' />
    //   </Backdrop>
    //   <KendoDataTablesReports
    //     rows={rows}
    //     setRows={setRows}
    //     title='Budget Upload'
    //     modifiedCells={modifiedCells}
    //     setModifiedCells={setModifiedCells}
    //     columns={columns}
    //     permissions={{
    //       showCalculate: false,
    //       saveBtnForRemark: true,
    //       saveBtn: !isOldYear,
    //       showWorkFlowBtns: true,
    //       showTitle: true,
    //     }}
    //     remarkDialogOpen={remarkDialogOpen}
    //     setRemarkDialogOpen={setRemarkDialogOpen}
    //     currentRemark={currentRemark}
    //     setCurrentRemark={setCurrentRemark}
    //     currentRowId={currentRowId}
    //     setCurrentRowId={setCurrentRowId}
    //     enableSaveAddBtn={enableSaveAddBtn}
    //     saveChanges={saveChanges}
    //     handleRemarkCellClick={handleRemarkCellClick}
    //   />
    //   <Notification
    //     open={snackbarOpen}
    //     message={snackbarData.message}
    //     severity={snackbarData.severity}
    //     onClose={() => setSnackbarOpen(false)}
    //   />
    // </Box>
    <div>Templates All Sites</div>
  )
}
