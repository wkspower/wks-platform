import { DataService } from 'services/DataService'
import React, { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import { useSelector } from 'react-redux'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'

import { GridRowModes } from '../../../node_modules/@mui/x-data-grid/models/gridEditRowModel'
import KendoDataTables from './index'
import { validateFields } from 'utils/validationUtils'

const SlowDown = ({ permissions }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear, plantID } =
    dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical

  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [rowModesModel, setRowModesModel] = useState({})
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const [rows, setRows] = useState()
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
          return matched?.realId || ''
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
      setLoading(false)
    }
  }
  const saveChanges = React.useCallback(async () => {
    setTimeout(() => {
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
          'rate',
          // 'durationInHrs',
          'productName1',
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
    }, 400)
  }, [modifiedCells])

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
      fetchData()
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
      setLoading(false) // Hide loading
    } catch (error) {
      console.error('Error fetching SlowDown data:', error)
      setLoading(false) // Hide loading
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

  const colDefs = [
    {
      field: 'discription',
      title: 'Slowdown Desc',
      editable: true,
    },

    {
      field: 'maintenanceId',
      title: 'maintenanceId',
      editable: false,
      hidden: true,
    },

    {
      field: 'productName1',
      title: 'Particulars',
      editable: true,
      hidden: lowerVertName === 'elastomer' ? true : false,
    },

    {
      field: 'maintStartDateTime',
      title: 'SD- From',
      type: 'dateTime',
      editable: true,
    },

    {
      field: 'maintEndDateTime',
      title: 'SD- To',
      type: 'dateTime',
      editable: true,
    },

    {
      field: 'durationInHrs',
      title: 'Duration (hrs)',
      editable: true,
    },

    {
      field: 'rate',
      title: 'Rate (TPH)',
      editable: true,
      type: 'number',
    },

    {
      field: 'remark',
      title: 'Remarks',
      editable: true,
    },
  ]

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
    </div>
  )
}

export default SlowDown

// import { DataService } from 'services/DataService'
// import React, { useState, useEffect } from 'react'
// import { useSession } from 'SessionStoreContext'
// import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
// import { useSelector } from 'react-redux'
// import { Tabs, Tab, Box } from '@mui/material'

// import Backdrop from '@mui/material/Backdrop'
// import CircularProgress from '@mui/material/CircularProgress'

// import { GridRowModes } from '../../../node_modules/@mui/x-data-grid/models/gridEditRowModel'
// import KendoDataTables from './index'
// import { validateFields } from 'utils/validationUtils'

// const SlowDown = ({ permissions }) => {
//   const dataGridStore = useSelector((state) => state.dataGridStore)
//   const { sitePlantChange, verticalChange, yearChanged, oldYear, plantID } =
//     dataGridStore
//   const isOldYear = oldYear?.oldYear
//   const vertName = verticalChange?.selectedVertical

//   const lowerVertName = vertName?.toLowerCase() || 'meg'
//   const [rowModesModel, setRowModesModel] = useState({})
//   const [modifiedCells, setModifiedCells] = React.useState({})
//   const [allProducts, setAllProducts] = useState([])
//   const apiRef = useGridApiRef()
//   const [open1, setOpen1] = useState(false)
//   const [deleteId, setDeleteId] = useState(null)
//   const [rows, setRows] = useState()

//   const [loading, setLoading] = useState(false)
//   const [snackbarData, setSnackbarData] = useState({
//     message: '',
//     severity: 'info',
//   })
//   const [snackbarOpen, setSnackbarOpen] = useState(false)
//   const keycloak = useSession()

//   const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
//   const [currentRemark, setCurrentRemark] = useState('')
//   const [currentRowId, setCurrentRowId] = useState(null)

//   const unsavedChangesRef = React.useRef({
//     unsavedRows: {},
//     rowsBeforeChange: {},
//   })

//   const [_plantID, set_PlantID] = useState('')

//   useEffect(() => {
//     if (plantID?.plantId) {
//       set_PlantID(plantID?.plantId)
//     }
//   }, [plantID])

//   const [selectedTab, setSelectedTab] = useState(0)

//   const handleTabChange = (event, newValue) => {
//     setSelectedTab(newValue)
//   }

//   const handleCancelClick = () => () => {
//     const rowsInEditMode = Object.keys(rowModesModel).filter(
//       (id) => rowModesModel[id]?.mode === 'edit',
//     )

