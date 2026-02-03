import React, { useState } from 'react'
import WorkflowTimeline from './WorkflowTimeline'

/**
 * Example usage of WorkflowTimeline component
 * This demonstrates how to use the component with your 4-step workflow
 */
const WorkflowTimelineExample = () => {
  // Example data structure for your workflow steps
  const [workflowSteps] = useState([
    {
      id: 1,
      label: 'Step 1',
      role: 'Plant Manager',
      status: 'completed',
      completedDate: '2026-01-20',
      completedBy: 'John Doe',
    },
    {
      id: 2,
      label: 'Step 2',
      role: 'EPS Engineer',
      status: 'completed',
      completedDate: '2026-01-22',
      completedBy: 'Jane Smith',
    },
    {
      id: 3,
      label: 'Step 3',
      role: 'EPS Head / CTS Head',
      status: 'active',
    },
    {
      id: 4,
      label: 'Step 4',
      role: 'R&M Cluster Head',
      status: 'pending',
    },
  ])

  // Alternative: Use currentStep prop instead of status in each step
  const currentStepIndex = 2 // 0-based index (Step 3 is active)

  return (
    <div>
      {/* Method 1: Using status in each step object */}
      <WorkflowTimeline steps={workflowSteps} />

      {/* Method 2: Using currentStep prop (component will auto-calculate status) */}
      {/* <WorkflowTimeline steps={workflowSteps} currentStep={currentStepIndex} /> */}
    </div>
  )
}

export default WorkflowTimelineExample

/**
 * DATA STRUCTURE REFERENCE
 *
 * Use this structure when passing data to WorkflowTimeline component:
 *
 * const workflowData = [
 *   {
 *     id: 1,                           // Unique identifier
 *     label: 'Step 1',                 // Step name/title
 *     role: 'Plant Manager',           // Role responsible for this step
 *     status: 'completed',             // 'completed' | 'active' | 'pending'
 *     completedDate: '2026-01-20',     // Optional: Date when completed
 *     completedBy: 'John Doe'          // Optional: Person who completed
 *   },
 *   {
 *     id: 2,
 *     label: 'Step 2',
 *     role: 'EPS Engineer',
 *     status: 'active'
 *   },
 *   // ... more steps
 * ];
 *
 * USAGE:
 * <WorkflowTimeline steps={workflowData} />
 *
 * OR with currentStep prop:
 * <WorkflowTimeline steps={workflowData} currentStep={1} />
 */
