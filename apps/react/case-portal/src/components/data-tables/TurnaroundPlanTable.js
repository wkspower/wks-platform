import { DataService } from 'services/DataService'
import {
  Autocomplete,
  TextField,
} from '../../../node_modules/@mui/material/index'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'
import { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'

const TurnaroundPlanTable = () => {
  const [TaData, setTaData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const keycloak = useSession()

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await DataService.getTAPlantData(keycloak)
        const formattedData = data.map((item, index) => ({
          ...item,
          id: index,
        }))

        setTaData(formattedData)
      } catch (error) {
        console.error('Error fetching Turnaround data:', error)
      }
    }


    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        console.log('API Response:', data)

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
    getAllProducts()
  }, [])

  const colDefs = [
    {
      field: 'discription',
      headerName: 'Turnaround Desc',
      minWidth: 300,
      editable: true,
      renderHeader: () => (
        <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
          Turnaround Desc
        </div>
      ),
      flex: 3,
    },

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
        const isEditable = id > 0

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
            disabled={!isEditable}
            fullWidth
          />
        )
      },
    },

    {
      field: 'maintStartDateTime',
      headerName: 'Ta- From',
      type: 'dateTime',
      editable: true,
      minWidth: 200,
      valueGetter: (params) => {
        const value = params
        const parsedDate = value
          ? dayjs(value, 'MMM D, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },

    {
      field: 'maintEndDateTime',
      headerName: 'Ta- To',
      type: 'dateTime',
      editable: true,
      minWidth: 200,
      valueGetter: (params) => {
        const value = params
        const parsedDate = value
          ? dayjs(value, 'MMM D, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },

    {
      field: 'durationInMins',
      headerName: 'Duration (hrs)',
      editable: true,
      type: 'number',
      minWidth: 100,
      maxWidth: 150,
      renderCell: (params) => {
        // const durationInHours = params.value ? (params.value / 60).toFixed(2) : "0.00";
        return `${params.value}`
      },
    },

    {
      field: 'remark',
      headerName: 'Remarks',
      editable: true,
      minWidth: 200,
      maxWidth: 400,
    },
  ]

  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={TaData}
        title='Turnaround Plan Table'
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

export default TurnaroundPlanTable
