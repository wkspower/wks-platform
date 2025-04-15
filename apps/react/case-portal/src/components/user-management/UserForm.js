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
  FormControlLabel,
  FormGroup,
  IconButton,
  Button,
  Divider,
} from '@mui/material'
import { useLocation } from 'react-router-dom'
import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'
import { DataService } from 'services/DataService'

const UserAccessForm = ({ keycloak }) => {
  const location = useLocation()
  const data = location.state || {}

  // User Details State – username is hidden from the UI.
  const [userDetails, setUserDetails] = useState({
    username: data.username || '',
    firstname: data.firstName || '',
    lastname: data.lastName || '',
    role: data.role || '',
    email: data.email || '',
  })

  // Plant Site Data from API and user roles.
  const [plantSiteData, setPlantSiteData] = useState([])
  const [userRoles, setUserRoles] = useState([])

  useEffect(() => {
    const getPlantAndSite = async () => {
      try {
        const response = await DataService.getAllSites(keycloak)
        if (response) {
          console.log(response)
          setPlantSiteData(response)
        }
      } catch (error) {
        console.error('Error fetching plant and site data:', error)
      }
    }
    const getAllRoles = async () => {
      try {
        const response = await DataService.getUserRoles(keycloak)
        if (response) {
          setUserRoles(response?.data.map((i) => i.name))
        }
      } catch (error) {
        console.error('Error fetching plant and site data:', error)
      }
    }
    getPlantAndSite()
    getAllRoles()
  }, [keycloak])

  // Initialize verticalSites state with each vertical's entry.
  const getInitialVerticalSites = (plantData) => {
    return plantData?.reduce((acc, vertical) => {
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

  // Vertical selection state and verticalSites state.
  const [selectedVerticals, setSelectedVerticals] = useState([])
  const [verticalSites, setVerticalSites] = useState({})

  // Initialize verticalSites when plantSiteData loads.
  useEffect(() => {
    if (plantSiteData.length > 0) {
      if (selectedVerticals.length === 0) {
        setSelectedVerticals([plantSiteData[0].id])
      }
      setVerticalSites(getInitialVerticalSites(plantSiteData))
    }
  }, [plantSiteData])

  // Prepopulate from navigation data if available.
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

  // Helper: Retrieve vertical object by id.
  const getVerticalById = (id) =>
    plantSiteData?.find((vertical) => vertical.id === id)

  // NEW HELPER: Get available screens for a given plant block.
  // MODIFIED: This function ensures that if the same plant (duplicate) is selected,
  // screens already chosen in another duplicate plant block for the same site are filtered out.
  // However, if a new plant is selected (plantId is empty or differs), the full options (screenOptions) are returned.
  const getAvailableScreens = (verticalId, siteIndex, plantIndex) => {
    const verticalData = verticalSites[verticalId]
    if (!verticalData) return screenOptions
    const siteEntry = verticalData[siteIndex]
    if (!siteEntry || !siteEntry.plants) return screenOptions
    const currentPlantBlock = siteEntry.plants[plantIndex]
    if (!currentPlantBlock) return screenOptions
    // If no plantId is selected, return full screenOptions.
    if (!currentPlantBlock.plantId) return screenOptions

    // Gather screens from other plant blocks in the same site with the same plantId.
    const duplicateScreens = siteEntry.plants
      .filter(
        (_, idx) =>
          idx !== plantIndex && _.plantId === currentPlantBlock.plantId,
      )
      .reduce((acc, block) => acc.concat(block.screens || []), [])

    // Allow the current block's selected screens even if they are in duplicateScreens.
    const currentSelected = currentPlantBlock.screens || []

    return screenOptions.filter(
      (screen) =>
        currentSelected.includes(screen) || !duplicateScreens.includes(screen),
    )
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

  // Returns available sites for a vertical excluding ones already selected (except the current row).
  const getAvailableSites = (verticalId, currentIndex) => {
    const vertical = getVerticalById(verticalId)
    if (!vertical) return []
    const selectedSiteIds = verticalSites[verticalId]
      .filter((_, idx) => idx !== currentIndex)
      .map((entry) => entry.site)
      .filter((site) => site)
    return vertical.sites.filter((site) => !selectedSiteIds.includes(site.id))
  }

  // MODIFIED: Allow duplicate plants by returning all plant options without filtering.
  const getAvailablePlants = (vertical, siteEntry, currentPlantIndex) => {
    if (siteEntry.site) {
      const siteObj = vertical.sites.find((s) => s.id === siteEntry.site)
      return siteObj ? siteObj.plants : []
    }
    return []
  }

  // Add a new site entry for a given vertical.
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

  // Remove a site entry for a vertical.
  const removeSiteEntry = (verticalId, siteIndex) => {
    setVerticalSites((prev) => {
      const updatedSites = [...prev[verticalId]]
      updatedSites.splice(siteIndex, 1)
      return { ...prev, [verticalId]: updatedSites }
    })
  }

  // Add a new plant entry for a given vertical & site.
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

  // Remove a plant entry for a given vertical & site.
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

  // Save handler.
  const handleSave = () => {
    const result = selectedVerticals.map((verticalId) => {
      const siteData = verticalSites[verticalId] || []
      const siteObject = siteData.reduce((acc, entry) => {
        if (entry.site) {
          acc[entry.site] = entry.plants.map((plant) => ({
            plantId: plant.plantId,
            screens: plant.screens,
            permissions: plant.permissions,
          }))
        }
        return acc
      }, {})
      return { [verticalId]: siteObject }
    })
    console.log('Saved JSON:', JSON.stringify(result, null, 2))
    // Optionally, submit the result to an API.
  }

  // Reset handler.
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

  // Define screen options.
  const screenOptions = data.screens || [
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

  return (
    <Container
      sx={{
        backgroundColor: 'white',
        color: 'black',
        p: 2,
        marginLeft: '10px',
      }}
    >
      {/* Header */}
      <Box sx={{ mb: 2 }}>
        <Typography variant='h5'>User Details</Typography>
      </Box>

      {/* User Details Form */}
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
                {plantSiteData.map((vertical) => (
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

      {/* Vertical-Specific Boxes */}
      {selectedVerticals.map((verticalId) => {
        const vertical = getVerticalById(verticalId)
        if (!vertical) return null

        return (
          <Box key={verticalId} mb={2}>
            <Typography variant='h6' fontWeight='bold' sx={{ mb: 1, ml: 1 }}>
              {vertical.displayName}
            </Typography>
            {verticalSites[verticalId]?.map((siteEntry, siteIndex) => (
              <Box
                key={siteIndex}
                p={2}
                mb={2}
                sx={{ border: '1px solid #eee', borderRadius: '4px' }}
              >
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
                            <MenuItem key={siteOption.id} value={siteOption.id}>
                              <ListItemText primary={siteOption.displayName} />
                            </MenuItem>
                          ),
                        )}
                      </Select>
                    </FormControl>
                  </Grid>

                  {/* Plus Icon to add new site entry */}
                  <Grid item xs={1}>
                    <IconButton
                      onClick={() => addSiteEntry(verticalId)}
                      color='primary'
                      sx={{ marginTop: '15px' }}
                    >
                      <AddIcon />
                    </IconButton>
                  </Grid>

                  {/* Delete Icon for site entry if more than one exists */}
                  {verticalSites[verticalId].length > 1 && (
                    <Grid item xs={1}>
                      <IconButton
                        onClick={() => removeSiteEntry(verticalId, siteIndex)}
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
                      {/* MODIFIED: Show plus icon only on the last plant item; otherwise show delete icon */}
                      <Grid item xs={1}>
                        {plantIndex === siteEntry.plants.length - 1 ? (
                          <IconButton
                            onClick={() => addPlantEntry(verticalId, siteIndex)}
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
                      {/* Screens Dropdown using the new getAvailableScreens helper */}
                      <Grid item xs={4} sm={3}>
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
                            renderValue={(selected) => selected.join(', ')}
                          >
                            {getAvailableScreens(
                              verticalId,
                              siteIndex,
                              plantIndex,
                            ).map((screen) => (
                              <MenuItem key={screen} value={screen}>
                                <Checkbox
                                  checked={(plantEntry.screens || []).includes(
                                    screen,
                                  )}
                                />
                                <ListItemText primary={screen} />
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      </Grid>
                      {/* Permission Checkboxes */}
                      <Grid item xs={4} sm={3}>
                        <FormGroup row>
                          {['read', 'write', 'approve'].map((flag) => (
                            <FormControlLabel
                              key={flag}
                              control={
                                <Checkbox
                                  checked={plantEntry.permissions[flag]}
                                  onChange={(e) =>
                                    handlePlantChange(
                                      verticalId,
                                      siteIndex,
                                      plantIndex,
                                      flag,
                                      e.target.checked,
                                    )
                                  }
                                />
                              }
                              label={
                                flag.charAt(0).toUpperCase() + flag.slice(1)
                              }
                            />
                          ))}
                        </FormGroup>
                      </Grid>
                    </Grid>
                  ))}
                </Box>
              </Box>
            ))}
          </Box>
        )
      })}

      {/* Save and Reset Buttons */}
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
          <Button variant='outlined' color='secondary' onClick={handleReset}>
            Reset
          </Button>
        </Box>
      </Box>
    </Container>
  )
}

export default UserAccessForm
