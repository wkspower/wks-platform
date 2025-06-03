// CrackerConfig.jsx
import { Box, Tab, Tabs } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import SelectivityData from './SelectivityData'

const CrackerConfig = () => {
  // Cracker’s own tabs
  const rawTabs = [
    'feed',
    'composition',
    'hydrogenation',
    'recovery',
    'optimizing',
    'furnace',
  ]
  const [rawTabIndex, setRawTabIndex] = useState(0)
  const feedRows = [
    {
      id: 1,
      particulars: 'Propane',
      uom: 'tpd',
      remarks: 'from planning team (1.8 TPH min is required)',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'C2/C3',
      uom: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'Ethane',
      uom: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'FCC C3',
      uom: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'BPCL Kochi Propylene',
      uom: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'LDPE off Gas',
      uom: 'tpd',
      remarks: 'PAS data for current financial year',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'PP off gas',
      uom: 'tpd',
      remarks: 'PAS data for current financial year',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 8,
      particulars: 'Hexene Purge Gas',
      uom: 'tpd',
      remarks: 'PAS data for current financial year',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 9,
      particulars: 'Additional feed (Future scope)',
      uom: '',
      remarks:
        '1) Add button will be required for new addition of rows.\n2) Option to be provided to get the export',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const compositionRows = [
    {
      id: 1,
      particulars: 'Methane',
      uom: 'Wt%',
      remarks: 'Manual Entry / Lab Tag for the current year',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'Ethane',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'Propane',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'i-C4',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'n-C4',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'C5',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const hydrogenationRows = [
    {
      id: 1,
      particulars: 'C3 Hdn selectivity',
      uom: '%',
      remarks: 'Manual Entry (remain same for each mode of operation)',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'MAPD Conversion',
      uom: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'C2 Hdn selectivity',
      uom: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'C2 Conversion',
      uom: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const recoveryRows = [
    {
      id: 1,
      particulars: 'Ethylene loss in RG',
      uom: 'ppmv',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'Ethylene in C2 spl bot',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'ethane in ethylene',
      uom: 'ppmw',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'C4 loss in DP O/h',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'Propylene in C3',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'Propane in propylene Product',
      uom: 'ppmw',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'LDPE off gas to Fuel',
      uom: 'Kg/day',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const optimizingRows = [
    {
      id: 1,
      particulars: 'C2C3 feed',
      uom: 'tpd',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'Import Propane',
      uom: 'tpd',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'Ethane conversion',
      uom: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'Propane conversion',
      uom: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'Propylene loss in Propane',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'Zone on Ethane',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'Additional Variable',
      uom: '',
      remarks: 'Add button will be required for new addition of rows.',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const furnaceRows = [
    {
      id: 1,
      particulars: 'Max Flow per Zone for Ethane',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'Max Flow per Zone for Propane',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'Max Ethane Conversion',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'Max Propane Conversion',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'Max CGC molar flow',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'Max Caustic Tower rho *V2',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'Max Furnace Duty',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 8,
      particulars: 'Additional Constraint',
      uom: '',
      remarks: 'Add button will be required for new addition of rows.',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const [feedRowsDummy, setFeedRows] = useState([])
  const [compositionRowsDummy, setCompositionRows] = useState([])
  const [hydrogenationRowsDummy, setHydrogenationRows] = useState([])
  const [recoveryRowsDummy, setRecoveryRows] = useState([])
  const [optimizingRowsDummy, setOptimizingRows] = useState([])
  const [furnaceRowsDummy, setFurnaceRows] = useState([])

  const fetchCrackerRows = useCallback((tab) => {
    // Simulate network delay
    setTimeout(() => {
      switch (tab) {
        case 'feed':
          setFeedRows(feedRows)
          break
        case 'composition':
          setCompositionRows(compositionRows)
          break
        case 'hydrogenation':
          setHydrogenationRows(hydrogenationRows)
          break
        case 'recovery':
          setRecoveryRows(recoveryRows)
          break
        case 'optimizing':
          setOptimizingRows(optimizingRows)
          break
        case 'furnace':
          setFurnaceRows(furnaceRows)
          break
        default:
          break
      }
    }, 500) // 500ms delay to mimic async
  }, [])

  // 5️⃣ Whenever the selected tab changes, reload that tab’s rows
  useEffect(() => {
    const currentTab = rawTabs[rawTabIndex]
    fetchCrackerRows(currentTab)
  }, [rawTabIndex, fetchCrackerRows])

  return (
    <Box>
      <Tabs
        sx={{
          borderBottom: '0px solid #ccc',
          '.MuiTabs-indicator': { display: 'none' },
          // margin: '-35px 0px -8px 0%',
        }}
        textColor='primary'
        indicatorColor='primary'
        value={rawTabIndex}
        onChange={(e, newIndex) => setRawTabIndex(newIndex)}
      >
        {rawTabs.map((tabId) => (
          <Tab
            key={tabId}
            sx={{
              border: '1px solid #ADD8E6',
              borderBottom: '1px solid #ADD8E6',
              textTransform: 'capitalize',
            }}
            label={tabId}
          />
        ))}
      </Tabs>

      <Box>
        {(() => {
          switch (rawTabs[rawTabIndex]) {
            case 'feed':
              return (
                <Box>
                  <SelectivityData
                    rows={feedRowsDummy}
                    setRows={setFeedRows}
                    fetchData={() => fetchCrackerRows('feed')}
                    configType='cracker'
                  />
                </Box>
              )

            case 'composition':
              return (
                <Box>
                  <SelectivityData
                    rows={compositionRowsDummy}
                    setRows={setCompositionRows}
                    fetchData={() => fetchCrackerRows('composition')}
                    configType='cracker'
                  />
                </Box>
              )

            case 'hydrogenation':
              return (
                <Box>
                  <SelectivityData
                    rows={hydrogenationRowsDummy}
                    setRows={setHydrogenationRows}
                    fetchData={() => fetchCrackerRows('hydrogenation')}
                    configType='cracker'
                  />
                </Box>
              )

            case 'recovery':
              return (
                <Box>
                  <SelectivityData
                    rows={recoveryRowsDummy}
                    setRows={setRecoveryRows}
                    fetchData={() => fetchCrackerRows('recovery')}
                    configType='cracker'
                  />
                </Box>
              )

            case 'optimizing':
              return (
                <Box>
                  <SelectivityData
                    rows={optimizingRowsDummy}
                    setRows={setOptimizingRows}
                    fetchData={() => fetchCrackerRows('optimizing')}
                    configType='cracker'
                  />
                </Box>
              )

            case 'furnace':
              return (
                <Box>
                  <SelectivityData
                    rows={furnaceRowsDummy}
                    setRows={setFurnaceRows}
                    fetchData={() => fetchCrackerRows('furnace')}
                    configType='cracker'
                  />
                </Box>
              )

            default:
              return null
          }
        })()}
      </Box>
    </Box>
  )
}

export default CrackerConfig
