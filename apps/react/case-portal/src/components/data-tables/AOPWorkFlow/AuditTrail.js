// UserManagement.jsx
import { Box } from '@mui/material'
// import { useNavigate } from 'react-router-dom'
import DataGridTable from 'components/data-tables/ASDataGrid'

const columns = [
  { field: 'username', headerName: 'Username', width: 150 },
  //   {
  //     field: 'firstName',
  //     headerName: 'First Name',
  //     width: 150,
  //   },
  //   { field: 'lastName', headerName: 'Last Name', width: 150 },
  { field: 'role', headerName: 'User Role', width: 150 },
  { field: 'status', headerName: 'Status', width: 150 },
  {
    field: 'dateTime',
    headerName: 'Date And Time',
    width: 150,
  },
  { field: 'comments', headerName: 'Comments', width: 150 },
]

const rows = [
  {
    id: 1,
    username: 'user1',
    // firstName: 'Pavan',
    // lastName: 'Pophale',
    role: 'Head',
    status: ['Submit Plan AOP'],
    dateTime: ['09/04/2025'],
    comments: ['Remark1'],
  },
  {
    id: 2,
    username: 'user2',
    // firstName: 'John',
    // lastName: 'Doe',
    role: 'Head',
    status: 'Validate Plan AOP',
    dateTime: ['10/04/2025'],
    comments: ['Remark2'],
  },
]

const AuditTrail = () => {
  // const navigate = useNavigate()

  const handleAddPlantSite = () => {
    // Navigate to our dedicated form screen.
    // navigate('/user-form', {
    //   state: row,
    // })
    console.log('handleAdd')
  }
  const defaultCustomHeight = { mainBox: '72vh', otherBox: '118%' }

  return (
    <Box sx={{ height: 600, width: '100%', p: 0 }}>
      {/* <Button onClick={handleAdd} variant='contained' sx={{ mb: 2 }}>
        Add
      </Button> */}

      <DataGridTable
        columns={columns}
        rows={rows}
        handleAddPlantSite={handleAddPlantSite}
        permissions={{
          showAction: false,
          addButton: false,
          deleteButton: false,
          editButton: false,
          viewBtn: true,
          showUnit: false,
          saveWithRemark: false,
          saveBtn: false,
          customHeight: defaultCustomHeight,
        }}
      />
    </Box>
  )
}

export default AuditTrail
