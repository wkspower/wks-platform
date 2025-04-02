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
  const [rows1, setRows1] = useState([])
  const [rows2, setRows2] = useState([])

  //   useEffect(() => {
  //     fetchData()
  //   }, [sitePlantChange, lowerVertName])

  const fetchData = async () => {
    setLoading(true)
    try {
      const data = await DataService.getCatalystSelectivityData(keycloak)
      if (lowerVertName === 'meg') {
        const formattedData = data.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
        }))
        setRows(formattedData)
      } else {
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
        const groupedRows = []
        const shutdownRows = []

        groups.forEach((normGroup, lossCategory) => {
          const targetArray =
            lossCategory.toLowerCase() === 'shutdownnorms'
              ? shutdownRows
              : groupedRows
          targetArray.push({
            id: groupId++,
            Particulars: lossCategory,
            isGroupHeader: true,
          })

          normGroup.forEach((items, normType) => {
            targetArray.push({
              id: groupId++,
              Particulars2: normType,
              isSubGroupHeader: true,
            })
            items.forEach((item) => {
              targetArray.push({ ...item, idFromApi: item.id, id: groupId++ })
            })
          })
        })
        const formatNormParameter = (value) => {
          const cleanedValue = value?.split(' - ')[0]
          return cleanedValue?.replace(/([a-z])([A-Z])/g, '$1 $2')
        }
        const transformedData = groupedRows.map((row) => {
          if (row.Particulars2) {
            return {
              ...row,
              normParameterFKId: formatNormParameter(
                `${row.Particulars2} - ${row.normParameterFKId}`,
              ), // Merging Particulars2
            }
          }
          return row
        })
        // Reorder logic: Move "startuplosses" first and "otherlosses" second
        // const desiredOrder = ['Startuplosses', 'Otherlosses']

        // transformedData.sort((a, b) => {
        //   return (
        //     desiredOrder.indexOf(a.lossCategory) -
        //     desiredOrder.indexOf(b.lossCategory)
        //   )
        // })

        // const reorderedTransformedData = reorderData(transformedData)

        setRows1(transformedData)
        console.log(transformedData)
        setRows2(shutdownRows)
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
        sx={{ margin: '0px 0px -22px 1%' }}
        textColor='primary'
        indicatorColor='primary'

        //   variant="fullWidth"
      >
        <Tab
          label='Configurations'
          sx={{
            border: tabIndex === 0 ? '1px solid ' : 'none',
            borderBottom: '1px solid',
            // borderRadius: '4px',
          }}
        />
        <Tab
          label='Constants'
          sx={{
            border: tabIndex === 1 ? '1px solid ' : 'none',
            borderBottom: '1px solid',
            // borderRadius: '4px',
          }}
        />
      </Tabs>
      <Box>
        {tabIndex === 0 && (
          <SelectivityData
            rows={rows1}
            loading={loading}
            fetchData={fetchData}
            setRows={setRows1}
            defaultCustomHeight={defaultCustomHeight}
          />
        )}
        {tabIndex === 1 && (
          <SelectivityData
            rows={rows2}
            loading={loading}
            setRows={setRows2}
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
