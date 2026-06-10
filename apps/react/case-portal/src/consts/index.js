console.log(process.env.NODE_ENV)

// Each service location is configured via a three-leg contract that must stay
// in lockstep (see the CASE PORTAL block in the root .env for the canonical list):
//   REACT_APP_X  (build-time, .env)  <->  __SERVER_X__ (docker-compose env)  <->  window.X (public/index.html)
// getEnv() picks the build-time value in dev and the runtime window.* value in
// a built bundle. To add a service URL, add all three legs AND an entry here.
const Config = {
  CaseEngineUrl: getEnv(process.env.REACT_APP_API_URL, window.API_URL),
  AuthMode:
    getEnv(process.env.REACT_APP_AUTH_MODE, window.AUTH_MODE) || 'keycloak',
  AuthIssuerUrl: getEnv(
    process.env.REACT_APP_AUTH_ISSUER_URL,
    window.AUTH_ISSUER_URL,
  ),
  StorageUrl: getEnv(process.env.REACT_APP_STORAGE_URL, window.STORAGE_URL),
  StorageMode:
    getEnv(process.env.REACT_APP_STORAGE_MODE, window.STORAGE_MODE) || 'minio',
  WebsocketsEnabled: getEnv(
    process.env.REACT_APP_WEBSOCKETS_ENABLED,
    window.WEBSOCKETS_ENABLED,
  ),
  WebsocketUrl: getEnv(
    process.env.REACT_APP_WEBSOCKETS_URL,
    window.WEBSOCKETS_URL,
  ),
  WebsocketsTopicCaseCreated: getEnv(
    process.env.REACT_APP_WEBSOCKETS_CASE_CREATED,
    window.WEBSOCKETS_CASE_CREATED,
  ),
  WebsocketsTopicHumanTaskCreated: getEnv(
    process.env.REACT_APP_WEBSOCKETS_HUMAN_TASK_CREATED,
    window.WEBSOCKETS_HUMAN_TASK_CREATED,
  ),
  NovuEnabled: getEnv(process.env.REACT_APP_NOVU_ENABLED, window.NOVU_ENABLED),
  NovuPublisherApiUrl: getEnv(
    process.env.REACT_APP_NOVU_PUBLISHER_API_URL,
    window.NOVU_PUBLISHER_API_URL,
  ),
  NovuAppId:
    getEnv(process.env.REACT_APP_NOVU_ENABLED, window.NOVU_ENABLED) === 'true'
      ? await fetchNovuAppId()
      : undefined,
}

async function fetchNovuAppId() {
  try {
    const host = getEnv(
      process.env.REACT_APP_NOVU_PUBLISHER_API_URL,
      window.NOVU_PUBLISHER_API_URL,
    )

    const apiUrl = `${host}/novu-app-id`

    const response = await fetch(apiUrl)

    if (!response.ok) {
      throw new Error(`Failed to fetch NovuAppId. Status: ${response.status}`)
    }

    const data = await response.json()
    return data.novuAppId
  } catch (error) {
    console.error('Error fetching NovuAppId:', error)
    return null
  }
}

function getEnv(key, defaultValue) {
  const isDev = process.env.NODE_ENV === 'development'

  if (isDev && !!key) {
    return key
  }

  return defaultValue
}

export default Config
