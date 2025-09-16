import {
  Box,
  FormControl,
  MenuItem,
  Select,
  Skeleton,
  Stack,
  Typography,
  useMediaQuery,
} from '@mui/material'
import Logo from 'assets/images/ril-logo2.png'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import {
  setAopYear,
  setCurrentYear,
  setOldYear,
  setPlantID,
  setSiteID,
  setSitePlantChange,
  setVerticalChange,
  setYearChange,
} from 'store/reducers/dataGridStore'
import MobileSection from './MobileSection'
import Profile from './Profile/index'
import Search from './Search'

const STORAGE_KEYS = {
  SELECTED_VERTICAL: 'selectedVertical',
  SELECTED_SITE: 'selectedSite',
  SELECTED_SITE_ID: 'selectedSiteId',
  SELECTED_PLANT: 'selectedPlant',
  VERTICAL_ID: 'verticalId',
  YEAR: 'year',
}

const parseAllowed = (raw) => {
  if (!Array.isArray(raw)) return {}

  return raw.reduce((map, vObj) => {
    const vid = Object.keys(vObj)[0]
    if (!vid) return map

    map[vid] = vObj[vid].reduce((siteMap, siteObj) => {
      const sid = Object.keys(siteObj)[0]
      if (sid) siteMap[sid] = siteObj[sid]
      return siteMap
    }, {})

    return map
  }, {})
}

const getStorageItem = (key, defaultValue = null) => {
  try {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : defaultValue
  } catch (error) {
    console.error(`Error parsing localStorage item ${key}:`, error)
    return defaultValue
  }
}

const setStorageItem = (key, value) => {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (error) {
    console.error(`Error setting localStorage item ${key}:`, error)
  }
}

