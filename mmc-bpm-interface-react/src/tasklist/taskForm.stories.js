import React from 'react';

import { TaskForm } from './taskForm';

export default {
    title: 'TaskForm',
    component: TaskForm
};

const Template = (args) => <TaskForm open={true} {...args} />;

export const Empty = Template.bind({});
Empty.args = {
    components: [
    ]
};

export const AFewComponents = Template.bind({});
AFewComponents.args = {
    components: [
        {
            id: 'Field_0o3o6jw',
            key: null,
            type: 'text',
            text: 'Person Record',
            label: null
        },
        {
            id: 'Field_034vurz',
            key: 'field_1ctm6kb',
            type: 'textfield',
            text: null,
            label: 'Name'
        },
        {
            id: 'Field_1et5npe',
            key: 'field_0su9fo3',
            type: 'number',
            text: null,
            label: 'Age'
        }
    ]
};