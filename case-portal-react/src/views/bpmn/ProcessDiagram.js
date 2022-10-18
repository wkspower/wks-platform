import { ReactBpmn } from './BpmnReact';

export const ProcessDiagram = ({ processDefinitionId, activityInstances, bpmEngineId }) => {
    const url = 'http://localhost:8081/process-definition/' + bpmEngineId + '/' + processDefinitionId + '/xml';

    return <ReactBpmn url={url} activities={activityInstances} />;
};
