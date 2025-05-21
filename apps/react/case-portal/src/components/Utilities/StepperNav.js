import { useEffect, useState } from 'react'
import { Stepper, Step, StepLabel } from '@mui/material'
import { useLocation, useNavigate } from 'react-router-dom'
import { useMenuContext } from 'menu/menuProvider'

export default function StepperNav() {
  const location = useLocation()
  const navigate = useNavigate()

  const [steps, setSteps] = useState([])

  const { items: menuItems } = useMenuContext()

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

  useEffect(() => {
    const newSteps = buildSteps(menuItems)
    setSteps(newSteps)

    // Derive current path slug
    const currentSlug = location.pathname.split('/').pop()
    const found = newSteps.some((s) => s.key === currentSlug)

    // If current path isn't one of our steps, redirect to the first
    if (newSteps.length && !found) {
      navigate(newSteps[0].url, { replace: true })
    }
  }, [menuItems])

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
          fontWeight: 'normal',
        },
        '& .MuiStepLabel-label.Mui-active': {
          fontWeight: 'bold',
          color: '#000',
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
