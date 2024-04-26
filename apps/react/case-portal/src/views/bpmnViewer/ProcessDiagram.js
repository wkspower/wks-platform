import { ReactBpmn } from './BpmnReact'
import Config from 'consts/index'

export const ProcessDiagram = ({ processDefinitionId, activityInstances }) => {
  const url = `${Config.CaseEngineUrl}/process-definition/${processDefinitionId}/xml`

  return <ReactBpmn url={url} activities={activityInstances} />
}
