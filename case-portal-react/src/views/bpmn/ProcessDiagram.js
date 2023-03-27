import { ReactBpmn } from './BpmnReact';

export const ProcessDiagram = ({ processDefinitionId, activityInstances }) => {
    const url = process.env.REACT_APP_API_URL + '/process-definition/' + processDefinitionId + '/xml';

    return <ReactBpmn url={url} activities={activityInstances} />;
};
