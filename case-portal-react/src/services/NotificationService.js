import * as React from 'react';
import moment from 'moment';
import CaseService from './CaseService';
import { Typography } from '@mui/material';

function getNotifications() {
    function truncIfAboveFiveElements(data) {
        if (!data.length) {
            return Promise.resolve({ data: [], page: {} });
        }

        return Promise.resolve({
            data: data.splice(0, Math.min(4, data.length)),
            page: {
                total: data.length,
                limit: 5
            }
        });
    }

    function toMessage({ data, page }) {
        return Promise.resolve(
            data.map((it) => {
                function getEventType(s) {
                    const mapper = {
                        'Data Collection': 'data_collection_stg',
                        'Contract Writing': 'contract_writing_stg',
                        'Info & Docs Analysis': 'info_docs_analysis_stg'
                    };

                    return mapper[s] || 'data_collection_stg';
                }

                return {
                    ...data,
                    createdAt: moment(it.createdAt, 'DD/MM/YYYY').calendar(),
                    daysAgo: moment(it.createdAt, 'DD/MM/YYYY').startOf('day').fromNow(),
                    eventType: getEventType(it.stage),
                    total: page.total,
                    message: (
                        <Typography variant="h6">
                            Case{' '}
                            <Typography component="span" variant="subtitle1">
                                #{it.businessKey}
                            </Typography>{' '}
                            in{' '}
                            <Typography component="span" variant="subtitle1">
                                {it.stage}
                            </Typography>{' '}
                            stage
                        </Typography>
                    )
                };
            })
        );
    }

    return CaseService.getAllByStatus('WIP_CASE_STATUS', 5)
        .then(truncIfAboveFiveElements)
        .then(toMessage);
}

const NotificationService = {
    getNotifications: getNotifications
};

export default NotificationService;
