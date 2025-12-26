import { Chip } from '@progress/kendo-react-buttons'
import {
  Card,
  CardBody,
  CardHeader,
  CardTitle,
} from '@progress/kendo-react-layout'
import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { setVerticalChangeFromDashboard } from 'store/reducers/dataGridStore'
import '../../dashboard.css'

import { Box, Grid, Stack, Typography } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { BusinessDemandDataApiService } from 'services/business-demand-data-api-service'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

/* ---------------- DATA (unchanged IDs & data) ---------------- */
const ID_MAP = {
  PET: '343EE904-E809-4201-92C5-13FEC09CE091',
  CRUDE: '905BEC3F-EBE6-4C43-BC09-1724901BBA86',
  ELASTOMER: 'E7EA9AEC-A2F2-4F06-8370-2E298BA8FAAC',
  PTA: '77355617-C31D-457E-B886-42A02B8CC808',
  PE: 'BF5D7508-96EB-496E-BEB0-4828CB1A1B11',
  AROMATICS: '96C448F9-645C-4604-A4D5-6EE854B40F26',
  Cracker: '90A693BE-9709-4C8E-9EA2-884AA8A60063',
  Maintenance: '3A9D6A3D-B7A5-41E4-86C8-8947476E4A54',
  PVC: '4411270B-AA0F-466F-8CB7-8D4C0C3A740D',
  CPP: 'C14A03AE-FAB3-4B64-8D40-9CD4C69BF763',
  MEG: '5CC84A47-9717-4142-8E66-B60EBE0CF703',
  PP: 'F928E832-BC0A-4783-8206-DFD064EAD8F7',
  VCM: '261E1737-AE3C-4F57-AEA8-FACF33A89996',
}

const data = [
  {
    plant: 'NMD',
    rows: [
      { id: ID_MAP.MEG, name: 'MEG', status: 'Go Live' },
      { id: ID_MAP.PE, name: 'PE', status: 'Pre UAT' },
      { id: ID_MAP.PP, name: 'PP', status: 'Pre UAT' },
      { id: ID_MAP.Cracker, name: 'Cracker', status: 'UAT' },
      { id: ID_MAP.CPP, name: 'CPP', status: 'Development' },
    ],
  },
  {
    plant: 'HMD',
    rows: [
      { id: ID_MAP.PVC, name: 'PVC', status: 'Development' },
      { id: ID_MAP.PE, name: 'PE', status: 'Pre UAT' },
      { id: ID_MAP.MEG, name: 'MEG', status: 'Go Live' },
      { id: ID_MAP.PP, name: 'PP', status: 'Pre UAT' },
      { id: ID_MAP.ELASTOMER, name: 'ELASTOMER', status: 'Development' },
      { id: ID_MAP.PTA, name: 'PTA', status: 'Development' },
    ],
  },
  {
    plant: 'DMD',
    rows: [
      { id: ID_MAP.Maintenance, name: 'Maintenance', status: 'Development' },
      { id: ID_MAP.PVC, name: 'PVC', status: 'Development' },
      { id: ID_MAP.MEG, name: 'MEG', status: 'Go Live' },
      { id: ID_MAP.PTA, name: 'PTA', status: 'Development' },
      { id: ID_MAP.VCM, name: 'VCM', status: 'Development' },
      { id: ID_MAP.PET, name: 'PET', status: 'Development' },
      { id: ID_MAP.PE, name: 'PE', status: 'Pre UAT' },
      { id: ID_MAP.Cracker, name: 'Cracker', status: 'UAT' },
    ],
  },
  {
    plant: 'VMD',
    rows: [
      { id: ID_MAP.PE, name: 'PE', status: 'Pre UAT' },
      { id: ID_MAP.Maintenance, name: 'Maintenance', status: 'Development' },
      { id: ID_MAP.Cracker, name: 'Cracker', status: 'UAT' },
      { id: ID_MAP.ELASTOMER, name: 'ELASTOMER', status: 'Development' },
      { id: ID_MAP.PP, name: 'PP', status: 'Pre UAT' },
      { id: ID_MAP.MEG, name: 'MEG', status: 'Go Live' },
    ],
  },
  {
    plant: 'C2',
    rows: [
      { id: ID_MAP.PE, name: 'PE', status: 'Pre UAT' },
      { id: ID_MAP.MEG, name: 'MEG', status: 'Go Live' },
    ],
  },
  {
    plant: 'JMD',
    rows: [
      { id: ID_MAP.Maintenance, name: 'Maintenance', status: 'Development' },
      { id: ID_MAP.PE, name: 'PE', status: 'Pre UAT' },
    ],
  },
  {
    plant: 'DTA',
    rows: [
      { id: ID_MAP.PP, name: 'PP', status: 'Pre UAT' },
      { id: ID_MAP.AROMATICS, name: 'AROMATICS', status: 'Pre UAT' },
    ],
  },
]

