import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { EtudeProvider } from '@etude/ui';
import '@etude/ui/styles.css';
import { App } from './app/App';
import { ErrorBoundary } from './app/ErrorBoundary';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <EtudeProvider>
      <ErrorBoundary>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </ErrorBoundary>
    </EtudeProvider>
  </React.StrictMode>,
);
