import { Box, useMediaQuery } from '@mui/material'
import { Inbox } from '@novu/react'
import MobileSection from './MobileSection'
import Profile from './Profile'
import Search from './Search'
import Config from 'consts/index'
import Notification from './Notification'

const HeaderContent = ({ keycloak }) => {
  const matchesXs = useMediaQuery((theme) => theme.breakpoints.down('md'))

  return (
    <>
      {!matchesXs && <Search />}
      {matchesXs && <Box sx={{ width: '100%', ml: 1 }} />}
      {Config.NovuEnabled ? (
        <Inbox
          applicationIdentifier={Config.NovuAppId}
          subscriberId={keycloak.idTokenParsed.email}
        />
      ) : (
        <Notification />
      )}

      {!matchesXs && <Profile keycloak={keycloak} />}
      {matchesXs && <MobileSection />}
    </>
  )
}

export default HeaderContent
