import { ReactBpmn } from './BpmnReact';

export const ProcessDiagram = ({ processDefinitionId, activityInstances }) => {
    const url = 'http://localhost:8081/process-definition/' + processDefinitionId + '/xml';

    return <ReactBpmn url={url} activities={activityInstances} />;
};
