import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'

import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import ASDataGrid from './ASDataGrid'
const headerMap = generateHeaderNames()

import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
import { GridRowModes } from '@mui/x-data-grid'

const ProductionNorms = () => {
  const keycloak = useSession()
  const [csData, setCsData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [rowModesModel, setRowModesModel] = useState({})

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow
    if (unsavedChangesRef.current.unsavedRows) {
      setCsData(oldRow?.map((row) => (row.id === newRow.id ? newRow : row)))
    }
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }
    return newRow
  }, [])

  // const saveChanges = React.useCallback(async () => {
  //   console.log(
  //     'Edited Data: ',
  //     Object.values(unsavedChangesRef.current.unsavedRows),
  //   )
  //   try {
  //     // if (title === 'Business Demand') {
  //     var data = Object.values(unsavedChangesRef.current.unsavedRows)
  //     updateProductNormData(data)
  //     // }

  //     unsavedChangesRef.current = {
  //       unsavedRows: {},
  //       rowsBeforeChange: {},
  //     }
  //   } catch (error) {
  //     // setIsSaving(false);
  //   }
  // }, [apiRef])
  const saveChanges = React.useCallback(async () => {
    setTimeout(() => {
      try {
        // setIsSaving(true)
        const allRows = Array.from(apiRef.current.getRowModels().values())
        const updatedRows = allRows.map(
          (row) => unsavedChangesRef.current.unsavedRows[row.id] || row,
        )
        updateProductNormData(updatedRows)
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
        // setHasUnsavedRows(false)
        // setIsSaving(false)
      } catch (error) {
        // setIsSaving(false)
      }
    }, 1000)
  }, [apiRef])

  const updateProductNormData = async (newRow) => {
    try {
      let plantId = ''
      const isTPH = selectedUnit != 'TPH'
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

        april: isTPH && row.april ? row.april * 24 : row.april || null,
        may: isTPH && row.may ? row.may * 24 : row.may || null,
        june: isTPH && row.june ? row.june * 24 : row.june || null,
        july: isTPH && row.july ? row.july * 24 : row.july || null,
        aug: isTPH && row.aug ? row.aug * 24 : row.aug || null,
        sep: isTPH && row.sep ? row.sep * 24 : row.sep || null,
        oct: isTPH && row.oct ? row.oct * 24 : row.oct || null,
        nov: isTPH && row.nov ? row.nov * 24 : row.nov || null,
        dec: isTPH && row.dec ? row.dec * 24 : row.dec || null,
        jan: isTPH && row.jan ? row.jan * 24 : row.jan || null,
        feb: isTPH && row.feb ? row.feb * 24 : row.feb || null,
        march: isTPH && row.march ? row.march * 24 : row.march || null,

        avgTPH: findAvg('1', row) || null,

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
        const isTPH = selectedUnit != 'TPH'
        return {
          ...item,
          idFromApi: item.id,
          normParametersFKId: item?.normParametersFKId?.toLowerCase(),
          id: index,
          ...(isTPH && {
            jan: item.jan ? item.jan / 24 : item.jan,
            feb: item.feb ? item.feb / 24 : item.feb,
            march: item.march ? item.march / 24 : item.march,
            april: item.april ? item.april / 24 : item.april,
            may: item.may ? item.may / 24 : item.may,
            june: item.june ? item.june / 24 : item.june,
            july: item.july ? item.july / 24 : item.july,
            aug: item.aug ? item.aug / 24 : item.aug,
            sep: item.sep ? item.sep / 24 : item.sep,
            oct: item.oct ? item.oct / 24 : item.oct,
            nov: item.nov ? item.nov / 24 : item.nov,
            dec: item.dec ? item.dec / 24 : item.dec,
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
      const data = await DataService.getAOPData(keycloak)
      const formattedData = data.map((item, index) => {
        const isTPH = selectedUnit != 'TPH'
        return {
          ...item,
          idFromApi: item.id,
          normParametersFKId: item?.normParametersFKId?.toLowerCase(),
          id: index,
          ...(isTPH && {
            jan: item.jan ? item.jan / 24 : item.jan,
            feb: item.feb ? item.feb / 24 : item.feb,
            march: item.march ? item.march / 24 : item.march,
            april: item.april ? item.april / 24 : item.april,
            may: item.may ? item.may / 24 : item.may,
            june: item.june ? item.june / 24 : item.june,
            july: item.july ? item.july / 24 : item.july,
            aug: item.aug ? item.aug / 24 : item.aug,
            sep: item.sep ? item.sep / 24 : item.sep,
            oct: item.oct ? item.oct / 24 : item.oct,
            nov: item.nov ? item.nov / 24 : item.nov,
            dec: item.dec ? item.dec / 24 : item.dec,
          }),
        }
      })

      setCsData(formattedData)
    } catch (error) {
      console.error('Error fetching Production AOP data:', error)
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
        const data = await DataService.getAllProducts(keycloak)
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

    fetchData()
    getAllProducts()
  }, [sitePlantChange, keycloak, selectedUnit])

  const productionColumns = [
    { field: 'idFromApi', headerName: 'ID' },
    {
      field: 'aopCaseId',
      headerName: 'Case ID',
      minWidth: 120,
      editable: false,
    },
    { field: 'aopType', headerName: 'Type', minWidth: 80 },

    { field: 'aopYear', headerName: 'Year', minWidth: 80 },

    { field: 'plantFkId', headerName: 'Plant ID', minWidth: 80 },

    // normParametersFKId
    {
      field: 'normParametersFKId',
      headerName: 'Product',
      editable: false,
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
                field: 'normParametersFKId',
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
            {/* Disabled first option */}
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

    // {
    //   field: 'productNameHide',
    //   headerName: 'Product Name',
    //   width: 200,
    //   valueGetter: getProductName,
    // },

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
      field: 'aug',
      headerName: headerMap['aug'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'sep',
      headerName: headerMap['sep'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'oct',
      headerName: headerMap['oct'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'nov',
      headerName: headerMap['nov'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'dec',
      headerName: headerMap['dec'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jan',
      headerName: headerMap['jan'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'feb',
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
      field: 'averageTPH',
      headerName: 'AVG',
      minWidth: 150,
      editable: false,
      valueGetter: findAvg,
    },
    {
      field: 'aopRemarks',
      headerName: 'Remark',
      minWidth: 75,
      editable: true,
    },
    { field: 'aopStatus', headerName: 'aopStatus', editable: false },
  ]

  const handleRowEditStop = (params, event) => {
    setRowModesModel({
      ...rowModesModel,
      [params.id]: { mode: GridRowModes.View, ignoreModifications: false },
    })
  }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const handleUnitChange = (unit) => {
    console.log('Selected unit:', unit)
    setSelectedUnit(unit)
  }

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={csData}
        title='Production AOP'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        updateProductNormData={updateProductNormData}
        processRowUpdate={processRowUpdate}
        onRowEditStop={handleRowEditStop}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        handleCalculate={handleCalculate}
        apiRef={apiRef}
        // deleteId={deleteId}
        // setDeleteId={setDeleteId}
        // setOpen1={setOpen1}
        // open1={open1}
        // handleDeleteClick={handleDeleteClick}
        fetchData={fetchData}
        onProcessRowUpdateError={onProcessRowUpdateError}
        setRowModesModel={setRowModesModel}
        rowModesModel={rowModesModel}
        handleUnitChange={handleUnitChange}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: true,
          saveWithRemark: true,
          showCalculate: true,
          saveBtn: true,
          UOM: 'TPH',
        }}
      />
    </div>
  )
}

export default ProductionNorms
