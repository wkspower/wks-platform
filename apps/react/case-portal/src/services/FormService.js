import { json, nop } from './request'
import Config from '../consts'

export const FormService = {
  getAll,
  getByKey,
  getVariableById,
  update,
  remove,
  create,
}

async function create(keycloak, body) {
  const url = `${Config.CaseEngineUrl}/form`

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(body),
    })
    return nop(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function update(keycloak, id, body) {
  const url = `${Config.CaseEngineUrl}/form/${id}`

  try {
    const resp = await fetch(url, {
      method: 'PATCH',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(body),
    })
    return nop(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function remove(keycloak, id) {
  const url = `${Config.CaseEngineUrl}/form/${id}`

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
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getAll(keycloak) {
  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  var url = `${Config.CaseEngineUrl}/form`

  try {
    const resp = await fetch(url, { headers })
    return json(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getByKey(keycloak, formKey) {
  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  var url = `${Config.CaseEngineUrl}/form/${formKey}`

  try {
    const resp = await fetch(url, { headers })

    const requested = await json(keycloak, resp)

    const data = requestRemoteDataSourceAndFillRecordTypesIfRequired(
      requested,
      keycloak,
    )

    return Promise.resolve(data)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getVariableById(keycloak, processInstanceId) {
  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  const body = JSON.stringify({ processInstanceId })

  var url = `${Config.CaseEngineUrl}/variable`

  try {
    const resp = await fetch(url, { headers, body })
    return json(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

function requestRemoteDataSourceAndFillRecordTypesIfRequired(
  original,
  keycloak,
) {
  function processComponentWithContext(components) {
    return components?.map((item) => {
      if (item.type === 'recordtype') {
        const options = item.customOptions
        const typeRender =
          options.inputType === 'selectone' ? 'select' : 'selectboxes'
        const template = options.template
        const recordId = options.recordType.id
        const valueProperty = options.valueProperty

        return {
          ...item,
          type: typeRender,
          dataSrc: 'url',
          template: `<span>${template}</span>`,
          valueProperty: valueProperty,
          data: {
            url: `${Config.CaseEngineUrl}/record/${recordId}`,
            headers: [
              {
                key: 'Authorization',
                value: `Bearer ${keycloak.token}`,
              },
            ],
          },
        }
      }

      if (item.components) {
        return {
          ...item,
          components: processComponentWithContext(item.components),
        }
      }

      return item
    })
  }

  return {
    ...original,
    structure: {
      ...original.structure,
      components: processComponentWithContext(original.structure?.components),
    },
  }
}
