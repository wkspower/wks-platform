import { useGridApiRef } from '@mui/x-data-grid'
import React, { useEffect, useState, useMemo } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
import { verticalEnums } from 'enums/verticalEnums'
import KendoDataTables from './index'
import { ShutDownPeColumns } from 'components/colums/ShutdownColumn'
import { ShutDownPpColumns } from 'components/colums/ShutdownColumn'
import { ShutDownAllColumns } from 'components/colums/ShutdownColumn'
import { MaintenanceDetailsApiService } from 'services/maintenance-details-api-service'
const ShutDown = ({ permissions }) => {
  const [_plantID, set_PlantID] = useState('')
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [allProducts, setAllProducts] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, yearChanged, oldYear, plantID } = dataGridStore

  const vertName = verticalChange?.selectedVertical

  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const plantName = JSON.parse(localStorage.getItem('selectedPlant'))?.name

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
      const yearStr = localStorage.getItem('year'); // e.g. "2025-26"
let startLimit, endLimit;
if (yearStr) {
  const [startYear, endYear] = yearStr.split('-').map((y) => parseInt(y.trim(), 10));
  if (!isNaN(startYear) && !isNaN(endYear)) {
    // Use yyyy-mm-dd format for reliable parsing
    startLimit = new Date(`${startYear}-04-01T00:00:00`);
    endLimit = new Date(`20${endYear}-03-31T23:59:59`);
  }
}
// Helper to format date as dd/mm/yyyy
function formatDateDDMMYYYY(date) {
  if (!(date instanceof Date) || isNaN(date)) return '';
  const d = date.getDate().toString().padStart(2, '0');
  const m = (date.getMonth() + 1).toString().padStart(2, '0');
  const y = date.getFullYear();
  return `${d}/${m}/${y}`;
}

