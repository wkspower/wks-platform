const Config = {
  CaseEngineUrl: process.env.REACT_APP_API_URL,
  LoginUrl: process.env.REACT_APP_KEYCLOAK_URL,
  StorageUrl: process.env.REACT_APP_STORAGE_URL,
  WebsocketsEnabled:
    process.env.REACT_APP_WEBSOCKETS_ENABLED === 'true' || false,
  WebsocketUrl: process.env.REACT_APP_WEBSOCKETS_URL,
  WebsocketsTopicCaseCreated: process.env.REACT_APP_WEBSOCKETS_CASE_CREATED,
  WebsocketsTopicHumanTaskCreated:
    process.env.REACT_APP_WEBSOCKETS_HUMAN_TASK_CREATED,
  NovuEnabled: process.env.REACT_APP_NOVU_ENABLED === 'true' || false,
  NovuPublisherApiUrl: process.env.REACT_APP_NOVU_PUBLISHER_API_URL,
  NovuAppId:
    process.env.REACT_APP_NOVU_ENABLED === 'true'
      ? await fetchNovuAppId()
      : undefined,
};

async function fetchNovuAppId() {
  try {
    const apiUrl = `${process.env.REACT_APP_NOVU_PUBLISHER_API_URL}/novu-app-id`;

    const response = await fetch(apiUrl);

    if (!response.ok) {
      throw new Error(`Failed to fetch NovuAppId. Status: ${response.status}`);
    }

    const data = await response.json();
    return data.novuAppId;
  } catch (error) {
    console.error('Error fetching NovuAppId:', error);
    return null;
  }
}
export default Config;
