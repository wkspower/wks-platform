import React, { useEffect, useState } from 'react'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { Box, Typography } from '@mui/material'
import { DataGrid, GridToolbar } from '@mui/x-data-grid'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedProductionColDefsView from 'components/data-tables/CommonHeader/ProductionVolumeHeaderView'

const SimpleDataTable = () => {
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [rows, setRows] = useState()
  const [loading, setLoading] = useState(false)

  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const jioColors = {
    primaryBlue: '#387ec3',
    accentRed: '#E31C3D',
    background: '#FFFFFF',
    headerBg: '#0F3CC9',
    rowEven: '#FFFFFF',
    rowOdd: '#E8F1FF',
    textPrimary: '#2D2D2D',
    border: '#D0D0D0',
    darkTransparentBlue: 'rgba(127, 147, 206, 0.8)',
  }

  const fetchData = async () => {
    try {
      setLoading(true)
      const data = await DataService.getAOPMCCalculatedData(keycloak)
      const formattedData = data.map((item, index) => {
        // const isTPH = selectedUnit == 'TPD'
        const isTPH = false
        return {
          ...item,
          idFromApi: item?.id,
          normParametersFKId: item?.materialFKId.toLowerCase(),
          id: index,
          ...(isTPH && {
            april: item.april
              ? (item.april * 24).toFixed(2)
              : item.april || null,
            may: item.may ? (item.may * 24).toFixed(2) : item.may || null,
            june: item.june ? (item.june * 24).toFixed(2) : item.june || null,
            july: item.july ? (item.july * 24).toFixed(2) : item.july || null,
            august: item.august
              ? (item.august * 24).toFixed(2)
              : item.august || null,
            september: item.september
              ? (item.september * 24).toFixed(2)
              : item.september || null,
            october: item.october
              ? (item.october * 24).toFixed(2)
              : item.october || null,
            november: item.november
              ? (item.november * 24).toFixed(2)
              : item.november || null,
            december: item.december
              ? (item.december * 24).toFixed(2)
              : item.december || null,
            january: item.january
              ? (item.january * 24).toFixed(2)
              : item.january || null,
            february: item.february
              ? (item.february * 24).toFixed(2)
              : item.february || null,
            march: item.march
              ? (item.march * 24).toFixed(2)
              : item.march || null,
          }),
        }
      })
      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  const findAvg = (value, row) => {
    const months = [
      'april',
      'may',
      'june',
      'july',
      'august',
      'september',
      'october',
      'november',
      'december',
      'january',
      'february',
      'march',
    ]

    const values = months.map((month) => row[month] || 0)
    const sum = values.reduce((acc, val) => acc + val, 0)
    const avg = (sum / values.length).toFixed(2)

    return avg === '0.00' ? null : avg
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProductsAll(
          keycloak,
          // lowerVertName === 'meg' ? 'Production' : 'Grade',
          'All',
        )
        const productList = data.map((product) => ({
          id: product.id.toLowerCase(),
          displayName: product.displayName,
          name: product.name,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }

    getAllProducts()
    fetchData()
  }, [sitePlantChange, yearChanged, keycloak, lowerVertName])

  const productionColumns = getEnhancedProductionColDefsView({
    headerMap,
    allProducts,
    findAvg,
  })

  return (
    <Box
      sx={{
        // height: '120px',
        width: '100%',
        marginBottom: 0,
        padding: 0,
      }}
    >
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Typography
        variant='h6'
        gutterBottom
        fontWeight='bold'
        sx={{ marginTop: 2, marginBottom: 3 }}
      >
        Production Volume Data
      </Typography>

      <DataGrid
        rows={rows || []}
        columns={productionColumns.map((col) => ({
          ...col,
          filterable: false,
          sortable: true,
        }))}
        disableColumnMenu
        disableColumnFilter
        disableColumnSelector
        disableColumnSorting
        columnVisibilityModel={{
          maintenanceId: false,
          id: false,
          plantFkId: false,
          aopCaseId: false,
          aopType: false,
          aopYear: false,
          avgTph: false,
          NormParameterMonthlyTransactionId: false,
          aopStatus: false,
          idFromApi: false,
          period: false,
        }}
        rowHeight={35}
        slotProps={{
          toolbar: { setRows },
          loadingOverlay: {
            variant: 'linear-progress',
            noRowsVariant: 'skeleton',
          },
        }}
        getRowClassName={(params) => {
          return params.row.Particulars || params.row.Particulars2
            ? 'no-border-row'
            : params.indexRelativeToCurrentPage % 2 === 0
              ? 'even-row'
              : 'even-row'
        }}
        sx={{
          borderRadius: '0px',
          border: `1px solid ${jioColors.border}`,
          backgroundColor: jioColors.background,
          fontSize: '0.8rem',
          ' & .MuiDataGrid-columnHeaderTitleContainer:last-child:after .MuiDataGrid-columnHeaderTitleContainer:after':
            {
              bordeRight: 'none !important',
            },

          '& .MuiDataGrid-cell:last-child:after': {
            borderRight: 'none',
          },
          '& .MuiDataGrid-columnHeader:last-child:after': {
            borderRight: 'none',
          },
          '& .MuiDataGrid-columnHeader:last-child .MuiDataGrid-columnHeaderTitleContainer:after':
            {
              borderRight: 'none',
            },
          // Added direct rule for the title container without the pseudo-element:
          '& .MuiDataGrid-columnHeader:last-child .MuiDataGrid-columnHeaderTitleContainer':
            {
              borderRight: 'none',
            },
          '& .MuiDataGrid-cell.last-column, & .MuiDataGrid-columnHeaderTitleContainer.last-column & .MuiDataGrid-columnHeader.last-column':
            {
              borderRight: 'none',
            },

          // borderRight: `1px solid ${jioColors.border}`,
          '& .MuiDataGrid-root .MuiDataGrid-cell': {
            fontSize: '0.8rem',
            color: '#A9A9A9',
          },
          '& .MuiDataGrid-root': {
            borderRadius: '0px',
          },
          '& .MuiDataGrid-footerContainer': {
            display: 'none',
          },
          // Remove the direct right border from cells and headers
          '& .MuiDataGrid-cell, & .MuiDataGrid-columnHeaders & .MuiDataGrid-columnHeaderTitleContainer':
            {
              borderRight: 'none',
              position: 'relative',
            },
          // Apply a pseudo-element for a short right border on cells
          '& .MuiDataGrid-cell:after': {
            content: '""',
            position: 'absolute',
            right: 0,
            top: '50%',
            transform: 'translateY(-50%)',
            height: '60%',
            borderRight: `1px solid ${jioColors.border}`,
          },
          //Do not remove this prop (for Grouped row it can be usefull !!!!!)
          '& .MuiDataGrid-row.no-border-row .MuiDataGrid-cell:after': {
            backgroundColor: jioColors.rowOdd,
            borderRight: 'none !important',
          },

          // Apply a similar pseudo-element for header cells
          '& .MuiDataGrid-columnHeaders:after': {
            content: '""',
            position: 'absolute',
            right: 0,
            top: '50%',
            transform: 'translateY(-50%)',
            height: '60%', // Adjust as needed
            borderRight: `1px solid ${jioColors.border}`,
          },
          '& .MuiDataGrid-columnHeaderTitleContainer:after': {
            content: '""',
            position: 'absolute',
            right: 0,
            top: '50%',
            transform: 'translateY(-50%)',
            height: '60%', // Adjust as needed
            borderRight: `1px solid ${jioColors.border}`,
          },

          '& .MuiDataGrid-columnHeaders': {
            backgroundColor: '#FAFAFC',
            color: '#3E4E75',
            fontSize: '0.8rem',
            // fontWeight: 600,
            fontWeight: 'bold',
            borderBottom: `2px solid ${'#DAE0EF'}`,
            borderTopLeftRadius: '0px',
            borderTopRightRadius: '0px',
          },
          '& .MuiDataGrid-cell': {
            // borderRight: `1px solid ${jioColors.border}`,
            borderBottom: `1px solid ${'#DAE0EF'}`,
            color: '#3E4E75',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            fontSize: '0.8rem',
            cursor: 'pointer',
          },
          '& .MuiDataGrid-row': {
            borderBottom: `1px solid ${jioColors.border}`,
          },
          '& .even-row': {
            backgroundColor: jioColors.rowEven,
          },
          '& .no-border-row': {
            backgroundColor: jioColors.rowOdd,
          },

          '& .odd-row': {
            backgroundColor: jioColors.rowOdd,
          },
          '& .MuiDataGrid-toolbarContainer': {
            display: 'flex',
            justifyContent: 'flex-end',
            gap: 1,
            paddingRight: 2,
            alignSelf: 'flex-end',
          },
          // '& .MuiDataGrid-columnHeaders .last-column-header': {
          //   paddingRight: '16px',
          // },
          // '& .MuiDataGrid-cell.last-column-cell': {
          //   paddingRight: '16px',
          // },
          '& .MuiDataGrid-columnHeaderTitle': {
            fontWeight: 'bold', // Ensure column titles are bold
          },

          // '& .MuiDataGrid-columnHeader[data-field="Particulars"] .MuiDataGrid-columnHeaderTitle':
          //   {
          //     color: 'red',
          //   },

          '& .disabled-cell': {
            // color: '#A9A9A9 !important', // Grey color for disabled text
            // backgroundColor: '#F0F0F0 !important', // Light grey background
            // cursor: 'not-allowed !important', // Indicate it's not interactive
            // Optional: Add a border or other visual cues
            // border: '1px solid #ccc !important',
            opacity: 0.7, // Reduce opacity for a faded look
          },
          '& .disabled-header': {
            color: '#A9A9A9 !important', // Fade the header text color
            opacity: 0.7, // Optionally fade the header opacity as well
            // You might want to adjust other header styles if needed
          },
        }}
      />

      <Typography
        variant='h6'
        gutterBottom
        fontWeight='bold'
        sx={{ marginTop: 3 }}
      >
        Business Demand Data
      </Typography>
    </Box>
  )
}

export default SimpleDataTable
