import React from 'react';

import { TaskList } from './tasklist';

export default {
    title: 'TaskList',
    component: TaskList
};

const Template = (args) => <TaskList {...args} />;

export const Empty = Template.bind({});
Empty.args = {
    data: {
        rows : [
        ]
    }
};

export const OneRow = Template.bind({});
OneRow.args = {
    data: {
        rows : [
            { id: '1', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' }
        ]
    }
};

export const ManyRows = Template.bind({});
ManyRows.args = {
    data: {
        rows : [
            { id: '1', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '2', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '3', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '4', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '5', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '6', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '7', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '8', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '9', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '10', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '11', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '12', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '13', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '14', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '15', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '16', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '17', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '18', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '19', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '20', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '21', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '22', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '23', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '24', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
            { id: '25', task: 'Review Case', case: 'Generic Case', process: 'Generic Case Process', createdAt: '01/01/2022' },
        ]
    }
};