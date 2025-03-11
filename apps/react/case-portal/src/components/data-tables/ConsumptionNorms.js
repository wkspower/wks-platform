import { DataService } from 'services/DataService'
import DataGridTable from './ASDataGrid'
import React, { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'

const NormalOpNormsScreen = () => {
  const keycloak = useSession()
  const [csData, setCsData] = useState([])
  const [csDataTransformed, setCsDataTransformed] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const headerMap = generateHeaderNames()
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
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
  const saveChanges = React.useCallback(async () => {
    console.log(
      'Edited Data: ',
      Object.values(unsavedChangesRef.current.unsavedRows),
    )
    setTimeout(async () => {
      try {
        // var data = Object.values(unsavedChangesRef.current.unsavedRows)
        // saveShutdownData(data)

        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
      } catch (error) {
        // setIsSaving(false);
      }
    }, 1000) // Delay of 2 seconds
  }, [apiRef])
  const fetchData = async () => {
    try {
      const data = await DataService.getConsumptionNormsData(keycloak)
      setCsData(data)

      let rowIndex = 1
      const groupedRows = []

      Object.entries(data).forEach(([Particulars, rows], index) => {
        groupedRows.push({
          id: `group-${index}`,
          Particulars: Particulars,
        })
        rows.forEach((row) => {
          groupedRows.push({
            ...row,
            id: row.NormParameterMonthlyTransactionId || `row-${rowIndex++}`,
          })
        })
      })

      setCsDataTransformed(groupedRows)
      setRows(groupedRows)
    } catch (error) {
      console.error('Error fetching data:', error)
    }
  }
  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
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
  }, [sitePlantChange, keycloak])

  const productionColumns = [
    {
      field: 'Particulars',
      headerName: 'Particulars',
      minWidth: 150,
      editable: false,
      renderCell: (params) => {
        const isGroupRow = params.row.id.startsWith('group-')
        return (
          <span style={{ fontWeight: isGroupRow ? 'bold' : 'normal' }}>
            {params.value}
          </span>
        )
      },
    },

    { field: 'TPH', headerName: 'Unit', width: 100, editable: false },

    {
      field: 'NormParametersId',
      headerName: 'Product Norm',
      editable: true,
      minWidth: 225,
      valueGetter: (params) => {
        return params || ''
      },
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params) => {
        const { value } = params
        return (
          <select
            value={value || ''}
            onChange={(event) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'NormParametersId',
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
      field: 'apr24',
      headerName: headerMap['apr'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'may24',
      headerName: headerMap['may'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jun24',
      headerName: headerMap['jun'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jul24',
      headerName: headerMap['jul'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'aug24',
      headerName: headerMap['aug'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'sep24',
      headerName: headerMap['sep'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'oct24',
      headerName: headerMap['oct'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'nov24',
      headerName: headerMap['nov'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'dec24',
      headerName: headerMap['dec'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jan25',
      headerName: headerMap['jan'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'feb25',
      headerName: headerMap['feb'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'mar25',
      headerName: headerMap['mar'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },

    { field: 'remark', headerName: 'Remark', editable: true },
  ]

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
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: true,
          saveWithRemark: true,
        }}
      />
    </div>
  )
}

export default NormalOpNormsScreen
