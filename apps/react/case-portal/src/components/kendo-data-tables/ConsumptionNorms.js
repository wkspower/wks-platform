//import DataGridTable from './ASDataGrid'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import { validateFields } from 'utils/validationUtils'
import getEnhancedColDefs from '../data-tables/CommonHeader/kendoconsumptionHeader'

import { Box } from '@mui/material'
//import './data-grid-css.css'
//import './extra-css.css'

import KendoDataTables from './index'
import { ConsumptionNormsApiService } from 'services/consumption-norms-api-service'

const ConsumptionNorms = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [calculationObject, setCalculationObject] = useState([])
  const keycloak = useSession()
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [isAccordionExpanded, setIsAccordionExpanded] = useState(true)

  const [open1, setOpen1] = useState(false)

  const dataGridStore = useSelector((state) => state.dataGridStore)

  const { sitePlantChange, verticalChange, yearChanged, oldYear, plantID } =
    dataGridStore
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const [loading, setLoading] = useState(false)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const [_plantID, set_PlantID] = useState('')
  const dispatch = useDispatch()
  const [gradeId, setGradeId] = useState(null)
  const [grades, setGrades] = useState([])

  useEffect(() => {
    if (plantID?.plantId) {
      set_PlantID(plantID?.plantId)
    }
  }, [plantID])

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.aopRemarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveEditedData = async (newRows) => {
    setLoading(true)
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      let siteID =
        JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

      let verticalId = localStorage.getItem('verticalId')

      const businessData = newRows.map((row) => ({
        april: row.april || null,
        may: row.may || null,
        june: row.june || null,
        july: row.july || null,
        aug: row.aug || null,
        sep: row.sep || null,
        oct: row.oct || null,
        nov: row.nov || null,
        dec: row.dec || null,
        jan: row.jan || null,
        feb: row.feb || null,
        march: row.march || null,
        aopRemarks: row.aopRemarks || null,
        aopYear: localStorage.getItem('year'),
        plantFkId: plantId,
        siteFkId: siteID,
        verticalFkId: verticalId,
        materialFkId: row.NormParametersId,
        id: row.idFromApi || null,
        aopCaseId: '2025-26-NormsAOP',
        aopStatus: 'Saved',
      }))
      const response = await ConsumptionNormsApiService.saveAOPConsumptionNorm(
        plantId,
        businessData,
        keycloak,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Consumption AOP Saved Successfully!',
        severity: 'success',
      })
      //

      setLoading(false)
      setModifiedCells({})

      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
      fetchData(gradeId)
      dispatch(setIsBlocked(false))

      return response
    } catch (error) {
      console.error('Error saving Consumption AOP!', error)
    } finally {
      //
      setLoading(false)
    }
  }

  const saveChanges = React.useCallback(async () => {
    setLoading(true)

    setTimeout(() => {
      const lowerVertName = JSON.parse(
        localStorage.getItem('selectedVertical'),
      )?.name?.toLowerCase()

      if (lowerVertName == 'meg') {
        try {
          var data = Object.values(modifiedCells)
          if (data.length == 0) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: 'No Records to Save!',
              severity: 'info',
            })
            //
            setLoading(false)

            return
          }
          const requiredFields = ['aopRemarks']
          const validationMessage = validateFields(data, requiredFields)
          if (validationMessage) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: validationMessage,
              severity: 'error',
            })
            //
            setLoading(false)
            return
          }

          saveEditedData(data)
        } catch (error) {
          console.log('Error saving changes:', error)
          //
          setLoading(false)
        }
      }

      if (lowerVertName == 'pe' || lowerVertName == 'pp') {
        try {
          setLoading(true)

          var editedData = Object.values(modifiedCells)

          const requiredFields = ['aopRemarks']

          const validationMessage = validateFields(editedData, requiredFields)
          if (validationMessage) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: validationMessage,
              severity: 'error',
            })

            setLoading(false)
            return
          }

          if (calculatebtnClicked == false) {
            // if (editedData.length === 0) {
            //   setSnackbarOpen(true)
            //   setSnackbarData({
            //     message: 'No Records to Save!',
            //     severity: 'info',
            //   })
            //   setLoading(false)

            //   setCalculatebtnClicked(false)
            //   return
            // }
            //UNCOMMNET THIS IF saveBtn IS SET TO --> TRUE
            saveEditedData(editedData)

            // setLoading(false)
            setCalculatebtnClicked(false)
            // saveEditedData(editedData)
          } else {
            saveEditedData(editedData)
          }
        } catch (error) {
          setLoading(false)
          console.log('Error saving changes:', error)
          setCalculatebtnClicked(false)
        }
      }
    }, 400)
  }, [apiRef, selectedUnit, modifiedCells, calculatebtnClicked])

  const fetchGradeDropdowns = async () => {
    try {
      setGrades([])
      const response =
        await ConsumptionNormsApiService.getConsumptionAOPNormsGrades(keycloak)

      if (response?.code == 200) {
        setGrades(response?.data)
      }

      fetchData(gradeId)
    } catch (error) {
      setGrades([])
      console.error('Error fetching Business Demand data:', error)
    }
  }

  const fetchGradeDropdownsAfterCalc = async () => {
    try {
      setGrades([])
      const response =
        await ConsumptionNormsApiService.getConsumptionAOPNormsGrades(keycloak)

      if (response?.code == 200) {
        setGrades(response?.data)
      }

      if (response?.data?.length === 0) {
        // no grades — clear selection and fetch blank data
        setGradeId(null)
        await fetchData(null)
        return
      }

      const firstGrade = response?.data[0]
      const firstId =
        firstGrade?.id ?? firstGrade?.gradeId ?? firstGrade?.gradeFkId ?? null

      setGradeId(firstId)

      fetchData(firstId)
    } catch (error) {
      setGrades([])
      console.error('Error fetching Business Demand data:', error)
    }
  }

  const fetchData = async (gradeId) => {
    if ((lowerVertName === 'pe' || lowerVertName === 'pp') && !gradeId) return
    setLoading(true)
    try {
      var response
      if (lowerVertName === 'pe' || lowerVertName === 'pp') {
        response = await ConsumptionNormsApiService.getConsumptionNormsData(
          keycloak,
          gradeId,
        )
      } else {
        response = await ConsumptionNormsApiService.getConsumptionNormsData(
          keycloak,
          null,
        )
      }

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
      setCalculationObject(response?.data?.aopCalculation)
      const formattedData = response?.data?.aopConsumptionNormDTOList?.map(
        (item, index) => ({
          ...item,
          idFromApi: item.id,
          NormParametersId: item.materialFkId.toLowerCase(),
          originalRemark: item.aopRemarks?.trim() || null,
          id: index,
          isEditable: false,
          Particulars: item.normParameterTypeDisplayName,
        }),
      )
      setRows(formattedData)
      setLoading(false)
      setCalculatebtnClicked(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
      setCalculatebtnClicked(false)
    }
  }

  // const getAopSummary = async () => {
  //   setLoading(true)
  //   try {
  //     var res = await ConsumptionNormsApiService.getAopSummary(keycloak)

  //     if (res?.code == 200) {
  //       setSummary(res?.data?.summary)
  //     } else {
  //       setSummary('')
  //     }

  //     setLoading(false)
  //     setCalculatebtnClicked(false)
  //   } catch (error) {
  //     console.error('Error fetching data:', error)
  //     setLoading(false)
  //     setCalculatebtnClicked(false)
  //   }
  // }

  useEffect(() => {
    fetchData(gradeId)
    if (lowerVertName === 'pe' || lowerVertName === 'pp') {
      fetchGradeDropdowns()
    }
  }, [plantID, oldYear, yearChanged, keycloak, selectedUnit, gradeId])

  const productionColumns = getEnhancedColDefs({
    headerMap,
  })

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  const handleCalculate = () => {
    handleCalculateMeg()
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
      const data =
        await ConsumptionNormsApiService.handleCalculateonsumptionNorms(
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

        if (lowerVertName === 'pe' || lowerVertName === 'pp') {
          fetchGradeDropdownsAfterCalc()
        } else {
          fetchData(null)
        }
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

  const defaultCustomHeight = { mainBox: '55vh', otherBox: '112%' }

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
      showCalculate: true,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      units: ['TPH', 'TPD'],
      saveWithRemark: true,
      saveBtn: false,
      showCalculate: true,
      allAction: true,
      showCalculateVisibility:
        Object.keys(calculationObject || {}).length > 0 ? true : false,
      showRefresh: false,
      noColor: false,
      customHeight: defaultCustomHeight,
      showG: lowerVertName === 'pe' || lowerVertName === 'pp' ? true : false,
      dropdownLabel: 'Select Grade',
      downloadExcelBtnFromUI: true,
      // ExcelName: `${lowerVertName}${gradeId ? `_${gradeId}` : ''}_Overall AOP Consumption`,
      ExcelName: `${lowerVertName}_Overall AOP Consumption`,
      isHeight: lowerVertName !== 'meg' && rows?.length > 10,
    },
    isOldYear,
  )

  const handleGradeChange = (gradeId) => {
    setGradeId(gradeId)
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <div>
        {true && (
          // <CustomAccordion
          //   defaultExpanded
          //   disableGutters
          //   onChange={handleAccordionChange}
          // >
          // <CustomAccordionSummary
          //   aria-controls='meg-grid-content'
          //   id='meg-grid-header'
          // >
          // <Typography component='span' className='grid-title'>
          //   Consumption AOP
          // </Typography>
          // </CustomAccordionSummary>
          // <CustomAccordionDetails>
          <Box
            sx={{
              width: '100%',
              padding: '0px ',
              margin: '0px',
              backgroundColor: '#F2F3F8',
              borderRadius: 0,
              borderBottom: 'none',
            }}
          >
            <KendoDataTables
              autoHeight={true}
              modifiedCells={modifiedCells}
              setModifiedCells={setModifiedCells}
              columns={productionColumns}
              rows={rows}
              setRows={setRows}
              getRowId={(row) => row.id}
              title='Consumption AOP'
              paginationOptions={[100, 200, 300]}
              saveChanges={saveChanges}
              snackbarData={snackbarData}
              snackbarOpen={snackbarOpen}
              apiRef={apiRef}
              open1={open1}
              setOpen1={setOpen1}
              setSnackbarOpen={setSnackbarOpen}
              setSnackbarData={setSnackbarData}
              handleCalculate={handleCalculate}
              handleRemarkCellClick={handleRemarkCellClick}
              fetchData={fetchData}
              handleUnitChange={handleUnitChange}
              remarkDialogOpen={remarkDialogOpen}
              setRemarkDialogOpen={setRemarkDialogOpen}
              currentRemark={currentRemark}
              setCurrentRemark={setCurrentRemark}
              currentRowId={currentRowId}
              permissions={adjustedPermissions}
              groupBy='Particulars'
              grades={grades}
              handleGradeChange={handleGradeChange}
              calculatebtnClicked={calculatebtnClicked}
            />
          </Box>
          // </CustomAccordionDetails>
          // </CustomAccordion>
        )}
      </div>
    </div>
  )
}

export default ConsumptionNorms
