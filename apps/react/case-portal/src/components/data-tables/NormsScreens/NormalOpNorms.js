import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'
import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import NumericInputOnly from 'utils/NumericInputOnly'
import DataGridTable from '../ASDataGrid'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
import { useDispatch } from 'react-redux'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import TextField from '@mui/material/TextField'

const NormalOpNormsScreen = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [allProducts, setAllProducts] = useState([])
  const [allRedCell, setAllRedCell] = useState([])
  // const [bdData, setBDData] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)

  // const { sitePlantChange } = menu
  const [open1, setOpen1] = useState(false)
  // const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  const [rowModesModel, setRowModesModel] = useState({})
  const [rows, setRows] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [loading, setLoading] = useState(false)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const dispatch = useDispatch()
  const headerMap = generateHeaderNames(localStorage.getItem('year'))

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
  const fetchData = async () => {
    setLoading(true)
    try {
      var data = []
      data = await DataService.getNormalOperationNormsData(keycloak)

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
          originalRemark: item.remarks,
          id: groupId++,
        }

        groups.get(groupKey).push(formattedItem)
        groupedRows.push(formattedItem)
      })

      // setBDData(groupedRows)
      setRows(groupedRows)
      setLoading(false)
    } catch (error) {
      setLoading(false)
      console.error('Error fetching Business Demand data:', error)
    }
  }

  const getNormTransactions = async () => {
    try {
      const res = await DataService.getNormTransactions(keycloak)
      if (res?.code == 200) {
        const normalized = res?.data.map((obj) => ({
          ...obj,
          normParameterFKId: obj.normParameterFKId.toUpperCase(),
        }))
        setAllRedCell(normalized)
      }

      // const isRedCellObject = [
      //   {
      //     normParameterFKId: '513104DB-94A5-4DCF-9B8F-FCB2CB897C9D',
      //     month: 4,
      //     value: 6.5,
      //   },
      // ]

      // setAllRedCell(isRedCellObject)
    } catch (error) {
      console.error('Error fetching data:', error)
    } finally {
      // handleMenuClose();
    }
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak, null)
        const productList = data.map((product) => ({
          id: product.id,
          displayName: product.displayName,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }
    setTimeout(() => {
      fetchData()
    }, 450)
    getAllProducts()
    getNormTransactions()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const formatValueToThreeDecimals = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(3) : ''
  }

  const colDefs = [
    {
      field: 'Particulars',
      headerName: 'Type',
      minWidth: 110,
      groupable: true,
      renderCell: (params) => (
        <div
          style={{
            whiteSpace: 'normal',
            wordBreak: 'break-word',
            lineHeight: 1.4,
          }}
        >
          <strong>{params.value}</strong>
        </div>
      ),
    },
    {
      field: 'materialFkId',
      // headerName: 'Particulars',
      headerName: lowerVertName === 'meg' ? 'Particulars' : 'Particulars',
      minWidth: 120,
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
        const { value, api } = params
        return (
          <select
            value={value || ''}
            onChange={(event) => {
              api.setEditCellValue({
                id: params.id,
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

    {
      field: 'UOM',
      headerName: 'UOM / MT',
      minWidth: 80,
      editable: false,
    },

    {
      field: 'april',
      headerName: headerMap[4],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'may',
      headerName: headerMap[5],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'june',
      headerName: headerMap[6],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'july',
      headerName: headerMap[7],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },

    {
      field: 'august',
      headerName: headerMap[8],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'september',
      headerName: headerMap[9],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'october',
      headerName: headerMap[10],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'november',
      headerName: headerMap[11],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'december',
      headerName: headerMap[12],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'january',
      headerName: headerMap[1],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'february',
      headerName: headerMap[2],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'march',
      headerName: headerMap[3],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'remarks',
      headerName: 'Remark',
      minWidth: 125,
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
    {
      field: 'isEditable',
      headerName: 'isEditable',
    },
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

  const saveChanges = React.useCallback(async () => {
    const rowsInEditMode = Object.keys(rowModesModel).filter(
      (id) => rowModesModel[id]?.mode === 'edit',
    )

    rowsInEditMode.forEach((id) => {
      apiRef.current.stopRowEditMode({ id })
    })

    setTimeout(() => {
      try {
        var data = Object.values(unsavedChangesRef.current.unsavedRows)
        if (data.length == 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          return
        }

        const requiredFields = ['materialFkId', 'remarks']
        const validationMessage = validateFields(data, requiredFields)
        if (validationMessage) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: validationMessage,
            severity: 'error',
          })
          return
        }

        saveNormalOperationNormsData(data)
      } catch (error) {
        /* empty */
      }
    }, 400)
  }, [apiRef, rowModesModel])

  const saveNormalOperationNormsData = async (newRows) => {
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const businessData = newRows.map((row) => ({
        april: row.april || null,
        may: row.may || null,
        june: row.june || null,
        july: row.july || null,
        august: row.august || null,
        september: row.september || null,
        october: row.october || null,
        november: row.november || null,
        december: row.december || null,
        january: row.january || null,
        february: row.february || null,
        march: row.march || null,
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
        const response = await DataService.saveNormalOperationNormsData(
          plantId,
          businessData,
          keycloak,
        )

        // if (response.status === 200) {
        if (response) {
          dispatch(setIsBlocked(false))
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Normal Operations Norms Saved Successfully!`,
            severity: 'success',
          })
          setModifiedCells({})

          unsavedChangesRef.current = {
            unsavedRows: {},
            rowsBeforeChange: {},
          }
          fetchData()
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Normal Operations Norms not saved!`,
            severity: 'error',
          })
        }
        return response
      }
    } catch (error) {
      console.error(`Error saving Normal Operations Norms`, error)
    } finally {
      // fetchData()
    }
  }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

  const isCellEditable = (params) => {
    return params.row.isEditable
  }

  const handleCalculate = async () => {
    setLoading(true)
    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var plantId = plantId
      const data = await DataService.handleCalculateNormalOpsNorms34(
        plantId,
        year,
        keycloak,
      )

      if (data == 0 || data) {
        // dispatch(setIsBlocked(true))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        fetchData()
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
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      setLoading(false)
      console.error('Error!', error)
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
      showCalculate: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: true,
      saveBtn: true,
      showCalculate: lowerVertName == 'meg' ? true : false,
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
        title='Normal Operations Norms'
        columns={colDefs}
        setRows={setRows}
        rows={rows}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        rowModesModel={rowModesModel}
        onRowModesModelChange={onRowModesModelChange}
        saveChanges={saveChanges}
        isCellEditable={isCellEditable}
        snackbarData={snackbarData}
        handleCalculate={handleCalculate}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
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
        permissions={adjustedPermissions}
        allRedCell={allRedCell}

        // permissions={{
        //   showAction: false,
        //   addButton: false,
        //   deleteButton: false,
        //   editButton: true,
        //   showUnit: false,
        //   saveWithRemark: true,
        //   saveBtn: true,
        //   showCalculate: lowerVertName == 'meg' ? true : false,
        // }}
      />
    </div>
  )
}

export default NormalOpNormsScreen
