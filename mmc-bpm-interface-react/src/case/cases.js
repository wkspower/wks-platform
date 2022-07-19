import * as React from "react";
import { List, Datagrid, TextField, EmailField } from 'react-admin';

export const CaseList = () => (
    <List>
        <Datagrid rowClick="edit">
            <TextField source="businessKey" />
            <TextField source="attributes" />
            <TextField source="processesInstances" />
        </Datagrid>
    </List>
);