import { useGridApiRef } from '@mui/x-data-grid'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'

import KendoDataTables from './index'

const ShutDown = ({ permissions }) => {
  const [_plantID, set_PlantID] = useState('')

  const [modifiedCells, setModifiedCells] = React.useState({})

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, yearChanged, oldYear, plantID } = dataGridStore

  const vertName = verticalChange?.selectedVertical

  const lowerVertName = vertName?.toLowerCase() || 'meg'

  useEffect(() => {
    if (plantID?.plantId) {
      set_PlantID(plantID?.plantId)
    }
  }, [plantID])

  const isOldYear = oldYear?.oldYear

  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [rowsSlowdown, setRowsSlowdown] = useState()

  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const keycloak = useSession()
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCells)

      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      const requiredFields = ['discription', 'remark']
      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }

      const allDescriptions = rows.map((r) =>
        (r.discription || '').trim().toLowerCase(),
      )
      const duplicate = allDescriptions.find(
        (d, i) => d && allDescriptions.indexOf(d) !== i,
      )

      if (duplicate) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: `The description "${duplicate}" already exists in the list. Please enter a unique description to avoid duplication.`,
          severity: 'error',
        })
        return
      }

      const allRecords = [...rows]

      for (const record of data) {
        if (
          record.maintStartDateTime &&
          record.maintEndDateTime &&
          record.maintStartDateTime.getTime() >=
            record.maintEndDateTime.getTime()
        ) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Start time must be before end time for "${record.discription || 'this record'}".`,
            severity: 'error',
          })
          return
        }
      }

      if (lowerVertName == 'meg') {
        for (const row of allRecords) {
          const start = new Date(row.maintStartDateTime)
          const end = new Date(row.maintEndDateTime)

          if (isNaN(start.getTime()) || isNaN(end.getTime())) continue

          const formatDate = (date) =>
            date.toLocaleDateString('en-GB', {
              day: '2-digit',
              month: 'short',
              year: 'numeric',
            })

          const isSameMonth =
            start.getMonth() === end.getMonth() &&
            start.getFullYear() === end.getFullYear()

          if (!isSameMonth) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: `The shutdown timeframe for '${row.discription}' spans multiple months (from ${formatDate(start, 'dd MMM yyyy')} to ${formatDate(end, 'dd MMM yyyy')}). Please split it into separate entries for each month.`,
              severity: 'error',
            })
            return
          }
        }

        for (let i = 0; i < allRecords.length; i++) {
          const a = allRecords[i]
          const aStart = new Date(a.maintStartDateTime).getTime()
          const aEnd = new Date(a.maintEndDateTime).getTime()

          if (isNaN(aStart) || isNaN(aEnd)) continue

          for (let j = 0; j < allRecords.length; j++) {
            if (i === j) continue
            const b = allRecords[j]
            const bStart = new Date(b.maintStartDateTime).getTime()
            const bEnd = new Date(b.maintEndDateTime).getTime()

            if (isNaN(bStart) || isNaN(bEnd)) continue

            if (aStart < bEnd && bStart < aEnd) {
              setSnackbarOpen(true)
              setSnackbarData({
                message: `The shutdown timeframe for "${a.discription}" overlaps with "${b.discription}". Please ensure no overlapping timeframes.`,
                severity: 'error',
              })
              return
            }
          }
        }

        //THEN CHECK 1 SCREEN DATA WITH ANOTHER SCREEN
        for (let i = 0; i < rows.length; i++) {
          const a = rows[i]
          const aStart = new Date(a.maintStartDateTime).getTime()
          const aEnd = new Date(a.maintEndDateTime).getTime()

          if (isNaN(aStart) || isNaN(aEnd)) continue

          for (let j = 0; j < rowsSlowdown.length; j++) {
            const b = rowsSlowdown[j]
            const bStart = new Date(b.maintStartDateTime).getTime()
            const bEnd = new Date(b.maintEndDateTime).getTime()

            if (isNaN(bStart) || isNaN(bEnd)) continue

            if (aStart < bEnd && bStart < aEnd) {
              setSnackbarOpen(true)
              setSnackbarData({
                message: `The timeframe for "${a.discription} (Shutdown)" overlaps with "${b.discription} (Slowdown)". Please ensure no overlapping timeframes.`,
                severity: 'error',
              })
              return
            }
          }
        }
      }

      saveShutdownData(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCells])

  function addTimeOffset(dateTime) {
    if (!dateTime) return null
    const date = new Date(dateTime)
    date.setUTCHours(date.getUTCHours() + 5)
    date.setUTCMinutes(date.getUTCMinutes() + 30)
    return date
  }

  const saveShutdownData = async (newRow) => {
    setLoading(true)
    try {
      let plantId = ''

      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const shutdownDetails = newRow.map((row) => ({
        productId: row.product,
        discription: row.discription,
        durationInHrs: (() => {
          const v = findDuration('1', row)
          if (!v) return null
          const [h = '00', m = '00'] = String(v).split('.')
          return `${h.padStart(2, '0')}.${m.padStart(2, '0')}`
        })(),
        maintEndDateTime: addTimeOffset(row.maintEndDateTime),
        maintStartDateTime: addTimeOffset(row.maintStartDateTime),
        audityear: localStorage.getItem('year'),
        id: row.idFromApi || null,
        remark: row.remark || 'null',
      }))

      const response = await DataService.saveShutdownData(
        plantId,
        shutdownDetails,
        keycloak,
      )

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Shutdown data Saved Successfully!',
        severity: 'success',
      })

      const maintenanceResponse = await DataService.getMaintenanceData(keycloak)

      setModifiedCells({})

      setLoading(false)
      return response
    } catch (error) {
      setLoading(false)
      console.error('Error saving shutdown data:', error)
    } finally {
      fetchData()

      setLoading(false)
    }
  }

  const updateShutdownData = async (newRow) => {
    try {
      var maintenanceId = newRow?.maintenanceId

      const slowDownDetails = {
        productId: newRow.product,
        discription: newRow.discription,
        durationInHrs: newRow.durationInHrs,
        maintEndDateTime: newRow.maintEndDateTime,
        maintStartDateTime: newRow.maintStartDateTime,
      }

      const response = await DataService.updateShutdownData(
        maintenanceId,
        slowDownDetails,
        keycloak,
      )

      setSnackbarOpen(true)

      setSnackbarData({
        message: 'Shutdown data Updated successfully!',
        severity: 'success',
      })

      return response
    } catch (error) {
      console.error('Error saving Shutdown data:', error)
    } finally {
      fetchData()
    }
  }

  const fetchData = async () => {
    if (!plantID) return
    try {
      setLoading(true)
      const data = await DataService.getShutDownPlantData(
        keycloak,
        plantID?.plantId,
      )
      const dataSlowDown = await DataService.getSlowDownPlantData(
        keycloak,
        plantID?.plantId,
      )

      const formattedDataSlowDown = dataSlowDown.map((item, index) => ({
        ...item,
        idFromApi: item?.id,
        id: index,
        originalRemark: item.remark,
        inEdit: false,
        maintStartDateTime: new Date(item?.maintStartDateTime),
        maintEndDateTime: new Date(item?.maintEndDateTime),
      }))

      setRowsSlowdown(formattedDataSlowDown)

      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item?.id,
        id: index,
        originalRemark: item.remark,
        inEdit: false,
        maintStartDateTime: new Date(item?.maintStartDateTime),
        maintEndDateTime: new Date(item?.maintEndDateTime),
      }))

      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching Shutdown data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [oldYear, yearChanged, keycloak, _plantID])

  const findDuration = (v, row) => {
    if (row.durationInHrs) return row.durationInHrs

    if (row.maintStartDateTime && row.maintEndDateTime) {
      const start = new Date(row.maintStartDateTime)
      const end = new Date(row.maintEndDateTime)

      if (!isNaN(start?.getTime()) && !isNaN(end?.getTime())) {
        const durationInMs = end - start
        const durationInMinutes = durationInMs / (1000 * 60)
        const hours = Math.floor(durationInMinutes / 60)
        const minutes = durationInMinutes % 60
        return `${hours}.${minutes.toString().padStart(2, '0')}`
      }
    }

    return ''
  }

  const colDefs = [
    {
      field: 'discription',
      title: 'Shutdown Desc',
      width: 250,
      editable: true,
      type: 'descLimit',
    },
    {
      field: 'maintenanceId',
      title: 'Maintenance ID',
      editable: false,
      hidden: true,
    },
    {
      field: 'maintStartDateTime',
      title: 'SD - From',
      editable: true,
    },
    {
      field: 'maintEndDateTime',
      title: 'SD - To',
      editable: true,
    },
    {
      field: 'durationInHrs',
      title: 'Duration (hrs)',
      editable: true,
    },
    {
      field: 'remark',
      title: 'Shutdown Basis',
      editable: true,
    },
  ]

  const deleteRowData = async (paramsForDelete) => {
    setLoading(true)

    try {
      const { idFromApi, id } = paramsForDelete
      const deleteId = id

      if (!idFromApi) {
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
      }

      if (idFromApi) {
        await DataService.deleteShutdownData(idFromApi, keycloak)
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        fetchData()

        const maintenanceResponse =
          await DataService.getMaintenanceData(keycloak)
      } else {
        setLoading(false)
      }
    } catch (error) {
      console.error('Error deleting Record', error)
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
      allAction: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? true,
      addButton: permissions?.addButton ?? true,
      deleteButton: permissions?.deleteButton ?? true,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? false,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      saveBtn: permissions?.saveBtn ?? true,
      customHeight: permissions?.customHeight,
      allAction: true,
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
        setRows={setRows}
        columns={colDefs}
        rows={rows}
        paginationOptions={[100, 200, 300]}
        updateShutdownData={updateShutdownData}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        deleteId={deleteId}
        open1={open1}
        setDeleteId={setDeleteId}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        handleRemarkCellClick={handleRemarkCellClick}
        fetchData={fetchData}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        deleteRowData={deleteRowData}
        permissions={adjustedPermissions}
      />
    </div>
  )
}

export default ShutDown
