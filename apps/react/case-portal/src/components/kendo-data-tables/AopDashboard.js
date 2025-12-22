import React from 'react'
import { Chip } from '@progress/kendo-react-buttons'
import { useDispatch } from 'react-redux'
import { setVerticalChangeFromDashboard } from 'store/reducers/dataGridStore'
import '../../dashboard.css'

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
  return (
    <div className='dashboard-container'>
      <h6 className='dashboard-header'>Digital AOP Dashboard</h6>

      {data.map((section) => (
        <div key={section.plant} className='plant-section'>
          <div className='plant-title'>{section.plant}</div>

          <div className='chip-grid'>
            {section.rows.map((row) => (
              <div key={row.id} className='chip-item'>
                <div className='chip-label'>{row.name}</div>

                <Chip
                  text={row.status}
                  onClick={() => {
                    dispatch(
                      setVerticalChangeFromDashboard({
                        id: row.id,
                      }),
                    )
                  }}
                  style={{
                    ...getStatusStyle(row.status),
                    width: '100%',
                    justifyContent: 'center',
                    fontWeight: 600,
                  }}
                />
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  )
}

export default AopDashboard
