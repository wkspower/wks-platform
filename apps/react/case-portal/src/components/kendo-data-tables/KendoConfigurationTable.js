import { Box, Tab, Tabs } from '@mui/material'
import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import SelectivityData from './SelectivityData'
import businessDemandColumns from '../../assets/kendo_config_cracker_coldefs.json'

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
  const fetchData = async () => {
    // setRows([])
    setProductionRows([])
    setLoading(true)
    try {
      var data = await DataService.getCatalystSelectivityData(keycloak)

      if (tabs.length == 0) {
        setLoading(true)
        // data = data.sort((a, b) => b.normType.localeCompare(a.normType))
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
          // setRows(groupedRows)
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

  const feedRows = [
    {
      id: 1,
      particulars: 'Propane',
      UOM: 'tpd',
      remarks: 'from planning team (1.8 TPH min is required)',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'C2/C3',
      UOM: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'Ethane',
      UOM: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'FCC C3',
      UOM: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'BPCL Kochi Propylene',
      UOM: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'LDPE off Gas',
      UOM: 'tpd',
      remarks: 'PAS data for current financial year',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'PP off gas',
      UOM: 'tpd',
      remarks: 'PAS data for current financial year',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 8,
      particulars: 'Hexene Purge Gas',
      UOM: 'tpd',
      remarks: 'PAS data for current financial year',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 9,
      particulars: 'Additional feed (Future scope)',
      UOM: '',
      remarks:
        '1) Add button will be required for new addition of rows.\n2) Option to be provided to get the export',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
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
      UOM: 'Wt%',
      remarks: 'Manual Entry / Lab Tag for the current year',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'Ethane',
      UOM: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'Propane',
      UOM: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'i-C4',
      UOM: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'n-C4',
      UOM: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'C5',
      UOM: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
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
      UOM: '%',
      remarks: 'Manual Entry (remain same for each mode of operation)',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'MAPD Conversion',
      UOM: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'C2 Hdn selectivity',
      UOM: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'C2 Conversion',
      UOM: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
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
      UOM: 'ppmv',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'Ethylene in C2 spl bot',
      UOM: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'ethane in ethylene',
      UOM: 'ppmw',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'C4 loss in DP O/h',
      UOM: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'Propylene in C3',
      UOM: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'Propane in propylene Product',
      UOM: 'ppmw',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'LDPE off gas to Fuel',
      UOM: 'Kg/day',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
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
      UOM: 'tpd',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'Import Propane',
      UOM: 'tpd',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'Ethane conversion',
      UOM: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'Propane conversion',
      UOM: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'Propylene loss in Propane',
      UOM: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'Zone on Ethane',
      UOM: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'Additional Variable',
      UOM: '',
      remarks: 'Add button will be required for new addition of rows.',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
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
      UOM: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'Max Flow per Zone for Propane',
      UOM: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'Max Ethane Conversion',
      UOM: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'Max Propane Conversion',
      UOM: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'Max CGC molar flow',
      UOM: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'Max Caustic Tower rho *V2',
      UOM: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'Max Furnace Duty',
      UOM: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 8,
      particulars: 'Additional Constraint',
      UOM: '',
      remarks: 'Add button will be required for new addition of rows.',
      jan: 100.0,
      feb: 100.0,
      marchs: 100.0,
      aprils: 200.0,
      may: 100.0,
      junes: 100.0,
      julys: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  // If no dynamic tabs have loaded yet, show “Production” by default:
  if (tabs.length === 0 && lowerVertName !== 'cracker') {
    return (
      <Box sx={{ marginTop: '20px' }}>
        <SelectivityData
          rows={productionRows}
          loading={loading}
          fetchData={fetchData}
          setRows={setProductionRows}
          configType='production'
        />
      </Box>
    )
  } else if (lowerVertName === 'cracker') {
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
                      rows={feedRows}
                      columns={businessDemandColumns}
                    />
                  </Box>
                )

              case 'composition':
                return (
                  <Box>
                    <SelectivityData
                      rows={compositionRows}
                      columns={businessDemandColumns}
                    />
                  </Box>
                )

              case 'hydrogenation':
                return (
                  <Box>
                    <SelectivityData
                      rows={hydrogenationRows}
                      columns={businessDemandColumns}
                    />
                  </Box>
                )

              case 'recovery':
                return (
                  <Box>
                    <SelectivityData
                      rows={recoveryRows}
                      columns={businessDemandColumns}
                    />
                  </Box>
                )

              case 'optimizing':
                return (
                  <Box>
                    <SelectivityData
                      rows={optimizingRows}
                      columns={businessDemandColumns}
                    />
                  </Box>
                )

              case 'furnace':
                return (
                  <Box>
                    <SelectivityData
                      rows={furnaceRows}
                      columns={businessDemandColumns}
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
