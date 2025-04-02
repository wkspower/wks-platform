import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import React, { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
// Import the catalyst options from the JSON file
// import catalystOptionsData from '../../assets/Catalyst.json'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import NumericInputOnly from 'utils/NumericInputOnly'
import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'

const headerMap = generateHeaderNames()

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'

const SelectivityData = (props) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const keycloak = useSession()
  // const [csData, setCsData] = useState([])
  // const [allProducts, setAllProducts] = useState([])
  // const [allCatalyst, setAllCatalyst] = useState([])

  const [loading, setLoading] = useState(false)

  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)

  // const [rows, setRows] = useState()
  // const [rows2, setRows2] = useState()

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  // States for the Remark Dialog
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [allProducts, setAllProducts] = useState([])

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    props.setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    return newRow
  }, [])

  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(unsavedChangesRef.current.unsavedRows)
      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
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
        return
      }

      saveCatalystData(data)
    } catch (error) {
      // Handle error if necessary
    }
  }, [apiRef])

  const saveCatalystData = async (newRow) => {
    setLoading(true)

    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const turnAroundDetails = newRow.map((row) => ({
        apr: row.apr || null,
        may: row.may || null,
        jun: row.jun || null,
        jul: row.jul || null,
        aug: row.aug || null,
        sep: row.sep || null,
        oct: row.oct || null,
        nov: row.nov || null,
        dec: row.dec || null,
        jan: row.jan || null,
        feb: row.feb || null,
        mar: row.mar || null,
        UOM: '',
        auditYear: localStorage.getItem('year'),
        normParameterFKId: row.normParameterFKId,
        remarks: row.remarks,
        id: row.idFromApi || null,
      }))

      const response = await DataService.saveCatalystData(
        plantId,
        turnAroundDetails,
        keycloak,
      )

      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Configuration data Saved Successfully!',
          severity: 'success',
        })
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
        setLoading(false)

        props.fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving Configuration data:', error)
      setLoading(false)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }

  const handleDeleteClick = async (id, params) => {
    try {
      const maintenanceId =
        id?.maintenanceId ||
        params?.row?.idFromApi ||
        params?.row?.maintenanceId ||
        params?.NormParameterMonthlyTransactionId

      // console.log(maintenanceId, params, id)

      // Ensure UI state updates before the deletion process
      setOpen1(true)
      setDeleteId(id)

      // Perform the delete operation
      return await DataService.deleteBusinessDemandData(maintenanceId, keycloak)
    } catch (error) {
      console.error(`Error deleting Configuration data:`, error)
    } finally {
      props.fetchData()
    }
  }
  // const fetchData = async () => {
  //   setLoading(true)
  //   try {
  //     const data = await DataService.getCatalystSelectivityData(keycloak)
  //     console.log(data)
  //     if (lowerVertName === 'meg') {
  //       // For 'meg', simply map items without grouping.
  //       const formattedData = data.map((item, index) => ({
  //         ...item,
  //         idFromApi: item.id,
  //         id: index,
  //       }))
  //       setRows(formattedData)
  //     } else {
  //       // Create a nested grouping: first by lossCategory then by normType.
  //       const groups = new Map()

  //       data.forEach((item) => {
  //         const lossCategory = item.lossCategory
  //         const normType = item.normType

  //         if (!groups.has(lossCategory)) {
  //           groups.set(lossCategory, new Map())
  //         }
  //         const normGroup = groups.get(lossCategory)
  //         if (!normGroup.has(normType)) {
  //           normGroup.set(normType, [])
  //         }
  //         normGroup.get(normType).push(item)
  //       })

  //       let groupId = 0
  //       const groupedRows = []
  //       const shutdownRows = []

  //       // Build the final grouped arrays.
  //       groups.forEach((normGroup, lossCategory) => {
  //         if (lossCategory.toLowerCase() === 'shutdownnorms') {
  //           // Add shutdown norms to the shutdownRows array.
  //           shutdownRows.push({
  //             id: groupId++,
  //             Particulars: lossCategory,
  //             isGroupHeader: true,
  //           })
  //           normGroup.forEach((items, normType) => {
  //             shutdownRows.push({
  //               id: groupId++,
  //               Particulars2: normType,
  //               isSubGroupHeader: true,
  //             })
  //             items.forEach((item) => {
  //               shutdownRows.push({
  //                 ...item,
  //                 idFromApi: item.id,
  //                 id: groupId++,
  //               })
  //             })
  //           })
  //         } else {
  //           // Add all other items to the groupedRows array.
  //           groupedRows.push({
  //             id: groupId++,
  //             Particulars: lossCategory,
  //             isGroupHeader: true,
  //           })
  //           normGroup.forEach((items, normType) => {
  //             groupedRows.push({
  //               id: groupId++,
  //               Particulars2: normType,
  //               isSubGroupHeader: true,
  //             })
  //             items.forEach((item) => {
  //               groupedRows.push({
  //                 ...item,
  //                 idFromApi: item.id,
  //                 id: groupId++,
  //               })
  //             })
  //           })
  //         }
  //       })

  //       console.log('groupedRows:', groupedRows)
  //       console.log('shutdownRows:', shutdownRows)
  //       setRows(groupedRows)
  //       setRows2(shutdownRows)

  //       // If needed, you can store shutdownRows separately, e.g.:
  //       // setShutdownRows(shutdownRows);
  //     }
  //     setLoading(false)
  //   } catch (error) {
  //     console.error('Error fetching data:', error)
  //     setLoading(false)
  //   }
  // }

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

    getAllProducts()
    // getAllCatalyst()
    props.fetchData()
  }, [sitePlantChange, keycloak, lowerVertName])

  // Use catalyst options from the JSON file
  // const productOptions = catalystOptionsData.catalystOptions
  const productionColumns = [
    {
      field: 'normParameterFKId',
      headerName: 'Particulars',
      editable: false,
      minWidth: 160,
      valueGetter: (params) => params || '',
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params) => {
        const { value, id, api } = params

        const existingValues = new Set(
          [...api.getRowModels().values()]
            .filter((row) => row.id !== id)
            .map((row) => row.normParameterFKId),
        )

        return (
          <select
            value={value || ''}
            onChange={(event) => {
              api.setEditCellValue({
                id: params.id,
                field: 'normParameterFKId',
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
            {allProducts
              .filter((product) => !existingValues.has(product.id))
              .map((product) => (
                <option key={product.id} value={product.id}>
                  {product.displayName}
                </option>
              ))}
          </select>
        )
      },
    },

    // {
    //   field: 'NormParameterFKId',
    //   headerName: 'NormParameterFKId',
    //   // editable: true,
    //   minWidth: 250,
    // },
    {
      field: 'UOM',
      headerName: 'UOM',
      editable: false,
      align: 'left',
      headerAlign: 'left',
      // valueGetter: convertUnits,
    },
    {
      field: 'apr',
      headerName: headerMap[4],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      // valueGetter: convertUnits,
    },

    {
      field: 'may',
      headerName: headerMap[5],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jun',
      headerName: headerMap[6],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jul',
      headerName: headerMap[7],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'aug',
      headerName: headerMap[8],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'sep',
      headerName: headerMap[9],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'oct',
      headerName: headerMap[10],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'nov',
      headerName: headerMap[11],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'dec',
      headerName: headerMap[12],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jan',
      headerName: headerMap[1],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'feb',
      headerName: headerMap[2],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'mar',
      headerName: headerMap[3],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'remarks',
      headerName: 'Remark',
      minWidth: 150,
      editable: true,
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
  ]
  const productionColumnsPE1 = [
    {
      field: 'Particulars',
      headerName: 'Type',
      minWidth: 125,
      groupable: true,
      renderCell: (params) => <strong>{params.value}</strong>,
    },
    // {
    //   field: 'Particulars2',
    //   headerName: 'Sub-Type',
    //   minWidth: 125,
    //   groupable: true,
    //   headerClass: 'bold-header',
    // },
    {
      field: 'normParameterFKId',
      headerName: 'Particulars',
      editable: false,
      minWidth: 160,
      renderCell: (params) => {
        if (
          params.value === 'By Products' ||
          params.value === 'Cat Chem' ||
          params.value === 'Utility Consumption' ||
          params.value === 'Raw Material'
        ) {
          return <strong>{params.value}</strong>
        } else {
          const product = allProducts.find((p) => p.id === params.value)
          return product ? product.displayName : params.value
        }
      },

      valueGetter: (params) => {
        // console.log('valueGetter params:', params)
        return params ?? ''
      },
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : params?.Particulars2
      },
      renderEditCell: (params) => {
        const { value, id, api } = params

        const existingValues = new Set(
          [...api.getRowModels().values()]
            .filter((row) => row.id !== id)
            .map((row) => row.normParameterFKId || row.Particulars2),
        )

        return (
          <select
            value={value || ''}
            onChange={(event) => {
              api.setEditCellValue({
                id: params.id,
                field: 'normParameterFKId',
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
            {allProducts
              .filter((product) => !existingValues.has(product.id))
              .map((product) => (
                <option key={product.id} value={product.id}>
                  {product.displayName}
                </option>
              ))}
          </select>
        )
      },
    },

    // {
    //   field: 'NormParameterFKId',
    //   headerName: 'NormParameterFKId',
    //   // editable: true,
    //   minWidth: 250,
    // },
    {
      field: 'UOM',
      headerName: 'UOM',
      editable: false,
      align: 'left',
      headerAlign: 'left',
      // valueGetter: convertUnits,
    },
    {
      field: 'apr',
      headerName: headerMap[4],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      // valueGetter: convertUnits,
    },

    {
      field: 'may',
      headerName: headerMap[5],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jun',
      headerName: headerMap[6],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jul',
      headerName: headerMap[7],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'aug',
      headerName: headerMap[8],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'sep',
      headerName: headerMap[9],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'oct',
      headerName: headerMap[10],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'nov',
      headerName: headerMap[11],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'dec',
      headerName: headerMap[12],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jan',
      headerName: headerMap[1],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'feb',
      headerName: headerMap[2],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'mar',
      headerName: headerMap[3],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'remarks',
      headerName: 'Remark',
      minWidth: 110,
      editable: true,
      renderCell: (params) => {
        const displayText = truncateRemarks(params.value)
        const isEditable = !(
          params.row.Particulars || params.row.isSubGroupHeader
        )
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
  ]
  const productionColumnsPE2 = [
    {
      field: 'Particulars',
      headerName: 'Constant',
      minWidth: 125,
      groupable: true,
      flex: 1,
      renderCell: (params) => <strong>{params.value}</strong>,
    },
    {
      field: 'Particulars2',
      headerName: 'Type',
      minWidth: 125,
      groupable: true,
      flex: 1,
      renderCell: (params) => <strong>{params.value}</strong>,
    },
    {
      field: 'normParameterFKId',
      headerName: 'Particulars',
      editable: false,
      minWidth: 160,
      valueGetter: (params) => params || '',
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params) => {
        const { value, id, api } = params

        const existingValues = new Set(
          [...api.getRowModels().values()]
            .filter((row) => row.id !== id)
            .map((row) => row.normParameterFKId),
        )

        return (
          <select
            value={value || ''}
            onChange={(event) => {
              api.setEditCellValue({
                id: params.id,
                field: 'normParameterFKId',
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
            {allProducts
              .filter((product) => !existingValues.has(product.id))
              .map((product) => (
                <option key={product.id} value={product.id}>
                  {product.displayName}
                </option>
              ))}
          </select>
        )
      },
    },

    // {
    //   field: 'NormParameterFKId',
    //   headerName: 'NormParameterFKId',
    //   // editable: true,
    //   minWidth: 250,
    // },
    {
      field: 'apr',
      headerName: 'Values',
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      // valueGetter: NumericInputOnly || 0,
    },

    {
      field: 'remarks',
      headerName: 'Remark',
      minWidth: 250,
      editable: true,
      renderCell: (params) => {
        const displayText = truncateRemarks(params.value)
        const isEditable = !params.row.Particulars && !params.row.Particulars2

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
  ]

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <ASDataGrid
        columns={
          lowerVertName === 'meg'
            ? productionColumns
            : props?.tabIndex === 1
              ? productionColumnsPE2
              : productionColumnsPE1
        }
        rows={props?.rows}
        setRows={props?.setRows}
        title='Configuration'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        setDeleteId={setDeleteId}
        // fetchData={props?.fetchData}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        deleteId={deleteId}
        open1={open1}
        handleDeleteClick={handleDeleteClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        unsavedChangesRef={unsavedChangesRef}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: false,
          saveWithRemark: true,
          saveBtn: true,
          customHeight:
            lowerVertName === 'meg' ? undefined : props.defaultCustomHeight,
        }}
      />
    </div>
  )
}

export default SelectivityData
