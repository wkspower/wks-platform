import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { Backdrop, Box, CircularProgress } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import KendoDataTables from './index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { add } from 'lodash'
import { validateFields } from 'utils/validationUtils'
export default function PlantTeam() {
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

  const SCREEN_NAME = screenTitle?.title
  const AOP_YEAR = year?.selectedYear
  const thisYear = AOP_YEAR
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()

  const headerMap = generateHeaderNames(AOP_YEAR)
  const [peopleInitiativeRows, setPeopleInitiativeRows] = useState([])
  const [modifiedPeopleCells, setModifiedPeopleCells] = useState({})
  const [remarkDialogOpenPeople, setRemarkDialogOpenPeople] = useState(false)
  const [currentRemarkPeople, setCurrentRemarkPeople] = useState('')
  const [currentRowIdPeople, setCurrentRowIdPeople] = useState(null)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const unsavedChangesRef = useRef({ unsavedRows: {}, rowsBeforeChange: {} })

  const columns = useMemo(
    () => [
      {
        field: 'id',
        title: 'ID',
        editable: false,
        type: 'number',
        hidden: true,
      },
      {
        field: 'serialNumber',
        title: 'S.No.',
        widthT: 70,
        editable: false,
        type: 'number',
      },
      {
        field: 'function',
        title: 'Function',

        editable: true,
      },
      {
        field: 'jobRole',
        title: 'Job Role',

        editable: true,
      },
      {
        field: 'name',
        title: 'Name',
        editable: true,
      },
      {
        field: 'age',
        title: 'Age',
        editable: true,
        type: 'number',
        widthT: 100,
      },
      {
        field: 'teamSize',
        title: 'Team Size',
        editable: true,
        type: 'number',
        widthT: 120,
      },
    ],
    [plantID, yearChanged],
  )

  const peopleInitiativeColumns = [
    {
      field: 'id',
      title: 'ID',
      editable: false,
      type: 'number',
      hidden: true,
    },
    {
      field: 'serialNumber',
      title: 'S.No.',
      widthT: 70,
      editable: false,
      type: 'number',
    },
    { field: 'initiative', title: 'Initiative', editable: true },
    { field: 'outcome', title: 'Outcome', editable: true },
    { field: 'recommendation', title: 'Recommendation', editable: true },
    {
      field: 'targetDate',
      title: 'Target Date',
      editable: true,
      type: 'date',
      widthT: 180,
    },
    { field: 'responsible', title: 'Resp.', editable: true, widthT: 120 },
  ]

  const fetchData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      // var res = await DataService.getMonthWiseSummary(keycloak)
      const res = await DataService.getDataTeamPlant(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const res1 = await DataService.getPeopleInitiative(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (res?.code === 200) {
        const mapped = res?.data?.Data?.map((item, index) => ({
          id: item.id || null,
          idFromApi: item.id || null,
          serialNumber: index,
          function: item.functions,
          jobRole: item.jobRole,
          name: item.name,
          age: item.age,
          teamSize: item.teamSize,
          remarks: item.remark,
          isEditable: item?.isEditable,
          originalRemark: item.remark,
        }))

        const peopleInitiativeMapped = res1?.data?.Data?.map((item, index) => ({
          ...item,
          id: item.id || null,
          idFromApi: item.id || null,
          serialNumber: index,
        }))
        setRows(mapped)
        setPeopleInitiativeRows(peopleInitiativeMapped)
      } else {
        setRows([])
        setPeopleInitiativeRows([])
      }
    } catch (err) {
      console.error('fetchData error', err)
      setRows([])
      setPeopleInitiativeRows([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, yearChanged, plantID])

  useEffect(() => {
    fetchData()
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak])

  const saveChanges = React.useCallback(async () => {
    try {
      setLoading(true)
      const data = Object.values(modifiedCells)
      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      const requiredFields = ['function', 'jobRole', 'name', 'age', 'teamSize']

      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }

      const payload = data.map((item, index) => ({
        id: item.id || null,
        functions: item.function,
        jobRole: item.jobRole,
        name: item.name,
        age: item.age,
        teamSize: item.teamSize,
        remark: item.remarks || 'system generated',
      }))

      // 3. Save to API
      const response = await DataService.savePlantTeam(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        payload,
      )

      // 4. Handle API response
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Save failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }, [modifiedCells, keycloak, PLANT_ID, AOP_YEAR, fetchData])

  const saveChangesP = useCallback(async () => {
    try {
      setLoading(true)
      const data = Object.values(modifiedPeopleCells)
      if (!data.length) {
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setSnackbarOpen(true)
        return
      }
      const requiredFields = [
        'initiative',
        'outcome',
        'recommendation',
        'targetDate',
        'responsible',
      ]

      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }

      const payload = data.map((item, index) => ({
        id: item.id || null,
        initiative: item.initiative,
        outcome: item.outcome,
        recommendation: item.recommendation,
        //targetDate: item.targetDate,
        targetDate: item.targetDate
          ? new Date(item.targetDate).toLocaleDateString('en-CA')
          : null,
        responsible: item.responsible,
        remark: item.remark || 'system generated',
        // add/remove fields as needed
      }))

      await DataService.savePeopleInitiative(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        payload,
      )
      setSnackbarData({ message: 'Saved successfully!', severity: 'success' })
      setSnackbarOpen(true)
      setModifiedPeopleCells({})
      fetchData()
    } catch (err) {
      setSnackbarData({ message: 'Save failed!', severity: 'error' })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }, [modifiedPeopleCells, keycloak, PLANT_ID, AOP_YEAR, fetchData])

  const deleteRowData = async (paramsForDelete) => {
    setLoading(true)

    try {
      const { idFromApi, id } = paramsForDelete
      const deleteId = id

      if (!idFromApi) {
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
      }

      if (idFromApi) {
        await DataService.deletePlantTeam(idFromApi, keycloak)
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        fetchData()
      } else {
        setLoading(false)
      }
    } catch (error) {
      console.error('Error deleting Record!', error)
    }
  }
  const deleteRowDataPeople = async (paramsForDelete) => {
    setLoading(true)

    try {
      const { idFromApi, id } = paramsForDelete
      const deleteId = id

      if (!idFromApi) {
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
      }

      if (idFromApi) {
        await DataService.deletePeopleInitiative(idFromApi, keycloak)
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        fetchData()
      } else {
        setLoading(false)
      }
    } catch (error) {
      console.error('Error deleting Record!', error)
    }
  }
  const handleExcelUpload = (type) => (rawFile) => {
    uploadPeopleDetails(rawFile, type)
  }
  const uploadPeopleDetails = async (rawFile, type) => {
    setLoading(true)
    try {
      let response
      if (type === 'plantTeam') {
        response = await DataService.ImportPlantTeamExcel(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      } else if (type === 'peopleInitiative') {
        response = await DataService.ImportPeopleInitiativeExcel(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      }

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData()
      } else if (response?.code === 400 && response?.data) {
        const byteCharacters = atob(response.data)
        const byteNumbers = Array.from(byteCharacters, (char) =>
          char.charCodeAt(0),
        )
        const byteArray = new Uint8Array(byteNumbers)

        const blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        })

        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `Error File - ${type}.xlsx`)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Error uploading excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const downloadExcelForConfiguration = async (type) => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      let response
      let EXCEL_EXPORT_TITLE = ''

      if (type === 'plantTeam') {
        EXCEL_EXPORT_TITLE = `${vertName}_Plant_Team`
        response = await DataService.PlantTeamExport(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          EXCEL_EXPORT_TITLE,
        )
      } else if (type === 'peopleInitiative') {
        EXCEL_EXPORT_TITLE = `${vertName}_People_Initiative`
        response = await DataService.ExportPeopleInitiative(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          EXCEL_EXPORT_TITLE,
        )
      }

      // Optionally handle the response here (e.g., trigger file download)
    } catch (error) {
      console.error('Error downloading Excel:', error)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    } finally {
      setSnackbarOpen(true)
    }
  }

  const handleRemarkCellClick = useCallback((row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }, [])

  const getAdjustedPermissionsC = (permissions, isOldYear) => {
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
    }
  }

  const adjustedPermissionsC = getAdjustedPermissionsC(
    {
      allAction: true,
      saveBtn: true,
      showTitleNameBusiness: true,
      titleName: 'Plant Team (Size)',
      adjustedPermissions: true,
      //downloadExcelBtnFromUI: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      ExcelName: `${lowerVertName}_Plant_Team`,
      addButton: true,
      deleteButton: true,
    },
    isOldYear,
  )
  const getAdjustedPermissionsPeople = (permissions, isOldYear) => {
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
    }
  }

  const peopleInitiativePermissions = getAdjustedPermissionsPeople({
    saveBtn: true,
    allAction: true,
    showTitleNameBusiness: true,
    titleName: 'People Initiative',
    adjustedPermissions: true,
    downloadExcelBtn: true,
    uploadExcelBtn: true,
    ExcelName: `${lowerVertName}_People_Initiative`,
    addButton: true,
    deleteButton: true,
  })

  const commonGridProps = {
    columns,
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <KendoDataTables
        columns={columns}
        rows={rows}
        setRows={setRows}
        title='Plant Team (Size)'
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        enableSaveAddBtn={enableSaveAddBtn}
        saveChanges={saveChanges}
        handleRemarkCellClick={handleRemarkCellClick}
        deleteRowData={deleteRowData}
        permissions={adjustedPermissionsC}
        downloadExcelForConfiguration={() =>
          downloadExcelForConfiguration('plantTeam')
        }
        handleExcelUpload={handleExcelUpload('plantTeam')}
      />
      <KendoDataTables
        columns={peopleInitiativeColumns}
        rows={peopleInitiativeRows}
        setRows={setPeopleInitiativeRows}
        title='People Initiative'
        modifiedCells={modifiedPeopleCells}
        setModifiedCells={setModifiedPeopleCells}
        remarkDialogOpen={remarkDialogOpenPeople}
        setRemarkDialogOpen={setRemarkDialogOpenPeople}
        currentRemark={currentRemarkPeople}
        setCurrentRemark={setCurrentRemarkPeople}
        currentRowId={currentRowIdPeople}
        setCurrentRowId={setCurrentRowIdPeople}
        permissions={peopleInitiativePermissions}
        saveChanges={saveChangesP}
        deleteRowData={deleteRowDataPeople}
        downloadExcelForConfiguration={() =>
          downloadExcelForConfiguration('peopleInitiative')
        }
        handleExcelUpload={handleExcelUpload('peopleInitiative')}
      />
      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}
