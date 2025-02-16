import { json, nop } from './request'
import Config from 'consts/index'

export const DataService = {
    getProductById, 
    getYearWiseProduct
}

async function getProductById(keycloak, id) {
    const url = `${process.env.REACT_APP_API_URL}/task/productList`; 
  
    const headers = {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      Authorization: `Bearer ${keycloak.token}`,
    };
  
    try {
      const resp = await fetch(url, { method: 'GET', headers });
      return json(keycloak, resp);
    } catch (e) {
      console.log(e);
      return await Promise.reject(e);
    }
  }

async function getYearWiseProduct(keycloak, id) {
    var type = 'Business Demand Data'
    var year = '2025'
    const url = `${process.env.REACT_APP_API_URL}/task/getMonthWiseData?type=${type}&year=${year}`; 
  
    const headers = {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      Authorization: `Bearer ${keycloak.token}`,
    };
  
    try {
      const resp = await fetch(url, { method: 'GET', headers });
      return json(keycloak, resp);
    } catch (e) {
      console.log(e);
      return await Promise.reject(e);
    }
  }

