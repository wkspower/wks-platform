import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'

import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import ASDataGrid from './ASDataGrid'
const headerMap = generateHeaderNames()

import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'

import NumericCellEditor from 'utils/NumericCellEditor'
import NumericInputOnly from 'utils/NumericInputOnly'
import getEnhancedColDefs from './CommonHeader/ProductionAopHeader'
const ProductionNorms = () => {
  const keycloak = useSession()
  const [csData, setCsData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange } = dataGridStore
  const vertName = verticalChange?.verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
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

  const saveChanges_DoNotDelete = React.useCallback(async () => {
    setTimeout(() => {
      try {
        var data = Object.values(unsavedChangesRef.current.unsavedRows)
        updateProductNormData(data)
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
      } catch (error) {}
    }, 1000)
  }, [apiRef])

  const saveChanges = React.useCallback(async () => {
    setTimeout(() => {
      try {
        // setIsSaving(true)
        const allRows = Array.from(apiRef.current.getRowModels().values())
        const updatedRows = allRows.map(
          (row) => unsavedChangesRef.current.unsavedRows[row.id] || row,
        )
        // Validation: Check if there are any rows to save
        if (updatedRows.length === 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          return
        }

        // Validate that both normParameterId and remark are not empty
        const invalidRows = updatedRows.filter(
          (row) => !row.normParametersFKId.trim() || !row.remark.trim(),
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
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
        // setHasUnsavedRows(false)
      } catch (error) {}
    }, 1000)
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
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Production AOP Saved Successfully !',
        severity: 'success',
      })
      return response
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
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Data refresh successfully!',
        severity: 'success',
      })

      return data
    } catch (error) {
      console.error('Error saving refresh data:', error)
    }
  }

  const fetchData = async () => {
    try {
      setIsSaving(true)
      const data = await DataService.getAOPData(keycloak)
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
      setCsData(formattedData)
      setRows(formattedData)
    } catch (error) {
      console.error('Error fetching Production AOP data:', error)
    } finally {
      setIsSaving(false)
    }
  }

  const getProductName = async (value, row) => {
    if (!row || !row.normParametersFKId) {
      return ''
    }

    let product
    if (allProducts && allProducts.length > 0) {
      product = allProducts.find((p) => p.id === row.normParametersFKId)
    } else {
      try {
        const data = await DataService.getAllProducts(keycloak, 'Production')
        product = data.find((p) => p.id === row.normParametersFKId)
      } catch (error) {
        console.error('Error fetching products:', error)
        return ''
      }
    }

    return product ? product.name : ''
  }

  const findAvg = (value, row) => {
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

    const values = months.map((month) => row[month] || 0)
    const sum = values.reduce((acc, val) => acc + val, 0)
    const avg = (sum / values.length).toFixed(2)

    return avg === '0.00' ? null : avg
  }

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
        const data = await DataService.getAllProducts(keycloak, 'Production')
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
  }, [sitePlantChange, keycloak, selectedUnit])

  // const productionColumns = [
  //   { field: 'idFromApi', headerName: 'ID' },
  //   {
  //     field: 'aopCaseId',
  //     headerName: 'Case ID',
  //     minWidth: 120,
  //     editable: false,
  //   },
  //   { field: 'aopType', headerName: 'Type', minWidth: 80 },

  //   { field: 'aopYear', headerName: 'Year', minWidth: 80 },

  //   { field: 'plantFkId', headerName: 'Plant ID', minWidth: 80 },

  //   // normParametersFKId
  //   {
  //     field: 'normParametersFKId',
  //     headerName: lowerVertName === 'meg' ? 'Product' : 'Grade Name',
  //     editable: false,
  //     minWidth: 125,
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
  //               field: 'normParametersFKId',
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
  //           {/* Disabled first option */}
  //           <option value='' disabled>
  //             Select
  //           </option>
  //           {allProducts.map((product) => (
  //             <option key={product.id} value={product.id}>
  //               {product.displayName}
  //             </option>
  //           ))}
  //         </select>
  //       )
  //     },
  //   },

  //   // {
  //   //   field: 'productNameHide',
  //   //   headerName: 'Product Name',
  //   //   width: 200,
  //   //   valueGetter: getProductName,
  //   // },

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
  //     field: 'aug',
  //     headerName: headerMap[8],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'sep',
  //     headerName: headerMap[9],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'oct',
  //     headerName: headerMap[10],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },

  //   {
  //     field: 'nov',
  //     headerName: headerMap[11],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'dec',
  //     headerName: headerMap[12],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'jan',
  //     headerName: headerMap[1],
  //     editable: true,
  //     type: 'number',
  //     align: 'left',
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'feb',
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
  //   {
  //     field: 'averageTPH',
  //     headerName: 'Total',
  //     // minWidth: 150,
  //     editable: false,
  //     // valueGetter: findAvg,
  //     valueGetter: findSum,
  //   },
  //   {
  //     field: 'aopRemarks',
  //     headerName: 'Remark',
  //     minWidth: 175,
  //     editable: true,
  //     renderCell: (params) => {
  //       // console.log(params)
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
  //   { field: 'aopStatus', headerName: 'aopStatus', editable: false },
  // ]

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
