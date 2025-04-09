import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import PropTypes from 'prop-types'
import {
  DataGrid,
  GridToolbarColumnsButton,
  GridToolbarContainer,
  GridToolbarDensitySelector,
  gridFilterModelSelector,
  useGridApiContext,
  useGridSelector,
  useGridApiRef,
  GridPreferencePanelsValue,
  gridPreferencePanelStateSelector,
} from '@mui/x-data-grid'
import {
  Avatar,
  Box,
  Typography,
  Select,
  MenuItem,
  IconButton,
  Tooltip,
  useMediaQuery,
  Button,
  FormControl,
  InputLabel,
} from '@mui/material'
import SkipNextIcon from '@mui/icons-material/SkipNext'
import SkipPreviousIcon from '@mui/icons-material/SkipPrevious'
import NavigateNextIcon from '@mui/icons-material/NavigateNext'
import NavigateBeforeIcon from '@mui/icons-material/NavigateBefore'
import SaveAltRoundedIcon from '@mui/icons-material/SaveAltRounded'
import FilterListRoundedIcon from '@mui/icons-material/FilterListRounded'
import { utils, writeFile } from 'xlsx'
import dayjs from 'dayjs'
// import '../../styles/commonCSS.styles.css'
// import {
//   filterOptionsForOrg,
//   filterOptionsForStatus,
// } from '../../utils/constants'

