import { Tab, Tabs } from '@mui/material'
import PropTypes from 'prop-types'

const TabSection = ({ tabIndex, setTabIndex, tabs }) => {
  return (
    <>
      <Tabs
        value={tabIndex}
        onChange={(e, newIndex) => setTabIndex(newIndex)}
        sx={{
          borderBottom: '0px solid #ccc',
          '.MuiTabs-indicator': { display: 'none' },
          margin: '0px 0px 0px 0px',
          minHeight: '25px',
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
              fontSize: '0.75rem',
              padding: '9px',
              minHeight: '12px',
            }}
          />
        ))}
      </Tabs>
    </>
  )
}

TabSection.propTypes = {
  tabIndex: PropTypes.number.isRequired,
  setTabIndex: PropTypes.func.isRequired,
  tabs: PropTypes.array.isRequired,
}

export default TabSection
