/**
 * Transform approval status API response to workflow timeline steps
 * @param {Object} approvalStatusJson - Parsed JSON from approvalStatus.value
 * @param {string} selectedPlant - Currently selected plant name
 * @param {Object} submissionStatusJson - Parsed JSON from submissionStatus.value
 * @returns {Array} Timeline steps array
 */
export const transformApprovalStatusToSteps = (
  approvalStatusJson,
  selectedPlant,
  submissionStatusJson,
) => {
  // Fixed sequence of workflow steps
  const workflowSequence = [
    {
      id: 1,
      label: 'Step 1',
      role: 'Plant Manager',
      key: 'plant_manager_approved', // Not in API response, always completed
    },
    {
      id: 2,
      label: 'Step 2',
      role: 'EPS Engineer',
      key: 'ebs_approved',
    },
    {
      id: 3,
      label: 'Step 3',
      role: 'EPS Head / CTS Head',
      key: 'cts_approved',
    },
    {
      id: 4,
      label: 'Step 4',
      role: 'R&M Cluster Head',
      key: 'cluster_head_approved',
    },
  ]

  // Check if Step 1 (Plant Manager) is completed
  // If selectedPlant is provided, check that specific plant
  // Otherwise, check if ALL plants are submitted
  let isStep1Completed = false
  if (selectedPlant && submissionStatusJson) {
    isStep1Completed = submissionStatusJson[selectedPlant] === true
  } else if (submissionStatusJson) {
    // Check if all plants are submitted
    const allPlants = Object.keys(submissionStatusJson)
    isStep1Completed =
      allPlants.length > 0 &&
      allPlants.every((plant) => submissionStatusJson[plant] === true)
  }

  // Find the first false status in sequence (only if Step 1 is completed)
  let firstFalseIndex = -1
  if (isStep1Completed) {
    for (let i = 1; i < workflowSequence.length; i++) {
      const step = workflowSequence[i]
      if (approvalStatusJson[step.key] === false) {
        firstFalseIndex = i
        break
      }
    }
  }

  // Transform to timeline steps
  const timelineSteps = workflowSequence.map((step, index) => {
    let status = 'pending'

    if (index === 0) {
      // Plant Manager step - check submission status
      if (isStep1Completed) {
        status = 'completed'
      } else {
        // If Step 1 is not completed, it should be active (first step)
        status = 'active'
      }
    } else if (!isStep1Completed) {
      // If Step 1 is not completed, all other steps are pending
      status = 'pending'
    } else if (approvalStatusJson[step.key] === true) {
      // If true in API response, it's completed
      status = 'completed'
    } else if (index === firstFalseIndex) {
      // First false in sequence is active
      status = 'active'
    } else {
      // All other false values are pending
      status = 'pending'
    }

    return {
      id: step.id,
      label: step.label,
      role: step.role,
      status,
    }
  })

  return timelineSteps
}

/**
 * Parse and transform the API response
 * @param {Array} apiResponse - The full API response array
 * @param {string} selectedPlant - Currently selected plant name
 * @returns {Array} Timeline steps array
 */
export const parseApprovalStatusResponse = (apiResponse, selectedPlant) => {
  try {
    // Find the approvalStatus object in the response
    const approvalStatusItem = apiResponse.find(
      (item) => item.name === 'approvalStatus',
    )

    if (!approvalStatusItem) {
      console.error('approvalStatus not found in response')
      return []
    }

    // Find the submissionStatus object in the response
    const submissionStatusItem = apiResponse.find(
      (item) => item.name === 'submissionStatus',
    )

    // Parse the JSON strings
    const approvalStatusJson = JSON.parse(approvalStatusItem.value)
    const submissionStatusJson = submissionStatusItem
      ? JSON.parse(submissionStatusItem.value)
      : null

    // Transform to timeline steps
    return transformApprovalStatusToSteps(
      approvalStatusJson,
      selectedPlant,
      submissionStatusJson,
    )
  } catch (error) {
    console.error('Error parsing approval status:', error)
    return []
  }
}
