import Config from '../consts'
import { json } from './request'
export const DataSetaApiService = {
  bestAchievedMinCCExport,
  getQualityPackagingBasis,
}

export async function bestAchievedMinCCExport(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  VERTICAL_NAME,
) {
  const url = `${Config.CaseEngineUrl}/task/best-achieved-export?year=${AOP_YEAR}&plantId=${PLANT_ID}`
  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'GET',
      headers,
    })
    if (!resp.ok) {
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }
    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = `Best Achieved MinCC.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Best Achieved MinCC Export Excel:', e)
    return Promise.reject(e)
  }
}

async function getQualityPackagingBasis(
  keycloak,
  reportType,
  periodFrom,
  periodTo,
  mode,
  PLANT_ID,
  AOP_YEAR,
) {
  let url = `${Config.CaseEngineUrl}/task/data-set-quality-packaging?plantId=${PLANT_ID}&year=${AOP_YEAR}`

  if (periodFrom !== undefined) {
    url += `&periodFrom=${periodFrom}`
  }

  if (periodTo !== undefined) {
    url += `&periodTo=${periodTo}`
  }

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return Promise.reject(e)
  }
}
