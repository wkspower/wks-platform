import {
  Box,
  FormControl,
  MenuItem,
  Select,
  Stack,
  Typography,
  useMediaQuery,
} from '@mui/material'
import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import {
  setAopYear,
  setCurrentYear,
  setOldYear,
  setPlantID,
  setPlantObject,
  setSiteID,
  setSiteObject,
  setSitePlantChange,
  setVerticalChange,
  setVerticalObject,
  setYearChange,
} from 'store/reducers/dataGridStore'
import MobileSection from './MobileSection'
import Profile from './Profile/index'

// import Logo from '../../../assets/images/ril-logo2.png'
import Logo from 'assets/images/ril-logo2.png'
import DropdownSkeleton from 'utils/DropdownSkeleton'
// import { Skeleton } from '../../../../../node_modules/@progress/kendo-react-indicators/index'

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
  const [headerLoading, setHeaderLoading] = useState(false)
  const getSelectedVerticalStorage = localStorage.getItem('selectedVertical')
    ? JSON.parse(localStorage.getItem('selectedVertical'))
    : null
  const dispatch = useDispatch()
  const matchesXs = useMediaQuery((theme) => theme.breakpoints.down('md'))

  const [aopYears, setAopYears] = useState([])
  const [selectedYear, setSelectedYear] = useState('')

  const screenTitle = useSelector((s) => s.dataGridStore.screenTitle)
  const screenTitleName = screenTitle?.title

  const [allowedMap, setAllowedMap] = useState({})
  const [fullDetails, setFullDetails] = useState([])

  const [verticals, setVerticals] = useState([])
  const [sites, setSites] = useState([])
  const [plants, setPlants] = useState([])

  const [selectedVertical, setSelectedVertical] = useState(
    getSelectedVerticalStorage ? getSelectedVerticalStorage.id : '',
  )
  const [selectedSite, setSelectedSite] = useState('')
  const [selectedPlant, setSelectedPlant] = useState('')

  const HIDE_VERTICAL_DROPDOWN =
    keycloak?.realmAccess?.roles?.includes('maintenance_users')

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

  const fetchAllSites = async () => {
    setHeaderLoading(true)
    try {
      const data = await DataService.getAllSites(keycloak)
      setFullDetails(data || [])
    } catch (error) {
      console.error('Error fetching data', error)
      setFullDetails([])
    } finally {
      // setTimeout(() => {
      setHeaderLoading(false)
      // }, 2000)
    }
  }

  useEffect(() => {
    fetchAllSites()
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

      dispatch(setVerticalObject({ id: defV.id, name: defV.name }))
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

      dispatch(
        setSiteObject({
          id: defS.id,
          name: defS.displayName ?? defS.name ?? '',
        }),
      )

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

      dispatch(
        setPlantObject({
          id: defP.id,
          name: defP.displayName ?? defP.name ?? '',
        }),
      )

      dispatch(setSitePlantChange({ sitePlantChange: true }))
      dispatch(setPlantID({ plantId: defP.id, plantName: defP.name }))
    }
  }, [selectedSite, selectedVertical, fullDetails, allowedMap, dispatch])

  useEffect(() => {
    async function fetchYears() {
      // setHeaderLoading(true)
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
            dispatch(setOldYear({ oldYear: 0 }))
          }
        }
      } catch (err) {
        console.error('Error fetching data', err)
      } finally {
        // setHeaderLoading(false)
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

      dispatch(
        setPlantObject({
          id: plantObj.id,
          name: plantObj.name ?? plantObj.name ?? '',
        }),
      )

      dispatch(setSitePlantChange({ sitePlantChange: true }))
      dispatch(setPlantID({ plantId: plantObj.id, plantName: plantObj.name }))
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

      dispatch(setVerticalObject({ id: vert.id, name: vert.name }))

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

    dispatch(setVerticalObject({ id: vert.id, name: vert.name }))

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

      dispatch(
        setSiteObject({ id: site.id, name: site.name ?? site.name ?? '' }),
      )

      localStorage.setItem('selectedSiteId', JSON.stringify({ id: site?.id }))

      dispatch(
        setSiteObject({ id: site.id, name: site.name ?? site.name ?? '' }),
      )

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

  const verticalFromDashboard = useSelector(
    (state) => state.dataGridStore.verticalChangeFromDashboard,
  )

  useEffect(() => {
    if (verticalFromDashboard?.id) {
      setSelectedVertical('')
      handleVertChange({
        target: {
          value: verticalFromDashboard.id,
        },
      })
    }
  }, [verticalFromDashboard])

  return (
    <>
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          width: '100%',
          // py: 0, // 4px top/bottom
        }}
      >
        {/* LEFT SIDE: Logo + Title */}
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Box sx={{ ml: 0 }}>
            <img src={Logo} alt='RIL Logo' style={{ height: 32 }} />
          </Box>

          <Box sx={{ ml: 1 }}>
            <Typography
              variant='body2'
              color='white'
              className='custom-title-font'
            >
              {screenTitleName}
            </Typography>
          </Box>
        </Box>

        {/* RIGHT SIDE: Dropdowns */}
        <Stack direction='row' spacing={1} alignItems='center'>
          {/* Year */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant='body2' className='custom-title-dropdown'>
              Year:
            </Typography>
            {headerLoading ? (
              <DropdownSkeleton />
            ) : (
              <FormControl sx={{ width: 80 }}>
                <Select
                  value={selectedYear}
                  onChange={handleYearChange}
                  className='custom-title-dropdown-content'
                  MenuProps={
                    ({
                      PaperProps: { style: { maxHeight: 200 } },
                    },
                    { disableScrollLock: true })
                  }
                >
                  {aopYears.map((y) => (
                    <MenuItem key={y.AOPYear} value={y.AOPYear}>
                      {y.AOPDisplayYear}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
          </Box>

          {/* Vertical */}
          {!HIDE_VERTICAL_DROPDOWN && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography variant='body2' className='custom-title-dropdown'>
                Vertical:
              </Typography>

              {headerLoading ? (
                <DropdownSkeleton />
              ) : (
                <FormControl sx={{ width: 100 }}>
                  <Select
                    value={selectedVertical}
                    onChange={handleVertChange}
                    className='custom-title-dropdown-content'
                    MenuProps={{
                      PaperProps: { style: { maxHeight: 200 } },
                      disableScrollLock: true,
                    }}
                  >
                    {verticals.map((v) => (
                      <MenuItem key={v.id} value={v.id}>
                        {v.name}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              )}
            </Box>
          )}

          {/* Site */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant='body2' className='custom-title-dropdown'>
              Site:
            </Typography>
            {headerLoading ? (
              <DropdownSkeleton />
            ) : (
              <FormControl sx={{ width: 80 }}>
                <Select
                  value={selectedSite}
                  onChange={handleSiteChange}
                  disabled={!sites.length}
                  className='custom-title-dropdown-content'
                  MenuProps={
                    ({
                      PaperProps: { style: { maxHeight: 200 } },
                    },
                    { disableScrollLock: true })
                  }
                >
                  {sites.map((s) => (
                    <MenuItem key={s.id} value={s.id}>
                      {s.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
          </Box>

          {/* Plant */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant='body2' className='custom-title-dropdown'>
              Plant:
            </Typography>
            {headerLoading ? (
              <DropdownSkeleton />
            ) : (
              <FormControl sx={{ width: 110}}>
                <Select
                  value={selectedPlant}
                  onChange={handlePlantChange}
                  disabled={!plants.length}
                  className='custom-title-dropdown-content'
                  MenuProps={
                    ({
                      PaperProps: { style: { maxHeight: 200 } },
                    },
                    { disableScrollLock: true })
                  }
                >
                  {plants.map((p) => (
                    <MenuItem key={p.id} value={p.id}>
                      {p.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
          </Box>
        </Stack>
      </Box>

      {!matchesXs ? <Profile keycloak={keycloak} /> : <MobileSection />}
    </>
  )
}
