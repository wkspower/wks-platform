import Config from 'consts/index'
import { json } from './request'

export class SlowdownNormForMegServices {
  static async getSlowdownNormsDataForMeg({ keycloak, PLANT_ID, year }) {
    const url = `${Config.CaseEngineUrl}/task/slowdown-consumption?plantId=${PLANT_ID}&year=${year}`
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
      return await Promise.reject(e)
    }
  }
  static async getSlowdownNormsColumnsForMeg({ keycloak, PLANT_ID, year }) {
    const url = `${Config.CaseEngineUrl}/task/slowdown-consumption-columns?plantId=${PLANT_ID}&year=${year}`
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
      return await Promise.reject(e)
    }
  }
  static async getSlowdownNormsCalculateForMeg({ keycloak, PLANT_ID, year }) {
    const url = `${Config.CaseEngineUrl}/task/calculate-slowdown-consumption?plantId=${PLANT_ID}&year=${year}`
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
      return await Promise.reject(e)
    }
  }
  static async updateSlowdownNormsForMeg({
    keycloak,
    PLANT_ID,
    year,
    payload,
  }) {
    const url = `${Config.CaseEngineUrl}/task/slowdown-consumption?plantId=${PLANT_ID}&year=${year}`
    const headers = {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      Authorization: `Bearer ${keycloak.token}`,
    }
    try {
      const resp = await fetch(url, {
        method: 'POST',
        headers,
        body: JSON.stringify(payload),
      })
      return json(keycloak, resp)
    } catch (e) {
      console.log(e)
      return await Promise.reject(e)
    }
  }
}
