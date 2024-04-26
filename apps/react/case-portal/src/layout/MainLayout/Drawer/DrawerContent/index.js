import SimpleBar from 'components/third-party/SimpleBar'
import Navigation from './Navigation'

const DrawerContent = () => (
  <SimpleBar
    sx={{
      '& .simplebar-content': {
        display: 'flex',
        flexDirection: 'column',
      },
    }}
  >
    <Navigation />
  </SimpleBar>
)

export default DrawerContent