//     rowsInEditMode.forEach((id) => {
//       apiRef.current.stopRowEditMode({ id })
//     })
//   }

//   const handleRemarkCellClick = (row) => {
//     setCurrentRemark(row.remark || '')
//     setCurrentRowId(row.id)
//     setRemarkDialogOpen(true)
//   }

//   function addTimeOffset(dateTime) {
//     if (!dateTime) return null
//     const date = new Date(dateTime)
//     date.setUTCHours(date.getUTCHours() + 5)
//     date.setUTCMinutes(date.getUTCMinutes() + 30)
//     return date
//   }
//   const findDuration = (v, row) => {
//     if (row.durationInHrs) return row.durationInHrs

//     if (row.maintStartDateTime && row.maintEndDateTime) {
//       const start = new Date(row.maintStartDateTime)
//       const end = new Date(row.maintEndDateTime)

//       if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
//         const durationInMs = end - start
//         const durationInMinutes = durationInMs / (1000 * 60)
//         const hours = Math.floor(durationInMinutes / 60)
//         const minutes = durationInMinutes % 60
//         return `${hours}.${minutes.toString().padStart(2, '0')}`
//       }
//     }

//     return ''
//   }
//   const saveSlowDownData = async (newRow) => {
//     setLoading(true)
//     try {
//       var plantId = ''
//       const storedPlant = localStorage.getItem('selectedPlant')
//       if (storedPlant) {
//         const parsedPlant = JSON.parse(storedPlant)
//         plantId = parsedPlant.id
//       }

//       const slowDownDetailsMEG = newRow.map((row) => ({
//         productId: (() => {
//           const matched = allProducts.find(
//             (p) => p.displayName === row.productName1,
//           )
//           return matched?.realId || ''
//         })(),
//         productName: row.productName1,
//         discription: row.discription,
//         durationInHrs: (() => {
//           const v = findDuration('1', row)
//           if (!v) return null
//           const [h = '00', m = '00'] = String(v).split('.')
//           return `${h.padStart(2, '0')}.${m.padStart(2, '0')}`
//         })(),
//         maintEndDateTime: addTimeOffset(row.maintEndDateTime),
//         maintStartDateTime: addTimeOffset(row.maintStartDateTime),
//         remark: row.remark,
//         rate: row.rate,
//         audityear: localStorage.getItem('year'),
//         id: row.idFromApi || null,
//       }))
//       const slowDownDetailsElastomer = newRow.map((row) => ({
//         discription: row.discription,
//         durationInHrs: (() => {
//           const v = findDuration('1', row)
//           if (!v) return null
//           const [h = '00', m = '00'] = String(v).split('.')
//           return `${h.padStart(2, '0')}.${m.padStart(2, '0')}`
//         })(),
//         maintEndDateTime: addTimeOffset(row.maintEndDateTime),
//         maintStartDateTime: addTimeOffset(row.maintStartDateTime),
//         remark: row.remark,
//         rate: row.rate,
//         audityear: localStorage.getItem('year'),
//         id: row.idFromApi || null,
//       }))
//       const response = await DataService.saveSlowdownData(
//         plantId,
//         lowerVertName === 'elastomer'
//           ? slowDownDetailsElastomer
//           : slowDownDetailsMEG,
//         keycloak,
//       )

//       const maintenanceResponse = await DataService.getMaintenanceData(keycloak)

//       setSnackbarOpen(true)

//       setSnackbarData({
//         message: 'Slowdown data Saved Successfully!',
//         severity: 'success',
//       })
//       setModifiedCells({})

//       unsavedChangesRef.current = {
//         unsavedRows: {},
//         rowsBeforeChange: {},
//       }
//       setLoading(false)

//       return response
//     } catch (error) {
//       console.error('Error saving Slowdown data:', error)
//       setLoading(false)
//     } finally {
//       fetchData()
//       setLoading(false)
//     }
//   }
//   const saveChanges = React.useCallback(async () => {
//     setTimeout(() => {
//       try {
//         var data = Object.values(modifiedCells)
//         if (data.length == 0) {
//           setSnackbarOpen(true)
//           setSnackbarData({
//             message: 'No Records to Save!',
//             severity: 'info',
//           })
//           return
//         }

