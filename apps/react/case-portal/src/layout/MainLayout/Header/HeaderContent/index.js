import {
  Box,
  useMediaQuery,
  Select,
  MenuItem,
  FormControl,
  Stack,
} from '@mui/material'
import MobileSection from './MobileSection'
import Profile from './Profile'
import Search from './Search'
import { useState, useEffect } from 'react'
import { DataService } from 'services/DataService'
import { Typography } from '../../../../../node_modules/@mui/material/index'
import { setSitePlantChange } from 'store/reducers/menu' // Import the action
import { useDispatch } from 'react-redux'
import siteData from '../../../../assets/SitesData.json'

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
  const dispatch = useDispatch()

  useEffect(() => {
    localStorage.setItem('year', '2024-2025')
    getPlantAndSite()
  }, [])

  const getPlantAndSite = async () => {
    try {
      // Assuming DataService.getAllSites returns the nested JSON structure
      const response = siteData
      // const response = await DataService.getAllSites(keycloak)
      if (response && response.verticals) {
        // Set verticals directly
        setVerticals(response.verticals)

        // Flatten sites and plants from verticals
        const sitesData = []
        const plantsData = []
        response.verticals.forEach((vertical) => {
          if (vertical.sites && vertical.sites.length) {
            vertical.sites.forEach((site) => {
              // Add vertical info to site
              const siteWithVertical = {
                ...site,
                verticalId: vertical.id,
                verticalName: vertical.name,
              }
              sitesData.push(siteWithVertical)
              if (site.plants && site.plants.length) {
                site.plants.forEach((plant) => {
                  // Attach site and vertical info to plant
                  plantsData.push({
                    ...plant,
                    siteId: site.id,
                    siteName: site.name,
                    verticalId: vertical.id,
                    verticalName: vertical.name,
                  })
                })
              }
            })
          }
        })

        setSites(sitesData)
        setAllSites(sitesData)
        setPlants(plantsData)
        setAllPlants(plantsData)

        // Set default selections using the first plant in the flattened array
        if (plantsData.length > 0) {
          const defaultPlant = plantsData[0]
          setSelectedOption(defaultPlant.name)
          const defaultSite = sitesData.find(
            (site) => site.id === defaultPlant.siteId,
          )
          if (defaultSite) {
            setSelectedSite(defaultSite.name)
            localStorage.setItem(
              'selectedSite',
              JSON.stringify({ id: defaultSite.id, name: defaultSite.name }),
            )
          }
          const defaultVertical = response.verticals.find(
            (vertical) => vertical.id === defaultPlant.verticalId,
          )
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
      }
    } catch (error) {
      console.error('Error fetching product:', error)
    }
  }

  // Handle site change
  const handleSiteChange = (event) => {
    const selectedSiteName = event.target.value
    setSelectedSite(selectedSiteName)
    const selectedSiteData = sites.find(
      (site) => site.name === selectedSiteName,
    )
    if (selectedSiteData) {
      // Filter plants by matching siteId
      const updatedPlants = allPlants.filter(
        (plant) => plant.siteId === selectedSiteData.id,
      )
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

  // Handle plant (option) change and update site/vertical accordingly.
  const handleOptionChange = (event) => {
    dispatch(setSitePlantChange({ sitePlantChange: true }))
    const selectedPlantName = event.target.value
    setSelectedOption(selectedPlantName)
    const selectedPlantData = allPlants.find(
      (plant) => plant.name === selectedPlantName,
    )
    if (selectedPlantData) {
      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({
          id: selectedPlantData.id,
          name: selectedPlantData.name,
          displayName: selectedPlantData.displayName,
        }),
      )
      // Update site
      const relatedSite = allSites.find(
        (site) => site.id === selectedPlantData.siteId,
      )
      if (relatedSite) {
        setSelectedSite(relatedSite.name)
        localStorage.setItem(
          'selectedSite',
          JSON.stringify({ id: relatedSite.id, name: relatedSite.name }),
        )
      }
      // Update vertical
      const relatedVertical = verticals.find(
        (vertical) => vertical.id === selectedPlantData.verticalId,
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
    }
  }

  const handleVerticalChange = (event) => {
    dispatch(setSitePlantChange({ sitePlantChange: true }))
    const verticalName = event.target.value
    setSelectedVertical(verticalName)
    // Find vertical id from verticals array
    const verticalData = verticals.find((v) => v.name === verticalName)
    if (verticalData) {
      // Filter plants that belong to the selected vertical
      const updatedPlants = allPlants.filter(
        (plant) => plant.verticalId === verticalData.id,
      )
      // Get corresponding sites from these plants
      const siteIds = updatedPlants.map((plant) => plant.siteId)
      const updatedSites = allSites.filter((site) => siteIds.includes(site.id))
      if (updatedPlants.length > 0) {
        setPlants(updatedPlants)
        setSelectedOption(updatedPlants[0]?.name)
        setSelectedSite(updatedSites[0]?.name || '')
        localStorage.setItem(
          'selectedPlant',
          JSON.stringify({
            id: updatedPlants[0].id,
            name: updatedPlants[0].name,
            displayName: updatedPlants[0].displayName,
          }),
        )
        localStorage.setItem(
          'selectedSite',
          JSON.stringify({
            id: updatedSites[0].id,
            name: updatedSites[0].name,
          }),
        )
      } else {
        setPlants([])
        setSelectedOption('')
      }
    }
  }

  // Update sites when plant (selected option) changes
  useEffect(() => {
    if (!selectedOption) return
    const selectedPlantData = allPlants.find(
      (plant) => plant.name === selectedOption,
    )
    if (!selectedPlantData) return
    const relatedSite = allSites.find(
      (site) => site.id === selectedPlantData.siteId,
    )
    if (relatedSite) {
      setSelectedSite(relatedSite.name)
      setSites([relatedSite])
      localStorage.setItem(
        'selectedSite',
        JSON.stringify({ id: relatedSite.id, name: relatedSite.name }),
      )
    } else {
      setSites([])
      setSelectedSite('')
    }
  }, [selectedOption, allPlants, allSites])

  // console.log(plants, verticals, sites)

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
