import { DataService } from 'services/DataService'
import { Autocomplete, TextField } from '@mui/material'
import ASDataGrid from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'

const BusinessDemand = () => {
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const [bdData, setBDData] = useState([])

  useEffect(() => {
    function transformData(inputData) {
      const months = [
        "apr24", "may24", "jun24", "jul24", "aug24", "sep24", 
        "oct24", "nov24", "dec24", "jan25", "feb25", "mar25"
      ];
      
      const transformed = {};
      
      inputData.forEach((item) => {
        const productKey = item.Product;
        
        if (!transformed[productKey]) {
          transformed[productKey] = {
            product: productKey,
            apr24: 0,
            may24: 0,
            jun24: 0,
            jul24: 0,
            aug24: 0,
            sep24: 0,
            oct24: 0,
            nov24: 0,
            dec24: 0,
            jan25: 0,
            feb25: 0,
            mar25: 0,
            averageTPH: 0,
            remark: "",
          };
        }
        
        const monthKey = Object.keys(item).find((key) => key.match(/^[A-Za-z]{3}-\d{2}$/));
        
        if (monthKey) {
          const formattedMonth = monthKey.toLowerCase().replace("-25", "25").replace("-24", "24");
          transformed[productKey][formattedMonth] = item[monthKey];
        }
        
        transformed[productKey].averageTPH = item.Average || transformed[productKey].averageTPH;
        transformed[productKey].remark = item.Remark || transformed[productKey].remark;
      });
      
      return Object.values(transformed);
    }

    const fetchData = async () => {
      try {
        const data = await DataService.getBDData(keycloak)


    


        const transformedData = transformData(data);

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
    getAllProducts()
    fetchData()    
  }, [])



  const colDefs = [


    {
      field: 'product',
      headerName: 'Product',
      editable: true,
      minWidth: 225,
      valueGetter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : params
      },
      renderEditCell: (params) => {
        const { id } = params

        return (
          <Autocomplete
            options={allProducts}
            getOptionLabel={(option) => option.displayName}
            value={
              allProducts.find((product) => product.id === params.value) || null
            }
            disableClearable
            onChange={(event, newValue) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'product',
                value: newValue ? newValue.id : '',
              })
            }}
            onInputChange={(event, newInputValue) => {
              if (event && event.type === 'keydown' && event.key === 'Enter') {
                const selectedProduct = allProducts.find(
                  (product) =>
                    product.displayName.toLowerCase() ===
                    newInputValue.toLowerCase(),
                )
                params.api.setEditCellValue({
                  id: params.id,
                  field: 'product',
                  value: selectedProduct ? selectedProduct.id : '',
                })
              }
            }}
            renderInput={(params) => (
              <TextField {...params} variant='outlined' size='small' />
            )}
            fullWidth
          />
        )
      },
    },
    { field: 'apr24', headerName: 'Apr-24', editable: true },
    { field: 'may24', headerName: 'May-24', editable: true },
    { field: 'jun24', headerName: 'Jun-24', editable: true },
    { field: 'jul24', headerName: 'Jul-24', editable: true },
    { field: 'aug24', headerName: 'Aug-24', editable: true },
    { field: 'sep24', headerName: 'Sep-24', editable: true },
    { field: 'oct24', headerName: 'Oct-24', editable: true },
    { field: 'nov24', headerName: 'Nov-24', editable: true },
    { field: 'dec24', headerName: 'Dec-24', editable: true },
    { field: 'jan25', headerName: 'Jan-25', editable: true },
    { field: 'feb25', headerName: 'Feb-25', editable: true },
    { field: 'mar25', headerName: 'Mar-25', editable: true },
    {
      field: 'averageTPH',
      headerName: 'Average TPH',
      width: 150,
      editable: false,
      renderHeader: () => (
        <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
          <div>Average</div>
          <div>TPH</div>
        </div>
      ),
    },
    { field: 'remark', headerName: 'Remark', minWidth: 150, editable: true },
  ]

  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={bdData}
        title='Business Demand Data'
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
