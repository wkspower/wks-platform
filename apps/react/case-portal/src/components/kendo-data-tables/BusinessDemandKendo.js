import { Box } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import KendoDataGrid from 'components/Kendo-Data-Grid-Ankit/index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import React from 'react'
import { Grid, GridColumn as Column } from '@progress/kendo-react-grid'
import '@progress/kendo-theme-default/dist/all.css'
import { GridColumn } from '../../../node_modules/@progress/kendo-react-grid/index'
import { GridCell } from '@progress/kendo-react-grid'
import KendoDataTables from './index'

const BusinessDemandKendo = ({ permissions }) => {
  const keycloak = useSession()
  const [rows, setRows] = useState()
  const [allProducts, setAllProducts] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [loading, setLoading] = useState(false)
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [editedRows, setEditedRows] = useState([])

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const [modifiedCells, setModifiedCells] = React.useState({})
  const [rowModesModel, setRowModesModel] = useState({})

  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)

  const isOldYear = oldYear?.oldYear

  // const apiRef = useGridApiRef()
  // const [updatedRows, setUpdatedRows] = useState()
  // const [rows2, setRows2] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  useEffect(() => {}, [
    sitePlantChange,
    oldYear,
    yearChanged,
    keycloak,
    lowerVertName,
  ])

  const getAllProducts = async () => {
    try {
      var data = []
      if (lowerVertName == 'meg')
        data = await DataService.getAllProductsAll(
          keycloak,
          'BusinessDemandMEG',
        )
      else {
        data = await DataService.getAllProductsAll(keycloak, 'Production')
      }
      var productList = []
      if (lowerVertName === 'meg') {
        productList = data
      } else {
        productList = data.map((product) => ({
          id: product.id,
          displayName: product.displayName,
        }))
      }

      setAllProducts(productList)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const fetchData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getBDData(keycloak)
      const updatedData = data.map((item) => ({ ...item, inEdit: true }))

      setRows(updatedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    const fetchAllData = async () => {
      setLoading(true)
      const allFetches = [fetchData(), getAllProducts()]
      await Promise.all(allFetches)
      setLoading(false)
    }

    fetchAllData()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const NormParameterIdCell = (props) => {
    const productId = props.dataItem.normParameterId
    const product = allProducts.find((p) => p.id === productId)
    const displayName = product?.displayName || 'Unknown'

    return <td>{displayName}</td>
  }

  const colDefs = [
    {
      field: 'normParameterTypeName',
      headerName: 'Type',
      width: 150,
    },
    {
      field: 'normParameterId',
      title: 'Particulars',
      width: 150,
    },
    {
      field: 'UOM',
      headerName: 'UOM',
      width: 150,
    },
    {
      field: 'april',
      headerName: headerMap[4],
      align: 'left',
      headerAlign: 'left',
      width: 150,
    },
    {
      field: 'may',
      headerName: headerMap[5],

      align: 'left',
      headerAlign: 'left',
      width: 150,
    },
    {
      field: 'june',
      headerName: headerMap[6],

      align: 'left',
      headerAlign: 'left',
      width: 150,
    },
    {
      field: 'july',
      headerName: headerMap[7],

      align: 'left',
      headerAlign: 'left',
      width: 150,
    },
    {
      field: 'aug',
      headerName: headerMap[8],

      align: 'left',
      headerAlign: 'left',
      width: 150,
    },
    {
      field: 'sep',
      headerName: headerMap[9],

      align: 'left',
      headerAlign: 'left',
      width: 150,
    },

    {
      field: 'oct',
      headerName: headerMap[10],

      align: 'left',
      headerAlign: 'left',
      width: 150,
    },
    {
      field: 'nov',
      headerName: headerMap[11],

      align: 'left',
      headerAlign: 'left',
      width: 150,
    },
    {
      field: 'dec',
      headerName: headerMap[12],

      align: 'left',
      headerAlign: 'left',
      width: 150,
    },
    {
      field: 'jan',
      headerName: headerMap[1],

      align: 'left',
      headerAlign: 'left',
      width: 150,
    },
    {
      field: 'feb',
      headerName: headerMap[2],

      align: 'left',
      headerAlign: 'left',
      width: 150,
    },
    {
      field: 'march',
      headerName: headerMap[3],

      align: 'left',
      headerAlign: 'left',
      width: 150,
    },
    {
      field: 'remark',
      headerName: 'Remark',
      width: 150,
    },
  ]

  // const colDefs = getEnhancedColDefs({
  //   allProducts,
  //   headerMap,
  //   handleRemarkCellClick,
  // })

  const handleItemChange = (e) => {
    const updatedRows = [...rows]
    const index = updatedRows.findIndex((r) => r.id === e.dataItem.id)

    if (index !== -1) {
      updatedRows[index] = {
        ...updatedRows[index],
        [e.field]: e.value,
      }
      setRows(updatedRows)

      setEditedRows((prevEditedRows) => {
        const existingIndex = prevEditedRows.findIndex(
          (r) => r.id === e.dataItem.id,
        )
        let updatedEditedRows

        if (existingIndex !== -1) {
          updatedEditedRows = [...prevEditedRows]
          updatedEditedRows[existingIndex] = {
            ...updatedEditedRows[existingIndex],
            [e.field]: e.value,
          }
        } else {
          updatedEditedRows = [...prevEditedRows, updatedRows[index]]
        }
        return updatedEditedRows
      })
    }
  }

  const saveBusinessDemandData = async () => {
    try {
      let plantId = ''
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

      let verticalId = localStorage.getItem('verticalId')

      const businessData = editedRows?.map((row) => ({
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
        remark: row.remark || null,
        avgTph: row.avgTph || null,
        year: localStorage.getItem('year'),
        plantId: plantId,
        siteFKId: siteId,
        verticalFKId: verticalId,
        normParameterId: row.normParameterId,
        id: row.id || null,
      }))

      // console.log('businessData', businessData)

      const response = await DataService.saveBusinessDemandData(
        plantId,
        businessData,
        keycloak,
      )

      fetchData()
      return response
    } catch (error) {
      console.error('Error saving Business Demand data!', error)
    } finally {
      // fetchData()
    }
  }

  const saveChanges = React.useCallback(async () => {
    console.log('editedRows', editedRows)
    console.log('modifiedCells', modifiedCells)

    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      // saveBusinessDemandData(editedRows)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [rowModesModel, modifiedCells, editedRows])

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
    }
  }
  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? false,
      addButton: permissions?.addButton ?? false,
      deleteButton: permissions?.deleteButton ?? false,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? false,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      saveBtn: permissions?.saveBtn ?? true,
      units: ['TPH', 'TPD'],
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

      <Box display='flex' flexDirection='column' gap={2}>
        <div>
          <Box sx={{ width: '100%', margin: 0 }}>
            <KendoDataTables
              data={rows}
              dataItemKey='id'
              autoProcessData={true}
              sortable={true}
              scrollable='virtual'
              defaultSkip={0}
              filterable={true}
              filter={filter}
              defaultTake={10}
              onFilterChange={(e) => setFilter(e.filter)}
              onItemChange={handleItemChange}
              editable={true}
              resizable={true}
              modifiedCells={modifiedCells}
              setModifiedCells={setModifiedCells}
              setRows={setRows}
              columns={colDefs}
              NormParameterIdCell={NormParameterIdCell}
              // .map((col) =>
              //   col.field === 'normParameterId' ? (
              //     <GridColumn
              //       key={col.field}
              //       field={col.field}
              //       title={col.title || col.headerName}
              //       width={col.width}
              //       cells={{
              //         data: NormParameterIdCell,
              //       }}
              //     />
              //   ) : (
              //     <GridColumn
              //       key={col.field}
              //       field={col.field}
              //       title={col.title || col.headerName}
              //       width={col.width}
              //     />
              //   ),
              // )}
              rows={rows || []}
              title='Business Demand'
              saveChanges={saveChanges}
              snackbarData={snackbarData}
              snackbarOpen={snackbarOpen}
              setSnackbarOpen={setSnackbarOpen}
              setSnackbarData={setSnackbarData}
              fetchData={fetchData}
              remarkDialogOpen={remarkDialogOpen}
              setRemarkDialogOpen={setRemarkDialogOpen}
              currentRemark={currentRemark}
              setCurrentRemark={setCurrentRemark}
              currentRowId={currentRowId}
              setCurrentRowId={setCurrentRowId}
              permissions={adjustedPermissions}
            ></KendoDataTables>
          </Box>
        </div>
      </Box>
    </div>
  )
}

export default BusinessDemandKendo
