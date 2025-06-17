import { DataService } from 'services/DataService'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { validateFields } from 'utils/validationUtils'
import {
  Box,
  Button,
  TextField,
  Typography,
} from '../../../node_modules/@mui/material/index'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import KendoDataTables from './index'

const SelectivityData = (props) => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const [allGradesReciepes, setAllGradesReciepes] = useState(null)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [allProducts, setAllProducts] = useState([])
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [isEdited, setIsEdited] = useState(false)

  const [summary, setSummary] = useState('')
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const saveChanges = React.useCallback(async () => {
    setTimeout(() => {
      try {
        var data = Object.values(modifiedCells)
        if (data.length === 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          setSnackbarOpen(true)
          return
        }

        // console.log(props?.configType)
        if (props?.configType !== 'grades') {
          const requiredFields = ['remarks']
          const validationMessage = validateFields(data, requiredFields)
          if (validationMessage) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: validationMessage,
              severity: 'error',
            })
            return
          }
          saveCatalystData(data)
        } else {
          handleUpdate(data)
        }
      } catch (error) {
        // Handle error if necessary
      }
    }, 400)
  }, [modifiedCells])

  const saveCatalystData = async (newRow) => {
    setLoading(true)
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const payload = newRow.map((row) => ({
        apr: row.apr || row.ConstantValue || null,
        may: row.may || null,
        jun: row.jun || null,
        jul: row.jul || null,
        aug: row.aug || null,
        sep: row.sep || null,
        oct: row.oct || null,
        nov: row.nov || null,
        dec: row.dec || null,
        jan: row.jan || null,
        feb: row.feb || null,
        mar: row.mar || null,
        UOM: '',
        auditYear: localStorage.getItem('year'),
        normParameterFKId: row.normParameterFKId || row.NormParameter_FK_Id,
        remarks: row.remarks,
        id: row.idFromApi || null,
      }))

      const response = await DataService.saveCatalystData(
        plantId,
        payload,
        keycloak,
      )
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Configuration data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        setLoading(false)

        if (props?.configType !== 'grades' && lowerVertName !== 'cracker') {
          props?.fetchData()
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving Configuration data:', error)
      setLoading(false)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }

  const handleUpdate = async (updatedRows) => {
    setLoading(true)
    try {
      const payload = updatedRows.map((row) => ({
        recId: row.Reciepe_FK_ID.toString(),
        grades: Object.entries(row)
          .filter(([key]) => /^[0-9A-Fa-f-]{36}$/.test(key))
          .reduce((acc, [key, value]) => {
            acc[key] = Number(value)
            return acc
          }, {}),
      }))

      if (payload.length > 0) {
        const response = await DataService.updatePeConfigData(keycloak, payload)
        if (response) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Configuration Data Saved Successfully!',
            severity: 'success',
          })
          fetchConfigData()
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Configuration Data Saved failed!',
            severity: 'error',
          })
        }

        return response
      }
    } catch (error) {
      console.error('Error updating data:', error)
    } finally {
      setLoading(false)
    }
  }

  const isCellEditable = (params) => {
    if (lowerVertName != 'meg') {
      return !(
        params.row.Particulars ||
        params.row.isGroupHeader ||
        params.row.isSubGroupHeader
      )
    } else {
      return params.row.isEditable
    }
  }

  // const isCellEditable = (params) => {
  // }

  useEffect(() => {
    const getAllGrades = async () => {
      try {
        const data = await DataService.getAllGrades(keycloak)
        setAllGradesReciepes(data)
      } catch (error) {
        console.error('Error fetching Grades/Reciepes:', error)
      } finally {
        // handleMenuClose();
      }
    }

    if (verticalChange?.selectedVertical === 'PE') getAllGrades()

    // getAllCatalyst()
    if (props?.configType !== 'grades' && lowerVertName !== 'cracker') {
      props?.fetchData()
    }
    if (props?.configType === 'grades') fetchConfigData()
  }, [
    sitePlantChange,
    oldYear,
    yearChanged,
    keycloak,
    lowerVertName,
    props?.configType,
  ])

  const fetchConfigData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getPeConfigData(keycloak)

      data = data.map((item, index) => ({
        ...item,
        id: index,
      }))

      props?.setRows(data)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    } finally {
      setLoading(false)
    }
  }

  const productionColumns = getEnhancedAOPColDefs({
    allGradesReciepes,
    allProducts,
    headerMap,
    handleRemarkCellClick,
    configType: props?.configType,
    // columnConfig,
  })

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
      downloadExcelBtn: false,
      uploadExcelBtn: false,
      isOldYear: isOldYear,
      allAction: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: true,
      saveBtn: true,
      downloadExcelBtn: lowerVertName == 'meg' ? true : false,
      uploadExcelBtn: lowerVertName == 'meg' ? true : false,
      allAction: true,
    },
    isOldYear,
  )
  const NormParameterIdCell = (props) => {
    const productId = props.dataItem.normParameterFKId
    const product = allProducts.find((p) => p.id === productId)
    const displayName = product?.displayName || ''
    return <td>{displayName ? displayName : props?.dataItem?.particulars}</td>
  }

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
      await DataService.getConfigurationExcel(keycloak)

      // If no error is thrown, the request was successful
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

  useEffect(() => {
    getAopSummary()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const saveExcelFile = async (rawFile) => {
    setLoading(true)
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const response = await DataService.saveConfigurationExcel(
        rawFile,
        keycloak,
      )
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Configuration data Upload Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        setLoading(false)

        if (props?.configType !== 'grades' && lowerVertName !== 'cracker') {
          props?.fetchData()
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving Configuration data:', error)
      setLoading(false)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }

  // console.log('loading', loading)

  const saveSummary = async () => {
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      let year = localStorage.getItem('year')
      const response = await DataService.saveSummaryAOPConsumptionNorm(
        plantId,
        year,
        summary,
        keycloak,
      )

      if (response?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Summary Saved Successfully!',
          severity: 'success',
        })
        setIsEdited(false)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Summary Saved Failed!',
          severity: 'error',
        })
      }

      //

      return response
    } catch (error) {
      console.error('Error saving Summary!', error)
    } finally {
      //
    }
  }

  const getAopSummary = async () => {
    try {
      var res = await DataService.getAopSummary(keycloak)

      if (res?.code == 200) {
        setSummary(res?.data?.summary)
      } else {
        setSummary('')
      }
    } catch (error) {
      console.error('Error fetching data:', error)
    }
  }

  const handleSave = () => {
    saveSummary()
  }

  return (
    <div>
      <Box>
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading}
        >
          <CircularProgress color='inherit' />
        </Backdrop>
        <KendoDataTables
          handleRemarkCellClick={handleRemarkCellClick}
          NormParameterIdCell={NormParameterIdCell}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          columns={productionColumns}
          rows={props?.rows}
          setRows={props?.setRows}
          title='Configuration'
          isCellEditable={isCellEditable}
          paginationOptions={[100, 200, 300]}
          saveChanges={saveChanges}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          apiRef={apiRef}
          setDeleteId={setDeleteId}
          setOpen1={setOpen1}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          deleteId={deleteId}
          open1={open1}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          permissions={adjustedPermissions}
          groupBy={props?.groupBy}
          handleExcelUpload={handleExcelUpload}
          downloadExcelForConfiguration={downloadExcelForConfiguration}
        />
      </Box>
      <div>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mt: '25px',
            ml: '5px',
          }}
        >
          <Typography component='div' sx={{ fontWeight: 'bold' }}>
            AOP Summary
          </Typography>

          {isOldYear !== 1 && (
            <Button
              variant='contained'
              className='btn-save'
              onClick={handleSave}
              disabled={!isEdited}
            >
              Save
            </Button>
          )}
        </Box>

        <TextField
          label='Summary'
          multiline
          // minRows={isAccordionExpanded ? 4 : 20}
          minRows={4}
          fullWidth
          margin='normal'
          variant='outlined'
          disabled={isOldYear == 1}
          value={summary}
          onChange={(e) => {
            setSummary(e.target.value)
            setIsEdited(true)
          }}
          sx={{
            '& .MuiInputBase-root': {
              backgroundColor: '#ffffff',
              borderRadius: '8px',
              boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
              padding: '8px',
            },
            '& label': {
              fontSize: '1rem',
              color: '#666',
              lineHeight: '1.2',
              transform: 'translate(14px, 12px) scale(1)',
            },
            '& .MuiInputLabel-shrink': {
              transform: 'translate(14px, -6px) scale(0.75)',
            },
            '& .MuiOutlinedInput-notchedOutline': {
              borderColor: '#ccc',
            },
            '&:hover .MuiOutlinedInput-notchedOutline': {
              borderColor: '#999',
            },
            '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
              borderColor: '#1976d2',
            },
            '& .MuiInputBase-input': {
              resize: 'vertical',
            },
          }}
        />
      </div>
    </div>
  )
}

export default SelectivityData
