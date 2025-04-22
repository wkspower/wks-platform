import PropTypes from 'prop-types'
import { useTheme } from '@mui/material/styles'
import Stack from '@mui/material/Stack'
import DrawerHeaderStyled from './DrawerHeaderStyled'
import Logo from 'components/Logo'

const DrawerHeader = ({ open }) => {
  const theme = useTheme()

  return (
    <DrawerHeaderStyled theme={theme} open={open}>
      <Stack direction='row' spacing={1} alignItems='center'>
        <Logo />
      </Stack>
    </DrawerHeaderStyled>
  )
}

DrawerHeader.propTypes = {
  open: PropTypes.bool,
}

DrawerHeader.defaultProps = {
  open: false,
}

export default DrawerHeader