for (const record of data) {
  const startDate = record.maintStartDateTime instanceof Date ? record.maintStartDateTime : new Date(record.maintStartDateTime);
  const endDate = record.maintEndDateTime instanceof Date ? record.maintEndDateTime : new Date(record.maintEndDateTime);

  // Validate date format: dd/mm/yyyy (by parsing and checking)
  if (
    startLimit &&
    endLimit &&
    (
      !startDate ||
      !endDate ||
      isNaN(startDate) ||
      isNaN(endDate) ||
      startDate < startLimit ||
      startDate > endLimit ||
      endDate < startLimit ||
      endDate > endLimit
    )
  ) {
    record.isError = true;
    setSnackbarOpen(true);
    setSnackbarData({
      message: `Dates must be between ${formatDateDDMMYYYY(startLimit)} and ${formatDateDDMMYYYY(endLimit)} for selected year. `,
      severity: 'error',
    });
    return;
  }
}
      let requiredFields
      if (lowerVertName === 'pe') {
        if (plantName?.toLowerCase() === 'ldpe') {
          requiredFields = ['discription', 'remark', 'productName1']
        } else {
          requiredFields = ['discription', 'remark']
        }
      } else if (lowerVertName === 'pp') {
        requiredFields = ['discription', 'remark']
      } else {
        requiredFields = ['discription', 'remark']
      }

      const rowsWithErrors = new Set()

      // Check each record for missing required fields
      for (const record of data) {
        for (const field of requiredFields) {
          if (!record[field] || record[field].trim() === '') {
            record.isError = true
            rowsWithErrors.add(record.id)
            break // Exit inner loop once we find one missing field
          }
        }
      }

      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }

      // Track duplicate descriptions
      const duplicateRows = new Set()
      const allDescriptions = rows.map((r) =>
        (r.discription || '').trim().toLowerCase(),
      )
      const duplicate = allDescriptions.find(
        (d, i) => d && allDescriptions.indexOf(d) !== i,
      )

      if (duplicate) {
        // Find all rows with duplicate descriptions
        // rows.forEach((row, index) => {
        //   if ((row.discription || '').trim().toLowerCase() === duplicate) {
        //     duplicateRows.add(row.id)
        //   }
        // })
        rows.forEach((row) => {
          if ((row.discription || '').trim().toLowerCase() === duplicate) {
            row.isError = true
          } else {
            row.isError = false
          }
        })
        setSnackbarOpen(true)
        setSnackbarData({
          message: `The description "${duplicate}" already exists in the list. Please enter a unique description to avoid duplication.`,
          severity: 'error',
        })
        return
      }

      const allRecords = [...rows]
      const timeErrorRows = new Set() // Add this line

      for (const record of data) {
        // Date required validation (before checking time order)
        const dateRequiredRows = new Set()
        for (const record of data) {
          const startMissing = !record.maintStartDateTime
          const endMissing = !record.maintEndDateTime

          if (startMissing || endMissing) {
            record.isError = true
            dateRequiredRows.add(record.id)
          }
        }

        if (dateRequiredRows.size > 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Start Date and End Date are required for all records.',
            severity: 'error',
          })
          return
        }

        if (
          record.maintStartDateTime &&
          record.maintEndDateTime &&
          record.maintStartDateTime.getTime() >=
            record.maintEndDateTime.getTime()
        ) {
          record.isError = true
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Start time must be before end time for "${record.discription || 'this record'}".`,
            severity: 'error',
          })
          return
        }
      }

      if (lowerVertName == 'meg') {
        const monthSpanRows = new Set() // Add this line
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
            row.isError = true
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
              a.isError = true
              b.isError = true
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
              // Add this line
              a.isError = true // Add this line
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
  }, [modifiedCells, rows, rowsSlowdown, lowerVertName]) // Add setErrorRows to dependencies

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
        productId: (() => {
          if (
            lowerVertName === verticalEnums.PE ||
            lowerVertName === verticalEnums.PP
          ) {
            const matched = allProducts.find(
              (p) => p.displayName === row.productName1,
            )
            return matched?.realId || null
          }
          return null
        })(),
        productName:
          lowerVertName === verticalEnums.PE ||
          lowerVertName === verticalEnums.PP
            ? row.productName1
            : null,
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

      const maintenanceResponse =
        await MaintenanceDetailsApiService.getMaintenanceData(keycloak)

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

      const formattedData = data.map((item, index) => {
        // Find the product display name from allProducts using the product ID
        const productObj = allProducts.find((p) => p.realId === item.product)
        return {
          ...item,
          idFromApi: item?.id,
          id: index,
          originalRemark: item.remark,
          inEdit: false,
          maintStartDateTime: new Date(item?.maintStartDateTime),
          maintEndDateTime: new Date(item?.maintEndDateTime),
          productName1: productObj ? productObj.displayName : '', // <-- Fix here
        }
      })

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
  useEffect(() => {
    const getAllProducts = async () => {
      try {
        let data = []
        if (lowerVertName === 'meg') {
          data = await DataService.getAllProducts(keycloak, null)
        } else {
          data = await DataService.getAllProductsAll(keycloak, 'Production')
        }
        let productList = []
        if (lowerVertName === 'meg') {
          productList = data
            .filter((product) => ['EO', 'EOE'].includes(product.displayName))
            .map((product) => ({
              id: product.displayName,
              displayName: product.displayName,
              realId: product.id,
            }))
        } else {
          productList = data.map((product) => ({
            id: product.displayName,
            displayName: product.displayName,
            realId: product.id,
          }))
        }
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching products', error)
      }
    }

    getAllProducts()
  }, [oldYear, yearChanged, keycloak, _plantID, lowerVertName])
  useEffect(() => {
    if (allProducts.length > 0) {
      fetchData()
    }
  }, [allProducts, oldYear, yearChanged, keycloak, _plantID, lowerVertName])

  const colDefs = useMemo(() => {
    switch (lowerVertName) {
      case verticalEnums.PE:
        if (plantName?.toLowerCase() != 'ldpe') {
          return ShutDownAllColumns
        }

        return ShutDownPeColumns

      case verticalEnums.PP:
        return ShutDownPpColumns

      default:
        return ShutDownAllColumns
    }
  }, [lowerVertName, plantName])

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
          await MaintenanceDetailsApiService.getMaintenanceData(keycloak)
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
      downloadExcelBtnFromUI: true,
      ExcelName: `${lowerVertName}_Shutdown Activities`,
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
        disableRedHighlight={true}
        allProducts={allProducts}
      />
    </div>
  )
}

export default ShutDown
