import { Box } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import DataGridTable from 'components/data-tables/ASDataGrid'
import { useCallback, useEffect, useState, useRef } from 'react'
import { DataService } from 'services/DataService'
import Autocomplete from '@mui/material/Autocomplete'
import TextField from '@mui/material/TextField'
import { Checkbox, ListItemText } from '@mui/material'
import { useLocation } from '../../../node_modules/react-router-dom/dist/index'

const UserManagementTable = ({ keycloak }) => {
  const navigate = useNavigate()
  const [rows, setRows] = useState([]) // State for grid rows
  const [plantSiteData, setPlantSiteData] = useState([])
  // const [roles, setRoles] = useState([])
  const [vert, setAllVerts] = useState([]) // used for mapping row data
  const [site, setAllSites] = useState([])
  const [plant, setAllPlants] = useState([])
  const [loading, setLoading] = useState(false)
  const [rowModesModel, setRowModesModel] = useState({})
  const [modifiedCells, setModifiedCells] = useState({})

  const unsavedChangesRef = useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [inputValue, setInputValue] = useState('') // keeps the typed text intact
  const [open, setOpen] = useState(false)

  const location = useLocation()
  const data = location.state || ''
  // console.log(data)
  useEffect(() => {
    // if (data?.includes('')) return
    if (data?.includes('success')) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'User Data Updated successfully!',
        severity: 'success',
      })
    } else {
      // setSnackbarOpen(true)
      // setSnackbarData({
      //   message: 'User Data not updated!',
      //   severity: 'error',
      // })
      return
    }
  }, [data])
  // State for Autocomplete selections.
  // final selections: used for grid rows.
  const [selectedUsers, setSelectedUsers] = useState([])
  const [selectionModel, setSelectionModel] = useState([])

  // temporary state: used for user selection (confirmation required via Enter)
  const [tempSelectedUsers, setTempSelectedUsers] = useState([])
  const [searchOptions, setSearchOptions] = useState([])

  const columns = [
    { field: 'username', headerName: 'Username', width: 150 },
    // add any additional columns if needed
  ]

  const processRowUpdate = useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    const updatedFields = []
    for (const key in newRow) {
      if (
        Object.prototype.hasOwnProperty.call(newRow, key) &&
        newRow[key] !== oldRow[key]
      ) {
        updatedFields.push(key)
      }
    }

    unsavedChangesRef.current.unsavedRows[rowId] = newRow
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }
    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )
    if (updatedFields.length > 0) {
      setModifiedCells((prevModifiedCells) => ({
        ...prevModifiedCells,
        [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
      }))
    }
    return newRow
  }, [])

  const handleAddPlantSite = () => {
    navigate('/user-form', { state: rows })
  }

  // Fetch plant and site data.
  useEffect(() => {
    const getPlantAndSite = async () => {
      try {
        const response = await DataService.getAllSites(keycloak)
        if (response) {
          setPlantSiteData(response)
        }
      } catch (error) {
        console.error('Error fetching plant and site data:', error)
      }
    }
    getPlantAndSite()
  }, [keycloak])

  const handleSearchChange = async (value) => {
    if (value.length > 2) {
      setLoading(true)
      try {
        const res = await DataService.getUserBySearch(keycloak, value)
        setSearchOptions(res.data)
      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    } else {
      setSearchOptions([])
    }
  }
  // Finalize selection when Enter key is pressed.
  const finalizeSelection = (selected) => {
    setSelectedUsers(selected)
    // Map selected users into grid row data.
    const newRows = selected.map((user, index) => ({
      id: index,
      username: user.username,
      userId: user.id,
      role: 'cts_head',
      email: user.email,
      verticals: vert,
      plants: plant,
      sites: site,
      // map additional properties as needed
    }))
    setRows(newRows)
  }

  // Handle Autocomplete selection changes by updating the temporary state.
  // const handleTempSelectionChange = (event, newSelected) => {
  //   setTempSelectedUsers(newSelected)
  // }

  // Existing effect to fetch existing user data for the grid.
  useEffect(() => {
    if (plantSiteData.length === 0) return

    const getUsersData = async () => {
      try {
        const res = await DataService.getUsersData(keycloak)
        const mappedRows = res.data.map((item, index) => {
          const userPlantsSiteVert =
            JSON?.parse(item?.user?.attributes?.plants || '[]') || []
          const userVerticalIds = userPlantsSiteVert.reduce(
            (acc, mapping) => acc.concat(Object.keys(mapping)),
            [],
          )
          const userSiteIds = userPlantsSiteVert.reduce((acc, mapping) => {
            Object.values(mapping).forEach((siteObj) => {
              acc = acc.concat(Object.keys(siteObj))
            })
            return acc
          }, [])
          const userPlantIds = userPlantsSiteVert.reduce((acc, mapping) => {
            Object.values(mapping).forEach((siteObj) => {
              Object.values(siteObj).forEach((plantArray) => {
                acc = acc.concat(plantArray)
              })
            })
            return acc
          }, [])

          const mappedVerticals = plantSiteData
            .filter((v) => userVerticalIds.includes(v.id))
            .map((v) => v.displayName)
            .join(', ')
          const allSites = plantSiteData.flatMap((v) => v.sites)
          const mappedSites = allSites
            .filter((s) => userSiteIds.includes(s.id))
            .map((s) => s.displayName)
            .join(', ')
          const allPlants = allSites.flatMap((s) => s.plants)
          const mappedPlants = allPlants
            .filter((p) => userPlantIds.includes(p.id))
            .map((p) => p.displayName)
            .join(', ')

          return {
            id: index,
            userId: item?.user?.id,
            username: item.user.username,
            role: item.realmRoles[0] || '',
            verticals: mappedVerticals,
            sites: mappedSites,
            plants: mappedPlants,
            email: item.user.email,
            screen: item.user.attributes?.verticals || '',
          }
        })
        // Optionally update these states if needed.
        setAllPlants(mappedRows.plants)
        setAllSites(mappedRows.sites)
        setAllVerts(mappedRows.verticals)
        // console.log('Mapped Rows: ', mappedRows.verticals)
        // Optionally, uncomment if you want to show API users by default:
        // setRows(mappedRows);
      } catch (error) {
        console.error('Error fetching users data:', error)
      }
    }

    // const getUserRole = async () => {
    //   try {
    //     const res = await DataService.getUserRoles(keycloak)
    //     setRoles(res.data.map((item) => item.name))
    //   } catch (error) {
    //     console.error(error)
    //   }
    // }

    getUsersData()
    // getUserRole()
  }, [keycloak, plantSiteData])

  const defaultCustomHeight = { mainBox: '60vh', otherBox: '124%' }
  const handleDeleteSelected = () => {
    console.log(selectionModel)
    setRows((prev) => prev.filter((row) => !selectionModel.includes(row.id)))
    setSelectionModel([]) // clear selection
  }
  return (
    <Box sx={{ height: 600, width: '100%', p: 2 }}>
      {/* Autocomplete for selecting multiple users */}
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2, mr: 1 }}>
        <Autocomplete
          multiple
          disableCloseOnSelect
          filterSelectedOptions
          open={open}
          onOpen={() => setOpen(true)}
          onClose={() => setOpen(false)}
          options={searchOptions}
          getOptionLabel={(opt) => opt.username}
          value={tempSelectedUsers}
          inputValue={inputValue} // controlled text :contentReference[oaicite:3]{index=3}
          onInputChange={(e, newVal, reason) => {
            if (reason === 'input') {
              setInputValue(newVal)
              handleSearchChange(newVal)
            }
          }}
          onChange={(e, newSel) => {
            setTempSelectedUsers(newSel)
            finalizeSelection(newSel)
            e.preventDefault()
          }}
          loading={loading}
          noOptionsText='No users found'
          sx={{ width: '100%' }}
          renderOption={(props, option, { selected }) => {
            const { id } = props
            return (
              <li key={id} {...props}>
                <Checkbox checked={selected} style={{ marginRight: 8 }} />
                <ListItemText primary={option.username} />
              </li>
            )
          }}
          renderInput={(params) => (
            <TextField
              {...params}
              label='Select Users'
              variant='outlined'
              // onKeyDown={(e) => {
              //   if (e.key === 'Enter') {
              //     finalizeSelection(tempSelectedUsers)
              //     e.preventDefault()
              //   }
              // }}
            />
          )}
        />
      </Box>

      {/* DataGridTable now uses selectedUsers as the rows to show only the confirmed selections */}
      <DataGridTable
        modifiedCells={modifiedCells}
        columns={columns}
        rows={selectedUsers}
        handleAddPlantSite={handleAddPlantSite}
        processRowUpdate={processRowUpdate}
        rowModesModel={rowModesModel}
        selectedUsers={selectedUsers}
        setSelectedUsers={setSelectedUsers}
        unsavedChangesRef={unsavedChangesRef}
        onRowModesModelChange={(newModel) => setRowModesModel(newModel)}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        selectionModel={selectionModel}
        setSelectionModel={setSelectionModel}
        setModifiedCells={setModifiedCells}
        handleDeleteSelected={handleDeleteSelected}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: true,
          editButton: false,
          viewBtn: false,
          showUnit: false,
          saveWithRemark: true,
          saveBtn: false,
          showCheckBox: true,
          nextBtn: true,
          deleteAllBtn: true,
          customHeight: defaultCustomHeight,
          allAction: false,
        }}
      />
    </Box>
  )
}

export default UserManagementTable
