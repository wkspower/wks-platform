import { Box, Tab, Tabs } from '@mui/material'
import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import SelectivityData from './SelectivityData'
// import CrackerConfig from './KendoConfigCracker'

const ConfigurationTable = () => {
  const keycloak = useSession()

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  // const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical

  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [tabIndex, setTabIndex] = useState(0)
  const [loading, setLoading] = useState(false)
  // const [rows, setRows] = useState([])
  const [startUpRows, setStartUpRows] = useState([])
  const [otherLossRows, setOtherLossRows] = useState([])
  const [shutdownNormsRows, setShutdownRows] = useState([])
  const [productionRows, setProductionRows] = useState([])
  const [gradeData, setGradeData] = useState([])
  const [continiousGradeData, setContiniousGradeData] = useState([])
  const [discontiniousGradeData, setDiscontiniousGradeData] = useState([])
  const [tabs, setTabs] = useState([])
  const [availableTabs, setAvailableTabs] = useState([])

  const fetchData = async () => {
    // setRows([])
    setProductionRows([])
    setLoading(true)
    try {
      var data = await DataService.getCatalystSelectivityData(keycloak)

      if (tabs.length == 0) {
        setLoading(true)
        // data = data.sort((a, b) => b.normType.localeCompare(a.normType))

        const formattedData = data.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.remarks,
          srNo: index + 1,
          Particulars: item.normType,
        }))
        // console.log(formattedData)
        setProductionRows(formattedData)
        // setRows(formattedData)
      } else {
        const groups = new Map()
        data.forEach((item) => {
          const ConfigTypeName = item.ConfigTypeName
          const TypeName = item.TypeDisplayName
          if (!groups.has(ConfigTypeName)) {
            groups.set(ConfigTypeName, new Map())
          }
          const normGroup = groups.get(ConfigTypeName)
          if (!normGroup.has(TypeName)) {
            normGroup.set(TypeName, [])
          }
          normGroup.get(TypeName).push(item)
        })
        let groupId = 0
        let shutdownRows = []
        let startUpRows = []
        let otherLossRows = []
        let continiousGradeRows = []
        let discontiniousGradeRows = []
        groups.forEach((normGroup, ConfigTypeName) => {
          let rowsForThisCategory = []
          normGroup.forEach((items, TypeName) => {
            items.forEach((item) => {
              rowsForThisCategory.push({
                ...item,
                idFromApi: item.id,
                id: groupId++,
              })
            })
          })
          if (ConfigTypeName == 'ShutdownNorms') {
            shutdownRows = rowsForThisCategory
          } else if (ConfigTypeName == 'StartupLosses') {
            startUpRows = rowsForThisCategory
          } else if (ConfigTypeName == 'Otherlosses') {
            otherLossRows = rowsForThisCategory
          } else if (ConfigTypeName == 'ContineGradeChange') {
            continiousGradeRows = rowsForThisCategory
          } else if (ConfigTypeName == 'DisContineGradeChange') {
            discontiniousGradeRows = rowsForThisCategory
          }
        })
        setShutdownRows(shutdownRows)
        setStartUpRows(startUpRows)
        setOtherLossRows(otherLossRows)
        setContiniousGradeData(continiousGradeRows)
        setDiscontiniousGradeData(discontiniousGradeRows)
      }
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  const getConfigurationTabsMatrix = async () => {
    setLoading(true)
    try {
      var response = await DataService.getConfigurationTabsMatrix(keycloak)
      if (response?.code == 200) {
        const parsedData = JSON.parse(response?.data)

        setTabs(parsedData)
      } else {
        // setTabs([
        //   'StartupLosses',
        //   'OtherLosses',
        //   'ShutdownNorms',
        //   'Receipes',
        //   'ContineGradeChange',
        //   'DisContineGradeChange',
        // ])
        setTabs([])
      }
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setTabs([])
      setLoading(false)
    }
  }

  const getConfigurationAvailableTabs = async () => {
    setLoading(true)
    try {
      var response = await DataService.getConfigurationAvailableTabs(keycloak)

      if (response?.code == 200) {
        setAvailableTabs(response?.data?.configurationTypeList)
      } else {
        setAvailableTabs([])
      }
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setAvailableTabs([])
      setLoading(false)
    }
  }

  useEffect(() => {
    getConfigurationTabsMatrix()
    getConfigurationAvailableTabs()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const getTheId = (name) => {
    const tab = availableTabs.find((tab) => tab.name === name)
    return tab ? tab.id : null
  }
  // and want to paste that new crakcer component here
  if (tabs.length === 0 && lowerVertName !== 'cracker') {
    return (
      <Box sx={{ marginTop: '20px' }}>
        <SelectivityData
          rows={productionRows}
          loading={loading}
          fetchData={fetchData}
          setRows={setProductionRows}
          configType='meg'
          groupBy='Particulars'
        />
      </Box>
    )
  }
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '5px',
        marginTop: '20px',
      }}
    >
      <Tabs
        sx={{
          borderBottom: '0px solid #ccc',
          '.MuiTabs-indicator': { display: 'none' },
          margin: '-35px 0px -8px 0%',
        }}
        textColor='primary'
        indicatorColor='primary'
        value={tabIndex}
        onChange={(e, newIndex) => setTabIndex(newIndex)}
      >
        {tabs.map((tabId) => {
          const tabInfo = availableTabs.find(
            (tab) => tab.id.toLowerCase() === tabId.toLowerCase(),
          )
          return (
            <Tab
              key={tabId}
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
              }}
              label={tabInfo?.displayName || 'N/A'}
            />
          )
        })}
      </Tabs>

      <Box>
        {(() => {
          const currentTabId = tabs[tabIndex]?.toLowerCase()
          switch (currentTabId) {
            // case 'ac3c9ad7-82b5-4550-b04d-fed0f1fb4908': // StartupLosses
            case getTheId('StartupLosses'):
              return (
                <SelectivityData
                  rows={startUpRows}
                  loading={loading}
                  fetchData={fetchData}
                  setRows={setStartUpRows}
                  configType='StartupLosses'
                  groupBy='TypeDisplayName'
                />
              )
            case getTheId('Otherlosses'): // Otherlosses
              return (
                <SelectivityData
                  rows={otherLossRows}
                  loading={loading}
                  fetchData={fetchData}
                  setRows={setOtherLossRows}
                  configType='Otherlosses'
                  groupBy='TypeDisplayName'
                />
              )
            case getTheId('ShutdownNorms'): // ShutdownNorms
              return (
                <SelectivityData
                  rows={shutdownNormsRows}
                  loading={loading}
                  setRows={setShutdownRows}
                  fetchData={fetchData}
                  configType='ShutdownNorms'
                  groupBy='TypeDisplayName'
                  // groupBy2='ConfigTypeDisplayName'
                />
              )
            case getTheId('Receipe'): // Receipe
              return (
                <SelectivityData
                  rows={gradeData}
                  loading={loading}
                  setRows={setGradeData}
                  configType='grades'
                />
              )
            case getTheId('ContineGradeChange'): // ContineGradeChange
              return (
                <SelectivityData
                  rows={continiousGradeData}
                  loading={loading}
                  setRows={setContiniousGradeData}
                  fetchData={fetchData}
                  configType='ContineGradeChange'
                />
              )
            case getTheId('DisContineGradeChange'): // DisContineGradeChange
              return (
                <SelectivityData
                  rows={discontiniousGradeData}
                  loading={loading}
                  setRows={setDiscontiniousGradeData}
                  fetchData={fetchData}
                  configType='DisContineGradeChange'
                />
              )
            default:
              return null
          }
        })()}
      </Box>
    </div>
  )
}

export default ConfigurationTable