const ASDataGrid1 = ({
  columns,
  rows,
  pageSize = 10,
  rowsPerPageOptions = [10, 20, 50],
  checkboxSelection = false,
  disableSelectionOnClick = true,
  autoHeight = true,
  cNameToAddAvatar,
  isReordering = false,
  exportFileName = 'Assessforce',
  columnVisibilityModel,
  defaultSortModel,
  handleSortModelChange,
  handleCellClick,
  loader = false,
  filterOptionsForRoles,
  ...props
}) => {
  const apiRef = useGridApiRef(null)
  const columnVisibilityRef = useRef(columnVisibilityModel)

  const isMobile = useMediaQuery('(max-width:900px)')
  const [currentPage, setCurrentPage] = React.useState(0)
  const [currentPageSize, setCurrentPageSize] = React.useState(pageSize)
  const [showHeaderFilters, setShowHeaderFilters] = useState(false)
  const [totalPages, setTotalPages] = useState(rows?.length)
  const [totalRows, setTotalRows] = useState(rows?.length)
  const [processedRows, setProcessedRows] = useState([])
  const [filterModel, setFilterModel] = useState({ items: [] })
  const [finalRows, setFinalRows] = useState([])
  const [selectedDensity, setDensity] = useState('standard')
  const [previousFilterModel, setPreviousFilterModel] = useState(null)
  const [anchorEl, setAnchorEl] = useState(null)
  const [resizedColumns, setResizedColumns] = useState({})
  const [columnVisibility, setColumnVisibility] = useState(
    columnVisibilityModel,
  )

  const currentDate = dayjs()
  const fileName = `${exportFileName}-${currentDate.format('MM-DD-YY HH_mm_ss')}`

  const hiddenFields = ['actions']

  useEffect(() => {
    if (columnVisibilityModel) {
      setColumnVisibility(columnVisibilityModel)
    }
  }, [columnVisibilityModel])

  const handleChangePage = (newPage) => {
    setCurrentPage(newPage)
  }

  const handleChangePageSize = (event) => {
    setCurrentPageSize(event.target.value)
    setCurrentPage(0)
  }

  useEffect(() => {
    let updatedRows = [...rows]

    // Detect filter change
    const isFilterChanged =
      JSON.stringify(filterModel?.items || []) !==
      JSON.stringify(previousFilterModel?.items || [])

    if (isFilterChanged) {
      // Reset to page 0 only if the filter model has changed
      setCurrentPage(0)
      setPreviousFilterModel(filterModel)
    }

    if (filterModel?.items?.length > 0) {
      updatedRows = updatedRows?.filter((row) =>
        filterModel.items?.every((filter) => {
          const rowValue =
            typeof row[filter.field] === 'string'
              ? row[filter.field]?.toString().toLowerCase()
              : Number(row[filter.field])

          const filterValue =
            typeof filter.value === 'string'
              ? filter?.value?.toString().toLowerCase()
              : filter?.value

          switch (filter.operator) {
            case 'contains':
              return rowValue?.includes(filterValue)
            case 'doesNotContain':
              return !rowValue?.includes(filterValue)
            case typeof filterValue === 'string' ? 'equals' : '=':
              return rowValue === filterValue
            case typeof filterValue === 'string' ? 'doesNotEqual' : '!=':
              return rowValue !== filterValue
            case 'startsWith':
              return rowValue?.startsWith(filterValue)
            case 'endsWith':
              return rowValue?.endsWith(filterValue)
            case 'isEmpty':
              return rowValue === ''
            case 'isNotEmpty':
              return rowValue !== ''
            case '>':
              return parseFloat(rowValue) > parseFloat(filterValue)
            case '<':
              return parseFloat(rowValue) < parseFloat(filterValue)
            case '>=':
              return parseFloat(rowValue) >= parseFloat(filterValue)
            case '<=':
              return parseFloat(rowValue) <= parseFloat(filterValue)
            default:
              return true
          }
        }),
      )
    }

    // Apply sorting
    if (defaultSortModel?.length > 0) {
      updatedRows.sort((a, b) => {
        for (let sort of defaultSortModel) {
          const field = sort.field
          const sortOrder =
            sort.sort === 'asc' ? 1 : sort.sort === 'desc' ? -1 : 0
          if (sortOrder === 0) return 0

          let valueA = a[field]
          let valueB = b[field]

          // Ensure values are strings for consistency
          valueA = valueA != null ? String(valueA).trim() : ''
          valueB = valueB != null ? String(valueB).trim() : ''

          // Check if both values are numeric strings
          const numA = parseFloat(valueA)
          const numB = parseFloat(valueB)

          const isNumericA = !isNaN(numA) && valueA === numA.toString()
          const isNumericB = !isNaN(numB) && valueB === numB.toString()

          if (isNumericA && isNumericB) {
            // Compare as numbers
            if (numA < numB) return -1 * sortOrder
            if (numA > numB) return 1 * sortOrder
          } else {
            // Compare as strings (case-insensitive)
            if (valueA.toLowerCase() < valueB.toLowerCase())
              return -1 * sortOrder
            if (valueA.toLowerCase() > valueB.toLowerCase())
              return 1 * sortOrder
          }
        }
        return 0
      })
    }

    // Pagination
    const start = currentPage * currentPageSize
    const end = start + currentPageSize
    setFinalRows(updatedRows)
    const finalRows = updatedRows?.slice(start, end)
    const totalRows = updatedRows?.length
    const totalPages = Math.ceil(totalRows / currentPageSize)
    setProcessedRows(finalRows)
    setTotalPages(totalPages)
    setTotalRows(totalRows)
  }, [rows, currentPage, currentPageSize, defaultSortModel, filterModel])

  const onColumnResized = (params) => {
    if (params.column) {
      const field = params.column.getColDef().field
      setResizedColumns((prev) => ({
        ...prev,
        [field]: true,
      }))
    }
  }

  const defaultColumns = useMemo(() => {
    return columns.map((col) => ({
      ...col,
      flex: !isMobile && !resizedColumns[col.field] ? 1 : null,
    }))
  }, [columns, isMobile, resizedColumns])

  const getDefaultFilter = (field) => ({ field, operator: 'equals' })
  const transformLabel = (value) =>
    value.charAt(0).toUpperCase() + value.slice(1).toLowerCase()

  const AdminFilter = (props) => {
    // eslint-disable-next-line react/prop-types
    const { colDef, filterOptions } = props
    const apiRef = useGridApiContext()
    const filterModel = useGridSelector(apiRef, gridFilterModelSelector)
    const currentFieldFilters = useMemo(
      // eslint-disable-next-line react/prop-types
      () => filterModel.items?.filter(({ field }) => field === colDef?.field),
      // eslint-disable-next-line react/prop-types
      [colDef?.field, filterModel.items],
    )

    const handleChange = useCallback(
      (event) => {
        const newValue = event.target.value

        if (newValue === undefined || newValue === null || newValue === '') {
          if (currentFieldFilters[0]) {
            apiRef.current.deleteFilterItem(currentFieldFilters[0])
          }
          return
        }

        const newFilterItem = {
          // eslint-disable-next-line react/prop-types
          ...(currentFieldFilters[0] || getDefaultFilter(colDef?.field)),
          value: newValue,
        }

        const updatedFilters = [
          // eslint-disable-next-line react/prop-types
          ...filterModel.items.filter(({ field }) => field !== colDef?.field),
          newFilterItem,
        ]

        apiRef.current.setFilterModel({ ...filterModel, items: updatedFilters })
      },
      // eslint-disable-next-line react/prop-types
      [apiRef, colDef?.field, currentFieldFilters, filterModel],
    )

    const value = currentFieldFilters[0]?.value ?? ''
    const label = value === '' ? 'Filter' : transformLabel(value)

    return (
      <FormControl variant='standard' sx={{ m: 0, minWidth: 120 }} fullWidth>
        <InputLabel id='select-is-admin-label'>{label}</InputLabel>
        <Select
          labelId='select-is-admin-label'
          id='select-is-admin'
          value={value}
          onChange={handleChange}
          label={label}
          sx={{ textTransform: 'capitalize', fontFamily: 'inter-regular' }}
        >
          <MenuItem value=''>All</MenuItem>
          {filterOptions?.map((item) => (
            <MenuItem value={item?.value} key={item?.label}>
              {item?.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    )
  }

  const modifiedColumns = useMemo(() => {
    return defaultColumns.map((col) => {
      if (col.field === cNameToAddAvatar) {
        return {
          ...col,
          renderCell: (params) => {
            const avatarColors = [
              '#AB47BC',
              '#D81B60',
              '#FF6F00',
              '#1B7C95',
              '#00796B',
              '#558B2F',
              '#5C6BC0',
              '#0277BD',
            ]
            const name = params.row?.[`${cNameToAddAvatar}`]
            // const generateHash = (str) => {
            //   if (!str) return 0;
            //   let hash = 0;
            //   for (let i = 0; i < str.length; i++) {
            //     hash = (hash * 31 + str.charCodeAt(i)) % avatarColors.length;
            //   }
            //   return hash;
            // };
            // const index = generateHash(name);
            const initials =
              name && name.trim()
                ? name?.includes(' ')
                  ? name?.split(' ')[0]?.charAt(0) +
                    name?.split(' ')[1]?.charAt(0)
                  : name?.length === 1
                    ? name?.charAt(0) + 'X'
                    : name?.charAt(0) + name?.charAt(1)
                : ', XX'
            function getInitialsMagicNumber(initials) {
              const numbers = initials
                ?.toLowerCase()
                ?.split('')
                ?.map((char) => char?.charCodeAt(0))
              const spice = numbers?.[0] < numbers?.[1] ? 0 : 1
              return numbers?.reduce((acc, n) => acc + n) + spice
            }

            const magicNumber = getInitialsMagicNumber(initials)
            const colorIndex = magicNumber % avatarColors.length
            const randomColor = avatarColors[colorIndex]

            return (
              <Tooltip title={params?.value || ''}>
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    height: '100%',
                    cursor: 'pointer',
                  }}
                >
                  <Avatar
                    sx={{
                      width: '1.875rem',
                      height: '1.875rem',
                      marginRight: 1,
                      textTransform: 'uppercase',
                      fontSize: '.8rem',
                      backgroundColor: randomColor,
                    }}
                  >
                    <Typography
                      component='text'
                      sx={{
                        fontSize: '.8rem',
                      }}
                    >
                      {name
                        ? name?.includes(' ')
                          ? name?.split(' ')[0].charAt(0) +
                            name?.split(' ')[1].charAt(0)
                          : name?.length === 1
                            ? name?.charAt(0) + 'X'
                            : name?.charAt(0) + name?.charAt(1)
                        : 'XX'}
                    </Typography>
                  </Avatar>
                  <Typography
                    variant='body2'
                    sx={{
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                    }}
                  >
                    {name}
                  </Typography>
                </Box>
              </Tooltip>
            )
          },
        }
      }

      const fromOrg = [
        'field_orgz_status',
        'field_orgz_gen_sponser_status',
        'field_orgz_self_sponser_status',
        'field_orgz_service_pro_status',
      ]?.includes(col?.field)

      return {
        ...col,
        filterOperators:
          col?.field === 'status' || col?.field === 'role' || fromOrg
            ? [
                {
                  // label: 'equals',
                  value: 'equals',
                  getApplyFilterFn: (filterItem) => {
                    if (!filterItem?.value) {
                      return null
                    }
                    return (value) => String(value) === String(filterItem.value)
                  },
                  InputComponent: (prop) => (
                    <AdminFilter
                      {...prop}
                      colDef={col}
                      filterOptions={
                        col?.field === 'role'
                          ? props.filterOptionsForRoles
                          : fromOrg
                        // ? filterOptionsForOrg
                        // : filterOptionsForStatus
                      }
                    />
                  ),
                },
              ]
            : col?.filterOperators,
        renderHeaderFilter:
          col?.field === 'status' || col?.field === 'role' || fromOrg
            ? (params) => (
                <AdminFilter
                  {...params}
                  filterOptions={
                    col?.field === 'role'
                      ? props?.filterOptionsForRoles
                      : fromOrg
                    // ? filterOptionsForOrg
                    // : filterOptionsForStatus
                  }
                />
              )
            : col?.renderHeaderFilter,
      }
    })
  }, [defaultColumns, filterOptionsForRoles])

  const CustomPagination = () => {
    return (
      <Box
        sx={{
          display: 'flex',
          flexDirection: isMobile ? 'column' : 'row',
          alignItems: 'center',
          justifyContent: isMobile ? 'center' : 'flex-end',
          padding: isMobile ? 1 : 2,
          gap: isMobile ? 1 : 2,
        }}
      >
        {/* Top row: Rows per page, Select, and page info */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: isMobile ? 1 : 2,
            width: isMobile ? '100%' : 'auto',
          }}
        >
          {/* Rows per page */}
          <InputLabel
            htmlFor='row-per-page-select'
            id='row-per-page-select-label'
            sx={{
              fontSize: isMobile ? '0.8rem' : '0.9rem',
              color: 'var(--ternary-color) !important',
            }}
          >
            Rows per page:
          </InputLabel>
          <Select
            //id="row-per-page-select"
            inputProps={{ id: 'row-per-page-select' }}
            labelId='row-per-page-select-label'
            value={currentPageSize}
            onChange={handleChangePageSize}
            size='small'
            sx={{
              fontSize: isMobile ? '0.8rem' : '0.9rem',
              width: isMobile ? '80px' : 'auto',
              border: 'none',
              '& fieldset': {
                border: 'none',
              },
            }}
          >
            {rowsPerPageOptions.map((option, index) => (
              <MenuItem id={`${option}-${index}`} key={option} value={option}>
                {option}
              </MenuItem>
            ))}
          </Select>

          {/* Page info */}
          <Typography
            variant='body2'
            sx={{
              fontSize: isMobile ? '0.8rem' : '0.9rem',
            }}
          >
            {totalRows === 0
              ? '0-0 of 0'
              : `${currentPage * currentPageSize + 1}-${Math.min((currentPage + 1) * currentPageSize, totalRows)} of ${totalRows}`}
          </Typography>
        </Box>

        {/* Bottom row: Pagination icons */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: isMobile ? 0.5 : 1,
            marginTop: isMobile ? 1 : 0,
          }}
        >
          <Tooltip title='Go to first page'>
            <IconButton
              onClick={() => handleChangePage(0)}
              disabled={currentPage === 0}
              size='small'
              sx={{
                fontSize: isMobile ? '0.8rem' : '0.9rem',
              }}
            >
              <SkipPreviousIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title='Go to previous page'>
            <IconButton
              onClick={() => handleChangePage(currentPage - 1)}
              disabled={currentPage === 0}
              size='small'
              sx={{
                fontSize: isMobile ? '0.8rem' : '0.9rem',
              }}
            >
              <NavigateBeforeIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title='Go to next page'>
            <IconButton
              onClick={() => handleChangePage(currentPage + 1)}
              disabled={currentPage >= totalPages - 1}
              size='small'
              sx={{
                fontSize: isMobile ? '0.8rem' : '0.9rem',
              }}
            >
              <NavigateNextIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title='Go to last page'>
            <IconButton
              onClick={() => handleChangePage(totalPages - 1)}
              disabled={currentPage >= totalPages - 1}
              size='small'
              sx={{
                fontSize: isMobile ? '0.8rem' : '0.9rem',
              }}
            >
              <SkipNextIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>
    )
  }

  const handleExportExcel = () => {
    try {
      const filteredColumns = defaultColumns?.filter(
        (col) =>
          !hiddenFields.includes(col.field) &&
          (columnVisibilityRef.current[col.field] === true ||
            !(col.field in columnVisibilityRef.current)),
      )

      const columnHeaders = filteredColumns?.reduce((acc, col) => {
        acc[col.field] = col.headerName
        return acc
      }, {})

      const filteredRows = finalRows?.map((row) => {
        const filteredRow = {}
        filteredColumns?.forEach((col) => {
          filteredRow[col.headerName] =
            row[col.field] !== undefined ? row[col.field] : '-'
        })
        return filteredRow
      })

      const ws = utils.json_to_sheet(filteredRows)

      const headerRow = Object.values(columnHeaders)?.map((header) =>
        header.toUpperCase(),
      )
      utils.sheet_add_aoa(ws, [headerRow], { origin: 'A1' })

      const wb = utils.book_new()
      utils.book_append_sheet(wb, ws, 'Data')

      writeFile(wb, `${fileName}.xlsx`)
    } catch (err) {
      console.error('Error exporting to Excel:', err)
    }
  }

  const getTogglableColumns = (columns) => {
    return columns
      .filter((column) => !hiddenFields.includes(column.field))
      .map((column) => column.field)
  }

  const handleColumnsClick = () => {
    const preferencePanelState = gridPreferencePanelStateSelector(
      apiRef.current.state,
    )
    if (preferencePanelState.open) {
      apiRef.current.hidePreferences()
    } else {
      apiRef.current.showPreferences(GridPreferencePanelsValue.columns)
    }
  }

  useEffect(() => {
    columnVisibilityRef.current = columnVisibility
  }, [columnVisibility])

  const CustomToolbar = useCallback(() => {
    return (
      <GridToolbarContainer className='custom-toolbar'>
        <GridToolbarColumnsButton
          onClick={handleColumnsClick}
          ref={setAnchorEl}
          slotProps={{
            tooltip: { title: 'Select columns to display' },
            button: {
              sx: {
                textTransform: 'capitalize',
                fontSize: '0.9rem',
                fontWeight: 500,
              },
            },
          }}
        />

        <Tooltip title='Show filters'>
          <Button
            variant='text'
            size='small'
            onClick={() => setShowHeaderFilters(!showHeaderFilters)}
            startIcon={<FilterListRoundedIcon />}
            sx={{
              textTransform: 'capitalize',
              fontSize: '0.9rem',
              fontWeight: 500,
            }}
          >
            Filters
          </Button>
        </Tooltip>
        <GridToolbarDensitySelector
          slotProps={{
            tooltip: { title: 'Adjust row height' },
            button: {
              sx: {
                textTransform: 'capitalize',
                fontSize: '0.9rem',
                fontWeight: 500,
              },
            },
          }}
        />
        <Tooltip title='Export table data'>
          <Button
            variant='text'
            size='small'
            onClick={handleExportExcel}
            startIcon={<SaveAltRoundedIcon />}
            sx={{
              textTransform: 'capitalize',
              fontSize: '0.9rem',
              fontWeight: 500,
            }}
          >
            Export
          </Button>
        </Tooltip>
      </GridToolbarContainer>
    )
  }, [showHeaderFilters, finalRows])

  return (
    <Box sx={{ width: '100%', position: 'relative', zIndex: 10 }}>
      <Box sx={{ position: 'absolute', top: { xs: -20, md: 5 }, left: 6 }}>
        <Typography
          variant='h6'
          className='grid-result-count'
          fontSize='0.9rem'
          fontFamily='inter-regular'
        >
          {totalRows} Results
        </Typography>
      </Box>

      <DataGrid
        apiRef={apiRef}
        columns={modifiedColumns}
        rows={processedRows}
        // filterModel={filterModel}
        // onFilterModelChange={(newFilterModel) =>
        //   setFilterModel(newFilterModel || { items: [] })
        // }
        checkboxSelection={checkboxSelection}
        disableSelectionOnClick={disableSelectionOnClick}
        autoHeight={autoHeight}
        headerFilters={showHeaderFilters}
        slots={{
          toolbar: CustomToolbar,
          footer: CustomPagination,
          noRowsOverlay: () => (
            <Box
              sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: '100%',
                width: '100%',
                mt: 2,
              }}
            >
              No results found.
            </Box>
          ),
        }}
        slotProps={{
          panel: {
            anchorEl: anchorEl,
          },
          loadingOverlay: {
            variant: 'skeleton',
            noRowsVariant: 'skeleton',
          },
          columnsManagement: {
            getTogglableColumns,
          },
        }}
        rowReordering={isReordering}
        componentsProps={{
          toolbar: {
            showQuickFilter: true,
            quickFilterProps: { debounceMs: 500 },
          },
        }}
        loading={loader}
        getRowId={(row, index) => row.nid || row.id || `${index}`}
        columnVisibilityModel={columnVisibility}
        onColumnVisibilityModelChange={(newModel) =>
          setColumnVisibility(newModel)
        }
        disableRowSelectionOnClick
        sortModel={defaultSortModel}
        onSortModelChange={handleSortModelChange}
        onCellClick={handleCellClick}
        headerFilterHeight={
          selectedDensity === 'compact'
            ? 80
            : selectedDensity === 'comfortable'
              ? 45
              : 58
        }
        onDensityChange={(data) => setDensity(data)}
        onColumnResized={onColumnResized}
        sx={{
          border: 0,
          minHeight: rows?.length === 0 ? 200 : 'auto',
          '& .MuiDataGrid-columnHeaders': {
            borderLeft: 'none',
            borderRight: 'none',
            borderTop: '1px solid #d3d3d3',
            lineHeight: 'normal',
          },
          '& .MuiDataGrid-cell': {
            borderLeft: 'none',
            borderRight: 'none',
            fontFamily: 'inter-regular',
          },
          '& .MuiDataGrid-toolbarContainer': {
            display: 'flex',
            justifyContent: 'flex-end',
            gap: 1,
            paddingRight: 2,
            alignSelf: 'flex-end',
          },
          '& .MuiDataGrid-columnHeader': {
            borderRight: 'none',
            fontWeight: 'bold !important',
            color: 'var(--ternary-color) !important',
            fontSize: '0.8rem',
          },
          '& .MuiDataGrid-columnHeaderTitle': {
            textTransform: 'uppercase !important',
            fontFamily: 'inter-regular',
            color: 'var(--ternary-color) !important',
          },
          '& .MuiDataGrid-filterForm .MuiInputBase-input': {
            textTransform: 'none',
          },
          '& .MuiDataGrid-filterForm .MuiInputBase-input::placeholder': {
            textTransform: 'none',
          },
        }}
        {...props}
      />
    </Box>
  )
}

