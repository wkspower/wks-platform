import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'

import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import ASDataGrid from './ASDataGrid'
const headerMap = generateHeaderNames()

import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
// import NumericCellEditor from 'utils/NumericCellEditor'
// import NumericInputOnly from 'utils/NumericInputOnly'
import getEnhancedColDefs from './CommonHeader/ProductionAopHeader'
const ProductionNorms = () => {
  const keycloak = useSession()
  // const [csData, setCsData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange } = dataGridStore
  const vertName = verticalChange?.verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('Ton')

  const [rows, setRows] = useState([])
  const [isSaving, setIsSaving] = useState(false)
  // States for the Remark Dialog
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || row.aopRemarks || '')
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
    try {
      const allRows = Array.from(apiRef.current.getRowModels().values())
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

      const invalidRows = updatedRows.filter(
        (row) => !row.normParametersFKId.trim() || !row.aopRemarks.trim(),
      )

      if (invalidRows.length > 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Please fill required fields: Product and Remark.',
          severity: 'error',
        })
        return
      }
      updateProductNormData(updatedRows)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [apiRef, selectedUnit])

  const updateProductNormData = async (newRow) => {
    try {
      let plantId = ''
      const isKiloTon = selectedUnit != 'Ton'
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const productNormData = newRow.map((row) => ({
        aopType: row.aopType || 'production',
        aopCaseId: row.aopCaseId || null,
        aopStatus: row.aopStatus || null,
        aopYear: localStorage.getItem('year'),
        plantFkId: plantId,
        normParametersFKId: row.normParametersFKId,
        // normItem: getProductName('1', row.normParametersFKId) || null,
        normItem: 'EOE',
        april: isKiloTon && row.april ? row.april * 1000 : row.april || null,
        may: isKiloTon && row.may ? row.may * 1000 : row.may || null,
        june: isKiloTon && row.june ? row.june * 1000 : row.june || null,
        july: isKiloTon && row.july ? row.july * 1000 : row.july || null,
        aug: isKiloTon && row.aug ? row.aug * 1000 : row.aug || null,
        sep: isKiloTon && row.sep ? row.sep * 1000 : row.sep || null,
        oct: isKiloTon && row.oct ? row.oct * 1000 : row.oct || null,
        nov: isKiloTon && row.nov ? row.nov * 1000 : row.nov || null,
        dec: isKiloTon && row.dec ? row.dec * 1000 : row.dec || null,
        jan: isKiloTon && row.jan ? row.jan * 1000 : row.jan || null,
        feb: isKiloTon && row.feb ? row.feb * 1000 : row.feb || null,
        march: isKiloTon && row.march ? row.march * 1000 : row.march || null,
        // avgTPH: findAvg('1', row) || null,
        avgTPH: findSum('1', row) || null,
        aopRemarks: row.aopRemarks || 'remarks',
        id: row.idFromApi || null,
      }))

      const response = await DataService.updateProductNormData(
        productNormData,
        keycloak,
      )

      if (response.status === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Production AOP Saved Successfully !',
          severity: 'success',
        })
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error Saving Production AOP:', error)
    }
  }

  const handleCalculate = async (year) => {
    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      const data = await DataService.handleCalculate(plantId, year, keycloak)

      if (data.status === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })

        const formattedData = data.map((item, index) => {
          const isKiloTon = selectedUnit != 'Ton'
          return {
            ...item,
            idFromApi: item.id,
            normParametersFKId: item?.normParametersFKId?.toLowerCase(),
            id: index,
            ...(isKiloTon && {
              jan: item.jan ? item.jan / 1000 : item.jan,
              feb: item.feb ? item.feb / 1000 : item.feb,
              march: item.march ? item.march / 1000 : item.march,
              april: item.april ? item.april / 1000 : item.april,
              may: item.may ? item.may / 1000 : item.may,
              june: item.june ? item.june / 1000 : item.june,
              july: item.july ? item.july / 1000 : item.july,
              aug: item.aug ? item.aug / 1000 : item.aug,
              sep: item.sep ? item.sep / 1000 : item.sep,
              oct: item.oct ? item.oct / 1000 : item.oct,
              nov: item.nov ? item.nov / 1000 : item.nov,
              dec: item.dec ? item.dec / 1000 : item.dec,
            }),
          }
        })

        setCsData(formattedData)
        setRows(formattedData)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
      }

      return data
    } catch (error) {
      console.error('Error saving refresh data:', error)
    }
  }

  const fetchData = async () => {
    try {
      setLoading(true)
      const data = await DataService.getAOPData(keycloak)
      // const data1 = data1.slice(0, 3)
      // if (data.status === 200) {
      const formattedData = data.map((item, index) => {
        const isKiloTon = selectedUnit !== 'Ton'
        return {
          ...item,
          idFromApi: item.id,
          normParametersFKId: item?.normParametersFKId?.toLowerCase(),
          id: index,
          ...(isKiloTon && {
            jan: item.jan ? item.jan / 1000 : item.jan,
            feb: item.feb ? item.feb / 1000 : item.feb,
            march: item.march ? item.march / 1000 : item.march,
            april: item.april ? item.april / 1000 : item.april,
            may: item.may ? item.may / 1000 : item.may,
            june: item.june ? item.june / 1000 : item.june,
            july: item.july ? item.july / 1000 : item.july,
            aug: item.aug ? item.aug / 1000 : item.aug,
            sep: item.sep ? item.sep / 1000 : item.sep,
            oct: item.oct ? item.oct / 1000 : item.oct,
            nov: item.nov ? item.nov / 1000 : item.nov,
            dec: item.dec ? item.dec / 1000 : item.dec,
          }),
        }
      })
      setRows(formattedData)
      setLoading(false) // Hide loading
      // }
      // else {
      //   setSnackbarOpen(true)
      //   setSnackbarData({
      //     message: 'Error fetching Production AOP data!',
      //     severity: 'error',
      //   })
      // }
    } catch (error) {
      console.error('Error fetching Production AOP data:', error)
    } finally {
      setLoading(false)
    }
  }

  // const getProductName = async (value, row) => {
  //   if (!row || !row.normParametersFKId) {
  //     return ''
  //   }

  //   let product
  //   if (allProducts && allProducts.length > 0) {
  //     product = allProducts.find((p) => p.id === row.normParametersFKId)
  //   } else {
  //     try {
  //       const data = await DataService.getAllProducts(
  //         keycloak,
  //         lowerVertName === 'meg' ? 'Production' : 'Grade',
  //       )
  //       product = data.find((p) => p.id === row.normParametersFKId)
  //     } catch (error) {
  //       console.error('Error fetching products:', error)
  //       return ''
  //     }
  //   }

  //   return product ? product.name : ''
  // }

  // const findAvg = (value, row) => {
  //   const months = [
  //     'april',
  //     'may',
  //     'june',
  //     'july',
  //     'aug',
  //     'sep',
  //     'oct',
  //     'nov',
  //     'dec',
  //     'jan',
  //     'feb',
  //     'march',
  //   ]

  //   const values = months.map((month) => row[month] || 0)
  //   const sum = values.reduce((acc, val) => acc + val, 0)
  //   const avg = (sum / values.length).toFixed(2)

  //   return avg === '0.00' ? null : avg
  // }

  const findSum = (value, row) => {
    const months = [
      'april',
      'may',
      'june',
      'july',
      'aug',
      'sep',
      'oct',
      'nov',
      'dec',
      'jan',
      'feb',
      'march',
    ]

    const values = months.map((month) => Number(row[month]) || 0)
    const sum = values.reduce((acc, val) => acc + val, 0)

    const total = sum.toFixed(2)
    return total === '0.00' ? null : total
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(
          keycloak,
          lowerVertName === 'meg' ? 'Production' : 'Grade',
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
        setIsSaving(false) // Reset loading state when API call finishes
      }
    }

    fetchData()
    getAllProducts()
  }, [sitePlantChange, keycloak, selectedUnit, verticalChange, lowerVertName])

  const productionColumns = getEnhancedColDefs({
    allProducts,
    headerMap,
    handleRemarkCellClick,
    findSum,
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
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <ASDataGrid
        columns={productionColumns}
        rows={rows}
        setRows={setRows}
        title={'Production AOP'}
        // title={lowerVertName === 'meg' ? 'Production AOP' : 'Budget Production'}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        updateProductNormData={updateProductNormData}
        processRowUpdate={processRowUpdate}
        // onRowEditStop={handleRowEditStop}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        handleCalculate={handleCalculate}
        apiRef={apiRef}
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
          saveWithRemark: true,
          showCalculate: true,
          saveBtn: true,
          // UOM: 'Ton',
          units: ['Ton', 'Kilo Ton'],
          // UnitToShow: 'Values/Ton',
        }}
        loading={isSaving}
      />
    </div>
  )
}

export default ProductionNorms
