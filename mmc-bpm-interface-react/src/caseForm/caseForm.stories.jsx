import React from 'react';

import { CaseForm } from './caseForm';

export default {
    title: 'CaseForm',
    component: CaseForm
};

const Template = (args) => <CaseForm open={true} componentsParam={args} />;

export const Empty = Template.bind({});
Empty.args = {
    components: [
    ],
    variables: {},
    tasks: []
};

export const AFewComponents = Template.bind({});
AFewComponents.args = {
    components: [
        {
            id: 'firstName',
            key: 'firstName',
            type: 'textfield',
            text: null,
            label: 'Name'
        },
        {
            id: 'lastName',
            key: 'lastName',
            type: 'number',
            text: null,
            label: 'Age'
        }
    ],
    variables: {
        firstName: {
            value: 'John',
        },
        lastName: {
            value: 'James',
        }
    },
    tasks:
        [
            { id: '1', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '2', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '3', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '4', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '5', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '6', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '7', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '8', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '9', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '10', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '11', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '12', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '13', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '14', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '15', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '16', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '17', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '18', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '19', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '20', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '21', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '22', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '23', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '24', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
            { id: '25', name: 'Review Case', caseInstanceId: 'Generic Case', processDefinitionId: 'Generic Case Process', created: '01/01/2022' },
        ]
};