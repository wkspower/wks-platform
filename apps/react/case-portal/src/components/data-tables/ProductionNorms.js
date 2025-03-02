import { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'

import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import ASDataGrid from './ASDataGrid'
const headerMap = generateHeaderNames()

import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'

const ProductionNorms = () => {
  const keycloak = useSession()
  const [csData, setCsData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await DataService.getAOPMCCalculatedData(keycloak)
        // console.log(data)
        const formattedData = data.map((item, index) => ({
          ...item,
          id: item.id,
        }))
        setCsData(formattedData)
      } catch (error) {
        console.error('Error fetching Production AOP data:', error)
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

    fetchData()
    getAllProducts()
  }, [sitePlantChange, keycloak])

  const productionColumns = [
    { field: 'id', headerName: 'ID' },
    {
      field: 'aopCaseId',
      headerName: 'Case ID',
      minWidth: 120,
      editable: false,
    },

    { field: 'material', headerName: 'Material', editable: false },
    { field: 'plant', headerName: 'Plant', editable: false },
    { field: 'site', headerName: 'Site', editable: false },

    { field: 'april', headerName: headerMap['apr'], editable: true },
    { field: 'may', headerName: headerMap['may'], editable: true },
    { field: 'june', headerName: headerMap['jun'], editable: true },
    { field: 'july', headerName: headerMap['jul'], editable: true },

    { field: 'august', headerName: headerMap['aug'], editable: true },
    { field: 'september', headerName: headerMap['sep'], editable: true },
    { field: 'october', headerName: headerMap['oct'], editable: true },
    { field: 'november', headerName: headerMap['nov'], editable: true },
    { field: 'december', headerName: headerMap['dec'], editable: true },
    { field: 'january', headerName: headerMap['jan'], editable: true },
    { field: 'february', headerName: headerMap['feb'], editable: true },
    { field: 'march', headerName: headerMap['mar'], editable: true },
  ]

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={csData}
        title='Production AOP'
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
          saveBtn: true,
        }}
      />
    </div>
  )
}

export default ProductionNorms
