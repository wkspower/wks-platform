// UserManagement.jsx
import { Box } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import DataGridTable from 'components/data-tables/ASDataGrid'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'

const columns = [
  { field: 'username', headerName: 'Username', width: 150 },
  {
    field: 'firstName',
    headerName: 'First Name',
    width: 150,
  },
  { field: 'lastName', headerName: 'Last Name', width: 150 },
  { field: 'role', headerName: 'Role', width: 150 },
  { field: 'verticals', headerName: 'Verticals', width: 150 },
  {
    field: 'sites',
    headerName: 'Sites',
    width: 150,
  },
  { field: 'plants', headerName: 'Plants', width: 150 },
]

const row = [
  {
    id: 1,
    username: 'user1',
    firstName: 'Pavan',
    lastName: 'Pophale',
    role: 'Head',
    verticals: ['MEG', 'PE', 'PP'],
    sites: ['HMD', 'DMD', 'VMD'],
    plants: ['MEG1', 'MEG2', 'MEG3', 'EOEG', 'PE1', 'PE2'],
    email: 'pophale8499@gmail.com',
  },
  {
    id: 2,
    username: 'user2',
    firstName: 'John',
    lastName: 'Doe',
    role: 'Manager',
    verticals: ['PE'],
    sites: ['HMD', 'DMD'],
    plants: ['PE1', 'PE2'],
    email: 'test@gmail.com',
  },
]

const UserManagementTable = ({ keycloak }) => {
  const navigate = useNavigate()
  const [rows, setRows] = useState()
  const [rowModesModel, setRowModesModel] = useState({})

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    // Keep track of original values before editing
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    return newRow
  }, [])
  const handleAddPlantSite = (row) => {
    // // Navigate to our dedicated form screen.
    // console.log(row)
    console.log(rows)
    navigate('/user-form', {
      state: row,
    })
  }
  useEffect(() => {
    const getUsersData = async () => {
      try {
        const res = await DataService.getUsersData(keycloak)
        console.log(res)
      } catch (error) {
        console.log(error)
      }
    }
    getUsersData()
  })

  return (
    <Box sx={{ height: 600, width: '100%', p: 2 }}>
      {/* <Button onClick={handleAdd} variant='contained' sx={{ mb: 2 }}>
        Add
      </Button> */}

      <DataGridTable
        columns={columns}
        rows={row}
        handleAddPlantSite={handleAddPlantSite}
        processRowUpdate={processRowUpdate}
        rowModesModel={rowModesModel}
        unsavedChangesRef={unsavedChangesRef}
        onRowModesModelChange={(newModel) => setRowModesModel(newModel)}
        permissions={{
          showAction: true,
          addButton: true,
          deleteButton: true,
          editButton: true,
          viewBtn: true,
          showUnit: false,
          saveWithRemark: true,
          saveBtn: true,
        }}
      />
    </Box>
  )
}

export default UserManagementTable
