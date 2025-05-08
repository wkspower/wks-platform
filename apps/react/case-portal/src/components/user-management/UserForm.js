import { useState, useEffect } from 'react'
import {
  Container,
  Box,
  Typography,
  Grid,
  FormControl,
  Select,
  MenuItem,
  Checkbox,
  ListItemText,
  IconButton,
  Button,
  Divider,
  Backdrop,
  CircularProgress,
  // Snackbar,
} from '@mui/material'
import { useLocation, useNavigate } from 'react-router-dom'
import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'
import { DataService } from 'services/DataService'
import Notification from 'components/Utilities/Notification'
import i18n from '../../i18n'
// import DataGridTable from 'components/data-tables/ASDataGrid'

const UserAccessForm = ({ keycloak }) => {
  const location = useLocation()
  const data = location.state || {}
  const navigate = useNavigate()

  // Loading & Snackbar state
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  // User Details State – username is hidden from UI.
  const [userDetails, setUserDetails] = useState({
    username: data.username || '',
    firstname: data.firstName || '',
    lastname: data.lastName || '',
    role: data.role || '',
    email: data.email || '',
    userId: data.userId || '',
  })

  // Plant Site Data and Roles from API.
  const [plantSiteData, setPlantSiteData] = useState([])
  const [userRoles, setUserRoles] = useState([])

  // Vertical selection state and verticalSites state.
  const [selectedVerticals, setSelectedVerticals] = useState([])
  const [verticalSites, setVerticalSites] = useState({})

  // Screens fetched dynamically (based on active vertical).
  const [screens, setScreens] = useState([])

  // Check if data from navigation is empty. Redirect if so.
  useEffect(() => {
    if (Object.keys(data).length === 0) {
      setLoading(true)
      setSnackbarData({
        message: 'No data found. Redirecting to user management.',
        severity: 'error',
      })
      setSnackbarOpen(true)
      setTimeout(() => setSnackbarOpen(false), 1000)
      setLoading(false)
      navigate('/user-management')
    }
  }, [data, navigate])

  // Fetch Plant Site Data and User Roles.
  useEffect(() => {
    const fetchData = async () => {
      try {
        const plantResponse = await DataService.getAllSites(keycloak)
        if (plantResponse) {
          setPlantSiteData(plantResponse)
        }
        const rolesResponse = await DataService.getUserRoles(keycloak)
        if (rolesResponse) {
          setUserRoles(rolesResponse.data.map((roleObj) => roleObj.name))
        }
      } catch (error) {
        console.error('Error fetching data:', error)
      }
    }
    fetchData()
  }, [keycloak])

  // Helper to create the initial verticalSites structure.
  const getInitialVerticalSites = (plantData) => {
    return plantData.reduce((acc, vertical) => {
      acc[vertical.id] = [
        {
          site: '',
          plants: [
            {
              plantId: '',
              screens: [],
              permissions: { read: false, write: false, approve: false },
            },
          ],
        },
      ]
      return acc
    }, {})
  }

  // Initialize verticalSites & default vertical selection when plantSiteData loads.
  useEffect(() => {
    if (plantSiteData.length > 0) {
      if (selectedVerticals.length === 0) {
        setSelectedVerticals([plantSiteData[0].id])
      }
      setVerticalSites(getInitialVerticalSites(plantSiteData))
    }
  }, [plantSiteData])

  // Prepopulate vertical selections from navigation data if available.
  useEffect(() => {
    if (plantSiteData.length > 0 && data && data.verticals) {
      const newSelectedVerticals = plantSiteData
        .filter(
          (vertical) =>
            data.verticals.includes(vertical.displayName) ||
            data.verticals.includes(vertical.name),
        )
        .map((vertical) => vertical.id)

      const newVerticalSites = {}
      newSelectedVerticals.forEach((verticalId) => {
        const vertical = plantSiteData.find((v) => v.id === verticalId)
        if (!vertical) return
        const siteEntries = vertical.sites.reduce((acc, site) => {
          if (
            data.sites &&
            (data.sites.includes(site.displayName) ||
              data.sites.includes(site.name))
          ) {
            const matchedPlant = site.plants.find(
              (plant) =>
                data.plants &&
                (data.plants.includes(plant.displayName) ||
                  data.plants.includes(plant.name)),
            )
            acc.push({
              site: site.id,
              plants: [
                {
                  plantId: matchedPlant ? matchedPlant.id : '',
                  screens: [],
                  permissions: { read: false, write: false, approve: false },
                },
              ],
            })
          }
          return acc
        }, [])
        newVerticalSites[verticalId] =
          siteEntries.length > 0
            ? siteEntries
            : [
                {
                  site: '',
                  plants: [
                    {
                      plantId: '',
                      screens: [],
                      permissions: {
                        read: false,
                        write: false,
                        approve: false,
                      },
                    },
                  ],
                },
              ]
      })
      setSelectedVerticals(newSelectedVerticals)
      setVerticalSites(newVerticalSites)
    } else if (plantSiteData.length > 0 && !data.verticals) {
      setSelectedVerticals([plantSiteData[0].id])
      setVerticalSites(getInitialVerticalSites(plantSiteData))
    }
  }, [plantSiteData, data])

  // Helper: Retrieve a vertical by ID.
  const getVerticalById = (id) =>
    plantSiteData.find((vertical) => vertical.id === id)

  // --- Dynamic Fetching of Screens ---
  // Whenever selectedVerticals changes, fetch the available screens
  // For simplicity, we use the first selected vertical.
  // useEffect(() => {
  //   const fetchScreensForActiveVertical = async () => {
  //     if (selectedVerticals.length > 0) {
  //       const activeVerticalId = selectedVerticals[0]
  //       try {
  //         const response = await DataService.getUserScreen(
  //           keycloak,
  //           activeVerticalId,
  //         )
  //         if (response) {
  //           // Map the response data to get the display names of screens.
  //           // setScreens(
  //           //   response.data[0].children[0].children[0].map(
  //           //     (item) => item.title,
  //           //   ),
  //           // )
  //           const screenTitles = response.data[0].children[0].children.map(
  //             (item) => item.title,
  //           )
  //           setScreens(screenTitles)
  //         }
  //       } catch (error) {
  //         console.error('Error fetching screens:', error)
  //       }
  //     }
  //   }

  //   fetchScreensForActiveVertical()
  // }, [selectedVerticals, keycloak])
  function extractScreens(nodes) {
    const result = []

    function recurse(list) {
      for (const node of list) {
        if (node.type === 'item' && node.url) {
          result.push({
            id: node.id,
            title: node.title,
            url: node.url,
            icon: node.icon || '',
          })
        }
        if (Array.isArray(node.children)) {
          recurse(node.children)
        }
      }
    }

    recurse(nodes)
    return result
  }
  useEffect(() => {
    async function fetchScreens() {
      if (!selectedVerticals.length) return
      try {
        const activeVerticalId = selectedVerticals[0]
        const response = await DataService.getUserScreen(
          keycloak,
          activeVerticalId,
        )
        const groups = response.data

        // Flatten all “item” nodes under all children/groups:
        const screens = extractScreens(groups)

        // e.g. setScreens to an array of titles:
        setScreens(screens.map((s) => s.title))

        // If you want to store full metadata:
        // setScreensMetadata(screens)
      } catch (err) {
        console.error('Error fetching screens', err)
      }
    }

    fetchScreens()
  }, [selectedVerticals, keycloak])

  // --- End of Dynamic Fetching of Screens ---

  // Default screen options: if not fetched or provided in navigation data.
  const screenOptions = screens.length
    ? screens
    : [
        'product-demand',
        'product-mcu-val',
        'shutdown-plan',
        'slowdown-plan',
        'maintenance-details',
        'production-norms',
        'catalyst-selectivity',
        'normal-op-norms',
        'shutdown-norms',
        'consumption-norms',
        'feed-stock',
      ]

  // Retrieve available screens for a given plant block.
  const getAvailableScreens = (verticalId, siteIndex, plantIndex) => {
    const verticalData = verticalSites[verticalId]
    if (!verticalData) return screenOptions
    const siteEntry = verticalData[siteIndex]
    if (!siteEntry || !siteEntry.plants) return screenOptions
    const currentPlantBlock = siteEntry.plants[plantIndex]
    if (!currentPlantBlock) return screenOptions
    if (!currentPlantBlock.plantId) return screenOptions

    // Gather screens from duplicate plant blocks (same plantId) in the same site.
    const duplicateScreens = siteEntry.plants
      .filter(
        (_, idx) =>
          idx !== plantIndex && _.plantId === currentPlantBlock.plantId,
      )
      .reduce((acc, block) => acc.concat(block.screens || []), [])
    const currentSelected = currentPlantBlock.screens || []
    return screenOptions.filter(
      (screen) =>
        currentSelected.includes(screen) || !duplicateScreens.includes(screen),
    )
  }

  // Retrieve available sites for a vertical, excluding already selected ones.
  const getAvailableSites = (verticalId, currentIndex) => {
    const vertical = getVerticalById(verticalId)
    if (!vertical) return []
    const selectedSiteIds = verticalSites[verticalId]
      .filter((_, idx) => idx !== currentIndex)
      .map((entry) => entry.site)
      .filter((site) => site)
    return vertical.sites.filter((site) => !selectedSiteIds.includes(site.id))
  }

  // Retrieve available plants for a given site entry, disallowing duplicate selection.
  const getAvailablePlants = (vertical, siteEntry, currentPlantIndex) => {
    if (siteEntry.site) {
      const siteObj = vertical.sites.find((s) => s.id === siteEntry.site)
      const allPlants = siteObj ? siteObj.plants : []
      const selectedPlantIds = siteEntry.plants
        .filter((_, index) => index !== currentPlantIndex)
        .map((plantEntry) => plantEntry.plantId)
      return allPlants.filter((plant) => !selectedPlantIds.includes(plant.id))
    }
    return []
  }

  // Handler for user details.
  const handleUserDetailChange = (e) => {
    setUserDetails({ ...userDetails, [e.target.name]: e.target.value })
  }

  // Handler for vertical multi‑select change.
  const handleVerticalChange = (e) => {
    const { value } = e.target
    const newValue = typeof value === 'string' ? value.split(',') : value
    setSelectedVerticals(newValue)
    setVerticalSites((prev) => {
      const updated = { ...prev }
      newValue.forEach((verticalId) => {
        if (!updated[verticalId]) {
          updated[verticalId] = [
            {
              site: '',
              plants: [
                {
                  plantId: '',
                  screens: [],
                  permissions: { read: false, write: false, approve: false },
                },
              ],
            },
          ]
        }
      })
      return updated
    })
  }

  // Handler for updating a site-level field.
  const handleSiteChange = (verticalId, siteIndex, field, newValue) => {
    setVerticalSites((prev) => {
      const updatedSites = [...prev[verticalId]]
      updatedSites[siteIndex] = {
        ...updatedSites[siteIndex],
        [field]: newValue,
      }
      return { ...prev, [verticalId]: updatedSites }
    })
  }

  // Handler for updating a plant-level field.
  const handlePlantChange = (
    verticalId,
    siteIndex,
    plantIndex,
    field,
    newValue,
  ) => {
    setVerticalSites((prev) => {
      const updatedSites = [...prev[verticalId]]
      const plantEntry = { ...updatedSites[siteIndex].plants[plantIndex] }
      if (['read', 'write', 'approve'].includes(field)) {
        plantEntry.permissions = {
          ...plantEntry.permissions,
          [field]: newValue,
        }
      } else {
        plantEntry[field] = newValue
      }
      updatedSites[siteIndex].plants[plantIndex] = plantEntry
      return { ...prev, [verticalId]: updatedSites }
    })
  }

  // Handlers to add or remove site and plant entries.
  const addSiteEntry = (verticalId) => {
    setVerticalSites((prev) => ({
      ...prev,
      [verticalId]: [
        ...prev[verticalId],
        {
          site: '',
          plants: [
            {
              plantId: '',
              screens: [],
              permissions: { read: false, write: false, approve: false },
            },
          ],
        },
      ],
    }))
  }

  const removeSiteEntry = (verticalId, siteIndex) => {
    setVerticalSites((prev) => {
      const updatedSites = [...prev[verticalId]]
      updatedSites.splice(siteIndex, 1)
      return { ...prev, [verticalId]: updatedSites }
    })
  }

  const addPlantEntry = (verticalId, siteIndex) => {
    setVerticalSites((prev) => {
      const updatedSites = [...prev[verticalId]]
      updatedSites[siteIndex] = {
        ...updatedSites[siteIndex],
        plants: [
          ...updatedSites[siteIndex].plants,
          {
            plantId: '',
            screens: [],
            permissions: { read: false, write: false, approve: false },
          },
        ],
      }
      return { ...prev, [verticalId]: updatedSites }
    })
  }

  const removePlantEntry = (verticalId, siteIndex, plantIndex) => {
    setVerticalSites((prev) => {
      const updatedSites = [...prev[verticalId]]
      const newPlantList = [...updatedSites[siteIndex].plants]
      newPlantList.splice(plantIndex, 1)
      updatedSites[siteIndex] = {
        ...updatedSites[siteIndex],
        plants: newPlantList,
      }
      return { ...prev, [verticalId]: updatedSites }
    })
  }

  // Helper: Transform permissions object into the required permission array.
  const transformPermissions = (permissions) => {
    const perm = []
    if (permissions.read && permissions.write) {
      perm.push('read-write')
    } else {
      if (permissions.read) perm.push('read')
      if (permissions.write) perm.push('write')
    }
    if (permissions.approve) perm.push('approve')
    return perm
  }

  // Save handler that builds the payload in the required structure and calls the API.
  const handleSave = async () => {
    // Build payload from state:
    const userIds = data.map((user) => user.userId)
    const result = {
      role: userDetails.role,
      userIds: userIds,
      attributes: {
        plants: selectedVerticals.map((verticalId) => {
          const verticalData = verticalSites[verticalId] || []
          return {
            verticalId,
            sites: verticalData
              .filter((entry) => entry.site) // only include entries with a selected site.
              .map((entry) => ({
                siteId: entry.site,
                plants: entry.plants
                  .filter((plantEntry) => plantEntry.plantId) // only include entries with a selected plant.
                  .map((plantEntry) => ({
                    plantId: plantEntry.plantId,
                    screens: plantEntry.screens || [],
                    permission: transformPermissions(plantEntry.permissions),
                  })),
              })),
          }
        }),
      },
    }

    //console.log('Saved JSON:', JSON.stringify(result, null, 2))
    try {
      setLoading(true)
      const res = await DataService.updateUserPlants(keycloak, result)
      if (res.status !== 200) {
        throw new Error('Failed to update user')
      }
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'User Data Updated successfully!',
        severity: 'success',
      })
      // alert('User Data Updated successfully!')
      navigate('/user-management', {
        state: 'success',
      })
    } catch (error) {
      console.error('Update failed:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to update user. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  // Reset handler to restore initial state.
  const handleReset = () => {
    setUserDetails({
      username: '',
      firstname: '',
      lastname: '',
      role: '',
      email: '',
    })
    if (plantSiteData.length > 0) {
      setSelectedVerticals([plantSiteData[0].id])
      setVerticalSites(getInitialVerticalSites(plantSiteData))
    }
  }

  if (plantSiteData.length === 0) {
    return <div>Loading...</div>
  }

  return (
    <Container
      sx={{
        backgroundColor: 'white',
        color: 'black',
        p: 2,
        marginLeft: '10px',
      }}
    >
      {loading ? (
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={loading}
        >
          <CircularProgress color='inherit' />
        </Backdrop>
      ) : (
        <>
          <Box p={3}>
            <Grid container spacing={2} alignItems='center'>
              {/* Role Dropdown */}
              <Grid item xs={12} sm={4}>
                <Typography variant='h6' gutterBottom>
                  Role
                </Typography>
                <FormControl fullWidth size='small'>
                  <Select
                    name='role'
                    value={userDetails.role}
                    onChange={handleUserDetailChange}
                  >
                    {userRoles.map((role) => (
                      <MenuItem key={role} value={role}>
                        {role}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              {/* Vertical Dropdown */}
              <Grid item xs={12} sm={4}>
                <Typography variant='h6' gutterBottom>
                  Vertical
                </Typography>
                <FormControl fullWidth size='small'>
                  <Select
                    multiple
                    value={selectedVerticals}
                    onChange={handleVerticalChange}
                    renderValue={(selected) =>
                      selected
                        .map((id) => getVerticalById(id)?.displayName || '')
                        .join(', ')
                    }
                  >
                    {plantSiteData?.map((vertical) => (
                      <MenuItem key={vertical.id} value={vertical.id}>
                        <Checkbox
                          checked={selectedVerticals.includes(vertical.id)}
                        />
                        <ListItemText primary={vertical.displayName} />
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          </Box>
          <Box my={2}>
            <Divider sx={{ borderTop: '2px solid #ccc', mt: 2, pt: 2 }} />
          </Box>

          {selectedVerticals.map((verticalId) => {
            const vertical = getVerticalById(verticalId)
            if (!vertical) return null

            return (
              <Box key={verticalId} mb={2}>
                <Typography
                  variant='h6'
                  fontWeight='bold'
                  sx={{ mb: 1, ml: 1 }}
                >
                  {vertical.displayName}
                </Typography>
                <Box
                  p={2}
                  sx={{ border: '1px solid #eee', borderRadius: '4px' }}
                >
                  {verticalSites[verticalId]?.map((siteEntry, siteIndex) => (
                    <Box key={siteIndex} mb={2}>
                      <Grid container spacing={2} alignItems='center'>
                        {/* Site Dropdown */}
                        <Grid item xs={3} sm={2}>
                          <Typography variant='h6' gutterBottom>
                            Site
                          </Typography>
                          <FormControl fullWidth size='small'>
                            <Select
                              value={siteEntry.site}
                              sx={{ height: '40px' }}
                              onChange={(e) =>
                                handleSiteChange(
                                  verticalId,
                                  siteIndex,
                                  'site',
                                  e.target.value,
                                )
                              }
                            >
                              {getAvailableSites(verticalId, siteIndex).map(
                                (siteOption) => (
                                  <MenuItem
                                    key={siteOption.id}
                                    value={siteOption.id}
                                  >
                                    <ListItemText
                                      primary={siteOption.displayName}
                                    />
                                  </MenuItem>
                                ),
                              )}
                            </Select>
                          </FormControl>
                        </Grid>
                        {/* Add/Remove Site Entry */}
                        <Grid item xs={1}>
                          <IconButton
                            onClick={() => addSiteEntry(verticalId)}
                            color='primary'
                            sx={{ marginTop: '15px' }}
                          >
                            <AddIcon />
                          </IconButton>
                        </Grid>
                        {verticalSites[verticalId].length > 1 && (
                          <Grid item xs={1}>
                            <IconButton
                              onClick={() =>
                                removeSiteEntry(verticalId, siteIndex)
                              }
                              color='secondary'
                              sx={{ marginTop: '15px' }}
                            >
                              <DeleteIcon />
                            </IconButton>
                          </Grid>
                        )}
                      </Grid>

                      {/* Plant Section */}
                      <Box ml={4} mt={2}>
                        {siteEntry.plants.map((plantEntry, plantIndex) => (
                          <Grid
                            container
                            spacing={2}
                            alignItems='center'
                            key={plantIndex}
                          >
                            {/* Plant Dropdown */}
                            <Grid item xs={4} sm={3}>
                              <Typography variant='subtitle2'>Plant</Typography>
                              <FormControl fullWidth size='small'>
                                <Select
                                  value={plantEntry.plantId}
                                  onChange={(e) =>
                                    handlePlantChange(
                                      verticalId,
                                      siteIndex,
                                      plantIndex,
                                      'plantId',
                                      e.target.value,
                                    )
                                  }
                                >
                                  {getAvailablePlants(
                                    vertical,
                                    siteEntry,
                                    plantIndex,
                                  ).map((plantOption) => (
                                    <MenuItem
                                      key={plantOption.id}
                                      value={plantOption.id}
                                    >
                                      {plantOption.displayName}
                                    </MenuItem>
                                  ))}
                                </Select>
                              </FormControl>
                            </Grid>
                            {/* Add or Remove Plant Entry */}
                            <Grid item xs={1}>
                              {plantIndex === siteEntry.plants.length - 1 ? (
                                <IconButton
                                  onClick={() =>
                                    addPlantEntry(verticalId, siteIndex)
                                  }
                                  color='primary'
                                  sx={{ marginTop: '8px' }}
                                >
                                  <AddIcon />
                                </IconButton>
                              ) : (
                                <IconButton
                                  onClick={() =>
                                    removePlantEntry(
                                      verticalId,
                                      siteIndex,
                                      plantIndex,
                                    )
                                  }
                                  color='secondary'
                                  sx={{ marginTop: '8px' }}
                                >
                                  <DeleteIcon />
                                </IconButton>
                              )}
                            </Grid>
                            {/* Screens Dropdown */}
                            <Grid item xs={4} sm={3}>
                              <Typography variant='subtitle2'>
                                Screens
                              </Typography>
                              <FormControl fullWidth size='small'>
                                <Select
                                  multiple
                                  value={plantEntry.screens || []}
                                  onChange={(e) =>
                                    handlePlantChange(
                                      verticalId,
                                      siteIndex,
                                      plantIndex,
                                      'screens',
                                      e.target.value,
                                    )
                                  }
                                  renderValue={(selected) =>
                                    selected
                                      .map((screen) => i18n.t(screen))
                                      .join(', ')
                                  }
                                >
                                  {getAvailableScreens(
                                    verticalId,
                                    siteIndex,
                                    plantIndex,
                                  ).map((screen) => (
                                    <MenuItem key={screen} value={screen}>
                                      <Checkbox
                                        checked={(
                                          plantEntry.screens || []
                                        ).includes(screen)}
                                      />
                                      <ListItemText primary={i18n.t(screen)} />
                                    </MenuItem>
                                  ))}
                                </Select>
                              </FormControl>
                            </Grid>
                          </Grid>
                        ))}
                      </Box>
                    </Box>
                  ))}
                </Box>
              </Box>
            )
          })}

          <Box sx={{ display: 'flex', flexDirection: 'column' }}>
            <Box
              sx={{
                mt: 'auto',
                display: 'flex',
                justifyContent: 'flex-end',
                gap: 2,
              }}
            >
              <Button variant='contained' color='primary' onClick={handleSave}>
                Save
              </Button>
              <Button
                variant='outlined'
                color='secondary'
                onClick={handleReset}
              >
                Reset
              </Button>
            </Box>
          </Box>
        </>
      )}

      <Notification
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
      />
    </Container>
  )
}

export default UserAccessForm
