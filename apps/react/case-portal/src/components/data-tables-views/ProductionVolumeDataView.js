import { Box } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { DataGrid } from '@mui/x-data-grid'
import getEnhancedProductionColDefsView from 'components/data-tables/CommonHeader/ProductionVolumeHeaderView'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

const ProductionVolumeDataView = () => {
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore

  const isOldYear = oldYear?.oldYear

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [rows, setRows] = useState()
  const [loading, setLoading] = useState(false)

  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const fetchData = async () => {
    try {
      setLoading(true)
      const data = await DataService.getAOPMCCalculatedData(keycloak)
      const formattedData = data.map((item, index) => {
        const isTPH = false
        return {
          ...item,
          idFromApi: item?.id,
          normParametersFKId: item?.materialFKId.toLowerCase(),
          id: index,
          isEditable: false,
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
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const productionColumns = getEnhancedProductionColDefsView({
    headerMap,
    allProducts,
    findAvg,
  })

  return (
    <Box
      sx={{
        height: 'auto',
        width: '100%',
        margin: '0px 0px 0px',
        backgroundColor: '#F2F3F8',
        borderRadius: 0,
        borderBottom: 'none',
        overflowY: 'hidden',
      }}
    >
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <DataGrid
        autoHeight={true}
        rows={rows || []}
        className='custom-data-grid'
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
          isEditable: false,
        }}
        rowHeight={35}
        slotProps={{
          toolbar: { setRows },
          loadingOverlay: {
            variant: 'linear-progress',
            norowsvariant: 'skeleton',
          },
        }}
        getRowClassName={(params) => {
          const classes = []
          if (params.row.isEditable === false) {
            return [...classes, 'odd-row'].join(' ')
          }
          return [...classes, 'even-row'].join(' ')
        }}
      />
    </Box>
  )
}

export default ProductionVolumeDataView