ASDataGrid1.propTypes = {
  columns: PropTypes.arrayOf(PropTypes.object).isRequired,
  rows: PropTypes.arrayOf(PropTypes.object).isRequired,
  pageSize: PropTypes.number,
  rowsPerPageOptions: PropTypes.arrayOf(PropTypes.number),
  checkboxSelection: PropTypes.bool,
  disableSelectionOnClick: PropTypes.bool,
  autoHeight: PropTypes.bool,
  cNameToAddAvatar: PropTypes.string,
  isReordering: PropTypes.bool,
  exportFileName: PropTypes.string,
  columnVisibilityModel: PropTypes.object,
  defaultSortModel: PropTypes.array,
  handleSortModelChange: PropTypes.func,
  handleCellClick: PropTypes.func,
  filterOptions: PropTypes.array,
  filterOptionsForRoles: PropTypes.array,
  loader: PropTypes.bool,
}

export default ASDataGrid1

// //my code
// import { useState } from 'react'
// import { DataGrid } from '@mui/x-data-grid'
// import { Button, TextField, Menu, MenuItem, IconButton } from '@mui/material'
// import MoreVertIcon from '@mui/icons-material/MoreVert'
// import SearchIcon from '@mui/icons-material/Search'
// import FilterAltIcon from '@mui/icons-material/FilterAlt'
// import { InputAdornment } from '../../../node_modules/@mui/material/index'

