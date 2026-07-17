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
  // Explicit auth realm/tenant. Empty = derive it from the browser hostname's
  // first DNS label (the historical behavior, which requires an FQDN); set it
  // to serve the portal from an IP address or a dotless hostname.
  Realm: optional(getEnv(process.env.REACT_APP_REALM, window.REALM)),
  StorageUrl: getEnv(process.env.REACT_APP_STORAGE_URL, window.STORAGE_URL),
  // How case documents are stored:
  //   'minio' | 'filesystem' — bytes go to storage-api (needs the storage profile);
  //   'inline'               — bytes travel on the document as base64, no storage-api
  //                            (the minimal, no-storage-api deployment).
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

// For OPTIONAL runtime vars: when index.html hasn't been envsubst'd (yarn
// start / tests) the window.X value is the raw "$__SERVER_X__" placeholder,
// which must not be mistaken for a configured value.
function optional(value) {
  if (!value || /^\$__.*__$/.test(value)) {
    return undefined
  }

  return value
}

export default Config
