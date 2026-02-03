import React from 'react'
import { Tooltip } from '@mui/material'
import './../../../css/WorkflowTimeline.css'

const WorkflowTimeline = ({ steps, currentStep }) => {
  const getStepStatus = (index) => {
    if (index < currentStep) return 'completed'
    if (index === currentStep) return 'active'
    return 'pending'
  }

  const getTooltipContent = (step, status) => {
    return (
      <div style={{ padding: '4px' }}>
        <div style={{ fontWeight: 600, marginBottom: '4px' }}>{step.role}</div>
        {step.completedDate && status === 'completed' && (
          <div style={{ fontSize: '0.75rem' }}>
            <div style={{ color: '#a7f3d0' }}>{step.completedDate}</div>
            {step.completedBy && (
              <div style={{ color: '#d1d5db', marginTop: '2px' }}>
                by {step.completedBy}
              </div>
            )}
          </div>
        )}
      </div>
    )
  }

  return (
    <div className='workflow-timeline'>
      <div className='timeline-container'>
        {steps.map((step, index) => {
          const status = step.status || getStepStatus(index)
          const isLastStep = index === steps.length - 1

          return (
            <div key={step.id} className='timeline-step-wrapper'>
              <div className={`timeline-step ${status}`}>
                {/* Step Circle/Icon with Tooltip */}
                <Tooltip
                  title={getTooltipContent(step, status)}
                  arrow
                  placement='top'
                >
                  <div className='step-indicator'>
                    <div className='step-circle'>
                      {status === 'completed' ? (
                        <svg
                          className='check-icon'
                          viewBox='0 0 24 24'
                          fill='none'
                          stroke='currentColor'
                          strokeWidth='3'
                        >
                          <polyline points='20 6 9 17 4 12' />
                        </svg>
                      ) : (
                        <span className='step-number'>{index + 1}</span>
                      )}
                    </div>
                  </div>
                </Tooltip>

                {/* Connecting Line */}
                {!isLastStep && (
                  <div
                    className={`step-connector ${status === 'completed' ? 'completed' : ''}`}
                  />
                )}

                {/* Step Details - Only Label */}
                <div className='step-details'>
                  <div className='step-label'>{step.role}</div>
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default WorkflowTimeline