/* ---------------- STATUS → STYLE ---------------- */
const getStatusStyle = (status) => {
  const styles = {
    Development: {
      backgroundColor: '#fed7aa',
      color: '#92400e',
      borderColor: '#f97316',
    },
    'Pre UAT': {
      backgroundColor: '#fef08a',
      color: '#92400e',
      borderColor: '#eab308',
    },
    UAT: {
      backgroundColor: '#c7d2fe',
      color: '#3730a3',
      borderColor: '#6366f1',
    },
    'Go Live': {
      backgroundColor: '#a7f3d0',
      color: '#065f46',
      borderColor: '#10b981',
    },
  }
  return styles[status] || { backgroundColor: '#e2e8f0', color: '#1e293b' }
}

/* ---------- Helper: get counts ---------- */
const statusKeys = ['Go Live', 'Development', 'Pre UAT', 'UAT', 'Other']

function computeStatusCounts(rows = []) {
  const counts = {
    'Go Live': 0,
    Development: 0,
    'Pre UAT': 0,
    UAT: 0,
    Other: 0,
  }
  rows.forEach((r) => {
    if (counts.hasOwnProperty(r.status)) counts[r.status]++
    else counts.Other++
  })
  return counts
}

/* ---------------- COMPONENT ---------------- */
export default function AopDashboardCompact() {
  const dispatch = useDispatch()
  const keycloak = useSession()

  const dataGridStore = useSelector((state) => state.dataGridStore || {})
  const {
    yearChanged,
    oldYear,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [fullDetails, setFullDetails] = useState([])
  const [allowedMap, setAllowedMap] = useState({})
  const [verticals, setVerticals] = useState([])

  // Add this animation trigger when a row is clicked:
  const handleChipClick = (id) => {
    const hasAccess = verticals.some((v) => v.id === id)

    if (!hasAccess) {
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Access Denied!', severity: 'error' })
      return
    }

    // Add pulse animation feedback
    const element = event.currentTarget
    element.style.animation = 'pulse 0.4s ease-out'
    setTimeout(() => {
      element.style.animation = 'none'
    }, 400)

    dispatch(setVerticalChangeFromDashboard({ id, trigger: Date.now() }))
  }
  useEffect(() => {
    if (!fullDetails.length || !Object.keys(allowedMap).length) return

    const avail = fullDetails
      .filter((v) => allowedMap[v.id])
      .map((v) => ({ id: v.id, name: v.displayName }))

    setVerticals(avail)
  }, [fullDetails, allowedMap])

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

  const fetchAllSites = async () => {
    try {
      let parsed = []
      try {
        parsed = JSON.parse(keycloak.idTokenParsed.plants)
      } catch (e) {
        console.error('Token parse error', e)
      }
      setAllowedMap(parseAllowed(parsed))
      const data = await DataService.getAllSites(keycloak)
      setFullDetails(data || [])
    } catch (error) {
      console.error('Error fetching data', error)
      setFullDetails([])
    }
  }

  const fetchData = async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      var d = await BusinessDemandDataApiService.getDashboardData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAllSites()
    fetchData()
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak])

  return (
    <Box className='dashboard-root'>
      <Typography className='dashboard-title'>Digital AOP Dashboard</Typography>

      <Grid container spacing={1}>
        {data.map((section) => {
          const total = section.rows.length
          const counts = computeStatusCounts(section.rows)

          return (
            <Grid item xs={12} sm={6} md={4} lg={3} key={section.plant}>
              <Card className='plant-card'>
                <CardHeader className='plant-card-header'>
                  <CardTitle className='plant-card-title'>
                    {section.plant}
                  </CardTitle>

                  {/* DETAILS ROW: total + status breakdown */}
                  <div className='section-details'>
                    <div className='detail-pill'>
                      <strong>{total}</strong>
                      <span className='detail-label'> Verticals</span>
                    </div>

                    <div className='status-breakdown'>
                      {statusKeys.map((key) => {
                        const c = counts[key] || 0
                        const style = getStatusStyle(key === 'Other' ? '' : key)
                        return (
                          <div
                            key={key}
                            className='status-pill'
                            style={{
                              background: style.backgroundColor,
                              color: style.color,
                            }}
                            title={`${key}: ${c}`}
                          >
                            <span className='status-label'>{key}</span>
                            <span className='status-count'>{c}</span>
                          </div>
                        )
                      })}
                    </div>
                  </div>
                </CardHeader>

                <CardBody className='plant-card-body'>
                  <Stack spacing={0.5}>
                    {section.rows.map((row) => (
                      <Stack
                        key={row.id}
                        direction='row'
                        alignItems='center'
                        justifyContent='space-between'
                        className='plant-row'
                        onClick={() => handleChipClick(row.id)}
                      >
                        <Typography className='plant-name'>
                          {row.name}
                        </Typography>

                        <Chip
                          text={row.status}
                          size='small'
                          className='small-chip'
                          style={getStatusStyle(row.status)}
                        />
                      </Stack>
                    ))}
                  </Stack>
                </CardBody>
              </Card>
            </Grid>
          )
        })}
      </Grid>

      <Notification
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}
