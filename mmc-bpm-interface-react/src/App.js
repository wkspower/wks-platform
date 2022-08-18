import React from "react";
import { Admin, Resource, CustomRoutes } from 'react-admin';
import { Route } from "react-router-dom";
import { CustomLayout } from "./customLayout.js";
import { CaseDefList } from './caseDefList/caseDefList'
import { CaseList } from './caseList/caseList'
import { TaskList } from './taskList/taskList'
import jsonServerProvider from 'ra-data-json-server';

const dataProvider = jsonServerProvider('http://localhost:8081');

const App = () => {


  return (
    <Admin layout={CustomLayout} dataProvider={dataProvider}>
      <Resource name="case" list={CaseList} />
      <CustomRoutes>
      <Route path="/caseDefList" element={<CaseDefList />} />
        <Route path="/caseList" element={<CaseList />} />
        <Route path="/taskList" element={<TaskList />} />
      </CustomRoutes>
    </Admin>
  )
};

export default App;