import '@progress/kendo-font-icons/dist/index.css'
import { Grid, GridColumn } from '@progress/kendo-react-grid'
import '@progress/kendo-theme-default/dist/all.css'
import React, { useCallback, useEffect, useRef, useState } from 'react'

import {
  Backdrop,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
  Typography,
} from '../../../node_modules/@mui/material/index'
import '../../kendo-data-grid.css'

import { ibrGridThreePopUP, singleRowColumn } from './columnDefs'

import Notification from 'components/Utilities/Notification'
import { SvgIcon } from '../../../node_modules/@progress/kendo-react-common/index'
import { trashIcon } from '../../../node_modules/@progress/kendo-svg-icons/dist/index'

import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import { styled } from '@mui/material/styles'
import { getColumnMenuCheckboxFilter } from 'components/data-tables/Reports-kendo/ColumnMenu1'
import {
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
} from '../../../node_modules/@progress/kendo-react-grid/index'
import { Tooltip } from '../../../node_modules/@progress/kendo-react-tooltip/index'

import { DatePicker } from '@progress/kendo-react-dateinputs'
import { Input } from '@progress/kendo-react-inputs'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { Skeleton } from '../../../node_modules/@progress/kendo-react-indicators/index'
import moment from '../../../node_modules/moment/moment'
import { ExcelExport } from '../../../node_modules/@progress/kendo-react-excel-export/index'
import { useSelector } from 'react-redux'

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

const year = localStorage.getItem('year')
const startYear = parseInt(year?.split('-')[0], 10)

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

export const dateFields1 = ['ibrSD', 'ibrED', 'taSD', 'taED', 'sdED', 'sdSD']
export const dateFieldsRunLength = ['date']