// const jioColors = {
//   primaryBlue: '#1B4E9B',
//   accentRed: '#E31C3D',
//   background: '#FFFFFF',
//   headerBg: '#DAE0EF',
//   rowEven: '#FFFFFF',
//   rowOdd: '#FFFFFF',
//   textPrimary: '#2D2D2D',
//   border: '#D0D0D0',
// }

// const TurnaroundPlanTable = ({
//   columns: initialColumns = [],
//   rows: initialRows = [],
//   title = 'Turnaround Plan Details',
//   onAddRow,
//   onDeleteRow,
//   onRowUpdate,
//   //   filterOptions = [],
//   paginationOptions = [5, 10, 20],
// }) => {
//   const [rows, setRows] = useState(initialRows)
//   const [searchText, setSearchText] = useState('')
//   const [anchorEl, setAnchorEl] = useState(null)
//   const [selectedRow, setSelectedRow] = useState(null)
//   const [isFilterActive, setIsFilterActive] = useState(false)
//   const [paginationModel, setPaginationModel] = useState({
//     page: 0,
//     pageSize: paginationOptions[0],
//   })

//   const handleSearchChange = (event) => {
//     setSearchText(event.target.value)
//   }

//   const handleFilterClick = () => {
//     setIsFilterActive(!isFilterActive)
//   }

//   const filteredRows = rows.filter((row) => {
//     const matchesSearch = Object.values(row).some((value) =>
//       String(value).toLowerCase().includes(searchText.toLowerCase()),
//     )
//     const matchesDuration = !isFilterActive || row.durationHrs > 100
//     return matchesSearch && matchesDuration
//   })

//   const handleMenuClick = (event, row) => {
//     setAnchorEl(event.currentTarget)
//     setSelectedRow(row)
//   }

//   const handleMenuClose = () => {
//     setAnchorEl(null)
//     setSelectedRow(null)
//   }

//   const handleDeleteRow = (id) => {
//     const updatedRows = rows.filter((row) => row?.id !== id)
//     setRows(updatedRows)
//     onDeleteRow?.(id) // Call the onDeleteRow prop if provided
//     handleMenuClose()
//   }

//   const processRowUpdate = (newRow) => {
//     const updatedRow = { ...newRow, isNew: false }
//     const updatedRows = rows.map((row) =>
//       row?.id === newRow?.id ? updatedRow : row,
//     )
//     setRows(updatedRows)
//     onRowUpdate?.(updatedRow) // Call the onRowUpdate prop if provided
//     return updatedRow
//   }

//   const handleAddRow = () => {
//     const newRow = {
//       id: rows?.length ? rows[rows?.length - 1]?.id + 1 : 1,
//       ...Object.fromEntries(initialColumns.map((col) => [col.field, ''])),
//     }
//     const updatedRows = [...rows, newRow]
//     setRows(updatedRows)
//     onAddRow?.(newRow) // Call the onAddRow prop if provided
//   }

//   const columns = [
//     ...initialColumns,
//     {
//       field: 'actions',
//       headerName: 'Actions',
//       width: 180,
//       cellClassName: 'with-border',
//       textAlign: 'center',
//       renderCell: (params) => (
//         <>
//           <IconButton
//             onClick={(event) => handleMenuClick(event, params.row)}
//             aria-label='more'
//             aria-controls='long-menu'
//             aria-haspopup='true'
//           >
//             <MoreVertIcon />
//           </IconButton>
//           <Menu
//             id='long-menu'
//             anchorEl={anchorEl}
//             keepMounted
//             open={Boolean(anchorEl)}
//             onClose={handleMenuClose}
//             sx={{ boxShadow: 'none' }}
//           >
//             <MenuItem
//               onClick={() => handleDeleteRow(selectedRow?.id)}
//               sx={{ boxShadow: 'none' }}
//             >
//               Delete
//             </MenuItem>
//           </Menu>
//         </>
//       ),
//       flex: 0.3,
//     },
//   ]

