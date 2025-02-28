import { DataService } from 'services/DataService'
import { Autocomplete, TextField } from '@mui/material'
import ASDataGrid from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
const headerMap = generateHeaderNames();

const BusinessDemand = () => {
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const [bdData, setBDData] = useState([])
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  useEffect(() => {
    function transformData(inputData) {
      const months = [
        'apr24',
        'may24',
        'jun24',
        'jul24',
        'aug24',
        'sep24',
        'oct24',
        'nov24',
        'dec24',
        'jan25',
        'feb25',
        'mar25',
      ]

      const transformed = {}

      inputData.forEach((item) => {
        const productKey = item.NormParametersId

        if (!transformed[productKey]) {
          transformed[productKey] = {
            product: productKey,
            apr24: item.apr24,
            may24: item.may24,
            jun24: item.jun24,
            jul24: item.jul24,
            aug24: item.aug24,
            sep24: item.aug24,
            oct24: item.oct24,
            nov24: item.nov24,
            dec24: item.dec24,
            jan25: item.jan25,
            feb25: item.feb25,
            mar25: item.mar25,
            averageTPH: item.TPH,
            remark: item.Remark,
          }
        }

        const monthKey = Object.keys(item).find((key) =>
          key.match(/^[A-Za-z]{3}-\d{2}$/),
        )

        if (monthKey) {
          const formattedMonth = monthKey
            .toLowerCase()
            .replace('-25', '25')
            .replace('-24', '24')
          transformed[productKey][formattedMonth] = item[monthKey]
        }

        transformed[productKey].averageTPH =
          item.Average || transformed[productKey].averageTPH
        transformed[productKey].remark =
          item.Remark || transformed[productKey].remark
        const validValues = months
          .map((month) => transformed[productKey][month])
          .filter((value) => value !== null && value !== undefined)

        const average =
          validValues.length > 0
            ? (
                validValues.reduce((sum, val) => sum + val, 0) /
                validValues.length
              ).toFixed(2)
            : 0

        transformed[productKey].averageTPH = parseFloat(average)
      })

      return Object.values(transformed)
    }

    const fetchData = async () => {
      try {
        const data = await DataService.getBDData(keycloak)
        const transformedData = transformData(data)

        const formattedData = transformedData.map((item, index) => ({
          ...item,
          id: index,
        }))

        setBDData(formattedData)
      } catch (error) {
        console.error('Error fetching Turnaround data:', error)
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
        // handleMenuClose();
      }
    }

    // Initial data fetch on mount or when selectedPlant changes
    fetchData()
    getAllProducts()
    console.log('sitePlant--->', sitePlantChange)
    console.log(sitePlantChange, 'changed plant or site')
  }, [sitePlantChange, keycloak])

  const colDefs = [
    {
      field: 'NormParametersId',
      headerName: 'Product',
      editable: true,
      minWidth: 225,
      valueGetter: (params , params2) => {         
        return params || ''; 
      },
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params);
        return product ? product.displayName : '';
      },
      renderEditCell: (params , params2) => {
        const { id, value } = params; 
        return (
          <select
            value={value || ""}
            onChange={(event) => {
              // console.log('event',event);
              
              params.api.setEditCellValue({
                id: params.id,
                field: 'product',
                value: event.target.value, 
              });
            }}
            style={{
              width: '100%',
              padding: '5px',
              border: 'none',  
              outline: 'none', 
              background: 'transparent', 
            }}
          >
            {allProducts.map((product) => (
              <option key={product.id} value={product.id}>
                {product.displayName}
              </option>
            ))}
          </select>
        );
      },
    }, 


    { field: 'apr24', headerName: headerMap['apr'], editable: true },
    { field: 'may24', headerName: headerMap['may'], editable: true },
    { field: 'jun24', headerName: headerMap['jun'], editable: true },
    { field: 'jul24', headerName: headerMap['jul'], editable: true },
    { field: 'aug24', headerName: headerMap['aug'], editable: true },
    { field: 'sep24', headerName: headerMap['sep'], editable: true },
    { field: 'oct24', headerName: headerMap['oct'], editable: true },
    { field: 'nov24', headerName: headerMap['nov'], editable: true },
    { field: 'dec24', headerName: headerMap['dec'], editable: true },
    { field: 'jan25', headerName: headerMap['jan'], editable: true },
    { field: 'feb25', headerName: headerMap['feb'], editable: true },
    { field: 'mar25', headerName: headerMap['mar'], editable: true },


    { field: 'Average', headerName: 'Average TPH', editable: true, 

         renderHeader: () => (
        <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
          <div>Average</div>
          <div>TPH</div>
        </div>
      ),
    },
    // {
    //   field: 'averageTPH',
    //   headerName: 'Average TPH',
    //   minWidth: 100,
    //   maxWidth: 120,
    //   editable: false,
    //   valueGetter: (params) => {
       
    //     // console.log('check--->', params)
    //     // const existingAverage = params
    //     // if (
    //     //   existingAverage !== undefined &&
    //     //   existingAverage !== null &&
    //     //   existingAverage !== ''
    //     // ) {
    //     //   return existingAverage
    //     // }

    //     // const sum = params2?.months?.reduce((total, month) => {
    //     //   // Ensure that the value is treated as a number (defaulting to 0 if not set)
    //     //   return total + (Number(params2[month]) || 0)
    //     // }, 0)
    //     // // Calculate average and format to 2 decimals
    //     // return (sum / params2?.months?.length).toFixed(2)
    //   },
    //   valueFormatter: (params, params2) => {
    //     console.log(params, '------->', params2)
    //   },
    //   renderHeader: () => (
    //     <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
    //       <div>Average</div>
    //       <div>TPH</div>
    //     </div>
    //   ),
    // },
    { field: 'Remark', headerName: 'Remark', minWidth: 150, editable: true },
  ]

  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={bdData}
        title='Business Demand'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        permissions={{
          showAction: true,
          addButton: true,
          deleteButton: true,
          editButton: true,
          showUnit: true,
          saveWithRemark: true,
        }}
      />
    </div>
  )
}

export default BusinessDemand
