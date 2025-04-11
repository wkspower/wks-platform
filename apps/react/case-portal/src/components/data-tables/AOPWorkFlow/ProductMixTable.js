import { useState } from 'react'

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
} from '../../../../node_modules/@mui/material/index'
import BusinessDemand from '../BusinessDemand'
import ProductionNorms from '../ProductionNorms'
import ProductionvolumeData from '../ProductionVoluemData'
import ShutDown from '../ShutDown'
import SlowDown from '../Slowdown'
import AuditTrail from './AuditTrail'
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

  const defaultCustomHeight = { mainBox: '60vh', otherBox: '124%' }

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

      {
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
        ))
        // ) : (
        //   <Accordion
        //     expanded={true}
        //     sx={{
        //       mb: '5px',
        //       '&.Mui-expanded': {
        //         margin: 0,
        //         padding: 0,
        //       },
        //     }}
        //   >
        //     <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        //       Audit Trail
        //     </AccordionSummary>
        //     <AccordionDetails>
        //       <AuditTrail />
        //     </AccordionDetails>
        //   </Accordion>
        // )
      }
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
