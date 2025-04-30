// UserManagement.jsx
import { Box } from '@mui/material'
// import { useNavigate } from 'react-router-dom'
import DataGridTable from 'components/data-tables/ASDataGrid'
// import { remarkColumn } from 'components/Utilities/remarkColumn'
import { useEffect, useState } from 'react'
import { CaseService } from 'services/CaseService'
import { truncateRemarks } from 'utils/remarksUtils'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  Tooltip,
  Typography,
} from '../../../../node_modules/@mui/material/index'
//     username: 'user1',
//     // firstName: 'Pavan',
//     // lastName: 'Pophale',
//     role: 'Head',
//     status: ['Submit Plan AOP'],
//     dateTime: ['09/04/2025'],
//     comments: ['Remark1'],
//   },
//   {
//     id: 2,
//     username: 'user2',
//     // firstName: 'John',
//     // lastName: 'Doe',
//     role: 'Head',
//     status: 'Validate Plan AOP',
//     dateTime: ['10/04/2025'],
//     comments: ['Remark2'],
//   },
// ]

const AuditTrail = ({ keycloak, businessKey }) => {
  // const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  // const [currentRowId, setCurrentRowId] = useState(null)
  const openRemarkPopup = (text) => {
    // console.log(row, newRow)
    setCurrentRemark(text || '')
    setRemarkDialogOpen(true)
  }
  // const [columns, setColumns] = useState([])
    // Navigate to our dedicated form screen.
    //   state: row,
    // })

  const columns = [
    { field: 'userName', headerName: 'Username', width: 150 },
    //   {
    //     field: 'firstName',
    //     headerName: 'First Name',
    //     width: 150,
    //   },
    //   { field: 'lastName', headerName: 'Last Name', width: 150 },
    { field: 'role', headerName: 'User Role', width: 150 },
    { field: 'status', headerName: 'Status', width: 150 },
    { field: 'createdAt', headerName: 'Date And Time', width: 150 },
    {
      field: 'body',
      headerName: 'Remark',
      width: 150,
      sortable: false, // no sorting (optional)
      editable: false, // disable edit-on-click
      renderCell: (params) => {
        const text = params.value || ''
        return (
          <Tooltip title='Click to view full remark'>
            <Typography
              noWrap
              sx={{
                cursor: text ? 'pointer' : 'default',
                color: text ? 'inherit' : 'gray',
              }}
              onClick={() => text && openRemarkPopup(text)}
            >
              {truncateRemarks(text)}
            </Typography>
          </Tooltip>
        )
      },
    },
    // { field: 'body', headerName: 'Remark', width: 150 },
  ]
  const handleAddPlantSite = () => {
    // navigate('/user-form', {
    console.log('handleAdd')
  }
  const defaultCustomHeight = { mainBox: '72vh', otherBox: '118%' }
  const fetchData = async () => {
    setLoading(true)
    try {
      var data = await CaseService.getCaseById(keycloak, businessKey)
      // console.log('Data:', data)
      setRows(data?.comments)
      // setColumns(data?.headers)

      // 2. Use it in your component
      // setColumns(generateColumns(data))
      setLoading(false) // Hide loading
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
      setRows([]) // Clear rows on error
      setLoading(false) // Hide loading
    }
  }
  useEffect(() => {
    fetchData()
  }, [])
  return (
    <Box sx={{ height: 600, width: '100%', p: 0 }}>
      {/* <Button onClick={handleAdd} variant='contained' sx={{ mb: 2 }}>
        Add
      </Button> */}

      <DataGridTable
        columns={columns}
        rows={rows}
        loading={loading}
        handleAddPlantSite={handleAddPlantSite}
        // remarkDialogOpen={remarkDialogOpen}
        // setRemarkDialogOpen={setRemarkDialogOpen}
        // currentRemark={currentRemark}
        // setCurrentRemark={setCurrentRemark}
        // currentRowId={currentRowId}
        // setCurrentRowId={setCurrentRowId}
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
      <Dialog
        open={!!remarkDialogOpen}
        onClose={() => setRemarkDialogOpen(false)}
      >
        <DialogTitle>Add Remark</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin='dense'
            id='remark'
            label='Remark'
            type='text'
            fullWidth
            variant='outlined'
            sx={{ width: '100%', minWidth: '600px' }}
            value={currentRemark || ''}
            onChange={(e) => setCurrentRemark(e.target.value)}
            multiline
            rows={8}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRemarkDialogOpen(false)}>Cancel</Button>
          {/* <Button onClick={handleCloseRemark}>Cancel</Button> */}
          {/* <Button onClick={handleRemarkSave} disabled={!currentRemark?.trim()}>
                </Button> */}
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default AuditTrail
