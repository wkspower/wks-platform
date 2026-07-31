import Config from 'consts'
import { json } from './request'

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
}

export default CmmnImportService
