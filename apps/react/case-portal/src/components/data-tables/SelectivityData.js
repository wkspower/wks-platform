import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import React, { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
import getEnhancedAOPColDefs from './CommonHeader/ConfigHeader'

const SelectivityData = (props) => {
  const headerMap = generateHeaderNames()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange } = dataGridStore
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

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }
    props.setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )
    return newRow
  }, [])

  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(unsavedChangesRef.current.unsavedRows)
      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }
      console.log(props?.configType)
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
  }, [apiRef])

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
        apr: row.apr || null,
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
        normParameterFKId: row.normParameterFKId,
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
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
        setLoading(false)

        if (props?.configType !== 'grades') {
          props.fetchData()
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
      const payload = []
      updatedRows.forEach((row) => {
        Object.keys(row).forEach((key) => {
          if (
            key === 'id' ||
            key === 'receipeName' ||
            key === 'reciepeFkId' ||
            key === 'grades'
          ) {
            return
          }
          if (row.grades && row.grades[key]) {
            const updatedValue = row[key]
            row.grades[key].attributeValue = updatedValue
            payload.push({
              gradeName: row.grades[key].gradeName,
              receipeName: row.receipeName,
              gradeFkId: row.grades[key].gradeFkId,
              reciepeFkId: row.reciepeFkId,
              attributeValue: parseInt(updatedValue),
            })
          }
        })
      })
      console.log(payload)
      const response = await DataService.updatePeConfigData(keycloak, payload)
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Update successful!',
          severity: 'success',
        })
        fetchConfigData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Update failed!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error updating data:', error)
    } finally {
      setLoading(false)
    }
  }

  const isCellEditable = (params) => {
    return !(
      params.row.Particulars ||
      params.row.isGroupHeader ||
      params.row.isSubGroupHeader
    )
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak, null)
        const productList = data.map((product) => ({
          id: product.id,
          displayName: product.displayName,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }
    const getAllGradesReciepes = async () => {
      try {
        const data = await DataService.getAllGradesReciepes(keycloak)
        const productList = data.map((product) => ({
          id: product.id,
          displayName: product.displayName,
        }))
        setAllGradesReciepes(productList)
      } catch (error) {
        console.error('Error fetching Grades/Reciepes:', error)
      } finally {
        // handleMenuClose();
      }
    }

    getAllProducts()
    getAllGradesReciepes()
    // getAllCatalyst()
    if (props?.configType !== 'grades') {
      props.fetchData()
    }
    if (props?.configType === 'grades') fetchConfigData()
  }, [sitePlantChange, keycloak, lowerVertName])

  const [columnConfig, setColumnConfig] = useState([])

  // setColumnConfig()

  const fetchConfigData = async () => {
    setLoading(true)
    try {
      var data1 = await DataService.getPeConfigData(keycloak)
      data1 = [
        {
          id: 0,
          receipeName: 'Recipe A',
          F19010: 10.5,
          E52007: 12.0,
          P15807: 5.5,
          M24300: 7.25,
          M60075: 8.0,
          gradeFkId: '445D935C-D3A6-4AD4-99E3-67FED285E665',
          reciepeFkId: '2AE40205-F960-4A57-975A-4598664E7F71',
        },
        {
          id: 1,
          receipeName: 'Recipe B',
          F19010: 9.0,
          E52007: 13.5,
          P15807: 6.0,
          M24300: 7.75,
          M60075: 8.5,
          gradeFkId: '445D935C-D3A6-4AD4-99E3-67FED285E665',
        },
        {
          id: 2,
          receipeName: 'Recipe C',
          F19010: 11.0,
          E52007: 12.5,
          P15807: 5.0,
          M24300: 6.5,
          M60075: 9.0,
          reciepeFkId: '2AE40205-F960-4A57-975A-4598664E7F71',
        },
      ]
      props?.setRows(data1)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    } finally {
      setLoading(false)
    }
  }

  const productionColumns = getEnhancedAOPColDefs({
    allProducts,
    headerMap,
    handleRemarkCellClick,
    configType: props?.configType,
    columnConfig,
  })

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <ASDataGrid
        columns={productionColumns}
        rows={props?.rows}
        setRows={props?.setRows}
        title='Configuration'
        isCellEditable={isCellEditable}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
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
        unsavedChangesRef={unsavedChangesRef}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: false,
          saveWithRemark: true,
          saveBtn: true,
          customHeight:
            lowerVertName === 'meg' ? undefined : props.defaultCustomHeight,
        }}
      />
    </div>
  )
}

export default SelectivityData
