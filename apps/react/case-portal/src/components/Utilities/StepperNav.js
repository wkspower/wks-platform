// --- StepperNav.jsx ---------------------------------------------------
import { useEffect, useState } from 'react'
import { Stepper, Step, StepLabel } from '@mui/material'
import { useLocation, useNavigate } from 'react-router-dom'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import plan from '../../menu/plan'
import workspace from '../../menu/workspace'
import { mapScreen } from 'components/Utilities/menuRefractoring'

const staticMenu = [plan, workspace]

export default function StepperNav() {
  // -- Router & Auth -----------------------
  const location = useLocation()
  const navigate = useNavigate()
  const keycloak = useSession()

  // -- IDs from Storage --------------------
  const verticalId = localStorage.getItem('verticalId')
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id

  // -- Steps State -------------------------
  const [steps, setSteps] = useState([])

  // -- Helper to build steps array ---------
  const buildSteps = (menuArr) => {
    const planGroup = menuArr
      .flatMap((m) => m.children || [])
      .find((c) => c.id === 'production-norms-plan')
    if (!planGroup?.children) return []
    return planGroup.children.map((item) => {
      const slug = item.url.split('/').pop()
      return { label: item.title, url: item.url, key: slug }
    })
  }

  // -- Effect: load steps when verticalId (or auth) changes --
  useEffect(() => {
    // guard: need auth + vertical
    if (!keycloak?.token || !verticalId) return

    DataService.getScreenbyPlant(keycloak, verticalId, plantId)
      .then((res) => {
        // map API -> menu items
        const dynamic = Array.isArray(res.data) ? res.data.map(mapScreen) : []
        // choose dynamic if present, else static
        const sourceMenu = dynamic.length ? dynamic : staticMenu

        // build steps and set state
        const newSteps = buildSteps(sourceMenu)
        setSteps(newSteps)

        // navigate to first step whenever verticalId changes
        if (newSteps.length) {
          navigate(newSteps[0].url, { replace: true })
        }
      })
      .catch((err) => {
        console.error('Menu API failed, fallback to static', err)
        const newSteps = buildSteps(staticMenu)
        setSteps(newSteps)
        if (newSteps.length) {
          navigate(newSteps[0].url, { replace: true })
        }
      })
    // IMPORTANT: include verticalId so this re-runs on vertical change
  }, [keycloak, verticalId, plantId])

  // -- Determine active step index ---------
  const currentPath = location.pathname.split('/').pop()
  const activeStep = steps.findIndex((s) => s.key === currentPath)

  // -- Render Stepper ----------------------tested
  return (
    <Stepper
      nonLinear
      alternativeLabel
      activeStep={activeStep >= 0 ? activeStep : 0}
      sx={{
        '& .MuiStepLabel-label': {
          color: 'black',
          fontWeight: 'normal',
        },
        '& .MuiStepLabel-label.Mui-active': {
          fontWeight: 'bold',
        },
      }}
    >
      {steps.map((step) => (
        <Step
          sx={{ cursor: 'pointer' }}
          key={step.key}
          onClick={() => navigate(step.url)}
        >
          <StepLabel sx={{ cursor: 'pointer' }}>{step.label}</StepLabel>
        </Step>
      ))}
    </Stepper>
  )
}
