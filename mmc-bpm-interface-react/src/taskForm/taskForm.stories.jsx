import React from 'react';

import { TaskForm } from './taskForm';

export default {
    title: 'TaskForm',
    component: TaskForm
};

const Template = (args) => <TaskForm open={true} componentsParam={args} />;

export const Empty = Template.bind({});
Empty.args = {
    components: [
    ],
    variablesParam: {}
};

export const AFewComponents = Template.bind({});
AFewComponents.args = {
    components: [
        {
            id: 'label',
            key: 'label',
            type: 'text',
            text: 'Person Record',
            label: null
        },
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
    }
};