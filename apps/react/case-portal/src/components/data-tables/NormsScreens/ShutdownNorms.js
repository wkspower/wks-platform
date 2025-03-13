import React, { useState, useEffect } from 'react'
import DataGridTable from '../ASDataGrid'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
import { GridRowModes } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { DataService } from 'services/DataService'
const headerMap = generateHeaderNames()

const shutdownNormsColumns = [
  {
    field: 'srNo',
    headerName: 'Sr. No',
    minWidth: 50,
    maxWidth: 70,
    editable: false,
  },
  { field: 'particular', headerName: 'Particular', width: 150, editable: true },
  {
    field: 'unit',
    headerName: 'Unit',
    minWidth: 50,
    editable: true,
  },
  {
    field: 'norms',
    headerName: 'Norms',
    minWidth: 50,
    editable: true,
  },
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
]

const shutdownNormsData = [
  {
    id: 1,
    srNo: 1,
    normParametersFKId: '1020',
    particular: 'Equipment A',
    unit: 'Hours',
    norms: 10,
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
    id: 2,
    srNo: 2,
    normParametersFKId: '1030',
    particular: 'Equipment B',
    unit: 'Days',
    norms: 2,
    april: 1,
    may: 1,
    june: 2,
    july: 2,
    august: 3,
    september: 3,
    october: 4,
    november: 4,
    december: 5,
    january: 5,
    february: 6,
    march: 6,
  },
  {
    id: 3,
    srNo: 3,
    normParametersFKId: '1040',
    particular: 'Material C',
    unit: 'Kg',
    norms: 50,
    april: 25,
    may: 26,
    june: 27,
    july: 28,
    august: 29,
    september: 30,
    october: 31,
    november: 32,
    december: 33,
    january: 34,
    february: 35,
    march: 36,
  },
  {
    id: 4,
    srNo: 4,
    normParametersFKId: '1050',
    particular: 'Tool D',
    unit: 'Pcs',
    norms: 5,
    april: 2,
    may: 3,
    june: 3,
    july: 4,
    august: 4,
    september: 5,
    october: 5,
    november: 6,
    december: 6,
    january: 7,
    february: 7,
    march: 8,
  },
  {
    id: 5,
    srNo: 5,
    normParametersFKId: '1060',
    particular: 'Machine E',
    unit: 'Hours',
    norms: 20,
    april: 10,
    may: 12,
    june: 14,
    july: 16,
    august: 18,
    september: 20,
    october: 22,
    november: 24,
    december: 26,
    january: 28,
    february: 30,
    march: 32,
  },
  {
    id: 6,
    srNo: 6,
    normParametersFKId: '1070',
    particular: 'Component F',
    unit: 'Litres',
    norms: 15,
    april: 7,
    may: 8,
    june: 9,
    july: 10,
    august: 11,
    september: 12,
    october: 13,
    november: 14,
    december: 15,
    january: 16,
    february: 17,
    march: 18,
  },
  {
    id: 7,
    srNo: 7,
    normParametersFKId: '1080',
    particular: 'System G',
    unit: 'Units',
    norms: 3,
    april: 1,
    may: 2,
    june: 2,
    july: 3,
    august: 3,
    september: 4,
    october: 4,
    november: 5,
    december: 5,
    january: 6,
    february: 6,
    march: 7,
  },
  {
    id: 8,
    srNo: 8,
    normParametersFKId: '1090',
    particular: 'Gear H',
    unit: 'Sets',
    norms: 8,
    april: 4,
    may: 5,
    june: 5,
    july: 6,
    august: 6,
    september: 7,
    october: 7,
    november: 8,
    december: 8,
    january: 9,
    february: 9,
    march: 10,
  },
  {
    id: 9,
    srNo: 9,
    normParametersFKId: '1110',
    particular: 'Pump I',
    unit: 'Hours',
    norms: 12,
    april: 6,
    may: 7,
    june: 8,
    july: 9,
    august: 10,
    september: 11,
    october: 12,
    november: 13,
    december: 14,
    january: 15,
    february: 16,
    march: 17,
  },
  {
    id: 10,
    srNo: 10,
    normParametersFKId: '12320',
    particular: 'Sensor J',
    unit: 'Pcs',
    norms: 6,
    april: 3,
    may: 4,
    june: 4,
    july: 5,
    august: 5,
    september: 6,
    october: 6,
    november: 7,
    december: 7,
    january: 8,
    february: 8,
    march: 9,
  },
]

const ShutdownNorms = () => {
  const menu = useSelector((state) => state.menu)
  const [allProducts, setAllProducts] = useState([])
  const { sitePlantChange } = menu
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState([])
  const [productNormData, setProductNormData] = useState([])
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const keycloak = useSession()
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
      try {
        var data = Object.values(unsavedChangesRef.current.unsavedRows)
        editAOPMCCalculatedData(data)
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
      } catch (error) {
        // setIsSaving(false);
      }
    }, 1000)
  }, [apiRef, selectedUnit])

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        const productList = data.map((product) => ({
          id: product.id.toLowerCase(),
          displayName: product.displayName,
          name: product.name,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }

    getAllProducts()
    fetchData()
  }, [sitePlantChange, keycloak, selectedUnit])

  const editAOPMCCalculatedData = async (newRows) => {
    try {
      let plantId = ''
      const isTPH = selectedUnit == 'TPD'
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      let siteId = ''

      const storedSite = localStorage.getItem('selectedSite')
      if (storedSite) {
        const parsedSite = JSON.parse(storedSite)
        siteId = parsedSite.id
      }

      const aopmccCalculatedData = newRows.map((row) => ({
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

        aopStatus: row.aopStatus || 'draft',
        year: localStorage.getItem('year'),
        plant: plantId,
        plantFKId: plantId,
        site: siteId,
        material: 'EOE',
        normParametersFKId: row.normParametersFKId,
        id: row.idFromApi || null,
        avgTPH: findAvg('1', row) || null,
      }))

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
      return response
    } catch (error) {
      console.error('Error saving Shutdown Norms Data:', error)
    } finally {
      fetchData()
    }
  }

  const fetchData = async () => {
    try {
      // const data = await DataService.getAOPMCCalculatedData(keycloak)
      const data = shutdownNormsData

      const formattedData = data.map((item, index) => {
        const isTPD = selectedUnit == 'TPD'
        return {
          ...item,
          idFromApi: item?.id,
          normParametersFKId: item?.normParametersFKId,
          id: index,

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
      })
      setProductNormData(formattedData)
      setRows(formattedData)
    } catch (error) {
      console.error('Error fetching data:', error)
    }
  }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  return (
    <div>
      <DataGridTable
        columns={shutdownNormsColumns}
        rows={productNormData}
        setRows={setRows}
        title='Shutdown Norms'
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
        fetchData={fetchData}
        onProcessRowUpdateError={onProcessRowUpdateError}
        handleUnitChange={handleUnitChange}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: true,
          units: ['TPH', 'TPD'],
          saveWithRemark: false,
          saveBtn: true,
        }}
      />
    </div>
  )
}

export default ShutdownNorms
