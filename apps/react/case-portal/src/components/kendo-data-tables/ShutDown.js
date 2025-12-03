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
import { ShutDownPeColumnsldpe12 } from 'components/colums/ShutdownColumn'
import { ShutDownPpColumns } from 'components/colums/ShutdownColumn'
import { ShutDownAllColumns } from 'components/colums/ShutdownColumn'
import { ShutDownPTAColumns } from 'components/colums/ShutdownColumn'
import { MaintenanceDetailsApiService } from 'services/maintenance-details-api-service'
import { getRoleName } from 'services/role-service'
const ShutDown = ({ permissions }) => {
  const [_plantID, set_PlantID] = useState('')
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [allProducts, setAllProducts] = useState([])
  const [allDescriptionDrpdwn, setAllDescriptionDrpdwn] = useState([])
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
  const PLANT_NAME = plantObject?.name

  const SITE_ID = siteObject?.id
  const SITE_NAME = siteObject?.name

  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name

  const AOP_YEAR = year?.selectedYear
  const vertName = verticalChange?.selectedVertical
  const SCREEN_NAME = screenTitle?.title

  const lowerVertName = vertName?.toLowerCase()
  const lowerSiteName = SITE_NAME?.toLowerCase()
  const lowerPlantName = PLANT_NAME?.toLowerCase()
  const plantName = plantObject?.name
  const siteName = siteObject?.name
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear

  const IS_NON_PRODUCT_VERTICAL =
    lowerVertName === 'elastomer' ||
    lowerVertName === 'pvc' ||
    lowerVertName === 'vcm' ||
    lowerVertName === 'aromatics' ||
    lowerVertName === 'pta' ||
    lowerVertName === 'pet' ||
    lowerVertName === 'meg' ||
    (lowerVertName === 'pe' &&
      !['lldpe1', 'lldpe2'].includes(lowerPlantName)) ||
    lowerVertName === 'pp'

  const DELETE_NOTE =
    'Warning: Please verify the shutdown consumption quantity before deleting the shutdown activity.'

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

  // const READ_ONLY = getRoleName(keycloak)
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const IS_PE_PP_VERTICAL = lowerVertName === 'pe' || lowerVertName === 'pp'

  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCells)

      //1 NO RECORDS
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      const yearStr = AOP_YEAR
      let startLimit, endLimit
      if (yearStr) {
        const [startYear, endYear] = yearStr
          .split('-')
          .map((y) => parseInt(y.trim(), 10))
        if (!isNaN(startYear) && !isNaN(endYear)) {
          // Use yyyy-mm-dd format for reliable parsing
          startLimit = new Date(`${startYear}-04-01T00:00:00`)
          endLimit = new Date(`20${endYear}-03-31T23:59:59`)
        }
      }
      // Helper to format date as dd/mm/yyyy
      // eslint-disable-next-line
      function formatDateDDMMYYYY(date) {
        if (!(date instanceof Date) || isNaN(date)) return ''
        const d = date.getDate().toString().padStart(2, '0')
        const m = (date.getMonth() + 1).toString().padStart(2, '0')
        const y = date.getFullYear()
        return `${d}/${m}/${y}`
      }

      for (const record of data) {
        const startDate =
          record.maintStartDateTime instanceof Date
            ? record.maintStartDateTime
            : new Date(record.maintStartDateTime)
        const endDate =
          record.maintEndDateTime instanceof Date
            ? record.maintEndDateTime
            : new Date(record.maintEndDateTime)

        // Validate date format: dd/mm/yyyy (by parsing and checking)
        if (
          startLimit &&
          endLimit &&
          (!startDate ||
            !endDate ||
            isNaN(startDate) ||
            isNaN(endDate) ||
            startDate < startLimit ||
            startDate > endLimit ||
            endDate < startLimit ||
            endDate > endLimit)
        ) {
          record.isError = true
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Dates must be between ${formatDateDDMMYYYY(startLimit)} and ${formatDateDDMMYYYY(endLimit)} for selected year. `,
            severity: 'error',
          })
          return
        }
      }

      //2 REMARKS VALIDATION
      let requiredFields
      if (lowerVertName === 'pe') {
        if (
          siteName?.toLowerCase() === 'nmd' &&
          (plantName?.toLowerCase() === 'lldpe1' ||
            plantName?.toLowerCase() === 'lldpe2')
        ) {
          requiredFields = ['discription', 'remark', 'productName1']
        } else {
          requiredFields = ['discription', 'remark']
        }
      } else if (lowerVertName === 'pta') {
        requiredFields = ['discriptionDrpdwn', 'remark']
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

      //3 Track duplicate descriptions
      const duplicateRows = new Set()
      const allDescriptions = rows.map((r) =>
        (r.discription || '').trim().toLowerCase(),
      )
      const duplicate = allDescriptions.find(
        (d, i) => d && allDescriptions.indexOf(d) !== i,
      )

      if (duplicate && lowerVertName !== 'pta') {
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

      //5 START DATE END DATE MANDATORY
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

      //6 SHUTDOWN SPANS MULTIPLE MONTHS
      if (
        lowerVertName == 'meg' ||
        lowerVertName == 'elastomer' ||
        lowerVertName == 'vcm' ||
        lowerVertName == 'pvc' ||
        lowerVertName == 'pta' ||
        lowerVertName == 'pe' ||
        lowerVertName == 'pp'
      ) {
        // Check for shutdown timeframe spanning multiple months
        const monthSpanRows = new Set() // Add this line
        for (const row of allRecords) {
          const start = new Date(row.maintStartDateTime)
          const end = new Date(row.maintEndDateTime)
          //shutdown timeframe for Multiple months
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
        //Shutdown timeframe overlapping of same time
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
                message: `The shutdown timeframe for "${a.discription || b.discription || 'this record'}" overlaps with "${b.discription}". Please ensure no overlapping timeframes.`,
                severity: 'error',
              })
              return
            }
          }
        }

        //7 Slowdown and shutdown timeframe overlapping
        //THEN CHECK 1 SCREEN DATA WITH ANOTHER SCREEN

        if (
          lowerVertName != 'elastomer' &&
          // VCM logic change
          // lowerVertName != 'vcm' &&
          lowerVertName != 'pvc'
        ) {
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
      }

      saveShutdownData(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCells, rows, rowsSlowdown, lowerVertName])

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
        discription: row.discription || row.discriptionDrpdwn,
        durationInHrs: (() => {
          const v = findDuration('1', row)
          if (!v) return null
          const [h = '00', m = '00'] = String(v).split('.')
          return `${h.padStart(2, '0')}.${m.padStart(2, '0')}`
        })(),
        maintEndDateTime: addTimeOffset(row.maintEndDateTime),
        maintStartDateTime: addTimeOffset(row.maintStartDateTime),
        audityear: AOP_YEAR,
        id: row.idFromApi || null,
        remark: row.remark || 'null',
      }))

      const response = await DataService.saveShutdownData(
        PLANT_ID,
        shutdownDetails,
        keycloak,
      )

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Saved Successfully!',
        severity: 'success',
      })

      const maintenanceResponse =
        await MaintenanceDetailsApiService.getMaintenanceData(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

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
        message: 'Updated successfully!',
        severity: 'success',
      })

      return response
    } catch (error) {
      console.error('Error saving  data:', error)
    } finally {
      fetchData()
    }
  }

  const fetchData = async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setLoading(true)
      const data = await DataService.getShutDownPlantData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      const dataSlowDown = await DataService.getSlowDownPlantData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
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
        const productObj = allProducts.find((p) => p.realId === item.product)
        const descriptionObj = allDescriptionDrpdwn.find(
          (p) => p.name === item.discription,
        )

        if (lowerVertName == 'pta') {
          return {
            ...item,
            idFromApi: item?.id,
            id: index,
            originalRemark: item.remark,
            inEdit: false,
            maintStartDateTime: new Date(item?.maintStartDateTime),
            maintEndDateTime: new Date(item?.maintEndDateTime),
            discriptionDrpdwn: descriptionObj ? descriptionObj.displayName : '',
          }
        }

        return {
          ...item,
          idFromApi: item?.id,
          id: index,
          originalRemark: item.remark,
          inEdit: false,
          maintStartDateTime: new Date(item?.maintStartDateTime),
          maintEndDateTime: new Date(item?.maintEndDateTime),
          productName1: productObj ? productObj.displayName : '',
        }
      })

      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [oldYear, yearChanged, keycloak, PLANT_ID, AOP_YEAR])

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
      if (!PLANT_ID || !AOP_YEAR) return

      try {
        let data = []
        if (lowerVertName === 'meg') {
          data = await DataService.getAllProducts(keycloak, PLANT_ID, AOP_YEAR)
        } else if (lowerVertName === 'pe' || lowerVertName === 'pp') {
          data = await DataService.gradeDetails(keycloak, AOP_YEAR, PLANT_ID)
        } else {
          data = await DataService.getAllProductsAll(
            keycloak,
            'Production',
            PLANT_ID,
          )
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
        } else if (lowerVertName === 'pe' || lowerVertName === 'pp') {
          productList = data?.data.map((product) => ({
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
  }, [oldYear, yearChanged, keycloak, PLANT_ID, lowerVertName])

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return

    const getAllDescriptionDrpdwn = async () => {
      try {
        let data = []
        data = await DataService.dropdownValues(keycloak, PLANT_ID, AOP_YEAR)

        // let data = {
        //   code: 200,
        //   message: 'Data fetched successfully',
        //   data: [
        //     {
        //       DisplayName: 'Catalyst Full Topup',
        //       Name: 'Catalyst Full Topup',
        //     },
        //     {
        //       DisplayName: 'Catalyst Partial Topup',
        //       Name: 'Catalyst Partial Topup',
        //     },
        //     {
        //       DisplayName: 'Preheater Cleaning',
        //       Name: 'Preheater Cleaning',
        //     },
        //     {
        //       DisplayName: 'Preheater Cleaning',
        //       Name: 'Other',
        //     },
        //   ],
        // }

        let descriptionObjList = []
        {
          descriptionObjList = data?.data.map((product) => ({
            id: product.Name,
            name: product.Name,
            displayName: product.DisplayName,
          }))
        }
        setAllDescriptionDrpdwn(descriptionObjList)
      } catch (error) {
        console.error('Error fetching products', error)
      }
    }

    if (lowerVertName == 'pta') getAllDescriptionDrpdwn()
  }, [oldYear, AOP_YEAR, keycloak, PLANT_ID, lowerVertName])

  useEffect(() => {
    if (lowerVertName == 'pta' && allDescriptionDrpdwn?.length > 0) {
      fetchData()
    } else if (allProducts.length > 0) {
      if (!PLANT_ID || !AOP_YEAR) return
      fetchData()
    }
  }, [
    allProducts,
    allDescriptionDrpdwn,
    oldYear,
    yearChanged,
    keycloak,
    PLANT_ID,
    lowerVertName,
  ])

  const colDefs = useMemo(() => {
    switch (lowerVertName) {
      case verticalEnums.PE:
        if (
          siteName?.toLowerCase() === 'nmd' &&
          (plantName?.toLowerCase() === 'lldpe1' ||
            plantName?.toLowerCase() === 'lldpe2')
        ) {
          return ShutDownPeColumnsldpe12
        }
        return ShutDownPeColumns

      case verticalEnums.PP:
        return ShutDownPpColumns

      case verticalEnums.PTA:
        return ShutDownPTAColumns

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
        await DataService.deleteShutdownData(idFromApi, keycloak, PLANT_ID)
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        fetchData()

        const maintenanceResponse =
          await MaintenanceDetailsApiService.getMaintenanceData(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
          )
      } else {
        setLoading(false)
      }
    } catch (error) {
      console.error('Error deleting Record', error)
    }
  }

  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      let response
      if (IS_NON_PRODUCT_VERTICAL) {
        response = await DataService.exportShutdownNonProduct(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      } else {
        response = await DataService.exportShutdownNonProductWise(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      }
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

  const importShutdown = async (rawFile) => {
    setLoading(true)

    try {
      let response

      if (IS_NON_PRODUCT_VERTICAL) {
        response = await DataService.ImportShutdownNonProduct(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      } else {
        response = await DataService.ImportShutdownProductWise(
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
        link.setAttribute('download', 'Error File - Shutdown.xlsx')
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
      console.error('Error uploading xcel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = (rawFile) => {
    importShutdown(rawFile)
  }

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      downloadExcelBtn: false,
      uploadExcelBtn: false,
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
      downloadExcelBtn: true,
      showNoteWhileDeleting: IS_PE_PP_VERTICAL ? true : false,

      showTitleNameBusiness: true,
      titleName: `${SCREEN_NAME}`,

      uploadExcelBtn:
        lowerVertName === 'pe' ||
        lowerVertName === 'pp' ||
        lowerVertName === 'elastomer' ||
        lowerVertName === 'pvc' ||
        lowerVertName === 'vcm' ||
        lowerVertName === 'pta'
          ? true
          : false,
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
        allDescriptionDrpdwn={allDescriptionDrpdwn}
        handleExcelUpload={handleExcelUpload}
        downloadExcelForConfiguration={downloadExcelForConfiguration}
        deleteNoteOnDeleteDialogeBox={DELETE_NOTE}
        screenType='shutdown'
      />
    </div>
  )
}

export default ShutDown
