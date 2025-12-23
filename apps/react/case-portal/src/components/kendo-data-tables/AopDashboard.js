import { Chip } from '@progress/kendo-react-buttons'
import {
  Card,
  CardHeader,
  CardTitle,
  CardBody,
} from '@progress/kendo-react-layout'
import React from 'react'
import { useDispatch } from 'react-redux'
import { setVerticalChangeFromDashboard } from 'store/reducers/dataGridStore'
import '../../dashboard.css'

import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { BusinessDemandDataApiService } from 'services/business-demand-data-api-service'
import { getRoleName } from 'services/role-service'
import { useSession } from 'SessionStoreContext'

import { useNavigate } from 'react-router-dom'

/* ---------------- DATA ---------------- */

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

/* ---------------- STATUS → COLOR ---------------- */

const getStatusStyle = (status) => {
  switch (status) {
    case 'Development':
      return { backgroundColor: '#e0e0e0', color: '#000' }
    case 'Pre UAT':
      return { backgroundColor: '#ffb74d', color: '#000' }
    case 'UAT':
      return { backgroundColor: '#64b5f6', color: '#000' }
    case 'Go Live':
      return { backgroundColor: '#2e7d32', color: '#fff' }
    default:
      return { backgroundColor: '#e0e0e0', color: '#000' }
  }
}

/* ---------------- COMPONENT ---------------- */

const AopDashboard = () => {
  const dispatch = useDispatch()
  const keycloak = useSession()

  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear

  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()

  const IS_PE_PP_VERTICAL = lowerVertName === 'pp' || lowerVertName === 'pe'
  const IS_PTA_VERTICAL = lowerVertName === 'pta'
  const IS_PET_VERTICAL = lowerVertName === 'pet'

  const SCREEN_NAME = screenTitle?.title
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const headerMap = generateHeaderNames(AOP_YEAR)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const navigate = useNavigate()

  const handleChipClick = (id) => {
    dispatch(
      setVerticalChangeFromDashboard({
        id,
        trigger: Date.now(),
      }),
    )
  }

  const fetchData = async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      var data = await BusinessDemandDataApiService.getDashboardData(
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
    fetchData()
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak])

  const getStatusClass = (status) => {
    switch (status) {
      case 'Development':
        return 'chip-development'
      case 'Pre UAT':
        return 'chip-pre-uat'
      case 'UAT':
        return 'chip-uat'
      case 'Go Live':
        return 'chip-go-live'
      default:
        return 'chip-development'
    }
  }

  return (
    <div className='dashboard-root'>
      <h2 className='dashboard-title'>Digital AOP Dashboard</h2>

      {data.map((section) => (
        <Card key={section.plant} className='plant-card'>
          <CardHeader className='plant-card-header'>
            <CardTitle className='plant-card-title'>{section.plant}</CardTitle>
          </CardHeader>

          <CardBody className='plant-card-body'>
            <div className='plant-grid'>
              {section.rows.map((row) => (
                <div key={row.id} className='plant-tile'>
                  <div className='plant-tile-title'>{row.name}</div>

                  <Chip
                    text={row.status}
                    onClick={() => handleChipClick(row.id)}
                    className={`status-chip ${getStatusClass(row.status)}`}
                  />
                </div>
              ))}
            </div>
          </CardBody>
        </Card>
      ))}
    </div>
  )
}

export default AopDashboard
