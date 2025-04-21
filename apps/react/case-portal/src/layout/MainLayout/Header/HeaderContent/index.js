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
} from 'store/reducers/dataGridStore'
import { DataService } from 'services/DataService'
import Search from './Search'
import Profile from './Profile/index'
import MobileSection from './MobileSection'

// Utility to parse the Keycloak “allowed” JSON
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

  // --- 1. Year logic state
  const [aopYears, setAopYears] = useState([])
  const [selectedYear, setSelectedYear] = useState('')

  // --- 2. screenTitleName from Redux
  const screenTitle = useSelector((s) => s.dataGridStore.screenTitle)
  const screenTitleName = screenTitle?.title || 'Honeywell Digital AOP'

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
      console.log(defV.name)
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
        const resp = await DataService.getAopyears(keycloak)
        if (resp?.length) {
          setAopYears(resp)
          const firstYear = resp[0].AOPYear
          setSelectedYear(firstYear)

          localStorage.setItem('year', firstYear)
          dispatch(setAopYear({ selectedYear: firstYear }))
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

  return (
    <>
      <Box sx={{ ml: 3, mt: 1 }}>
        <Typography
          variant='body1'
          color='white'
          sx={{ fontWeight: 'bold', whiteSpace: 'nowrap', fontSize: '1rem' }}
        >
          {screenTitleName}
        </Typography>
      </Box>

      {matchesXs ? <Search /> : <Box sx={{ width: '100%', ml: 1 }} />}

      <Stack direction='row' spacing={2} alignItems='center'>
        {/* Year Selector */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant='body1' color='white'>
            Year:
          </Typography>
          <FormControl sx={{ minWidth: 120 }}>
            <Select
              value={selectedYear}
              onChange={handleYearChange}
              sx={{ color: 'white' }}
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
          <Typography variant='body1' color='white'>
            Vertical:
          </Typography>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedVertical}
              onChange={handleVertChange}
              sx={{ color: 'white' }}
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
          <Typography variant='body1' color='white'>
            Site:
          </Typography>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedSite}
              onChange={(e) => setSelectedSite(e.target.value)}
              disabled={!sites.length}
              sx={{ color: 'white' }}
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
          <Typography variant='body1' color='white'>
            Plant:
          </Typography>
          <FormControl sx={{ minWidth: 150 }}>
            <Select
              value={selectedPlant}
              onChange={handlePlantChange}
              disabled={!plants.length}
              sx={{ color: 'white' }}
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
