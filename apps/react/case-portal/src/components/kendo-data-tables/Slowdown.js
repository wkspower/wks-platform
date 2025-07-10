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

  const [_plantID, set_PlantID] = useState('')

  useEffect(() => {
    if (plantID?.plantId) {
      set_PlantID(plantID?.plantId)
    }
  }, [plantID])

  const [selectedTab, setSelectedTab] = useState(0)
  const handleTabChange = (event, newValue) => {
    setSelectedTab(newValue)
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
        rate: row.rate || null,
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
        message: 'Slowdown data Saved Successfully!',
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
      console.error('Error saving Slowdown data:', error)
      setLoading(false)
    } finally {
      fetchData()
      fetchData2()
      setLoading(false)
    }
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

      const requiredFields = [
        'maintStartDateTime',
        'maintEndDateTime',
        'discription',
        'remark',
        // 'rate',
        // 'durationInHrs',
        // 'productName1',
      ]
      const requiredFieldsForElastomer = [
        'maintStartDateTime',
        'maintEndDateTime',
        'discription',
        'remark',
        'rate',
      ]
      const validationMessage = validateFields(
        data,
        lowerVertName === 'elastomer'
          ? requiredFieldsForElastomer
          : requiredFields,
      )
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }

      saveSlowDownData(data)
    } catch (error) {
      // setIsSaving(false);
    }
  }, [modifiedCells])
  const saveChanges2 = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCells2)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      saveSlowDownData(data)
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
        message: 'Slowdown data Updated successfully!',
        severity: 'success',
      })

      return response
    } catch (error) {
      console.error('Error saving Slowdown data:', error)
    } finally {
      fetchData2()
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
        message: 'Slowdown data Updated successfully!',
        severity: 'success',
      })

      return response
    } catch (error) {
      console.error('Error saving Slowdown data:', error)
    } finally {
      fetchData2()
    }
  }

  const fetchData = async () => {
    setLoading(true)
    try {
      const data = await DataService.getSlowDownPlantData(keycloak)

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
  const fetchData2 = async () => {
    setLoading(true)
    try {
      const data1 = await DataService.getSlowDownPlantDataTab(keycloak)

      if (data1?.code === 200 && Array.isArray(data1.data)) {
        const dynamicColDefs = data1.data.map((item) => ({
          field: item.field,
          title: item.title,
        }))

        setColDefs2(dynamicColDefs)
      } else {
        setColDefs2([])
      }

      setRows2([])
      setLoading(false)
    } catch (error) {
      console.error('Error fetching SlowDown data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
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
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }

    fetchData()
    fetchData2()

    // saveShutdownData()
    getAllProducts()
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

  // const colDefs = [
  //   {
  //     field: 'discription',
  //     title: 'Slowdown Desc',
  //     editable: true,
  //   },

  //   {
  //     field: 'maintenanceId',
  //     title: 'maintenanceId',
  //     editable: false,
  //     hidden: true,
  //   },

  //   {
  //     field: 'productName1',
  //     title: 'Particulars',
  //     editable: true,
  //     hidden:
  //       lowerVertName === 'elastomer' || lowerVertName === 'meg' ? true : false,
  //   },

  //   {
  //     field: 'maintStartDateTime',
  //     title: 'SD- From',
  //     type: 'dateTime',
  //     editable: true,
  //   },

  //   {
  //     field: 'maintEndDateTime',
  //     title: 'SD- To',
  //     type: 'dateTime',
  //     editable: true,
  //   },

  //   {
  //     field: 'durationInHrs',
  //     title: 'Duration (hrs)',
  //     editable: true,
  //   },

  //   {
  //     field: 'rate',
  //     title: 'Rate (TPH)',
  //     editable: true,
  //     type: 'number',
  //     hidden: lowerVertName === 'meg' ? true : false,
  //   },

  //   {
  //     field: 'rateEOE',
  //     title: 'Rate (EOE)',
  //     editable: true,
  //     type: 'number',
  //     hidden: lowerVertName === 'meg' ? false : true,
  //   },
  //   {
  //     field: 'rateEO',
  //     title: 'Rate (EO)',
  //     editable: true,
  //     type: 'number',
  //     hidden: lowerVertName === 'meg' ? false : true,
  //   },

  //   {
  //     field: 'remark',
  //     title: 'Remarks',
  //     editable: true,
  //   },
  // ]

  const deleteRowData = async (paramsForDelete) => {
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
        fetchData2()
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
              padding: '9px',
              minHeight: '10px',
            }}
          />
        </Tabs>
      </Box>

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
          permissions={{ saveBtn: true, allAction: true }}
          handleCancelClick={handleCancelClick}
        />
      )}
    </div>
  )
}

export default SlowDown
