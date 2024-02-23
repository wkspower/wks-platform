const Config = {
    CaseEngineUrl: process.env.REACT_APP_API_URL,
    LoginUrl: process.env.REACT_APP_KEYCLOAK_URL,
    EmailUrl: process.env.REACT_APP_EMAIL_URL,
    StorageUrl: process.env.REACT_APP_STORAGE_URL,
    WebsocketsEnabled: process.env.REACT_APP_WEBSOCKETS_ENABLED === 'true' || false,
    WebsocketsTopicCaseCreated: process.env.REACT_APP_WEBSOCKETS_CASE_CREATED,
    WebsocketsTopicHumanTaskCreated: process.env.REACT_APP_WEBSOCKETS_HUMAN_TASK_CREATED,
    NovuEnabled: process.env.REACT_APP_NOVU_ENABLED === 'true' || false,
    NovuAppId: process.env.REACT_APP_NOVU_APP_ID,
};

export default Config;
