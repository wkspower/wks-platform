import { DataService } from 'services/DataService'
import { Autocomplete, TextField } from '@mui/material'
import ASDataGrid from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
const headerMap = generateHeaderNames()

const BusinessDemand = () => {
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const [bdData, setBDData] = useState([])
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await DataService.getBDData(keycloak)

        const formattedData = data.map((item, index) => ({
          ...item,
          idFromApi: item.id,
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
          id: product.id.toLowerCase(), // Convert id to lowercase
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
    // console.log('sitePlant--->', sitePlantChange)
    // console.log(sitePlantChange, 'changed plant or site')
  }, [sitePlantChange, keycloak])

  const colDefs = [
    {
      field: 'normParameterId',
      headerName: 'Product',
      editable: true,
      minWidth: 225,
      valueGetter: (params, params2) => {
        return params || ''
      },
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params, params2) => {
        const { id, value } = params
        return (
          <select
            value={value || ''}
            onChange={(event) => {
              // console.log('event',event);

              params.api.setEditCellValue({
                id: params.id,
                field: 'normParameterId',
                value: event.target.value,
              })
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
        )
      },
    },

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

    { field: 'avgTph', headerName: 'AVG TPH', minWidth: 150, editable: true },
    { field: 'remark', headerName: 'Remark', minWidth: 150, editable: true },
    {
      field: 'idFromApi',
      headerName: 'idFromApi',
    },
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
          saveBtn: true,
        }}
      />
    </div>
  )
}

export default BusinessDemand
