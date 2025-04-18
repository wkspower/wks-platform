import { useState } from 'react'

import {
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Stepper,
  Step,
  StepLabel,
} from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'

import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '../../../../node_modules/@mui/material/index'
import BusinessDemand from '../BusinessDemand'
import ProductionNorms from '../ProductionNorms'
import ProductionvolumeData from '../ProductionVoluemData'
import ShutDown from '../ShutDown'
import SlowDown from '../Slowdown'
import AuditTrail from './AuditTrail'
import './jio-grid-style.css'
import DataGridTable from '../ASDataGrid'

// import DataGridTable from '../ASDataGrid'
const FiveTables = () => {
  const [activeStep, setActiveStep] = useState(0)
  const [expanded, setExpanded] = useState(0)
  const [openRejectDialog, setOpenRejectDialog] = useState(false)
  const [rejectReason, setRejectReason] = useState('')

  const steps = [
    'Submit Plant AOP',
    'Validate Plant AOP',
    'Review Plant AOP',
    'Approve AOP',
    'Final Approval O2C AOP',
    // 'Closed',
  ]
  const columns = [
    {
      field: 'particulars',
      headerName: 'Particulars',
      minWidth: 300,
      // custom header renderer
      renderHeader: (params) => (
        <div
          style={{
            display: 'flex',
            alignItems: 'center', // vertical centering
            justifyContent: 'center', // horizontal centering
            height: '100%', // span the full header height
            width: '100%',
          }}
        >
          {params.colDef.headerName}
        </div>
      ),
    },
    {
      field: 'UOM',
      headerName: 'UOM',
      minWidth: 100,
      renderHeader: (params) => (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            height: '100%',
            width: '100%',
          }}
        >
          {params.colDef.headerName}
        </div>
      ),
    },
    // grouped children can skip renderHeader:
    { field: 'aop_2025_26', headerName: 'FY 2025-26 AOP', minWidth: 150 },
    { field: 'actual_2025_26', headerName: 'FY 2025-26 Actual', minWidth: 150 },
    { field: 'aop_2026_27', headerName: 'FY 2026-27 AOP', minWidth: 150 },
    {
      field: 'remarks',
      headerName: 'Remarks',
      minWidth: 200,
      renderHeader: (params) => (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            height: '100%',
            width: '100%',
          }}
        >
          {params.colDef.headerName}
        </div>
      ),
    },
  ]

  const columnGroupingModel = [
    {
      groupId: 'annualAOP',
      headerName: 'Annual AOP Cost',
      children: [
        { field: 'aop_2025_26' },
        { field: 'actual_2025_26' },
        { field: 'aop_2026_27' },
      ],
    },
  ]
  const rows = [
    {
      id: 1,
      site: 'HMD',
      plant: 'MEG1',
      particulars: 'Raw Material Cost',
      UOM: 'Rs/MT',
      aop_2025_26: 9999,
      actual_2025_26: 9999,
      aop_2026_27: 9999,
      remarks: '',
    },
    {
      id: 2,
      site: 'HMD',
      plant: 'MEG1',
      particulars: 'By Product Cost',
      UOM: 'Rs/MT',
      aop_2025_26: 9999,
      actual_2025_26: 9999,
      aop_2026_27: 9999,
      remarks: 'Lower due to',
    },
    {
      id: 3,
      site: 'HMD',
      plant: 'MEG1',
      particulars: 'Utility Cost',
      UOM: 'Rs/MT',
      aop_2025_26: 9999,
      actual_2025_26: 9999,
      aop_2026_27: 9999,
      remarks: 'Improved due to',
    },
    {
      id: 4,
      site: 'HMD',
      plant: 'MEG1',
      particulars: 'Cat-Chem Cost',
      UOM: 'Rs/MT',
      aop_2025_26: 9999,
      actual_2025_26: 9999,
      aop_2026_27: 9999,
      remarks: '',
    },
    {
      id: 5,
      site: 'HMD',
      plant: 'MEG1',
      particulars: 'RM Net of By Product Cost',
      UOM: 'Rs/MT',
      aop_2025_26: 9999,
      actual_2025_26: 9999,
      aop_2026_27: 9999,
      remarks: '',
    },
    {
      id: 6,
      site: 'HMD',
      plant: 'MEG1',
      particulars: 'Conversion Cost',
      UOM: 'Rs/MT',
      aop_2025_26: 9999,
      actual_2025_26: 9999,
      aop_2026_27: 9999,
      remarks: '',
    },
    {
      id: 7,
      site: 'HMD',
      plant: 'MEG1',
      particulars: 'Variable Cost',
      UOM: 'Rs/MT',
      aop_2025_26: 9999,
      actual_2025_26: 9999,
      aop_2026_27: 9999,
      remarks: '',
    },
  ]

  const defaultCustomHeight = { mainBox: '60vh', otherBox: '114%' }

  const tables = [
    {
      title: 'Production AOP',
      component: (
        <ProductionNorms
          permissions={{
            showAction: false,
            addButton: false,
            deleteButton: false,
            editButton: false,
            showUnit: false,
            saveWithRemark: false,
            showCalculate: false,
            saveBtn: false,
            units: ['Ton', 'Kilo Ton'],
            customHeight: defaultCustomHeight, // use default height
          }}
        />
      ),
    },
    {
      title: 'Business Demand',
      component: (
        <BusinessDemand
          permissions={{
            showAction: false,
            addButton: false,
            deleteButton: false,
            editButton: false,
            showUnit: false,
            saveWithRemark: false,
            saveBtn: false,
            units: ['TPH', 'TPD'],
            customHeight: defaultCustomHeight, // add height here too
          }}
        />
      ),
    },
    {
      title: 'Production Volume Data',
      component: (
        <ProductionvolumeData
          permissions={{
            showAction: false,
            addButton: false,
            deleteButton: false,
            editButton: false,
            showUnit: false,
            saveWithRemark: false,
            showRefreshBtn: false,
            showCalculate: false,
            saveBtn: false,
            units: ['TPH', 'TPD'],
            customHeight: defaultCustomHeight,
          }}
        />
      ),
    },
    {
      title: 'Shutdown Activities',
      component: (
        <ShutDown
          permissions={{
            showAction: false,
            addButton: false,
            deleteButton: false,
            editButton: false,
            showUnit: false,
            saveWithRemark: false,
            saveBtn: false,
            customHeight: defaultCustomHeight,
          }}
        />
      ),
    },
    {
      title: 'Slowdown Activities',
      component: (
        <SlowDown
          permissions={{
            showAction: false,
            addButton: false,
            deleteButton: false,
            editButton: false,
            showUnit: false,
            saveWithRemark: false,
            saveBtn: false,
            customHeight: defaultCustomHeight,
          }}
        />
      ),
    },
  ]

  const handleRejectClick = () => {
    setOpenRejectDialog(true)
  }
  // const [audit, setAudit] = useState(true)
  const [openAuditPopup, setOpenAuditPopup] = useState(false)
  // const handleAudit = () => {
  //   setAudit((prev) => !prev)
  // }

  // Updated Audit handler: open popup dialog
  const handleAuditOpen = () => {
    setOpenAuditPopup(true)
  }

  const handleAuditClose = () => {
    setOpenAuditPopup(false)
  }

  const handleRejectSubmit = () => {
    // Perform rejection logic here (e.g., API call with rejectReason)
    setOpenRejectDialog(false)
    setRejectReason('')
  }

  const handleRejectCancel = () => {
    setOpenRejectDialog(false)
    setRejectReason('')
  }

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '5px',
        marginTop: '20px',
      }}
    >
      <Stepper activeStep={activeStep} alternativeLabel>
        {steps.map((label, index) => (
          <Step key={label} onClick={() => setActiveStep(index)}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>
      <Stack
        direction='row'
        spacing={1}
        justifyContent='right'
        sx={{ mt: 2, mb: 1 }}
      >
        <Button
          variant='contained'
          color='primary'
          sx={{ fontWeight: 'bold', textTransform: 'none', px: 3 }}
          onClick={handleRejectClick}
        >
          Accept
        </Button>
        <Button
          variant='outlined'
          color='secondary'
          sx={{ fontWeight: 'bold', textTransform: 'none', px: 3 }}
          onClick={handleRejectClick}
        >
          Reject
        </Button>
        <Button
          variant='outlined'
          color='primary'
          sx={{ fontWeight: 'bold', textTransform: 'none', px: 3 }}
          onClick={handleAuditOpen}
        >
          Audit Trail
        </Button>
      </Stack>
      {/* Reject Dialog */}
      <Dialog open={openRejectDialog} onClose={handleRejectCancel}>
        <DialogTitle>Please provide remarks on the changes?</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin='dense'
            label='Rejection Reason'
            type='text'
            fullWidth
            sx={{ width: '100%', minWidth: '600px' }}
            multiline
            rows={8}
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            variant='outlined'
          />
        </DialogContent>
        <DialogActions sx={{ justifyContent: 'flex-end' }}>
          <Button onClick={handleRejectCancel} color='primary'>
            Cancel
          </Button>
          <Button
            onClick={handleRejectSubmit}
            color='primary'
            variant='contained'
            disabled={!rejectReason?.trim()}
          >
            Submit
          </Button>
        </DialogActions>
      </Dialog>

      {false &&
        tables.map((table, index) => (
          <Accordion
            key={index}
            expanded={expanded === index}
            onChange={(event, isExpanded) =>
              setExpanded(isExpanded ? index : false)
            }
            sx={{
              mb: '5px',
              '&.Mui-expanded': {
                margin: 0,
              },
            }}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              {table.title}
            </AccordionSummary>
            <AccordionDetails
            // sx={{
            //   height: 'calc(100% - 70rem)', // Fixed height for testing
            //   overflowY: 'auto', // Allows scrolling if content exceeds 300px
            // }}
            >
              {table.component}
            </AccordionDetails>
          </Accordion>
        ))}
      <DataGridTable
        columns={columns}
        rows={rows}
        columnGroupingModel={columnGroupingModel}
        className='jio-data-grid'
        permissions={{
          customHeight: defaultCustomHeight,
        }}

        // sx={{
        //   '& .MuiDataGrid-columnHeaderTitle': {
        //     fontWeight: 'bold',
        //   },
        //   '& .MuiDataGrid-columnHeaderGroup': {
        //     justifyContent: 'center',
        //     border: '1px solid #ccc', // border for group header
        //   },
        //   '& .MuiDataGrid-columnHeadersInner': {
        //     borderBottom: '2px solid #ccc',
        //   },
        //   '& .MuiDataGrid-columnHeader': {
        //     borderRight: '1px solid #eee',
        //   },
        //   '& .MuiDataGrid-cell': {
        //     borderRight: '1px solid #eee',
        //   },
        //   '& .MuiDataGrid-columnHeaderGroup--filled': {
        //     justifyContent: 'center',
        //   },
        // }}
      />

      {/* Audit Trail Popup Dialog */}
      <Dialog
        open={openAuditPopup}
        onClose={handleAuditClose}
        maxWidth='lg'
        fullWidth
      >
        <DialogTitle>Audit Trail</DialogTitle>
        <DialogContent dividers>
          <AuditTrail />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleAuditClose} color='primary'>
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  )
}

export default FiveTables
