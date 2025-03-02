import { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import ASDataGrid from './ASDataGrid'

import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
const headerMap = generateHeaderNames()

const ProductionvolumeData = () => {
  const keycloak = useSession()
  const [productNormData, setProductNormData] = useState([])
  const [slowDownData, setSlowDownData] = useState([])
  const [allProducts, setAllProducts] = useState([])

  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await DataService.getAOPData(keycloak)
        const formattedData = data.map((item, index) => ({
          ...item,
          id: item.id,
        }))
        setProductNormData(formattedData)
      } catch (error) {
        console.error('Error fetching SlowDown data:', error)
      }
    }

    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        const productList = data.map((product) => ({
          id: product.id,
          displayName: product.displayName,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
      }
    }

    getAllProducts()
    fetchData()
  }, [sitePlantChange, keycloak])

  const productionColumns = [
    { field: 'id', headerName: 'ID' },
    {
      field: 'aopCaseId',
      headerName: 'Case ID',
      minWidth: 120,
      editable: false,
    },
    { field: 'aopType', headerName: 'Type', minWidth: 80 },
    { field: 'aopYear', headerName: 'Year', minWidth: 80 },
    { field: 'plantFkId', headerName: 'Plant ID', minWidth: 80 },
    { field: 'normItem', headerName: 'Product', minWidth: 80, editable: false },

    { field: 'april', headerName: headerMap['apr'], editable: true },
    { field: 'may', headerName: headerMap['may'], editable: true },
    { field: 'june', headerName: headerMap['jun'], editable: true },
    { field: 'july', headerName: headerMap['jul'], editable: true },

    { field: 'aug', headerName: headerMap['aug'], editable: true },
    { field: 'sep', headerName: headerMap['sep'], editable: true },
    { field: 'oct', headerName: headerMap['oct'], editable: true },

    { field: 'nov', headerName: headerMap['nov'], editable: true },
    { field: 'dec', headerName: headerMap['dec'], editable: true },
    { field: 'jan', headerName: headerMap['jan'], editable: true },
    { field: 'feb', headerName: headerMap['feb'], editable: true },
    { field: 'march', headerName: headerMap['mar'], editable: true },

    {
      field: 'averageTPH',
      headerName: 'Average TPH',
      minWidth: 100,
      maxWidth: 120,
      editable: false,
      valueGetter: (params) => {
        // console.log('check--->', params)
        // const existingAverage = params
        // if (
        //   existingAverage !== undefined &&
        //   existingAverage !== null &&
        //   existingAverage !== ''
        // ) {
        //   return existingAverage
        // }
        // const sum = params2?.months?.reduce((total, month) => {
        //   // Ensure that the value is treated as a number (defaulting to 0 if not set)
        //   return total + (Number(params2[month]) || 0)
        // }, 0)
        // // Calculate average and format to 2 decimals
        // return (sum / params2?.months?.length).toFixed(2)
      },
      valueFormatter: (params, params2) => {
        // console.log(params, '------->', params2)
      },
      renderHeader: () => (
        <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
          <div>Average</div>
          <div>TPH</div>
        </div>
      ),
    },
    { field: 'aopStatus', headerName: 'Remark', minWidth: 75, editable: false },
  ]

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={productNormData}
        title='Production Volume Data'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: false,
          saveWithRemark: true,
          showRefreshBtn: true,
          saveBtn: true,
        }}
      />
    </div>
  )
}

export default ProductionvolumeData
