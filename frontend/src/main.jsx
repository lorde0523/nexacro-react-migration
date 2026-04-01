// [MIGRATION] AS-IS: Nexacro Application Entry Point
// [TO-BE]: React 17 Entry Point (ReactDOM.render - createRoot 사용 금지)

import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import './assets/styles/global.css';

// React 17 방식: ReactDOM.render 사용 (createRoot 사용 금지)
ReactDOM.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
  document.getElementById('root')
);
