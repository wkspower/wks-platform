import React from 'react';

import { TaskForm } from './taskForm';

export default {
    title: 'TaskForm',
    component: TaskForm
};

const Template = (args) => <TaskForm {...args} open={true}/>;

export const Empty = Template.bind({});
Empty.args = {
    form: [
    ]
};