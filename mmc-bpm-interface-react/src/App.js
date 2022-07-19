import * as React from "react";
import { Admin, Resource } from 'react-admin';
import { CaseList } from './case/cases.js';
import jsonServerProvider from 'ra-data-json-server';

const dataProvider = jsonServerProvider('http://localhost:8081');

const App = () => (
  <Admin dataProvider={dataProvider}>
    <Resource name="case" list={CaseList} />
  </Admin>
);

export default App;