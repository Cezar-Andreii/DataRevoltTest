import React from 'react';
import { useTagging } from './context/TaggingContext';
import EventSelector from './components/EventSelector/EventSelector';
import TaggingTable from './components/TaggingTable/TaggingTable';
import './App.css';
import './styles/tagging-style.css';
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
  const { loading, error } = useTagging();

  return (
    <div className="container-fluid" style={{ padding: '30px', background: 'linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)', minHeight: '100vh' }}>
      <div className="row">
        <div className="col-12">
          <div className="logo-container" style={{ marginBottom: '1rem' }}>
            <img src="/images/logo.png" alt="Data Revolt Logo" />
          </div>
          <h1 className="text-center mb-4">Tagging Plan - Event Tracking</h1>

          {error && (
            <div className="alert alert-danger alert-dismissible fade show" role="alert">
              <strong>Error:</strong> {error}
              <button type="button" className="btn-close" data-bs-dismiss="alert"></button>
            </div>
          )}

          {loading && (
            <div className="text-center">
              <div className="spinner-border" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
            </div>
          )}

          <EventSelector />

          <TaggingTable />
        </div>
      </div>
    </div>
  );
}

export default App;
