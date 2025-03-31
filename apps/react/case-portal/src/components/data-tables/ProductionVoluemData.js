import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import ASDataGrid from './ASDataGrid'
import { useGridApiRef } from '@mui/x-data-grid'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedProductionColDefs from './CommonHeader/ProductionVolumeHeader'
const headerMap = generateHeaderNames()

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'

const ProductionvolumeData = () => {
  const keycloak = useSession()
  // const [productNormData, setProductNormData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [rows, setRows] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [loading, setLoading] = useState(false)

  // States for the Remark Dialog
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks ?? row.remark ?? '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const findAvg = (value, row) => {
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

    const values = months.map((month) => row[month] || 0)
    const sum = values.reduce((acc, val) => acc + val, 0)
    const avg = (sum / values.length).toFixed(2)

    return avg === '0.00' ? null : avg
  }

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
        april: isTPH && row.april ? row.april / 24 : row.april || null,
        may: isTPH && row.may ? row.may / 24 : row.may || null,
        june: isTPH && row.june ? row.june / 24 : row.june || null,
        july: isTPH && row.july ? row.july / 24 : row.july || null,
        august: isTPH && row.august ? row.august / 24 : row.august || null,
        september:
          isTPH && row.september ? row.september / 24 : row.september || null,
        october: isTPH && row.october ? row.october / 24 : row.october || null,
        november:
          isTPH && row.november ? row.november / 24 : row.november || null,
        december:
          isTPH && row.december ? row.december / 24 : row.december || null,
        january: isTPH && row.january ? row.january / 24 : row.january || null,
        february:
          isTPH && row.february ? row.february / 24 : row.february || null,
        march: isTPH && row.march ? row.march / 24 : row.march || null,

        // aopStatus: row.aopStatus || 'draft',
        financialYear: row.financialYear,
        // plant: plantId,
        plantFKId: row.plantFKId || plantId,
        siteFKId: row.siteFKId || siteId,
        // material: 'EOE',
        materialFKId: row.normParametersFKId,
        verticalFKId: row.verticalFKId ??  localStorage.getItem('verticalId'),
        id: row.idFromApi || null,
        avgTPH: findAvg('1', row) || null,
        remark: row.remarks,
        remarks: row.remarks,
      }))

      const response = await DataService.editAOPMCCalculatedData(
        plantId,
        aopmccCalculatedData,
        keycloak,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Production Vol Data Saved Successfully!',
        severity: 'success',
      })
      // fetchData()
      return response
    } catch (error) {
      console.error('Error saving Production Vol Data:', error)
    } finally {
      fetchData()
    }
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
      // Validate that both normParameterId and remark are not empty
      // const invalidRows = data.filter(
      //   (row) => !row.normParametersFKId.trim(),
      //   // (row) => !row.normParametersFKId.trim() || !row.remark.trim(),
      // )
      const requiredMonths = [
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

      const invalidRows = data.filter((row) => {
        const hasProduct =
          row.normParametersFKId && row.normParametersFKId.trim() !== ''

        const hasRemark = row.remarks && row.remarks.trim() !== ''
        const hasValidMonth = requiredMonths.some((month) => {
          let value = row[month]
          if (value === 0) {
            value = null
          }
          return value !== null && value !== ''
        })

        console.log(
          `Row ID ${row.id}: hasProduct=${hasProduct}, hasRemark=${hasRemark}, hasValidMonth=${hasValidMonth}`,
        )

        return !(hasProduct && hasRemark && hasValidMonth)
      })

      if (invalidRows.length > 0) {
        setSnackbarData({
          message:
            'Please fill required fields: Product, Remark, and at least one month data.',
          severity: 'error',
        })
        setSnackbarOpen(true)
        console.log('Invalid rows:', invalidRows)
        return
      }
      await editAOPMCCalculatedData(data)
      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
    } catch (error) {
      // setIsSaving(false);
    }
  }, [apiRef, selectedUnit])

  const fetchData = async () => {
    try {
      setLoading(true)
      const data = await DataService.getAOPMCCalculatedData(keycloak)
      // const data = data1.slice(0, 3)
      const formattedData = data.map((item, index) => {
        const isTPH = selectedUnit == 'TPD'
        return {
          ...item,
          idFromApi: item?.id,
          normParametersFKId: item?.materialFKId.toLowerCase(),
          remarks: item?.remarks?.trim() || null,
          id: index,

          ...(isTPH && {
            april: item.april
              ? (item.april * 24).toFixed(2)
              : item.april || null,
            may: item.may ? (item.may * 24).toFixed(2) : item.may || null,
            june: item.june ? (item.june * 24).toFixed(2) : item.june || null,
            july: item.july ? (item.july * 24).toFixed(2) : item.july || null,
            august: item.august
              ? (item.august * 24).toFixed(2)
              : item.august || null,
            september: item.september
              ? (item.september * 24).toFixed(2)
              : item.september || null,
            october: item.october
              ? (item.october * 24).toFixed(2)
              : item.october || null,
            november: item.november
              ? (item.november * 24).toFixed(2)
              : item.november || null,
            december: item.december
              ? (item.december * 24).toFixed(2)
              : item.december || null,
            january: item.january
              ? (item.january * 24).toFixed(2)
              : item.january || null,
            february: item.february
              ? (item.february * 24).toFixed(2)
              : item.february || null,
            march: item.march
              ? (item.march * 24).toFixed(2)
              : item.march || null,
          }),
        }
      })
      // setProductNormData(formattedData)
      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProductsAll(
          keycloak,
          // lowerVertName === 'meg' ? 'Production' : 'Grade',
          'All',
        )
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
  }, [sitePlantChange, keycloak, selectedUnit, lowerVertName])

  const productionColumns = getEnhancedProductionColDefs({
    headerMap,
    allProducts,
    handleRemarkCellClick,
    findAvg,
  })
  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <ASDataGrid
        setRows={setRows}
        columns={productionColumns}
        rows={rows}
        title='Production Volume Data'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        apiRef={apiRef}
        // deleteId={deleteId}
        // setDeleteId={setDeleteId}
        // setOpen1={setOpen1}
        // open1={open1}
        // handleDeleteClick={handleDeleteClick}
        fetchData={fetchData}
        // onRowEditStop={handleRowEditStop}
        onProcessRowUpdateError={onProcessRowUpdateError}
        handleUnitChange={handleUnitChange}
        experimentalFeatures={{ newEditingApi: true }}
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
          saveWithRemark: true,
          showRefreshBtn: true,
          saveBtn: true,
          units: ['TPH', 'TPD'],
        }}
      />
    </div>
  )
}

export default ProductionvolumeData
