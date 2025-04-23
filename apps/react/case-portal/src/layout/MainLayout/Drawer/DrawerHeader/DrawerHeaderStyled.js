import { styled } from '@mui/material/styles'
import Box from '@mui/material/Box'

const DrawerHeaderStyled = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'open',
})(({ theme, open }) => ({
  ...theme.mixins.toolbar,
  display: 'flex',
  alignItems: 'center',
  minHeight: '0px',
  paddingTop: '0px',
  paddingBottom: '0px',
  justifyContent: open ? 'flex-start' : 'center',
  paddingLeft: theme.spacing(open ? 3 : 0),
}))

export default DrawerHeaderStyled
