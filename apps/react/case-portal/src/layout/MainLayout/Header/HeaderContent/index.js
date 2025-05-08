import { useState, useEffect } from 'react'
import {
  Box,
  Typography,
  FormControl,
  Select,
  MenuItem,
  Stack,
  useMediaQuery,
} from '@mui/material'
import { useDispatch, useSelector } from 'react-redux'
import {
  setAopYear,
  setYearChange,
  setVerticalChange,
  setSitePlantChange,
  setCurrentYear,
  setOldYear,
} from 'store/reducers/dataGridStore'
import { DataService } from 'services/DataService'
import Search from './Search'
import Profile from './Profile/index'
import MobileSection from './MobileSection'

// import Logo from '../../../assets/images/ril-logo2.png'
import Logo from 'assets/images/ril-logo2.png'

// Utility to parse the Keycloak ?allowed? JSON
function parseAllowed(raw) {
  const map = {}
  raw.forEach((vObj) => {
    const vid = Object.keys(vObj)[0]
    map[vid] = {}
    vObj[vid].forEach((siteObj) => {
      const sid = Object.keys(siteObj)[0]
      map[vid][sid] = siteObj[sid]
    })
  })
  return map
}

export default function HeaderContent({ keycloak }) {
  const dispatch = useDispatch()
  const matchesXs = useMediaQuery((theme) => theme.breakpoints.down('md'))
  const { drawerOpen } = useSelector((state) => state.menu) // Get drawer state

  // --- 1. Year logic state
  const [aopYears, setAopYears] = useState([])
  const [selectedYear, setSelectedYear] = useState('')

  // --- 2. screenTitleName from Redux
  const screenTitle = useSelector((s) => s.dataGridStore.screenTitle)
  const screenTitleName = screenTitle?.title || 'Digital AOP'

  // allowed/full data
  const [allowedMap, setAllowedMap] = useState({})
  const [fullDetails, setFullDetails] = useState([])

  // dropdowns
  const [verticals, setVerticals] = useState([])
  const [sites, setSites] = useState([])
  const [plants, setPlants] = useState([])

  const [selectedVertical, setSelectedVertical] = useState('')
  const [selectedSite, setSelectedSite] = useState('')
  const [selectedPlant, setSelectedPlant] = useState('')

  // 1?? parse Keycloak allowed once
  useEffect(() => {
    let parsed = []
    try {
      parsed = JSON.parse(keycloak.idTokenParsed.plants)
    } catch (e) {
      console.error('Token parse error', e)
    }
    setAllowedMap(parseAllowed(parsed))
  }, [keycloak])

  // 2?? fetch full details once
  useEffect(() => {
    DataService.getAllSites(keycloak)
      .then((data) => setFullDetails(data || []))
      .catch((err) => {
        console.error('Error fetching sites', err)
        setFullDetails([])
      })
  }, [keycloak])

  // 3?? build verticals list & default ? localStorage + Redux
  useEffect(() => {
    if (!fullDetails.length || !Object.keys(allowedMap).length) return

    const avail = fullDetails
      .filter((v) => allowedMap[v.id])
      .map((v) => ({ id: v.id, name: v.displayName }))
    setVerticals(avail)

    if (!selectedVertical && avail.length) {
      const defV = avail[0]
      setSelectedVertical(defV.id)

      // persist vertical default
      localStorage.setItem('verticalId', defV.id)
      localStorage.setItem(
        'selectedVertical',
        JSON.stringify({ id: defV.id, name: defV.name }),
      )
      // console.log(defV.name)
      // dispatch Redux
      dispatch(
        setVerticalChange({
          selectedVertical: defV.name,
          selectedSite: '',
          selectedPlant: '',
        }),
      )
    }
  }, [fullDetails, allowedMap, selectedVertical, dispatch])

  // 4?? update sites when vertical changes ? localStorage + Redux
  useEffect(() => {
    if (!selectedVertical) {
      setSites([])
      setSelectedSite('')
      return
    }
    const vertObj = fullDetails.find((v) => v.id === selectedVertical)
    const allowedSites = allowedMap[selectedVertical] || {}
    const list = (vertObj?.sites || [])
      .filter((s) => allowedSites[s.id])
      .map((s) => ({ id: s.id, name: s.displayName }))
    setSites(list)

    if (list.length) {
      const defS = list[0]
      setSelectedSite(defS.id)

      localStorage.setItem(
        'selectedSite',
        JSON.stringify({ id: defS.id, name: defS.name }),
      )
      localStorage.setItem('selectedSiteId', JSON.stringify({ id: defS.id }))
      dispatch(setSitePlantChange({ sitePlantChange: true }))
    }
  }, [selectedVertical, fullDetails, allowedMap, dispatch])

  // 5?? update plants when site changes ? localStorage + Redux
  useEffect(() => {
    if (!selectedSite) {
      setPlants([])
      setSelectedPlant('')
      return
    }
    const vertObj = fullDetails.find((v) => v.id === selectedVertical)
    const siteObj = vertObj?.sites.find((s) => s.id === selectedSite)
    const allowedPlants = allowedMap[selectedVertical]?.[selectedSite] || []
    const list = (siteObj?.plants || [])
      .filter((p) => allowedPlants.includes(p.id))
      .map((p) => ({ id: p.id, name: p.displayName }))
    setPlants(list)

    if (list.length) {
      const defP = list[0]
      setSelectedPlant(defP.id)

      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({ id: defP.id, name: defP.name }),
      )
      dispatch(setSitePlantChange({ sitePlantChange: true }))
    }
  }, [selectedSite, selectedVertical, fullDetails, allowedMap, dispatch])

  // 6?? fetch AOP years on mount ? localStorage + Redux
  useEffect(() => {
    async function fetchYears() {
      try {
        var resp = await DataService.getAopyears(keycloak)
        if (resp?.length) {
          setAopYears(resp)

          const currentYear = resp.find(
            (item) => item.currentYear == 1,
          )?.AOPYear

          if (currentYear) {
            setSelectedYear(currentYear)
            localStorage.setItem('year', currentYear)
            dispatch(setAopYear({ selectedYear: currentYear }))
          }
        }
      } catch (err) {
        console.error('Error fetching AOP years', err)
      }
    }
    fetchYears()
  }, [keycloak, dispatch])

  // Handler for year change ? localStorage + Redux
  const handleYearChange = (e) => {
    const newYear = e.target.value
    setSelectedYear(newYear)

    localStorage.setItem('year', newYear)
    dispatch(setYearChange({ yearChanged: true }))
    dispatch(setAopYear({ selectedYear: newYear }))

    // Find the selected year object to determine if it's the current year
    const selectedYearObj = aopYears.find((y) => y.AOPYear === newYear)
    const isCurrentYear = selectedYearObj?.currentYear == 1

    const currentYear = aopYears.find((y) => y.currentYear == 1)
    dispatch(setCurrentYear({ currentYear: isCurrentYear ? 1 : 0 }))
    let isOldYear = 0
    let currentYear1 = currentYear?.AOPYear
    const [currentStartYear] = currentYear1.split('-').map(Number)
    const [selectedStartYear] = newYear.split('-').map(Number)
    if (selectedStartYear < currentStartYear) {
      isOldYear = 1
    }
    dispatch(setOldYear({ oldYear: isOldYear }))
  }
  // inside HeaderContent, above the return
  const handlePlantChange = (e) => {
    const newPlantId = e.target.value
    setSelectedPlant(newPlantId)

    // find the selected plant object from your plants array
    const plantObj = plants.find((p) => p.id === newPlantId)
    if (plantObj) {
      // persist exactly the same key & shape you've used elsewhere:
      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({ id: plantObj.id, name: plantObj.name }),
      )
      // notify Redux that plant (or site/plant combination) changed:
      dispatch(setSitePlantChange({ sitePlantChange: true }))
    }
  }
  const handleVertChange = (e) => {
    const newVId = e.target.value
    setSelectedVertical(newVId)

    const vert = verticals.find((v) => v.id === newVId)
    if (vert) {
      localStorage.setItem('verticalId', vert.id)
      localStorage.setItem('selectedVertical', JSON.stringify(vert))
      dispatch(
        setVerticalChange({
          selectedVertical: vert.name,
          selectedSite: '',
          selectedPlant: '',
        }),
      )
    }
  }
  useEffect(() => {
    if (!selectedVertical) return
    const vert = verticals.find((v) => v.id === selectedVertical)
    if (!vert) return

    localStorage.setItem('verticalId', vert.id)
    localStorage.setItem('selectedVertical', JSON.stringify(vert))
    dispatch(
      setVerticalChange({
        selectedVertical: vert.name,
        selectedSite: '',
        selectedPlant: '',
      }),
    )
  }, [selectedVertical, verticals, dispatch])
  const handleSiteChange = (e) => {
    const newSiteId = e.target.value
    setSelectedSite(newSiteId)
    const site = sites.find((s) => s.id === newSiteId)
    if (site) {
      localStorage.setItem(
        'selectedSite',
        JSON.stringify({ id: site.id, name: site.name }),
      )
      localStorage.setItem('selectedSiteId', site.id)
      setSelectedPlant('')
      dispatch(
        setSitePlantChange({
          selectedSite: site.name,
          selectedPlant: '',
          sitePlantChange: true,
        }),
      )
    }
  }

  return (
    <>
      <Box sx={{ ml: 3, mt: 0 }}>
        {true && <img src={Logo} alt='RIL Logo' style={{ height: 40 }} />}
      </Box>
      <Box sx={{ ml: 1, mt: 0 }}>
        {true && (
          <Typography
            variant='body1'
            color='white'
            className='custom-title-font'
          >
            {screenTitleName}
          </Typography>
        )}
      </Box>

      {matchesXs ? <Search /> : <Box sx={{ width: '100%', ml: 1 }} />}

      <Stack direction='row' spacing={2} alignItems='center'>
        {/* Year Selector */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' className='custom-title-dropdown'>
            Year:
          </Typography>
          <FormControl sx={{ minWidth: 100 }}>
            <Select
              value={selectedYear}
              onChange={handleYearChange}
              className='custom-title-dropdown-content'
            >
              {aopYears.map((y) => (
                <MenuItem key={y.AOPYear} value={y.AOPYear}>
                  {y.AOPDisplayYear}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        {/* Vertical */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' className='custom-title-dropdown'>
            Vertical:
          </Typography>
          <FormControl sx={{ minWidth: 100 }}>
            <Select
              value={selectedVertical}
              onChange={handleVertChange}
              className='custom-title-dropdown-content'
            >
              {verticals.map((v) => (
                <MenuItem key={v.id} value={v.id}>
                  {v.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        {/* Site */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' className='custom-title-dropdown'>
            Site:
          </Typography>
          <FormControl sx={{ minWidth: 100 }}>
            <Select
              value={selectedSite}
              onChange={handleSiteChange}
              disabled={!sites.length}
              className='custom-title-dropdown-content'
            >
              {sites.map((s) => (
                <MenuItem key={s.id} value={s.id}>
                  {s.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        {/* Plant */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' className='custom-title-dropdown'>
            Plant:
          </Typography>
          <FormControl sx={{ minWidth: 100 }}>
            <Select
              value={selectedPlant}
              onChange={handlePlantChange}
              disabled={!plants.length}
              className='custom-title-dropdown-content'
            >
              {plants.map((p) => (
                <MenuItem key={p.id} value={p.id}>
                  {p.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      </Stack>

      {!matchesXs ? <Profile keycloak={keycloak} /> : <MobileSection />}
    </>
  )
}
