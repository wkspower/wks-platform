import { useState } from 'react'
import { Tabs, Tab, Box } from '@mui/material'
import SelectivityData from '../SelectivityData'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'

const ConfigurationTable = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const [tabIndex, setTabIndex] = useState(0)
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [startUpRows, setStartUpRows] = useState([])
  const [otherLossRows, setOtherLossRows] = useState([])
  const [shutdownNormsRows, setShutdownRows] = useState([])
  const [productionRows, setProductionRows] = useState([])
  const [consumptionRows, setConsumptionRows] = useState([])
  const [consumptionRows2, setConsumptionRows2] = useState([])
  const [gradeData, setGradeData] = useState([])

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
        // let formattedItem = []

        data.forEach((item, index) => {
          const formattedItem = {
            ...item,
            idFromApi: item.id,
            id: groupId++,
            originalRemark: item.remarks,
            srNo: index + 1,
          }

          // if (lowerVertName !== 'pe') {
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
          // }

          groupedRows.push(formattedItem)
          setProductionRows(groupedRows)
          setRows(groupedRows) // Optional: if you still need all rows
        })

        // const formattedItem = data.map((item, index) => ({
        //   ...item,
        //   idFromApi: item.id,
        //   id: index,
        //   originalRemark: item.remarks,
        // }))

        // const productionData = formattedItem
        //   .filter((item) => item.normType === 'Production')
        //   .map((item, index) => ({
        //     ...item,
        //     srNo: index + 1, // Production srNo starts from 1
        //   }))

        // const consumptionData = formattedItem
        //   .filter((item) => item.normType === 'Consumption')
        //   .map((item, index) => ({
        //     ...item,
        //     srNo: index + 1, // Consumption srNo starts from 1
        //   }))

        // const consumptionData2 = formattedItem
        //   .filter((item) => item.normType === 'Calculated Intermediate Values')
        //   .map((item, index) => ({
        //     ...item,
        //     srNo: index + 1,
        //   }))

        // setProductionRows(productionData)
        // setProductionRows(formattedItem)
        // setConsumptionRows(consumptionData)
        // setConsumptionRows2(consumptionData2)
        // setRows(formattedItem) // Optional: if you still need all rows
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

        // Build the final grouped arrays.
        groups.forEach((normGroup, ConfigTypeName) => {
          let rowsForThisCategory = []
          // For shutdown norms, include the ConfigTypeName header.
          if (ConfigTypeName === 'ShutdownNorms') {
            rowsForThisCategory.push({
              id: groupId++,
              Particulars: ConfigTypeName,
              isGroupHeader: true,
            })
          }
          // For each TypeName group within this ConfigTypeName:
          normGroup.forEach((items, TypeName) => {
            // For shutdown norms, add a sub-group header.
            // For others, use TypeName as the main header (since we don't want the ConfigTypeName header).
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

            // Append each item.
            items.forEach((item) => {
              rowsForThisCategory.push({
                ...item,
                idFromApi: item.id,
                id: groupId++,
              })
            })
          })

          // Separate arrays based on ConfigTypeName (case-insensitive)
          if (ConfigTypeName == 'ShutdownNorms') {
            shutdownRows = rowsForThisCategory
          } else if (ConfigTypeName == 'StartupLosses') {
            startUpRows = rowsForThisCategory
          } else if (ConfigTypeName == 'Otherlosses') {
            otherLossRows = rowsForThisCategory
          }
        })
        setShutdownRows(shutdownRows)
        setStartUpRows(startUpRows)
        setOtherLossRows(otherLossRows)
      }

      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }
  const fetchDataIV = async () => {
    setLoading(true)
    try {
      const data = await DataService.getCatalystSelectivityDataIV(keycloak)

      if (lowerVertName === 'meg') {
        const formattedData = data?.data.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.remarks,
        }))

        const consumptionData2 = formattedData.map((item, index) => ({
          ...item,
          srNo: index + 1,
        }))

        // setProductionRows(productionData)
        // setConsumptionRows(consumptionData)
        setConsumptionRows2(consumptionData2)
        // setRows(formattedData) // Optional: if you still need all rows
      }

      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  const defaultCustomHeight = { mainBox: '64vh', otherBox: '125%' }

  if (lowerVertName === 'meg') {
    return (
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '5px',
          marginTop: '20px',
        }}
      >
        {/* <Tabs
          value={tabIndex}
          onChange={(event, newIndex) => setTabIndex(newIndex)}
          sx={{
            borderBottom: '0px solid #ccc',
            '.MuiTabs-indicator': { display: 'none' },
            margin: '-35px 0px -15px 0%',
          }}
          textColor='primary'
          indicatorColor='primary'
        >
          <Tab
            label='Production'
            sx={{
              border: tabIndex === 0 ? '1px solid ' : 'none',
              borderBottom: '1px solid',
            }}
          />
          <Tab
            label='Consumption'
            sx={{
              border: tabIndex === 1 ? '1px solid ' : 'none',
              borderBottom: '1px solid',
            }}
          />
          <Tab
            label='Calculated Intermediate Values'
            sx={{
              border: tabIndex === 2 ? '1px solid ' : 'none',
              borderBottom: '1px solid',
            }}
          />
        </Tabs> */}

        <Box>
          {/* {tabIndex === 0 && ( */}
          <SelectivityData
            rows={productionRows}
            loading={loading}
            fetchData={fetchData}
            setRows={setProductionRows}
            defaultCustomHeight={defaultCustomHeight}
            configType={'production'}
          />
          {/* )} */}
          {/* {tabIndex === 1 && (
            <SelectivityData
              rows={consumptionRows}
              loading={loading}
              fetchData={fetchData}
              setRows={setConsumptionRows}
              defaultCustomHeight={defaultCustomHeight}
              configType={'consumption'}
            />
          )}
          {tabIndex === 2 && (
            <SelectivityData
              rows={consumptionRows2}
              loading={loading}
              fetchData={fetchDataIV}
              setRows={setConsumptionRows2}
              defaultCustomHeight={defaultCustomHeight}
              configType={'Calculated Intermediate Values'}
            />
          )} */}
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
        value={tabIndex}
        onChange={(event, newIndex) => setTabIndex(newIndex)}
        sx={{
          borderBottom: '0px solid #ccc',
          '.MuiTabs-indicator': { display: 'none' },
          margin: '-35px 0px -15px 0%',
        }}
        textColor='primary'
        indicatorColor='primary'
      >
        <Tab
          label='Startup Losses'
          sx={{
            border: tabIndex === 0 ? '1px solid ' : 'none',
            borderBottom: '1px solid',
          }}
        />
        <Tab
          label='Other Losses'
          sx={{
            border: tabIndex === 1 ? '1px solid ' : 'none',
            borderBottom: '1px solid',
          }}
        />
        <Tab
          label='Constants'
          sx={{
            border: tabIndex === 2 ? '1px solid ' : 'none',
            borderBottom: '1px solid',
          }}
        />
        <Tab
          label='Receipes'
          sx={{
            border: tabIndex === 3 ? '1px solid ' : 'none',
            borderBottom: '1px solid',
          }}
        />
      </Tabs>
      <Box>
        {tabIndex === 0 && (
          <SelectivityData
            rows={startUpRows}
            loading={loading}
            fetchData={fetchData}
            setRows={setStartUpRows}
            defaultCustomHeight={defaultCustomHeight}
            configType={'StartupLosses'}
          />
        )}
        {tabIndex === 1 && (
          <SelectivityData
            rows={otherLossRows}
            loading={loading}
            fetchData={fetchData}
            setRows={setOtherLossRows}
            defaultCustomHeight={defaultCustomHeight}
            configType={'Otherlosses'}
          />
        )}
        {tabIndex === 2 && (
          <SelectivityData
            rows={shutdownNormsRows}
            loading={loading}
            setRows={setShutdownRows}
            fetchData={fetchData}
            defaultCustomHeight={defaultCustomHeight}
            tabIndex={2}
            configType={'ShutdownNorms'}
          />
        )}
        {tabIndex === 3 && (
          <SelectivityData
            rows={gradeData}
            loading={loading}
            setRows={setGradeData}
            defaultCustomHeight={defaultCustomHeight}
            tabIndex={3}
            configType={'grades'}
          />
        )}
      </Box>
    </div>
  )
}

export default ConfigurationTable
