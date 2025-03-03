import {
  Box,
  useMediaQuery,
  Select,
  MenuItem,
  FormControl,
  Stack,
} from '@mui/material'
import {
  NotificationBell,
  NovuProvider,
  PopoverNotificationCenter,
} from '@novu/notification-center'
import MobileSection from './MobileSection'
import Profile from './Profile'
import Search from './Search'
import Config from 'consts/index'
import Notification from './Notification'
import { useState, useEffect } from 'react'
import { DataService } from 'services/DataService'
import { Typography } from '../../../../../node_modules/@mui/material/index'
import { setSitePlantChange } from 'store/reducers/menu' // Import the action
import { useDispatch } from 'react-redux'

const HeaderContent = ({ keycloak }) => {
  const matchesXs = useMediaQuery((theme) => theme.breakpoints.down('md'))
  const [selectedOption, setSelectedOption] = useState('')
  const [selectedSite, setSelectedSite] = useState('')
  const [selectedVertical, setSelectedVertical] = useState('')
  const [sites, setSites] = useState([])
  const [allSites, setAllSites] = useState([])
  const [plants, setPlants] = useState([])
  const [allPlants, setAllPlants] = useState([])
  const [verticals, setVerticals] = useState([])
  const [userSiteToPlants, setUserSiteToPlants] = useState([])
  const dispatch = useDispatch() // Initialize Redux dispatch

  useEffect(() => {
    localStorage.setItem('year', '2024-2025')
    getPlantAndSite()
  }, [])

  const getPlantAndSite = async () => {
    try {
      // Assuming DataService.getAllSites returns an object with { sites, plants, verticals }
      const response = await DataService.getAllSites(keycloak)
      // Set plants, sites, and verticals from the response
      setPlants(response.plants)
      setAllPlants(response.plants)
      setSites(response.sites)
      setAllSites(response.sites)
      setVerticals(response.verticals)

      // Set default selections using the first plant in the list
      if (response?.plants && response.plants?.length > 0) {
        const defaultPlant = response.plants[0]
        setSelectedOption(defaultPlant.name)
        const defaultSite = response.sites.find(
          (site) => site.id === defaultPlant.siteFkId,
        )
        if (defaultSite) {
          setSelectedSite(defaultSite.name)
          localStorage.setItem(
            'selectedSite',
            JSON.stringify({ id: defaultSite.id, name: defaultSite.name }),
          )
        }
        const defaultVertical = response.verticals.find(
          (vertical) => vertical.id === defaultPlant.verticalFKId,
        )
        console.log(defaultVertical)
        if (defaultVertical) {
          setSelectedVertical(defaultVertical.name)
          localStorage.setItem(
            'selectedVertical',
            JSON.stringify({
              id: defaultVertical.id,
              name: defaultVertical.name,
            }),
          )
        }
        localStorage.setItem(
          'selectedPlant',
          JSON.stringify({
            id: defaultPlant.id,
            name: defaultPlant.name,
            displayName: defaultPlant.displayName,
          }),
        )
      }
    } catch (error) {
      console.error('Error fetching product:', error)
    }
  }

  // Handle site change (if user manually changes site)
  const handleSiteChange = (event) => {
    // dispatch(setSitePlantChange({ sitePlantChange: true }))
    const selectedSiteName = event.target.value
    setSelectedSite(selectedSiteName)

    const selectedSiteData = sites.find(
      (site) => site.name === selectedSiteName,
    )
    if (selectedSiteData) {
      // Optionally, update plants based on the selected site
      const updatedPlants = plants.filter(
        (plant) => plant.siteFkId === selectedSiteData.id,
      )
      // If you want to restrict plant choices to those of the site:
      setPlants(updatedPlants)
      setSelectedOption(updatedPlants[0]?.name || '')
      localStorage.setItem(
        'selectedSite',
        JSON.stringify({
          id: selectedSiteData.id,
          name: selectedSiteData.name,
        }),
      )
    }
  }

  // Handle plant change: update selected plant AND update site and vertical accordingly.
  const handleOptionChange = (event) => {
    dispatch(setSitePlantChange({ sitePlantChange: true }))
    const selectedPlantName = event.target.value
    setSelectedOption(selectedPlantName)

    // Find the selected plant in the plants array
    const selectedPlantData = plants.find(
      (plant) => plant.name === selectedPlantName,
    )
    if (selectedPlantData) {
      // Update local storage for the plant
      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({
          id: selectedPlantData.id,
          name: selectedPlantData.name,
          displayName: selectedPlantData.displayName,
        }),
      )

      // Update site based on the selected plant's siteFkId
      const relatedSite = allSites.find(
        (site) => site.id === selectedPlantData.siteFkId,
      )
      if (relatedSite) {
        setSelectedSite(relatedSite.name)
        setSites(relatedSite)
        localStorage.setItem(
          'selectedSite',
          JSON.stringify({ id: relatedSite.id, name: relatedSite.name }),
        )
      }

      // Update vertical based on the selected plant's verticalFKId
      const relatedVertical = verticals.find(
        (vertical) => vertical.id === selectedPlantData.verticalFKId,
      )
      if (relatedVertical) {
        setSelectedVertical(relatedVertical.name)
        localStorage.setItem(
          'selectedVertical',
          JSON.stringify({
            id: relatedVertical.id,
            name: relatedVertical.name,
          }),
        )
      }

      const checkVertical = verticals.find(
        (vertical) => vertical.name === selectedVertical,
      )
      if (checkVertical) {
        console.log(checkVertical)
      }
    }
  }

  const handleVerticalChange = (event) => {
    dispatch(setSitePlantChange({ sitePlantChange: true }))

    const verticalName = event.target.value
    setSelectedVertical(verticalName)
    console.log(verticalName)
    // Update plants based on the selected vertical
    const updatedPlants = allPlants.filter(
      (plant) =>
        plant.verticalFKId ===
        verticals.find((v) => v.name === verticalName)?.id,
    )
    console.log(updatedPlants)

    // const showSite = allSites.find(
    //   (site) => site.siteFkId === updatedPlants?.siteFkId,
    // )
    const siteIds = updatedPlants.map((plant) => plant.siteFkId)
    const showSite = allSites.filter((site) => siteIds.includes(site.id))
    console.log(showSite)
    // console.log(showSite)

    if (updatedPlants?.length > 0) {
      setSelectedOption(updatedPlants[0]?.name)
      setPlants(updatedPlants)
      setSelectedSite(showSite?.displayName || showSite[0]?.displayName)
      setSites(showSite)
      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({
          id: updatedPlants[0].id,
          name: updatedPlants[0].name,
          displayName: updatedPlants[0].displayName,
        }),
      )
    }

    //available sites only
  }
  // Update plants when vertical changes
  useEffect(() => {
    if (!selectedVertical) return // Avoid unnecessary execution

    // Find Vertical ID
    const verticalData = verticals.find((v) => v.name === selectedVertical)
    if (!verticalData) return

    // Filter plants belonging to selected vertical
    const updatedPlants = allPlants.filter(
      (plant) => plant.verticalFKId === verticalData.id,
    )

    if (updatedPlants.length > 0) {
      setPlants(updatedPlants)
      setSelectedOption(updatedPlants[0].name) // Select the first plant

      // Store selected plant in localStorage
      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({
          id: updatedPlants[0].id,
          name: updatedPlants[0].name,
          displayName: updatedPlants[0].displayName,
        }),
      )
    } else {
      setPlants([])
      setSelectedOption('') // Reset selection
    }
  }, [selectedVertical, allPlants])

  // Update sites when plant changes
  useEffect(() => {
    if (!selectedOption) return // Avoid unnecessary execution

    // Find the selected plant object
    const selectedPlantData = plants.find(
      (plant) => plant.name === selectedOption,
    )
    if (!selectedPlantData) return

    console.log(selectedPlantData)
    // Filter sites based on selected plant's siteFkId
    const relatedSite = allSites.find(
      (site) => site.id === selectedPlantData.siteFkId,
    )
    console.log(relatedSite)
    if (relatedSite) {
      setSelectedSite(relatedSite.name)
      setSites([relatedSite]) // Only show the related site

      localStorage.setItem(
        'selectedSite',
        JSON.stringify({
          id: relatedSite.id,
          name: relatedSite.name,
        }),
      )
    } else {
      setSites([])
      setSelectedSite('') // Reset selection
    }
  }, [selectedOption, plants, allSites])

  console.log(plants, verticals, sites)
  return (
    <>
      {matchesXs && <Search />}
      {!matchesXs && <Box sx={{ width: '100%', ml: 1 }} />}

      {/* Horizontal layout for Plant & Site */}
      <Stack direction='row' spacing={2} alignItems='center'>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' color='white'>
            Vertical:
          </Typography>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedVertical}
              onChange={handleVerticalChange}
              sx={{ color: 'white' }}
            >
              {verticals?.map((vertical) => (
                <MenuItem key={vertical.id} value={vertical.name}>
                  {vertical?.displayName}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' color='white'>
            Site:
          </Typography>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedSite}
              onChange={handleSiteChange}
              sx={{ color: 'white' }}
              disabled={sites?.length <= 1} // Disable if only 1 site
            >
              {Array.isArray(sites) &&
                sites.map((site, index) => (
                  <MenuItem key={index} value={site.name}>
                    {site?.name}
                  </MenuItem>
                ))}
            </Select>
          </FormControl>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' color='white'>
            Plant:
          </Typography>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedOption}
              onChange={handleOptionChange}
              sx={{ color: 'white' }}
              disabled={plants?.length <= 1} // Disable if only 1 plant
            >
              {plants?.map((plant, index) => (
                <MenuItem key={index} value={plant.name}>
                  {plant?.displayName}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      </Stack>

      {/* {Config.NovuEnabled ? (
        <NovuProvider
          subscriberId={keycloak.idTokenParsed.email}
          applicationIdentifier={Config.NovuAppId}
        >
          <PopoverNotificationCenter colorScheme={'light'}>
            {({ unseenCount }) => (
              <NotificationBell unseenCount={unseenCount} />
            )}
          </PopoverNotificationCenter>
        </NovuProvider>
      ) : (
        <Notification />
      )} */}

      {!matchesXs && <Profile keycloak={keycloak} />}
      {matchesXs && <MobileSection />}
    </>
  )
}

export default HeaderContent
