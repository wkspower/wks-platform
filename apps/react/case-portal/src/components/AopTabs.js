import { Tab, Tabs } from '@mui/material'
import PropTypes from 'prop-types'

const AopTabs = ({ tabIndex, setTabIndex, tabs }) => {
  return (
    <>
      <Tabs
        value={tabIndex}
        onChange={(e, newIndex) => setTabIndex(newIndex)}
        sx={{
          borderBottom: '0px solid #ccc',
          '.MuiTabs-indicator': { display: 'none' },
          margin: '0px 0px 0px 0px',
          minHeight: '32px', // reduce tab bar height
        }}
        textColor='primary'
        indicatorColor='primary'
      >
        {tabs.map((tab) => (
          <Tab
            key={tab}
            label={tab}
            sx={{
              border: '1px solid #ADD8E6',
              borderBottom: '1px solid #ADD8E6',

              padding: '9px',
              minHeight: '10px',
            }}
          />
        ))}
      </Tabs>
    </>
  )
}

AopTabs.propTypes = {
  tabIndex: PropTypes.number.isRequired,
  setTabIndex: PropTypes.func.isRequired,
  tabs: PropTypes.array.isRequired,
}

export default AopTabs
