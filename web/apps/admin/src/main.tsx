import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { EtudeProvider } from '@etude/ui';
import './styles.css';
import { App } from './app/App';
import { ErrorBoundary } from './app/ErrorBoundary';
import { RootErrorFallback } from './app/RootErrorFallback';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ErrorBoundary fallback={<RootErrorFallback />}>
      <EtudeProvider>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </EtudeProvider>
    </ErrorBoundary>
  </React.StrictMode>,
);
