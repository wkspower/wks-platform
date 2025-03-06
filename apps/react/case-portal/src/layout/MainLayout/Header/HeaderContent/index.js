import React, { useState, useEffect } from 'react'
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
import { setSitePlantChange } from 'store/reducers/menu'
import siteData from '../../../../assets/SitesData.json'
import { DataService } from 'services/DataService'

const HeaderContent = ({ keycloak }) => {
  const matchesXs = useMediaQuery((theme) => theme.breakpoints.down('md'))
  const [selectedPlant, setSelectedPlant] = useState('')
  const [selectedSite, setSelectedSite] = useState('')
  const [selectedVertical, setSelectedVertical] = useState('')
  const [verticals, setVerticals] = useState([])
  const [sites, setSites] = useState([])
  const [allSites, setAllSites] = useState([])
  const [plants, setPlants] = useState([])
  const [allPlants, setAllPlants] = useState([])
  const dispatch = useDispatch()

  useEffect(() => {
    localStorage.setItem('year', '2025-26')
    getPlantAndSite()
  }, [])

  const getPlantAndSite = async () => {
    try {
      const response = await DataService.getAllSites(keycloak)
      // const response = siteData
      // console.log(response)
      if (response && response) {
        setVerticals(response)

        // Flatten verticals into sites and plants arrays.
        const sitesData = []
        const plantsData = []
        response.forEach((vertical) => {
          if (vertical.sites && vertical.sites.length) {
            vertical.sites.forEach((site) => {
              const siteWithVertical = {
                name: site.name,
                verticalName: vertical.name,
              }
              sitesData.push(siteWithVertical)
              if (site.plants && site.plants.length) {
                site.plants.forEach((plant) => {
                  plantsData.push({
                    id: plant.id,
                    name: plant.name,
                    siteName: site.name,
                    verticalName: vertical.name,
                  })
                })
              }
            })
          }
        })

        // console.log('All Sites Data:', sitesData)
        setAllSites(sitesData)
        setAllPlants(plantsData)
        // setPlants(plantsData)
        // Remove: setSites(sitesData)

        // Set default selections based on the first available plant.
        if (plantsData.length > 0) {
          const defaultPlant = plantsData[0]
          setSelectedPlant(defaultPlant.name)
          // setSelectedSite(defaultPlant.siteName)
          setSelectedVertical(defaultPlant.verticalName)
          // console.log(response)
          // Immediately filter sites based on the default vertical.
          const defaultVerticalData = response.find(
            (v) => v.name === defaultPlant.verticalName,
          )
          const siteAvailable = defaultVerticalData
            ? defaultVerticalData.sites.map((m) => m.name)
            : []
          setSites(siteAvailable)
          setSelectedSite(siteAvailable[0] || '')

          // Optionally, update the plants to reflect only those from the first site.
          const filteredPlants = plantsData.filter(
            (plant) =>
              plant.siteName === (siteAvailable[0] || '') &&
              plant.verticalName === defaultPlant.verticalName,
          )
          setPlants(filteredPlants)

          localStorage.setItem(
            'selectedPlant',
            JSON.stringify({ id: defaultPlant.id, name: defaultPlant.name }),
          )
          localStorage.setItem(
            'selectedSite',
            JSON.stringify({ name: defaultPlant.siteName }),
          )
          localStorage.setItem(
            'selectedVertical',
            JSON.stringify({ name: defaultPlant.verticalName }),
          )
        }
      }
    } catch (error) {
      console.error('Error fetching plant and site data:', error)
    }
  }

  const handleSiteChange = (event) => {
    const siteName = event.target.value
    setSelectedSite(siteName)
    const selectedSiteData = sites.find((site) => site === siteName)
    const filteredPlants = allPlants.filter(
      (plant) =>
        plant.siteName === siteName && plant.verticalName === selectedVertical,
    )
    if (selectedSiteData) {
      setPlants(filteredPlants)
      setSelectedPlant(filteredPlants[0]?.name || '')
      localStorage.setItem('selectedSite', JSON.stringify({ name: siteName }))
    }
  }

  const handlePlantChange = (event) => {
    dispatch(setSitePlantChange({ sitePlantChange: true }))
    const plantName = event.target.value
    setSelectedPlant(plantName)
    const selectedPlantData = allPlants.find(
      (plant) => plant.name === plantName,
    )
    if (selectedPlantData) {
      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({
          id: selectedPlantData.id,
          name: selectedPlantData.name,
        }),
      )
      // setSelectedSite(selectedPlantData.siteName)
      // localStorage.setItem(
      //   'selectedSite',
      //   JSON.stringify({ name: selectedPlantData.siteName }),
      // )
      // setSelectedVertical(selectedPlantData.verticalName)
      // localStorage.setItem(
      //   'selectedVertical',
      //   JSON.stringify({ name: selectedPlantData.verticalName }),
      // )
    }
  }

  const handleVerticalChange = (event) => {
    dispatch(setSitePlantChange({ sitePlantChange: true }))
    const verticalName = event.target.value
    setSelectedVertical(verticalName)
    const verticalData = verticals.find((v) => v.name === verticalName)
    if (verticalData) {
      // const updatedPlants = allPlants.filter(
      //   (plant) => plant.verticalName === verticalName,
      // )
      // Filter sites for this vertical.
      const siteAvailable = verticalData.sites.map((m) => m.name)
      setSites(siteAvailable)
      setSelectedSite(siteAvailable[0] || '')
      const filteredPlants = allPlants.filter(
        (plant) =>
          plant.siteName === siteAvailable[0] &&
          plant.verticalName === verticalName,
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

  // Keep this effect if you want to sync selected site when plant changes.
  useEffect(() => {
    if (!selectedPlant || !allPlants) return
    const selectedPlantData = allPlants.find(
      (plant) => plant.name === selectedPlant,
    )
    if (!selectedPlantData) return
    setSelectedSite(selectedPlantData.siteName)
    localStorage.setItem(
      'selectedSite',
      JSON.stringify({ name: selectedPlantData.siteName }),
    )
  }, [selectedPlant, allPlants])

  // This effect updates sites whenever verticals or selectedVertical changes.
  useEffect(() => {
    if (verticals.length === 0 || !selectedVertical) return
    const selectedVerticalData = verticals.find(
      (v) => v.name === selectedVertical,
    )
    const siteAvailable = selectedVerticalData
      ? selectedVerticalData.sites.map((m) => m.name)
      : []
    setSites(siteAvailable)
    setSelectedSite(siteAvailable[0] || '')
  }, [verticals, selectedVertical])

  return (
    <>
      {matchesXs && <Search />}
      {!matchesXs && <Box sx={{ width: '100%', ml: 1 }} />}

      <Stack direction='row' spacing={2} alignItems='center'>
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
              {verticals.map((vertical, index) => (
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
              disabled={sites.length <= 1}
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
              disabled={plants.length <= 1}
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
