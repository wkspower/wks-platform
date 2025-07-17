import Config from 'consts/index'
import { json } from './request'

export class SlowdownNormForMegServices {
  static async getSlowdownNormsDataForMeg({ keycloak, plantId, year }) {
    const url = `${Config.CaseEngineUrl}/task/slowdown-norms/configuration?plantId=${plantId}&year=${year}`
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
  static async getSlowdownNormsColumnsForMeg({ keycloak, plantId, year }) {
    const url = `${Config.CaseEngineUrl}/task/slowdown-norms/dynamic/columns?plantId=${plantId}&year=${year}`
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
  static async getSlowdownNormsCalculateForMeg({ keycloak, plantId, year }) {
    const url = `${Config.CaseEngineUrl}/task/slowdown-norms/calculate?plantId=${plantId}&year=${year}`
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
  static async updateSlowdownNormsForMeg({ keycloak, plantId, year, payload }) {
    const url = `${Config.CaseEngineUrl}/task/slowdown-norms/configuration?plantId=${plantId}&year=${year}`
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
