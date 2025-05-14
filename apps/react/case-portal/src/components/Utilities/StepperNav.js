// StepperNav.jsx
import { useEffect, useMemo, useState } from 'react'
import { Stepper, Step, StepLabel } from '@mui/material'
import { useLocation, useNavigate } from 'react-router-dom'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import plan from '../../menu/plan'
import workspace from '../../menu/workspace'
import { mapScreen } from 'components/Utilities/menuRefractoring'

const steps = [
  { label: 'Configuration', path: 'configuration' },
  { label: 'Production Volume', path: 'production-volume-data' },
  { label: 'Business Demand', path: 'business-demand' },
  { label: 'Shutdown Plan', path: 'shutdown-plan' },
  { label: 'Slowdown Plan', path: 'slowdown-plan' },
  { label: 'Maintenance', path: 'maintenance-details' },
  { label: 'Production AOP', path: 'production-aop' },
  { label: 'Normal Ops', path: 'normal-op-norms' },
  { label: 'Shutdown Norms', path: 'shutdown-norms' },
  { label: 'Slowdown Norms', path: 'slowdown-norms' },
  { label: 'Consumption AOP', path: 'consumption-aop' },
]

const StepperNav = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const keycloak = useSession()
  const verticalId = localStorage.getItem('verticalId')
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const staticMenu = [plan, workspace]
  const [menuItems, setMenuItems] = useState(staticMenu)

  useEffect(() => {
    if (!keycloak?.token || !verticalId) return

    DataService.getScreenbyPlant(keycloak, verticalId, plantId)
      .then((res) => {
        const dynamic = Array.isArray(res.data) ? res.data.map(mapScreen) : []

        if (dynamic.length) {
          setMenuItems(dynamic)
          // setMenuItems(staticMenu)
        }
      })
      .catch((err) => {
        console.error('Menu API failed, using static menu', err)
        setMenuItems(staticMenu)
      })
  }, [keycloak, verticalId, plantId])
  const menuValue = useMemo(() => ({ items: menuItems }), [menuItems])
  //   const steps = useMemo(() => {
  //     // Locate the group
  //     const planGroup = menuItems
  //       .flatMap((item) => item.children || [])
  //       .find((child) => child.id === 'production-norms-plan')

  //     // If not found or no children, return empty
  //     if (!planGroup?.children) return []

  //     // Map each child to { label, url }
  //     return planGroup.children.map((item) => ({
  //       label: item.title,
  //       url: item.url, // e.g. "/production-norms-plan/business-demand"
  //       key: item.id, // unique key
  //     }))
  //   }, [menuItems])

  const currentPath = location.pathname.split('/').pop()
  const activeStep = steps.findIndex((step) => step.path === currentPath)
  // console.log(menuValue)

  const handleStepClick = (index) => {
    navigate(`/production-norms-plan/${steps[index].path}`)
  }

  return (
    <Stepper
      sx={{
        // active icon & label
        // '& .MuiStepIcon-root.Mui-active': { color: 'lightgreen' },
        '& .MuiStepLabel-label.Mui-active': { color: '#1ba0f2' },
        // completed icon
        // '& .MuiStepIcon-root.Mui-completed': { color: 'lightgreen' },
      }}
      nonLinear
      activeStep={activeStep >= 0 ? activeStep : 0}
      alternativeLabel
    >
      {steps.map((step, index) => (
        <Step
          key={step.path}
          onClick={() => handleStepClick(index)}
          //   onClick={() => navigate(step.url)}
        >
          <StepLabel>{step.label}</StepLabel>
        </Step>
      ))}
    </Stepper>
  )
}

export default StepperNav
