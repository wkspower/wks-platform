import Tooltip from '@mui/material/Tooltip'

import { useGridApiRef } from '@mui/x-data-grid'
import { useSession } from 'SessionStoreContext'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
// import DataGridTable from '../ASDataGrid'
// import { GridRowModes } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { DataService } from 'services/DataService'
// import NumericInputOnly from 'utils/NumericInputOnly'
import { truncateRemarks } from 'utils/remarksUtils'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
// import TextField from '@mui/material/TextField'
// import { useDispatch } from 'react-redux'
// import { setIsBlocked } from 'store/reducers/dataGridStore'
import KendoDataTables from './index'

const SlowdownNorms = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [loading, setLoading] = useState(false)
  const menu = useSelector((state) => state.dataGridStore)
  const [allProducts, setAllProducts] = useState([])

  const [slowdownMonths, setSlowdownMonths] = useState([])
  const { sitePlantChange, yearChanged, oldYear } = menu
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear

  const [open1, setOpen1] = useState(false)
  // const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  // const dispatch = useDispatch()
  const [rows, setRows] = useState([])
  // const [productNormData, setProductNormData] = useState([])
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [rowModesModel, setRowModesModel] = useState({}) // Track row edit state

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  // const getProductDisplayName = (id) => {
  //   if (!id) return
  //   const product = allProducts.find((p) => p.id === id)
  //   return product ? product.displayName : ''
  // }

  const keycloak = useSession()

  const saveChanges = React.useCallback(async () => {
    // const rowsInEditMode = Object.keys(rowModesModel).filter(
    //   (id) => rowModesModel[id]?.mode === 'edit',
    // )

    // rowsInEditMode.forEach((id) => {
    //   apiRef.current.stopRowEditMode({ id })
    // })

    setTimeout(() => {
      if (lowerVertName == 'meg') {
        try {
          var data = Object.values(modifiedCells)
          if (data.length == 0) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: 'No Records to Save!',
              severity: 'info',
            })
            setLoading(false)
            return
          }

          const requiredFields = ['remarks']
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

          saveSlowdownNormsData(data)
        } catch (error) {
          /* empty */
          setLoading(false)
        }
      }
      if (lowerVertName == 'pe') {
        try {
          var editedData = Object.values(modifiedCells)
          // var allRows = Array.from(apiRef.current.getRowModels().values())
          // allRows = allRows.filter((row) => !row.isGroupHeader)

          // const updatedRows = allRows.map(
          //   (row) => unsavedChangesRef.current.unsavedRows[row.id] || row,
          // )

          if (editedData.length === 0) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: 'No Records to Save!',
              severity: 'info',
            })
            return
          }

          const requiredFields = ['remarks']

          const validationMessage = validateFields(editedData, requiredFields)
          if (validationMessage) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: validationMessage,
              severity: 'error',
            })
            setLoading(false)
            return
          }

          if (calculatebtnClicked == false) {
            if (editedData.length === 0) {
              setSnackbarOpen(true)
              setSnackbarData({
                message: 'No Records to Save!',
                severity: 'info',
              })
              setCalculatebtnClicked(false)
              return
            }

            saveSlowdownNormsData(editedData)
          } else {
            saveSlowdownNormsData(modifiedCells)
          }
        } catch (error) {
          console.log('Error saving changes:', error)
          setLoading(false)
          setCalculatebtnClicked(false)
        }
      }
    }, 400)
  }, [apiRef, selectedUnit, calculatebtnClicked, modifiedCells])

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak, null)
        const productList = data.map((product) => ({
          id: product.id.toLowerCase(),
          displayName: product.displayName,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }
    const getSlowdownMonths = async () => {
      try {
        const data = await DataService.getSlowdownMonths(keycloak, null)
        if (data) setSlowdownMonths(data)
      } catch (error) {
        console.error('Error fetching months:', error)
      } finally {
        // handleMenuClose();
      }
    }
    fetchData()
    getAllProducts()
    getSlowdownMonths()
  }, [
    sitePlantChange,
    oldYear,
    yearChanged,
    keycloak,
    selectedUnit,
    lowerVertName,
  ])

  // const formatValueToFiveDecimals = (params) =>
  //   params ? parseFloat(params).toFixed(5) : ''

  const isCellEditable = (params) => {
    return params.row.isEditable
  }

  // const months = slowdownMonths

  const colDefs = [
    {
      field: 'Particulars',
      title: 'Type',
      width: 100,
      groupable: true,
      editable: false,
    },

    {
      field: 'materialFkId',
      title: 'Particulars',
      width: 150,
      editable: false,
    },

    { field: 'UOM', title: 'UOM', width: 100, editable: false },

    {
      field: 'april',
      title: headerMap[4],
      editable: slowdownMonths?.includes(4),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(4),
      columnClassName: 'first-column',
      width: 120,
    },

    {
      field: 'may',
      title: headerMap[5],
      editable: slowdownMonths?.includes(5),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(5),
      columnClassName: 'first-column',
      width: 120,
    },

    {
      field: 'june',
      title: headerMap[6],
      editable: slowdownMonths?.includes(6),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(6),
      width: 120,
    },
    {
      field: 'july',
      title: headerMap[7],
      editable: slowdownMonths?.includes(7),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(7),
      width: 120,
    },

    {
      field: 'august',
      title: headerMap[8],
      editable: slowdownMonths?.includes(8),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(8),
      width: 120,
    },
    {
      field: 'september',
      title: headerMap[9],
      editable: slowdownMonths?.includes(9),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(9),
      width: 120,
    },
    {
      field: 'october',
      title: headerMap[10],
      editable: slowdownMonths?.includes(10),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(10),
      width: 120,
    },
    {
      field: 'november',
      title: headerMap[11],
      editable: slowdownMonths?.includes(11),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(11),
      width: 120,
    },
    {
      field: 'december',
      title: headerMap[12],
      editable: slowdownMonths?.includes(12),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(12),
      width: 120,
    },
    {
      field: 'january',
      title: headerMap[1],
      editable: slowdownMonths?.includes(1),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(1),
      width: 120,
    },
    {
      field: 'february',
      title: headerMap[2],
      editable: slowdownMonths?.includes(2),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(2),
      width: 120,
    },
    {
      field: 'march',
      title: headerMap[3],
      editable: slowdownMonths?.includes(3),
      //renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      format: '{0:n5}',
      isDisabled: !slowdownMonths?.includes(3),
      width: 120,
    },

    // remarks
    {
      field: 'remarks',
      title: 'Remark',
      width: 180,
      editable: false,
      renderCell: (params) => {
        const displayText = truncateRemarks(params.value)
        const isEditable = !params.row.Particulars

        return (
          <Tooltip title={params.value || ''} arrow>
            <div
              style={{
                cursor: 'pointer',
                color: params.value ? 'inherit' : 'gray',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                width: ' 100%',
              }}
              onDoubleClick={() => handleRemarkCellClick(params.row)}
            >
              {displayText || (isEditable ? 'Click to add remark' : '')}
            </div>
          </Tooltip>
        )
      },
    },
    {
      field: 'idFromApi',
      title: 'idFromApi',
    },
    // {
    //   field: 'isEditable',
    //   title: 'isEditable',
    // },
  ]

  const handleRemarkCellClick = (row) => {
    if (!row?.isEditable) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    const updatedFields = []
    for (const key in newRow) {
      if (
        Object.prototype.hasOwnProperty.call(newRow, key) &&
        newRow[key] !== oldRow[key]
      ) {
        updatedFields.push(key)
      }
    }

    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    if (updatedFields.length > 0) {
      setModifiedCells((prevModifiedCells) => ({
        ...prevModifiedCells,
        [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
      }))
    }

    return newRow
  }, [])

  const saveSlowdownNormsData = async (newRows) => {
    setLoading(true)
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      const isTPH = selectedUnit == 'TPD'
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const businessData = newRows.map((row) => ({
        april: isTPH && row.april ? row.april * 24 : row.april || null,
        may: isTPH && row.may ? row.may * 24 : row.may || null,
        june: isTPH && row.june ? row.june * 24 : row.june || null,
        july: isTPH && row.july ? row.july * 24 : row.july || null,
        august: isTPH && row.august ? row.august * 24 : row.august || null,
        september:
          isTPH && row.september ? row.september * 24 : row.september || null,
        october: isTPH && row.october ? row.october * 24 : row.october || null,
        november:
          isTPH && row.november ? row.november * 24 : row.november || null,
        december:
          isTPH && row.december ? row.december * 24 : row.december || null,
        january: isTPH && row.january ? row.january * 24 : row.january || null,
        february:
          isTPH && row.february ? row.february * 24 : row.february || null,
        march: isTPH && row.march ? row.march * 24 : row.march || null,
        remark: row.remarks,
        remarks: row.remarks,
        financialYear: localStorage.getItem('year'),
        plantId: plantId,
        normParameterId: row.normParameterId,
        id: row.idFromApi || null,
        materialFkId: row.materialFkId || null,
        mcuVersion: row.mcuVersion || null,
        plantFkId: row.plantFkId || null,
        siteFkId: row.siteFkId || null,
        verticalFkId: row.verticalFkId || null,
        unit: row.unit || null,
        normParameterTypeId: row.normParameterTypeId || null,
      }))
      if (businessData.length > 0) {
        // console.log(title)

        const response = await DataService.saveSlowdownNormsData(
          plantId,
          businessData,
          keycloak,
        )
        // dispatch(setIsBlocked(true))

        setSnackbarOpen(true)
        setSnackbarData({
          message: `Shutdown Norms Saved Successfully!`,
          severity: 'success',
        })
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
        setModifiedCells({})

        setLoading(false)
        setCalculatebtnClicked(false)

        // fetchData()
        return response
      } else {
        setSnackbarOpen(true)
        setLoading(false)
        setSnackbarData({
          message: `Shutdown Norms not saved!`,
          severity: 'error',
        })
        setCalculatebtnClicked(false)
      }
    } catch (error) {
      console.error(`Error saving Shutdown Norms`, error)
      setLoading(false)
    } finally {
      fetchData()
      setCalculatebtnClicked(false)
      setLoading(false)
    }
  }

  const fetchData = async () => {
    try {
      setLoading(true)

      // Fetch data from API
      const data = await DataService.getSlowdownNormsData(keycloak)
      const isTPD = selectedUnit === 'TPD'

      const formattedData = data.map((item, index) => {
        const baseItem = {
          ...item,
          idFromApi: item.id,
          id: index,
          remarks: item?.remarks?.trim() || null,
          originalRemark: item?.remarks?.trim(),
          materialFkId: item?.materialFkId?.toLowerCase(),
          Particulars: item.normParameterTypeDisplayName || 'By Products',
          isEditable: true,
        }

        if (isTPD) {
          const months = [
            'april',
            'may',
            'june',
            'july',
            'august',
            'september',
            'october',
            'november',
            'december',
            'january',
            'february',
            'march',
          ]

          months.forEach((month) => {
            const value = item[month]
            baseItem[month] = value ? (value / 24).toFixed(2) : value || null
          })
        }

        return baseItem
      })

      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const handleCalculatePe = async () => {
    setCalculatebtnClicked(true)
    setLoading(true)
    try {
      const year = localStorage.getItem('year')
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      const data = await DataService.handleCalculateSlowdownNorms(
        plantId,
        year,
        keycloak,
      )

      if (data) {
        const groupedRows = []
        const groups = new Map()
        let groupId = 0

        data.forEach((item) => {
          const groupKey = item.normParameterTypeDisplayName

          if (!groups.has(groupKey)) {
            groups.set(groupKey, [])
            groupedRows.push({
              id: groupId++,
              Particulars: groupKey,
              //isGroupHeader: true,
            })
          }
          const formattedItem = {
            ...item,
            idFromApi: item.id,
            // NormParametersId: item.materialFkId.toLowerCase(),
            materialFkId: item?.materialFkId.toLowerCase(),
            id: groupId++,
            remarks: item?.remarks?.trim() || null,
          }
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Data refreshed successfully!',
            severity: 'success',
          })

          groups.get(groupKey).push(formattedItem)
          groupedRows.push(formattedItem)
        })

        setRows(groupedRows)
        // dispatch(setIsBlocked(true))
        setLoading(false)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
        setLoading(false)
      }

      return data
    } catch (error) {
      console.error('Error saving refresh data:', error)
      setLoading(false)
    }
  }

  const handleCalculate = () => {
    handleCalculatePe()
  }

  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
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
      showCalculate: false,
      // noColor: true,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      units: ['TPH', 'TPD'],
      saveWithRemark: false,
      saveBtn: true,
      showCalculate: lowerVertName == 'meg' ? false : false,
      allAction: true,
    },
    isOldYear,
  )
  const NormParameterIdCell = (props) => {
    const productId = props.dataItem.materialFkId
    const product = allProducts.find((p) => p.id === productId)
    const displayName = product?.displayName || ''
    // console.log(displayName)
    return <td>{displayName}</td>
  }

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
        NormParameterIdCell={NormParameterIdCell}
        setModifiedCells={setModifiedCells}
        isCellEditable={isCellEditable}
        title='Shutdown Norms'
        columns={colDefs}
        setRows={setRows}
        rows={rows}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        handleUnitChange={handleUnitChange}
        onRowModesModelChange={onRowModesModelChange}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        rowModesModel={rowModesModel}
        open1={open1}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        onProcessRowUpdateError={onProcessRowUpdateError}
        fetchData={fetchData}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        unsavedChangesRef={unsavedChangesRef}
        handleRemarkCellClick={handleRemarkCellClick}
        handleCalculate={handleCalculate}
        permissions={adjustedPermissions}
        groupBy='Particulars'

        // permissions={{
        //   showAction: false,
        //   addButton: false,
        //   deleteButton: false,
        //   editButton: false,
        //   showUnit: false,
        //   units: ['TPH', 'TPD'],
        //   saveWithRemark: false,
        //   saveBtn: true,
        //   showCalculate: lowerVertName == 'meg' ? false : false,
        // }}
      />
    </div>
  )
}

export default SlowdownNorms
