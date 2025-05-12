import Tooltip from '@mui/material/Tooltip'

import { useGridApiRef } from '@mui/x-data-grid'
import { useSession } from 'SessionStoreContext'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import DataGridTable from '../ASDataGrid'
// import { GridRowModes } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { DataService } from 'services/DataService'
import NumericInputOnly from 'utils/NumericInputOnly'
import { truncateRemarks } from 'utils/remarksUtils'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
import TextField from '@mui/material/TextField'
import { useDispatch } from 'react-redux'
import { setIsBlocked } from 'store/reducers/dataGridStore'

const ShutdownNorms = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})

  const [loading, setLoading] = useState(false)
  const menu = useSelector((state) => state.dataGridStore)
  const [allProducts, setAllProducts] = useState([])
  const [shutdownMonths, setShutdownMonths] = useState([])
  const { sitePlantChange, yearChanged, oldYear } = menu
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear

  const [open1, setOpen1] = useState(false)
  // const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  const dispatch = useDispatch()
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

  const getProductDisplayName = (id) => {
    if (!id) return
    const product = allProducts.find((p) => p.id === id)
    return product ? product.displayName : ''
  }

  const keycloak = useSession()

  const saveChanges = React.useCallback(async () => {
    const rowsInEditMode = Object.keys(rowModesModel).filter(
      (id) => rowModesModel[id]?.mode === 'edit',
    )

    rowsInEditMode.forEach((id) => {
      apiRef.current.stopRowEditMode({ id })
    })

    setTimeout(() => {
      if (lowerVertName == 'meg') {
        try {
          var data = Object.values(unsavedChangesRef.current.unsavedRows)
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

          saveShutDownNormsData(data)
        } catch (error) {
          /* empty */
          setLoading(false)
        }
      }
      if (lowerVertName == 'pe') {
        try {
          var editedData = Object.values(unsavedChangesRef.current.unsavedRows)
          var allRows = Array.from(apiRef.current.getRowModels().values())
          allRows = allRows.filter((row) => !row.isGroupHeader)

          const updatedRows = allRows.map(
            (row) => unsavedChangesRef.current.unsavedRows[row.id] || row,
          )

          if (updatedRows.length === 0) {
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

            saveShutDownNormsData(editedData)
          } else {
            saveShutDownNormsData(updatedRows)
          }
        } catch (error) {
          console.log('Error saving changes:', error)
          setLoading(false)
          setCalculatebtnClicked(false)
        }
      }
    }, 400)
  }, [apiRef, selectedUnit, calculatebtnClicked, rowModesModel])

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
    const getShutdownMonths = async () => {
      try {
        const data = await DataService.getShutdownMonths(keycloak, null)
        setShutdownMonths(data)
        // console.log('setShutdownMonths', data)
      } catch (error) {
        console.error('Error fetching months:', error)
      } finally {
        // handleMenuClose();
      }
    }
    fetchData()
    getAllProducts()
    getShutdownMonths()
  }, [
    sitePlantChange,
    oldYear,
    yearChanged,
    keycloak,
    selectedUnit,
    lowerVertName,
  ])

  const formatValueToThreeDecimals = (params) =>
    params ? parseFloat(params).toFixed(3) : ''

  const isCellEditable = (params) => {
    return !params.row.Particulars
  }

  // const months = shutdownMonths

  const colDefs = [
    {
      field: 'Particulars',
      headerName: 'Type',
      minWidth: 140,
      groupable: true,
      renderCell: (params) => <strong>{params.value}</strong>,
    },

    {
      field: 'materialFkId',
      headerName: 'Particulars',
      minWidth: 160,
      editable: false,
      valueGetter: (params) => params || '',
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },

      filterOperators: [
        {
          label: 'contains',
          value: 'contains',
          getApplyFilterFn: (filterItem) => {
            if (!filterItem?.value) {
              return
            }
            return (rowId) => {
              const filterValue = filterItem.value.toLowerCase()
              if (filterValue) {
                const productName = getProductDisplayName(rowId)
                if (productName) {
                  return productName.toLowerCase().includes(filterValue)
                }
              }
              return true
            }
          },
          InputComponent: ({ item, applyValue, focusElementRef }) => (
            <TextField
              autoFocus
              inputRef={focusElementRef}
              size='small'
              label='Contains'
              value={item.value || ''}
              onChange={(event) =>
                applyValue({ ...item, value: event.target.value })
              }
              style={{ marginTop: '8px' }}
            />
          ),
        },
      ],

      renderEditCell: (params) => {
        const { value, id, api } = params
        return (
          <select
            value={value || ''}
            onChange={(event) => {
              api.setEditCellValue({
                id,
                field: 'materialFkId',
                value: event.target.value,
              })
            }}
            style={{
              width: '100%',
              padding: '5px',
              border: 'none',
              outline: 'none',
              background: 'transparent',
            }}
          >
            <option value='' disabled>
              Select
            </option>
            {allProducts.map((product) => (
              <option key={product.id} value={product.id}>
                {product.displayName}
              </option>
            ))}
          </select>
        )
      },
    },

    { field: 'UOM', headerName: 'UOM', width: 100, editable: false },

    {
      field: 'april',
      headerName: headerMap[4],
      editable: shutdownMonths?.includes(4),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(4),
      columnClassName: 'first-column',
    },

    {
      field: 'may',
      headerName: headerMap[5],
      editable: shutdownMonths?.includes(5),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(5),
      columnClassName: 'first-column',
    },

    {
      field: 'june',
      headerName: headerMap[6],
      editable: shutdownMonths?.includes(6),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(6),
    },
    {
      field: 'july',
      headerName: headerMap[7],
      editable: shutdownMonths?.includes(7),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(7),
    },

    {
      field: 'august',
      headerName: headerMap[8],
      editable: shutdownMonths?.includes(8),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(8),
    },
    {
      field: 'september',
      headerName: headerMap[9],
      editable: shutdownMonths?.includes(9),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(9),
    },
    {
      field: 'october',
      headerName: headerMap[10],
      editable: shutdownMonths?.includes(10),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(10),
    },
    {
      field: 'november',
      headerName: headerMap[11],
      editable: shutdownMonths?.includes(11),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(11),
    },
    {
      field: 'december',
      headerName: headerMap[12],
      editable: shutdownMonths?.includes(12),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(12),
    },
    {
      field: 'january',
      headerName: headerMap[1],
      editable: shutdownMonths?.includes(1),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(1),
    },
    {
      field: 'february',
      headerName: headerMap[2],
      editable: shutdownMonths?.includes(2),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(2),
    },
    {
      field: 'march',
      headerName: headerMap[3],
      editable: shutdownMonths?.includes(3),
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
      isDisabled: !shutdownMonths?.includes(3),
    },

    // remarks
    {
      field: 'remarks',
      headerName: 'Remark',
      minWidth: 150,
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
                maxWidth: 140,
              }}
              onClick={() => handleRemarkCellClick(params.row)}
            >
              {displayText || (isEditable ? 'Click to add remark' : '')}
            </div>
          </Tooltip>
        )
      },
    },
    {
      field: 'idFromApi',
      headerName: 'idFromApi',
    },
    // {
    //   field: 'isEditable',
    //   headerName: 'isEditable',
    // },
  ]

  const handleRemarkCellClick = (row) => {
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

  const saveShutDownNormsData = async (newRows) => {
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

        const response = await DataService.saveShutDownNormsData(
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
        setModifiedCells({})

        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }

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
      const data = await DataService.getShutdownNormsData(keycloak)

      // const customOrder = [
      //   'Raw Material',
      //   'By Products',
      //   'Cat Chem',
      //   'Utility Consumption',
      //   'Configuration',
      // ]

      // const data = data1.sort(
      //   (a, b) =>
      //     customOrder.indexOf(a.normParameterTypeDisplayName) -
      //     customOrder.indexOf(b.normParameterTypeDisplayName),
      // )

      const groupedRows = []
      const groups = new Map()
      let groupId = 0
      const isTPD = selectedUnit == 'TPD'

      data.forEach((item) => {
        const groupKey = item.normParameterTypeDisplayName || 'By Products'

        if (!groups.has(groupKey)) {
          groups.set(groupKey, [])
          groupedRows.push({
            id: groupId++,
            Particulars: groupKey,
            isGroupHeader: true,
          })
        }

        const formattedItem = {
          ...item,
          idFromApi: item.id,
          id: groupId++,
          remarks: item?.remarks?.trim() || null,
          originalRemark: item?.remarks?.trim(),
          materialFkId: item?.materialFkId.toLowerCase(),

          ...(isTPD && {
            april: item.april
              ? (item.april / 24).toFixed(2)
              : item.april || null,
            may: item.may ? (item.may / 24).toFixed(2) : item.may || null,
            june: item.june ? (item.june / 24).toFixed(2) : item.june || null,
            july: item.july ? (item.july / 24).toFixed(2) : item.july || null,
            august: item.august
              ? (item.august / 24).toFixed(2)
              : item.august || null,
            september: item.september
              ? (item.september / 24).toFixed(2)
              : item.september || null,
            october: item.october
              ? (item.october / 24).toFixed(2)
              : item.october || null,
            november: item.november
              ? (item.november / 24).toFixed(2)
              : item.november || null,
            december: item.december
              ? (item.december / 24).toFixed(2)
              : item.december || null,
            january: item.january
              ? (item.january / 24).toFixed(2)
              : item.january || null,
            february: item.february
              ? (item.february / 24).toFixed(2)
              : item.february || null,
            march: item.march
              ? (item.march / 24).toFixed(2)
              : item.march || null,
          }),
        }

        groups.get(groupKey).push(formattedItem)
        groupedRows.push(formattedItem)
      })

      // setBDData(groupedRows);
      setRows(groupedRows)
      setLoading(false)
    } catch (error) {
      setLoading(false)
      console.error('Error fetching data:', error)
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
      const data = await DataService.handleCalculateShutdownNorms(
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
              isGroupHeader: true,
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
      noColor: true,
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
      showCalculate: lowerVertName == 'meg' ? false : true,
      noColor: true,
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
      <DataGridTable
        modifiedCells={modifiedCells}
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
        // permissions={{
        //   showAction: false,
        //   addButton: false,
        //   deleteButton: false,
        //   editButton: false,
        //   showUnit: false,
        //   units: ['TPH', 'TPD'],
        //   saveWithRemark: false,
        //   saveBtn: true,
        //   showCalculate: lowerVertName == 'meg' ? false : true,
        // }}
        permissions={adjustedPermissions}
      />
    </div>
  )
}

export default ShutdownNorms
