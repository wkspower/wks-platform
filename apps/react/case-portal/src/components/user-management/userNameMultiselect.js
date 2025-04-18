import React, { useState } from 'react'
import Autocomplete from '@mui/material/Autocomplete'
import TextField from '@mui/material/TextField'
import Checkbox from '@mui/material/Checkbox'
import ListItemText from '@mui/material/ListItemText'
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank'
import CheckBoxIcon from '@mui/icons-material/CheckBoxIcon'

const UserAutocompleteWithEnterConfirm = ({
  searchOptions,
  loading,
  handleSearchChange,
  handleUserSelectChange, // expects (event, finalSelectedUsers) => {}
}) => {
  // Temporary state only for the Autocomplete selection.
  const [tempSelectedUsers, setTempSelectedUsers] = useState([])

  return (
    <Autocomplete
      multiple
      disableCloseOnSelect
      id='user-autocomplete'
      // Filter out options that are already temporarily selected.
      options={searchOptions.filter(
        (option) =>
          !tempSelectedUsers.some((selected) => selected.id === option.id),
      )}
      getOptionLabel={(option) => option.username}
      value={tempSelectedUsers}
      onInputChange={(event, inputValue) => {
        handleSearchChange(event, inputValue)
      }}
      onChange={(event, newSelected) => {
        // Only update the temporary state on each selection.
        setTempSelectedUsers(newSelected)
      }}
      loading={loading}
      noOptionsText='No users found'
      sx={{ width: '100%' }}
      renderOption={(props, option, { selected }) => (
        <li {...props}>
          <Checkbox
            icon={<CheckBoxOutlineBlankIcon fontSize='small' />}
            checkedIcon={<CheckBoxIcon fontSize='small' />}
            style={{ marginRight: 8 }}
            checked={selected}
          />
          <ListItemText primary={option.username} />
        </li>
      )}
      renderInput={(params) => (
        <TextField
          {...params}
          label='Select Users'
          variant='outlined'
          onKeyDown={(e) => {
            // Confirm selection on Enter key press.
            if (e.key === 'Enter') {
              // Call the higher-level handler to update the grid
              handleUserSelectChange(e, tempSelectedUsers)
              // Optionally, you can clear the temporary selection if required:
              // setTempSelectedUsers([]);
              // Also, you might want to prevent the default "Enter" behavior.
              e.preventDefault()
            }
          }}
        />
      )}
    />
  )
}

export default UserAutocompleteWithEnterConfirm
