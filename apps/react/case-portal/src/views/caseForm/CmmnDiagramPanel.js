import { useEffect, useMemo, useRef } from 'react'
import PropTypes from 'prop-types'
import { Box, Chip, Stack, Typography } from '@mui/material'
import { useTheme } from '@mui/material/styles'
import { useTranslation } from 'react-i18next'
import DOMPurify from 'dompurify'

/**
 * The diagram this case type was modelled as, with the case's progress marked on it.
 *
 * Shows the drawing the customer authored rather than a rendering of our own, and
 * marks it up live: the stage the case is in is outlined, and each milestone reached
 * is filled. Stages and milestones carry the id of the element they were generated
 * from, and every shape in an exported diagram carries that id as `data-element-id`,
 * so the two can be matched without a modelling library — no cmmn-js, no extra
 * bundle.
 *
 * The markup is customer-supplied, so it is sanitised here before it goes into the
 * page. The engine already refuses anything active at upload; this is the second
 * pass, with a library built for the job, because a value may have been stored
 * before those rules existed.
 */
const CmmnDiagramPanel = ({
  sourceDiagram,
  stages,
  activeStage,
  achievedMilestoneIds,
}) => {
  const { t } = useTranslation()
  const theme = useTheme()
  const containerRef = useRef(null)

  // SVG needs to survive sanitising, so the profile has to allow it explicitly —
  // the default one strips it. USE_PROFILES is what keeps this from becoming a
  // hand-rolled allow-list.
  const safeMarkup = useMemo(
    () =>
      sourceDiagram
        ? DOMPurify.sanitize(sourceDiagram, {
            USE_PROFILES: { svg: true, svgFilters: true },
            ADD_ATTR: ['data-element-id'],
          })
        : null,
    [sourceDiagram],
  )

  useEffect(() => {
    const root = containerRef.current
    if (!root || !safeMarkup) return

    const svg = root.querySelector('svg')
    if (svg) {
      // Exported diagrams carry a fixed width/height. Let it scale to the panel
      // instead, or a wide model is cut off with no way to see the rest.
      svg.removeAttribute('width')
      svg.removeAttribute('height')
      svg.setAttribute('style', 'width:100%;height:auto;max-height:60vh')
    }

    const paint = (elementId, stroke, fill) => {
      if (!elementId) return
      const group = root.querySelector(
        `[data-element-id="${CSS.escape(elementId)}"]`,
      )
      if (!group) return

      // Only the visual layer: the hit/outline layers a modeller adds are invisible
      // helpers, and colouring them would draw boxes where there is no shape.
      group.querySelectorAll('.djs-visual > *').forEach((shape) => {
        shape.style.stroke = stroke
        shape.style.strokeWidth = '3px'
        if (fill) {
          shape.style.fill = fill
        }
      })
    }

    const current = stages?.find((stage) => stage.name === activeStage)
    paint(current?.sourceElementId, theme.palette.primary.main, null)

    stages?.forEach((stage) =>
      (stage.milestones ?? []).forEach((milestone) => {
        if (achievedMilestoneIds?.includes(milestone.id)) {
          paint(
            milestone.sourceElementId,
            theme.palette.success.dark,
            theme.palette.success.light,
          )
        }
      }),
    )
  }, [safeMarkup, stages, activeStage, achievedMilestoneIds, theme])

  if (!safeMarkup) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant='body2' color='text.secondary'>
          {t('pages.caseform.diagram.none')}
        </Typography>
      </Box>
    )
  }

  return (
    <Box sx={{ p: 2 }}>
      <Stack
        direction='row'
        spacing={1}
        sx={{ mb: 1, flexWrap: 'wrap', gap: 0.5 }}
      >
        <Chip
          size='small'
          color='primary'
          variant='outlined'
          label={t('pages.caseform.diagram.currentStage')}
        />
        <Chip
          size='small'
          color='success'
          variant='outlined'
          label={t('pages.caseform.diagram.achieved')}
        />
      </Stack>

      <Box
        ref={containerRef}
        sx={{
          overflow: 'auto',
          border: 1,
          borderColor: 'divider',
          borderRadius: 1,
          p: 1,
        }}
        dangerouslySetInnerHTML={{ __html: safeMarkup }}
      />
    </Box>
  )
}

CmmnDiagramPanel.propTypes = {
  sourceDiagram: PropTypes.string,
  stages: PropTypes.arrayOf(
    PropTypes.shape({
      name: PropTypes.string,
      sourceElementId: PropTypes.string,
      milestones: PropTypes.array,
    }),
  ),
  activeStage: PropTypes.string,
  achievedMilestoneIds: PropTypes.arrayOf(PropTypes.string),
}

export default CmmnDiagramPanel
