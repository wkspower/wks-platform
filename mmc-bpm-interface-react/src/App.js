import React from "react";
import { Admin, Resource, CustomRoutes } from 'react-admin';
import { Route } from "react-router-dom";
import { CustomLayout } from "./customLayout.js";
import { CaseDefList } from "./admin/caseAndProcessManagement/caseDef/caseDefList/caseDefList.jsx";
import { CaseList } from "./caseList/caseList";
import { TaskList } from "./taskList/taskList.jsx";
import { EventTypeList } from "./admin/caseAndProcessManagement/eventType/eventTypeList.jsx";
import jsonServerProvider from 'ra-data-json-server';

const dataProvider = jsonServerProvider('http://localhost:8081');

const App = () => {


  return (
    <Admin layout={CustomLayout} dataProvider={dataProvider}>
      <Resource name="case" list={CaseList} />
      <CustomRoutes>
        <Route path="/eventTypeList" element={<EventTypeList />} />
        <Route path="/caseDefList" element={<CaseDefList />} />
        <Route path="/caseList" element={<CaseList />} />
        <Route path="/taskList" element={<TaskList />} />
      </CustomRoutes>
    </Admin>
  )
};

export default App;