/* Minimal fixtures exercising the contract (happy path + each guard). */

const validCaseDefinition = {
  schemaVersion: '1.0',
  id: 'demo-type',
  name: 'Demo Type',
  formKey: 'demo-form',
  stagesLifecycleProcessKey: '',
  deployed: false,
  stages: [
    { id: '0', index: 0, name: 'Intake' },
    { id: '1', index: 1, name: 'Resolution' },
  ],
  caseHooks: [
    {
      eventType: 'TASK_COMPLETE_EVENT_TYPE',
      processDefKey: 'demo',
      taskDefKey: 'do-intake',
      actions: [{ actionType: 'CASE_STAGE_UPDATE_ACTION', newStage: 'Resolution' }],
    },
  ],
  kanbanConfig: {},
};

const validForm = {
  schemaVersion: '1.0',
  key: 'demo-form',
  title: 'Demo Form',
  toolTip: '',
  structure: {
    display: 'form',
    components: [
      { type: 'textfield', key: 'subject', label: 'Subject', input: true },
      { type: 'button', key: 'submit', label: 'Submit' },
    ],
  },
};

const validQueue = { schemaVersion: '1.0', id: 'q1', name: 'Queue One', description: '' };

const validRecordType = { schemaVersion: '1.0', id: 'rt1', fields: { components: [] } };

module.exports = { validCaseDefinition, validForm, validQueue, validRecordType };
