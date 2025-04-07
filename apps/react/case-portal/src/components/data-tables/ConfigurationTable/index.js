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
  const [gradeData, setGradeData] = useState([])

  const fetchData = async () => {
    setLoading(true)
    try {
      const data = await DataService.getCatalystSelectivityData(keycloak)
      // console.log(data)
      if (lowerVertName === 'meg') {
        // For 'meg', simply map items without grouping.
        const formattedData = data.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
        }))
        setRows(formattedData)
      } else {
        // Group data by lossCategory and then by normType.
        const groups = new Map()
        data.forEach((item) => {
          const lossCategory = item.lossCategory
          const normType = item.normType
          if (!groups.has(lossCategory)) {
            groups.set(lossCategory, new Map())
          }
          const normGroup = groups.get(lossCategory)
          if (!normGroup.has(normType)) {
            normGroup.set(normType, [])
          }
          normGroup.get(normType).push(item)
        })

        let groupId = 0
        let shutdownRows = []
        let startUpRows = []
        let otherLossRows = []

        // Build the final grouped arrays.
        groups.forEach((normGroup, lossCategory) => {
          let rowsForThisCategory = []
          // For shutdown norms, include the lossCategory header.
          if (lossCategory.toLowerCase() === 'shutdownnorms') {
            rowsForThisCategory.push({
              id: groupId++,
              Particulars: lossCategory,
              isGroupHeader: true,
            })
          }
          // For each normType group within this lossCategory:
          normGroup.forEach((items, normType) => {
            // For shutdown norms, add a sub-group header.
            // For others, use normType as the main header (since we don't want the lossCategory header).
            if (lossCategory.toLowerCase() === 'shutdownnorms') {
              rowsForThisCategory.push({
                id: groupId++,
                Particulars2: normType,
                isSubGroupHeader: true,
              })
            } else {
              rowsForThisCategory.push({
                id: groupId++,
                Particulars: normType,
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

          // Separate arrays based on lossCategory (case-insensitive)
          if (lossCategory.toLowerCase() === 'shutdownnorms') {
            shutdownRows = rowsForThisCategory
          } else if (lossCategory.toLowerCase() === 'startuplosses') {
            startUpRows = rowsForThisCategory
          } else if (lossCategory.toLowerCase() === 'otherlosses') {
            otherLossRows = rowsForThisCategory
          }
        })

        // console.log('shutdownRows:', shutdownRows)
        // console.log('startUpRows:', startUpRows)
        // console.log('otherLossRows:', otherLossRows)

        // Update state as needed.
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
  //   const generateDynamicColumns = (data) => {
  //     const columns = [
  //       {
  //         field: 'receipeName',
  //         headerName: 'Grade',
  //         minWidth: 120,
  //         flex: 1,
  //       },
  //     ]

  //     const uniqueGradeNames = [
  //       ...new Set(data.map((item) => item.gradeName)),
  //     ]

  //     uniqueGradeNames.forEach((grade) => {
  //       columns.push({
  //         field: grade,
  //         headerName: grade,
  //         editable: true,
  //         align: 'left',
  //         headerAlign: 'left',
  //       })
  //     })

  //     return columns
  //   }

  //   const fetchConfigData = async () => {
  //     setLoading(true)
  //     try {
  //       // const data = await DataService.getPeConfigData(keycloak)
  //       const data = [
  //         {
  //           gradeName: 'M60075',
  //           receipeName: 'abcd',
  //           gradeFkId: '1AC76D49-D113-4FF0-9516-9F9E96D85DAE',
  //           reciepeFkId: '2AE40205-F960-4A57-975A-4598664E7F71',
  //           attributeValue: 104,
  //         },
  //         {
  //           gradeName: 'M24300',
  //           receipeName: 'xyz',
  //           gradeFkId: '60733500-7F07-443B-B893-2FCA2EBD8744',
  //           reciepeFkId: '2AE40205-F960-4A57-975A-4598664E7F71',
  //           attributeValue: 105,
  //         },
  //         {
  //           gradeName: 'E52007',
  //           receipeName: '',
  //           gradeFkId: '6481FB2E-373F-4CB5-8D1C-15E80D187060',
  //           reciepeFkId: 'B93B67DF-DDDB-41CB-8E9B-98E27342EF4A',
  //           attributeValue: 100,
  //         },
  //         {
  //           gradeName: 'P15807',
  //           receipeName: '',
  //           gradeFkId: '7BB94524-FFE3-4D04-8CAC-972047D8AD2F',
  //           reciepeFkId: 'B93B67DF-DDDB-41CB-8E9B-98E27342EF4A',
  //           attributeValue: 100,
  //         },
  //         {
  //           gradeName: 'F19010',
  //           receipeName: '',
  //           gradeFkId: '1AC76D49-D113-4FF0-9516-9F9E96D85DAE',
  //           reciepeFkId: 'B93B67DF-DDDB-41CB-8E9B-98E27342EF4A',
  //           attributeValue: 100,
  //         },
  //         {
  //           gradeName: 'F19010',
  //           receipeName: '',
  //           gradeFkId: '051934D2-3C1B-47C3-8624-BA56018C22A3',
  //           reciepeFkId: '2AE40205-F960-4A57-975A-4598664E7F71',
  //           attributeValue: 100,
  //         },
  //       ]
  //       const columnConfig = generateDynamicColumns(data)
  // console.log(columnConfig)

  //       console.log(data)
  //       // For 'meg', simply map items without grouping.
  //       const formattedData = data.map((item, index) => ({
  //         ...item,
  //         // idFromApi: item.id,
  //         id: index,
  //       }))
  //       setGradeData(formattedData)
  //     } catch (error) {
  //       console.error('Error fetching data:', error)
  //       setLoading(false)
  //     } finally {
  //       setLoading(false)
  //     }
  //   }

  const defaultCustomHeight = { mainBox: '64vh', otherBox: '125%' }
  // const configData3 = [
  //   {
  //     id: 1,
  //     grade: 'recipe1',
  //     F19010: 10,
  //     E52007: 5,
  //     P15807: 3,
  //     M24300: 7,
  //     M60075: 0,
  //     B56003: 2,
  //   },
  //   {
  //     id: 2,
  //     grade: 'recipe2',
  //     F19010: 8,
  //     E52007: 4,
  //     P15807: 2,
  //     M24300: 6,
  //     M60075: 1,
  //     B56003: 3,
  //   },
  //   {
  //     id: 3,
  //     grade: 'recipe3',
  //     F19010: 12,
  //     E52007: 6,
  //     P15807: 4,
  //     M24300: 5,
  //     M60075: 2,
  //     B56003: 1,
  //   },
  //   {
  //     id: 4,
  //     grade: 'recipe4',
  //     F19010: 9,
  //     E52007: 3,
  //     P15807: 5,
  //     M24300: 8,
  //     M60075: 2,
  //     B56003: 4,
  //   },
  //   {
  //     id: 5,
  //     grade: 'recipe5',
  //     F19010: 11,
  //     E52007: 7,
  //     P15807: 3,
  //     M24300: 6,
  //     M60075: 3,
  //     B56003: 2,
  //   },
  // ]

  // const colDefs = [
  //   {
  //     field: 'grade',
  //     headerName: 'GRADE',
  //     flex: 1, // or width: 120
  //   },
  //   {
  //     field: 'F19010',
  //     headerName: 'F19010',
  //     flex: 1,
  //   },
  //   {
  //     field: 'E52007',
  //     headerName: 'E52007',
  //     flex: 1,
  //   },
  //   {
  //     field: 'P15807',
  //     headerName: 'P15807',
  //     flex: 1,
  //   },
  //   {
  //     field: 'M24300',
  //     headerName: 'M24300',
  //     flex: 1,
  //   },
  //   {
  //     field: 'M60075',
  //     headerName: 'M60075',
  //     flex: 1,
  //   },
  //   {
  //     field: 'B56003',
  //     headerName: 'B56003',
  //     flex: 1,
  //   },
  // ]

  if (lowerVertName === 'meg') {
    return (
      <SelectivityData
        rows={rows}
        loading={loading}
        fetchData={fetchData}
        setRows={setRows}
        configType={'meg'}
      />
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
          margin: '0px 0px -22px 1%',
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
          label='Grades'
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
            configType={'startupLosses'}
          />
        )}
        {tabIndex === 1 && (
          <SelectivityData
            rows={otherLossRows}
            loading={loading}
            fetchData={fetchData}
            setRows={setOtherLossRows}
            defaultCustomHeight={defaultCustomHeight}
            configType={'otherLosses'}
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
            configType={'shutdownNorms'}
          />
        )}
        {tabIndex === 3 && (
          <SelectivityData
            rows={gradeData}
            loading={loading}
            setRows={setGradeData}
            // fetchData={fetchConfigData}
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
