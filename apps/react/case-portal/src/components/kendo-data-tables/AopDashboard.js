import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { Chip } from '@progress/kendo-react-buttons'
import {
  Card,
  CardBody,
  CardHeader,
  CardTitle,
} from '@progress/kendo-react-layout'
import { Box, Grid, Stack, Typography } from '@mui/material'
import { useDispatch, useSelector } from 'react-redux'
import Notification from 'components/Utilities/Notification'
import { BusinessDemandDataApiService } from 'services/business-demand-data-api-service'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { setVerticalChangeFromDashboard } from 'store/reducers/dataGridStore'
import '../../dashboard-v2.css'
import {
  Backdrop,
  CircularProgress,
} from '../../../node_modules/@mui/material/index'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'

export default function AopDashboardCompact() {
  const dispatch = useDispatch()
  const keycloak = useSession()

  // store slice
  const {
    yearChanged,
    oldYear,
    plantObject = {},
    siteObject = {},
    verticalObject = {},
    year = {},
  } = useSelector((s) => s.dataGridStore || {})

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  // local UI state
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info',
  })
  const [loading, setLoading] = useState(false)
  const [fullDetails, setFullDetails] = useState([])
  const [allowedMap, setAllowedMap] = useState({})
  const [allowedMapForSites, setAllowedMapForSites] = useState({})
  const [verticals, setVerticals] = useState([])
  const [sites, setSites] = useState([])
  const [statusData, setStatusData] = useState([])
  const [siteGroupedRows, setSiteGroupedRows] = useState([])
  const [idMap, setIdMap] = useState({})

  // ------------------ helpers ------------------
  const showSnackbar = useCallback((message, severity = 'info') => {
    setSnackbar({ open: true, message, severity })
  }, [])

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

  const buildIdMap = useCallback((details = []) => {
    return details.reduce((acc, item) => {
      if (!item?.name || !item?.id) return acc
      const key = item.name.toUpperCase().replace(/\s+/g, '_')
      acc[key] = item.id
      return acc
    }, {})
  }, [])

  // pulse animation utility
  const pulseElement = (el) => {
    if (!el) return
    el.classList.add('pulse')
    window.setTimeout(() => el.classList.remove('pulse'), 400)
  }

  // ------------------ event handlers ------------------

  const handleChipClick = useCallback(
    (event, vid, sid) => {
      setLoading(true)
      // find vertical
      const vertical = verticals.find((v) => v.vid === vid)

      // check vertical access
      if (!vertical) {
        showSnackbar('Access Denied!', 'error')
        setLoading(false)
        return
      }

      // check site access (if site is involved)
      if (sid && !vertical.sids.includes(sid)) {
        showSnackbar('Access Denied!', 'error')
        setLoading(false)
        return
      }

      // visual feedback
      pulseElement(event.currentTarget)

      console.log('sid', sid)
      console.log('vid', vid)

      dispatch(
        setVerticalChangeFromDashboard({ vid, trigger: Date.now(), sid }),
      )
    },
    [dispatch, verticals, showSnackbar],
  )

  // ------------------ data fetching ------------------

  const fetchAllSites = useCallback(async () => {
    try {
      let parsedPlants = []
      try {
        parsedPlants = JSON.parse(keycloak?.idTokenParsed?.plants || '[]')
      } catch (e) {
        console.warn('Token parse error', e)
      }

      setAllowedMap(parseAllowed(parsedPlants))

      const allSites = await DataService.getAllSites(keycloak)
      const details = allSites || []
      setFullDetails(details)
      setIdMap(buildIdMap(details))
    } catch (error) {
      console.error('Error fetching all sites', error)
      setFullDetails([])
      setIdMap({})
    }
  }, [buildIdMap, keycloak])

  const fetchDashboardData = useCallback(async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return

    setLoading(true)
    setSiteGroupedRows([])

    try {
      const res = await BusinessDemandDataApiService.getDashboardData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const apiRows = res?.data?.data || []

      setStatusData(apiRows)

      let idx = 0

      const grouped = Object.values(
        apiRows.reduce((acc, item) => {
          const site = item.site_name || 'Unknown Site'
          if (!acc[site]) acc[site] = { site, rows: [] }

          acc[site].rows.push({
            idx: idx++,
            id: idMap[item.vertical_name] ?? item.vertical_id,
            sId: item.site_id,
            verticalName: item.vertical_name,
            status: item.status,
            status_color: item.status_color,
            status_text_color: item.status_text_color,
          })

          return acc
        }, {}),
      )

      setSiteGroupedRows(grouped)
    } catch (error) {
      console.error('Error fetching dashboard data', error)
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, SITE_ID, VERTICAL_ID, AOP_YEAR, idMap, keycloak])

  // keep verticals list in sync with allowedMap + fullDetails
  useEffect(() => {
    if (!fullDetails.length || !Object.keys(allowedMap).length) return

    const result = fullDetails
      .filter((v) => allowedMap[v.id])
      .map((v) => ({
        vid: v.id,
        vname: v.displayName,
        sids: Object.keys(allowedMap[v.id]),
      }))

    setVerticals(result)
  }, [fullDetails, allowedMap])

  // initial + reactive fetches
  useEffect(() => {
    fetchAllSites()
    fetchDashboardData()
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak])

  // memoized status summary
  const statusSummary = useMemo(() => {
    const map = {}
    statusData.forEach((r) => {
      const key = r.status || 'Other'
      if (!map[key]) {
        map[key] = {
          count: 0,
          backgroundColor: r.status_color || '#e2e8f0',
          color: r.status_text_color
            ? `#${r.status_text_color.replace('#', '')}`
            : '#1e293b',
        }
      }
      map[key].count += 1
    })
    return map
  }, [statusData])

  const statusKeys = Object.keys(statusSummary)

  return (
    <div>
      <LoaderBackdrop open={!!loading} />

      <Box className='dashboard-root-v2'>
        <Typography className='dashboard-title-v2'>
          Digital AOP Dashboard
        </Typography>

        <Grid container spacing={0.8}>
          {siteGroupedRows.map((section) => {
            const total = section.rows.length

            // build per-section status summary
            const localStatusSummary = section.rows.reduce((acc, r) => {
              const key = r.status || 'Other'
              if (!acc[key]) {
                acc[key] = {
                  count: 0,
                  backgroundColor: r.status_color || '#e2e8f0',
                  color: r.status_text_color
                    ? `#${r.status_text_color.replace('#', '')}`
                    : '#1e293b',
                }
              }
              acc[key].count += 1
              return acc
            }, {})

            const localStatusKeys = Object.keys(localStatusSummary)

            return (
              <Grid item xs={12} sm={6} md={4} lg={2} key={section.site}>
                <Card className='plant-card-v2'>
                  <CardHeader className='plant-card-header-v2'>
                    <CardTitle className='plant-card-title-v2'>
                      {section.site}
                    </CardTitle>

                    <div className='section-details-v2'>
                      <div className='detail-pill-v2'>
                        <strong>{total}</strong>
                      </div>

                      <div className='status-breakdown-v2'>
                        {localStatusKeys.map((key) => {
                          const { count, backgroundColor, color } =
                            localStatusSummary[key]

                          return (
                            <div
                              key={key}
                              className='status-pill-v2'
                              style={{ background: backgroundColor, color }}
                              title={`${key}: ${count}`}
                            >
                              <span className='status-count-v2'>{count}</span>
                            </div>
                          )
                        })}
                      </div>
                    </div>
                  </CardHeader>

                  <CardBody className='plant-card-body-v2'>
                    <Stack spacing={0.2}>
                      {section.rows.map((row) => {
                        const statusStyle = localStatusSummary[row.status] || {}

                        return (
                          <Stack
                            key={`${row.sId}-${row.id}-${row.idx}`}
                            direction='row'
                            alignItems='center'
                            justifyContent='space-between'
                            className='plant-row-v2'
                            onClick={(e) => handleChipClick(e, row.id, row.sId)}
                          >
                            <Typography className='vertical-name-v2'>
                              {row.verticalName}
                            </Typography>

                            <Chip
                              text={row.status}
                              size='small'
                              className='status-chip-v2'
                              style={{
                                background: statusStyle.backgroundColor,
                                color: statusStyle.color,
                              }}
                            />
                          </Stack>
                        )
                      })}
                    </Stack>
                  </CardBody>
                </Card>
              </Grid>
            )
          })}
        </Grid>

        <Notification
          open={snackbar.open}
          message={snackbar.message || ''}
          severity={snackbar.severity || 'info'}
          onClose={() => setSnackbar((s) => ({ ...s, open: false }))}
        />
      </Box>
    </div>
  )
}