const KendoDataTablesCrackerRunLength = ({
  rows = [],

  modifiedCells = [],
  setRows,
  columns,
  loading = false,
  permissions = {},
  setRemarkDialogOpen = () => {},
  currentRemark = '',
  setCurrentRemark = () => {},
  currentRowId = null,
  setModifiedCells = () => {},
  remarkDialogOpen = false,
  handleDeleteSelected = () => {},
  saveChanges = () => {},
  deleteRowData = () => {},
  handleCalculate = () => {},
  selectedUsers = [],
  note = '',
  titleName = '',
  handleExcelUpload = () => {},
  downloadExcelForConfiguration = () => {},
}) => {
  const fileInputRef = useRef(null)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { yearChanged } = dataGridStore

  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const showDeleteAll = permissions?.deleteAllBtn && selectedUsers.length > 1
  const [selectedGrade, setSelectedGrade] = useState()
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  const [openSaveDialogeBoxSingleRow, setOpenSaveDialogeBoxSingleRow] =
    useState(false)
  const [paramsForDelete, setParamsForDelete] = useState([])
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)
  const closeSaveDialogeBoxSingleRow = () =>
    setOpenSaveDialogeBoxSingleRow(false)
  const [edit, setEdit] = useState({})
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [sort, setSort] = useState([])
  const [issRowEdited, setIsRowEdited] = useState(false)
  const ColumnMenuCheckboxFilter = getColumnMenuCheckboxFilter(rows)
  const [rowsPopUp, setRowsPopUp] = useState([])
  const ColumnMenuCheckboxFilter1 = getColumnMenuCheckboxFilter(rowsPopUp)
  const [singleRow, setSingleRow] = useState([])
  const [modifiedCellsSingleRow, setModifiedCellsSingleRow] = useState([])
  const [modifiedCellsDayWise, setModifiedCellsDayWise] = useState([])

  const keycloak = useSession()
  const [loading1, setLoading1] = useState(false)
  const [open, setOpen] = useState(false)
  const [startDate, setStartDate] = useState(null)

  const [hValues, setHValues] = useState({
    H10: '0',
    H11: '0',
    H12: '0',
    H13: '0',
    H14: '0',
    Demo: 'SAD',
  })
  const [snackbarOpen1, setSnackbarOpen1] = useState(false)
  const [snackbarData1, setSnackbarData1] = useState({
    message: '',
    severity: 'info',
  })

  const [lowerLimitDate, setLowerLimitDate] = useState(null)
  const [upperLimitDate, setUpperLimitDate] = useState(null)

  useEffect(() => {
    setHValues({})
    setRowsPopUp([])
    setSingleRow([])

    setStartDate(null)
  }, [yearChanged])

  useEffect(() => {
    const year = localStorage.getItem('year')
    const startYear = parseInt(year?.split('-')[0], 10)
    const lowerLimit = new Date(startYear, 3, 1)
    const upperLimit = new Date(startYear + 1, 2, 31)

    // console.log(lowerLimit)
    // console.log(upperLimit)

    setLowerLimitDate(lowerLimit)
    setUpperLimitDate(upperLimit)
  }, [yearChanged])

  const itemChange = useCallback(
    (e) => {
      const { dataItem, field, value } = e
      const itemId = dataItem.id

      // console.log('e', e)

      setRows((prevRows) =>
        prevRows.map((row) =>
          row.id === itemId ? { ...row, [field]: value } : row,
        ),
      )

      let updatedRows = []

      if (value?.toUpperCase() === 'SAD' && dataItem[field] !== 'SAD') {
        setTimeout(() => {
          setRows((prevRows) => {
            const editedIndex = prevRows.findIndex((r) => r.id === itemId)
            updatedRows = [...prevRows]
            const next = prevRows[editedIndex + 1]?.[field]
            const nextNext = prevRows[editedIndex + 2]?.[field]
            const isNextNonNumeric =
              editedIndex + 1 < prevRows.length && isNaN(Number(next))
            const isNextNextNonNumeric =
              editedIndex + 2 < prevRows.length && isNaN(Number(nextNext))

            if (isNextNonNumeric || isNextNextNonNumeric) {
              let anchorIndex = -1
              for (let i = editedIndex - 1; i >= 1; i--) {
                if (
                  prevRows[i][field] === 'SAD' &&
                  prevRows[i - 1]?.[field] === 'SAD'
                ) {
                  anchorIndex = i - 2
                  break
                }
              }

              let startValue = 0
              if (
                anchorIndex >= 0 &&
                !isNaN(Number(prevRows[anchorIndex][field]))
              ) {
                startValue = Number(prevRows[anchorIndex][field]) + 1
              }

              let counter = startValue

              updatedRows = prevRows.map((row, index) => {
                const updatedRow = { ...row }

                if (index > anchorIndex && index < editedIndex - 1) {
                  updatedRow[field] = counter++
                }

                if (
                  index <= editedIndex - 2 &&
                  prevRows[index][field] === 'SAD' &&
                  prevRows[index + 1]?.[field] === 'SAD'
                ) {
                  updatedRow.demo = 'SD'
                }
                if (
                  index <= editedIndex - 1 &&
                  prevRows[index - 1]?.[field] === 'SAD' &&
                  prevRows[index][field] === 'SAD'
                ) {
                  updatedRow.demo = 'SD'
                }
                if (
                  index <= editedIndex - 3 &&
                  prevRows[index + 1]?.[field] === 'SAD' &&
                  prevRows[index + 2]?.[field] === 'SAD'
                ) {
                  // updatedRow.demo = 'BBU'
                  updatedRow.demo = 'SD'
                }

                if (isNextNonNumeric) {
                  if (index === editedIndex - 2) {
                    updatedRow.demo = 'BBU'
                  }
                  if (index === editedIndex - 1) {
                    updatedRow[field] = 'SAD'
                    updatedRow.demo = 1
                  }
                  if (index === editedIndex) {
                    updatedRow[field] = 'SAD'
                    updatedRow.demo = 2
                  }

                  // Continue demo numbering: 3, 4, ... until a non-numeric is encountered
                  if (index > editedIndex) {
                    const currentDemo = prevRows[index]?.demo
                    if (!isNaN(Number(currentDemo))) {
                      updatedRow.demo = index - editedIndex + 2 // demo = 3, 4, 5...
                    } else {
                      // stop numbering when non-numeric encountered
                      updatedRow.demo = prevRows[index]?.demo
                    }
                  }
                }

                if (!isNextNonNumeric && isNextNextNonNumeric) {
                  if (index === editedIndex - 1) {
                    updatedRow.demo = 'BBU'
                  }
                  if (index === editedIndex) {
                    updatedRow[field] = 'SAD'
                    updatedRow.demo = 1
                  }
                  if (index === editedIndex + 1) {
                    updatedRow[field] = 'SAD'
                    updatedRow.demo = 2
                  }

                  // Continue demo numbering: 3, 4, ... until a non-numeric is encountered
                  if (index > editedIndex + 1) {
                    const currentDemo = prevRows[index]?.demo
                    if (!isNaN(Number(currentDemo))) {
                      updatedRow.demo = index - editedIndex + 1 // demo = 3, 4, 5...
                    } else {
                      // stop numbering when non-numeric encountered
                      updatedRow.demo = prevRows[index]?.demo
                    }
                  }
                }

                return updatedRow
              })
            } else {
              // console.log('SAD is Typed but cells are numeric')
              // console.log('updatedRows', updatedRows)
            }

            setModifiedCells(() => updatedRows)
            setIsRowEdited(true)
            return updatedRows
          })
        }, 150)
      } else {
        setModifiedCells((prev) => {
          const base = { ...dataItem, [field]: value }
          return { ...prev, [itemId]: base }
        })
      }
    },
    [setRows, setModifiedCells],
  )

  const itemChangeDayWise = useCallback(
    (e) => {
      const { dataItem, field, value } = e
      const itemId = dataItem.id

      setRowsPopUp((prevRows) =>
        prevRows.map((row) =>
          row.id === itemId ? { ...row, [field]: value } : row,
        ),
      )

      let updatedRows = []

      if (value?.toUpperCase() === 'SAD' && dataItem[field] !== 'SAD') {
        setTimeout(() => {
          setRowsPopUp((prevRows) => {
            const editedIndex = prevRows.findIndex((r) => r.id === itemId)
            updatedRows = [...prevRows]

            const next = prevRows[editedIndex + 1]?.[field]
            const nextNext = prevRows[editedIndex + 2]?.[field]

            const isNextNonNumeric =
              editedIndex + 1 < prevRows.length && isNaN(Number(next))
            const isNextNextNonNumeric =
              editedIndex + 2 < prevRows.length && isNaN(Number(nextNext))

            if (isNextNonNumeric || isNextNextNonNumeric) {
              let anchorIndex = -1
              for (let i = editedIndex - 1; i >= 1; i--) {
                if (
                  prevRows[i][field] === 'SAD' &&
                  prevRows[i - 1]?.[field] === 'SAD'
                ) {
                  anchorIndex = i - 2
                  break
                }
              }

              let startValue = 0
              if (
                anchorIndex >= 0 &&
                !isNaN(Number(prevRows[anchorIndex][field]))
              ) {
                startValue = Number(prevRows[anchorIndex][field]) + 1
              }

              let counter = startValue

              updatedRows = prevRows.map((row, index) => {
                const updatedRow = { ...row }

                if (index > anchorIndex && index < editedIndex - 1) {
                  updatedRow[field] = counter++
                }

                if (
                  index <= editedIndex - 2 &&
                  prevRows[index][field] === 'SAD' &&
                  prevRows[index + 1]?.[field] === 'SAD'
                ) {
                  updatedRow.demo = 'SD'
                }
                if (
                  index <= editedIndex - 1 &&
                  prevRows[index - 1]?.[field] === 'SAD' &&
                  prevRows[index][field] === 'SAD'
                ) {
                  updatedRow.demo = 'SD'
                }
                if (
                  index <= editedIndex - 3 &&
                  prevRows[index + 1]?.[field] === 'SAD' &&
                  prevRows[index + 2]?.[field] === 'SAD'
                ) {
                  // updatedRow.demo = 'BBU'
                  updatedRow.demo = 'SD'
                }

                if (isNextNonNumeric) {
                  if (index === editedIndex - 2) {
                    updatedRow.demo = 'BBU'
                  }
                  if (index === editedIndex - 1) {
                    updatedRow[field] = 'SAD'
                    updatedRow.demo = 1
                  }
                  if (index === editedIndex) {
                    updatedRow[field] = 'SAD'
                    updatedRow.demo = 2
                  }

                  // Continue demo numbering: 3, 4, ... until a non-numeric is encountered
                  if (index > editedIndex) {
                    const currentDemo = prevRows[index]?.demo
                    if (!isNaN(Number(currentDemo))) {
                      updatedRow.demo = index - editedIndex + 2 // demo = 3, 4, 5...
                    } else {
                      // stop numbering when non-numeric encountered
                      updatedRow.demo = prevRows[index]?.demo
                    }
                  }
                }

                if (!isNextNonNumeric && isNextNextNonNumeric) {
                  if (index === editedIndex - 1) {
                    updatedRow.demo = 'BBU'
                  }
                  if (index === editedIndex) {
                    updatedRow[field] = 'SAD'
                    updatedRow.demo = 1
                  }
                  if (index === editedIndex + 1) {
                    updatedRow[field] = 'SAD'
                    updatedRow.demo = 2
                  }

                  // Continue demo numbering: 3, 4, ... until a non-numeric is encountered
                  if (index > editedIndex + 1) {
                    const currentDemo = prevRows[index]?.demo
                    if (!isNaN(Number(currentDemo))) {
                      updatedRow.demo = index - editedIndex + 1 // demo = 3, 4, 5...
                    } else {
                      // stop numbering when non-numeric encountered
                      updatedRow.demo = prevRows[index]?.demo
                    }
                  }
                }

                return updatedRow
              })
            }

            setModifiedCellsSingleRow(() => updatedRows)
            return updatedRows
          })
        }, 150)
      } else {
        setModifiedCellsDayWise((prev) => {
          const base = { ...dataItem, [field]: value }
          return { ...prev, [itemId]: base }
        })
      }
    },
    [setRowsPopUp, setModifiedCellsDayWise],
  )
  const itemChangeSingleRow = useCallback(
    (e) => {
      const { dataItem, field, value } = e
      const itemId = dataItem.id

      setSingleRow((prevRows) =>
        prevRows.map((row) =>
          row.id === itemId ? { ...row, [field]: value } : row,
        ),
      )

      setModifiedCellsSingleRow((prev) => {
        const base = { ...dataItem, [field]: value }
        return { ...prev, [itemId]: base }
      })
    },
    [setSingleRow, setModifiedCellsSingleRow],
  )

  const handleOpen = () => {
    setOpen(true)
  }

  const handleChange = (key, value) => {
    setHValues((prev) => ({ ...prev, [key]: value }))
  }

  const handleRemarkSave = () => {
    setRows((prevRows) => {
      let updatedRow = null
      let keyToUpdate = ''
      const updatedRows = prevRows.map((row) => {
        if (row.id === currentRowId) {
          const keysToUpdate = ['aopRemarks', 'remarks', 'remark'].filter(
            (key) => key in row,
          )
          keyToUpdate = keysToUpdate[0] || 'remark'
          updatedRow = { ...row, [keyToUpdate]: currentRemark, inEdit: true }
          return updatedRow
        }
        return row
      })
      if (updatedRow) {
        setModifiedCells((prev) => ({
          ...prev,
          [updatedRow.id]: updatedRow,
        }))
      }
      return updatedRows
    })
    setRemarkDialogOpen(false)
  }
  const saveConfirmation = async () => {
    saveChanges()
    setOpenSaveDialogeBox(false)
    setEdit({})
  }
  const saveConfirmationSingleRow = async () => {
    handleSave()
    setOpenSaveDialogeBoxSingleRow(false)
  }
  const handleDeleteClick = async (params) => {
    setParamsForDelete(params)
    setOpenDeleteDialogeBox(true)
  }
  const deleteTheRecord = async () => {
    deleteRowData(paramsForDelete)
    setOpenDeleteDialogeBox(false)
  }
  const ActionsCell = ({ dataItem }) => {
    return (
      <td style={{ textAlign: 'center', verticalAlign: 'middle' }}>
        <SvgIcon
          onClick={() => handleDeleteClick(dataItem)}
          icon={trashIcon}
          themeColor='dark'
        />
      </td>
    )
  }
  const saveModalOpen = async () => {
    setIsButtonDisabled(true)
    setOpenSaveDialogeBox(true)
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }
  const saveModalOpenSingleRow = async () => {
    setOpenSaveDialogeBoxSingleRow(true)
  }

  const handleCalculateBtn = async () => {
    setIsButtonDisabled(true)
    handleCalculate()
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }

  const isColumnActive = (field, filter, sort) => {
    return (
      isColumnMenuFilterActive(field, filter) ||
      isColumnMenuSortActive(field, sort)
    )
  }

  const triggerFileUpload = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click()
    }
  }
  const onFileChange = (event) => {
    const file = event.target.files[0]
    if (!file) return

    handleExcelUpload(file)
    event.target.value = ''
  }

  const CellWithState = (props) => {
    const field = props.field || ''
    const [inEdit, setInEdit] = React.useState(false)
    const [value, setValue] = React.useState(props.dataItem[field])

    const handleChange = (event) => {
      setValue(event.target.value)
    }

    const handleBlur = (e) => {
      setInEdit(false)

      if (props.onChange) {
        props.onChange({
          dataItem: props.dataItem,
          field,
          value,
          syntheticEvent: e,
        })
      }
    }
    const handleKeyDown = (e) => {
      const isNumber = /^[0-9]$/.test(e.key)
      const isControl = [
        'Backspace',
        'ArrowLeft',
        'ArrowRight',
        'Tab',
        'Enter',
      ].includes(e.key)

      const currentValue = e.target.value

      if (!isNumber && !isControl) {
        if (currentValue.length >= 3 && /^[^0-9]+$/.test(currentValue)) {
          e.preventDefault()
        }
      }

      if (e.key === 'Enter') {
        e.preventDefault()
        handleBlur(e)
      }
    }

    if (inEdit) {
      return (
        <td {...props.tdProps}>
          <Input
            value={value}
            style={{ width: '100%' }}
            onChange={handleChange}
            onBlur={handleBlur}
            onKeyDown={handleKeyDown}
            autoFocus
          />
        </td>
      )
    }

    return (
      <td {...props.tdProps} onClick={() => setInEdit(true)}>
        {value}
      </td>
    )
  }

  const LoadingCell = (props) => {
    const field = props.field || ''
    if (props.dataItem[field] === undefined) {
      // shows loading cell if no data
      return (
        <td {...props.tdProps}>
          {' '}
          <Skeleton
            shape={'text'}
            style={{
              width: '100%',
            }}
          />
        </td>
      )
    } // default rendering for this cell

    return <td {...props.tdProps}>{props.children}</td>
  }

  const SimpleHeaderWithTooltip = (props) => {
    const { ariaSort, ...restThProps } = props.thProps || {}

    return (
      <th
        {...restThProps}
        aria-sort={ariaSort}
        title={props.title}
        style={{ padding: '0px' }}
      >
        <Tooltip
          position='top'
          anchorElement='target'
          parentTitle={true}
          className='test'
        >
          {props.children}
        </Tooltip>
      </th>
    )
  }
  const renderGrid = () => (
    <Grid
      style={{ height: 600 }}
      scrollable={'virtual'}
      rowHeight={40}
      data={rows}
      total={rows.length}
      sortable={{
        mode: 'multiple',
      }}
      sort={sort}
      defaultSkip={0}
      defaultTake={100}
      onItemChange={itemChange}
      dataItemKey='id'
      size='small'
      autoProcessData={true}
      cells={{
        data: LoadingCell,
      }}
      pageable={{
        buttonCount: 4,
        pageSizes: [10, 50, 100, 366],
      }}
    >
      {columns?.map((col) => {
        const isActive = isColumnActive(col.field, filter, sort)

        if (
          dateFields1.includes(col.field) ||
          dateFieldsRunLength.includes(col.field)
        ) {
          return (
            <GridColumn
              key={col.field}
              field={col.field}
              title={col.title || col.headerName}
              format='{0:dd-MM-yyyy}'
              editor='date'
              hidden={col.hidden}
              sortable={false}
              cells={{
                headerCell: SimpleHeaderWithTooltip,
              }}
              className={
                dateFieldsRunLength.includes(col.field)
                  ? 'k-right-disabled'
                  : ''
              }
            />
          )
        }

        if (!col.editable) {
          return (
            <GridColumn
              key={col.field}
              field={col.field}
              title={col.title || col.headerName}
              hidden={col.hidden}
              headerClassName={isActive ? 'active-column' : ''}
              columnMenu={col.filter ? ColumnMenuCheckboxFilter : undefined}
              sortable={!!col.filter}
              className={col.isDisabled ? 'k-right-disabled' : ''}
              cells={{
                headerCell: SimpleHeaderWithTooltip,
              }}
            />
          )
        }

        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            // width={col.widthT}
            hidden={col.hidden}
            headerClassName={isActive ? 'active-column' : ''}
            columnMenu={col.filter ? ColumnMenuCheckboxFilter : undefined}
            sortable={!!col.filter}
            className={col.isDisabled ? 'k-right-disabled' : ''}
            cells={
              col.editable
                ? { data: CellWithState, headerCell: SimpleHeaderWithTooltip }
                : { headerCell: SimpleHeaderWithTooltip }
            }
          />
        )
      })}

      {permissions?.deleteButton && (
        <GridColumn
          key='actions'
          field='actions'
          title='Action'
          width={80}
          className='k-text-center'
          filterable={false}
          editable={false}
          cells={{
            data: ActionsCell,
          }}
        />
      )}
    </Grid>
  )

  const renderGridDayWise = () => (
    <Tooltip openDelay={50} position='auto' anchorElement='target'>
      <ExcelExport
        data={rowsPopUp}
        ref={_export}
        fileName={`Cracker-runlength.xlsx`}
      >
        <Grid
          style={{ height: 630 }}
          scrollable={'virtual'}
          rowHeight={40}
          data={rowsPopUp}
          total={rowsPopUp?.length}
          sortable={{
            mode: 'multiple',
          }}
          sort={sort}
          defaultSkip={0}
          defaultTake={100}
          onItemChange={itemChangeDayWise}
          dataItemKey='id'
          size='small'
          autoProcessData={true}
          cells={{
            data: LoadingCell,
          }}
          pageable={{
            buttonCount: 4,
            pageSizes: [10, 50, 100, 366],
          }}
        >
          {ibrGridThreePopUP.map((col) => {
            const isActive = isColumnActive(col.field, filter, sort)

            if (
              dateFields1.includes(col.field) ||
              dateFieldsRunLength.includes(col.field)
            ) {
              return (
                <GridColumn
                  key={col.field}
                  field={col.field}
                  title={col.title || col.headerName}
                  format='{0:dd-MM-yyyy}'
                  editor='date'
                  hidden={col.hidden}
                  sortable={false}
                  cells={{
                    headerCell: SimpleHeaderWithTooltip,
                  }}
                  className={
                    dateFieldsRunLength.includes(col.field)
                      ? 'k-right-disabled'
                      : ''
                  }
                />
              )
            }

            if (!col.editable) {
              return (
                <GridColumn
                  key={col.field}
                  field={col.field}
                  title={col.title || col.headerName}
                  hidden={col.hidden}
                  headerClassName={isActive ? 'active-column' : ''}
                  columnMenu={
                    col.filter ? ColumnMenuCheckboxFilter1 : undefined
                  }
                  sortable={!!col.filter}
                  className={col.isDisabled ? 'k-right-disabled' : ''}
                  cells={{
                    headerCell: SimpleHeaderWithTooltip,
                  }}
                />
              )
            }

            return (
              <GridColumn
                key={col.field}
                field={col.field}
                title={col.title || col.headerName}
                // width={col.widthT}
                hidden={col.hidden}
                headerClassName={isActive ? 'active-column' : ''}
                columnMenu={col.filter ? ColumnMenuCheckboxFilter1 : undefined}
                sortable={!!col.filter}
                className={col.isDisabled ? 'k-right-disabled' : ''}
                cells={
                  col.editable
                    ? {
                        data: CellWithState,
                        headerCell: SimpleHeaderWithTooltip,
                      }
                    : { headerCell: SimpleHeaderWithTooltip }
                }
              />
            )
          })}

          {permissions?.deleteButton && (
            <GridColumn
              key='actions'
              field='actions'
              title='Action'
              width={80}
              className='k-text-center'
              filterable={false}
              editable={false}
              cells={{
                data: ActionsCell,
              }}
            />
          )}
        </Grid>
      </ExcelExport>
    </Tooltip>
  )

  const renderGridSingleRow = () => (
    <Grid
      scrollable={'virtual'}
      rowHeight={40}
      data={singleRow}
      sortable={{
        mode: 'multiple',
      }}
      sort={sort}
      onItemChange={itemChangeSingleRow}
      dataItemKey='id'
      size='small'
      autoProcessData={true}
    >
      {singleRowColumn.map((col) => {
        if (
          dateFields1.includes(col.field) ||
          dateFieldsRunLength.includes(col.field)
        ) {
          return (
            <GridColumn
              key={col.field}
              field={col.field}
              title={col.title || col.headerName}
              format='{0:dd-MM-yyyy}'
              editor='date'
              hidden={col.hidden}
              sortable={false}
              cells={{
                headerCell: SimpleHeaderWithTooltip,
              }}
              className={
                dateFieldsRunLength.includes(col.field)
                  ? 'k-right-disabled'
                  : ''
              }
            />
          )
        }

        if (!col.editable) {
          return (
            <GridColumn
              key={col.field}
              field={col.field}
              title={col.title || col.headerName}
              hidden={col.hidden}
              sortable={!!col.filter}
              className={col.isDisabled ? 'k-right-disabled' : ''}
              cells={{
                headerCell: SimpleHeaderWithTooltip,
              }}
            />
          )
        }

        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            // width={col.widthT}
            hidden={col.hidden}
            columnMenu={col.filter ? ColumnMenuCheckboxFilter : undefined}
            sortable={!!col.filter}
            className={col.isDisabled ? 'k-right-disabled' : ''}
            cells={
              col.editable
                ? { data: CellWithState, headerCell: SimpleHeaderWithTooltip }
                : { headerCell: SimpleHeaderWithTooltip }
            }
          />
        )
      })}

      {permissions?.deleteButton && (
        <GridColumn
          key='actions'
          field='actions'
          title='Action'
          width={80}
          className='k-text-center'
          filterable={false}
          editable={false}
          cells={{
            data: ActionsCell,
          }}
        />
      )}
    </Grid>
  )

  const handleCalculateData = (hValues) => {
    fetchDataNextYearCalculate(hValues, startDate)
  }

  const handleSave = () => {
    saveCrackerRunLength(singleRow)
  }

  const saveCrackerRunLength = async (singleRow) => {
    setLoading1(true)
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      const payload = [
        {
          tenProposed: singleRow[0]?.tenProposed || null,
          elevenProposed: singleRow[0]?.elevenProposed || null,
          twelveProposed: singleRow[0]?.twelveProposed || null,
          thirteenProposed: singleRow[0]?.thirteenProposed || null,
          fourteenProposed: singleRow[0]?.fourteenProposed || null,
          plantId: plantId,
          id: null,
          demo: singleRow[0]?.demo || null,
          date: moment(singleRow[0]?.date).format('YYYY-MM-DD'),
        },
      ]

      const response = await DataService.saveCrackerRunLength(
        plantId,
        payload,
        keycloak,
      )
      if (response?.code == 200) {
        setSnackbarOpen1(true)
        setSnackbarData1({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setLoading1(false)
      } else {
        setSnackbarOpen1(true)
        setSnackbarData1({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Error saving data:', error)
      setLoading1(false)
    } finally {
      // fetchData(3)
      setLoading1(false)
    }
  }

  const fetchDataNextYearParameters = useCallback(
    async (date) => {
      setLoading1(true)
      try {
        const res = await DataService.getCrackerNextYearParameters(
          keycloak,
          moment(date).format('YYYY-MM-DD'),
        )

        if (
          res?.code === 200 &&
          Array.isArray(res.data) &&
          res.data.length > 0
        ) {
          const item = res.data[0]

          const mappedValues = {
            H10: item.hTen || '0',
            H11: item.hEleven || '0',
            H12: item.hTwelve || '0',
            H13: item.hThirteen || '0',
            H14: item.hFourteen || '0',
            Demo: item.demo || '',
          }

          setHValues(mappedValues)
          // setStartDate(item.startDate ? new Date(item.startDate) : null)

          // const data4 = await DataService.getCrackerNextYearData(keycloak)
        }
      } catch (err) {
        console.error('Error loading data:', err)
      } finally {
        // setLoading(false)
        setLoading1(false)
      }
    },
    [keycloak],
  )

  const fetchDataNextYearCalculate = useCallback(
    async (hValuesParam, startDateParam) => {
      setLoading1(true)
      try {
        const queryParams = {
          h10: hValuesParam.H10,
          h11: hValuesParam.H11,
          h12: hValuesParam.H12,
          h13: hValuesParam.H13,
          h14: hValuesParam.H14,
          startDate: moment(startDateParam).format('YYYY-MM-DD'),
        }

        const res = await DataService.getCrackerNextYearData(
          keycloak,
          queryParams,
        )

        if (
          res?.code === 200 &&
          Array.isArray(res.data) &&
          res.data.length > 0
        ) {
          const toDateObject = (value) =>
            value ? moment(value, 'YYYY-MM-DD').toDate() : null

          const processedData = res?.data?.map((item, index) => ({
            ...item,
            month_: item?.month,
            id: index,
            remarks: item?.remarks || '',
            date: toDateObject(item.date),
            tenProposed: item.hTenProposed,
            hElevenActual: '',
            elevenProposed: item.hElevenProposed,
            hTwelveActual: '',
            twelveProposed: item.hTwelveProposed,
            hThirteenActual: '',
            thirteenProposed: item.hThirteenProposed,
            hFourteenActual: '',
            fourteenProposed: item.hFourteenProposed,
          }))

          const lastRow = processedData[processedData.length - 1]
          const secondLastRow = processedData[processedData.length - 2]

          // Add exactly 1 day (regardless of month/year)
          const nextDate = toDateObject(moment(lastRow.date).add(1, 'day'))
          const nextMonthName = moment(nextDate).format('MMMM')

          // Generate unique ID
          const generateRandomId = () =>
            `${Date.now()}-${Math.floor(Math.random() * 100000)}`

          // Enhanced increment function with SAD check
          const incrementIfNumberOrZero = (fieldName) => {
            const lastVal = lastRow[fieldName]
            const secondLastVal = secondLastRow?.[fieldName]

            if (lastVal === 'SAD' && secondLastVal === 'SAD') return '0'

            return !isNaN(lastVal)
              ? (parseInt(lastVal, 10) + 1).toString()
              : lastVal
          }

          const newRow = {
            ...lastRow,
            id: lastRow.id + 1,
            date: nextDate,
            month: nextMonthName,
            month_: nextMonthName,
            hTenActual: '',
            tenProposed: incrementIfNumberOrZero('tenProposed'),
            hElevenActual: '',
            elevenProposed: incrementIfNumberOrZero('elevenProposed'),
            hTwelveActual: '',
            twelveProposed: incrementIfNumberOrZero('twelveProposed'),
            hThirteenActual: '',
            thirteenProposed: incrementIfNumberOrZero('thirteenProposed'),
            hFourteenActual: '',
            fourteenProposed: incrementIfNumberOrZero('fourteenProposed'),
            demo: incrementIfNumberOrZero('demo'),
            idFromApi: generateRandomId(),
          }

          setRowsPopUp([...processedData, newRow])
          setSingleRow([newRow])
        }
      } catch (err) {
        setRowsPopUp([])
        console.error('Error loading data:', err)
      } finally {
        // setLoading(false)
        setLoading1(false)
      }
    },
    [keycloak],
  )

  const handleCancelClick = () => {
    setHValues({})
    setRowsPopUp([])
    setSingleRow([])
    setOpen(false)
    setStartDate(null)
  }

  const handleClose = () => {
    setOpen(false)
  }

  const handleStartDateChange = (e) => {
    setStartDate(e.value)

    const selectedDate = e.value
    const year = localStorage.getItem('year')

    if (
      lowerLimitDate &&
      upperLimitDate &&
      (selectedDate < lowerLimitDate || selectedDate > upperLimitDate)
    ) {
      setSnackbarOpen1(true)
      setSnackbarData1({
        message: `Date must be between 01-Apr and 31-Mar for financial year ${year}.`,
        severity: 'error',
      })
      return
    }
    fetchDataNextYearParameters(e.value)
  }

  const _export = useRef(null)

  const excelExport = () => {
    if (_export.current !== null) {
      _export.current.save()
    }
  }

  return (
    <Box>
      {(permissions?.allAction ?? false) && (
        <Box className='action-box'>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              width: '100%',
            }}
          >
            {/* Left side - Note */}
            <Box>
              {permissions?.showNote && (
                <Typography component='div' className='text-note'>
                  {note}
                </Typography>
              )}
            </Box>
            {/* Right side - All other actions */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {permissions?.downloadExcelBtn && (
                <Button
                  variant='contained'
                  className='btn-save'
                  onClick={downloadExcelForConfiguration}
                  disabled={isButtonDisabled}
                >
                  Export
                </Button>
              )}
              {/* {permissions?.uploadExcelBtn && (
                <Tooltip>
                  <span title='Import Data'>
                    <Button
                      variant='outlined'
                      size='large'
                      onClick={triggerFileUpload}
                      disabled={isButtonDisabled}
                    >
                      <UploadIcon fontSize='small' />
                    </Button>
                  </span>
                  <input
                    type='file'
                    accept='.xlsx,.xls'
                    onChange={onFileChange}
                    ref={fileInputRef}
                    style={{ display: 'none' }}
                  />
                </Tooltip>
              )} */}
              {permissions?.saveBtn && (
                <Button
                  variant='contained'
                  className='btn-save'
                  onClick={saveModalOpen}
                  disabled={
                    isButtonDisabled || Object.keys(modifiedCells).length === 0
                  }
                  {...(loading ? {} : {})}
                >
                  Save
                </Button>
              )}
              {permissions?.showCalculate && (
                <Button
                  variant='contained'
                  onClick={handleCalculateBtn}
                  disabled={
                    rows?.length === 0
                      ? false
                      : isButtonDisabled ||
                        !permissions?.showCalculateVisibility
                  }
                  className='btn-save'
                >
                  Calculate
                </Button>
              )}
              {permissions?.showCalculate && (
                <Button
                  variant='contained'
                  onClick={handleOpen}
                  className='btn-save'
                >
                  Calculate For Next Year
                </Button>
              )}
            </Box>
          </Box>
        </Box>
      )}

      <Box className='kendo-data-grid'>
        {!permissions?.showAccordian ? (
          <CustomAccordion
            defaultExpanded={!permissions?.byDefCollaps}
            disableGutters
          >
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                {titleName}
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Tooltip openDelay={50} position='auto' anchorElement='target'>
                {renderGrid()}
              </Tooltip>
            </CustomAccordionDetails>
          </CustomAccordion>
        ) : (
          <Tooltip openDelay={50} position='auto' anchorElement='target'>
            {renderGrid()}
          </Tooltip>
        )}
      </Box>

      <Box
        sx={{
          marginTop: 2,
          display: 'flex',
          gap: 2,
        }}
      >
        {showDeleteAll && (
          <Button
            variant='contained'
            className='btn-save'
            onClick={handleDeleteSelected}
            disabled={isButtonDisabled}
            loading={loading}
            loadingposition='start'
          >
            Delete
          </Button>
        )}
      </Box>

      <Notification
        open={snackbarOpen1}
        message={snackbarData1?.message || ''}
        severity={snackbarData1?.severity || 'info'}
        onClose={() => setSnackbarOpen1(false)}
      />

      <Dialog
        open={openDeleteDialogeBox}
        onClose={() => setOpenDeleteDialogeBox(false)}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Delete ?'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure you want to delete this row?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialogeBox(false)}>Cancel</Button>
          <Button onClick={deleteTheRecord} autoFocus>
            Delete
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={openSaveDialogeBox}
        onClose={closeSaveDialogeBox}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Save ?'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure you want to save these changes?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeSaveDialogeBox}>Cancel</Button>
          <Button onClick={saveConfirmation} autoFocus>
            Save
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={openSaveDialogeBoxSingleRow}
        onClose={closeSaveDialogeBoxSingleRow}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
        sx={{ zIndex: 2000 }} // Works in most cases
      >
        <DialogTitle id='alert-dialog-title'>{'Save ?'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure you want to save these changes?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeSaveDialogeBoxSingleRow}>Cancel</Button>
          <Button onClick={saveConfirmationSingleRow} autoFocus>
            Save
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={!!remarkDialogOpen}
        onClose={() => setRemarkDialogOpen(false)}
      >
        <DialogTitle>Add Remark</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin='dense'
            id='remark'
            label='Remark'
            type='text'
            fullWidth
            variant='outlined'
            sx={{ width: '100%', minWidth: '600px' }}
            value={currentRemark || ''}
            // value={remark}
            onChange={(e) => setCurrentRemark(e.target.value)}
            multiline
            rows={8}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRemarkDialogOpen(false)}>Cancel</Button>
          {/* <Button onClick={handleCloseRemark}>Cancel</Button> */}
          <Button onClick={handleRemarkSave} disabled={!currentRemark?.trim()}>
            Add
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={open}
        onClose={handleClose}
        maxWidth='xl'
        fullWidth
        PaperProps={{
          style: {
            height: '100vh', // full screen height
            maxHeight: '100vh', // prevent scroll beyond viewport
            display: 'flex',
            flexDirection: 'column',
          },
        }}
      >
        <DialogTitle style={{ padding: '8px 16px', fontSize: '16px' }}>
          Configuration for Next Year (
          {`${parseInt(localStorage.getItem('year')?.split('-')[0], 10) + 1}-${(parseInt(localStorage.getItem('year')?.split('-')[0], 10) + 2).toString().slice(-2)}`}
          )
        </DialogTitle>

        <DialogContent dividers style={{ padding: '8px' }}>
          <div
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              gap: '16px',
              alignItems: 'center',
              marginBottom: '16px',
            }}
          >
            {/* Start Date */}
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label style={{ fontSize: '14px', marginBottom: '4px' }}>
                Start Date
              </label>
              <DatePicker
                id='start-date-1'
                format='dd-MM-yyyy'
                value={startDate}
                onChange={handleStartDateChange}
                style={{ width: '160px' }}
                min={lowerLimitDate}
                max={upperLimitDate}
                size='small'
                popupSettings={{
                  appendTo: document.body,
                  style: { zIndex: 1302 },
                }}
              />
            </div>

            {/* H Inputs */}
            {['H10', 'H11', 'H12', 'H13', 'H14'].map((label) => (
              <div
                key={label}
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  minWidth: '80px',
                }}
              >
                <label style={{ fontSize: '14px', marginBottom: '4px' }}>
                  {label}
                </label>
                <input
                  type='text'
                  value={hValues[label]}
                  onChange={(e) => handleChange(label, e.target.value)}
                  style={{
                    height: '30px',
                    padding: '2px 6px',
                    fontSize: '14px',
                  }}
                />
              </div>
            ))}

            {/* Calculate Button */}
            <div style={{ alignSelf: 'flex-end' }}>
              <button
                onClick={() => handleCalculateData(hValues)}
                disabled={
                  !startDate ||
                  Object.values(hValues).some(
                    (value) => value === null || value === '',
                  )
                }
                className='btn-save'
                style={{
                  height: '34px',
                  padding: '0 16px',
                  fontSize: '14px',
                  marginRight: '10px',
                }}
              >
                Calculate
              </button>

              <button
                onClick={saveModalOpenSingleRow}
                disabled={singleRow?.length === 0}
                className='btn-save'
              >
                Save
              </button>
            </div>
          </div>

          {/* Grid Rendering */}
          <div style={{ marginTop: '12px' }}>
            <Tooltip openDelay={50} position='auto' anchorElement='target'>
              {renderGridSingleRow()}
            </Tooltip>
          </div>
          {/* Export button aligned right */}
          <div
            style={{
              display: 'flex',
              justifyContent: 'flex-end',
              marginTop: '8px',
            }}
          >
            <button
              className='btn-save'
              onClick={excelExport}
              disabled={rowsPopUp?.length === 0}
            >
              Export
            </button>
          </div>
          <div style={{ marginTop: '12px' }}>{renderGridDayWise()}</div>

          <Backdrop
            sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
            open={!!loading1}
          >
            <CircularProgress color='inherit' />
          </Backdrop>
        </DialogContent>

        <DialogActions style={{ padding: '4px 8px' }}>
          <Button onClick={handleCancelClick} size='small'>
            Cancel
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default KendoDataTablesCrackerRunLength
