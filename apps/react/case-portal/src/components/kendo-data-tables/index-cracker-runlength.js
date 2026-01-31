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

//import { ibrGridThreePopUP, singleRowColumn } from './columnDefs'

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
import { getRoleName } from 'services/role-service'

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
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id

  const AOP_YEAR = year?.selectedYear
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const SCREEN_NAME = screenTitle?.title

  const PLANT_NAME = plantObject?.name?.toUpperCase()
  const SITE_NAME = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME = verticalObject?.name?.toUpperCase()

  const startYear = parseInt(AOP_YEAR?.split('-')[0], 10)

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

  // const READ_ONLY = getRoleName(keycloak)
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const [loading1, setLoading1] = useState(false)
  const [open, setOpen] = useState(false)
  const [startDate, setStartDate] = useState(null)
  const [dynamicColumns, setDynamicColumns] = useState([])
  const [dynamicColumnsConfig, setDynamicColumnsConfig] = useState([])

  const [hValues, setHValues] = useState({})
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
    const startYear = parseInt(AOP_YEAR?.split('-')[0], 10)
    const lowerLimit = new Date(startYear, 3, 1)
    const upperLimit = new Date(startYear + 1, 2, 31)
    setLowerLimitDate(lowerLimit)
    setUpperLimitDate(upperLimit)
  }, [PLANT_ID, AOP_YEAR])

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

      // THIS LOGIC NEEDS TO BE UPDATED AS FOR NOW IT IS COMMENTED
      // if (value?.toUpperCase() === 'SAD' && dataItem[field] !== 'SAD') {
      // eslint-disable-next-line no-constant-condition
      if (1 == 0) {
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
        style={{ padding: '0px', borderRight: '1px solid #878787' }}
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
        if (col.type === 'date') {
          return (
            <GridColumn
              key={col.field}
              field={col.field}
              title={col.title || col.headerName}
              filter='date'
              filterable={true}
              columnMenu={ColumnMenuCheckboxFilter}
              format='{0:dd-MM-yyyy}'
              hidden={col.hidden}
              headerClassName={isActive ? 'active-column' : ''}
              cells={{
                headerCell: SimpleHeaderWithTooltip,
              }}
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
        fileName={`${RUN_LENGTH_EXCEL_NAME}.xlsx`}
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
          {dynamicColumnsConfig.map((col) => {
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
                  format={col.type === 'date' ? '{0:yyyy-MM-dd}' : undefined}
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
      {dynamicColumnsConfig
        .filter((col) => col.field && col.field.trim() !== '')
        .map((col) => {
          if (
            dateFields1.includes(col.field) ||
            dateFieldsRunLength.includes(col.field)
          ) {
            return (
              <GridColumn
                key={col.field}
                field={col.field}
                title={col.title || col.headerName}
                format={col.type === 'date' ? '{0:yyyy-MM-dd}' : undefined}
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

  const getNextAopYear = (aopYear) => {
    const parts = aopYear.split('-')
    const currentStartYear = parseInt(parts[0], 10)
    const nextStartYear = currentStartYear + 1
    const nextEndYear = nextStartYear + 1
    const formattedNextEndYear = String(nextEndYear).slice(-2)
    return `${nextStartYear}-${formattedNextEndYear}`
  }

  const NEXT_AOP_YEAR = getNextAopYear(AOP_YEAR)
  const RUN_LENGTH_EXCEL_NAME = `${VERTICAL_NAME}_${SITE_NAME}_${PLANT_NAME}_Run_Length_${NEXT_AOP_YEAR}`

  const saveCrackerRunLength = async (singleRow) => {
    setLoading1(true)
    try {
      // Helper function to transform field names: "H10_Proposed" -> "H10 Proposed"
      const transformFieldName = (fieldName) => {
        // Replace underscores with spaces
        return fieldName
      }

      const payload = singleRow.map((row) => {
        const payloadItem = {
          plantId: PLANT_ID,
          id: null,
        }

        // Fields to exclude from payload
        const excludeFields = ['id', 'idFromApi']

        // Iterate through all fields in the row
        Object.keys(row).forEach((frontendField) => {
          // Skip excluded fields
          if (excludeFields.includes(frontendField)) return

          // Transform the field name (remove underscores, add spaces)
          const backendField = transformFieldName(frontendField)

          // Handle date fields
          if (frontendField.toLowerCase().includes('date')) {
            payloadItem[backendField] = row[frontendField]
              ? moment(row[frontendField]).format('YYYY-MM-DD')
              : null
          } else {
            // Add all other fields with transformed names
            payloadItem[backendField] = row[frontendField] ?? null
          }
        })

        return payloadItem
      })

      const response = await DataService.saveCrackerRunLength(
        PLANT_ID,
        payload,
        keycloak,
        NEXT_AOP_YEAR,
      )

      if (response?.code == 200) {
        setSnackbarOpen1(true)
        setSnackbarData1({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
      } else {
        setSnackbarOpen1(true)
        setSnackbarData1({
          message: 'Data Save Failed!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Error saving data:', error)
      setSnackbarOpen1(true)
      setSnackbarData1({
        message: 'Data Save Failed!',
        severity: 'error',
      })
    } finally {
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
          PLANT_ID,
          AOP_YEAR,
        )

        if (res?.code === 200 && res.data && Array.isArray(res.data.columns)) {
          const HIDDEN_FIELDS = ['Plant_FK_Id', 'AOPYear', 'Demo', 'Date']
          const columnsFromApi = res.data.columns
            .filter((col) => col.field && col.field.trim() !== '')
            .map((col) => ({
              ...col,
              hidden: HIDDEN_FIELDS.includes(col.field),
              editable: true,
            }))
          setDynamicColumns(columnsFromApi)

          // Dynamically set hValues based on columns
          const item =
            Array.isArray(res.data.data) && res.data.data.length > 0
              ? res.data.data[0]
              : {}
          const hVals = {}
          columnsFromApi.forEach((col) => {
            if (!HIDDEN_FIELDS.includes(col.field)) {
              hVals[col.field] = item?.[col.field] ?? '0'
            }
          })
          setHValues(hVals)
          // Optionally set start date if needed
          // setStartDate(item.Date ? new Date(item.Date) : null)
        }
      } catch (err) {
        console.error('Error loading data:', err)
      } finally {
        setLoading1(false)
      }
    },
    [keycloak, AOP_YEAR, PLANT_ID],
  )

  const fetchDataNextYearCalculate = useCallback(
    async (hValuesParam, startDateParam) => {
      setLoading1(true)
      try {
        // Build query params dynamically
        const queryParams = {}
        Object.keys(hValuesParam).forEach((key) => {
          queryParams[key] = hValuesParam[key]
        })
        queryParams.startDate = moment(startDateParam).format('YYYY-MM-DD')

        // Fetch data from API
        const res = await DataService.getCrackerNextYearData(
          keycloak,
          queryParams,
          PLANT_ID,
          AOP_YEAR,
        )

        // Get columns and data from API response
        // Filter and map columns to handle empty field names
        const columnsFromApi = (res?.data?.columns || [])
          .filter((col) => col.field && col.field.trim() !== '')
          .map((col) => ({
            ...col,
            editable:
              col.field === 'Date' || col.field === 'Month' ? false : true,
            hidden: ['aopYear', 'plantId'].includes(col.field),
          }))

        setDynamicColumnsConfig(columnsFromApi)
        const dataArr = res?.data?.data || []

        if (Array.isArray(dataArr) && dataArr.length > 0) {
          // Helper to convert date fields
          const toDateObject = (value) =>
            value ? moment(value, 'YYYY-MM-DD').toDate() : null

          // Map data using backend column field names
          const processedData = dataArr.map((item, index) => {
            const row = { id: index }
            columnsFromApi.forEach((col) => {
              let val = item[col.field]

              // Handle the rowId field (previously empty string field)
              if (col.field === 'rowId' && item[''] !== undefined) {
                val = item['']
              }

              // Convert any field with type 'date' to JS Date object
              if (
                col.type &&
                col.type.toLowerCase() === 'date' &&
                val &&
                typeof val === 'string'
              ) {
                val = moment(val, [
                  'YYYY-MM-DD',
                  'DD-MM-YYYY',
                  moment.ISO_8601,
                ]).format('YYYY-MM-DD')
              }
              row[col.field] = val ?? ''
            })
            return row
          })

          const lastRow = processedData[processedData.length - 1]
          const secondLastRow = processedData[processedData.length - 2]

          // Add exactly 1 day (regardless of month/year)
          // Find the correct field name for date and month from columns
          const dateField =
            columnsFromApi.find((col) =>
              col.field.toLowerCase().includes('date'),
            )?.field || 'date'
          const monthField =
            columnsFromApi.find((col) => col.field.toLowerCase() === 'month')
              ?.field || 'month'

          // const nextDate = toDateObject(moment(lastRow[dateField]).add(1, 'day'));
          // const nextMonthName = moment(nextDate).format('MMMM');
          const nextDate = moment(lastRow[dateField])
            .add(1, 'day')
            .format('YYYY-MM-DD') // <-- format as string
          const nextMonthName = moment(nextDate, 'YYYY-MM-DD').format('MMMM')

          // Generate unique ID
          const generateRandomId = () =>
            `${Date.now()}-${Math.floor(Math.random() * 100000)}`

          // Build new row using backend field names
          const newRow = { ...lastRow }
          newRow.id = lastRow.id + 1
          newRow[dateField] = nextDate
          newRow[monthField] = nextMonthName
          newRow.idFromApi = generateRandomId()

          columnsFromApi.forEach((col) => {
            const field = col.field
            if (field === dateField) {
              newRow[field] = nextDate
            } else if (field === monthField) {
              newRow[field] = nextMonthName
            } else if (field === 'rowId') {
              // Generate new ID for the rowId field
              newRow[field] = generateRandomId()
            } else if (
              typeof lastRow[field] === 'string' &&
              !isNaN(lastRow[field])
            ) {
              newRow[field] =
                lastRow[field] === 'SAD' && secondLastRow?.[field] === 'SAD'
                  ? '0'
                  : (parseInt(lastRow[field], 10) + 1).toString()
            } else if (
              lastRow[field] === 'SAD' &&
              secondLastRow?.[field] === 'SAD'
            ) {
              newRow[field] = '0'
            } else {
              newRow[field] = lastRow[field]
            }
          })

          const processedDataWithNewRow = [...processedData, newRow]
          setRowsPopUp(processedDataWithNewRow)
          setSingleRow([newRow])
        } else {
          setRowsPopUp([])
          setSingleRow([])
        }
      } catch (err) {
        setRowsPopUp([])
        setSingleRow([])
        console.error('Error loading data:', err)
      } finally {
        setLoading1(false)
      }
    },
    [keycloak, AOP_YEAR, PLANT_ID],
  )

  const handleCancelClick = () => {
    setHValues({})
    setRowsPopUp([])
    setSingleRow([])
    setOpen(false)
    setStartDate(null)
    setDynamicColumns([])
    setDynamicColumnsConfig([])
  }

  const handleClose = () => {
    setOpen(false)
  }

  const handleStartDateChange = (e) => {
    setStartDate(e.value)

    const selectedDate = e.value

    if (
      lowerLimitDate &&
      upperLimitDate &&
      (selectedDate < lowerLimitDate || selectedDate > upperLimitDate)
    ) {
      setSnackbarOpen1(true)
      setSnackbarData1({
        message: `Date must be between 01-Apr and 31-Mar for financial year ${AOP_YEAR}.`,
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

  ;<Box>
    {permissions?.showNote && (
      <Typography component='div' className='text-note'>
        {note}
      </Typography>
    )}
  </Box>

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

            {permissions?.showTitleNameBusiness && (
              <Typography
                component='div'
                className='grid-title'
                sx={{
                  ...(permissions?.marginBottom && { marginBottom: '10px' }),
                }}
              >
                {permissions?.titleName}
              </Typography>
            )}

            {/* Right side - All other actions */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {permissions?.downloadExcelBtn && (
                <Button
                  variant='contained'
                  className='btn-save'
                  onClick={downloadExcelForConfiguration}
                  disabled={isButtonDisabled || READ_ONLY}
                >
                  Export
                </Button>
              )}

              {permissions?.uploadExcelBtn && (
                <Tooltip title='Import Data'>
                  <span>
                    <Button
                      variant='contained'
                      className='btn-save'
                      onClick={triggerFileUpload}
                      disabled={isButtonDisabled || READ_ONLY}
                    >
                      Import
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
              )}

              {permissions?.saveBtn && (
                <Button
                  variant='contained'
                  className='btn-save'
                  onClick={saveModalOpen}
                  disabled={
                    isButtonDisabled ||
                    Object.keys(modifiedCells).length === 0 ||
                    READ_ONLY
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
                    READ_ONLY ||
                    (rows?.length === 0
                      ? false
                      : isButtonDisabled ||
                        !permissions?.showCalculateVisibility)
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
                  disabled={READ_ONLY}
                >
                  Calculate For Next Year
                </Button>
              )}
            </Box>
          </Box>
        </Box>
      )}

      <Box className='kendo-data-grid'>
        {permissions?.showAccordian ? (
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
            disabled={isButtonDisabled || READ_ONLY}
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
        disableScrollLock
        slotProps={{
          backdrop: { disableScrollLock: true },
        }}
      >
        <DialogTitle id='alert-dialog-title'>{'Save ?'}</DialogTitle>
        <DialogContent>
          <DialogContentText
            id='alert-dialog-description'
            sx={{ color: 'text.primary' }}
          >
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
        sx={{ zIndex: 2000 }} // Works in most cases disableScrollLock
        disableScrollLock
        slotProps={{
          backdrop: { disableScrollLock: true },
        }}
      >
        <DialogTitle id='alert-dialog-title'>{'Save ?'}</DialogTitle>
        <DialogContent>
          <DialogContentText
            id='alert-dialog-description'
            sx={{ color: 'text.primary' }}
          >
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
        disableScrollLock
        slotProps={{
          backdrop: { disableScrollLock: true },
        }}
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
          {`${parseInt(AOP_YEAR?.split('-')[0], 10) + 1}-${(parseInt(AOP_YEAR?.split('-')[0], 10) + 2).toString().slice(-2)}`}
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
                disabled={READ_ONLY}
                popupSettings={{
                  appendTo: document.body,
                  style: { zIndex: 1302 },
                }}
              />
            </div>

            {/* H Inputs */}
            {dynamicColumns
              .filter(
                (col) =>
                  col.field &&
                  !['Plant_FK_Id', 'AOPYear', 'Demo', 'Date'].includes(
                    col.field,
                  ),
              )
              .map((col) => (
                <div
                  key={col.field}
                  style={{
                    display: 'flex',
                    flexDirection: 'column',
                    minWidth: '80px',
                  }}
                >
                  <label style={{ fontSize: '14px', marginBottom: '4px' }}>
                    {col.title}
                  </label>
                  <input
                    type='text'
                    value={hValues[col.field] || ''}
                    onChange={(e) => handleChange(col.field, e.target.value)}
                    style={{
                      height: '30px',
                      padding: '2px 6px',
                      fontSize: '14px',
                    }}
                  />
                </div>
              ))}

            {/* Calculate Button */}
            <div
              style={{
                display: 'flex',
                justifyContent: 'flex-end',
                alignSelf: 'flex-end',
                gap: '8px', // adds space between buttons
              }}
            >
              <button
                onClick={() => handleCalculateData(hValues)}
                disabled={
                  READ_ONLY ||
                  !startDate ||
                  Object.values(hValues).some(
                    (value) => value === null || value === '',
                  )
                }
                className='btn-save'
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

          {/* Export and Import buttons aligned side by side */}
          <div
            style={{
              display: 'flex',
              justifyContent: 'flex-end',
              marginTop: '8px',
              gap: '8px', // space between buttons
            }}
          >
            <button
              className='btn-save'
              onClick={excelExport}
              disabled={rowsPopUp?.length === 0}
            >
              Export
            </button>

            {/* <button
              className='btn-save'
              onClick={onFileChange}
              disabled={rowsPopUp?.length === 0}
            >
              Import
            </button> */}
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
          <Button onClick={handleCancelClick} className='btn-save' size='small'>
            Cancel
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default KendoDataTablesCrackerRunLength
