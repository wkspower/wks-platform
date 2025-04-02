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

  //   useEffect(() => {
  //     fetchData()
  //   }, [sitePlantChange, lowerVertName])
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

  // const fetchData = async () => {
  //   setLoading(true)
  //   try {
  //     const data = await DataService.getCatalystSelectivityData(keycloak)
  //     if (lowerVertName === 'meg') {
  //       const formattedData = data.map((item, index) => ({
  //         ...item,
  //         idFromApi: item.id,
  //         id: index,
  //       }))
  //       setRows(formattedData)
  //     } else {
  //       const groups = new Map()
  //       data.forEach((item) => {
  //         const lossCategory = item.lossCategory
  //         const normType = item.normType

  //         if (!groups.has(lossCategory)) {
  //           groups.set(lossCategory, new Map())
  //         }
  //         const normGroup = groups.get(lossCategory)
  //         if (!normGroup.has(normType)) {
  //           normGroup.set(normType, [])
  //         }
  //         normGroup.get(normType).push(item)
  //       })

  //       let groupId = 0
  //       const groupedRows = []
  //       const shutdownRows = []

  //       groups.forEach((normGroup, lossCategory) => {
  //         const targetArray =
  //           lossCategory.toLowerCase() === 'shutdownnorms'
  //             ? shutdownRows
  //             : groupedRows
  //         targetArray.push({
  //           id: groupId++,
  //           Particulars: lossCategory,
  //           isGroupHeader: true,
  //         })

  //         normGroup.forEach((items, normType) => {
  //           targetArray.push({
  //             id: groupId++,
  //             Particulars2: normType,
  //             isSubGroupHeader: true,
  //           })
  //           items.forEach((item) => {
  //             targetArray.push({ ...item, idFromApi: item.id, id: groupId++ })
  //           })
  //         })
  //       })
  //       const formatNormParameter = (value) => {
  //         const cleanedValue = value?.split(' - ')[0]
  //         return cleanedValue?.replace(/([a-z])([A-Z])/g, '$1 $2')
  //       }
  //       const transformedData = groupedRows.map((row) => {
  //         if (row.Particulars2) {
  //           return {
  //             ...row,
  //             normParameterFKId: formatNormParameter(
  //               `${row.Particulars2} - ${row.normParameterFKId}`,
  //             ), // Merging Particulars2
  //           }
  //         }
  //         return row
  //       })
  //       // Reorder logic: Move "startuplosses" first and "otherlosses" second
  //       // const desiredOrder = ['Startuplosses', 'Otherlosses']

  //       // transformedData.sort((a, b) => {
  //       //   return (
  //       //     desiredOrder.indexOf(a.lossCategory) -
  //       //     desiredOrder.indexOf(b.lossCategory)
  //       //   )
  //       // })

  //       // const reorderedTransformedData = reorderData(transformedData)

  //       setRows1(transformedData)
  //       setRows2(shutdownRows)
  //     }
  //     setLoading(false)
  //   } catch (error) {
  //     console.error('Error fetching data:', error)
  //     setLoading(false)
  //   }
  // }
  const defaultCustomHeight = { mainBox: '64vh', otherBox: '125%' }

  if (lowerVertName === 'meg') {
    return (
      <SelectivityData
        rows={rows}
        loading={loading}
        fetchData={fetchData}
        setRows={setRows}
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
      </Tabs>
      <Box>
        {tabIndex === 0 && (
          <SelectivityData
            rows={startUpRows}
            loading={loading}
            fetchData={fetchData}
            setRows={setStartUpRows}
            defaultCustomHeight={defaultCustomHeight}
          />
        )}
        {tabIndex === 1 && (
          <SelectivityData
            rows={otherLossRows}
            loading={loading}
            fetchData={fetchData}
            setRows={setOtherLossRows}
            defaultCustomHeight={defaultCustomHeight}
          />
        )}
        {tabIndex === 2 && (
          <SelectivityData
            rows={shutdownNormsRows}
            loading={loading}
            setRows={setShutdownRows}
            fetchData={fetchData}
            defaultCustomHeight={defaultCustomHeight}
            tabIndex={1}
          />
        )}
      </Box>
    </div>
  )
}

export default ConfigurationTable
