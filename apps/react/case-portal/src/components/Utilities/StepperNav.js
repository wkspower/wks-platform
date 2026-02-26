import React from 'react'
import {
  Box,
  Step,
  StepLabel,
  Stepper,
  Tooltip,
  Typography,
} from '@mui/material'
import { verticalEnums } from 'enums/verticalEnums'
import { drawerWidth } from 'config'
import { useMenuContext } from 'menu/menuProvider'
import { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { useLocation, useNavigate } from 'react-router-dom'

// Toggle between fixed and sticky behavior here
const USE_FIXED = true // set to false to use position: 'sticky'

export default function StepperNav() {
  const location = useLocation()
  const navigate = useNavigate()
  const { plantID, verticalChange } = useSelector(
    (state) => state.dataGridStore,
  )
  const plantName = plantID?.plantName
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const dataGridStore = useSelector((state) => state.dataGridStore)

  const {
    yearChanged,
    oldYear,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore

  const PLANT_NAME = plantObject?.name?.toLowerCase()

  const SITE_NAME = siteObject?.name?.toLowerCase()

  const [steps, setSteps] = useState([])

  const { items: menuItems } = useMenuContext()
  const { drawerOpen } = useSelector((state) => state.menu)
  const collectItems = useCallback(
    (nodes) =>
      nodes.flatMap((node) => {
        if (node.type === 'item') {
          return [node]
        }
        if (node.children) {
          return collectItems(node.children)
        }
        return []
      }),
    [],
  )

  const buildSteps = useCallback(
    (menuArr) => {
      const planGroup = menuArr
        .flatMap((m) => m.children || [])
        .find((c) => c.id === 'production-norms-plan')

      if (!planGroup?.children) return []
      const allItems = collectItems(planGroup.children)
      return allItems.map((item) => {
        const slug = item.url.split('/').pop()
        return { label: item.title, url: item.url, key: slug }
      })
    },
    [collectItems],
  )

  useEffect(() => {
    const newSteps = buildSteps(menuItems)

    const isPE = lowerVertName === 'pe'

    const shouldFilterSlowdown =
      // Condition 1: PE vertical AND LDPE plant
      (lowerVertName === verticalEnums.PE && plantName === 'LDPE') ||
      // Condition 2: PE vertical AND DMD site
      (lowerVertName === verticalEnums.PE && SITE_NAME === 'dmd')
    if (shouldFilterSlowdown) {
      const filteredSteps = newSteps.filter(
        (step) => step.key !== 'slowdown-norms',
      )
      setSteps(filteredSteps)
    } else {
      setSteps(newSteps)
    }
    const currentSlug = location.pathname.split('/').pop()
    const found = newSteps.some((s) => s.key === currentSlug)

    if (newSteps.length && !found) {
      navigate(newSteps[0].url, { replace: true })
    }
  }, [
    menuItems,
    lowerVertName,
    plantName,
    buildSteps,
    navigate,
    location.pathname,
  ])

  const currentPath = location.pathname.split('/').pop()
  const activeStep = steps.findIndex((s) => s.key === currentPath)

  // Helper to return first 15 characters then ellipsis
  const getAbbrev = (label) => {
    if (!label) return ''
    const text = label.trim()
    return text.length <= 12 ? text : `${text.slice(0, 12)}…`
  }

  const StepperElement = (
    <Stepper
      nonLinear
      alternativeLabel
      activeStep={activeStep >= 0 ? activeStep : 0}
      sx={{
        display: 'flex', // prevent wrapping so horizontal scroll is used
        flexWrap: 'nowrap',
        alignItems: 'center', // <-- ensure vertical centering
        minHeight: 64, // stable height
        px: 1,
        '& .MuiStepLabel-root': {
          padding: 0,
        },
        '& .MuiStepLabel-label': {
          fontWeight: 'normal',
          marginTop: '6px !important', // small label spacing
          lineHeight: 1,
        },
        '& .MuiStepLabel-label.Mui-active': {
          fontWeight: '700',
          color: '#000',
        },
        // Connector root is absolutely positioned; move it to icon center
        '& .MuiStepConnector-root': {
          top: '20px', // adjust if your icons are thicker
        },
        // connector line thickness
        '& .MuiStepConnector-line': {
          height: 2,
        },
        // Icon base — make it visually consistent and vertically aligned
        '& .MuiStepIcon-root': {
          zIndex: 2,
          backgroundColor: '#fff',
          borderRadius: '50%',
          width: 28,
          height: 28,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          position: 'relative', // allow top nudging
          top: 2, // small nudge to align with connector
        },
        // active icon color (keeps your color)
        '& .MuiStepIcon-root.Mui-active': {
          color: '#0100cb',
        },
      }}
    >
      {steps.map((step) => {
        return (
          <Step
            key={step.key}
            onClick={() => navigate(step.url)}
            sx={{
              cursor: 'pointer',
              display: 'inline-flex', // keep each step inline for nowrap layout
              flexDirection: 'column', // stack icon above label consistently
              alignItems: 'center',
              px: 0.5,
            }}
            aria-label={step.label}
          >
            {/* Tooltip shows full label on hover; visible text is abbreviated */}
            <StepLabel sx={{ cursor: 'pointer' }}>
              <Tooltip title={step.label} enterDelay={200} arrow>
                <Typography
                  variant='caption'
                  sx={{
                    minWidth: 28,
                    maxWidth: 80,
                    display: 'inline-block',
                    textAlign: 'center',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    fontWeight: (theme) =>
                      activeStep === steps.findIndex((s) => s.key === step.key)
                        ? '700'
                        : '500',
                    fontSize: '0.75rem',
                  }}
                >
                  {step.label}
                </Typography>
              </Tooltip>
            </StepLabel>
          </Step>
        )
      })}
    </Stepper>
  )

  return (
    <>
      {USE_FIXED ? (
        <>
          {/* Fixed to viewport (below AppBar if present) */}
          <Box
            sx={{
              position: 'fixed',
              top: '45px',
              left: drawerOpen ? `${drawerWidth + 12}px` : '12px',
              right: '12px',
              zIndex: (theme) => (theme.zIndex?.appBar ?? 1100) + 1,
              bgcolor: 'background.paper',
              boxShadow: 1,
              borderBottom: '1px solid',
              borderTop: '1px solid',
              borderLeft: '1px solid',
              borderRight: '1px solid',
              borderColor: 'grey.700',
              py: 0,
              transition: 'left 200ms ease',
              maxHeight: '70px',
            }}
          >
            {/* HORIZONTAL SCROLL WRAPPER - thin scrollbar only when needed */}
            <Box
              sx={{
                overflowX: 'auto',
                overflowY: 'hidden',
                whiteSpace: 'nowrap',
                WebkitOverflowScrolling: 'touch',
                px: 0.5,
                // Thin webkit scrollbar
                '&::-webkit-scrollbar': {
                  height: '6px',
                },
                '&::-webkit-scrollbar-thumb': {
                  borderRadius: 3,
                  backgroundColor: 'rgba(0,0,0,0.22)',
                },
                '&::-webkit-scrollbar-track': {
                  backgroundColor: 'transparent',
                },
                // Firefox
                scrollbarWidth: 'thin',
                scrollbarColor: 'rgba(0,0,0,0.22) transparent',
              }}
            >
              {StepperElement}
            </Box>
          </Box>

          {/* Spacer so fixed element doesn't cover content */}
          <Box
            sx={{ height: (theme) => theme?.mixins?.toolbar?.minHeight ?? 64 }}
          />
        </>
      ) : (
        // same thin-scroll wrapper for non-fixed mode
        <Box
          sx={{
            overflowX: 'auto',
            overflowY: 'hidden',
            whiteSpace: 'nowrap',
            WebkitOverflowScrolling: 'touch',
            px: 0.5,
            '&::-webkit-scrollbar': {
              height: '6px',
            },
            '&::-webkit-scrollbar-thumb': {
              borderRadius: 3,
              backgroundColor: 'rgba(0,0,0,0.22)',
            },
            '&::-webkit-scrollbar-track': {
              backgroundColor: 'transparent',
            },
            scrollbarWidth: 'thin',
            scrollbarColor: 'rgba(0,0,0,0.22) transparent',
          }}
        >
          {StepperElement}
        </Box>
      )}
    </>
  )
}
