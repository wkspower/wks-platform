import { useEffect, useState } from 'react'
import {
  Container,
  Box,
  Typography,
  TextField,
  FormControl,
  Select,
  MenuItem,
  Checkbox,
  ListItemText,
  IconButton,
  Divider,
  Grid,
  Button,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'
import { DataService } from 'services/DataService'
import { useLocation } from 'react-router-dom'

const UserAccessForm = ({ keycloak }) => {
  const location = useLocation()
  const data = location.state || {}

  // User Details State – will be prepopulated if data comes from navigation
  const [userDetails, setUserDetails] = useState({
    username: '',
    firstname: '',
    lastname: '',
    role: '',
    email: '',
  })

  // Plant Site Data from API
  const [plantSiteData, setPlantSiteData] = useState([])

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

  // Helper: Create initial verticalSites object from API data (default structure)
  const getInitialVerticalSites = (plantData) => {
    return plantData?.reduce((acc, vertical) => {
      acc[vertical.id] = [{ site: '', plant: [] }]
      return acc
    }, {})
  }

  // States for vertical selections and vertical-specific site/plant selections.
  const [selectedVerticals, setSelectedVerticals] = useState([])
  const [verticalSites, setVerticalSites] = useState({})

  // When plantSiteData loads, initialize the verticalSites if not prepopulated.
  useEffect(() => {
    if (plantSiteData.length > 0) {
      // Only set defaults if no navigation data has been processed.
      if (selectedVerticals.length === 0) {
        setSelectedVerticals([plantSiteData[0].id])
      }
      setVerticalSites(getInitialVerticalSites(plantSiteData))
    }
  }, [plantSiteData])

  // *** NEW EFFECT: Prepopulate form if navigation data is available ***
  useEffect(() => {
    if (plantSiteData.length > 0 && data && data.verticals) {
      // Set user details from navigation data
      setUserDetails({
        username: data.username || '',
        firstname: data.firstName || '',
        lastname: data.lastName || '',
        role: data.role || '',
        email: data.email || '',
      })

      // Build vertical selections from navigation data.
      // Assume data.verticals is an array of vertical names.
      const newSelectedVerticals = plantSiteData
        .filter(
          (vertical) =>
            data.verticals.includes(vertical.displayName) ||
            data.verticals.includes(vertical.name),
        )
        .map((vertical) => vertical.id)

      // Build verticalSites state:
      // For each selected vertical, look through its sites and pick the ones where the site name
      // appears in data.sites. For each such site, also filter its plants based on data.plants.
      const newVerticalSites = {}
      newSelectedVerticals.forEach((verticalId) => {
        // Find the corresponding vertical from API data.
        const vertical = plantSiteData.find((v) => v.id === verticalId)
        if (!vertical) return
        // For this vertical, create an array of site entries.
        const siteEntries = []
        vertical.sites.forEach((site) => {
          // Check if site's name is included in navigation sites.
          if (
            data.sites &&
            (data.sites.includes(site.displayName) ||
              data.sites.includes(site.name))
          ) {
            // For this site, determine selected plants.
            const selectedPlants = site.plants
              .filter(
                (plant) =>
                  data.plants &&
                  (data.plants.includes(plant.displayName) ||
                    data.plants.includes(plant.name)),
              )
              .map((plant) => plant.id)
            siteEntries.push({
              site: site.id,
              plant: selectedPlants,
            })
          }
        })
        // If at least one matching site found, assign it; otherwise use a default entry.
        newVerticalSites[verticalId] = siteEntries.length
          ? siteEntries
          : [{ site: '', plant: [] }]
      })

      setSelectedVerticals(newSelectedVerticals)
      setVerticalSites(newVerticalSites)
    }
  }, [plantSiteData, data])

  // Helper: Retrieve vertical object by id from API data.
  const getVerticalById = (id) =>
    plantSiteData?.find((vertical) => vertical.id === id)

  // Handlers for user detail changes.
  const handleUserDetailChange = (e) => {
    setUserDetails({ ...userDetails, [e.target.name]: e.target.value })
  }

  // Handler for vertical multi-select change.
  const handleVerticalChange = (e) => {
    const { value } = e.target
    const newValue = typeof value === 'string' ? value.split(',') : value
    setSelectedVerticals(newValue)
    // Update verticalSites for new vertical selections.
    setVerticalSites((prev) => {
      const updated = { ...prev }
      newValue.forEach((verticalId) => {
        if (!updated[verticalId]) {
          updated[verticalId] = [{ site: '', plant: [] }]
        }
      })
      return updated
    })
  }

  // Handler for updating a site or plant for a vertical.
  const handleSiteChange = (verticalId, index, field, newValue) => {
    setVerticalSites((prev) => {
      const updatedList = [...prev[verticalId]]
      updatedList[index] = {
        ...updatedList[index],
        [field]: newValue,
      }
      return { ...prev, [verticalId]: updatedList }
    })
  }

  // Returns available sites for a vertical excluding ones already selected (except for current index).
  const getAvailableSites = (verticalId, currentIndex) => {
    const vertical = getVerticalById(verticalId)
    if (!vertical) return []
    const selectedSiteIds = verticalSites[verticalId]
      .filter((_, idx) => idx !== currentIndex)
      .map((entry) => entry.site)
      .filter((site) => site)
    return vertical.sites.filter((site) => !selectedSiteIds.includes(site.id))
  }

  // Add new site entry for a given vertical.
  const addSiteEntry = (verticalId) => {
    setVerticalSites((prev) => ({
      ...prev,
      [verticalId]: [...prev[verticalId], { site: '', plant: [] }],
    }))
  }

  // Remove a site entry for a vertical.
  const removeSiteEntry = (verticalId, index) => {
    setVerticalSites((prev) => {
      const updatedList = [...prev[verticalId]]
      updatedList.splice(index, 1)
      return { ...prev, [verticalId]: updatedList }
    })
  }

  // Build the JSON output using only selected IDs.
  const handleSave = () => {
    // Build a JSON array where each object has the vertical (parent) id as key,
    // its value is an object with site (sub-parent) ids as keys and an array of plant (child) ids as value.
    const result = selectedVerticals.map((verticalId) => {
      const siteData = verticalSites[verticalId] || []
      const siteObject = siteData.reduce((acc, entry) => {
        if (entry.site) {
          acc[entry.site] = entry.plant
        }
        return acc
      }, {})
      return { [verticalId]: siteObject }
    })
    console.log('Saved JSON:', JSON.stringify(result, null, 2))
    // Optionally, send the result to your API.
  }

  // Handle Reset: Clear all fields to defaults.
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
      {/* Header */}
      <Box sx={{ mb: 2 }}>
        <Typography variant='h5'>User Details</Typography>
      </Box>

      {/* User Details Form */}
      <Box p={3}>
        <Grid container spacing={2} alignItems='center'>
          <Grid item xs={12} sm={4}>
            <Typography variant='h6' gutterBottom>
              First Name
            </Typography>
            <TextField
              fullWidth
              name='firstname'
              value={data?.firstName || userDetails.firstname}
              onChange={handleUserDetailChange}
              variant='outlined'
              size='small'
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <Typography variant='h6' gutterBottom>
              Last Name
            </Typography>
            <TextField
              fullWidth
              name='lastname'
              value={data?.lastName || userDetails.lastname}
              onChange={handleUserDetailChange}
              variant='outlined'
              size='small'
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <Typography variant='h6' gutterBottom>
              Username
            </Typography>
            <TextField
              fullWidth
              name='username'
              value={data?.username || userDetails.username}
              onChange={handleUserDetailChange}
              variant='outlined'
              size='small'
            />
          </Grid>

          <Grid item xs={12} sm={4}>
            <Typography variant='h6' gutterBottom>
              Email
            </Typography>
            <TextField
              fullWidth
              name='email'
              value={data?.email || userDetails.email}
              onChange={handleUserDetailChange}
              variant='outlined'
              size='small'
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <Typography variant='h6' gutterBottom>
              Role
            </Typography>
            <TextField
              fullWidth
              name='role'
              value={data?.role || userDetails.role}
              onChange={handleUserDetailChange}
              variant='outlined'
              size='small'
            />
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
            <Box p={2}>
              {verticalSites[verticalId]?.map((entry, index) => (
                <Grid
                  container
                  spacing={2}
                  alignItems='center'
                  key={index}
                  mt={0.5}
                >
                  <Grid item xs={3} sm={2}>
                    <Typography variant='h6' gutterBottom>
                      Site
                    </Typography>
                    <FormControl fullWidth size='small'>
                      <Select
                        value={entry.site}
                        sx={{ height: '40px' }}
                        onChange={(e) =>
                          handleSiteChange(
                            verticalId,
                            index,
                            'site',
                            e.target.value,
                          )
                        }
                      >
                        {getAvailableSites(verticalId, index).map((site) => (
                          <MenuItem key={site.id} value={site.id}>
                            <ListItemText primary={site.displayName} />
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid item xs={3} sm={4}>
                    <Typography variant='h6' gutterBottom>
                      Plants
                    </Typography>
                    <FormControl fullWidth size='small'>
                      <Select
                        multiple
                        value={entry.plant}
                        onChange={(e) =>
                          handleSiteChange(
                            verticalId,
                            index,
                            'plant',
                            e.target.value,
                          )
                        }
                        renderValue={(selected) => {
                          if (!entry.site) return ''
                          const siteObj = vertical.sites.find(
                            (s) => s.id === entry.site,
                          )
                          if (!siteObj) return ''
                          const selectedPlants = siteObj.plants.filter((p) =>
                            selected.includes(p.id),
                          )
                          return selectedPlants
                            .map((p) => p.displayName)
                            .join(', ')
                        }}
                        sx={{ height: '40px' }}
                      >
                        {entry.site &&
                          (() => {
                            const siteObj = vertical.sites.find(
                              (s) => s.id === entry.site,
                            )
                            if (!siteObj) return null
                            return siteObj.plants.map((plant) => (
                              <MenuItem key={plant.id} value={plant.id}>
                                <Checkbox
                                  checked={entry.plant.includes(plant.id)}
                                />
                                <ListItemText primary={plant.displayName} />
                              </MenuItem>
                            ))
                          })()}
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid item xs={1}>
                    {index === 0 ? (
                      <IconButton
                        onClick={() => addSiteEntry(verticalId)}
                        color='primary'
                        sx={{ top: '15px' }}
                      >
                        <AddIcon />
                      </IconButton>
                    ) : (
                      <IconButton
                        onClick={() => removeSiteEntry(verticalId, index)}
                        color='secondary'
                        sx={{ top: '15px' }}
                      >
                        <DeleteIcon />
                      </IconButton>
                    )}
                  </Grid>
                </Grid>
              ))}
            </Box>
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
