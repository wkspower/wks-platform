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
import sitesData from '../../../../assets/SitesData.json' // Adjust the import path
import { DataService } from 'services/DataService'
import { Typography } from '../../../../../node_modules/@mui/material/index'

const HeaderContent = ({ keycloak }) => {
  const matchesXs = useMediaQuery((theme) => theme.breakpoints.down('md'))
  const [selectedOption, setSelectedOption] = useState('')
  const [selectedSite, setSelectedSite] = useState('')
  const [sites, setSites] = useState([])
  const [plants, setPlants] = useState([])
  const [userSiteToPlants, setUserSiteToPlants] = useState([])

  useEffect(() => {
    getPlantAndSite()
  }, [])

  useEffect(() => {
    if (sites.length > 0 && selectedSite) {
      const site = sites.find((s) => s.name === selectedSite)
      // if (site) {
      //   setPlants(site.plants)
      //   setSelectedOption(site.plants[0]?.name || '')

      //   localStorage.setItem(
      //     'selectedSite',
      //     JSON.stringify({
      //       id: site.id,
      //       name: site.name,
      //     }),
      //   )

      //   if (site.plants.length > 0) {
      //     localStorage.setItem(
      //       'selectedPlant',
      //       JSON.stringify({
      //         id: site.plants[0].id,
      //         name: site.plants[0].name,
      //         displayName: site.plants[0].displayName,
      //       }),
      //     )
      //   }
      // }

      if (site) {
        const userPlantIds= userSiteToPlants[site.id];
  
        if(userPlantIds){
          const sitePlants = site.plants;
          const userPlants = sitePlants.filter((plant)=> userPlantIds[0].includes(plant.id));
  
          setPlants(userPlants)
          setSelectedOption(userPlants[0]?.name) // Default to first plant
        }else{
          setPlants(site.plants)
          setSelectedOption(site.plants[0]?.name) // Default to first plant
        }
      }
    }
  }, [sites, selectedSite])

  useEffect(() => {
    // Update plant options based on selected site
    const site = sites.find((s) => s.name === selectedSite)
    if (site) {
      setPlants(site.plants)
      setSelectedOption(site.plants[0]?.name) // Default to first plant
    }
  }, [selectedSite, sites])

  const getPlantAndSite = async () => {
    try {
      const sitesData = await DataService.getAllSites(keycloak)

      const sitePlantMap = sitesData.reduce((acc, site) => {
        acc[site.id] = site.plants.map(plant => plant.id);
        return acc;
      }, {});
      
      console.log(JSON.stringify(sitePlantMap));
      
      // setSites(data)
      // setSelectedSite(data[0]?.name)

      if(keycloak.idTokenParsed.plants){
        const siteToPlants = {};
  
        const data = JSON.parse(keycloak.idTokenParsed.plants);
        // const data = keycloak.idTokenParsed.plants;
  
        data.forEach(obj => {
          Object.entries(obj).forEach(([site, plant]) => {
              if (!siteToPlants[site]) {
                  siteToPlants[site] = [];
              }
              // Ensure plant is stored as an array
              if (!siteToPlants[site].includes(plant)) {
                  siteToPlants[site].push(plant);
              }
          });
        });
  
        if(siteToPlants){
          setUserSiteToPlants(siteToPlants);
          const sitesIds = Object.keys(siteToPlants);
          const userSitesIds = sitesIds.map(id => id.toLowerCase())

          const userSites = sitesData.filter((site)=> userSitesIds.includes(site.id?.toLowerCase()));

          if(userSites){
            setSites(userSites)
            setSelectedSite(userSites[0]?.name) // Default to first site
          }
        }
      }else{
        setSites(sitesData) // Setting the entire site data
        setSelectedSite(sitesData[0]?.name) // Default to first site
      }
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const handleSiteChange = (event) => {
    const selectedSiteName = event.target.value
    setSelectedSite(selectedSiteName)

    const selectedSiteData = sites.find(
      (site) => site.name === selectedSiteName,
    )

    if (selectedSiteData) {
      setPlants(selectedSiteData.plants)
      setSelectedOption(selectedSiteData.plants[0]?.name || '') // Default to first plant

      // Update local storage
      localStorage.setItem(
        'selectedSite',
        JSON.stringify({
          id: selectedSiteData.id, // Assuming site has an 'id' field
          name: selectedSiteData.name,
        }),
      )
    }
  }

  const handleOptionChange = (event) => {
    const selectedPlantName = event.target.value
    setSelectedOption(selectedPlantName)

    const selectedPlantData = plants.find(
      (plant) => plant.name === selectedPlantName,
    )

    if (selectedPlantData) {
      // Update local storage
      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({
          id: selectedPlantData.id, // Assuming plant has an 'id' field
          name: selectedPlantData.name,
          displayName: selectedPlantData.displayName,
        }),
      )
    }
  }

  return (
    <>
      {!matchesXs && <Search />}
      {matchesXs && <Box sx={{ width: '100%', ml: 1 }} />}

      {/* Horizontal layout for Plant & Site */}
      <Stack direction='row' spacing={2} alignItems='center'>
        {/* Site Dropdown */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' color='white'>
            Site:
          </Typography>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedSite}
              onChange={handleSiteChange}
              sx={{ color: 'white' }}
              disabled={sites.length <= 1} // Disable if only 1 site
            >
              {sites.map((site, index) => (
                <MenuItem key={index} value={site.name}>
                  {site.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        {/* Plant Dropdown */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' color='white'>
            Plant:
          </Typography>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedOption}
              onChange={handleOptionChange}
              sx={{ color: 'white' }}
              disabled={plants.length <= 1} // Disable if only 1 plant
            >
              {plants.map((plant, index) => (
                <MenuItem key={index} value={plant.name}>
                  {plant.displayName}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      </Stack>

      {Config.NovuEnabled ? (
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
      )}

      {!matchesXs && <Profile keycloak={keycloak} />}
      {matchesXs && <MobileSection />}
    </>
  )
}

export default HeaderContent
