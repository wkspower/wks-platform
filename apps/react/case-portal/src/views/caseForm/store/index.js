import { CaseService, FileService } from '../../../services'

const CaseStore = {
  saveDocumentsFromFiles,
}

async function saveDocumentsFromFiles(
  keycloak,
  files,
  businessKey,
  progressCallback,
) {
  return Promise.all(
    files.map((file) => {
      const args = {
        dir: 'cases',
        file: file,
        keycloak,
        progress: (e, percent) => {
          progressCallback(percent)
        },
      }

      return FileService.upload(args)
        .then((data) => saveDocument(keycloak, businessKey, data))
        .catch(() => {
          return Promise.reject(
            `Could't upload this file "${file.name}", try again with other file.`,
          )
        })
    }),
  )
}

async function saveDocument(keycloak, businessKey, document) {
  // addDocuments() now rejects on any non-OK status (see services/request.js),
  // so a failed upload lands in catch instead of returning a non-ok Response.
  try {
    await CaseService.addDocuments(keycloak, businessKey, document)
    return Promise.resolve(document)
  } catch (e) {
    return Promise.reject(e)
  }
}

export default CaseStore