//   return (
//     <div
//       style={{
//         height: '81vh',
//         width: '100%',
//         padding: 20,
//         backgroundColor: '#FAFAFB',
//         borderRadius: '8px',
//         borderBottom: 'none',
//       }}
//     >
//       <div
//         style={{
//           display: 'flex',
//           justifyContent: 'space-between',
//           alignItems: 'center',
//           padding: '5px',
//         }}
//       >
//         <h3
//           style={{
//             color: '#040510',
//             fontSize: '1.5rem',
//             fontWeight: 300,
//             letterSpacing: '0.5px',
//           }}
//         >
//           {title}
//         </h3>
//       </div>
//       <div
//         style={{
//           display: 'flex',
//           justifyContent: 'space-between',
//           alignItems: 'center',
//           marginTop: '20px',
//           marginBottom: '10px',
//         }}
//       >
//         <div style={{ display: 'flex', alignItems: 'center' }}>
//           <label
//             style={{ marginRight: '8px', fontWeight: '300', color: '#8A9BC2' }}
//           >
//             Show:
//           </label>
//           <select
//             value={paginationModel.pageSize}
//             onChange={(e) => {
//               const newSize = Number(e.target.value)
//               setPaginationModel({
//                 ...paginationModel,
//                 pageSize: newSize,
//                 page: 0,
//               })
//             }}
//             style={{ padding: '4px' }}
//           >
//             {paginationOptions.map((option) => (
//               <option key={option} value={option}>
//                 {option}
//               </option>
//             ))}
//           </select>
//           <label
//             style={{ marginLeft: '8px', fontWeight: '300', color: '#8A9BC2' }}
//           >
//             Entries
//           </label>
//         </div>
//         <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
//           <TextField
//             variant='outlined'
//             placeholder='Search...'
//             value={searchText}
//             onChange={handleSearchChange}
//             style={{
//               width: '250px',
//               borderRadius: '8px',
//               backgroundColor: jioColors.background,
//               color: '#8A9BC2',
//             }}
//             InputProps={{
//               endAdornment: (
//                 <InputAdornment position='start'>
//                   <SearchIcon />
//                 </InputAdornment>
//               ),
//             }}
//           />
//           <IconButton
//             aria-label='filter'
//             onClick={handleFilterClick}
//             style={{
//               border: `1px solid ${jioColors.border}`,
//               borderRadius: '4px',
//               padding: '8px',
//               display: 'flex',
//               alignItems: 'center',
//               gap: '5px',
//               backgroundColor: isFilterActive
//                 ? jioColors.primaryBlue
//                 : 'inherit',
//               color: isFilterActive ? jioColors.background : 'inherit',
//               width: '100px',
//             }}
//           >
//             <FilterAltIcon color={isFilterActive ? 'inherit' : 'inherit'} />
//             <span style={{ fontSize: '0.875rem', color: '#2A3ACD' }}>
//               Filter
//             </span>
//           </IconButton>
//         </div>
//       </div>
//       <div style={{ height: 'calc(100% - 150px)', width: '100%' }}>
//         <DataGrid
//           rows={filteredRows}
//           columns={columns}
//           rowHeight={35}
//           processRowUpdate={processRowUpdate}
//           paginationModel={paginationModel}
//           onPaginationModelChange={(model) => setPaginationModel(model)}
//           rowsPerPageOptions={paginationOptions}
//           pagination
//           disableColumnResize
//           disableSelectionOnClick
//           getRowClassName={(params) =>
//             params.indexRelativeToCurrentPage % 2 === 0 ? 'even-row' : 'odd-row'
//           }
//           sx={{
//             borderRadius: '4px',
//             border: `1px solid ${jioColors.border}`,
//             '& .MuiDataGrid-columnHeaders': {
//               backgroundColor: '#F2F3F8',
//               color: '#3E4E75',
//               fontSize: '0.875rem',
//               fontWeight: 600,
//               borderBottom: `2px solid ${'#DAE0EF'}`,
//               borderTopLeftRadius: '14px',
//               borderTopRightRadius: '14px',
//             },
//             '& .MuiDataGrid-cell': {
//               borderRight: `none`,
//               borderBottom: `1px solid ${'#DAE0EF'}`,
//               color: '#3E4E75',
//             },
//             '& .MuiDataGrid-row': {
//               borderBottom: `1px solid ${jioColors.border}`,
//             },
//             '& .even-row': {
//               backgroundColor: jioColors.rowEven,
//             },
//             '& .odd-row': {
//               backgroundColor: jioColors.rowOdd,
//             },
//           }}
//         />
//       </div>
//       <Button
//         variant='contained'
//         style={{
//           marginTop: 20,
//           backgroundColor: jioColors.primaryBlue,
//           color: jioColors.background,
//           borderRadius: '4px',
//           padding: '8px 24px',
//           textTransform: 'none',
//           fontSize: '0.875rem',
//           fontWeight: 500,
//         }}
//         onClick={handleAddRow}
//         sx={{
//           '&:hover': {
//             backgroundColor: '#143B6F',
//             boxShadow: 'none',
//           },
//         }}
//       >
//         Add Item
//       </Button>
//     </div>
//   )
// }

// export default TurnaroundPlanTable

// // pagination_
// import { useEffect, useMemo, useState } from 'react'
// import PropTypes from 'prop-types'
// import { DataGrid, useGridApiRef } from '@mui/x-data-grid'
// import {
//   Avatar,
//   Box,
//   Typography,
//   Select,
//   MenuItem,
//   IconButton,
//   Tooltip,
//   useMediaQuery,
//   Button,
//   InputLabel,
// } from '@mui/material'
// import SkipNextIcon from '@mui/icons-material/SkipNext'
// import SkipPreviousIcon from '@mui/icons-material/SkipPrevious'
// import NavigateNextIcon from '@mui/icons-material/NavigateNext'
// import NavigateBeforeIcon from '@mui/icons-material/NavigateBefore'

// const ASDataGrid = ({
//   columns,
//   rows,
//   pageSize = 10,
//   rowsPerPageOptions = [10, 20, 50],
//   checkboxSelection = false,
//   disableSelectionOnClick = true,
//   autoHeight = true,
//   cNameToAddAvatar,
//   isReordering = false,
//   defaultSortModel,
//   handleSortModelChange,
//   handleCellClick,
//   loader = false,
//   ...props
// }) => {
//   const apiRef = useGridApiRef(null)
//   const isMobile = useMediaQuery('(max-width:900px)')

//   // Use internal state for rows to allow adding new rows
//   const [gridRows, setGridRows] = useState(rows)

//   // Update internal state when prop rows change
//   useEffect(() => {
//     setGridRows(rows)
//   }, [rows])

//   const [currentPage, setCurrentPage] = useState(0)
//   const [currentPageSize, setCurrentPageSize] = useState(pageSize)
//   const [totalPages, setTotalPages] = useState(gridRows?.length)
//   const [totalRows, setTotalRows] = useState(gridRows?.length)
//   const [processedRows, setProcessedRows] = useState([])
//   // const [finalRows, setFinalRows] = useState([])
//   const [resizedColumns, setResizedColumns] = useState({})

//   // Update rows based on sorting and pagination using gridRows state
//   useEffect(() => {
//     let updatedRows = [...gridRows]

//     // Apply sorting if a sort model exists
//     if (defaultSortModel?.length > 0) {
//       updatedRows.sort((a, b) => {
//         for (let sort of defaultSortModel) {
//           const field = sort.field
//           const sortOrder =
//             sort.sort === 'asc' ? 1 : sort.sort === 'desc' ? -1 : 0
//           if (sortOrder === 0) return 0

//           let valueA = a[field] != null ? String(a[field]).trim() : ''
//           let valueB = b[field] != null ? String(b[field]).trim() : ''

//           const numA = parseFloat(valueA)
//           const numB = parseFloat(valueB)
//           const isNumericA = !isNaN(numA) && valueA === numA.toString()
//           const isNumericB = !isNaN(numB) && valueB === numB.toString()

//           if (isNumericA && isNumericB) {
//             if (numA < numB) return -1 * sortOrder
//             if (numA > numB) return 1 * sortOrder
//           } else {
//             if (valueA.toLowerCase() < valueB.toLowerCase())
//               return -1 * sortOrder
//             if (valueA.toLowerCase() > valueB.toLowerCase())
//               return 1 * sortOrder
//           }
//         }
//         return 0
//       })
//     }

//     // Pagination
//     const start = currentPage * currentPageSize
//     const end = start + currentPageSize
//     // setFinalRows(updatedRows)
//     setProcessedRows(updatedRows.slice(start, end))
//     setTotalRows(updatedRows.length)
//     setTotalPages(Math.ceil(updatedRows.length / currentPageSize))
//   }, [gridRows, currentPage, currentPageSize, defaultSortModel])

//   const handleChangePage = (newPage) => {
//     setCurrentPage(newPage)
//   }

//   const handleChangePageSize = (event) => {
//     setCurrentPageSize(event.target.value)
//     setCurrentPage(0)
//   }

//   const onColumnResized = (params) => {
//     if (params.column) {
//       const field = params.column.getColDef().field
//       setResizedColumns((prev) => ({
//         ...prev,
//         [field]: true,
//       }))
//     }
//   }

//   const defaultColumns = useMemo(() => {
//     return columns.map((col) => ({
//       ...col,
//       flex: !isMobile && !resizedColumns[col.field] ? 1 : null,
//     }))
//   }, [columns, isMobile, resizedColumns])

//   const modifiedColumns = useMemo(() => {
//     return defaultColumns.map((col) => {
//       if (col.field === cNameToAddAvatar) {
//         return {
//           ...col,
//           renderCell: (params) => {
//             const avatarColors = [
//               '#AB47BC',
//               '#D81B60',
//               '#FF6F00',
//               '#1B7C95',
//               '#00796B',
//               '#558B2F',
//               '#5C6BC0',
//               '#0277BD',
//             ]
//             const name = params.row?.[cNameToAddAvatar]
//             const initials =
//               name && name.trim()
//                 ? name.includes(' ')
//                   ? name.split(' ')[0].charAt(0) + name.split(' ')[1].charAt(0)
//                   : name.length === 1
//                     ? name.charAt(0) + 'X'
//                     : name.charAt(0) + name.charAt(1)
//                 : 'XX'
//             const getInitialsMagicNumber = (initials) => {
//               const numbers = initials
//                 .toLowerCase()
//                 .split('')
//                 .map((char) => char.charCodeAt(0))
//               const spice = numbers[0] < numbers[1] ? 0 : 1
//               return numbers.reduce((acc, n) => acc + n, 0) + spice
//             }
//             const magicNumber = getInitialsMagicNumber(initials)
//             const colorIndex = magicNumber % avatarColors.length
//             const randomColor = avatarColors[colorIndex]

