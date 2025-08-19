import React, { useEffect, useState } from 'react'
import KendoDataTables from './index'

export default function RelPerf() {
  // Reliability Performance Grid (already present)
  const reliabilityPerformanceColumns = [
    {
      field: 'serialNumber',
      title: 'S.No.',
      widthT: 70,
      editable: false,
      type: 'number',
    },
    { field: 'parameter', title: 'Parameter', editable: true },
    { field: 'uom', title: 'UOM', editable: true, widthT: 70 },
    { field: 'bestAchieved', title: 'Best Achieved', editable: true },
    { field: 'fy25Aop', title: 'FY25 AOP', editable: true },
    { field: 'fy25Actual', title: 'FY25 Actual', editable: true },
    { field: 'fy26Plan', title: 'FY26 Plan', editable: true },
    {
      field: 'rationale',
      title: 'Rationale / Reasons for Changes',
      editable: true,
    },
  ]

  const initialReliabilityRows = [
    {
      serialNumber: 1,
      parameter: 'Technical availability, YTD',
      uom: '%',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '>99%',
      rationale: '',
      id: 0,
    },
    {
      serialNumber: 2,
      parameter: 'Maintenance Effectiveness',
      uom: '%',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '>80%',
      rationale: '',
      id: 1,
    },
    {
      serialNumber: 3,
      parameter: 'Shutdown Schedule Compliance',
      uom: '%',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '100 ± 10%',
      rationale: '',
      id: 2,
    },
    {
      serialNumber: 4,
      parameter: 'Open NSD PM order backlog in weeks',
      uom: 'Weeks',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '4-6 weeks',
      rationale: '',
      id: 3,
    },
    {
      serialNumber: 5,
      parameter: 'Planned Jobs Schedule compliance',
      uom: '%',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '>96%',
      rationale: '',
      id: 4,
    },
    {
      serialNumber: 6,
      parameter: 'Inspection overdue (VitalEquipment)',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '0',
      rationale: '',
      id: 5,
    },
    {
      serialNumber: 7,
      parameter: 'Overdue reliability recommendations (NSD-APM)',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '0',
      rationale: '',
      id: 6,
    },
    {
      serialNumber: 8,
      parameter: 'Overdue IM Recommendations in E&M Discipline',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '0',
      rationale: '',
      id: 7,
    },
    {
      serialNumber: '9.1',
      parameter: 'Total no. of Asset Failures - YTD (IM count)',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '20% reduction YOY',
      rationale: '',
      id: 8,
    },
    {
      serialNumber: '9.2',
      parameter: 'Repetitive failures - YTD',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '0',
      rationale: '',
      id: 9,
    },
    {
      serialNumber: 10,
      parameter: 'JMS approval time till A2',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '7 days',
      rationale: '',
      id: 10,
    },
    {
      serialNumber: 11,
      parameter: 'Pending GRN',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '7 days',
      rationale: '',
      id: 11,
    },
  ]

  const [reliabilityRows, setReliabilityRows] = useState(initialReliabilityRows)
  const [modifiedReliabilityCells, setModifiedReliabilityCells] = useState({})
  const [remarkDialogOpenReliability, setRemarkDialogOpenReliability] =
    useState(false)
  const [currentRemarkReliability, setCurrentRemarkReliability] = useState('')
  const [currentRowIdReliability, setCurrentRowIdReliability] = useState(null)

  // Financial Aspect Grid
  const financialAspectColumns = [
    {
      field: 'serialNumber',
      title: 'S.No.',
      widthT: 70,
      editable: false,
      type: 'number',
    },
    { field: 'parameter', title: 'Parameter', editable: true },
    { field: 'uom', title: 'UOM', editable: true, widthT: 70 },
    { field: 'bestAchieved', title: 'Best Achieved', editable: true },
    { field: 'fy25Aop', title: 'FY25 AOP', editable: true },
    { field: 'fy25Actual', title: 'FY25 Actual', editable: true },
    { field: 'fy26Plan', title: 'FY26 Plan', editable: true },
    {
      field: 'rationale',
      title: 'Rationale / Reasons for Changes',
      editable: true,
    },
  ]
  const initialFinancialRows = [
    {
      serialNumber: 1,
      parameter: 'Spares- Routine',
      uom: 'Rs/Cr',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '',
      rationale: '',
      id: 0,
    },
    {
      serialNumber: 2,
      parameter: 'Spares- One Time',
      uom: 'Rs/Cr',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '',
      rationale: '',
      id: 1,
    },
    {
      serialNumber: 3,
      parameter: 'Spares- Shutdown',
      uom: 'Rs/Cr',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '',
      rationale: '',
      id: 2,
    },
    {
      serialNumber: 4,
      parameter: 'Services- Routine',
      uom: 'Rs/Cr',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '',
      rationale: '',
      id: 3,
    },
    {
      serialNumber: 5,
      parameter: 'Services- One Time',
      uom: 'Rs/Cr',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '',
      rationale: '',
      id: 4,
    },
    {
      serialNumber: 6,
      parameter: 'Services- Shutdown',
      uom: 'Rs/Cr',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '',
      rationale: '',
      id: 5,
    },
  ]
  const [financialRows, setFinancialRows] = useState(initialFinancialRows)
  const [modifiedFinancialCells, setModifiedFinancialCells] = useState({})
  const [remarkDialogOpenFinancial, setRemarkDialogOpenFinancial] =
    useState(false)
  const [currentRemarkFinancial, setCurrentRemarkFinancial] = useState('')
  const [currentRowIdFinancial, setCurrentRowIdFinancial] = useState(null)

  // Major Reliability Incidents Grid
  const majorIncidentsColumns = [
    {
      field: 'serialNumber',
      title: 'S.No.',
      widthT: 70,
      editable: false,
      type: 'number',
    },
    {
      field: 'incidentDescription',
      title: 'Incident Description',
      editable: true,
    },
    { field: 'rootCause', title: 'Root Cause Analysis', editable: true },
    { field: 'recommendation', title: 'Recommendation', editable: true },
    { field: 'targetDate', title: 'Target Date', editable: true, type: 'date' },
    { field: 'responsible', title: 'Resp.', editable: true },
  ]
  const initialMajorIncidentsRows = [
    {
      serialNumber: 1,
      incidentDescription: '',
      rootCause: '',
      recommendation: '',
      targetDate: '',
      responsible: '',
      id: 0,
    },
    {
      serialNumber: 2,
      incidentDescription: '',
      rootCause: '',
      recommendation: '',
      targetDate: '',
      responsible: '',
      id: 1,
    },
  ]
  const [majorIncidentsRows, setMajorIncidentsRows] = useState(
    initialMajorIncidentsRows,
  )
  const [modifiedMajorIncidentsCells, setModifiedMajorIncidentsCells] =
    useState({})
  const [remarkDialogOpenMajorIncidents, setRemarkDialogOpenMajorIncidents] =
    useState(false)
  const [currentRemarkMajorIncidents, setCurrentRemarkMajorIncidents] =
    useState('')
  const [currentRowIdMajorIncidents, setCurrentRowIdMajorIncidents] =
    useState(null)

  // Reliability Improvement Initiative Grid
  const reliabilityInitiativeColumns = [
    {
      field: 'serialNumber',
      title: 'S.No.',
      widthT: 70,
      editable: false,
      type: 'number',
    },
    { field: 'initiative', title: 'Initiative', editable: true },
    { field: 'outcome', title: 'Outcome', editable: true },
    { field: 'recommendation', title: 'Recommendation', editable: true },
    { field: 'targetDate', title: 'Target Date', editable: true, type: 'date' },
    { field: 'responsible', title: 'Resp.', editable: true },
  ]
  const initialReliabilityInitiativeRows = [
    {
      serialNumber: 1,
      initiative: '',
      outcome: '',
      recommendation: '',
      targetDate: '',
      responsible: '',
      id: 0,
    },
    {
      serialNumber: 2,
      initiative: '',
      outcome: '',
      recommendation: '',
      targetDate: '',
      responsible: '',
      id: 1,
    },
  ]
  const [reliabilityInitiativeRows, setReliabilityInitiativeRows] = useState(
    initialReliabilityInitiativeRows,
  )
  const [
    modifiedReliabilityInitiativeCells,
    setModifiedReliabilityInitiativeCells,
  ] = useState({})
  const [
    remarkDialogOpenReliabilityInitiative,
    setRemarkDialogOpenReliabilityInitiative,
  ] = useState(false)
  const [
    currentRemarkReliabilityInitiative,
    setCurrentRemarkReliabilityInitiative,
  ] = useState('')
  const [
    currentRowIdReliabilityInitiative,
    setCurrentRowIdReliabilityInitiative,
  ] = useState(null)

  // Permissions (reuse for all grids or customize per grid)
  const gridPermissions = {
    saveBtn: true,
    allAction: true,
    showTitleNameBusiness: true,
    adjustedPermissions: true,
    downloadExcelBtn: true,
    uploadExcelBtn: true,
  }

  return (
    <>
      {/* Reliability Performance Grid */}
      <KendoDataTables
        rows={reliabilityRows}
        setRows={setReliabilityRows}
        title='Reliability Performance'
        modifiedCells={modifiedReliabilityCells}
        setModifiedCells={setModifiedReliabilityCells}
        remarkDialogOpen={remarkDialogOpenReliability}
        setRemarkDialogOpen={setRemarkDialogOpenReliability}
        currentRemark={currentRemarkReliability}
        setCurrentRemark={setCurrentRemarkReliability}
        currentRowId={currentRowIdReliability}
        setCurrentRowId={setCurrentRowIdReliability}
        permissions={{
          ...gridPermissions,
          titleName: 'Reliability Performance',
          ExcelName: 'Reliability_Performance',
        }}
        columns={reliabilityPerformanceColumns}
      />

      {/* Financial Aspect Grid */}
      <KendoDataTables
        rows={financialRows}
        setRows={setFinancialRows}
        title='Financial Aspect'
        modifiedCells={modifiedFinancialCells}
        setModifiedCells={setModifiedFinancialCells}
        remarkDialogOpen={remarkDialogOpenFinancial}
        setRemarkDialogOpen={setRemarkDialogOpenFinancial}
        currentRemark={currentRemarkFinancial}
        setCurrentRemark={setCurrentRemarkFinancial}
        currentRowId={currentRowIdFinancial}
        setCurrentRowId={setCurrentRowIdFinancial}
        permissions={{
          ...gridPermissions,
          titleName: 'Financial Aspect',
          ExcelName: 'Financial_Aspect',
        }}
        columns={financialAspectColumns}
      />

      {/* Major Reliability Incidents Grid */}
      <KendoDataTables
        rows={majorIncidentsRows}
        setRows={setMajorIncidentsRows}
        title='Major Reliability Incidents FY25 (High & Medium Risks)'
        modifiedCells={modifiedMajorIncidentsCells}
        setModifiedCells={setModifiedMajorIncidentsCells}
        remarkDialogOpen={remarkDialogOpenMajorIncidents}
        setRemarkDialogOpen={setRemarkDialogOpenMajorIncidents}
        currentRemark={currentRemarkMajorIncidents}
        setCurrentRemark={setCurrentRemarkMajorIncidents}
        currentRowId={currentRowIdMajorIncidents}
        setCurrentRowId={setCurrentRowIdMajorIncidents}
        permissions={{
          ...gridPermissions,
          titleName: 'Major Reliability Incidents FY25: (High & Medium Risks)',
          ExcelName: 'Major_Reliability_Incidents',
        }}
        columns={majorIncidentsColumns}
      />

      {/* Reliability Improvement Initiative Grid */}
      <KendoDataTables
        rows={reliabilityInitiativeRows}
        setRows={setReliabilityInitiativeRows}
        title='Reliability Improvement Initiative'
        modifiedCells={modifiedReliabilityInitiativeCells}
        setModifiedCells={setModifiedReliabilityInitiativeCells}
        remarkDialogOpen={remarkDialogOpenReliabilityInitiative}
        setRemarkDialogOpen={setRemarkDialogOpenReliabilityInitiative}
        currentRemark={currentRemarkReliabilityInitiative}
        setCurrentRemark={setCurrentRemarkReliabilityInitiative}
        currentRowId={currentRowIdReliabilityInitiative}
        setCurrentRowId={setCurrentRowIdReliabilityInitiative}
        permissions={{
          ...gridPermissions,
          titleName: 'Reliability Improvement Initiative',
          ExcelName: 'Reliability_Improvement_Initiative',
        }}
        columns={reliabilityInitiativeColumns}
      />
    </>
  )
}