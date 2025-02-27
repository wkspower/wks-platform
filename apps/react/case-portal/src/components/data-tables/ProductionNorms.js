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
  // const [productOptions, setProductOptions] = useState([])
  // const [productionData, setProductionData] = useState([])
  const [csData, setCsData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const formattedDataHardCoded = [
    {
      id: 1,
      product: 'A603D79F-8023-445C-8EFA-15178AD05F0D', // GLYCOL RESIDUE I
      apr24: 120,
      may24: 130,
      jun24: 125,
      jul24: 110,
      aug24: 115,
      sep24: 140,
      oct24: 135,
      nov24: 150,
      dec24: 145,
      jan25: 160,
      feb25: 155,
      mar25: 170,
      averageTPH: 135,
      remark: 'Good performance',
    },
    {
      id: 2,
      product: '9B49BD06-9AC4-4123-B8F8-19822A0CCFFE', // MP Steam
      apr24: 100,
      may24: 105,
      jun24: 95,
      jul24: 90,
      aug24: 100,
      sep24: 110,
      oct24: 105,
      nov24: 95,
      dec24: 100,
      jan25: 115,
      feb25: 110,
      mar25: 120,
      averageTPH: 105,
      remark: 'Stable trend',
    },
    {
      id: 3,
      product: 'A0538116-245D-477E-A181-2D78F0411AB6', // Ethylene Di Chloride
      apr24: 80,
      may24: 85,
      jun24: 90,
      jul24: 95,
      aug24: 100,
      sep24: 105,
      oct24: 95,
      nov24: 90,
      dec24: 85,
      jan25: 80,
      feb25: 75,
      mar25: 70,
      averageTPH: 85,
      remark: 'Needs improvement',
    },
    {
      id: 4,
      product: '00DC05B1-9607-470E-A159-62497E0123E2', // EOE
      apr24: 140,
      may24: 145,
      jun24: 150,
      jul24: 155,
      aug24: 160,
      sep24: 165,
      oct24: 170,
      nov24: 175,
      dec24: 180,
      jan25: 185,
      feb25: 190,
      mar25: 195,
      averageTPH: 165,
      remark: 'Excellent growth',
    },
    {
      id: 5,
      product: 'E229A74C-0CF5-4F90-9A37-87A9761EAFAD', // Ethylene
      apr24: 95,
      may24: 90,
      jun24: 85,
      jul24: 80,
      aug24: 75,
      sep24: 70,
      oct24: 65,
      nov24: 60,
      dec24: 55,
      jan25: 50,
      feb25: 45,
      mar25: 40,
      averageTPH: 70,
      remark: 'Declining trend',
    },
    {
      id: 6,
      product: 'B3A88783-91D3-4D1A-ABA0-ADE4B0B7D504', // E52009
      apr24: 110,
      may24: 112,
      jun24: 114,
      jul24: 116,
      aug24: 118,
      sep24: 120,
      oct24: 122,
      nov24: 124,
      dec24: 126,
      jan25: 128,
      feb25: 130,
      mar25: 132,
      averageTPH: 121,
      remark: 'Consistent performance',
    },
    {
      id: 7,
      product: 'A061E050-0281-421F-81C1-B136CE2ED3F3', // EO
      apr24: 200,
      may24: 195,
      jun24: 190,
      jul24: 185,
      aug24: 180,
      sep24: 175,
      oct24: 170,
      nov24: 165,
      dec24: 160,
      jan25: 155,
      feb25: 150,
      mar25: 145,
      averageTPH: 175,
      remark: 'High volume',
    },
    {
      id: 8,
      product: '445DF72C-821A-4C26-B6EF-B82E5E6EFE62', // B56003
      apr24: 50,
      may24: 55,
      jun24: 60,
      jul24: 65,
      aug24: 70,
      sep24: 75,
      oct24: 80,
      nov24: 85,
      dec24: 90,
      jan25: 95,
      feb25: 100,
      mar25: 105,
      averageTPH: 75,
      remark: 'Low sales',
    },
    {
      id: 9,
      product: '60FA69B5-6BCF-45D3-9426-BD7FE63A8C58', // Purge
      apr24: 130,
      may24: 135,
      jun24: 140,
      jul24: 145,
      aug24: 150,
      sep24: 155,
      oct24: 160,
      nov24: 165,
      dec24: 170,
      jan25: 175,
      feb25: 180,
      mar25: 185,
      averageTPH: 150,
      remark: 'Growing steadily',
    },
    {
      id: 10,
      product: 'A5C84BA4-2C00-417C-86EC-CB0D1F613C65', // Oxygen
      apr24: 75,
      may24: 80,
      jun24: 85,
      jul24: 90,
      aug24: 95,
      sep24: 100,
      oct24: 105,
      nov24: 110,
      dec24: 115,
      jan25: 120,
      feb25: 125,
      mar25: 130,
      averageTPH: 95,
      remark: 'Moderate performance',
    },
  ]

  // useEffect(() => {
  //   function transformData(inputData) {
  //     const months = [
  //       'apr24',
  //       'may24',
  //       'jun24',
  //       'jul24',
  //       'aug24',
  //       'sep24',
  //       'oct24',
  //       'nov24',
  //       'dec24',
  //       'jan25',
  //       'feb25',
  //       'mar25',
  //     ]

  //     const transformed = {}

  //     inputData.forEach((item) => {
  //       const productKey = item.Product

  //       if (!transformed[productKey]) {
  //         transformed[productKey] = {
  //           product: productKey,
  //           apr24: 0,
  //           may24: 0,
  //           jun24: 0,
  //           jul24: 0,
  //           aug24: 0,
  //           sep24: 0,
  //           oct24: 0,
  //           nov24: 0,
  //           dec24: 0,
  //           jan25: 0,
  //           feb25: 0,
  //           mar25: 0,
  //           averageTPH: 0,
  //           remark: '',
  //         }
  //       }

  //       const monthKey = Object.keys(item).find((key) =>
  //         key.match(/^[A-Za-z]{3}-\d{2}$/),
  //       )

  //       if (monthKey) {
  //         const formattedMonth = monthKey
  //           .toLowerCase()
  //           .replace('-25', '25')
  //           .replace('-24', '24')
  //         transformed[productKey][formattedMonth] = item[monthKey]
  //       }

  //       transformed[productKey].averageTPH =
  //         item.Average || transformed[productKey].averageTPH
  //       transformed[productKey].remark =
  //         item.Remark || transformed[productKey].remark
  //     })

  //     return Object.values(transformed)
  //   }

  //   const fetchData = async () => {
  //     try {
  //       const data = await DataService.getProductionNormsData(keycloak)
  //       // const transformedData = transformData(data)

  //       const formattedData = data.map((item, index) => ({
  //         ...item,
  //         id: index,
  //       }))

  //       // setCsData(formattedData)
  //       setCsData(formattedDataHardCoded)
  //     } catch (error) {
  //       console.error('Error fetching Turnaround data:', error)
  //     }
  //   }

  //   const getAllProducts = async () => {
  //     try {
  //       const data = await DataService.getAllProducts(keycloak)
  //       const productList = data.map((product) => ({
  //         id: product.id,
  //         displayName: product.displayName,
  //       }))
  //       setAllProducts(productList)
  //     } catch (error) {
  //       console.error('Error fetching product:', error)
  //     } finally {
  //       // handleMenuClose();
  //     }
  //   }
  //   getAllProducts()
  //   fetchData()
  // }, [])

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

    // const saveShutdownData = async () => {
    //   try {
    //     var plantId = 'B989E3EE-00C8-493C-9CA4-709D340FA5A1'
    //     // var plantId = '7b7e0d7c-2666-43bb-847c-d78e144673de'
    //     // var plantId = '7b7e0d7c-2666-43bb-847c-d78e144673de'

    //     // discription
    //     // maintStartDateTime
    //     // maintEndDateTime
    //     // durationInMins
    //     // product

    //     const shutdownDetails = {
    //       product: 'Oxygen',
    //       discription: '1 Shutdown maintenance',
    //       durationInMins: 120,
    //       maintEndDateTime: '2025-02-20T18:00:00Z',
    //       maintStartDateTime: '2025-02-20T16:00:00Z',
    //     }

    //     const response = await DataService.saveShutdownData(
    //       plantId,
    //       shutdownDetails,
    //       keycloak,
    //     )
    //     console.log('Shutdown data saved successfully:', response)
    //     return response
    //   } catch (error) {
    //     console.error('Error saving shutdown data:', error)
    //   }
    // }

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

  // const getAllProducts = async () => {
  //   try {
  //     const data = await DataService.getAllProducts(keycloak)
  //     console.log('API Response:', data)
  //     const products = data.map((item) => item.displayName || item.name || item)
  //     setProductOptions(products)
  //   } catch (error) {
  //     console.error('Error fetching product:', error)
  //   } finally {
  //     // handleMenuClose();
  //   }
  // }
  // useEffect(() => {
  //   if (allProducts.length > 0) {
  //     const rows = allProducts.map((option, index) => ({
  //       id: index + 1,
  //       product: option,
  //       apr24: Math.floor(Math.random() * 100),
  //       may24: Math.floor(Math.random() * 100),
  //       jun24: Math.floor(Math.random() * 100),
  //       jul24: Math.floor(Math.random() * 100),
  //       aug24: Math.floor(Math.random() * 100),
  //       sep24: Math.floor(Math.random() * 100),
  //       oct24: Math.floor(Math.random() * 100),
  //       nov24: Math.floor(Math.random() * 100),
  //       dec24: Math.floor(Math.random() * 100),
  //       jan25: Math.floor(Math.random() * 100),
  //       feb25: Math.floor(Math.random() * 100),
  //       mar25: Math.floor(Math.random() * 100),
  //       averageTPH: Math.floor(Math.random() * 100),
  //       remark: 'Good',
  //     }))
  //     setProductionData(rows)
  //   }
  // }, [productOptions])

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
