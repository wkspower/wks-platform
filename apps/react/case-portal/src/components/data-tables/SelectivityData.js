import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import React, { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
// Import the catalyst options from the JSON file
// import catalystOptionsData from '../../assets/Catalyst.json'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
import getEnhancedAOPColDefs from './CommonHeader/ConfigHeader'
// import getEnhancedAOPColDefs from './CommonHeader/ProductionAopHeader'

const SelectivityData = (props) => {
  const headerMap = generateHeaderNames()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const keycloak = useSession()
  // const [csData, setCsData] = useState([])
  // const [allProducts, setAllProducts] = useState([])
  // const [allCatalyst, setAllCatalyst] = useState([])

  const [loading, setLoading] = useState(false)

  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)

  // const [rows, setRows] = useState()
  // const [rows2, setRows2] = useState()

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  // States for the Remark Dialog
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
    console.log(newRow, 'test1923888', oldRow)
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

      console.log(data)
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

      const turnAroundDetails = newRow.map((row) => ({
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
        turnAroundDetails,
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
        // Iterate over each key in the row
        Object.keys(row).forEach((key) => {
          // Skip non-grade keys
          if (
            key === 'id' ||
            key === 'receipeName' ||
            key === 'reciepeFkId' ||
            key === 'grades'
          ) {
            return
          }
          // If the key exists in the nested grades object
          if (row.grades && row.grades[key]) {
            // Use the updated value from the top-level key (e.g., "200" or "500")
            const updatedValue = row[key]
            // Optionally, you can update the nested object here
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

  const handleDeleteClick = async (id, params) => {
    try {
      const maintenanceId =
        id?.maintenanceId ||
        params?.row?.idFromApi ||
        params?.row?.maintenanceId ||
        params?.NormParameterMonthlyTransactionId

      // console.log(maintenanceId, params, id)

      // Ensure UI state updates before the deletion process
      setOpen1(true)
      setDeleteId(id)

      // Perform the delete operation
      const resp = await DataService.deleteBusinessDemandData(
        maintenanceId,
        keycloak,
      )
      if (props?.configType !== 'grades') {
        props.fetchData()
      }
      return resp
    } catch (error) {
      console.error(`Error deleting Configuration data:`, error)
    } finally {
      // props.fetchData()
      setOpen1(false)
    }
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

    getAllProducts()
    // getAllCatalyst()
    if (props?.configType !== 'grades') {
      props.fetchData()
    }
    if (props?.configType === 'grades') fetchConfigData()
  }, [sitePlantChange, keycloak, lowerVertName])

  // const transformGradeValue = (data) => {
  //   return data.map((item, index) => {
  //     const { attributeValue, gradeName, ...rest } = item
  //     return {
  //       id: index,
  //       ...rest,
  //       gradeName,
  //       [gradeName]: attributeValue, // dynamic key
  //     }
  //   })
  // }
  const generateDynamicColumns = (data) => {
    const columns = [
      {
        field: 'receipeName',
        headerName: 'Grade',
        editable: true,
        minWidth: 120,
        flex: 1,
      },
    ]

    const uniqueGradeNames = [...new Set(data.map((item) => item.gradeName))]

    uniqueGradeNames.forEach((grade) => {
      columns.push({
        field: grade,
        headerName: grade,
        editable: true,
        align: 'left',
        headerAlign: 'left',
      })
    })

    return columns
  }
  const [columnConfig, setColumnConfig] = useState([])
  const fetchConfigData = async () => {
    setLoading(true)
    try {
      const data = await DataService.getPeConfigData(keycloak)
      const data1 = [
        {
          id: 1,
          gradeName: 'F19010',
          receipeName: '',
          gradeFkId: '1AC76D49-D113-4FF0-9516-9F9E96D85DAE',
          reciepeFkId: '2AE40205-F960-4A57-975A-4598664E7F71',
          attributeValue: 104,
        },
        {
          id: 2,
          gradeName: 'E52007',
          receipeName: '',
          gradeFkId: '7BB94524-FFE3-4D04-8CAC-972047D8AD2F',
          reciepeFkId: '2AE40205-F960-4A57-975A-4598664E7F71',
          attributeValue: 100,
        },
      ]
      setColumnConfig(generateDynamicColumns(data1))
      // console.log(columnConfig)

      // console.log(data)
      // For 'meg', simply map items without grouping.
      // const formattedData = data.map((item, index) => ({
      //   ...item,
      //   // idFromApi: item.id,
      //   id: index,
      // }))
      // const formattedData = transformGradeValue(data)
      // console.log(formattedData)
      const data2 = groupRowsByReceipe(data1)

      console.log(data2)
      props?.setRows(data2)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    } finally {
      setLoading(false)
    }
  }

  // const groupRowsByReceipe = (data) => {
  //   const grouped = {}

  //   data.forEach((item) => {
  //     const { reciepeFkId, gradeName, attributeValue, gradeFkId, receipeName } =
  //       item
  //     if (!grouped[reciepeFkId]) {
  //       grouped[reciepeFkId] = {
  //         reciepeFkId,
  //         receipeName,
  //         grades: {},
  //       }
  //     }
  //     grouped[reciepeFkId][gradeName] = { attributeValue, gradeFkId }
  //   })

  //   return Object.values(grouped).map((item, index) => ({
  //     id: index,
  //     ...item,
  //   }))
  // }
  const groupRowsByReceipe = (data) => {
    const grouped = {}

    data.forEach((item) => {
      const { reciepeFkId, gradeName, attributeValue, gradeFkId, receipeName } =
        item
      if (!grouped[reciepeFkId]) {
        grouped[reciepeFkId] = {
          reciepeFkId,
          receipeName,
          grades: {},
        }
      }
      grouped[reciepeFkId].grades[gradeName] = {
        gradeName, // now explicitly stored
        attributeValue,
        gradeFkId,
      }
    })

    // Convert the grouped object to an array of rows
    return Object.values(grouped).map((item, index) => ({
      id: index,
      ...item,
    }))
  }

  // console.log(props?.configType)
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
        // fetchData={props?.fetchData}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        deleteId={deleteId}
        open1={open1}
        handleDeleteClick={handleDeleteClick}
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
