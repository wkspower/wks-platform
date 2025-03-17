import React, { useState } from 'react'
import ASDataGrid from './ASDataGrid'
// import { useSession } from 'SessionStoreContext'
// import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'

import { generateHeaderNames } from 'components/Utilities/generateHeaders'
// import NumericInputOnly from 'utils/NumericInputOnly'
import getEnhancedColDefs from './CommonHeader/MaintainaceDetails_header'
const headerMap = generateHeaderNames()

// const productionColumns = [
//   {
//     field: 'description',
//     headerName: 'Description ( in Hrs )',
//     editable: true,
//     minWidth: 250,
//     maxWidth: 300,
//   },

//   {
//     field: 'april',
//     headerName: headerMap[4],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },
//   {
//     field: 'may',
//     headerName: headerMap[5],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },
//   {
//     field: 'june',
//     headerName: headerMap[6],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },
//   {
//     field: 'july',
//     headerName: headerMap[7],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },

//   {
//     field: 'august',
//     headerName: headerMap[8],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },
//   {
//     field: 'september',
//     headerName: headerMap[9],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },
//   {
//     field: 'october',
//     headerName: headerMap[10],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },
//   {
//     field: 'november',
//     headerName: headerMap[11],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },
//   {
//     field: 'december',
//     headerName: headerMap[12],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },
//   {
//     field: 'january',
//     headerName: headerMap[1],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },
//   {
//     field: 'february',
//     headerName: headerMap[2],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },
//   {
//     field: 'march',
//     headerName: headerMap[3],
//     editable: true,
//     type: 'number',
//     align: 'left',
//     headerAlign: 'left',
//   },

//   // { field: 'remark', headerName: 'Remark', minWidth : 150, editable: true },
// ]

const productionData = [
  {
    id: 1,
    description: 'Total available hrs for the month',
    april: 720,
    may: 720,
    june: 720,
    july: 720,
    august: 720,
    september: 720,
    october: 720,
    november: 720,
    december: 720,
    january: 720,
    february: 720,
    march: 720,
  },
  {
    id: 2,
    description: 'Shutdown',
    april: 50,
    may: 40,
    june: 60,
    july: 55,
    august: 45,
    september: 50,
    october: 52,
    november: 48,
    december: 49,
    january: 50,
    february: 53,
    march: 51,
  },
  {
    id: 3,
    description: 'Non-shutdown operating hrs for the month',
    april: 670,
    may: 680,
    june: 660,
    july: 665,
    august: 675,
    september: 670,
    october: 668,
    november: 672,
    december: 671,
    january: 670,
    february: 667,
    march: 669,
  },
  {
    id: 4,
    description: 'Average slowdown load, % of PVT',
    april: 85,
    may: 80,
    june: 90,
    july: 88,
    august: 84,
    september: 86,
    october: 85,
    november: 87,
    december: 89,
    january: 88,
    february: 84,
    march: 85,
  },
  {
    id: 5,
    description: 'Slowdown load reduction as shutdown hrs equivalent',
    april: 30,
    may: 35,
    june: 28,
    july: 32,
    august: 31,
    september: 33,
    october: 29,
    november: 30,
    december: 32,
    january: 31,
    february: 34,
    march: 33,
  },
  {
    id: 6,
    description: 'Effective operating hrs @ PVT',
    april: 640,
    may: 645,
    june: 632,
    july: 633,
    august: 644,
    september: 637,
    october: 639,
    november: 642,
    december: 639,
    january: 641,
    february: 633,
    march: 636,
  },
  {
    id: 7,
    description: 'Machine oiling task',
    april: 100,
    may: 105,
    june: 98,
    july: 110,
    august: 115,
    september: 120,
    october: 130,
    november: 135,
    december: 140,
    january: 145,
    february: 150,
    march: 155,
  },
  {
    id: 8,
    description: 'Gearbox maintenance check',
    april: 95,
    may: 100,
    june: 105,
    july: 110,
    august: 115,
    september: 120,
    october: 125,
    november: 130,
    december: 135,
    january: 140,
    february: 145,
    march: 150,
  },
  {
    id: 9,
    description: 'Safety equipment check',
    april: 90,
    may: 95,
    june: 100,
    july: 105,
    august: 110,
    september: 115,
    october: 120,
    november: 125,
    december: 130,
    january: 135,
    february: 140,
    march: 145,
  },
  {
    id: 10,
    description: 'Electrical panel audit',
    april: 135,
    may: 140,
    june: 145,
    july: 150,
    august: 155,
    september: 160,
    october: 165,
    november: 170,
    december: 175,
    january: 180,
    february: 185,
    march: 190,
  },
]

const MaintenanceTable = () => {
  // const dataGridStore = useSelector((state) => state.dataGridStore)
  // const { sitePlantChange } = dataGridStore
  const [open1, setOpen1] = useState(false)
  // const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()

  const [rows, setRows] = useState(productionData)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  // const keycloak = useSession()
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

  const productionColumns = getEnhancedColDefs({
    // allProducts,
    headerMap,
    // handleRemarkCellClick,
  })
  return (
    <div>
      <ASDataGrid
        setRows={setRows}
        columns={productionColumns}
        rows={rows}
        title='Maintenance Details'
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
        permissions={{
          showAction: false,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: false,
          saveWithRemark: true,
          saveBtn: false,
        }}
      />
    </div>
  )
}
export default MaintenanceTable
