import { useState, useEffect } from 'react'
import {
  Box,
  useMediaQuery,
  Select,
  MenuItem,
  FormControl,
  Stack,
  Typography,
} from '@mui/material'
import MobileSection from './MobileSection'
import Profile from './Profile'
import Search from './Search'
import { useDispatch } from 'react-redux'
import {
  setSitePlantChange,
  setVerticalChange,
} from 'store/reducers/dataGridStore'
import { DataService } from 'services/DataService'
// import siteData from '../../../../assets/sitesData.json'

const HeaderContent = ({ keycloak }) => {
  const matchesXs = useMediaQuery((theme) => theme.breakpoints.down('md'))
  const [selectedPlant, setSelectedPlant] = useState('')
  const [selectedSite, setSelectedSite] = useState('')
  const [selectedVertical, setSelectedVertical] = useState('')
  const [verticals, setVerticals] = useState([])
  const [sites, setSites] = useState([])
  // const [allSites, setAllSites] = useState([])
  // const [userSites, setUserSites] = useState([])
  const [plants, setPlants] = useState([])
  const [allPlants, setAllPlants] = useState([])
  const dispatch = useDispatch()
  const [year, setYear] = useState('')

  // Helper: Extract allowed site IDs and allowed plant IDs from Keycloak token
  const getAllowedFilter = () => {
    try {
      const parsed = JSON.parse(keycloak.idTokenParsed.plants)
      let allowedSiteIds = []
      let allowedPlantIds = []
      parsed.forEach((obj) => {
        Object.keys(obj).forEach((siteId) => {
          allowedSiteIds.push(siteId)
          allowedPlantIds = allowedPlantIds.concat(obj[siteId])
        })
      })
      return { allowedSiteIds, allowedPlantIds }
    } catch (err) {
      console.error('Error parsing keycloak token:', err)
      return { allowedSiteIds: [], allowedPlantIds: [] }
    }
  }

  const getPlantAndSite = async () => {
    try {
      const response = await DataService.getAllSites(keycloak)
      if (response) {
        setVerticals(response)
        // setUserSites(keycloak.idTokenParsed.plants)

        // Flatten verticals into sites and plants arrays.
        const sitesData = []
        const plantsData = []
        response.forEach((vertical) => {
          if (vertical.sites && vertical.sites.length) {
            vertical.sites.forEach((site) => {
              // Include site id for filtering.
              const siteWithVertical = {
                id: site.id,
                name: site.name,
                verticalName: vertical.name,
              }
              sitesData.push(siteWithVertical)
              if (site.plants && site.plants.length) {
                site.plants.forEach((plant) => {
                  //console.log('plant', plant)

                  plantsData.push({
                    id: plant.id,
                    name: plant.name,
                    siteName: site.name,
                    siteId: site.id,
                    verticalId: vertical.id,
                    verticalName: vertical.name,
                  })
                })
              }
            })
          }
        })

        dispatch(
          setVerticalChange({
            selectedPlant,
            selectedSite,
            selectedVertical,
          }),
        )
        // setAllSites(sitesData)
        setAllPlants(plantsData)

        // Get allowed filter arrays.
        const { allowedSiteIds, allowedPlantIds } = getAllowedFilter()

        // Filter plants based on allowed plant IDs.
        const filteredPlantsData = plantsData.filter((plant) =>
          allowedPlantIds.includes(plant.id),
        )

        // Filter sites based on allowed site IDs.
        // const filteredSitesData = sitesData.filter((site) =>
        //   allowedSiteIds.includes(site.id),
        // )

        // Set default selections based on the first available allowed plant.
        if (filteredPlantsData.length > 0) {
          const defaultPlant = filteredPlantsData[0]
          setSelectedPlant(defaultPlant.name)
          setSelectedVertical(defaultPlant.verticalName)

          // Find the vertical data for the default vertical.
          const defaultVerticalData = response.find(
            (v) => v.name === defaultPlant.verticalName,
          )
          // Filter vertical's sites using allowed site IDs.
          const allowedSites = defaultVerticalData
            ? defaultVerticalData.sites.filter((site) =>
                allowedSiteIds.includes(site.id),
              )
            : []
          const siteAvailable = allowedSites.map((site) => site.name)

          setSites(siteAvailable)
          setSelectedSite(siteAvailable[0] || '')

          // Filter plants for the default vertical and first allowed site.
          const finalFilteredPlants = filteredPlantsData.filter(
            (plant) =>
              plant.siteName === (siteAvailable[0] || '') &&
              plant.verticalName === defaultPlant.verticalName,
          )
          setPlants(finalFilteredPlants)

          // console.log('defaultPlant', defaultPlant)
          // console.log('defaultPlant.verticalId', defaultPlant.verticalId)
          localStorage.setItem('verticalId', defaultPlant.verticalId)

          localStorage.setItem(
            'selectedPlant',
            JSON.stringify({ id: defaultPlant.id, name: defaultPlant.name }),
          )
          localStorage.setItem(
            'selectedSite',
            JSON.stringify({
              id: defaultPlant.siteId,
              name: defaultPlant.siteName,
            }),
          )
          localStorage.setItem(
            'selectedVertical',
            JSON.stringify({
              id: defaultPlant.verticalId,
              name: defaultPlant.verticalName,
            }),
          )
        }
      }
    } catch (error) {
      console.error('Error fetching plant and site data:', error)
    }
  }

  useEffect(() => {
    const year = '2025-26'
    localStorage.setItem('year', year)
    setYear(year)
    getPlantAndSite()
  }, [])

  const handleSiteChange = (event) => {
    // dispatch(setSitePlantChange({ sitePlantChange: true }))
    const siteName = event.target.value
    setSelectedSite(siteName)
    const { allowedPlantIds } = getAllowedFilter()
    // Filter plants for the selected site and vertical using allowed plant IDs.
    const filteredPlants = allPlants.filter(
      (plant) =>
        plant.siteName === siteName &&
        plant.verticalName === selectedVertical &&
        allowedPlantIds.includes(plant.id),
    )
    if (filteredPlants.length > 0) {
      setPlants(filteredPlants)
      setSelectedPlant(filteredPlants[0].name)

      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({
          id: filteredPlants[0].id,
          name: filteredPlants[0].name,
        }),
      )

      localStorage.setItem('selectedSite', JSON.stringify({ name: siteName }))
    }
  }

  const handlePlantChange = (event) => {
    dispatch(setSitePlantChange({ sitePlantChange: true }))
    const plantName = event.target.value
    setSelectedPlant(plantName)
    const { allowedPlantIds } = getAllowedFilter()
    const selectedPlantData = allPlants.find(
      (plant) => plant.name === plantName && allowedPlantIds.includes(plant.id),
    )
    if (selectedPlantData) {
      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({
          id: selectedPlantData.id,
          name: selectedPlantData.name,
        }),
      )
    }
  }

  const handleVerticalChange = (event) => {
    // dispatch(setSitePlantChange({ sitePlantChange: true }))
    const verticalName = event.target.value
    setSelectedVertical(verticalName)
    const verticalData = verticals.find((v) => v.name === verticalName)
    if (verticalData) {
      const { allowedSiteIds, allowedPlantIds } = getAllowedFilter()
      // Filter the vertical's sites using allowed site IDs.
      const allowedSites = verticalData.sites.filter((site) =>
        allowedSiteIds.includes(site.id),
      )
      const siteAvailable = allowedSites.map((site) => site.name)
      setSites(siteAvailable)
      setSelectedSite(siteAvailable[0] || '')

      // Filter plants for the selected vertical and first allowed site.
      const filteredPlants = allPlants.filter(
        (plant) =>
          plant.siteName === (siteAvailable[0] || '') &&
          plant.verticalName === verticalName &&
          allowedPlantIds.includes(plant.id),
      )
      if (filteredPlants.length > 0) {
        setPlants(filteredPlants)
        setSelectedPlant(filteredPlants[0].name)
        localStorage.setItem(
          'selectedPlant',
          JSON.stringify({
            id: filteredPlants[0].id,
            name: filteredPlants[0].name,
          }),
        )
        localStorage.setItem(
          'selectedSite',
          JSON.stringify({ name: siteAvailable[0] }),
        )
      } else {
        setPlants([])
        setSelectedPlant('')
      }
    }
  }

  // Sync selected site when plant changes.
  useEffect(() => {
    // console.log('test--->', keycloak.idTokenParsed.plants)
    const { allowedPlantIds } = getAllowedFilter()
    if (!selectedPlant || !allPlants) return
    const selectedPlantData = allPlants.find(
      (plant) =>
        plant.name === selectedPlant && allowedPlantIds.includes(plant.id),
    )
    if (!selectedPlantData) return
    setSelectedSite(selectedPlantData.siteName)
    localStorage.setItem(
      'selectedVertical',
      JSON.stringify({ name: selectedVertical }),
      // JSON.stringify({ name: selectedPlantData.siteName }),
    )
    dispatch(
      setVerticalChange({
        selectedPlant,
        selectedSite,
        selectedVertical,
      }),
    )
  }, [selectedPlant, allPlants])

  // Update sites whenever verticals or selectedVertical changes.
  useEffect(() => {
    if (verticals.length === 0 || !selectedVertical) return
    const verticalData = verticals.find((v) => v.name === selectedVertical)
    if (!verticalData) return
    const { allowedSiteIds } = getAllowedFilter()
    const allowedSites = verticalData.sites.filter((site) =>
      allowedSiteIds.includes(site.id),
    )
    const siteAvailable = allowedSites.map((site) => site.name)
    setSites(siteAvailable)
    setSelectedSite(siteAvailable[0] || '')
  //  console.log(selectedVertical)
    dispatch(
      setVerticalChange({
        selectedPlant,
        selectedSite,
        selectedVertical,
      }),
    )
  }, [verticals, selectedVertical])

  return (
    <>
      {matchesXs && <Search />}
      {!matchesXs && <Box sx={{ width: '100%', ml: 1 }} />}
      <Stack direction='row' spacing={2} alignItems='center'>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography
            variant='body1'
            color='white'
            sx={{ fontWeight: 'bold', whiteSpace: 'nowrap' }}
          >
            Year: {year}
          </Typography>
        </Box>

        {/* Vertical Selector */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' color='white'>
            Vertical:
          </Typography>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedVertical || ''}
              onChange={handleVerticalChange}
              sx={{ color: 'white' }}
            >
              {Array.isArray(verticals) &&
                verticals.map((vertical, index) => (
                  <MenuItem key={index} value={vertical.name}>
                    {vertical.name}
                  </MenuItem>
                ))}
            </Select>
          </FormControl>
        </Box>
        {/* Site Selector */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' color='white'>
            Site:
          </Typography>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedSite || ''}
              onChange={handleSiteChange}
              sx={{ color: 'white' }}
              // disabled={sites.length <= 1}
            >
              {sites.map((site, index) => (
                <MenuItem key={index} value={site}>
                  {site}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
        {/* Plant Selector */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' color='white'>
            Plant:
          </Typography>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedPlant || ''}
              onChange={handlePlantChange}
              sx={{ color: 'white' }}
              // disabled={plants.length <= 1}
            >
              {plants.map((plant, index) => (
                <MenuItem key={index} value={plant.name}>
                  {plant.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      </Stack>
      {!matchesXs && <Profile keycloak={keycloak} />}
      {matchesXs && <MobileSection />}
    </>
  )
}

export default HeaderContent
