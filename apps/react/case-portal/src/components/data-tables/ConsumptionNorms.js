import { DataService } from 'services/DataService'
import DataGridTable from './ASDataGrid'
import React, { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
import getEnhancedColDefs from './CommonHeader/consumptionHeader'

const NormalOpNormsScreen = () => {
  const keycloak = useSession()
  // const [csData, setCsData] = useState([])
  // const [csDataTransformed, setCsDataTransformed] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const headerMap = generateHeaderNames()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange } = dataGridStore
  const [open1, setOpen1] = useState(false)
  // const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  // States for the Remark Dialog
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const hardcodedData = [
    {
      id: 'group-0',
      Particulars: 'Equipment A',
    },
    {
      NormParameterMonthlyTransactionId: '1',
      NormParametersId: '92E0AF06-9535-4B93-8998-E56A71354393',
      april: 5,
      may: 6,
      june: 7,
      july: 8,
      august: 9,
      september: 10,
      october: 11,
      november: 12,
      december: 13,
      january: 14,
      february: 15,
      march: 16,
    },
    {
      NormParameterMonthlyTransactionId: '2',
      NormParametersId: '00DC05B1-9607-470E-A159-62497E0123E2',
      april: 3,
      may: 4,
      june: 5,
      july: 6,
      august: 7,
      september: 8,
      october: 9,
      november: 10,
      december: 11,
      january: 12,
      february: 13,
      march: 14,
    },
    {
      id: 'group-1',
      Particulars: 'Equipment B',
    },
    {
      NormParameterMonthlyTransactionId: '3',
      NormParametersId: 'A061E050-0281-421F-81C1-B136CE2ED3F3',
      april: 2,
      may: 3,
      june: 4,
      july: 5,
      august: 6,
      september: 7,
      october: 8,
      november: 9,
      december: 10,
      january: 11,
      february: 12,
      march: 13,
    },
    {
      NormParameterMonthlyTransactionId: '4',
      NormParametersId: '00DC05B1-9607-470E-A159-62497E0123E2',
      april: 1,
      may: 2,
      june: 3,
      july: 4,
      august: 5,
      september: 6,
      october: 7,
      november: 8,
      december: 9,
      january: 10,
      february: 11,
      march: 12,
    },
  ]

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    // Keep track of original values before editing
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    // setHasUnsavedRows(true)
    return newRow
  }, [])

  const saveEditedData = async () => {
    try {
      // let plantId = ''
      // const isTPH = selectedUnit == 'TPD'
      // const storedPlant = localStorage.getItem('selectedPlant')
      // if (storedPlant) {
      //   // const parsedPlant = JSON.parse(storedPlant)
      //   // plantId = parsedPlant.id
      // }

      // let siteId = ''

      const storedSite = localStorage.getItem('selectedSite')
      if (storedSite) {
        // const parsedSite = JSON.parse(storedSite)
        // siteId = parsedSite.id
      }

      // const aopmccCalculatedData = newRows.map((row) => ({
      //   april: isTPH && row.april ? row.april * 24 : row.april || null,
      //   may: isTPH && row.may ? row.may * 24 : row.may || null,
      //   june: isTPH && row.june ? row.june * 24 : row.june || null,
      //   july: isTPH && row.july ? row.july * 24 : row.july || null,
      //   august: isTPH && row.august ? row.august * 24 : row.august || null,
      //   september:
      //     isTPH && row.september ? row.september * 24 : row.september || null,
      //   october: isTPH && row.october ? row.october * 24 : row.october || null,
      //   november:
      //     isTPH && row.november ? row.november * 24 : row.november || null,
      //   december:
      //     isTPH && row.december ? row.december * 24 : row.december || null,
      //   january: isTPH && row.january ? row.january * 24 : row.january || null,
      //   february:
      //     isTPH && row.february ? row.february * 24 : row.february || null,
      //   march: isTPH && row.march ? row.march * 24 : row.march || null,

      //   aopStatus: row.aopStatus || 'draft',
      //   year: localStorage.getItem('year'),
      //   plant: plantId,
      //   plantFKId: plantId,
      //   site: siteId,
      //   material: 'EOE',
      //   normParametersFKId: row.normParametersFKId,
      //   id: row.idFromApi || null,
      //   avgTPH: findAvg('1', row) || null,
      // }))

      // const response = await DataService.editAOPMCCalculatedData(
      //   plantId,
      //   aopmccCalculatedData,
      //   keycloak,
      // )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Shutdown Norms Data Saved Successfully!',
        severity: 'success',
      })
      // fetchData()
      // return response
    } catch (error) {
      console.error('Error saving Shutdown Norms Data:', error)
    } finally {
      fetchData()
    }
  }

  const saveChanges = React.useCallback(async () => {
    setTimeout(async () => {
      try {
        var data = Object.values(unsavedChangesRef.current.unsavedRows)
        console.log('data', data)
        saveEditedData(data)
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
      } catch (error) {
        // setIsSaving(false);
      }
    }, 1000)
  }, [apiRef, selectedUnit])

  const fetchData = async () => {
    try {
      // const data = await DataService.getConsumptionNormsData(keycloak)
      const data = hardcodedData
      // setCsData(data)

      let rowIndex = 1
      const groupedRows = []
      const isTPD = selectedUnit === 'TPD'

      // Iterate through the data
      data.forEach((item, index) => {
        if (item.Particulars) {
          // If it's a group row (has 'Particulars' field)
          groupedRows.push({
            id: `group-${index}`,
            Particulars: item.Particulars,
          })
        } else {
          // Apply month division logic if selectedUnit is 'TPD'
          const formattedItem = {
            ...item,
            id: item.NormParameterMonthlyTransactionId || `row-${rowIndex++}`,
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
          groupedRows.push(formattedItem)
        }
      })

      // setCsDataTransformed(groupedRows)
      setRows(groupedRows)
    } catch (error) {
      console.error('Error fetching data:', error)
    }
  }

  useEffect(() => {

  const storedPlant = localStorage.getItem('selectedPlant')
        const parsedPlant = JSON.parse(storedPlant)
      
      const getAllProducts = async () => {
        try {
          const data = await DataService.getAllProducts(
            plantId= parsedPlant.id,
            keycloak, 'Consumption')
        const productList = data.map((product) => ({
          id: product.id,
          displayName: product.displayName,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching products:', error)
      }
    }

    getAllProducts()
    fetchData()
  }, [sitePlantChange, keycloak, selectedUnit])

  // const productionColumns = [
  //   {
  //     field: 'Particulars',
  //     headerName: 'Particulars',
  //     minWidth: 150,
  //     editable: false,
  //     renderCell: (params) => {
  //       const isGroupRow = params.row.id.startsWith('group-')
  //       return (
  //         <span style={{ fontWeight: isGroupRow ? 'bold' : 'normal' }}>
  //           {params.value}
  //         </span>
  //       )
  //     },
  //   },

  //   { field: 'TPH', headerName: 'Unit', width: 100, editable: false },

  //   {
  //     field: 'NormParametersId',
  //     headerName: 'Product Norm',
  //     editable: true,
  //     minWidth: 225,
  //     valueGetter: (params) => {
  //       return params || ''
  //     },
  //     valueFormatter: (params) => {
  //       const product = allProducts.find((p) => p.id === params)
  //       return product ? product.displayName : ''
  //     },
  //     renderEditCell: (params) => {
  //       const { value } = params
  //       return (
  //         <select
  //           value={value || ''}
  //           onChange={(event) => {
  //             params.api.setEditCellValue({
  //               id: params.id,
  //               field: 'NormParametersId',
  //               value: event.target.value,
  //             })
  //           }}
  //           style={{
  //             width: '100%',
  //             padding: '5px',
  //             border: 'none',
  //             outline: 'none',
  //             background: 'transparent',
  //           }}
  //         >
  //           {allProducts.map((product) => (
  //             <option key={product.id} value={product.id}>
  //               {product.displayName}
  //             </option>
  //           ))}
  //         </select>
  //       )
  //     },
  //   },

  //   {
  //     field: 'apr24',
  //     headerName: headerMap['apr'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'may24',
  //     headerName: headerMap['may'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'jun24',
  //     headerName: headerMap['jun'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'jul24',
  //     headerName: headerMap['jul'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'aug24',
  //     headerName: headerMap['aug'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'sep24',
  //     headerName: headerMap['sep'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'oct24',
  //     headerName: headerMap['oct'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'nov24',
  //     headerName: headerMap['nov'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'dec24',
  //     headerName: headerMap['dec'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'jan25',
  //     headerName: headerMap['jan'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'feb25',
  //     headerName: headerMap['feb'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'mar25',
  //     headerName: headerMap['mar'],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },

  //   {
  //     field: 'remark',
  //     headerName: 'Remark',
  //     editable: true,
  //     renderCell: (params) => {
  //       return (
  //         <div
  //           style={{
  //             cursor: 'pointer',
  //             color: params.value ? 'inherit' : 'gray',
  //           }}
  //           onClick={() => handleRemarkCellClick(params.row)}
  //         >
  //           {params.value || 'Click to add remark'}
  //         </div>
  //       )
  //     },
  //   },
  // ]
  const productionColumns = getEnhancedColDefs({
    allProducts,
    headerMap,
    handleRemarkCellClick,
  })

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  return (
    <div>
      <DataGridTable
        columns={productionColumns}
        rows={rows}
        setRows={setRows}
        getRowId={(row) => row.id}
        title='Consumption AOP'
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        // deleteId={deleteId}
        open1={open1}
        // setDeleteId={setDeleteId}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        // handleDeleteClick={handleDeleteClick}
        fetchData={fetchData}
        onProcessRowUpdateError={onProcessRowUpdateError}
        handleUnitChange={handleUnitChange}
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
          showUnit: true,
          units: ['TPH', 'TPD'],
          saveWithRemark: true,
          saveBtn: true,
          showCalculate: true,
        }}
      />
    </div>
  )
}

export default NormalOpNormsScreen
