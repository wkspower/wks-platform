import {
  Box,
  useMediaQuery,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Typography,
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
import { useState } from 'react'

const HeaderContent = ({ keycloak }) => {
  const matchesXs = useMediaQuery((theme) => theme.breakpoints.down('md'))
  const [selectedOption, setSelectedOption] = useState('Plant 1')
  const [selectedSite, setSelectedSite] = useState('Site A')

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
            <MenuItem value='Site A'>Site A</MenuItem>
            <MenuItem value='Site B'>Site B</MenuItem>
            <MenuItem value='Site C'>Site C</MenuItem>
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
            <MenuItem value='Plant 1'>Plant 1</MenuItem>
            <MenuItem value='Plant 2'>Plant 2</MenuItem>
            <MenuItem value='Plant 3'>Plant 3</MenuItem>
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
