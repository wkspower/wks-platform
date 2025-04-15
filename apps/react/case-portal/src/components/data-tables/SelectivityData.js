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

  const [rowModesModel, setRowModesModel] = useState({})
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

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
    const rowsInEditMode = Object.keys(rowModesModel).filter(
      (id) => rowModesModel[id]?.mode === 'edit',
    )

    rowsInEditMode.forEach((id) => {
      apiRef.current.stopRowEditMode({ id })
    })
    setTimeout(() => {
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
  }, [apiRef, rowModesModel])

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

    getAllProducts()
    getAllGrades()
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
      var data = await DataService.getPeConfigData(keycloak)

      data = data.map((item, index) => ({
        ...item,
        id: index,
      }))

      // data = [
      //   {
      //     id: 1,
      //     Reciepe_FK_ID: 12345,
      //     ReceipeName: 'Reciepe 01',
      //     AOPYear: '2025-26',
      //     '91278FC9-6554-4F80-B52B-05A5F8AC1B42': 100,
      //     '54E12CC7-6306-4E36-BEE5-0D97FC3BABCE': 120,
      //     '34D6DCB6-31E6-4D8D-B2F6-112649E23737': 120,
      //     '6481FB2E-373F-4CB5-8D1C-15E80D187060': 120,
      //     '23E76D93-7804-403F-A5E3-20FA11917505': 1020,
      //     '483EE917-9C42-4B16-8665-25CF39F7B454': 1020,
      //     '97FE0795-1900-43FD-A513-269BF965712C': 1020,
      //     '60733500-7F07-443B-B893-2FCA2EBD8744': 1020,
      //     'D1B0E1D0-50C7-429C-B545-4560DBB20A83': 1020,
      //     'EF022EA3-2A44-4B5C-BB3C-622B38DCA38C': 1020,
      //     '39C5CE0A-7C91-423F-BD8E-656B07B33002': 1020,
      //     '445D935C-D3A6-4AD4-99E3-67FED285E665': 1020,
      //     '9929136F-0CEF-402B-8FE9-825EC325E14E': 1020,
      //     '37D843AB-8066-4011-80CE-8E813A58A87A': 1020,
      //     '7BB94524-FFE3-4D04-8CAC-972047D8AD2F': 1020,
      //     '1AC76D49-D113-4FF0-9516-9F9E96D85DAE': 1020,
      //     '7744C9A0-7292-4D3E-A55C-B266EA2FAD3F': 1020,
      //     '051934D2-3C1B-47C3-8624-BA56018C22A3': 1020,
      //     '45657662-4EB2-4BF4-B529-E3175A754882': 1020,
      //     'EA9CE255-E8D2-4173-93C9-EEFA4BE0A0DA': 1020,
      //     '321E1892-7084-4918-AC47-F407F6363E47': 1020,
      //     '0F21B398-A787-48DA-B586-FA90E0E83D4E': 1020,
      //     'BC4DBDB9-349A-4755-818C-FDB213BE3596': 1020,
      //   },
      // ]

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
        rowModesModel={rowModesModel}
        onRowModesModelChange={onRowModesModelChange}
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