//         const requiredFields = [
//           'maintStartDateTime',
//           'maintEndDateTime',
//           'discription',
//           'remark',
//           'rate',
//           // 'durationInHrs',
//           'productName1',
//         ]
//         const requiredFieldsForElastomer = [
//           'maintStartDateTime',
//           'maintEndDateTime',
//           'discription',
//           'remark',
//           'rate',
//         ]
//         const validationMessage = validateFields(
//           data,
//           lowerVertName === 'elastomer'
//             ? requiredFieldsForElastomer
//             : requiredFields,
//         )
//         if (validationMessage) {
//           setSnackbarOpen(true)
//           setSnackbarData({
//             message: validationMessage,
//             severity: 'error',
//           })
//           return
//         }

//         saveSlowDownData(data)
//       } catch (error) {
//         // setIsSaving(false);
//       }
//     }, 400)
//   }, [modifiedCells])

//   const updateSlowdownData = async (newRow) => {
//     try {
//       var maintenanceId = newRow?.maintenanceId

//       const slowDownDetails = {
//         productId: newRow.product,
//         discription: newRow.discription,
//         durationInHrs: newRow.durationInHrs,
//         maintEndDateTime: newRow.maintEndDateTime,
//         maintStartDateTime: newRow.maintStartDateTime,
//         remark: newRow.remarks,
//         rate: newRow.rate,
//       }

//       const response = await DataService.updateSlowdownData(
//         maintenanceId,
//         slowDownDetails,
//         keycloak,
//       )

//       setSnackbarOpen(true)

//       setSnackbarData({
//         message: 'Slowdown data Updated successfully!',
//         severity: 'success',
//       })

//       return response
//     } catch (error) {
//       console.error('Error saving Slowdown data:', error)
//     } finally {
//       fetchData()
//     }
//   }

//   const fetchData = async () => {
//     setLoading(true)
//     try {
//       const data = await DataService.getSlowDownPlantData(keycloak)

//       const formattedData = data.map((item, index) => ({
//         ...item,
//         product: item.productId,
//         productName1: item.productName || '',
//         idFromApi: item?.maintenanceId || item?.id,
//         id: index,
//         originalRemark: item.remark,
//         maintStartDateTime: new Date(item?.maintStartDateTime),
//         maintEndDateTime: new Date(item?.maintEndDateTime),
//       }))

//       setRows(formattedData)
//       setLoading(false) // Hide loading
//     } catch (error) {
//       console.error('Error fetching SlowDown data:', error)
//       setLoading(false) // Hide loading
//     }
//   }

//   useEffect(() => {
//     const getAllProducts = async () => {
//       try {
//         var data = []
//         if (lowerVertName == 'meg')
//           data = await DataService.getAllProducts(keycloak, null)
//         else {
//           data = await DataService.getAllProductsAll(keycloak, 'Production')
//         }
//         var productList = []
//         if (lowerVertName === 'meg') {
//           productList = data
//             .filter((product) => ['EO', 'EOE'].includes(product.displayName))
//             .map((product) => ({
//               id: product.displayName,
//               displayName: product.displayName,
//               realId: product.id,
//             }))
//         } else {
//           productList = data.map((product) => ({
//             id: product.displayname,
//             displayName: product.displayName,
//             realId: product.id,
//           }))
//         }

//         setAllProducts(productList)
//       } catch (error) {
//         console.error('Error fetching product:', error)
//       } finally {
//         // handleMenuClose();
//       }
//     }

//     fetchData()
//     // saveShutdownData()
//     getAllProducts()
//   }, [oldYear, yearChanged, keycloak, plantID])

//   const focusFirstField = async () => {
//     const newRowId = rows.length
//       ? Math.max(...rows.map((row) => row.id)) + 1
//       : 1
//     setRowModesModel((oldModel) => ({
//       ...oldModel,
//       [newRowId]: { mode: GridRowModes.Edit, fieldToFocus: 'product' },
//     }))
//   }

//   const colDefs = [
//     {
//       field: 'discription',
//       title: 'Slowdown Desc',
//       editable: true,
//     },

//     {
//       field: 'maintenanceId',
//       title: 'maintenanceId',
//       editable: false,
//       hidden: true,
//     },

//     {
//       field: 'productName1',
//       title: 'Particulars',
//       editable: true,
//       hidden: lowerVertName === 'elastomer' ? true : true,
//     },

//     {
//       field: 'maintStartDateTime',
//       title: 'SD- From',
//       type: 'dateTime',
//       editable: true,
//     },

//     {
//       field: 'maintEndDateTime',
//       title: 'SD- To',
//       type: 'dateTime',
//       editable: true,
//     },

//     {
//       field: 'durationInHrs',
//       title: 'Duration (hrs)',
//       editable: true,
//     },

