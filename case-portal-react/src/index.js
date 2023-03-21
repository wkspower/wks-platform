import { BrowserRouter } from 'react-router-dom';
import { createRoot } from 'react-dom/client';
import { Provider } from 'react-redux';
import 'simplebar/src/simplebar.css';
import 'assets/third-party/apex-chart.css';
import { store } from 'store';
import App from './App';
import reportWebVitals from './reportWebVitals';
import i18n from './i18n';
import { I18nextProvider } from 'react-i18next';

const container = document.getElementById('root');
const root = createRoot(container);
root.render(
    <Provider store={store}>
        <BrowserRouter basename={`${!!process.env.REACT_APP_CONTEXT_PATH || '/'}`}>
            <I18nextProvider i18n={i18n}>
                <App />
            </I18nextProvider>
        </BrowserRouter>
    </Provider>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
