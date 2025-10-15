import React, { useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'

import { SlowDownElastomerColumns } from 'components/colums/ElastomerColums'
import { SlowDownAromaticsColumns } from 'components/colums/AromaticsColumns'
import { SlowDownMegColumns } from 'components/colums/MegColums'
import { SlowDownPeColumns } from 'components/colums/PeColums'
import { SlowDownPpColumns } from 'components/colums/PpColums'
import { SlowDownPtaColumns } from 'components/colums/PtaColums'
import { verticalEnums } from 'enums/verticalEnums'
import { validateFields } from 'utils/validationUtils'
import { Box, Tab, Tabs } from '../../../node_modules/@mui/material/index'
import { GridRowModes } from '../../../node_modules/@mui/x-data-grid/models/gridEditRowModel'
import KendoDataTables from './index'
import { MaintenanceDetailsApiService } from 'services/maintenance-details-api-service'

const SlowDown = ({ permissions }) => {
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
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const vertName = verticalChange?.selectedVertical
  const plantName = plantObject?.name
  const isOldYear = oldYear?.oldYear
  const [errorRows, setErrorRows] = useState(new Set())
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [rowModesModel, setRowModesModel] = useState({})
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [modifiedCells2, setModifiedCells2] = React.useState({})
  const [colDefs2, setColDefs2] = React.useState({})
  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const [rows, setRows] = useState()
  const [rowsShutdown, setRowsShutdown] = useState()
  const [rows2, setRows2] = useState()
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const keycloak = useSession()

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const [selectedTab, setSelectedTab] = useState(0)

  const handleTabChange = (event, newValue) => {
    setModifiedCells2({})
    setSelectedTab(newValue)

    if (newValue === 0) {
      setModifiedCells({})
    } else if (newValue === 1) {
      setColDefs2([])
      setRows2([])
      fetchData2()
    }
  }

  const handleCancelClick = () => () => {
    const rowsInEditMode = Object.keys(rowModesModel).filter(
      (id) => rowModesModel[id]?.mode === 'edit',
    )

    rowsInEditMode.forEach((id) => {
      apiRef.current.stopRowEditMode({ id })
    })
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  function addTimeOffset(dateTime) {
    if (!dateTime) return null
    const date = new Date(dateTime)
    date.setUTCHours(date.getUTCHours() + 5)
    date.setUTCMinutes(date.getUTCMinutes() + 30)
    return date
  }
  const findDuration = (v, row) => {
    if (row.durationInHrs) return row.durationInHrs

    if (row.maintStartDateTime && row.maintEndDateTime) {
      const start = new Date(row.maintStartDateTime)
      const end = new Date(row.maintEndDateTime)

      if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
        const durationInMs = end - start
        const durationInMinutes = durationInMs / (1000 * 60)
        const hours = Math.floor(durationInMinutes / 60)
        const minutes = durationInMinutes % 60
        return `${hours}.${minutes.toString().padStart(2, '0')}`
      }
    }

    return ''
  }
  const saveSlowDownData = async (newRow) => {
    setLoading(true)
    try {
      var plantId = PLANT_ID

      const slowDownDetailsMEG = newRow.map((row) => ({
        productId: (() => {
          const matched = allProducts.find(
            (p) => p.displayName === row.productName1,
          )
          return matched?.realId || null
        })(),
        productName: row.productName1,
        discription: row.discription,
        durationInHrs: (() => {
          const v = findDuration('1', row)
          if (!v) return null
          const [h = '00', m = '00'] = String(v).split('.')
          return `${h.padStart(2, '0')}.${m.padStart(2, '0')}`
        })(),
        maintEndDateTime: addTimeOffset(row.maintEndDateTime),
        maintStartDateTime: addTimeOffset(row.maintStartDateTime),
        remark: row.remark,
        rate: row.rate,
        audityear: AOP_YEAR,
        id: row.idFromApi || null,
        rateEO: row.rateEO,
        rateEOE: row.rateEOE,
      }))
      const slowDownDetailsElastomer = newRow.map((row) => ({
        discription: row.discription,
        durationInHrs: (() => {
          const v = findDuration('1', row)
          if (!v) return null
          const [h = '00', m = '00'] = String(v).split('.')
          return `${h.padStart(2, '0')}.${m.padStart(2, '0')}`
        })(),
        maintEndDateTime: addTimeOffset(row.maintEndDateTime),
        maintStartDateTime: addTimeOffset(row.maintStartDateTime),
        remark: row.remark,
        rate: row.rate,
        audityear: AOP_YEAR,
        id: row.idFromApi || null,
        rateEO: null,
        rateEOE: null,
      }))
      const response = await DataService.saveSlowdownData(
        plantId,
        lowerVertName === 'elastomer'
          ? slowDownDetailsElastomer
          : slowDownDetailsMEG,
        keycloak,
      )

      const maintenanceResponse =
        await MaintenanceDetailsApiService.getMaintenanceData(keycloak)

      setSnackbarOpen(true)

      setSnackbarData({
        message: 'Saved Successfully!',
        severity: 'success',
      })
      setModifiedCells({})

      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
      setLoading(false)

      return response
    } catch (error) {
      console.error('Error saving data:', error)
      setLoading(false)
    } finally {
      fetchData()

      setLoading(false)
    }
  }
  const saveSlowDownConfigurationData = async (row) => {
    setLoading(true)
    try {
      var plantId = PLANT_ID

      const year = AOP_YEAR

      const response = await DataService.saveSlowdownConfigData(
        plantId,
        year,
        row,
        keycloak,
      )
      if (response?.code === 200) {
        setSnackbarOpen(true)

        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells2({})

        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
        fetchConfigurationData()
      } else {
        setSnackbarOpen(true)

        setSnackbarData({
          message: 'Data Saved Failed!',
          severity: 'error',
        })
        setLoading(false)
      }

      return response
    } catch (error) {
      console.error('Error saving data:', error)
      setLoading(false)
    } finally {
      // fetchConfigurationData()
      // setLoading(false)
    }
  }

  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCells)

      if (data.length === 0) {
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

      // Select required fields based on vertical
      const requiredFields = ['discription', 'remark']
      const requiredFieldsForElastomer = ['discription', 'remark', 'rate']
      const requiredFieldsForMeg = [
        'discription',
        'remark',
        'rateEOE',
        'rateEO',
      ]

      const chosenFields =
        lowerVertName === 'elastomer'
          ? requiredFieldsForElastomer
          : lowerVertName === 'meg'
            ? requiredFieldsForMeg
            : requiredFields

      // Missing required fields
      for (const record of data) {
        for (const field of chosenFields) {
          const value = record[field]
          if (
            value === null ||
            value === undefined ||
            (typeof value === 'string' && value.trim() === '')
          ) {
            record.isError = true
            setSnackbarOpen(true)
            setSnackbarData({
              message: `Required field "${field}" is missing for "${record.discription || 'this record'}".`,
              severity: 'error',
            })
            return
          }
        }
      }

      const validationMessage = validateFields(data, chosenFields)
      if (validationMessage) {
        data.forEach((r) => (r.isError = true))
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }

      // Duplicate check
      const allDescriptions = rows.map((r) =>
        (r.discription || '').trim().toLowerCase(),
      )
      const duplicate = allDescriptions.find(
        (d, i) => d && allDescriptions.indexOf(d) !== i,
      )

      if (duplicate) {
        rows.forEach((row) => {
          if ((row.discription || '').trim().toLowerCase() === duplicate) {
            row.isError = true
          }
        })
        setSnackbarOpen(true)
        setSnackbarData({
          message: `The description "${duplicate}" already exists. Please enter a unique description.`,
          severity: 'error',
        })
        return
      }

      // Date required + Start < End check
      for (const record of data) {
        const startMissing = !record.maintStartDateTime
        const endMissing = !record.maintEndDateTime
        if (startMissing || endMissing) {
          record.isError = true
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

      // MEG specific checks
      if (lowerVertName === 'meg') {
        // Month span check
        for (const row of rows) {
          const start = new Date(row.maintStartDateTime)
          const end = new Date(row.maintEndDateTime)
          if (isNaN(start) || isNaN(end)) continue

          const isSameMonth =
            start.getMonth() === end.getMonth() &&
            start.getFullYear() === end.getFullYear()

          if (!isSameMonth) {
            row.isError = true
            setSnackbarOpen(true)
            setSnackbarData({
              message: `The slowdown timeframe for '${row.discription}' spans multiple months. Please split into separate entries.`,
              severity: 'error',
            })
            return
          }
        }

        // Overlap within Slowdown
        for (let i = 0; i < rows.length; i++) {
          const a = rows[i]
          const aStart = new Date(a.maintStartDateTime).getTime()
          const aEnd = new Date(a.maintEndDateTime).getTime()
          if (isNaN(aStart) || isNaN(aEnd)) continue

          for (let j = i + 1; j < rows.length; j++) {
            const b = rows[j]
            const bStart = new Date(b.maintStartDateTime).getTime()
            const bEnd = new Date(b.maintEndDateTime).getTime()
            if (isNaN(bStart) || isNaN(bEnd)) continue

            if (aStart < bEnd && bStart < aEnd) {
              a.isError = true
              b.isError = true
              setSnackbarOpen(true)
              setSnackbarData({
                message: `The slowdown timeframe for "${a.discription}" overlaps with "${b.discription}". Please ensure no overlapping of timeframes.`,
                severity: 'error',
              })
              return
            }
          }
        }

        // Cross overlap with Shutdown
        for (let i = 0; i < rows.length; i++) {
          const a = rows[i]
          const aStart = new Date(a.maintStartDateTime).getTime()
          const aEnd = new Date(a.maintEndDateTime).getTime()
          if (isNaN(aStart) || isNaN(aEnd)) continue

          for (let j = 0; j < rowsShutdown.length; j++) {
            const b = rowsShutdown[j]
            const bStart = new Date(b.maintStartDateTime).getTime()
            const bEnd = new Date(b.maintEndDateTime).getTime()
            if (isNaN(bStart) || isNaN(bEnd)) continue

            if (aStart < bEnd && bStart < aEnd) {
              a.isError = true
              setSnackbarOpen(true)
              setSnackbarData({
                message: `The timeframe for "${a.discription} (Slowdown)" overlaps with "${b.discription} (Shutdown)". Please ensure no overlapping of timeframes.`,
                severity: 'error',
              })
              return
            }
          }
        }
      }

      // If validations pass
      data.forEach((r) => (r.isError = false)) // Clear errors
      saveSlowDownData(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCells, rows, rowsShutdown, lowerVertName])

  const saveChanges2 = React.useCallback(async () => {
    try {
      let finalData = Object.values(modifiedCells2).map((mdata, i) => ({
        ...mdata,
        normParameterFKId: mdata.NormParameter_FK_Id,
        NormParameter_FK_Id: undefined,
        inEdit: undefined,
        particulars: undefined,
        id: undefined,
        aopYear: undefined,
        normParameterDisplayName: undefined,
        plantId: undefined,
        DisplayName: undefined,
        NormTypeName: undefined,
        srNo: undefined,
        isEditable: undefined,
        IsEditable: undefined,
        Particulars: undefined,
        uom: undefined,
        UOM: undefined,
      }))

      var data = Object.values(modifiedCells2)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      saveSlowDownConfigurationData(finalData)
    } catch (error) {
      // setIsSaving(false);
    }
  }, [modifiedCells2])

  const updateSlowdownData = async (newRow) => {
    try {
      var maintenanceId = newRow?.maintenanceId

      const slowDownDetails = {
        productId: newRow.product,
        discription: newRow.discription,
        durationInHrs: newRow.durationInHrs,
        maintEndDateTime: newRow.maintEndDateTime,
        maintStartDateTime: newRow.maintStartDateTime,
        remark: newRow.remarks,
        rate: newRow.rate,
      }

      const response = await DataService.updateSlowdownData(
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
      console.error('Error saving Slowdown data:', error)
    } finally {
      fetchData()

      // fetchConfigurationData()
    }
  }
  const updateSlowdownData2 = async (newRow) => {
    try {
      var maintenanceId = newRow?.maintenanceId

      const slowDownDetails = {
        productId: newRow.product,
        discription: newRow.discription,
        durationInHrs: newRow.durationInHrs,
        maintEndDateTime: newRow.maintEndDateTime,
        maintStartDateTime: newRow.maintStartDateTime,
        remark: newRow.remarks,
        rate: newRow.rate,
      }

      const response = await DataService.updateSlowdownData(
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
      console.error('Error saving data:', error)
    } finally {
      fetchData()
    }
  }

  const fetchData = async () => {
    setLoading(true)
    try {
      const data = await DataService.getSlowDownPlantData(
        keycloak,
        plantID?.plantId,
      )
      const dataShutDown = await DataService.getShutDownPlantData(
        keycloak,
        plantID?.plantId,
      )
      const formattedDataShutDown = dataShutDown?.map((item, index) => ({
        ...item,
        idFromApi: item?.id,
        id: index,
        originalRemark: item.remark,
        inEdit: false,
        maintStartDateTime: new Date(item?.maintStartDateTime),
        maintEndDateTime: new Date(item?.maintEndDateTime),
      }))
      setRowsShutdown(formattedDataShutDown)

      const formattedData = data.map((item, index) => ({
        ...item,
        product: item.productId,
        productName1: item.productName || '',
        idFromApi: item?.maintenanceId || item?.id,
        id: index,
        originalRemark: item.remark,
        maintStartDateTime: new Date(item?.maintStartDateTime),
        maintEndDateTime: new Date(item?.maintEndDateTime),
      }))

      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching SlowDown data:', error)
      setLoading(false)
    }
  }

  const [allRedCell, setAllRedCell] = useState([])

  const fetchConfigurationData = async () => {
    setRows2([])
    setLoading(true)
    try {
      var response = await DataService.getSlowDownConfigurationData(keycloak)
      var data = response?.data?.data
      var redCells = response?.data?.changedData

      const normalized = redCells.map((obj) => ({
        ...obj,
        normParameterFKId: obj.NormParameter_FK_Id.toUpperCase(),
      }))
      setAllRedCell(normalized)

      const formattedData = data.map((item, index) => {
        const parsedItem = Object.entries(item).reduce((acc, [key, value]) => {
          if (
            typeof value === 'string' &&
            !isNaN(value) &&
            value.trim() !== ''
          ) {
            const parsedValue = parseFloat(value)
            acc[key] = isNaN(parsedValue) ? value : parsedValue
          } else {
            acc[key] = value
          }
          return acc
        }, {})

        return {
          ...parsedItem,
          id: index,
          particulars: item.DisplayName,
          Particulars: item?.NormTypeName,
          isEditable: item?.IsEditable,
        }
      })

      setRows2(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching SlowDown Configuration data:', error)
      setLoading(false)
      setRows2([])
    }
  }
  const fetchData2 = async () => {
    setLoading(true)
    setColDefs2([])
    try {
      var data1 = await DataService.getSlowDownPlantDataTab(keycloak)

      const removedCols = [
        'srNo',
        'NormTypeName',
        'DisplayName',
        'NormParameter_FK_Id',
        'normParameterDisplayName',
        'aopYear',
        'plantId',
        'IsEditable',
      ]
      if (data1?.code === 200 && Array.isArray(data1.data)) {
        const dynamicColDefs = data1.data.map((item) => ({
          field: item.field,
          title: item.title,
          editable:
            item.field === 'particulars' || item.field.toLowerCase() === 'uom'
              ? false
              : true,
          hidden: removedCols.includes(item.field),
          ...(item.field !== 'particulars' &&
            item.field.toLowerCase() !== 'uom' && {
              format: '{0:#.###}',
              type: 'number',
            }),
        }))

        setColDefs2(dynamicColDefs)
        fetchConfigurationData()
      } else {
        setColDefs2([])
        setLoading(false)
      }
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    setSelectedTab(0)
    const getAllProducts = async () => {
      try {
        var data = []
        if (lowerVertName == 'meg')
          data = await DataService.getAllProducts(keycloak, null)
        else {
          data = await DataService.getAllProductsAll(keycloak, 'Production')
        }
        var productList = []
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
            id: product.displayname,
            displayName: product.displayName,
            realId: product.id,
          }))
        }

        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching data', error)
      } finally {
        // handleMenuClose();
      }
    }

    fetchData()

    getAllProducts()

    if (selectedTab == 1) {
      setColDefs2([])
      setRows2([])
      fetchData2()
    }
  }, [oldYear, yearChanged, keycloak, plantID])

  const focusFirstField = async () => {
    const newRowId = rows.length
      ? Math.max(...rows.map((row) => row.id)) + 1
      : 1
    setRowModesModel((oldModel) => ({
      ...oldModel,
      [newRowId]: { mode: GridRowModes.Edit, fieldToFocus: 'product' },
    }))
  }

  const colDefs = useMemo(() => {
    switch (lowerVertName) {
      case verticalEnums.PE:
        return SlowDownPeColumns
      case verticalEnums.PP:
        return SlowDownPpColumns
      case verticalEnums.PTA:
        return SlowDownPtaColumns
      case verticalEnums.ELASTOMER:
        return SlowDownElastomerColumns
      case verticalEnums.MEG:
        return SlowDownMegColumns
      case verticalEnums.AROMATICS:
        return SlowDownAromaticsColumns
      default:
        return SlowDownMegColumns
    }
  }, [lowerVertName])

  const deleteRowData = async (paramsForDelete) => {
    setLoading(true)

    try {
      const { idFromApi, id } = paramsForDelete
      const deleteId = id

      if (!idFromApi) {
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
      }

      if (idFromApi) {
        await DataService.deleteSlowdownData(idFromApi, keycloak)
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
      console.error('Error deleting Record!', error)
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
      response = await DataService.slowdownDetailsExport(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
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
  const uploadShutdownDetails = async (rawFile) => {
    setLoading(true)

    try {
      let response

      response = await DataService.ImportSlowdownDetails(
        rawFile,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

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
        link.setAttribute('download', 'Error File - Slowdown.xlsx')
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
    uploadShutdownDetails(rawFile)
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
      downloadExcelBtn: true,
      uploadExcelBtn:
        lowerVertName === 'pe' || lowerVertName === 'pp' ? true : false,
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

      {lowerVertName === 'meg' && (
        <Box style={{ margin: 0, padding: 0 }}>
          <Tabs
            value={selectedTab}
            onChange={handleTabChange}
            sx={{
              borderBottom: '0px solid #ccc',
              '.MuiTabs-indicator': { display: 'none' },
              margin: '0px 0px 0px 0px',
              minHeight: '28px',
            }}
          >
            <Tab
              label='Slowdown Details'
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                fontSize: '0.75rem',
                padding: '9px',
                minHeight: '12px',
              }}
            />

            <Tab
              label='Configuration'
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                fontSize: '0.75rem',
                padding: '9px',
                minHeight: '12px',
              }}
            />
          </Tabs>
        </Box>
      )}

      {selectedTab === 0 && (
        <KendoDataTables
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          setRows={setRows}
          columns={colDefs}
          rows={rows}
          paginationOptions={[100, 200, 300]}
          updateSlowdownData={updateSlowdownData}
          saveChanges={saveChanges}
          errorRows={errorRows}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          apiRef={apiRef}
          deleteId={deleteId}
          setDeleteId={setDeleteId}
          setOpen1={setOpen1}
          open1={open1}
          fetchData={fetchData}
          handleRemarkCellClick={handleRemarkCellClick}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          unsavedChangesRef={unsavedChangesRef}
          deleteRowData={deleteRowData}
          permissions={adjustedPermissions}
          handleCancelClick={handleCancelClick}
          focusFirstField={focusFirstField}
          allProducts={allProducts}
          disableRedHighlight={true}
          handleExcelUpload={handleExcelUpload}
          downloadExcelForConfiguration={downloadExcelForConfiguration}
        />
      )}

      {selectedTab === 1 && (
        <KendoDataTables
          modifiedCells={modifiedCells2}
          setModifiedCells={setModifiedCells2}
          setRows={setRows2}
          columns={colDefs2}
          rows={rows2}
          paginationOptions={[100, 200, 300]}
          updateSlowdownData={updateSlowdownData2}
          saveChanges={saveChanges2}
          errorRows={errorRows}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          apiRef={apiRef}
          deleteId={deleteId}
          setDeleteId={setDeleteId}
          setOpen1={setOpen1}
          open1={open1}
          fetchData={fetchData2}
          unsavedChangesRef={unsavedChangesRef}
          permissions={{
            saveBtn: true,
            allAction: true,
            onlyCellUpdate: true,
            downloadExcelBtnFromUI: true,
            ExcelName: `${lowerVertName}_Slowdown Activities Configuration`,
          }}
          handleCancelClick={handleCancelClick}
          groupBy='Particulars'
          allRedCell={allRedCell}
        />
      )}
    </div>
  )
}

export default SlowDown