//             return (
//               <Tooltip title={params?.value || ''}>
//                 <Box
//                   sx={{
//                     display: 'flex',
//                     alignItems: 'center',
//                     height: '100%',
//                     cursor: 'pointer',
//                   }}
//                 >
//                   <Avatar
//                     sx={{
//                       width: '1.875rem',
//                       height: '1.875rem',
//                       marginRight: 1,
//                       textTransform: 'uppercase',
//                       fontSize: '.8rem',
//                       backgroundColor: randomColor,
//                     }}
//                   >
//                     <Typography sx={{ fontSize: '.8rem' }}>
//                       {initials}
//                     </Typography>
//                   </Avatar>
//                   <Typography
//                     variant='body2'
//                     sx={{
//                       whiteSpace: 'nowrap',
//                       overflow: 'hidden',
//                       textOverflow: 'ellipsis',
//                     }}
//                   >
//                     {name}
//                   </Typography>
//                 </Box>
//               </Tooltip>
//             )
//           },
//         }
//       }
//       return { ...col }
//     })
//   }, [defaultColumns, cNameToAddAvatar])

//   const CustomPagination = () => {
//     return (
//       <Box
//         sx={{
//           display: 'flex',
//           flexDirection: isMobile ? 'column' : 'row',
//           alignItems: 'center',
//           justifyContent: isMobile ? 'center' : 'flex-end',
//           padding: isMobile ? 1 : 2,
//           gap: isMobile ? 1 : 2,
//         }}
//       >
//         <Box
//           sx={{
//             display: 'flex',
//             alignItems: 'center',
//             justifyContent: 'center',
//             gap: isMobile ? 1 : 2,
//             width: isMobile ? '100%' : 'auto',
//           }}
//         >
//           <InputLabel
//             htmlFor='row-per-page-select'
//             id='row-per-page-select-label'
//             sx={{
//               fontSize: isMobile ? '0.8rem' : '0.9rem',
//               color: 'var(--ternary-color) !important',
//             }}
//           >
//             Rows per page:
//           </InputLabel>
//           <Select
//             inputProps={{ id: 'row-per-page-select' }}
//             labelId='row-per-page-select-label'
//             value={currentPageSize}
//             onChange={handleChangePageSize}
//             size='small'
//             sx={{
//               fontSize: isMobile ? '0.8rem' : '0.9rem',
//               width: isMobile ? '80px' : 'auto',
//               border: 'none',
//               '& fieldset': { border: 'none' },
//             }}
//           >
//             {rowsPerPageOptions.map((option) => (
//               <MenuItem key={option} value={option}>
//                 {option}
//               </MenuItem>
//             ))}
//           </Select>

//           <Typography
//             variant='body2'
//             sx={{ fontSize: isMobile ? '0.8rem' : '0.9rem' }}
//           >
//             {totalRows === 0
//               ? '0-0 of 0'
//               : `${currentPage * currentPageSize + 1}-${Math.min(
//                   (currentPage + 1) * currentPageSize,
//                   totalRows,
//                 )} of ${totalRows}`}
//           </Typography>
//         </Box>

//         <Box
//           sx={{
//             display: 'flex',
//             alignItems: 'center',
//             justifyContent: 'center',
//             gap: isMobile ? 0.5 : 1,
//             marginTop: isMobile ? 1 : 0,
//           }}
//         >
//           <Tooltip title='Go to first page'>
//             <IconButton
//               onClick={() => handleChangePage(0)}
//               disabled={currentPage === 0}
//               size='small'
//             >
//               <SkipPreviousIcon />
//             </IconButton>
//           </Tooltip>
//           <Tooltip title='Go to previous page'>
//             <IconButton
//               onClick={() => handleChangePage(currentPage - 1)}
//               disabled={currentPage === 0}
//               size='small'
//             >
//               <NavigateBeforeIcon />
//             </IconButton>
//           </Tooltip>
//           <Tooltip title='Go to next page'>
//             <IconButton
//               onClick={() => handleChangePage(currentPage + 1)}
//               disabled={currentPage >= totalPages - 1}
//               size='small'
//             >
//               <NavigateNextIcon />
//             </IconButton>
//           </Tooltip>
//           <Tooltip title='Go to last page'>
//             <IconButton
//               onClick={() => handleChangePage(totalPages - 1)}
//               disabled={currentPage >= totalPages - 1}
//               size='small'
//             >
//               <SkipNextIcon />
//             </IconButton>
//           </Tooltip>
//         </Box>
//       </Box>
//     )
//   }

//   // Handler to add a new row/item to the grid
//   const handleAddItem = () => {
//     const newRow = columns.reduce((acc, col) => {
//       acc[col.field] = ''
//       return acc
//     }, {})
//     // Assign a unique id for the new row
//     newRow.id = `new-${Date.now()}`
//     setGridRows([...gridRows, newRow])
//   }

//   return (
//     <Box sx={{ width: '100%', position: 'relative', zIndex: 10 }}>
//       <Box sx={{ position: 'absolute', top: { xs: -20, md: 5 }, left: 6 }}>
//         <Typography
//           variant='h6'
//           className='grid-result-count'
//           fontSize='0.9rem'
//           fontFamily='inter-regular'
//         >
//           {totalRows} Results
//         </Typography>
//       </Box>

//       <DataGrid
//         apiRef={apiRef}
//         columns={modifiedColumns}
//         rows={processedRows}
//         checkboxSelection={checkboxSelection}
//         disableSelectionOnClick={disableSelectionOnClick}
//         autoHeight={autoHeight}
//         slots={{
//           footer: CustomPagination,
//           noRowsOverlay: () => (
//             <Box
//               sx={{
//                 display: 'flex',
//                 justifyContent: 'center',
//                 alignItems: 'center',
//                 height: '100%',
//                 width: '100%',
//                 mt: 2,
//               }}
//             >
//               No results found.
//             </Box>
//           ),
//         }}
//         componentsProps={{
//           toolbar: {
//             showQuickFilter: true,
//             quickFilterProps: { debounceMs: 500 },
//           },
//         }}
//         loading={loader}
//         getRowId={(row, index) => row.nid || row.id || `${index}`}
//         sortModel={defaultSortModel}
//         onSortModelChange={handleSortModelChange}
//         onCellClick={handleCellClick}
//         onColumnResized={onColumnResized}
//         rowReordering={isReordering}
//         sx={{
//           border: 0,
//           minHeight: gridRows?.length === 0 ? 200 : 'auto',
//           '& .MuiDataGrid-columnHeaders': {
//             borderLeft: 'none',
//             borderRight: 'none',
//             borderTop: '1px solid #d3d3d3',
//             lineHeight: 'normal',
//           },
//           '& .MuiDataGrid-cell': {
//             borderLeft: 'none',
//             borderRight: 'none',
//             fontFamily: 'inter-regular',
//           },
//           '& .MuiDataGrid-toolbarContainer': {
//             display: 'flex',
//             justifyContent: 'flex-end',
//             gap: 1,
//             paddingRight: 2,
//           },
//           '& .MuiDataGrid-columnHeader': {
//             borderRight: 'none',
//             fontWeight: 'bold !important',
//             color: 'var(--ternary-color) !important',
//             fontSize: '0.8rem',
//           },
//           '& .MuiDataGrid-columnHeaderTitle': {
//             textTransform: 'uppercase !important',
//             fontFamily: 'inter-regular',
//             color: 'var(--ternary-color) !important',
//           },
//           '& .MuiDataGrid-filterForm .MuiInputBase-input': {
//             textTransform: 'none',
//           },
//           '& .MuiDataGrid-filterForm .MuiInputBase-input::placeholder': {
//             textTransform: 'none',
//           },
//         }}
//         {...props}
//       />
//       {/* "Add Item" Button below the DataGrid */}
//       <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
//         <Button variant='contained' color='primary' onClick={handleAddItem}>
//           Add Item
//         </Button>
//       </Box>
//     </Box>
//   )
// }

