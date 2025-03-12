import { useSession } from 'SessionStoreContext'
import DataGridTable from '../ASDataGrid'
import React, { useState } from 'react'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
import { GridRowModes } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
const headerMap = generateHeaderNames()

// Define columns as usual

// Sample production data (10 rows)
const productionData = [
  {
    id: 1,
    srNo: 1,
    particulars: 'Material A',
    unit: 'Kg',
    april: 50,
    may: 60,
    june: 55,
    july: 70,
    august: 65,
    september: 75,
    october: 80,
    november: 85,
    december: 90,
    janurary: 95,
    february: 85,
    march: 100,
    remark: 'Stock Updated',
  },
  {
    id: 2,
    srNo: 2,
    particulars: 'Material B',
    unit: 'Litre',
    april: 30,
    may: 40,
    june: 35,
    july: 45,
    august: 50,
    september: 55,
    october: 60,
    november: 65,
    december: 70,
    janurary: 75,
    february: 65,
    march: 80,
    remark: 'Reorder Needed',
  },
  {
    id: 3,
    srNo: 3,
    particulars: 'Material C',
    unit: 'Pcs',
    april: 100,
    may: 120,
    june: 110,
    july: 130,
    august: 125,
    september: 135,
    october: 140,
    november: 145,
    december: 150,
    janurary: 155,
    february: 145,
    march: 160,
    remark: 'Sufficient Stock',
  },
  {
    id: 4,
    srNo: 4,
    particulars: 'Material D',
    unit: 'Kg',
    april: 20,
    may: 25,
    june: 30,
    july: 35,
    august: 30,
    september: 40,
    october: 45,
    november: 50,
    december: 55,
    janurary: 60,
    february: 50,
    march: 65,
    remark: 'Check Expiry',
  },
  {
    id: 5,
    srNo: 5,
    particulars: 'Material E',
    unit: 'Box',
    april: 5,
    may: 10,
    june: 15,
    july: 20,
    august: 25,
    september: 30,
    october: 35,
    november: 40,
    december: 45,
    janurary: 50,
    february: 40,
    march: 55,
    remark: 'New Shipment Arrived',
  },
  {
    id: 6,
    srNo: 6,
    particulars: 'Material F',
    unit: 'Tonne',
    april: 15,
    may: 20,
    june: 18,
    july: 25,
    august: 22,
    september: 28,
    october: 30,
    november: 32,
    december: 35,
    janurary: 38,
    february: 34,
    march: 40,
    remark: 'Monitor Usage',
  },
  {
    id: 7,
    srNo: 7,
    particulars: 'Material G',
    unit: 'Meter',
    april: 200,
    may: 220,
    june: 210,
    july: 250,
    august: 230,
    september: 270,
    october: 280,
    november: 290,
    december: 300,
    janurary: 310,
    february: 290,
    march: 320,
    remark: 'Stable Supply',
  },
  {
    id: 8,
    srNo: 8,
    particulars: 'Material H',
    unit: 'Kg',
    april: 12,
    may: 18,
    june: 16,
    july: 20,
    august: 22,
    september: 24,
    october: 26,
    november: 28,
    december: 30,
    janurary: 32,
    february: 28,
    march: 35,
    remark: 'Low Demand',
  },
  {
    id: 9,
    srNo: 9,
    particulars: 'Material I',
    unit: 'Litre',
    april: 55,
    may: 60,
    june: 58,
    july: 70,
    august: 68,
    september: 75,
    october: 80,
    november: 85,
    december: 90,
    janurary: 95,
    february: 88,
    march: 100,
    remark: 'Reorder Soon',
  },
  {
    id: 10,
    srNo: 10,
    particulars: 'Material J',
    unit: 'Box',
    april: 8,
    may: 12,
    june: 10,
    july: 15,
    august: 14,
    september: 18,
    october: 20,
    november: 22,
    december: 24,
    janurary: 26,
    february: 23,
    march: 28,
    remark: 'New Variant Available',
  },
]

const NormalOpNormsScreen = () => {
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState(productionData)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  // States for the Remark Dialog
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const keycloak = useSession()
  const productionColumns = [
    {
      field: 'srNo',
      headerName: 'Sr. No',
      minWidth: 210,
      maxWidth: 200,
      editable: false,
      flex: 2,
    },
    {
      field: 'particulars',
      headerName: 'Particulars',
      minWidth: 150,
      maxWidth: 160,
      editable: true,
    },
    { field: 'unit', headerName: 'Unit', width: 100, editable: true },
    {
      field: 'april',
      headerName: headerMap['apr'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'may',
      headerName: headerMap['may'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'june',
      headerName: headerMap['jun'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'july',
      headerName: headerMap['jul'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'august',
      headerName: headerMap['aug'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'september',
      headerName: headerMap['sep'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'october',
      headerName: headerMap['oct'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'november',
      headerName: headerMap['nov'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'december',
      headerName: headerMap['dec'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'january',
      headerName: headerMap['jan'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'february',
      headerName: headerMap['feb'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'march',
      headerName: headerMap['mar'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'remark',
      headerName: 'Remark',
      minWidth: 180,
      maxWidth: 200,
      editable: true,
      renderCell: (params) => {
        return (
          <div
            style={{
              cursor: 'pointer',
              color: params.value ? 'inherit' : 'gray',
            }}
            onClick={() => handleRemarkCellClick(params.row)}
          >
            {params.value || 'Click to add remark'}
          </div>
        )
      },
    },
  ]
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    return newRow
  }, [])

  const saveChanges = React.useCallback(async () => {
    setTimeout(() => {
      console.log(
        'Edited Data: ',
        Object.values(unsavedChangesRef.current.unsavedRows),
      )
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
    }, 1000)
  }, [apiRef])
  // Create groups by inserting a row with a groupHeader property
  const rawMaterialsData = productionData.slice(0, 2) // 2 rows for Raw Materials
  const byProductsData = productionData.slice(2, 5) // 3 rows for By Products
  const calChemData = productionData.slice(5, 9) // 1 row for Cal-chem

  const groupedRows = [
    { id: 'group-raw', groupHeader: 'Raw Materials' },
    ...rawMaterialsData,
    { id: 'group-by', groupHeader: 'By Products' },
    ...byProductsData,
    { id: 'group-cal', groupHeader: 'Cat-chem' },
    ...calChemData,
  ]

  // Custom render function for cells
  const groupRenderCell = (params) => {
    if (params.row.groupHeader) {
      // In the first column show the group title
      if (params.field === 'srNo') {
        return (
          <span
            style={{
              fontWeight: 'bold',
              padding: '4px 8px',
            }}
          >
            {params.row.groupHeader}
          </span>
        )
      }
      // For other columns, render empty
      return ''
    }
    return params.value
  }

  // Enhance columns to use the custom render function
  const enhancedColumns = productionColumns.map((col) => ({
    ...col,
    renderCell: groupRenderCell,
  }))

  return (
    <div>
      <DataGridTable
        columns={enhancedColumns}
        setRows={setRows}
        rows={groupedRows}
        title='Normal Operations Norms'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
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
        // fetchData={fetchData}
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
          showCalculate: false,
        }}
        getRowClassName={(params) =>
          params.row.groupHeader ? 'group-header-row' : ''
        }
        sx={{
          '& .group-header-row .MuiDataGrid-cell': {
            borderRight: 'none !important',
          },
          '& .MuiDataGrid-row.MuiDataGrid-row--firstVisible:nth-child(even) .MuiDataGrid-cell':
            {
              borderRight: 'none !important',
            },
        }}
      />
    </div>
  )
}

export default NormalOpNormsScreen
