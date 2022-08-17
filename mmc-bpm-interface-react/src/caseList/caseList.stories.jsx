import React from 'react';

import { CaseList } from './caseList';

export default {
    title: 'CaseList',
    component: CaseList
};

const Template = (args) => <CaseList {...args} />;

export const Empty = Template.bind({});
Empty.args = {
    cases: [
    ]
};

export const OneRow = Template.bind({});
OneRow.args = {
    cases: [
        { id: '1', status: 'New' }
    ]
};

export const ManyRows = Template.bind({});
ManyRows.args = {
    cases: [
        { id: '1', status: 'Review Case'},
        { id: '2', status: 'Review Case'},
        { id: '3', status: 'Review Case'},
        { id: '4', status: 'Review Case'},
        { id: '5', status: 'Review Case'},
        { id: '6', status: 'Review Case'},
        { id: '7', status: 'Review Case'},
        { id: '8', status: 'Review Case'},
        { id: '9', status: 'Review Case'},
        { id: '10', status: 'Review Case'},
        { id: '11', status: 'Review Case'},
        { id: '12', status: 'Review Case'},
        { id: '13', status: 'Review Case'},
        { id: '14', status: 'Review Case'},
        { id: '15', status: 'Review Case'},
        { id: '16', status: 'Review Case'},
        { id: '17', status: 'Review Case'},
        { id: '18', status: 'Review Case'},
        { id: '19', status: 'Review Case'},
        { id: '20', status: 'Review Case'},
        { id: '21', status: 'Review Case'},
        { id: '22', status: 'Review Case'},
        { id: '23', status: 'Review Case'},
        { id: '24', status: 'Review Case'},
        { id: '25', status: 'Review Case'},
    ]
};
