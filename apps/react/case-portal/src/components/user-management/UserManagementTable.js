// // UserManagement.jsx
// import { Box } from '@mui/material'
// import { useNavigate } from 'react-router-dom'
// import DataGridTable from 'components/data-tables/ASDataGrid'
// import React, { useEffect, useState } from 'react'
// import { DataService } from 'services/DataService'

// const columns = [
//   { field: 'username', headerName: 'Username', width: 150 },
//   // {
//   //   field: 'firstName',
//   //   headerName: 'First Name',
//   //   width: 150,
//   // },
//   // { field: 'lastName', headerName: 'Last Name', width: 150 },
//   { field: 'role', headerName: 'Role', width: 150 },
//   { field: 'verticals', headerName: 'Verticals', width: 150 },
//   {
//     field: 'sites',
//     headerName: 'Sites',
//     width: 150,
//   },
//   { field: 'plants', headerName: 'Plants', width: 150 },
// ]

// const row = [
//   {
//     id: 1,
//     username: 'user1',
//     firstName: 'Pavan',
//     lastName: 'Pophale',
//     role: 'Head',
//     verticals: ['MEG', 'PE', 'PP'],
//     sites: ['HMD', 'DMD', 'VMD'],
//     plants: ['MEG1', 'MEG2', 'MEG3', 'EOEG', 'PE1', 'PE2'],
//     email: 'pophale8499@gmail.com',
//   },
//   {
//     id: 2,
//     username: 'user2',
//     firstName: 'John',
//     lastName: 'Doe',
//     role: 'Manager',
//     verticals: ['PE'],
//     sites: ['HMD', 'DMD'],
//     plants: ['PE1', 'PE2'],
//     email: 'test@gmail.com',
//   },
// ]

import { Box } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import DataGridTable from 'components/data-tables/ASDataGrid'
import { useCallback, useEffect, useState, useRef } from 'react'
import { DataService } from 'services/DataService'

const UserManagementTable = ({ keycloak }) => {
  const navigate = useNavigate()
  const [rows, setRows] = useState([])
  const [vert, setAllVerts] = useState([]) // not used in mappedRows now; API data is in `plantSiteData`
  const [site, setAllSites] = useState([])
  const [plant, setAllPlants] = useState([])
  const [plantSiteData, setPlantSiteData] = useState([]) // full API hierarchy data
  const [roles, setRoles] = useState([])
  const [rowModesModel, setRowModesModel] = useState({})
  const unsavedChangesRef = useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const columns = [
    { field: 'username', headerName: 'Username', width: 150 },
    {
      field: 'role',
      headerName: 'Role',
      width: 150,
      // Optionally, you can add an editable dropdown for roles here.
    },
    { field: 'verticals', headerName: 'Verticals', width: 150 },
    { field: 'sites', headerName: 'Sites', width: 150 },
    { field: 'plants', headerName: 'Plants', width: 150 },
  ]

  const processRowUpdate = useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId] = newRow
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
    navigate('/user-form', { state: row })
  }

  // Fetch the plant/site data from the API.
  useEffect(() => {
    const getPlantAndSite = async () => {
      try {
        const response = await DataService.getAllSites(keycloak)
        if (response) {
          console.log('Plant Site Data: ', response)
          setPlantSiteData(response)
          // You could also flatten these into separate states if needed:
          // setAllVerts(response)
          // setAllSites(response.flatMap(v => v.sites))
          // setAllPlants(response.flatMap(v => v.sites).flatMap(s => s.plants))
        }
      } catch (error) {
        console.error('Error fetching plant and site data:', error)
      }
    }
    getPlantAndSite()
  }, [keycloak])

  // Map users data by filtering with plantSiteData to show display names.
  useEffect(() => {
    // Only map if API data is loaded (plantSiteData non-empty)
    if (plantSiteData.length === 0) return

    const getUsersData = async () => {
      try {
        const res = await DataService.getUsersData(keycloak)
        // console.log('Users data: ', res.data)

        // We'll use the API data to retrieve display names.
        // Assume user.attributes contains arrays of strings.
        const mappedRows = res.data.map((item, index) => {
          // const userVerticals = item.user.attributes?.verticals || []
          // const userSites = item.user.attributes?.sites || []
          const userPlantsSiteVert =
            JSON.parse(item.user.attributes?.plants) || []
          // console.log(userPlantsSiteVert)

          // Assume userPlantsSiteVert is the parsed JSON value.
          const userVerticalIds = userPlantsSiteVert.reduce((acc, mapping) => {
            // For each mapping (object) in the array, push its keys.
            return acc.concat(Object.keys(mapping))
          }, [])

          const userSiteIds = userPlantsSiteVert.reduce((acc, mapping) => {
            // mapping is an object whose value is an object with site ids.
            Object.values(mapping).forEach((siteObj) => {
              acc = acc.concat(Object.keys(siteObj))
            })
            return acc
          }, [])

          const userPlantIds = userPlantsSiteVert.reduce((acc, mapping) => {
            Object.values(mapping).forEach((siteObj) => {
              Object.values(siteObj).forEach((plantArray) => {
                acc = acc.concat(plantArray) // plantArray is an array of plant ids.
              })
            })
            return acc
          }, [])

          // console.log('User Vertical IDs: ', userVerticalIds)
          // console.log('User Site IDs: ', userSiteIds)
          // console.log('User Plant IDs: ', userPlantIds)
          // Map vertical display names:
          const mappedVerticals = plantSiteData
            .filter((v) => userVerticalIds.includes(v.id))
            .map((v) => v.displayName)
            .join(', ')

          // Flatten all sites from the API:
          const allSites = plantSiteData.flatMap((v) => v.sites)
          const mappedSites = allSites
            .filter((s) => userSiteIds.includes(s.id))
            .map((s) => s.displayName)
            .join(', ')

          // Flatten all plants from the API:
          const allPlants = allSites.flatMap((s) => s.plants)
          const mappedPlants = allPlants
            .filter((p) => userPlantIds.includes(p.id))
            .map((p) => p.displayName)
            .join(', ')

          // console.log(mappedVerticals, mappedSites, mappedPlants)

          return {
            id: index,
            username: item.user.username,
            role: item.realmRoles[0] || '',
            verticals: mappedVerticals,
            sites: mappedSites,
            plants: mappedPlants,
            email: item.user.email,
            screen: item.user.attributes?.verticals || '', // Assuming screen is in attributes
          }
        })

        console.log('Mapped Rows: ', mappedRows)
        setRows(mappedRows)
      } catch (error) {
        console.error('Error fetching users data:', error)
      }
    }

    // Also fetch roles
    const getUserRole = async () => {
      try {
        const res = await DataService.getUserRoles(keycloak)
        setRoles(res.data.map((item) => item.name))
      } catch (error) {
        console.error(error)
      }
    }

    getUsersData()
    getUserRole()
  }, [keycloak, plantSiteData])

  return (
    <Box sx={{ height: 600, width: '100%', p: 2 }}>
      <DataGridTable
        columns={columns}
        rows={rows}
        handleAddPlantSite={handleAddPlantSite}
        processRowUpdate={processRowUpdate}
        rowModesModel={rowModesModel}
        unsavedChangesRef={unsavedChangesRef}
        onRowModesModelChange={(newModel) => setRowModesModel(newModel)}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          viewBtn: true,
          showUnit: false,
          saveWithRemark: true,
          saveBtn: false,
          showCheckBox: true,
          nextBtn: true,
        }}
      />
    </Box>
  )
}

export default UserManagementTable
