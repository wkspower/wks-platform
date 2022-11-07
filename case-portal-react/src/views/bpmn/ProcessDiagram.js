import { ReactBpmn } from './BpmnReact';

export const ProcessDiagram = ({ processDefinitionId, activityInstances, bpmEngineId }) => {
    const url = process.env.REACT_APP_API_URL + '/process-definition/' + bpmEngineId + '/' + processDefinitionId + '/xml';

    return <ReactBpmn url={url} activities={activityInstances} />;
};
