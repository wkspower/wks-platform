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
    headerName: 'Shutdown Desc',
    minWidth: 300,
    editable: true,
    renderHeader: () => (
      <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
        Shutdown Desc
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
    field: "durationInMins",
    headerName: "Duration (hrs)",
    editable: false,
    type: "number",
    minWidth: 100,
    maxWidth: 150,
    renderCell: (params) => {
      const durationInHours = params.value ? (params.value / 60).toFixed(2) : "0.00";
      return `${durationInHours}`;
    },
  },


  {
    field: "remark",
    headerName: "Remarks",
    editable: false,
    minWidth: 200,
    maxWidth: 400,
  },
  
]

const TurnaroundPlanTable = () => {
  const [TaData, setTaData] = useState([])
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
      console.error('Error fetching shutdown data:', error)
    }
  }
  fetchData()
}, [])
  

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
      />
    </div>
  )
}

export default TurnaroundPlanTable

