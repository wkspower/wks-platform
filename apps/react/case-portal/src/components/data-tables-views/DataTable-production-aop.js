import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

// import DataGridTable from 'components/data-tables/ASDataGrid'
// import { DataGrid } from '@mui/x-data-grid'
import {
  // Backdrop,
  Box,
  Typography,
  // CircularProgress,
} from '../../../node_modules/@mui/material/index'
import { remarkColumn } from 'components/Utilities/remarkColumn'
import ReportDataGrid from './ReportDataGrid'
import Notification from 'components/Utilities/Notification'
import { styled } from '@mui/material/styles'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'

const CustomAccordion = styled((props) => (
  <MuiAccordion disableGutters elevation={0} square {...props} />
))(() => ({
  position: 'unset',
  border: 'none',
  boxShadow: 'none',
  margin: '0px',
  '&:before': {
    display: 'none',
  },
}))
const CustomAccordionSummary = styled((props) => (
  <MuiAccordionSummary expandIcon={<ExpandMoreIcon />} {...props} />
))(() => ({
  backgroundColor: '#fff',
  padding: '0px 12px',
  minHeight: '40px',
  '& .MuiAccordionSummary-content': {
    margin: '8px 0',
  },
}))
const CustomAccordionDetails = styled(MuiAccordionDetails)(() => ({
  padding: '0px 0px 12px',
  backgroundColor: '#F2F3F8',
}))
const ProductionAopView = () => {
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [columns, setColumns] = useState([])
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  // remark dialog state
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [modifiedCells, setModifiedCells] = React.useState({})

  const formatValueToNoDecimals = (val) =>
    val && !isNaN(val) ? Math.round(val) : val

  function getNumericKeysInAllRows(data) {
    if (!Array.isArray(data) || data.length === 0) return []

    const keys = Object.keys(data[0])

    return keys.filter((key) =>
      data.every((row) => {
        const value = row[key]
        // The column is considered numeric if:
        // - It's a valid number (including empty values)
        return value === '' || !isNaN(Number(value))
      }),
    )
  }
  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    const updatedFields = []
    for (const key in newRow) {
      if (
        Object.prototype.hasOwnProperty.call(newRow, key) &&
        newRow[key] !== oldRow[key]
      ) {
        updatedFields.push(key)
      }
    }

    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )
    if (updatedFields.length > 0) {
      setModifiedCells((prevModifiedCells) => ({
        ...prevModifiedCells,
        [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
      }))
    }

    return newRow
  }, [])
  const fetchData = async () => {
    // setLoading(true)
    try {
      var data = await DataService.getWorkflowDataProduction(keycloak, plantId)
      var formattedRows = data.results.map((row, id) => {
        const newRow = { id }
        Object.entries(row).forEach(([key, val]) => {
          if (!isNaN(val) && val !== '') {
            newRow[key] = formatValueToNoDecimals(val)
          } else {
            newRow[key] = val
          }
        })
        return newRow
      })

      formattedRows = formattedRows?.map((item) => ({
        ...item,
        isEditable: false,
      }))

      setRows(formattedRows)

      const results = data?.results
      const numericKeys = getNumericKeysInAllRows(results)

      // const generateColumns = ({ headers, keys }) => {
      //   return headers.map((header, idx) => {
      //     const key = keys[idx]
      //     return {
      //       field: key,
      //       headerName: header,
      //       flex: 1,
      //       ...(idx === 0 && {
      //         renderHeader: (params) => <div>{params.colDef.headerName}</div>,
      //       }),
      //       ...(numericKeys.includes(key) && { align: 'right' }),
      //     }
      //   })

      // }
      const handleRemarkCellClick = async (row) => {
        // do not delete commented code
        // try {
        //   const cases = await DataService.getCaseId(keycloak)
        //   console.log(cases?.workflowList?.length)
        //   if (cases?.workflowList?.length !== 0) return
        setCurrentRemark(row.remark || '')
        setCurrentRowId(row.id)
        setRemarkDialogOpen(true)
        // } catch (err) {
        //   console.error('Error fetching case', err)
        // }
      }
      const generateColumns = ({ headers, keys }) => {
        const cols = headers.map((header, idx) => {
          const key = keys[idx]
          return {
            field: key,
            headerName: header,
            flex: 1,
            ...(idx === 0 && {
              renderHeader: (params) => <div>{params.colDef.headerName}</div>,
            }),
            ...(numericKeys.includes(key) && { align: 'right' }),
          }
        })

        const remarkIdx = cols.findIndex((col) => col.field === 'remark')
        if (remarkIdx !== -1) {
          cols[remarkIdx] = remarkColumn(handleRemarkCellClick)
        }

        return cols
      }

      setColumns(generateColumns(data))
    } catch (error) {
      console.error('Error fetching data:', error)
      setRows([])
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  // const lastColumnField = columns[columns.length - 1]?.field
  const saveWorkflowData = async () => {
    try {
      // console.log(rows, 'workflowDto')
      const response = await DataService.saveAnnualWorkFlowData(
        keycloak,
        rows,
        plantId,
      )
      // console.log(response, 'response')
      setSnackbarData({
        message: 'Data Saved Successfully!',
        severity: 'success',
      })
      // setActionDisabled(true)
      // getCaseId()
    } catch (err) {
      console.error('Error while save', err)
      setSnackbarData({ message: err.message, severity: 'error' })
      // setActionDisabled(false)
    } finally {
      setSnackbarOpen(true)
      // setOpenRejectDialog(false)
      // setText('')
    }
  }
  return (
    <Box
      sx={{
        height: 'auto',
        width: '100%',
        padding: '0px 0px',
        margin: '0px 0px 0px',
        backgroundColor: '#F2F3F8',
        borderRadius: 0,
        borderBottom: 'none',
      }}
    >
      <div>
        <CustomAccordion defaultExpanded disableGutters>
          <CustomAccordionSummary
            aria-controls='meg-grid-content'
            id='meg-grid-header'
          >
            <Typography component='span' className='grid-title'>
              Production Data
            </Typography>
          </CustomAccordionSummary>
          <CustomAccordionDetails>
            {/* <Box> */}
            <ReportDataGrid
              rows={rows}
              loading={loading}
              setRows={setRows}
              title='Monthwise Production Summary'
              columns={columns}
              saveWorkflowData={saveWorkflowData}
              permissions={{
                // customHeight: defaultCustomHeightGrid1,
                textAlignment: 'center',
                remarksEditable: true,
                saveBtn: true,
                allAction: false,
                // showCalculate: true,
                // showWorkFlowBtns: true,
              }}
              treeData
              getTreeDataPath={(rows) => rows.path}
              defaultGroupingExpansionDepth={1} // expand only first level by default
              disableSelectionOnClick
              // columnGroupingModel={columnGroupingModel}
              processRowUpdate={processRowUpdate}
              remarkDialogOpen={remarkDialogOpen}
              unsavedChangesRef={unsavedChangesRef}
              setRemarkDialogOpen={setRemarkDialogOpen}
              currentRemark={currentRemark}
              setCurrentRemark={setCurrentRemark}
              currentRowId={currentRowId}
              setCurrentRowId={setCurrentRowId}
              modifiedCells={modifiedCells}
            />
            {/* </Box> */}
          </CustomAccordionDetails>
        </CustomAccordion>
      </div>
      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}

export default ProductionAopView
