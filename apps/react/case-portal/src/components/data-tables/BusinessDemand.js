import { DataService } from 'services/DataService'
import { Autocomplete, TextField } from '@mui/material'
import ASDataGrid from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'

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
      minWidth: 100,
      maxWidth: 120,
      editable: false,
    },
    { field: 'remark', headerName: 'Remark', minWidth: 150, editable: true }
    
  ]

  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={bdData}
        title='Product Demand'
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
