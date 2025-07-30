import React, { useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'

import { SlowDownElastomerColumns } from 'components/colums/ElastomerColums'
import { SlowDownMegColumns } from 'components/colums/MegColums'
import { SlowDownPeColumns } from 'components/colums/PeColums'
import { SlowDownPpColumns } from 'components/colums/PpColums'
import { SlowDownPtaColumns } from 'components/colums/PtaColums'
import { verticalEnums } from 'enums/verticalEnums'
import { validateFields } from 'utils/validationUtils'
import { Box, Tab, Tabs } from '../../../node_modules/@mui/material/index'
import { GridRowModes } from '../../../node_modules/@mui/x-data-grid/models/gridEditRowModel'
import KendoDataTables from './index'

const SlowDown = ({ permissions }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, yearChanged, oldYear, plantID } = dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
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
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

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
        audityear: localStorage.getItem('year'),
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
        audityear: localStorage.getItem('year'),
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

      const maintenanceResponse = await DataService.getMaintenanceData(keycloak)

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
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      const year = localStorage.getItem('year')

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
    setErrorRows(new Set()) // Clear old errors
    if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
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

    // 🔹 New addition: track missing required fields
    const rowsWithErrors = new Set()
    for (const record of data) {
      for (const field of chosenFields) {
        if (!record[field] || record[field].trim?.() === '') {
          rowsWithErrors.add(record.id)
          break // exit once we know this record has missing field
        }
      }
    }

    const validationMessage = validateFields(data, chosenFields)
      if (validationMessage) {
      setErrorRows(rowsWithErrors) // highlight error rows
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }

    // 🔹 Duplicate check
    const duplicateRows = new Set()
      const allDescriptions = rows.map((r) =>
        (r.discription || '').trim().toLowerCase(),
      )
      const duplicate = allDescriptions.find(
        (d, i) => d && allDescriptions.indexOf(d) !== i,
      )

    if (duplicate) {
      rows.forEach((row) => {
        if ((row.discription || '').trim().toLowerCase() === duplicate) {
          duplicateRows.add(row.id)
        }
      })
      setErrorRows(duplicateRows)
        setSnackbarOpen(true)
        setSnackbarData({
        message: `The description "${duplicate}" already exists. Please enter a unique description.`,
          severity: 'error',
        })
        return
      }

    // 🔹 Time validation
    const timeErrorRows = new Set()
      for (const record of data) {
        // 🔹 Required Date Validation
          const dateRequiredRows = new Set()
          for (const record of data) {
            if (!record.maintStartDateTime || !record.maintEndDateTime) {
              dateRequiredRows.add(record.id)
            }
          }

          if (dateRequiredRows.size > 0) {
            setErrorRows(dateRequiredRows)
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
        timeErrorRows.add(record.id)
        setErrorRows(timeErrorRows)
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Start time must be before end time for "${record.discription || 'this record'}".`,
            severity: 'error',
          })
          return
        }
      }

    // 🔹 MEG specific checks
      if (lowerVertName == 'meg') {
      const monthSpanRows = new Set()
      for (const row of rows) {
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
          monthSpanRows.add(row.id)
          setErrorRows(monthSpanRows)
            setSnackbarOpen(true)
            setSnackbarData({
            message: `The slowdown timeframe for '${row.discription}' spans multiple months. Please split into separate entries.`,
              severity: 'error',
            })
            return
          }
        }

      // Overlap within Slowdown
      const overlapRows = new Set()
        for (let i = 0; i < allRecords.length; i++) {
          const a = allRecords[i]
          const aStart = new Date(a.maintStartDateTime).getTime()
          const aEnd = new Date(a.maintEndDateTime).getTime()

          if (isNaN(aStart) || isNaN(aEnd)) continue

          for (let j = i + 1; j < allRecords.length; j++) {
            const b = allRecords[j]
            const bStart = new Date(b.maintStartDateTime).getTime()
            const bEnd = new Date(b.maintEndDateTime).getTime()

            if (isNaN(bStart) || isNaN(bEnd)) continue

            if (aStart < bEnd && bStart < aEnd) {
            overlapRows.add(a.id)
            overlapRows.add(b.id)
            setErrorRows(overlapRows)
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
      const crossOverlapRows = new Set()
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
            crossOverlapRows.add(a.id)
            setErrorRows(crossOverlapRows)
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
    setErrorRows(new Set()) // Clear errors
      saveSlowDownData(data)
    } catch (error) {
    console.log('Error saving changes:', error)
    }
}, [modifiedCells, rows, rowsShutdown, lowerVertName, setErrorRows])

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
          await DataService.getMaintenanceData(keycloak)
      } else {
        setLoading(false)
      }
    } catch (error) {
      console.error('Error deleting Record!', error)
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

      {lowerVertName === 'meg' && (
        <Box style={{ margin: 0, padding: 0 }}>
          <Tabs
            value={selectedTab}
            onChange={handleTabChange}
            sx={{
              borderBottom: '0px solid #ccc',
              '.MuiTabs-indicator': { display: 'none' },
              margin: '0px 0px 0px 0px',
              minHeight: '35px',
            }}
          >
            <Tab
              label='Slowdown Details'
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                padding: '9px',
                minHeight: '10px',
              }}
            />

            <Tab
              label='Configuration'
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                p: '9px',
                minHeight: 10,
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
          permissions={{ saveBtn: true, allAction: true, onlyCellUpdate: true }}
          handleCancelClick={handleCancelClick}
          groupBy='Particulars'
          allRedCell={allRedCell}
        />
      )}
    </div>
  )
}

export default SlowDown
