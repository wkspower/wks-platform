import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
// import ASDataGrid from '../data-tables/ASDataGrid'
import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
// import getEnhancedProductionColDefs from '../data-tables/CommonHeader/ProductionVolumeHeader'
import getEnhancedProductionColDefs from '../data-tables/CommonHeader/Kendo_ProductionVolumeHeader'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useDispatch } from 'react-redux'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import { Typography } from '../../../node_modules/@mui/material/index'
// import TextField from '@mui/material/TextField'
import KendoDataTables from './index'

const ProductionvolumeData = ({ permissions }) => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)

  const [_plantID, set_PlantID] = useState('')

  const keycloak = useSession()

  const [calculationObject, setCalculationObject] = useState([])

  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear, plantID } =
    dataGridStore
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [rows, setRows] = useState()
  const [rows2, setRows2] = useState()
  const [rows500, setRows500] = useState()
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
  // const unsavedChangesRef = React.useRef({
  //   unsavedRows: {},
  //   rowsBeforeChange: {},
  // })
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  useEffect(() => {
    if (plantID?.plantId) {
      set_PlantID(plantID?.plantId)
    }
  }, [plantID])

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
        setModifiedCells({})
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

  // const processRowUpdate = React.useCallback((newRow, oldRow) => {
  //   const rowId = newRow.id
  //   const updatedFields = []
  //   for (const key in newRow) {
  //     if (
  //       Object.prototype.hasOwnProperty.call(newRow, key) &&
  //       newRow[key] !== oldRow[key]
  //     ) {
  //       updatedFields.push(key)
  //     }
  //   }

  //   unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow
  //   if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
  //     unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
  //   }
  //   setRows((prevRows) =>
  //     prevRows.map((row) =>
  //       row.id === newRow.id ? { ...newRow, isNew: false } : row,
  //     ),
  //   )
  //   if (updatedFields.length > 0) {
  //     setModifiedCells((prevModifiedCells) => ({
  //       ...prevModifiedCells,
  //       [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
  //     }))
  //   }
  //   return newRow
  // }, [])
  // console.log(modifiedCells)
  const saveChanges = React.useCallback(async () => {
    // const rowsInEditMode = Object.keys(rowModesModel).filter(
    //   (id) => rowModesModel[id]?.mode === 'edit',
    // )

    // rowsInEditMode.forEach((id) => {
    //   apiRef.current.stopRowEditMode({ id })
    // })
    setTimeout(() => {
      try {
        var data = Object.values(modifiedCells)
        // console.log(data)
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
        setEnableSaveAddBtn(false)
      } catch (error) {
        console.log('Facing issue at saving data', error)
      }
    }, 400)
  }, [modifiedCells, selectedUnit])

  const fetchData = async () => {
    try {
      setLoading(true)
      const response = await DataService.getAOPMCCalculatedData(keycloak)

      if (response?.code != 200) {
        setRows([])
        setLoading(false)

        // setSnackbarOpen(true)
        // setSnackbarData({
        //   message: 'Error fetching data. Please try again.',
        //   severity: 'error',
        // })

        return
      }

      setCalculationObject(response?.data?.aopCalculation)

      var formattedData = response?.data?.aopMCCalculatedDataDTOList.map(
        (item, index) => {
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
        },
      )

      const formulatedData = normalizeAllRows(formattedData)

      const nonEditableRows = formulatedData.map((item) => ({
        ...item,
        isEditable: false,
      }))
      var formattedDataNONEDITABLE = formattedData.map((item) => ({
        ...item,
        isEditable: false,
      }))

      formattedData = formattedData.map((item) => ({
        ...item,
        remarks: item.remarks ? item.remarks.trim() : '',
      }))
      setRows2(nonEditableRows)
      setRows(formattedData)
      setRows500(formattedDataNONEDITABLE)
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
  const formatValueToFiveDecimals = (params) =>
    params ? parseFloat(params).toFixed(2) : ''

  const colDefs = [
    {
      field: 'idFromApi',
      title: 'ID',
      hidden: true,
    },
    {
      field: 'aopCaseId',
      title: 'Case ID',

      editable: false,
      hidden: true,
    },
    {
      field: 'materialFKId',
      title: 'Particulars',

      editable: false,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',

      editable: false,
    },
    {
      field: 'april',
      title: headerMap[4],
      editable: false,
      align: 'left',
      headerAlign: 'left',
      format: '{0:#.###}',
      type: 'number',
    },
    {
      field: 'may',
      title: headerMap[5],
      editable: false,

      align: 'left',
      headerAlign: 'left',
      format: '{0:#.###}',
      type: 'number',
    },
    {
      field: 'june',
      title: headerMap[6],
      format: '{0:#.###}',
      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'july',
      format: '{0:#.###}',
      title: headerMap[7],
      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'august',
      title: headerMap[8],
      format: '{0:#.###}',
      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'september',
      title: headerMap[9],
      format: '{0:#.###}',
      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'october',
      title: headerMap[10],
      format: '{0:#.###}',
      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'november',
      title: headerMap[11],
      format: '{0:#.###}',
      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'december',
      title: headerMap[12],
      format: '{0:#.###}',
      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'january',
      title: headerMap[1],
      format: '{0:#.###}',
      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'february',
      title: headerMap[2],
      format: '{0:#.###}',
      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'march',
      title: headerMap[3],
      format: '{0:#.###}',
      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },

    {
      field: 'avgTph',
      title: 'AVG',

      editable: false,
      hidden: true,
    },
    {
      field: 'isEditable',
      title: 'isEditable',
      hidden: true,
    },
  ]

  const colDefs1233 = [
    {
      field: 'idFromApi',
      title: 'ID',
      hidden: true,
    },
    {
      field: 'aopCaseId',
      title: 'Case ID',
      hidden: true,
    },

    {
      field: 'normParametersFKId',
      title: 'Particulars',

      editable: false,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',

      editable: false,
    },

    {
      field: 'april',
      title: headerMap[4],
      editable: false,
      align: 'left',
      headerAlign: 'left',
      format: '{0:#.###}',

      type: 'number',
    },
    {
      field: 'may',
      title: headerMap[5],
      format: '{0:#.###}',

      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'june',
      title: headerMap[6],
      format: '{0:#.###}',

      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'july',
      title: headerMap[7],
      format: '{0:#.###}',

      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'august',
      title: headerMap[8],
      format: '{0:#.###}',

      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'september',
      title: headerMap[9],
      format: '{0:#.###}',

      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'october',
      title: headerMap[10],
      format: '{0:#.###}',

      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'november',
      title: headerMap[11],
      format: '{0:#.###}',

      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'december',
      title: headerMap[12],
      format: '{0:#.###}',

      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'january',
      title: headerMap[1],
      format: '{0:#.###}',

      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'february',
      title: headerMap[2],
      format: '{0:#.###}',

      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },
    {
      field: 'march',
      title: headerMap[3],
      format: '{0:#.###}',

      editable: false,

      align: 'left',
      headerAlign: 'left',
      type: 'number',
    },

    {
      field: 'avgTph',
      title: 'AVG',

      editable: false,
      hidden: true,
    },
    {
      field: 'isEditable',
      title: 'isEditable',
      hidden: true,
    },
  ]

  useEffect(() => {
    fetchData()
  }, [oldYear, yearChanged, keycloak, selectedUnit, plantID])

  const productionColumns = getEnhancedProductionColDefs({
    headerMap,
  })

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
          message: 'Data Refresh Failed!',
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
      allAction: false,
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
      allAction: permissions?.allAction ?? true,
      addButton: permissions?.addButton ?? false,
      deleteButton: permissions?.deleteButton ?? false,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? true,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      showRefreshBtn: permissions?.showRefreshBtn ?? true,
      saveBtn: permissions?.saveBtn ?? true,
      units: ['TPH', 'TPD'],
      customHeight: permissions?.customHeight ?? defaultCustomHeight,
      showCalculate: permissions?.hideSummary ? false : lowerVertName === 'meg',
      showCalculateVisibility:
        lowerVertName === 'meg' &&
        Object.keys(calculationObject || {}).length > 0
          ? true
          : false,
      downloadExcelBtn: permissions?.hideDownloadExcel
        ? false
        : lowerVertName == 'meg'
          ? true
          : false,
      uploadExcelBtn: permissions?.hideUploadExcel
        ? false
        : lowerVertName == 'meg'
          ? true
          : false,
    },
    isOldYear,
  )
  const NormParameterIdCell = (props) => {
    const productId = props.dataItem.normParametersFKId
    const product = allProducts.find((p) => p.id === productId)
    const displayName = product?.displayName || ''
    return <td>{displayName}</td>
  }
  const NormParameterIdCell2 = (props) => {
    const productId = props.dataItem.normParametersFKId
    const product = allProducts.find((p) => p.id === productId)
    const displayName = product?.displayName || ''
    return <td>{displayName}</td>
  }
  var cols = permissions?.hideSummary ? colDefs1233 : productionColumns
  var rows1 = permissions?.hideSummary ? rows500 : rows

  const handleExcelUpload = (rawFile) => {
    saveExcelFile(rawFile)
  }
  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      await DataService.getProductionVolExcel(keycloak)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error!', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    } finally {
      // optional cleanup or logging
    }
  }

  const saveExcelFile = async (rawFile) => {
    setLoading(true)
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const response = await DataService.saveProductionVolDataExcel(
        rawFile,
        keycloak,
      )
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Upload Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        setLoading(false)

        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving data:', error)
      setLoading(false)
    } finally {
      // fetchData()
      setLoading(false)
    }
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
        enableSaveAddBtn={enableSaveAddBtn}
        setRows={setRows}
        columns={cols}
        rows={rows1}
        title='Production Volume Data'
        paginationOptions={[100, 200, 300]}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        NormParameterIdCell={NormParameterIdCell}
        setSnackbarData={setSnackbarData}
        apiRef={apiRef}
        fetchData={fetchData}
        handleUnitChange={handleUnitChange}
        handleRemarkCellClick={handleRemarkCellClick}
        experimentalFeatures={{ newEditingApi: true }}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        handleCalculate={handleCalculate}
        permissions={adjustedPermissions}
        selectedUnit={selectedUnit}
        setSelectedUnit={setSelectedUnit}
        handleExcelUpload={handleExcelUpload}
        downloadExcelForConfiguration={downloadExcelForConfiguration}
      />

      {!permissions?.hideSummary && (
        <>
          <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
            Percentage Summary
          </Typography>
          <KendoDataTables
            setRows={setRows2}
            columns={colDefs}
            rows={rows2}
            title='Production Volume Data Reference'
            fetchData={fetchData}
            NormParameterIdCell={NormParameterIdCell2}
          />
        </>
      )}
    </div>
  )
}

export default ProductionvolumeData
