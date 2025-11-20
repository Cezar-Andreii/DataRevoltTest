import React, { useState, useEffect, useRef } from 'react';
import { useTagging } from '../../context/TaggingContext';
import ItemsArrayModal from '../ItemsArrayModal/ItemsArrayModal';
import CustomEventModal from '../CustomEventModal/CustomEventModal';
import UserDataModal from '../UserDataModal/UserDataModal';

const EventSelector = () => {
  const { events, selectedEvents, setSelectedEvents, loadInitialData, loading, generateTaggingPlan } = useTagging();
  const [localSelectedEvents, setLocalSelectedEvents] = useState([]);
  const [showItemsModal, setShowItemsModal] = useState(false);
  const [showCustomEventModal, setShowCustomEventModal] = useState(false);
  const [showUserDataModal, setShowUserDataModal] = useState(false);
  const [selectedItems, setSelectedItems] = useState(['item_id', 'item_name', 'item_category']);
  const [selectedPlatforms, setSelectedPlatforms] = useState([]);
  const [selectedUserDataParams, setSelectedUserDataParams] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const hasLoadedRef = useRef(false);

  useEffect(() => {
    // Încarcă datele doar o dată la mount
    if (!hasLoadedRef.current) {
      hasLoadedRef.current = true;
      loadInitialData();
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // Sincronizează cu context doar când se schimbă selectedEvents din exterior
  useEffect(() => {
    if (selectedEvents.length === 0 && localSelectedEvents.length > 0) {
      // Nu face nimic - păstrează selecțiile locale
      return;
    }
    if (JSON.stringify(selectedEvents) !== JSON.stringify(localSelectedEvents)) {
      setLocalSelectedEvents(selectedEvents);
    }
  }, [selectedEvents]); // eslint-disable-line react-hooks/exhaustive-deps

  // Actualizează context-ul când se schimbă selecțiile locale
  useEffect(() => {
    if (JSON.stringify(localSelectedEvents) !== JSON.stringify(selectedEvents)) {
      setSelectedEvents(localSelectedEvents);
    }
  }, [localSelectedEvents]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleEventToggle = (eventName) => {
    setLocalSelectedEvents(prev => {
      const newSelection = prev.includes(eventName)
        ? prev.filter(e => e !== eventName)
        : [...prev, eventName];
      return newSelection;
    });
  };

  const handleSelectAll = () => {
    if (events.length === 0) return;
    const allEventNames = events.map(e => e.event);
    setLocalSelectedEvents(allEventNames);
  };

  const handleDeselectAll = () => {
    setLocalSelectedEvents([]);
  };

  // Filtrare evenimente
  const filteredEvents = events.filter(event => {
    if (!searchTerm) return true;
    const searchLower = searchTerm.toLowerCase();
    return (
      event.event.toLowerCase().includes(searchLower) ||
      event.eventName.toLowerCase().includes(searchLower) ||
      (event.purpose && event.purpose.toLowerCase().includes(searchLower)) ||
      (event.trigger && event.trigger.toLowerCase().includes(searchLower))
    );
  });

  const handleGenerate = () => {
    if (localSelectedEvents.length === 0) {
      alert('Te rugăm să selectezi cel puțin un eveniment!');
      return;
    }
    
    // Verifică dacă userData este selectat
    if (localSelectedEvents.includes('userData')) {
      setShowUserDataModal(true);
    } else {
      setShowItemsModal(true);
    }
  };

  const handleItemsConfirm = async (items, platforms) => {
    console.log('=== EventSelector: handleItemsConfirm ===');
    console.log('Items:', items);
    console.log('Platforms:', platforms);
    console.log('Platforms type:', typeof platforms);
    console.log('Platforms is array:', Array.isArray(platforms));
    if (platforms && platforms.length > 0) {
      platforms.forEach((p, i) => {
        console.log(`Platform[${i}]: '${p}' (type: ${typeof p})`);
      });
    }
    setSelectedItems(items);
    setSelectedPlatforms(platforms || []);
    try {
      await generateTaggingPlan(localSelectedEvents, items, platforms);
      // Deselectează toate evenimentele după generarea cu succes
      setLocalSelectedEvents([]);
      setSelectedEvents([]);
      alert('Tagging plan generat cu succes!');
    } catch (error) {
      alert('Eroare la generarea tagging plan: ' + error.message);
    }
  };

  const handleUserDataConfirm = async (params) => {
    console.log('=== EventSelector: handleUserDataConfirm ===');
    console.log('Selected UserData Params:', params);
    setSelectedUserDataParams(params);
    try {
      await generateTaggingPlan(['userData'], params, ['WEB']);
      // Deselectează toate evenimentele după generarea cu succes
      setLocalSelectedEvents([]);
      setSelectedEvents([]);
      alert('Tagging plan generat cu succes!');
    } catch (error) {
      alert('Eroare la generarea tagging plan: ' + error.message);
    }
  };

  if (loading && events.length === 0) {
    return (
      <div className="card mb-4">
        <div className="card-body text-center">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="card mb-4">
      <div className="card-header">
        <h5>Selectează Evenimente pentru Generare Automată</h5>
        <small className="text-muted">
          Selectează unul sau mai multe evenimente. Parametrii oficiali vor fi adăugați automat.
        </small>
      </div>
      <div className="card-body">
        <div className="row">
          <div className="col-12">
            <label className="form-label">Evenimente disponibile ({events.length}):</label>
            
            {/* Căutare */}
            <div className="mb-3">
              <div className="input-group">
                <span className="input-group-text">🔍</span>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Caută evenimente (nume, descriere, trigger)..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
                {searchTerm && (
                  <button
                    className="btn btn-outline-secondary"
                    type="button"
                    onClick={() => setSearchTerm('')}
                    title="Șterge căutare"
                  >
                    ✕
                  </button>
                )}
              </div>
              {searchTerm && (
                <small className="text-muted">
                  {filteredEvents.length} evenimente găsite din {events.length}
                </small>
              )}
            </div>

            {events.length === 0 ? (
              <p className="text-muted">Nu sunt evenimente disponibile. Verifică conexiunea la backend.</p>
            ) : filteredEvents.length === 0 ? (
              <p className="text-muted">Nu s-au găsit evenimente care să corespundă căutării "{searchTerm}".</p>
            ) : (
              <div
                className="events-grid"
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                  gap: '15px',
                  maxHeight: '400px',
                  overflowY: 'auto',
                  padding: '10px',
                  border: '1px solid #dee2e6',
                  borderRadius: '8px',
                  backgroundColor: '#f8f9fa'
                }}
              >
                {filteredEvents.map((event) => (
                <div
                  key={event.event}
                  className="form-check event-card"
                  style={{
                    background: localSelectedEvents.includes(event.event) ? '#e3f2fd' : 'white',
                    padding: '15px',
                    borderRadius: '8px',
                    border: `1px solid ${localSelectedEvents.includes(event.event) ? '#2196f3' : '#e9ecef'}`,
                    transition: 'all 0.2s ease',
                    transform: localSelectedEvents.includes(event.event) ? 'scale(1.02)' : 'scale(1)'
                  }}
                >
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id={`event_${event.event}`}
                    checked={localSelectedEvents.includes(event.event)}
                    onChange={() => handleEventToggle(event.event)}
                    style={{ transform: 'scale(1.2)' }}
                  />
                  <label
                    className="form-check-label"
                    htmlFor={`event_${event.event}`}
                    style={{ cursor: 'pointer', width: '100%' }}
                  >
                    <div className="event-info">
                      <div
                        className="event-name"
                        style={{
                          fontWeight: 'bold',
                          color: '#495057',
                          marginBottom: '5px'
                        }}
                      >
                        {event.eventName}
                      </div>
                      <div
                        className="event-description"
                        style={{
                          fontSize: '0.9em',
                          color: '#6c757d',
                          marginBottom: '8px'
                        }}
                      >
                        {event.purpose}
                      </div>
                      <div
                        className="event-trigger"
                        style={{
                          fontSize: '0.8em',
                          color: '#868e96',
                          fontStyle: 'italic'
                        }}
                      >
                        Trigger: {event.trigger}
                      </div>
                    </div>
                  </label>
                </div>
                ))}
              </div>
            )}
            <div className="mt-3">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <button
                    type="button"
                    className="btn btn-outline-secondary btn-sm me-2"
                    onClick={handleSelectAll}
                  >
                    Selectează Tot
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline-secondary btn-sm me-2"
                    onClick={handleDeselectAll}
                  >
                    Deselectează Tot
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline-success btn-sm"
                    onClick={() => setShowCustomEventModal(true)}
                  >
                    ➕ Creează Event Custom
                  </button>
                </div>
                <div>
                  <span
                    className={`badge ${localSelectedEvents.length > 0 ? 'bg-success' : 'bg-info'}`}
                  >
                    {localSelectedEvents.length} evenimente selectate
                  </span>
                </div>
              </div>
            </div>
            <div className="mt-4 d-flex gap-2 align-items-center">
              <button
                type="button"
                className="btn btn-primary btn-lg"
                onClick={handleGenerate}
                disabled={localSelectedEvents.length === 0}
              >
                🎯 Generează Tabel cu Parametrii Oficiali
              </button>
              <a
                href="https://developers.google.com/analytics/devguides/collection/ga4/reference/events"
                target="_blank"
                rel="noopener noreferrer"
                className="btn btn-outline-primary btn-lg"
              >
                📚 Link către documentația oficială
              </a>
            </div>
          </div>
        </div>
      </div>
      <ItemsArrayModal
        show={showItemsModal}
        onClose={() => setShowItemsModal(false)}
        onConfirm={handleItemsConfirm}
        selectedItems={selectedItems}
        selectedPlatforms={selectedPlatforms}
      />
      <CustomEventModal
        show={showCustomEventModal}
        onClose={() => setShowCustomEventModal(false)}
      />
      <UserDataModal
        show={showUserDataModal}
        onClose={() => setShowUserDataModal(false)}
        onConfirm={handleUserDataConfirm}
        selectedParams={selectedUserDataParams}
      />
    </div>
  );
};

export default EventSelector;