// ASDataGrid.propTypes = {
//   columns: PropTypes.arrayOf(PropTypes.object).isRequired,
//   rows: PropTypes.arrayOf(PropTypes.object).isRequired,
//   pageSize: PropTypes.number,
//   rowsPerPageOptions: PropTypes.arrayOf(PropTypes.number),
//   checkboxSelection: PropTypes.bool,
//   disableSelectionOnClick: PropTypes.bool,
//   autoHeight: PropTypes.bool,
//   cNameToAddAvatar: PropTypes.string,
//   isReordering: PropTypes.bool,
//   defaultSortModel: PropTypes.array,
//   handleSortModelChange: PropTypes.func,
//   handleCellClick: PropTypes.func,
//   loader: PropTypes.bool,
// }

// export default ASDataGrid

// //pagination_

//mycode or myform

// import { useState } from 'react'
// import { DataGrid } from '@mui/x-data-grid'
// import { Button, TextField, Menu, MenuItem, IconButton } from '@mui/material'
// import MoreVertIcon from '@mui/icons-material/MoreVert'
// import { InputAdornment } from '../../../node_modules/@mui/material/index'
// import SearchIcon from '@mui/icons-material/Search'
// import FilterAltIcon from '@mui/icons-material/FilterAlt'

// const jioColors = {
//   primaryBlue: '#1B4E9B',
//   accentRed: '#E31C3D',
//   background: '#FFFFFF',
//   headerBg: '#DAE0EF',
//   rowEven: '#FFFFFF',
//   rowOdd: '#FFFFFF',
//   textPrimary: '#2D2D2D',
//   border: '#D0D0D0',
// }

// const TurnaroundPlanTable = () => {
//   const [rows, setRows] = useState([
//     {
//       id: 1,
//       activities: 'Preheater cleaning',
//       taFrom: '2024-09-04', // Store as ISO string
//       taTo: '2024-09-05',
//       durationHrs: 120,
//       period: '',
//       remark: '',
//     },
//     {
//       id: 2,
//       activities: 'Strippers inspection',
//       taFrom: '2024-09-04', // Store as ISO string
//       taTo: '2024-09-05',
//       durationHrs: 144,
//       period: '7.5',
//       remark: '',
//     },
//   ])

//   const [searchText, setSearchText] = useState('')
//   const [anchorEl, setAnchorEl] = useState(null)
//   const [selectedRow, setSelectedRow] = useState(null)
//   const [isFilterActive, setIsFilterActive] = useState(false)
//   const [paginationModel, setPaginationModel] = useState({
//     page: 0,
//     pageSize: 10,
//   })

//   const handleSearchChange = (event) => {
//     setSearchText(event.target.value)
//   }

//   // const filteredRows = rows.filter((row) =>
//   //   Object.values(row).some((value) =>
//   //     String(value).toLowerCase().includes(searchText.toLowerCase()),
//   //   ),
//   // )

//   const handleFilterClick = () => {
//     setIsFilterActive(!isFilterActive)
//   }

//   const filteredRows = rows.filter((row) => {
//     const matchesSearch = Object.values(row).some((value) =>
//       String(value).toLowerCase().includes(searchText.toLowerCase()),
//     )
//     const matchesDuration = !isFilterActive || row.durationHrs > 100
//     return matchesSearch && matchesDuration
//   })

//   const handleMenuClick = (event, row) => {
//     setAnchorEl(event.currentTarget)
//     setSelectedRow(row)
//   }

//   const handleMenuClose = () => {
//     setAnchorEl(null)
//     setSelectedRow(null)
//   }

//   const handleDeleteRow = (id) => {
//     setRows((prevRows) => prevRows.filter((row) => row?.id !== id))
//     handleMenuClose()
//   }

//   const processRowUpdate = (newRow) => {
//     if (!rows?.some((row) => row?.id === newRow?.id)) {
//       console.error(`No row with id ${newRow?.id}`)
//       return newRow
//     }
//     const updatedRow = { ...newRow, isNew: false }
//     setRows(rows?.map((row) => (row?.id === newRow?.id ? updatedRow : row)))
//     return updatedRow
//   }
//   // Function to add a new row
//   const handleAddRow = () => {
//     const newRow = {
//       id: rows?.length ? rows[rows?.length - 1]?.id + 1 : 1, // Incremental ID
//       activities: '',
//       taFrom: '',
//       taTo: '',
//       durationHrs: '',
//       period: '',
//       remark: '',
//     }
//     setRows([...rows, newRow])
//   }

//   const columns = [
//     {
//       field: 'id',
//       headerName: 'Sr. No.',
//       width: 100,
//       cellClassName: 'with-border',
//       flex: 0.2,
//     },
//     {
//       field: 'activities',
//       headerName: 'Activities',
//       width: 300,
//       editable: true,
//       cellClassName: 'with-border',
//       flex: 1,
//     },

//     // {
//     //   field: 'activity',
//     //   headerName: 'Activities',
//     //   width: 200,
//     //   renderCell: (params) => (
//     //     // <Box
//     //     //   sx={{
//     //     //     border: '1px solid #ccc', // Added box around input fields
//     //     //     borderRadius: '4px',
//     //     //     padding: '2px',
//     //     //     width: '100%',
//     //     //   }}
//     //     // >
//     //     <TextField
//     //       variant='outlined'
//     //       size='small'
//     //       fullWidth
//     //       value={params.row.activity}
//     //     />
//     //     // </Box>
//     //   ),
//     // },
//     {
//       field: 'taFrom',
//       headerName: 'TA - From',
//       type: 'date',
//       width: 180,
//       editable: true,
//       textAlign: 'center',
//       valueFormatter: (params) =>
//         params?.value
//           ? new Date(params?.value).toISOString().split('T')[0]
//           : '',
//       valueGetter: (params) => (params?.value ? new Date(params?.value) : null),
//       flex: 0.4,
//     },
//     {
//       field: 'taTo',
//       headerName: 'TA - To',
//       type: 'date',
//       width: 180,
//       editable: true,
//       textAlign: 'center',
//       valueFormatter: (params) =>
//         params?.value
//           ? new Date(params?.value).toISOString().split('T')[0]
//           : '',
//       valueGetter: (params) => (params?.value ? new Date(params?.value) : null),

//       flex: 0.4,
//     },
//     {
//       field: 'durationHrs',
//       headerName: 'Duration Hrs',
//       width: 150,
//       editable: true,
//       cellClassName: 'with-border',
//       flex: 0.5,
//     },
//     {
//       field: 'period',
//       headerName: 'Period',
//       width: 250,
//       editable: true,
//       cellClassName: 'with-border',
//       flex: 0.5,
//     },
//     {
//       field: 'remark',
//       headerName: 'Remark',
//       width: 200,
//       editable: true,
//       cellClassName: 'with-border',
//       flex: 0.5,
//     },
//     {
//       field: 'actions',
//       headerName: 'Actions',
//       width: 180,
//       cellClassName: 'with-border',
//       textAlign: 'center',
//       renderCell: (params) => (
//         <>
//           <IconButton
//             onClick={(event) => handleMenuClick(event, params.row)}
//             aria-label='more'
//             aria-controls='long-menu'
//             aria-haspopup='true'
//           >
//             <MoreVertIcon />
//           </IconButton>
//           <Menu
//             id='long-menu'
//             anchorEl={anchorEl}
//             keepMounted
//             open={Boolean(anchorEl)}
//             onClose={handleMenuClose}
//             sx={{ boxShadow: 'none' }}
//           >
//             <MenuItem
//               onClick={() => handleDeleteRow(selectedRow.id)}
//               sx={{ boxShadow: 'none' }}
//             >
//               Delete
//             </MenuItem>
//             {/* Add more menu items as needed */}
//           </Menu>
//         </>
//       ),
//       flex: 0.3,
//     },
//   ]

