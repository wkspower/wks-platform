import { DataService } from 'services/DataService'
import { Autocomplete, TextField } from '@mui/material'
import ASDataGrid from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
// Import the catalyst options from the JSON file
// import catalystOptionsData from '../../assets/Catalyst.json'

const SelectivityData = () => {
  const keycloak = useSession()
  const [csData, setCsData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const formattedDataHardCoded = [
    {
      id: 1,
      catalyst: 'A603D79F-8023-445C-8EFA-15178AD05F0D', // GLYCOL RESIDUE I
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
      remark: 'Good performance',
    },
    {
      id: 2,
      catalyst: '9B49BD06-9AC4-4123-B8F8-19822A0CCFFE', // MP Steam
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
      remark: 'Stable trend',
    },
    {
      id: 3,
      catalyst: 'A0538116-245D-477E-A181-2D78F0411AB6', // Ethylene Di Chloride
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
      remark: 'Needs improvement',
    },
    {
      id: 4,
      catalyst: '00DC05B1-9607-470E-A159-62497E0123E2', // EOE
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
      remark: 'Excellent growth',
    },
    {
      id: 5,
      catalyst: 'E229A74C-0CF5-4F90-9A37-87A9761EAFAD', // Ethylene
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
      remark: 'Declining trend',
    },
    {
      id: 6,
      catalyst: 'B3A88783-91D3-4D1A-ABA0-ADE4B0B7D504', // E52009
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
      remark: 'Consistent performance',
    },
    {
      id: 7,
      catalyst: 'A061E050-0281-421F-81C1-B136CE2ED3F3', // EO
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
      remark: 'High volume',
    },
    {
      id: 8,
      catalyst: '445DF72C-821A-4C26-B6EF-B82E5E6EFE62', // B56003
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
      remark: 'Low sales',
    },
    {
      id: 9,
      catalyst: '60FA69B5-6BCF-45D3-9426-BD7FE63A8C58', // Purge
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
      remark: 'Growing steadily',
    },
    {
      id: 10,
      catalyst: 'A5C84BA4-2C00-417C-86EC-CB0D1F613C65', // Oxygen
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
      remark: 'Moderate performance',
    },
  ]
  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await DataService.getCatalystSelectivityData(keycloak)
        // const transformedData = transformData(data)

        console.log(data)

        // if (data) {
        //   const formattedData = data?.map((item, index) => ({
        //     ...item,
        //     id: index,
        //   }))
        // }

        setCsData(formattedDataHardCoded)
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
  // Use catalyst options from the JSON file
  // const productOptions = catalystOptionsData.catalystOptions

  const productionColumns = [
    // {
    //   field: 'catalyst',
    //   headerName: 'Catalyst',
    //   editable: true,
    //   filterable: true,
    //   minWidth: 125,
    //   renderEditCell: (params) => {
    //     const { id } = params
    //     // Enable editing condition based on your requirement.
    //     // Here, you can update the condition if needed.
    //     const isEditable = id >= 1

    //     return (
    //       <Autocomplete
    //         options={productOptions}
    //         value={params.value || ''}
    //         disableClearable
    //         onChange={(event, newValue) => {
    //           params.api.setEditCellValue({
    //             id: params.id,
    //             field: 'catalyst',
    //             value: newValue,
    //           })
    //         }}
    //         onInputChange={(event, newInputValue) => {
    //           if (event && event.type === 'keydown' && event.key === 'Enter') {
    //             params.api.setEditCellValue({
    //               id: params.id,
    //               field: 'catalyst',
    //               value: newInputValue,
    //             })
    //           }
    //         }}
    //         renderInput={(params) => (
    //           <TextField {...params} variant='outlined' size='small' />
    //         )}
    //         disabled={!isEditable}
    //         fullWidth
    //       />
    //     )
    //   },
    // },
    {
      field: 'catalyst',
      headerName: 'Catalyst',
      editable: true,
      minWidth: 225,
      valueGetter: (params, params2) => {
        // console.log('p1', params);
        // console.log('p2', params2);
        return params || ''
      },
      valueFormatter: (params) => {
        console.log('params valueFormatter ', params)
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params, params2) => {
        const { id, value } = params
        // console.log('q1', params);
        // console.log('q2', params2);
        return (
          <select
            value={value || allProducts[0]?.id}
            onChange={(event) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'catalyst',
                value: event.target.value,
              })
            }}
            style={{
              width: '100%',
              padding: '5px',
              border: 'none', // Removes border
              outline: 'none', // Removes focus outline
              background: 'transparent', // Keeps background clean
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
    { field: 'remark', headerName: 'Remark', minWidth: 150, editable: true },
  ]

  // const productionData = [
  //   {
  //     id: 1,
  //     catalyst: 'catalyst A',
  //     apr24: 100,
  //     may24: 120,
  //     jun24: 110,
  //     jul24: 105,
  //     aug24: 115,
  //     sep24: 108,
  //     oct24: 110,
  //     nov24: 112,
  //     dec24: 115,
  //     jan25: 118,
  //     feb25: 120,
  //     mar25: 122,
  //     remark: 'Good',
  //   },
  //   {
  //     id: 2,
  //     catalyst: 'catalyst B',
  //     apr24: 95,
  //     may24: 100,
  //     jun24: 102,
  //     jul24: 108,
  //     aug24: 104,
  //     sep24: 107,
  //     oct24: 110,
  //     nov24: 113,
  //     dec24: 111,
  //     jan25: 115,
  //     feb25: 118,
  //     mar25: 120,
  //     remark: 'Average',
  //   },
  //   {
  //     id: 3,
  //     catalyst: 'catalyst C',
  //     apr24: 130,
  //     may24: 135,
  //     jun24: 140,
  //     jul24: 142,
  //     aug24: 130,
  //     sep24: 125,
  //     oct24: 128,
  //     nov24: 132,
  //     dec24: 140,
  //     jan25: 145,
  //     feb25: 150,
  //     mar25: 148,
  //     remark: 'Excellent',
  //   },
  //   {
  //     id: 4,
  //     catalyst: 'catalyst D',
  //     apr24: 85,
  //     may24: 90,
  //     jun24: 92,
  //     jul24: 95,
  //     aug24: 100,
  //     sep24: 98,
  //     oct24: 100,
  //     nov24: 102,
  //     dec24: 105,
  //     jan25: 108,
  //     feb25: 110,
  //     mar25: 112,
  //     remark: 'Below Average',
  //   },
  //   {
  //     id: 5,
  //     catalyst: 'catalyst E',
  //     apr24: 110,
  //     may24: 120,
  //     jun24: 125,
  //     jul24: 130,
  //     aug24: 135,
  //     sep24: 140,
  //     oct24: 145,
  //     nov24: 150,
  //     dec24: 155,
  //     jan25: 160,
  //     feb25: 165,
  //     mar25: 170,
  //     remark: 'Good',
  //   },
  //   {
  //     id: 6,
  //     catalyst: 'catalyst F',
  //     apr24: 75,
  //     may24: 80,
  //     jun24: 85,
  //     jul24: 88,
  //     aug24: 90,
  //     sep24: 92,
  //     oct24: 95,
  //     nov24: 97,
  //     dec24: 100,
  //     jan25: 102,
  //     feb25: 105,
  //     mar25: 107,
  //     remark: 'Poor',
  //   },
  //   {
  //     id: 7,
  //     catalyst: 'catalyst G',
  //     apr24: 150,
  //     may24: 155,
  //     jun24: 160,
  //     jul24: 165,
  //     aug24: 170,
  //     sep24: 175,
  //     oct24: 180,
  //     nov24: 185,
  //     dec24: 190,
  //     jan25: 195,
  //     feb25: 200,
  //     mar25: 205,
  //     remark: 'Excellent',
  //   },
  //   {
  //     id: 8,
  //     catalyst: 'catalyst H',
  //     apr24: 120,
  //     may24: 130,
  //     jun24: 140,
  //     jul24: 150,
  //     aug24: 160,
  //     sep24: 155,
  //     oct24: 145,
  //     nov24: 135,
  //     dec24: 125,
  //     jan25: 120,
  //     feb25: 115,
  //     mar25: 110,
  //     remark: 'Good',
  //   },
  //   {
  //     id: 9,
  //     catalyst: 'catalyst I',
  //     apr24: 95,
  //     may24: 100,
  //     jun24: 110,
  //     jul24: 115,
  //     aug24: 105,
  //     sep24: 120,
  //     oct24: 125,
  //     nov24: 130,
  //     dec24: 135,
  //     jan25: 140,
  //     feb25: 145,
  //     mar25: 150,
  //     remark: 'Average',
  //   },
  //   {
  //     id: 10,
  //     catalyst: 'catalyst J',
  //     apr24: 100,
  //     may24: 110,
  //     jun24: 115,
  //     jul24: 120,
  //     aug24: 125,
  //     sep24: 130,
  //     oct24: 135,
  //     nov24: 140,
  //     dec24: 145,
  //     jan25: 150,
  //     feb25: 155,
  //     mar25: 160,
  //     remark: 'Good',
  //   },
  // ]

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={csData}
        // rows={productionData}
        title='Catalyst Selectivity Data'
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

export default SelectivityData
