import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import ASDataGrid from './ASDataGrid'
import { useGridApiRef } from '@mui/x-data-grid'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedProductionColDefs from './CommonHeader/ProductionVolumeHeader'

import { useDispatch } from 'react-redux'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { Typography } from '../../../node_modules/@mui/material/index'

const ProductionvolumeData = ({ permissions }) => {
  const keycloak = useSession()
  // const [productNormData, setProductNormData] = useState([])
  const [rowModesModel, setRowModesModel] = useState({})

  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [rows, setRows] = useState()
  const [rows2, setRows2] = useState()
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
  const dispatch = useDispatch()
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

      const storedSite = localStorage.getItem('selectedSiteId')
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
        verticalFKId: row.verticalFKId ?? localStorage.getItem('verticalId'),
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
      // console.log(response)
      if (response?.length > 0) {
        dispatch(setIsBlocked(false))

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Production Vol Data Saved Successfully!',
          severity: 'success',
        })
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Please fill all fields, try again!',
          severity: 'error',
        })
      }
      fetchData()
      return response
    } catch (error) {
      console.error('Error saving Production Vol Data:', error)
    } finally {
      // fetchData()
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
    const rowsInEditMode = Object.keys(rowModesModel).filter(
      (id) => rowModesModel[id]?.mode === 'edit',
    )

    rowsInEditMode.forEach((id) => {
      apiRef.current.stopRowEditMode({ id })
    })
    setTimeout(() => {
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

        const invalidRows = data.filter((row) => {
          if (!row.normParametersFKId || !row.normParametersFKId.trim()) {
            return true
          }

          for (const month of months) {
            const value = row[month]
            if (
              value === 0 ||
              value === null ||
              (typeof value === 'string' && !value.trim())
            ) {
              return true
            }
          }

          const remarkValue = row.remark || row.remarks
          const originalRemarkValue =
            row.originalRemark || row.originalRemarks || ''

          if (
            !remarkValue ||
            (typeof remarkValue === 'string' && !remarkValue.trim()) ||
            remarkValue.trim() === originalRemarkValue.trim()
          ) {
            return true
          }

          return false
        })

        if (invalidRows.length > 0) {
          setSnackbarData({
            message:
              'Please fill all fields in edited row and update the Remark!',
            severity: 'error',
          })
          setSnackbarOpen(true)
          return
        } else {
          editAOPMCCalculatedData(data)
        }

        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
      } catch (error) {
        console.log('Facing issue at saving data', error)
      }
    }, 400)
  }, [apiRef, rowModesModel, selectedUnit])

  const fetchData = async () => {
    try {
      setLoading(true)
      const data = await DataService.getAOPMCCalculatedData(keycloak)
      const formattedData = data.map((item, index) => {
        const isTPH = selectedUnit == 'TPD'
        return {
          ...item,
          idFromApi: item?.id,
          normParametersFKId: item?.materialFKId.toLowerCase(),
          remarks: item?.remarks?.trim() || null,
          originalRemark: item?.remarks?.trim() || null,

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
      // console.log(formattedData)
      // console.log(data)
      const formulatedData = normalizeAllRows(formattedData)
      // console.log(formulatedData)

      const nonEditableRows = formulatedData.map((item) => ({
        ...item,
        isEditable: false,
      }))

      setRows2(nonEditableRows)
      setRows(nonEditableRows)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }
  function normalizeAllRows(grid) {
    const monthKeys = [
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

    return grid?.map((row) => {
      // 1. Find this row?s max month value
      const vals = monthKeys?.map((k) => Number(row[k]))
      const maxVal = Math.max(...vals)

      // 2. Shallow-clone the entire row (carries over id, remarks, all FKs, etc.)
      const newRow = { ...row }

      // 3. Overwrite only the month fields:
      monthKeys.forEach((key) => {
        const orig = Number(row[key] || 0)
        const pct = maxVal ? (orig / maxVal) * 100 : 0
        newRow[key] = Number(pct.toFixed(2))
      })

      return newRow
    })
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
  }, [
    sitePlantChange,
    oldYear,
    yearChanged,
    keycloak,
    selectedUnit,
    lowerVertName,
  ])

  const productionColumns = getEnhancedProductionColDefs({
    headerMap,
    allProducts,
    handleRemarkCellClick,
    findAvg,
  })

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  const handleCalculate = () => {
    if (lowerVertName == 'meg') {
      handleCalculateMeg()
    } else {
      // handleCalculatePe()
    }
  }

  const handleCalculateMeg = async () => {
    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      const data = await DataService.handleCalculateProductionVolData(
        plantId,
        year,
        keycloak,
      )

      if (data || data == 0) {
        // dispatch(setIsBlocked(true))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
      }

      return data
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      console.error('Error!', error)
    }
  }
  const defaultCustomHeight = { mainBox: '36vh', otherBox: '112%' }

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      showCalculate: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? false,
      addButton: permissions?.addButton ?? false,
      deleteButton: permissions?.deleteButton ?? false,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? true,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      showRefreshBtn: permissions?.showRefreshBtn ?? true,
      saveBtn: permissions?.saveBtn ?? false,
      units: ['TPH', 'TPD'],
      customHeight: permissions?.customHeight ?? defaultCustomHeight,
      showCalculate:
        permissions?.showCalculate ?? lowerVertName == 'meg' ? true : false,
    },
    isOldYear,
  )

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
        rowModesModel={rowModesModel}
        onRowModesModelChange={onRowModesModelChange}
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
        handleCalculate={handleCalculate}
        permissions={adjustedPermissions}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Percentage Summary
      </Typography>
      <ASDataGrid
        setRows={setRows2}
        columns={productionColumns}
        rows={rows2}
        title='Production Volume Data'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        rowModesModel={rowModesModel}
        onRowModesModelChange={onRowModesModelChange}
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
        handleCalculate={handleCalculate}
        permissions={{ customHeight: defaultCustomHeight }}
      />
    </div>
  )
}

export default ProductionvolumeData
