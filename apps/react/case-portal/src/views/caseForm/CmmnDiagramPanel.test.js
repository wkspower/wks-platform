import { render, screen } from '@testing-library/react'
import { I18nextProvider } from 'react-i18next'
// Real catalogs behind the same provider the app mounts, rather than a stubbed t():
// the empty-state assertion then also proves the key exists, which a stub would hide.
import i18n from '../../i18n'
import CmmnDiagramPanel from './CmmnDiagramPanel'

/**
 * The markup rendered here is customer-supplied and goes in via
 * dangerouslySetInnerHTML, so what survives sanitising is the thing worth pinning —
 * along with the highlighting, which is the whole point of showing the diagram.
 */
describe('CmmnDiagramPanel', () => {
  const diagram = `
    <svg xmlns="http://www.w3.org/2000/svg">
      <g class="djs-element" data-element-id="PlanItem_stage">
        <g class="djs-visual"><rect width="100" height="80"/></g>
      </g>
      <g class="djs-element" data-element-id="PlanItem_reached">
        <g class="djs-visual"><rect width="100" height="40"/></g>
      </g>
      <g class="djs-element" data-element-id="PlanItem_pending">
        <g class="djs-visual"><rect width="100" height="40"/></g>
      </g>
    </svg>`

  const stages = [
    {
      name: 'Vorverfahren (NPOL/LPOL)',
      sourceElementId: 'PlanItem_stage',
      milestones: [
        {
          id: 'reached',
          name: 'Person erfasst',
          sourceElementId: 'PlanItem_reached',
        },
        {
          id: 'pending',
          name: 'Antrag gestellt',
          sourceElementId: 'PlanItem_pending',
        },
      ],
    },
  ]

  const renderPanel = (props) =>
    render(
      <I18nextProvider i18n={i18n}>
        <CmmnDiagramPanel {...props} />
      </I18nextProvider>,
    )

  const shapeIn = (container, elementId) =>
    container.querySelector(`[data-element-id="${elementId}"] .djs-visual > *`)

  it('renders the supplied diagram', () => {
    const { container } = renderPanel({
      sourceDiagram: diagram,
      stages,
      activeStage: 'Vorverfahren (NPOL/LPOL)',
      achievedMilestoneIds: [],
    })

    expect(container.querySelector('svg')).toBeInTheDocument()
    expect(container.querySelectorAll('[data-element-id]')).toHaveLength(3)
  })

  it('marks the stage the case is in', () => {
    const { container } = renderPanel({
      sourceDiagram: diagram,
      stages,
      activeStage: 'Vorverfahren (NPOL/LPOL)',
      achievedMilestoneIds: [],
    })

    expect(shapeIn(container, 'PlanItem_stage').style.stroke).toBeTruthy()
  })

  it('marks only the milestones actually reached', () => {
    const { container } = renderPanel({
      sourceDiagram: diagram,
      stages,
      activeStage: 'Vorverfahren (NPOL/LPOL)',
      achievedMilestoneIds: ['reached'],
    })

    expect(shapeIn(container, 'PlanItem_reached').style.fill).toBeTruthy()
    // An unreached milestone must stay plain, or the diagram overstates progress.
    expect(shapeIn(container, 'PlanItem_pending').style.fill).toBeFalsy()
  })

  it('marks nothing when the case is in a stage with no source shape', () => {
    const { container } = renderPanel({
      sourceDiagram: diagram,
      stages: [{ name: 'Abschluss', sourceElementId: null, milestones: [] }],
      activeStage: 'Abschluss',
      achievedMilestoneIds: [],
    })

    expect(shapeIn(container, 'PlanItem_stage').style.stroke).toBeFalsy()
  })

  it('lets the diagram scale instead of keeping its exported size', () => {
    const { container } = renderPanel({
      sourceDiagram: '<svg width="2031" height="591"><g/></svg>',
      stages: [],
    })

    const svg = container.querySelector('svg')
    expect(svg.getAttribute('width')).toBeNull()
    expect(svg.getAttribute('style')).toContain('width:100%')
  })

  // The engine refuses active content at upload; this is the second pass, for a
  // value stored before those rules existed or written by another route.
  it('strips script and event handlers from the stored markup', () => {
    const { container } = renderPanel({
      sourceDiagram:
        '<svg><script>window.pwned=1</script><rect onload="window.pwned=1"/></svg>',
      stages: [],
    })

    expect(container.querySelector('script')).toBeNull()
    expect(container.querySelector('rect')?.getAttribute('onload')).toBeNull()
  })

  it('says so when the case type has no diagram', () => {
    renderPanel({ sourceDiagram: null, stages })

    expect(
      screen.getByText('This case type has no diagram attached.'),
    ).toBeInTheDocument()
  })
})
