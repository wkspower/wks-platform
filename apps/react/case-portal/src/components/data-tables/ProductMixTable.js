import { useState } from 'react'
import ASDataGrid from './ASDataGrid'

import {
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Stepper,
  Step,
  StepLabel,
} from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '../../../node_modules/@mui/material/index'
import BusinessDemand from './BusinessDemand'
import ProductionNorms from './ProductionNorms'
import ProductionvolumeData from './ProductionVoluemData'
import ShutDown from './ShutDown'
import SlowDown from './Slowdown'
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
            // UOM: 'Ton',
            units: ['Ton', 'Kilo Ton'],
            // UnitToShow: 'Values/Ton',
          }}
        />
      ),
    },
    {
      title: 'Business Demand',
      // columns: businessDemandColumns,
      // rows: businessDemandData,
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
          }}
        />
      ),
    },
    {
      title: 'Production Volume Data',
      // columns: productionVolume,
      // rows: productionVolumeData,
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
            saveBtn: false,
            units: ['TPH', 'TPD'],
          }}
        />
      ),
    },
    {
      title: 'Shutdown Activities',
      // columns: shutdownColumns,
      // rows: shutdownData,
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
          }}
        />
      ),
    },
    {
      title: 'Slowdown Activities',
      // columns: slowdownColumns,
      // rows: slowDownData,
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
          }}
        />
      ),
    },
  ]

  // Handlers for Reject Dialog
  const handleRejectClick = () => {
    setOpenRejectDialog(true)
  }
  const handleRejectSubmit = () => {
    // Perform rejection logic here (e.g., API call with rejectReason)
    console.log('Rejected with reason:', rejectReason)
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
        {/* <Button
          variant='contained'
          color='secondary'
          sx={{ fontWeight: 'bold', px: 3 }}
        >
          Send Back
        </Button> */}
      </Stack>
      {/* Reject Dialog */}
      <Dialog open={openRejectDialog} onClose={handleRejectCancel}>
        <DialogTitle>Do you want to reject the data?</DialogTitle>
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

      {tables.map((table, index) => (
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
          <AccordionDetails>
            {table.component ? (
              // Render the imported component directly
              table.component
            ) : (
              // Otherwise, render ASDataGrid with provided columns and rows
              <ASDataGrid
                columns={table.columns}
                rows={table.rows}
                isCellEditable={() => false}
                permissions={{
                  showAction: false,
                  addButton: false,
                  deleteButton: false,
                  editButton: false,
                  saveBtn: false,
                  allAction: false,
                }}
              />
            )}
          </AccordionDetails>
        </Accordion>
      ))}
    </div>
  )
}

export default FiveTables
