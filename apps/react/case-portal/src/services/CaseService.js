import Config from '../consts'
import i18n from '../i18n'
import { json, nop } from './request'

export const CaseService = {
  getAllByStatus,
  getCaseDefinitions,
  getCaseDefinitionsById,
  getCaseById,
  filterCase,
  createCase,
  patch,
  addDocuments,
  addComment,
  updateComment,
  deleteComment,
  pollForCase,
}

async function pollForCase(
  keycloak,
  businessKey,
  maxAttempts = 15,
  interval = 10000,
) {
  let attempts = 0

  return new Promise((resolve, reject) => {
    const checkCase = async () => {
      if (attempts >= maxAttempts) {
        return reject()
      }

      try {
        const caseData = await getCaseById(keycloak, businessKey)
        if (caseData && caseData.businessKey === businessKey) {
          return resolve(caseData)
        }
        attempts++
        setTimeout(checkCase, interval)
      } catch (e) {
        attempts++
        setTimeout(checkCase, interval)
      }
    }

    checkCase()
  })
}
async function getAllByStatus(keycloak, status, limit) {
  if (!status) {
    return Promise.resolve([])
  }

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  var url = `${Config.CaseEngineUrl}/case?status=${status}&limit=${limit}`

  try {
    const resp = await fetch(url, { headers })
    const data = await json(keycloak, resp)
    return mapperToCase(data)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getCaseDefinitions(keycloak) {
  const url = `${Config.CaseEngineUrl}/case-definition?deployed=true`

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { headers })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getCaseDefinitionsById(keycloak, caseDefId) {
  const url = `${Config.CaseEngineUrl}/case-definition/${caseDefId || ''}`

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { headers })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getCaseById(keycloak, id) {
  let url = `${Config.CaseEngineUrl}/case/${id}`

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { headers })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function filterCase(keycloak, caseDefId, status, cursor) {
  let url = `${Config.CaseEngineUrl}/case?`
  url = url + (status ? `status=${status}` : '')
  url = url + (caseDefId ? `&caseDefinitionId=${caseDefId}` : '')
  url = url + `&before=${cursor.before || ''}`
  url = url + `&after=${cursor.after || ''}`
  url = url + `&sort=${cursor.sort || 'asc'}`
  url = url + `&limit=${cursor.limit || 10}`

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { headers })
    const data = await json(keycloak, resp)
    return mapperToCase(data)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function patch(keycloak, id, body) {
  const url = `${Config.CaseEngineUrl}/case/${id}`

  try {
    const resp = await fetch(url, {
      method: 'PATCH',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/merge-patch+json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: body,
    })
    return nop(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function createCase(keycloak, body) {
  const url = `${Config.CaseEngineUrl}/case`

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: body,
    })
    return json(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function addDocuments(keycloak, businessKey, document) {
  const url = `${Config.CaseEngineUrl}/case/${businessKey}/document`

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(document),
    })
    return nop(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function addComment(keycloak, text, parentId, businessKey) {
  const url = `${Config.CaseEngineUrl}/case/${businessKey}/comment`

  const comment = {
    body: text,
    parentId,
    userId: keycloak.tokenParsed.preferred_username,
    userName: keycloak.tokenParsed.given_name,
    caseId: businessKey,
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(comment),
    })
    return nop(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function updateComment(keycloak, text, commentId, businessKey) {
  const url = `${Config.CaseEngineUrl}/case/${businessKey}/comment/${commentId}`

  const comment = {
    id: commentId,
    body: text,
    userId: keycloak.tokenParsed.preferred_username,
    caseId: businessKey,
  }

  try {
    const resp = await fetch(url, {
      method: 'PUT',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(comment),
    })
    return nop(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function deleteComment(keycloak, commentId, businessKey) {
  const url = `${Config.CaseEngineUrl}/case/${businessKey}/comment/${commentId}`

  try {
    const resp = await fetch(url, {
      method: 'DELETE',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
    })
    return nop(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

function mapperToCase(resp) {
  const { data, paging } = resp

  if (!data.length) {
    return Promise.resolve({ data: [], paging: {} })
  }

  const toStatus = (status) => {
    const mapper = {
      WIP_CASE_STATUS: i18n.t('general.case.status.wip'),
      CLOSED_CASE_STATUS: i18n.t('general.case.status.closed'),
      ARCHIVED_CASE_STATUS: i18n.t('general.case.status.archived'),
    }

    return mapper[status] || '-'
  }

  const toCase = data.map((element) => {
    const createdAt = element?.attributes?.find(
      (attribute) => attribute.name === 'createdAt',
    )
    element.createdAt = createdAt ? createdAt.value : ''
    element.statusDescription = toStatus(element.status)
    return element
  })

  const toPaging = {
    cursors: paging.cursors,
    hasPrevious: paging.hasPrevious,
    hasNext: paging.hasNext,
  }

  return Promise.resolve({ data: toCase, paging: toPaging })
}
