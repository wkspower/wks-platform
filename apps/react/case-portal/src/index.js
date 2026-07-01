import { BrowserRouter } from 'react-router-dom'
import { createRoot } from 'react-dom/client'
import { Provider } from 'react-redux'
import { store } from './store'
import App from './App'
import i18n from './i18n'
import { I18nextProvider } from 'react-i18next'
import ErrorBoundary from './components/ErrorBoundary/ErrorBoundary'

const container = document.getElementById('root')
const root = createRoot(container)
root.render(
  // App-root boundary: a crash during bootstrap/auth degrades to a recoverable
  // message instead of a blank page.
  <ErrorBoundary title='The application failed to load'>
    <Provider store={store}>
      <BrowserRouter basename='/'>
        <I18nextProvider i18n={i18n}>
          <App />
        </I18nextProvider>
      </BrowserRouter>
    </Provider>
  </ErrorBoundary>,
)
