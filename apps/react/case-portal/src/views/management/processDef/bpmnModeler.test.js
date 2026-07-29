import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'

// The real modeler pulls in bpmn-js and a Material-UI v4 styling stack; none of
// that is what's under test here — the save/deploy outcome handling is.
jest.mock('@wkspower/camunda-web-modeler', () => ({
  // The real modeler publishes itself into the refs it is handed; the component
  // saves through that ref, so the stub has to do the same or Save is a no-op.
  BpmnModeler: ({ modelerTabOptions }) => {
    const refs = modelerTabOptions?.modelerOptions?.refs || []
    refs.forEach((ref) => {
      ref.current = { save: async () => ({ xml: '<bpmn/>', svg: '<svg/>' }) }
    })
    return <div data-testid='modeler' />
  },
  isBpmnIoEvent: () => false,
  isContentSavedEvent: () => false,
  isNotificationEvent: () => false,
  isPropertiesPanelResizedEvent: () => false,
  isUIUpdateRequiredEvent: () => false,
}))

jest.mock('services/ProcessDefService', () => ({
  ProcessDefService: { getBPMNXml: jest.fn() },
}))
jest.mock('services/DeploymentService', () => ({
  DeploymentService: { deploy: jest.fn() },
}))

const { ProcessDefService } = require('services/ProcessDefService')
const { DeploymentService } = require('services/DeploymentService')
const { BPMNModeler } = require('./bpmnModeler')

// The component saves through a ref the mocked modeler never populates, so hand
// it one that behaves like the real modeler's save().
function renderModeler(handleClose) {
  const view = render(
    <BPMNModeler
      open
      keycloak={{}}
      processDef={{ id: 'demoProc:1:abc', name: 'Demo Proc', key: 'demoProc' }}
      handleClose={handleClose}
    />,
  )
  return view
}

describe('BPMNModeler save', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    ProcessDefService.getBPMNXml.mockResolvedValue('<bpmn/>')
  })

  it('keeps the modeler open and reports the reason when the engine rejects the deployment', async () => {
    const handleClose = jest.fn()
    DeploymentService.deploy.mockRejectedValue(
      new Error(
        'Request failed with status 400: ENGINE-12018 History Time To Live (TTL) cannot be null',
      ),
    )

    renderModeler(handleClose)
    await waitFor(() => expect(ProcessDefService.getBPMNXml).toHaveBeenCalled())

    await userEvent.click(screen.getByRole('button', { name: /save/i }))

    await screen.findByText(/ENGINE-12018/)
    // The whole point: a rejected deployment must not look like a saved one.
    expect(handleClose).not.toHaveBeenCalled()
  })

  it('closes on a deployment the engine accepted', async () => {
    const handleClose = jest.fn()
    DeploymentService.deploy.mockResolvedValue(undefined)

    renderModeler(handleClose)
    await waitFor(() => expect(ProcessDefService.getBPMNXml).toHaveBeenCalled())

    await userEvent.click(screen.getByRole('button', { name: /save/i }))

    await waitFor(() => expect(handleClose).toHaveBeenCalled())
  })
})
