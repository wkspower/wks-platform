import { Box, Tab, Tabs } from '@mui/material'
import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import SelectivityData from '../SelectivityData'

const ConfigurationTable = () => {
  const keycloak = useSession()

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical

  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [tabIndex, setTabIndex] = useState(0)
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [startUpRows, setStartUpRows] = useState([])
  const [otherLossRows, setOtherLossRows] = useState([])
  const [shutdownNormsRows, setShutdownRows] = useState([])
  const [productionRows, setProductionRows] = useState([])
  const [gradeData, setGradeData] = useState([])
  const [continiousGradeData, setContiniousGradeData] = useState([])
  const [discontiniousGradeData, setDiscontiniousGradeData] = useState([])
  const [tabs, setTabs] = useState([])

  const fetchData = async () => {
    setRows([])
    setProductionRows([])
    setLoading(true)
    try {
      var data = await DataService.getCatalystSelectivityData(keycloak)
      if (lowerVertName === 'meg') {
        setLoading(true)
        data = data.sort((a, b) => b.normType.localeCompare(a.normType))
        const groupedRows = []
        const groups = new Map()
        let groupId = 0
        data.forEach((item, index) => {
          const formattedItem = {
            ...item,
            idFromApi: item.id,
            id: groupId++,
            originalRemark: item.remarks,
            srNo: index + 1,
          }
          const groupKey = item.normType
          if (!groups.has(groupKey)) {
            groups.set(groupKey, [])
            groupedRows.push({
              id: groupId++,
              Particulars: groupKey,
              isGroupHeader: true,
            })
          }
          groups.get(groupKey).push(formattedItem)
          groupedRows.push(formattedItem)
          setProductionRows(groupedRows)
          setRows(groupedRows)
        })
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
          if (ConfigTypeName === 'ShutdownNorms') {
            rowsForThisCategory.push({
              id: groupId++,
              Particulars: ConfigTypeName,
              isGroupHeader: true,
            })
          }
          normGroup.forEach((items, TypeName) => {
            if (ConfigTypeName === 'ShutdownNorms') {
              rowsForThisCategory.push({
                id: groupId++,
                Particulars2: TypeName,
                isSubGroupHeader: true,
              })
            } else {
              rowsForThisCategory.push({
                id: groupId++,
                Particulars: TypeName,
                isGroupHeader: true,
              })
            }
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
          } else if (ConfigTypeName == 'ContiniousGrades') {
            continiousGradeRows = rowsForThisCategory
          } else if (ConfigTypeName == 'DiscontiniousGrades') {
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
        //   'ContiniousGrades',
        //   'DiscontiniousGrades',
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

  useEffect(() => {
    getConfigurationTabsMatrix()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  if (lowerVertName != 'pe') {
    return (
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '5px',
          marginTop: '20px',
        }}
      >
        <Box>
          <SelectivityData
            rows={productionRows}
            loading={loading}
            fetchData={fetchData}
            setRows={setProductionRows}
            configType={'production'}
          />
        </Box>
      </div>
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
        {tabs.map((tab, index) => (
          <Tab
            sx={{
              border: '1px solid #ADD8E6',
              borderBottom: '1px solid #ADD8E6',
            }}
            key={tab}
            label={
              tab === 'StartupLosses'
                ? 'Startup Losses'
                : tab === 'OtherLosses'
                  ? 'Other Losses'
                  : tab === 'ShutdownNorms'
                    ? 'Constants'
                    : tab === 'Receipes'
                      ? 'Receipes'
                      : tab === 'ContiniousGrades'
                        ? 'Continuous Grade Changes'
                        : tab === 'DiscontiniousGrades'
                          ? 'Discontinuous Grade Changes'
                          : tab
            }
          />
        ))}
      </Tabs>

      <Box>
        {tabs[tabIndex] === 'StartupLosses' && (
          <SelectivityData
            rows={startUpRows}
            loading={loading}
            fetchData={fetchData}
            setRows={setStartUpRows}
            configType='StartupLosses'
          />
        )}
        {tabs[tabIndex] === 'OtherLosses' && (
          <SelectivityData
            rows={otherLossRows}
            loading={loading}
            fetchData={fetchData}
            setRows={setOtherLossRows}
            configType='Otherlosses'
          />
        )}
        {tabs[tabIndex] === 'ShutdownNorms' && (
          <SelectivityData
            rows={shutdownNormsRows}
            loading={loading}
            setRows={setShutdownRows}
            fetchData={fetchData}
            configType='ShutdownNorms'
          />
        )}
        {tabs[tabIndex] === 'Receipes' && (
          <SelectivityData
            rows={gradeData}
            loading={loading}
            setRows={setGradeData}
            configType='grades'
          />
        )}
        {tabs[tabIndex] === 'ContiniousGrades' && (
          <SelectivityData
            rows={continiousGradeData}
            loading={loading}
            setRows={setContiniousGradeData}
            fetchData={fetchData}
            configType='ContiniousGrades'
          />
        )}
        {tabs[tabIndex] === 'DiscontiniousGrades' && (
          <SelectivityData
            rows={discontiniousGradeData}
            loading={loading}
            setRows={setDiscontiniousGradeData}
            fetchData={fetchData}
            configType='DiscontiniousGrades'
          />
        )}
      </Box>
    </div>
  )
}

export default ConfigurationTable
