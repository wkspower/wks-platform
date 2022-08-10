import React from "react";
import { Admin, Resource, CustomRoutes } from 'react-admin';
import { Route } from "react-router-dom";
import { CustomLayout } from "./customLayout.js";
import { CaseList } from './case/cases.js';
import { TaskList } from './tasklist/tasklist'
import jsonServerProvider from 'ra-data-json-server';

const dataProvider = jsonServerProvider('http://localhost:8081');

const App = () => {


  return (
    <Admin layout={CustomLayout} dataProvider={dataProvider}>
      <Resource name="case" list={CaseList} />
      <CustomRoutes>
        <Route path="/tasklist" element={<TaskList />} />
      </CustomRoutes>
    </Admin>
  )
};

export default App;