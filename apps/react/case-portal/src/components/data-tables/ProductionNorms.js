import { DataService } from 'services/DataService'
import {
  Autocomplete,
  TextField,
} from '../../../node_modules/@mui/material/index'
import ASDataGrid from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'

const ProductionNorms = () => {
  const keycloak = useSession()
  const [csData, setCsData] = useState([])
  const [allProducts, setAllProducts] = useState([])

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await DataService.getAOPData(keycloak)
        // const data = await DataService.getProductionNormsData(keycloak)
        console.log(data)
        const formattedData = data.map((item, index) => ({
          ...item,
          id: item.id,
        }))
        setCsData(formattedData)
      } catch (error) {
        console.error('Error fetching Shutdown data:', error)
      }
    }

    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        // console.log('API Response:', data)

        // Extract only displayName and id
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

    fetchData()
    // saveShutdownData()
    getAllProducts()
  }, [])
  // const productionColumns = [
  //   {
  //     field: 'NormParametersId',
  //     headerName: 'Product',
  //     editable: true,
  //     minWidth: 225,
  //     valueGetter: (params, params2) => {
  //       // console.log('p1', params);
  //       // console.log('p2', params2);
  //       return params || ''
  //     },
  //     valueFormatter: (params) => {
  //       console.log('params valueFormatter ', params)
  //       const product = allProducts.find((p) => p.id === params)
  //       return product ? product.displayName : ''
  //     },
  //     renderEditCell: (params, params2) => {
  //       const { id, value } = params
  //       // console.log('q1', params);
  //       // console.log('q2', params2);
  //       return (
  //         <select
  //           value={value || allProducts[0]?.id}
  //           onChange={(event) => {
  //             params.api.setEditCellValue({
  //               id: params.id,
  //               field: 'product',
  //               value: event.target.value,
  //             })
  //           }}
  //           style={{
  //             width: '100%',
  //             padding: '5px',
  //             border: 'none', // Removes border
  //             outline: 'none', // Removes focus outline
  //             background: 'transparent', // Keeps background clean
  //           }}
  //         >
  //           {allProducts.map((product) => (
  //             <option key={product.id} value={product.id}>
  //               {product.displayName}
  //             </option>
  //           ))}
  //         </select>
  //       )
  //     },
  //   },
  //   { field: 'apr24', headerName: 'Apr-24', editable: true },
  //   { field: 'may24', headerName: 'May-24', editable: true },
  //   { field: 'jun24', headerName: 'Jun-24', editable: true },
  //   { field: 'jul24', headerName: 'Jul-24', editable: true },
  //   { field: 'aug24', headerName: 'Aug-24', editable: true },
  //   { field: 'sep24', headerName: 'Sep-24', editable: true },
  //   { field: 'oct24', headerName: 'Oct-24', editable: true },
  //   { field: 'nov24', headerName: 'Nov-24', editable: true },
  //   { field: 'dec24', headerName: 'Dec-24', editable: true },
  //   { field: 'jan25', headerName: 'Jan-25', editable: true },
  //   { field: 'feb25', headerName: 'Feb-25', editable: true },
  //   { field: 'mar25', headerName: 'Mar-25', editable: true },
  //   { field: 'Average', headerName: 'Average TPH', editable: true },
  //   { field: 'Remark', headerName: 'Remark', minWidth: 150, editable: true },
  // ]
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
    { field: 'april', headerName: 'Apr-24', editable: true },
    { field: 'may', headerName: 'May-24', editable: true },
    { field: 'june', headerName: 'Jun-24', editable: true },
    { field: 'july', headerName: 'Jul-24', editable: true },
    { field: 'aug', headerName: 'Aug-24', editable: true },
    { field: 'sep', headerName: 'Sep-24', editable: true },
    { field: 'oct', headerName: 'Oct-24', editable: true },
    { field: 'nov', headerName: 'Nov-24', editable: true },
    { field: 'dec', headerName: 'Dec-24', editable: true },
    { field: 'jan', headerName: 'Jan-25', editable: true },
    { field: 'feb', headerName: 'Feb-25', editable: true },
    { field: 'march', headerName: 'Mar-25', editable: true },
    {
      field: 'averageTPH',
      headerName: 'Average TPH',
      minWidth: 100,
      maxWidth: 120,
      editable: false,
      valueGetter: (params) => {
        console.log('check--->', params)
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
        console.log(params, '------->', params2)
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
  // useEffect(() => {
  //   console.log('api call here ')

  //   getAllProducts()
  // }, [])

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={csData}
        title='Production Norms Data'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: true,
          saveWithRemark: true,
          showCalculate: true,
        }}
      />
    </div>
  )
}

export default ProductionNorms
