import { json, nop } from './request'
import Config from 'consts/index'

export const DataService = {
    getProductById, 
}

async function getProductById(keycloak, id) {
    const url = `${process.env.REACT_APP_API_URL}/product/product`; 
  
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


