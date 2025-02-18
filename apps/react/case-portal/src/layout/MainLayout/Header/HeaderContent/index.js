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

const HeaderContent = ({ keycloak }) => {
  const matchesXs = useMediaQuery((theme) => theme.breakpoints.down('md'))
  const [selectedOption, setSelectedOption] = useState('')
  const [selectedSite, setSelectedSite] = useState('')
  const [sites, setSites] = useState([])
  const [plants, setPlants] = useState([])

  useEffect(() => {
    // Initialize site and plant data from the imported JSON file
    setSites(sitesData) // Setting the entire site data
    setSelectedSite(sitesData[0]?.site?.name) // Default to first site
  }, [])

  useEffect(() => {
    // Update plant options based on selected site
    const site = sites.find((s) => s.site.name === selectedSite)
    if (site) {
      setPlants(site.site.plants)
      setSelectedOption(site.site.plants[0]?.name) // Default to first plant
    }
  }, [selectedSite, sites])

  const handleOptionChange = (event) => setSelectedOption(event.target.value)
  const handleSiteChange = (event) => setSelectedSite(event.target.value)

  return (
    <>
      {!matchesXs && <Search />}
      {matchesXs && <Box sx={{ width: '100%', ml: 1 }} />}

      {/* Horizontal layout for Plant & Site */}
      <Stack direction='row' spacing={2} alignItems='center'>
        {/* Site Dropdown */}
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedSite}
              onChange={handleSiteChange}
              sx={{ color: 'white' }}
            >
              {sites.map((site, index) => (
                <MenuItem key={index} value={site.site.name}>
                  {site.site.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        {/* Plant Dropdown */}
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedOption}
              onChange={handleOptionChange}
              sx={{ color: 'white' }}
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