//   return (
//     <div
//       style={{
//         height: '81vh',
//         width: '100%',
//         padding: 20,
//         backgroundColor: '#FAFAFB',
//         borderRadius: '8px',
//         borderBottom: 'none',
//       }}
//     >
//       {/* Updated heading style */}
//       <div
//         style={{
//           display: 'flex',
//           justifyContent: 'space-between',
//           alignItems: 'center',
//           // backgroundColor: jioColors.headerBg,
//           padding: '5px',
//         }}
//       >
//         <h3
//           style={{
//             color: '#040510',
//             // color: jioColors.background,
//             // marginBottom:"10px" ,
//             fontSize: '1.5rem', // Larger font size
//             // fontSize: '1.25rem',
//             fontWeight: 300,
//             letterSpacing: '0.5px',
//           }}
//         >
//           Turnaround Plan Details
//         </h3>
//       </div>
//       <div
//         style={{
//           display: 'flex',
//           justifyContent: 'space-between',
//           alignItems: 'center',
//           marginTop: '20px',
//           marginBottom: '10px',
//         }}
//       >
//         {/* Show entries dropdown */}
//         <div style={{ display: 'flex', alignItems: 'center' }}>
//           <label
//             style={{ marginRight: '8px', fontWeight: '300', color: '#8A9BC2' }}
//           >
//             Show:
//           </label>
//           <select
//             value={paginationModel.pageSize}
//             onChange={(e) => {
//               const newSize = Number(e.target.value)
//               // Update pageSize in the controlled pagination model and reset to page 0
//               setPaginationModel({
//                 ...paginationModel,
//                 pageSize: newSize,
//                 page: 0,
//               })
//             }}
//             style={{ padding: '4px' }}
//           >
//             <option value={5}>5</option>
//             <option value={10}>10</option>
//             <option value={20}>20</option>
//           </select>
//           <label
//             style={{ marginLeft: '8px', fontWeight: '300', color: '#8A9BC2' }}
//           >
//             Entries
//           </label>
//         </div>
//         {/* Search box and filter icon */}
//         <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
//           <TextField
//             variant='outlined'
//             placeholder='Search...'
//             value={searchText}
//             onChange={handleSearchChange}
//             style={{
//               width: '250px',
//               borderRadius: '8px',
//               backgroundColor: jioColors.background,
//               color: '#8A9BC2',
//             }}
//             InputProps={{
//               endAdornment: (
//                 <InputAdornment position='start'>
//                   <SearchIcon />
//                 </InputAdornment>
//               ),
//             }}
//           />
//           <IconButton
//             aria-label='filter'
//             onClick={handleFilterClick}
//             style={{
//               border: `1px solid ${jioColors.border}`,
//               borderRadius: '4px',
//               padding: '8px',
//               display: 'flex',
//               alignItems: 'center',
//               gap: '5px',
//               backgroundColor: isFilterActive
//                 ? jioColors.primaryBlue
//                 : 'inherit',
//               color: isFilterActive ? jioColors.background : 'inherit',
//               width: '100px',
//             }}
//           >
//             <FilterAltIcon color={isFilterActive ? 'inherit' : 'inherit'} />
//             <span style={{ fontSize: '0.875rem', color: '#2A3ACD' }}>
//               Filter
//             </span>
//           </IconButton>
//         </div>
//       </div>

//       {/* Grid Table */}

//       <div style={{ height: 'calc(100% - 150px)', width: '100%' }}>
//         <DataGrid
//           rows={filteredRows}
//           columns={columns}
//           rowHeight={35}
//           processRowUpdate={processRowUpdate}
//           paginationModel={paginationModel} // Use controlled pagination model
//           onPaginationModelChange={(model) => setPaginationModel(model)}
//           rowsPerPageOptions={[5, 10, 20]}
//           pagination
//           // checkboxSelection
//           disableColumnResize
//           disableSelectionOnClick
//           getRowClassName={(params) =>
//             params.indexRelativeToCurrentPage % 2 === 0 ? 'even-row' : 'odd-row'
//           }
//           sx={{
//             borderRadius: '4px',
//             // border: `none`,
//             border: `1px solid ${jioColors.border}`,
//             borderLeft: `1px solid ${jioColors.border}`,
//             borderRight: `1px solid ${jioColors.border}`,
//             '& .MuiDataGrid-columnHeaders': {
//               backgroundColor: '#F2F3F8',
//               color: '#3E4E75',
//               fontSize: '0.875rem',
//               fontWeight: 600,
//               borderBottom: `2px solid ${'#DAE0EF'}`,
//               borderTopLeftRadius: '14px', // Added radius to top left corner
//               borderTopRightRadius: '14px', // Added radius to top right corner
//             },
//             '& .MuiDataGrid-columnHeaderTitleContainer': {
//               backgroundColor: '#F2F3F8',
//             },
//             '& .MuiDataGrid-cell': {
//               borderRight: `none`,
//               borderBottom: `1px solid ${'#DAE0EF'}`,
//               color: '#3E4E75',
//             },
//             '& .MuiDataGrid-row': {
//               borderBottom: `1px solid ${jioColors.border}`,
//             },
//             '& .even-row': {
//               backgroundColor: jioColors.rowEven,
//             },
//             '& .odd-row': {
//               backgroundColor: jioColors.rowOdd,
//             },
//             '& .MuiDataGrid-footerContainer': {
//               borderTop: `1px solid ${jioColors.border}`,
//               borderBottom: 'none',
//             },
//             '& .MuiDataGrid-root': {
//               border: 'none',
//             },
//             '& .MuiDataGrid-cell:last-child': {
//               borderRight: 'none',
//             },
//             '& .MuiDataGrid-cellEmpty': {
//               display: 'none',
//             },
//             '& .MuiDataGrid-menuIcon': {
//               display: 'none', // Hides the menu (3-dot button)
//             },
//             '& .MuiDataGrid-columnSeparator': {
//               display: 'none', // Hides the menu (3-dot button)
//               backgroundColor: '#F2F3F8',
//             },
//             '& .css-yrdy0g-MuiDataGrid-columnHeaderRow': {
//               // This is the parent row of the headers
//               backgroundColor: '#F2F3F8', // Set the background color of the header row
//             },
//             '& .MuiDataGrid-columnHeader': {
//               // This is the column header
//               backgroundColor: '#F2F3F8', // Set the background color of the header
//             },
//             '&  svg.MuiSvgIcon-root.MuiSvgIcon-fontSizeSmall.MuiDataGrid-sortIcon.css-ptiqhd-MuiSvgIcon-root':
//               {
//                 fill: '#2A3ACD' /* Replace #2A3ACD with your desired color */,
//               },
//           }}
//         />
//       </div>
//       {/* Add Row Button */}
//       <Button
//         variant='contained'
//         style={{
//           marginTop: 20,
//           backgroundColor: jioColors.primaryBlue,
//           color: jioColors.background,
//           borderRadius: '4px',
//           padding: '8px 24px',
//           textTransform: 'none',
//           fontSize: '0.875rem',
//           fontWeight: 500,
//         }}
//         onClick={handleAddRow}
//         sx={{
//           '&:hover': {
//             backgroundColor: '#143B6F',
//             boxShadow: 'none',
//           },
//         }}
//       >
//         Add Item
//       </Button>
//     </div>
//   )
// }

// export default TurnaroundPlanTable
// // NEWLY ADDED
