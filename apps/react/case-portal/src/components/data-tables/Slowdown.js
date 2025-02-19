import { DataService } from 'services/DataService'
import {
  Autocomplete,
  TextField,
} from '../../../node_modules/@mui/material/index'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'
import { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'

const productOptions = [
  'Product A',
  'Product B',
  'Product C',
  'Product D',
  'Product E',
  'Product F',
  'Product G',
  'Product H',
  'Product I',
  'Product J',
  'Product K',
  'Product L',
]
const colDefs = [
  {
    field: 'discription',
    headerName: 'Slowdown Desc',
    minWidth: 200,
    editable: true,
    renderHeader: () => (
      <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
        Slowdown Desc
      </div>
    ),
    flex: 3,
  },
  {
    field: 'product',
    headerName: 'Product',
    editable: true,
    minWidth: 200,
    renderEditCell: (params) => {
      const { id } = params
      const isEditable = id > 10 

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


  {
    field: "maintStartDateTime",
    headerName: "SD- From",
    type: "dateTime",
    minWidth: 200,
    valueGetter: (params) => {
      const value = params; 
      const parsedDate = value
        ? dayjs(value, "MMM D, YYYY, h:mm:ss A").toDate()
        : null;
      return parsedDate;
    },
  },
  
  {
    field: "maintEndDateTime",
    headerName: "SD- To",
    type: "dateTime",
    minWidth: 200,
    valueGetter: (params) => {
      const value = params; 
      const parsedDate = value
        ? dayjs(value, "MMM D, YYYY, h:mm:ss A").toDate()
        : null;
      return parsedDate;
    },
  },
  
  
  
  
  {
    field: "rate",
    headerName: "Rate",
    editable: false,
    type: "number",
    minWidth: 100,
    maxWidth: 150,
  },


  {
    field: "remarks",
    headerName: "Remarks",
    editable: false,
    minWidth: 200,
    maxWidth: 400,
  },
  
]

const SlowDown = () => {
  const [slowDownData, setSlowDownData] = useState([])
  const keycloak = useSession()

useEffect(() => {
  const fetchData = async () => {
    try {
      const data = await DataService.getSlowDownPlantData(keycloak)
      const formattedData = data.map((item, index) => ({
        ...item,
        id: item?.maintenanceId, 
      }))
      setSlowDownData(formattedData)
    } catch (error) {
      console.error('Error fetching slowdown data:', error)
    }
  }
  fetchData()
}, [])
  

  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={slowDownData}
        title='Slowdown Records'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
      />
    </div>
  )
}

export default SlowDown