//     {
//       field: 'rate',
//       title: 'Operating Production Rate (EOE) ',
//       editable: true,
//       type: 'number',
//     },
//     {
//       field: 'rate',
//       title: 'Operating Production Rate (EO) ',
//       editable: true,
//       type: 'number',
//     },

//     {
//       field: 'remark',
//       title: 'Remarks',
//       editable: true,
//     },
//   ]
//   const colDefs2 = [
//     {
//       field: 'Particulars',
//       title: 'Particulars',
//       editable: false,
//     },

//     {
//       field: 'July',
//       title: 'Slowdown 01 (July)',
//       editable: true,
//     },
//     {
//       field: 'August',
//       title: 'Slowdown 02 (August)',
//       editable: true,
//     },
//     {
//       field: 'September',
//       title: 'Slowdown 03 (September)',
//       editable: true,
//     },
//     {
//       field: 'October',
//       title: 'Slowdown 04 (October)',
//       editable: true,
//     },
//     // {
//     //   field: 'remark',
//     //   title: 'Remarks',
//     //   editable: true,
//     // },
//     {
//       field: 'id',
//       title: 'Id',
//       hidden: true,
//     },
//   ]
//   const configDummyData = [
//     {
//       id: 1,
//       Particulars: 'MEG Yield',
//       July: 0.28,
//       August: 0.3,
//       September: 0.28,
//       October: 0.3,
//       remark: 'Initial estimate for MEG',
//     },
//     {
//       id: 2,
//       Particulars: 'DEG Yield',
//       July: 0.11,
//       August: 0.12,
//       September: 0.28,
//       October: 0.3,
//       remark: 'Slight variation expected',
//     },
//     {
//       id: 3,
//       Particulars: 'TEG Yield',
//       July: 0.0063,
//       August: 0.0065,
//       September: 0.28,
//       October: 0.3,
//       remark: 'Stable value',
//     },
//     {
//       id: 4,
//       Particulars: 'TST',
//       July: 0.23,
//       August: 0.25,
//       September: 0.28,
//       October: 0.3,
//       remark: 'Projected increase',
//     },
//     {
//       id: 5,
//       Particulars: 'Total HP Steam Consumption',
//       July: 0.34,
//       August: 0.35,
//       September: 0.28,
//       October: 0.3,
//       remark: 'Consistent usage',
//     },
//     {
//       id: 6,
//       Particulars: 'Total MP Steam Consumption',
//       July: 0.3,
//       August: 0.31,
//       September: 0.28,
//       October: 0.3,
//       remark: 'Stable month-over-month',
//     },
//     {
//       id: 7,
//       Particulars: 'Total LP Steam Consumption',
//       July: 0.09,
//       August: 0.1,
//       September: 0.28,
//       October: 0.3,
//       remark: 'No change expected',
//     },
//     {
//       id: 8,
//       Particulars: 'Ethylenepurity',
//       July: 0.099,
//       August: 0.099,
//       September: 0.28,
//       October: 0.3,
//       remark: 'Maintained quality',
//     },
//     {
//       id: 9,
//       Particulars: 'Oxygenpurity',
//       July: 0.097,
//       August: 0.097,
//       September: 0.28,
//       October: 0.3,
//       remark: 'Controlled value',
//     },
//     {
//       id: 10,
//       Particulars: 'Ethylenelosses',
//       July: 0.101,
//       August: 0.101,
//       September: 0.28,
//       October: 0.3,
//       remark: 'No deviation',
//     },
//     {
//       id: 11,
//       Particulars: 'Oxygenlosses',
//       July: 0.101,
//       August: 0.055,
//       September: 0.28,
//       October: 0.3,
//       remark: 'Steady losses',
//     },
//     {
//       id: 12,
//       Particulars: 'HP Latent Heat',
//       July: 0.4,
//       August: 0.42,
//       September: 0.28,
//       October: 0.3,
//       remark: 'From previous config',
//     },
//   ]

//   const [configRows, setConfigRows] = useState(configDummyData)

//   const deleteRowData = async (paramsForDelete) => {
//     try {
//       const { idFromApi, id } = paramsForDelete
//       const deleteId = id

//       if (!idFromApi) {
//         setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
//       }

//       if (idFromApi) {
//         await DataService.deleteSlowdownData(idFromApi, keycloak)
//         setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
//         setSnackbarOpen(true)
//         setSnackbarData({
//           message: 'Record Deleted successfully!',
//           severity: 'success',
//         })
//         fetchData()
//       }
//     } catch (error) {
//       console.error('Error deleting Record!', error)
//     }
//   }