export default function HeaderContent({ keycloak }) {
  const dispatch = useDispatch()
  const matchesXs = useMediaQuery((theme) => theme.breakpoints.down('md'))

  const screenTitle = useSelector((s) => s.dataGridStore.screenTitle)
  const screenTitleName = screenTitle?.title

  const [headerLoading, setHeaderLoading] = useState(false)
  const [aopYears, setAopYears] = useState([])
  const [selectedYear, setSelectedYear] = useState('')
  const [allowedMap, setAllowedMap] = useState({})
  const [fullDetails, setFullDetails] = useState([])

  const initialVertical = getStorageItem(STORAGE_KEYS.SELECTED_VERTICAL)
  const [selectedVertical, setSelectedVertical] = useState(
    initialVertical?.id || '',
  )
  const [selectedSite, setSelectedSite] = useState('')
  const [selectedPlant, setSelectedPlant] = useState('')

  const verticals = useMemo(() => {
    if (!fullDetails.length || !Object.keys(allowedMap).length) return []

    return fullDetails
      .filter((v) => allowedMap[v.id])
      .map((v) => ({ id: v.id, name: v.displayName }))
  }, [fullDetails, allowedMap])

  const sites = useMemo(() => {
    if (!selectedVertical || !fullDetails.length) return []

    const vertObj = fullDetails.find((v) => v.id === selectedVertical)
    const allowedSites = allowedMap[selectedVertical] || {}

    return (vertObj?.sites || [])
      .filter((s) => allowedSites[s.id])
      .map((s) => ({ id: s.id, name: s.displayName }))
  }, [selectedVertical, fullDetails, allowedMap])

  const plants = useMemo(() => {
    if (!selectedSite || !selectedVertical || !fullDetails.length) return []

    const vertObj = fullDetails.find((v) => v.id === selectedVertical)
    const siteObj = vertObj?.sites.find((s) => s.id === selectedSite)
    const allowedPlants = allowedMap[selectedVertical]?.[selectedSite] || []

    return (siteObj?.plants || [])
      .filter((p) => allowedPlants.includes(p.id))
      .map((p) => ({ id: p.id, name: p.displayName }))
  }, [selectedSite, selectedVertical, fullDetails, allowedMap])

  useEffect(() => {
    if (!keycloak?.idTokenParsed?.plants) return

    try {
      const parsed = JSON.parse(keycloak.idTokenParsed.plants)
      setAllowedMap(parseAllowed(parsed))
    } catch (error) {
      console.error('Token parse error:', error)
      setAllowedMap({})
    }
  }, [keycloak?.idTokenParsed?.plants])

  const fetchYears = useCallback(async () => {
    try {
      const resp = await DataService.getAopyears(keycloak)
      if (resp?.length) {
        setAopYears(resp)

        const currentYear = resp.find((item) => item.currentYear == 1)?.AOPYear
        console.log('currentYear', currentYear)
        if (currentYear) {
          setSelectedYear(currentYear)
          setStorageItem(STORAGE_KEYS.YEAR, currentYear)
          dispatch(setAopYear({ selectedYear: currentYear }))
          dispatch(setOldYear({ oldYear: 0 }))
        }
      }
    } catch (error) {
      console.error('Error fetching AOP years:', error)
    }
  }, [keycloak, dispatch])

  const fetchSites = useCallback(async () => {
    try {
      const data = await DataService.getAllSites(keycloak)
      setFullDetails(data || [])
    } catch (error) {
      console.error('Error fetching sites:', error)
      setFullDetails([])
    }
  }, [keycloak])

  useEffect(() => {
    const initializeData = async () => {
      setHeaderLoading(true)
      try {
        await Promise.all([fetchYears(), fetchSites()])
      } catch (error) {
        console.error('Error initializing data:', error)
      } finally {
        setHeaderLoading(false)
      }
    }

    if (keycloak) {
      initializeData()
    }
  }, [keycloak, fetchYears, fetchSites])

  useEffect(() => {
    if (!verticals.length || selectedVertical) return

    const defaultVertical = verticals[0]
    setSelectedVertical(defaultVertical.id)

    setStorageItem(STORAGE_KEYS.VERTICAL_ID, defaultVertical.id)
    setStorageItem(STORAGE_KEYS.SELECTED_VERTICAL, defaultVertical)

    dispatch(
      setVerticalChange({
        selectedVertical: defaultVertical.name,
        selectedSite: '',
        selectedPlant: '',
      }),
    )
  }, [verticals, selectedVertical, dispatch])

  useEffect(() => {
    if (!sites.length) {
      setSelectedSite('')
      return
    }

    const defaultSite = sites[0]
    setSelectedSite(defaultSite.id)

    setStorageItem(STORAGE_KEYS.SELECTED_SITE, defaultSite)
    setStorageItem(STORAGE_KEYS.SELECTED_SITE_ID, { id: defaultSite.id })

    dispatch(setSiteID({ siteId: defaultSite.id }))
    dispatch(setSitePlantChange({ sitePlantChange: true }))
  }, [sites, dispatch])

  useEffect(() => {
    if (!plants.length) {
      setSelectedPlant('')
      return
    }

    const defaultPlant = plants[0]
    setSelectedPlant(defaultPlant.id)

    setStorageItem(STORAGE_KEYS.SELECTED_PLANT, defaultPlant)
    dispatch(setSitePlantChange({ sitePlantChange: true }))
    dispatch(
      setPlantID({ plantId: defaultPlant.id, plantName: defaultPlant.name }),
    )
  }, [plants, dispatch])

  const handleYearChange = useCallback(
    (e) => {
      const newYear = e.target.value
      setSelectedYear(newYear)
      setStorageItem(STORAGE_KEYS.YEAR, newYear)

      dispatch(setYearChange({ yearChanged: true }))
      dispatch(setAopYear({ selectedYear: newYear }))

      const selectedYearObj = aopYears.find((y) => y.AOPYear === newYear)
      const isCurrentYear = selectedYearObj?.currentYear == 1

      dispatch(setCurrentYear({ currentYear: isCurrentYear ? 1 : 0 }))

      const currentYear = aopYears.find((y) => y.currentYear == 1)
      if (currentYear) {
        const [currentStartYear] = currentYear.AOPYear.split('-').map(Number)
        const [selectedStartYear] = newYear.split('-').map(Number)
        const isOldYear = selectedStartYear < currentStartYear ? 1 : 0
        dispatch(setOldYear({ oldYear: isOldYear }))
      }
    },
    [aopYears, dispatch],
  )

  const handleVerticalChange = useCallback(
    (e) => {
      const newVerticalId = e.target.value

      setSelectedSite('')
      setSelectedPlant('')
      setSelectedVertical(newVerticalId)

      const vertical = verticals.find((v) => v.id === newVerticalId)
      if (vertical) {
        setStorageItem(STORAGE_KEYS.VERTICAL_ID, vertical.id)
        setStorageItem(STORAGE_KEYS.SELECTED_VERTICAL, vertical)

        dispatch(
          setVerticalChange({
            selectedVertical: vertical.name,
            selectedSite: '',
            selectedPlant: '',
          }),
        )
      }
    },
    [verticals, dispatch],
  )

  const handleSiteChange = useCallback(
    (e) => {
      const newSiteId = e.target.value
      setSelectedSite(newSiteId)

      const site = sites.find((s) => s.id === newSiteId)
      if (site) {
        setStorageItem(STORAGE_KEYS.SELECTED_SITE, site)
        setStorageItem(STORAGE_KEYS.SELECTED_SITE_ID, { id: site.id })

        dispatch(
          setSitePlantChange({
            selectedSite: site.name,
            selectedPlant: '',
            sitePlantChange: true,
          }),
        )
      }
    },
    [sites, dispatch],
  )

  const handlePlantChange = useCallback(
    (e) => {
      const newPlantId = e.target.value
      setSelectedPlant(newPlantId)

      const plant = plants.find((p) => p.id === newPlantId)
      if (plant) {
        setStorageItem(STORAGE_KEYS.SELECTED_PLANT, plant)
        dispatch(setSitePlantChange({ sitePlantChange: true }))
        dispatch(setPlantID({ plantId: plant.id, plantName: plant.name }))
      }
    },
    [plants, dispatch],
  )

  const DropdownSelector = ({
    label,
    value,
    onChange,
    options,
    disabled = false,
    loading = false,
  }) => (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <Typography variant='body1' className='custom-title-dropdown'>
        {label}:
      </Typography>
      {loading ? (
        <Skeleton variant='rectangle' width={100} height={40} />
      ) : (
        <FormControl sx={{ minWidth: 100 }}>
          <Select
            value={value}
            onChange={onChange}
            disabled={disabled || !options.length}
            className='custom-title-dropdown-content'
          >
            {options.map((option) => (
              <MenuItem key={option.id} value={option.id}>
                {option.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      )}
    </Box>
  )
  // console.log('selectedYear', selectedYear)
  return (
    <>
      <Box sx={{ ml: 3, mt: 0 }}>
        <img src={Logo} alt='RIL Logo' style={{ height: 40 }} />
      </Box>

      <Box sx={{ ml: 1, mt: 0 }}>
        <Typography variant='body1' color='white' className='custom-title-font'>
          {screenTitleName}
        </Typography>
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
              {aopYears.map((year) => (
                <MenuItem key={year.AOPYear} value={year.AOPYear}>
                  {year.AOPDisplayYear}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        <DropdownSelector
          label='Vertical'
          value={selectedVertical}
          onChange={handleVerticalChange}
          options={verticals}
          loading={headerLoading}
        />

        <DropdownSelector
          label='Site'
          value={selectedSite}
          onChange={handleSiteChange}
          options={sites}
          loading={headerLoading}
          disabled={!sites.length}
        />

        <DropdownSelector
          label='Plant'
          value={selectedPlant}
          onChange={handlePlantChange}
          options={plants}
          loading={headerLoading}
          disabled={!plants.length}
        />
      </Stack>

      {!matchesXs ? <Profile keycloak={keycloak} /> : <MobileSection />}
    </>
  )
}
