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
  setSiteID,
  setPlantID,
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

  const [aopYears, setAopYears] = useState([])
  const [selectedYear, setSelectedYear] = useState('')

  const screenTitle = useSelector((s) => s.dataGridStore.screenTitle)
  const screenTitleName = screenTitle?.title || 'Business Demand'

  const [allowedMap, setAllowedMap] = useState({})
  const [fullDetails, setFullDetails] = useState([])

  const [verticals, setVerticals] = useState([])
  const [sites, setSites] = useState([])
  const [plants, setPlants] = useState([])

  const [selectedVertical, setSelectedVertical] = useState('')
  const [selectedSite, setSelectedSite] = useState('')
  const [selectedPlant, setSelectedPlant] = useState('')

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

  useEffect(() => {
    if (!fullDetails.length || !Object.keys(allowedMap).length) return

    const avail = fullDetails
      .filter((v) => allowedMap[v.id])
      .map((v) => ({ id: v.id, name: v.displayName }))

    setVerticals(avail)

    if (!selectedVertical && avail.length) {
      const defV = avail[0]
      setSelectedVertical(defV.id)

      localStorage.setItem('verticalId', defV.id)
      localStorage.setItem(
        'selectedVertical',
        JSON.stringify({ id: defV.id, name: defV.name }),
      )

      dispatch(
        setVerticalChange({
          selectedVertical: defV.name,
          selectedSite: '',
          selectedPlant: '',
        }),
      )
    }
  }, [fullDetails, allowedMap, selectedVertical, dispatch])

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

      dispatch(setSiteID({ siteId: defS.id }))

      dispatch(setSitePlantChange({ sitePlantChange: true }))
    }
  }, [selectedVertical, fullDetails, allowedMap, dispatch])

  useEffect(() => {
    if (!selectedSite) {
      setPlants([])
      setSelectedPlant('')
      return
    }
    const vertObj = fullDetails.find((v) => v.id === selectedVertical)

    // console.log('vertObj', vertObj)

    const siteObj = vertObj?.sites.find((s) => s.id === selectedSite)

    // console.log('siteObj', siteObj)

    const allowedPlants = allowedMap[selectedVertical]?.[selectedSite] || []

    // console.log('allowedPlants', allowedPlants)

    const list = (siteObj?.plants || [])
      .filter((p) => allowedPlants.includes(p.id))
      .map((p) => ({ id: p.id, name: p.displayName }))

    setPlants(list)

    // console.log('list', list)

    if (list.length) {
      const defP = list[0]
      setSelectedPlant(defP.id)
      // console.log('defP.id', defP.id)
      // console.log('defP.name', defP.name)

      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({ id: defP.id, name: defP.name }),
      )
      dispatch(setSitePlantChange({ sitePlantChange: true }))
      dispatch(setPlantID({ plantId: defP.id }))
    }
  }, [selectedSite, selectedVertical, fullDetails, allowedMap, dispatch])

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

  const handleYearChange = (e) => {
    const newYear = e.target.value
    setSelectedYear(newYear)

    localStorage.setItem('year', newYear)
    dispatch(setYearChange({ yearChanged: true }))
    dispatch(setAopYear({ selectedYear: newYear }))

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

  const handlePlantChange = (e) => {
    const newPlantId = e.target.value
    setSelectedPlant(newPlantId)

    const plantObj = plants.find((p) => p.id === newPlantId)
    if (plantObj) {
      localStorage.setItem(
        'selectedPlant',
        JSON.stringify({ id: plantObj.id, name: plantObj.name }),
      )

      dispatch(setSitePlantChange({ sitePlantChange: true }))
      dispatch(setPlantID({ plantId: plantObj.id }))
    }
  }
  const handleVertChange = (e) => {
    const newVId = e.target.value

    // Immediately clear dependent selections
    setSelectedSite('')
    setSelectedPlant('')

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

      localStorage.setItem('selectedSiteId', JSON.stringify({ id: site?.id }))

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
