import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'

import { generateHeaderNames } from 'components/Utilities/generateHeaders'
// import ASDataGrid from './ASDataGrid'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
// import NumericCellEditor from 'utils/NumericCellEditor'
// import NumericInputOnly from 'utils/NumericInputOnly'
import { validateFields } from 'utils/validationUtils'
import getEnhancedColDefs from '../data-tables/CommonHeader/Kendo_ProductionAopHeader'
import { useDispatch } from 'react-redux'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import KendoDataTables from './index'

const ProductionNorms = ({ permissions }) => {
  const [modifiedCells, setModifiedCells] = React.useState({})

  const [calculationObject, setCalculationObject] = useState([])

  const keycloak = useSession()
  // const [csData, setCsData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  // const [rowModesModel, setRowModesModel] = useState({})
  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [loading, setLoading] = useState(false)
  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('MT')

  const [rows, setRows] = useState([])
  // const [isSaving, setIsSaving] = useState(false)
  // States for the Remark Dialog
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const dispatch = useDispatch()
  // const isBlocked = useSelector((state) => state.isBlocked) // Get block flag from Redux

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || row.aopRemarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveChanges = React.useCallback(async () => {
    // const rowsInEditMode = Object.keys(rowModesModel).filter(
    //   (id) => rowModesModel[id]?.mode === 'edit',
    // )

    // rowsInEditMode.forEach((id) => {
    //   apiRef.current.stopRowEditMode({ id })
    // })

    setTimeout(() => {
      try {
        var editedData = Object.values(modifiedCells)
        const allRows = Array.from(apiRef.current.getRowModels().values())
        const updatedRows = allRows.map(
          (row) => unsavedChangesRef.current.unsavedRows[row.id] || row,
        )
        const rowsToSave = updatedRows.filter((row) => row.id !== 'total')

        if (updatedRows.length === 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          return
        }

        const requiredFields = ['aopRemarks']

        const validationMessage = validateFields(editedData, requiredFields)
        if (validationMessage) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: validationMessage,
            severity: 'error',
          })
          return
        }

        if (calculatebtnClicked == false) {
          //Consition changed if permissions?.saveBtn --> SET TO FALSE
          //UNCOMMENT THE CODE IF permissions?.saveBtn --> SET TO TRUE
          // if (editedData.length === 0) {
          //   setSnackbarOpen(true)
          //   setSnackbarData({
          //     message: 'No Records to Save!',
          //     severity: 'info',
          //   })
          //   setCalculatebtnClicked(false)
          //   return
          // }
          // updateProductNormData(editedData)
          updateProductNormData(rowsToSave)
        } else {
          updateProductNormData(rowsToSave)
        }
      } catch (error) {
        console.log('Error saving changes:', error)

        setCalculatebtnClicked(false)
      }
    }, 400)
  }, [apiRef, selectedUnit, calculatebtnClicked])

  const updateProductNormData = async (newRow) => {
    setLoading(true)

    try {
      let plantId = ''
      const isKiloTon = selectedUnit != 'MT'
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      // let siteID =
      //   JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

      const productNormData = newRow.map((row) => ({
        aopType: row.aopType || 'production',
        aopCaseId: row.aopCaseId || null,
        aopStatus: row.aopStatus || null,
        aopYear: localStorage.getItem('year'),
        plantFKId: plantId,
        materialFKId: row.normParametersFKId,
        siteFKId: JSON.parse(localStorage.getItem('selectedSiteId')).id,
        verticalFKId: localStorage.getItem('verticalId'),
        // normItem: getProductName('1', row.normParametersFKId) || null,
        // normItem: 'EOE',

        april:
          row.april === 0
            ? 0
            : isKiloTon && row.april
              ? row.april * 1000
              : row.april || null,
        may:
          row.may === 0
            ? 0
            : isKiloTon && row.may
              ? row.may * 1000
              : row.may || null,
        june:
          row.june === 0
            ? 0
            : isKiloTon && row.june
              ? row.june * 1000
              : row.june || null,
        july:
          row.july === 0
            ? 0
            : isKiloTon && row.july
              ? row.july * 1000
              : row.july || null,
        aug:
          row.aug === 0
            ? 0
            : isKiloTon && row.aug
              ? row.aug * 1000
              : row.aug || null,
        sep:
          row.sep === 0
            ? 0
            : isKiloTon && row.sep
              ? row.sep * 1000
              : row.sep || null,
        oct:
          row.oct === 0
            ? 0
            : isKiloTon && row.oct
              ? row.oct * 1000
              : row.oct || null,
        nov:
          row.nov === 0
            ? 0
            : isKiloTon && row.nov
              ? row.nov * 1000
              : row.nov || null,
        dec:
          row.dec === 0
            ? 0
            : isKiloTon && row.dec
              ? row.dec * 1000
              : row.dec || null,
        jan:
          row.jan === 0
            ? 0
            : isKiloTon && row.jan
              ? row.jan * 1000
              : row.jan || null,
        feb:
          row.feb === 0
            ? 0
            : isKiloTon && row.feb
              ? row.feb * 1000
              : row.feb || null,
        march:
          row.march === 0
            ? 0
            : isKiloTon && row.march
              ? row.march * 1000
              : row.march || null,

        // avgTPH: findAvg('1', row) || null,
        avgTPH: findSum('1', row) || null,
        aopRemarks: row.aopRemarks,
        id: row.idFromApi || null,
      }))

      const response = await DataService.updateProductNormData(
        productNormData,
        keycloak,
      )

      if (response) {
        dispatch(setIsBlocked(false))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Production AOP Saved Successfully !',
          severity: 'success',
        })

        setCalculatebtnClicked(false)
        setLoading(false)
        setModifiedCells({})

        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }

        setCalculatebtnClicked(false)
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
        setLoading(false)

        setCalculatebtnClicked(false)
      }
    } catch (error) {
      console.error('Error Saving Production AOP:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error Saving Production AOP!',
        severity: 'error',
      })
    } finally {
      setLoading(false)

      setCalculatebtnClicked(false)
    }
  }

  const handleCalculate = async () => {
    // dispatch(setIsBlocked(true))

    setCalculatebtnClicked(true)
    setLoading(true)
    try {
      const year = localStorage.getItem('year')
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      const data = await DataService.handleCalculate(plantId, year, keycloak)

      if (lowerVertName == 'meg' && data?.code == 200) {
        fetchData()
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        setLoading(false)
        return
      }

      let res = data?.data

      if (res) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        setLoading(false)

        const formattedData = res.map((item, index) => {
          const isKiloTon = selectedUnit != 'MT'

          return {
            ...item,
            idFromApi: item.id,
            normParametersFKId:
              item?.normParametersFKId?.toLowerCase() ||
              item?.materialFKId?.toLowerCase(),
            id: index,
            ...(isKiloTon && {
              jan: item.jan != null ? item.jan / 1000 : item.jan,
              feb: item.feb != null ? item.feb / 1000 : item.feb,
              march: item.march != null ? item.march / 1000 : item.march,
              april: item.april != null ? item.april / 1000 : item.april,
              may: item.may != null ? item.may / 1000 : item.may,
              june: item.june != null ? item.june / 1000 : item.june,
              july: item.july != null ? item.july / 1000 : item.july,
              aug: item.aug != null ? item.aug / 1000 : item.aug,
              sep: item.sep != null ? item.sep / 1000 : item.sep,
              oct: item.oct != null ? item.oct / 1000 : item.oct,
              nov: item.nov != null ? item.nov / 1000 : item.nov,
              dec: item.dec != null ? item.dec / 1000 : item.dec,
            }),
          }
        })

        // let totalRows = []
        // if (formattedData) {
        //   totalRows = totalRow(formattedData)
        // }

        const finalData = [...formattedData]
        // console.log(finalData)
        if (lowerVertName == 'pe') {
          setRows(finalData)
        }
        if (lowerVertName == 'meg') {
          // setRows(formattedData)
        } else {
          setRows(finalData)
        }

        // setRows(finalData)
        setLoading(false)

        // setTimeout(() => {
        saveChanges()
        // }, 1000)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
        setLoading(false)
      }

      return res
    } catch (error) {
      console.error('Error saving refresh data:', error)
      setLoading(false)
    }
  }

  const fetchData = async () => {
    try {
      setLoading(true)

      const response = await DataService.getAOPData(keycloak)

      setCalculationObject(response?.data?.aopCalculation)

      if (response?.code != 200) {
        setRows([])
        setLoading(false)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error fetching data. Please try again.',
          severity: 'error',
        })

        return
      }

      var data = response?.data?.aopDTOList
        ?.map((product) => ({
          ...product,
          normParametersFKId: product.materialFKId,
          originalRemark: product.aopRemarks,
          isEditable: false,

          ...(product.materialFKId !== undefined
            ? { materialFKId: undefined }
            : {}),
        }))
        .map(({ materialFKId, ...rest }) => rest)

      const formattedData = data.map((item, index) => {
        const isKiloTon = selectedUnit !== 'MT'

        const transformedItem = {
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

        const total = [
          transformedItem.april,
          transformedItem.may,
          transformedItem.june,
          transformedItem.july,
          transformedItem.aug,
          transformedItem.sep,
          transformedItem.oct,
          transformedItem.nov,
          transformedItem.dec,
          transformedItem.jan,
          transformedItem.feb,
          transformedItem.march,
        ].reduce((sum, val) => sum + (parseFloat(val) || 0), 0)

        return {
          ...transformedItem,
          averageTPH: total,
        }
      })

      // let totalRows = []
      // if (formattedData.length > 0) {
      //   totalRows = totalRow(formattedData)
      // }
      const finalData = [...formattedData]

      if (lowerVertName == 'pe') {
        setRows(finalData)
      }
      if (lowerVertName == 'meg') {
        setRows(formattedData)
      }
      if (permissions?.needTotal) {
        setRows(finalData)
      } else {
        setRows(finalData)
      }

      // setRows(formattedData)
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
        const data = await DataService.getAllProductsAll(
          keycloak,
          lowerVertName === 'meg' ? 'All' : 'All',
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
        setLoading(false) // Reset loading state when API call finishes
      }
    }

    fetchData()
    getAllProducts()
  }, [
    sitePlantChange,
    oldYear,
    yearChanged,
    keycloak,
    selectedUnit,
    lowerVertName,
  ])

  const productionColumns = getEnhancedColDefs({
    allProducts,
    headerMap,
    handleRemarkCellClick,
    findSum,
  })

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }
  const isCellEditable = (params) => params.row.id !== 'total'

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
      showCalculate: false,
      isOldYear: isOldYear,
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
      showCalculate: permissions?.showCalculate ?? true,
      allAction: permissions?.allAction ?? true,

      showCalculateVisibility:
        Object.keys(calculationObject).length > 0
          ? permissions?.showCalculate ?? true
          : false,
      saveBtn: permissions?.saveBtn ?? false,
      units: ['MT', 'KT'],
      customHeight: permissions?.customHeight,
    },
    isOldYear,
  )
  const rowData = [
    {
      idFromApi: null,
      aopCaseId: null,
      aopType: null,
      aopYear: null,
      plantFkId: null,
      normParametersFKId: 'Ethyelene',
      uom: 'MT/Month',
      april: 13420,
      may: 12875,
      june: 14210,
      july: 13750,
      aug: 12995,
      sep: 14130,
      oct: 13580,
      nov: 13045,
      dec: 13670,
      jan: 13920,
      feb: 13105,
      march: 13840,
      averageTPH: '',
      isEditable: false,
      aopStatus: '',
    },
    {
      idFromApi: null,
      aopCaseId: null,
      aopType: null,
      aopYear: null,
      plantFkId: null,
      normParametersFKId: 'Propylene',
      uom: 'MT/Month',
      april: 9450,
      may: 10235,
      june: 11090,
      july: 10720,
      aug: 11560,
      sep: 10985,
      oct: 11340,
      nov: 10575,
      dec: 11120,
      jan: 11280,
      feb: 10850,
      march: 11430,
      averageTPH: '',
      isEditable: false,
      aopStatus: '',
    },
  ]
  const NormParameterIdCell = (props) => {
    // console.log(props)
    const productId = props.dataItem.normParametersFKId
    const product = allProducts.find((p) => p.id === productId)
    const displayName = product?.displayName || ''
    return (
      <td>{displayName ? displayName : props.dataItem.normParametersFKId}</td>
    )
  }
  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        columns={productionColumns}
        rows={lowerVertName === 'cracker' ? rowData : rows}
        setRows={setRows}
        NormParameterIdCell={NormParameterIdCell}
        title={'Production AOP'}
        isCellEditable={isCellEditable}
        // title={lowerVertName === 'meg' ? 'Production AOP' : 'Budget Production'}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        updateProductNormData={updateProductNormData}
        // processRowUpdate={processRowUpdate}

        // rowModesModel={rowModesModel}
        // onRowModesModelChange={onRowModesModelChange}
        // onRowEditStop={handleRowEditStop}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        handleCalculate={handleCalculate}
        apiRef={apiRef}
        fetchData={fetchData}
        // onProcessRowUpdateError={onProcessRowUpdateError}
        handleUnitChange={handleUnitChange}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        unsavedChangesRef={unsavedChangesRef}
        permissions={adjustedPermissions}
      />
    </div>
  )
}

export default ProductionNorms
