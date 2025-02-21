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
  const [productOptions, setProductOptions] = useState([])
  const [productionData, setProductionData] = useState([])
  const productionColumns = [
    {
      field: 'product',
      headerName: 'Products',
      editable: true,
      filterable: true,
      minWidth: 125,
      maxWidth: 200,
      renderEditCell: (params) => {
        const { id } = params
        const isEditable = id > productOptions?.length // Enable only for rows beyond 10

        return (
          <Autocomplete
            options={productOptions}
            value={params.value || ''}
            disableClearable
            onChange={(event, newValue) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'product',
                value: newValue,
              })
            }}
            onInputChange={(event, newInputValue) => {
              if (event && event.type === 'keydown' && event.key === 'Enter') {
                params.api.setEditCellValue({
                  id: params.id,
                  field: 'product',
                  value: newInputValue,
                })
              }
            }}
            renderInput={(params) => (
              <TextField {...params} variant='outlined' size='small' />
            )}
            disabled={!isEditable}
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
    { field: 'averageTPH', headerName: 'Average TPH', editable: true },
    { field: 'remark', headerName: 'Remark', minWidth: 150, editable: true },
  ]
  useEffect(() => {
    console.log('api call here ')

    getAllProducts()
  }, [])

  const getAllProducts = async () => {
    try {
      const data = await DataService.getAllProducts(keycloak)
      console.log('API Response:', data)
      const products = data.map((item) => item.displayName || item.name || item)
      setProductOptions(products)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }
  useEffect(() => {
    if (productOptions.length > 0) {
      const rows = productOptions.map((option, index) => ({
        id: index + 1,
        product: option,
        apr24: Math.floor(Math.random() * 100),
        may24: Math.floor(Math.random() * 100),
        jun24: Math.floor(Math.random() * 100),
        jul24: Math.floor(Math.random() * 100),
        aug24: Math.floor(Math.random() * 100),
        sep24: Math.floor(Math.random() * 100),
        oct24: Math.floor(Math.random() * 100),
        nov24: Math.floor(Math.random() * 100),
        dec24: Math.floor(Math.random() * 100),
        jan25: Math.floor(Math.random() * 100),
        feb25: Math.floor(Math.random() * 100),
        mar25: Math.floor(Math.random() * 100),
        averageTPH: Math.floor(Math.random() * 100),
        remark: 'Good',
      }))
      setProductionData(rows)
    }
  }, [productOptions])
  //   const productOptions = [
  //     'Product A',
  //     'Product B',
  //     'Product C',
  //     'Product D',
  //     'Product E',
  //     'Product F',
  //     'Product G',
  //     'Product H',
  //     'Product I',
  //     'Product J',
  //     'Product K',
  //     'Product L',
  //   ]

  //   const productionData = [
  //     {
  //       id: 1,
  //       product: 'Product A',
  //       apr24: 100,
  //       may24: 120,
  //       jun24: 110,
  //       jul24: 105,
  //       aug24: 115,
  //       sep24: 108,
  //       oct24: 110,
  //       nov24: 112,
  //       dec24: 115,
  //       jan25: 118,
  //       feb25: 120,
  //       mar25: 122,
  //       averageTPH: 111,
  //       remark: 'Good',
  //     },
  //     {
  //       id: 2,
  //       product: 'Product B',
  //       apr24: 95,
  //       may24: 100,
  //       jun24: 102,
  //       jul24: 108,
  //       aug24: 104,
  //       sep24: 107,
  //       oct24: 110,
  //       nov24: 113,
  //       dec24: 111,
  //       jan25: 115,
  //       feb25: 118,
  //       mar25: 120,
  //       averageTPH: 106,
  //       remark: 'Average',
  //     },
  //     {
  //       id: 3,
  //       product: 'Product C',
  //       apr24: 130,
  //       may24: 135,
  //       jun24: 140,
  //       jul24: 142,
  //       aug24: 130,
  //       sep24: 125,
  //       oct24: 128,
  //       nov24: 132,
  //       dec24: 140,
  //       jan25: 145,
  //       feb25: 150,
  //       mar25: 148,
  //       averageTPH: 137,
  //       remark: 'Excellent',
  //     },
  //     {
  //       id: 4,
  //       product: 'Product D',
  //       apr24: 85,
  //       may24: 90,
  //       jun24: 92,
  //       jul24: 95,
  //       aug24: 100,
  //       sep24: 98,
  //       oct24: 100,
  //       nov24: 102,
  //       dec24: 105,
  //       jan25: 108,
  //       feb25: 110,
  //       mar25: 112,
  //       averageTPH: 97,
  //       remark: 'Below Average',
  //     },
  //     {
  //       id: 5,
  //       product: 'Product E',
  //       apr24: 110,
  //       may24: 120,
  //       jun24: 125,
  //       jul24: 130,
  //       aug24: 135,
  //       sep24: 140,
  //       oct24: 145,
  //       nov24: 150,
  //       dec24: 155,
  //       jan25: 160,
  //       feb25: 165,
  //       mar25: 170,
  //       averageTPH: 142,
  //       remark: 'Good',
  //     },
  //     {
  //       id: 6,
  //       product: 'Product F',
  //       apr24: 75,
  //       may24: 80,
  //       jun24: 85,
  //       jul24: 88,
  //       aug24: 90,
  //       sep24: 92,
  //       oct24: 95,
  //       nov24: 97,
  //       dec24: 100,
  //       jan25: 102,
  //       feb25: 105,
  //       mar25: 107,
  //       averageTPH: 91,
  //       remark: 'Poor',
  //     },
  //     {
  //       id: 7,
  //       product: 'Product G',
  //       apr24: 150,
  //       may24: 155,
  //       jun24: 160,
  //       jul24: 165,
  //       aug24: 170,
  //       sep24: 175,
  //       oct24: 180,
  //       nov24: 185,
  //       dec24: 190,
  //       jan25: 195,
  //       feb25: 200,
  //       mar25: 205,
  //       averageTPH: 177,
  //       remark: 'Excellent',
  //     },
  //     {
  //       id: 8,
  //       product: 'Product H',
  //       apr24: 120,
  //       may24: 130,
  //       jun24: 140,
  //       jul24: 150,
  //       aug24: 160,
  //       sep24: 155,
  //       oct24: 145,
  //       nov24: 135,
  //       dec24: 125,
  //       jan25: 120,
  //       feb25: 115,
  //       mar25: 110,
  //       averageTPH: 133,
  //       remark: 'Good',
  //     },
  //     {
  //       id: 9,
  //       product: 'Product I',
  //       apr24: 95,
  //       may24: 100,
  //       jun24: 110,
  //       jul24: 115,
  //       aug24: 105,
  //       sep24: 120,
  //       oct24: 125,
  //       nov24: 130,
  //       dec24: 135,
  //       jan25: 140,
  //       feb25: 145,
  //       mar25: 150,
  //       averageTPH: 118,
  //       remark: 'Average',
  //     },
  //     {
  //       id: 10,
  //       product: 'Product J',
  //       apr24: 100,
  //       may24: 110,
  //       jun24: 115,
  //       jul24: 120,
  //       aug24: 125,
  //       sep24: 130,
  //       oct24: 135,
  //       nov24: 140,
  //       dec24: 145,
  //       jan25: 150,
  //       feb25: 155,
  //       mar25: 160,
  //       averageTPH: 128,
  //       remark: 'Good',
  //     },
  //   ]
  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={productionData}
        title='Production Norms Data'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
      />
    </div>
  )
}

export default ProductionNorms
