// Jest mock for src/consts. The real module uses a top-level `await` (for the
// optional Novu app id), which forces ESM parsing that jest's CommonJS transform
// rejects. Tests don't need live config — this static shape stands in for it and
// keeps any module that imports `consts` loadable under jest.
const Config = {
  CaseEngineUrl: 'http://localhost:8081',
  AuthMode: 'keycloak',
  AuthIssuerUrl: 'http://localhost:8082',
  StorageUrl: 'http://localhost:8085',
  StorageMode: 'minio',
  WebsocketsEnabled: 'false',
  WebsocketUrl: 'ws://localhost:8484',
  WebsocketsTopicCaseCreated: 'case-create',
  WebsocketsTopicHumanTaskCreated: 'human-task-create',
  NovuEnabled: 'false',
  NovuPublisherApiUrl: 'http://localhost:3002',
  NovuAppId: undefined,
}

export default Config
