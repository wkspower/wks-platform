import Config from 'consts'
import { json, nop } from './request'

/**
 * Imports a CMMN 1.1 model as a case definition.
 *
 * Mounted under /case-definition rather than a path of its own so it inherits the
 * case-definition authorization rule — see the controller for why that matters.
 *
 * The body is the raw model, not JSON: the file is uploaded as-is rather than being
 * wrapped, so nothing is lost or re-encoded on the way.
 */
export const CmmnImportService = {
  import: async (keycloak, cmmnXml, { dryRun = false } = {}) => {
    const response = await fetch(
      `${Config.CaseEngineUrl}/case-definition/import/cmmn?dryRun=${dryRun}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/xml',
          Authorization: `Bearer ${keycloak.token}`,
        },
        body: cmmnXml,
      },
    )

    return json(keycloak, response)
  },

  /**
   * Attaches the rendered diagram to an imported case type.
   *
   * Separate from the import because a diagram is a separate artifact — the model
   * says what the case does, the SVG is the picture of it, and one cannot be
   * derived from the other without a rendering library.
   */
  attachDiagram: async (keycloak, caseDefinitionId, svg) => {
    const response = await fetch(
      `${Config.CaseEngineUrl}/case-definition/${caseDefinitionId}/source-diagram`,
      {
        method: 'PUT',
        headers: {
          'Content-Type': 'image/svg+xml',
          Authorization: `Bearer ${keycloak.token}`,
        },
        body: svg,
      },
    )

    return nop(keycloak, response)
  },
}

export default CmmnImportService