//   const getAdjustedPermissions = (permissions, isOldYear) => {
//     if (isOldYear != 1) return permissions
//     return {
//       ...permissions,
//       showAction: false,
//       addButton: false,
//       deleteButton: false,
//       editButton: false,
//       showUnit: false,
//       saveWithRemark: false,
//       saveBtn: false,
//       isOldYear: isOldYear,
//       allAction: false,
//     }
//   }

//   const adjustedPermissions = getAdjustedPermissions(
//     {
//       showAction: permissions?.showAction ?? true,
//       addButton: permissions?.addButton ?? true,
//       deleteButton: permissions?.deleteButton ?? true,
//       editButton: permissions?.editButton ?? false,
//       showUnit: permissions?.showUnit ?? false,
//       saveWithRemark: permissions?.saveWithRemark ?? true,
//       saveBtn: permissions?.saveBtn ?? true,
//       customHeight: permissions?.customHeight,
//       allAction: true,
//     },
//     isOldYear,
//   )

//   return (
//     <div>
//       <Backdrop
//         sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
//         open={!!loading}
//       >
//         <CircularProgress color='inherit' />
//       </Backdrop>

//       <Box sx={{ borderBottom: 1, borderColor: 'divider', marginBottom: 2 }}>
//         <Tabs value={selectedTab} onChange={handleTabChange}>
//           <Tab label='Slowdown Details' />
//           <Tab label='Configuration' />
//         </Tabs>
//       </Box>

//       {selectedTab === 0 && (
//         <KendoDataTables
//           modifiedCells={modifiedCells}
//           setModifiedCells={setModifiedCells}
//           setRows={setRows}
//           columns={colDefs}
//           rows={rows}
//           paginationOptions={[100, 200, 300]}
//           updateSlowdownData={updateSlowdownData}
//           saveChanges={saveChanges}
//           snackbarData={snackbarData}
//           snackbarOpen={snackbarOpen}
//           setSnackbarOpen={setSnackbarOpen}
//           setSnackbarData={setSnackbarData}
//           apiRef={apiRef}
//           deleteId={deleteId}
//           setDeleteId={setDeleteId}
//           setOpen1={setOpen1}
//           open1={open1}
//           fetchData={fetchData}
//           handleRemarkCellClick={handleRemarkCellClick}
//           remarkDialogOpen={remarkDialogOpen}
//           setRemarkDialogOpen={setRemarkDialogOpen}
//           currentRemark={currentRemark}
//           setCurrentRemark={setCurrentRemark}
//           currentRowId={currentRowId}
//           unsavedChangesRef={unsavedChangesRef}
//           deleteRowData={deleteRowData}
//           permissions={adjustedPermissions}
//           handleCancelClick={handleCancelClick}
//           focusFirstField={focusFirstField}
//           allProducts={allProducts}
//         />
//       )}

//       {selectedTab === 1 && (
//         <KendoDataTables
//           modifiedCells={modifiedCells}
//           setModifiedCells={setModifiedCells}
//           setRows={setConfigRows}
//           columns={colDefs2}
//           rows={configRows}
//           paginationOptions={[100, 200, 300]}
//           updateSlowdownData={updateSlowdownData}
//           saveChanges={saveChanges}
//           snackbarData={snackbarData}
//           snackbarOpen={snackbarOpen}
//           setSnackbarOpen={setSnackbarOpen}
//           setSnackbarData={setSnackbarData}
//           apiRef={apiRef}
//           deleteId={deleteId}
//           setDeleteId={setDeleteId}
//           setOpen1={setOpen1}
//           open1={open1}
//           fetchData={fetchData}
//           handleRemarkCellClick={handleRemarkCellClick}
//           remarkDialogOpen={remarkDialogOpen}
//           setRemarkDialogOpen={setRemarkDialogOpen}
//           currentRemark={currentRemark}
//           setCurrentRemark={setCurrentRemark}
//           currentRowId={currentRowId}
//           unsavedChangesRef={unsavedChangesRef}
//           deleteRowData={deleteRowData}
//           permissions={{ saveBtn: true, allAction: true }}
//           handleCancelClick={handleCancelClick}
//           focusFirstField={focusFirstField}
//           allProducts={allProducts}
//         />
//       )}
//     </div>
//   )
// }

// export default SlowDown
