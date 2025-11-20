import React, { useState, useEffect, useRef } from 'react';

const EventDropdownCell = ({ rowIndex, events, allEvents, onEventSelect, currentValue }) => {
  // Folosim un ref pentru a ține minte dacă am făcut deja modificări locale
  const hasLocalChanges = useRef(false);
  
  // currentValue poate fi un array de evenimente selectate sau un string
  const [selectedEvents, setSelectedEvents] = useState(() => {
    if (Array.isArray(currentValue) && currentValue.length > 0) {
      return currentValue;
    }
    if (currentValue) {
      return [currentValue];
    }
    return [];
  });

  useEffect(() => {
    // Actualizează doar dacă nu avem modificări locale și currentValue are date noi
    if (!hasLocalChanges.current) {
      const newValue = Array.isArray(currentValue) ? currentValue : (currentValue ? [currentValue] : []);
      if (JSON.stringify(newValue) !== JSON.stringify(selectedEvents)) {
        setSelectedEvents(newValue);
      }
    }
  }, [currentValue]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleAddEvent = (e) => {
    const eventCode = e.target.value;
    if (!eventCode || eventCode === '') return;

    // Verifică dacă evenimentul nu este deja selectat
    if (!selectedEvents.includes(eventCode)) {
      hasLocalChanges.current = true;
      const newSelectedEvents = [...selectedEvents, eventCode];
      setSelectedEvents(newSelectedEvents);
      
      if (onEventSelect) {
        const eventsToUse = allEvents || events;
        const selectedEventObjects = newSelectedEvents.map(code => {
          const event = eventsToUse.find(ev => ev.event === code);
          return {
            code: code,
            name: event ? event.eventName : code
          };
        });
        onEventSelect(rowIndex, newSelectedEvents, selectedEventObjects);
      }
    }
    
    // Resetează dropdown-ul
    e.target.value = '';
  };

  const handleRemoveEvent = (eventCodeToRemove) => {
    hasLocalChanges.current = true;
    const newSelectedEvents = selectedEvents.filter(code => code !== eventCodeToRemove);
    setSelectedEvents(newSelectedEvents);
    
    if (onEventSelect) {
      const eventsToUse = allEvents || events;
      const selectedEventObjects = newSelectedEvents.map(code => {
        const event = eventsToUse.find(ev => ev.event === code);
        return {
          code: code,
          name: event ? event.eventName : code
        };
      });
      onEventSelect(rowIndex, newSelectedEvents, selectedEventObjects);
    }
  };

  // Folosim allEvents dacă este disponibil, altfel events
  const eventsToUse = allEvents || events;
  
  // Evenimente disponibile (exclude pe cele deja selectate)
  const availableEvents = eventsToUse.filter(event => {
    const eventCode = event.event || event.eventName;
    return !selectedEvents.includes(eventCode);
  });

  return (
    <div style={{ width: '100%' }}>
      {/* Tag-uri pentru evenimentele selectate */}
      {selectedEvents.length > 0 && (
        <div style={{ 
          display: 'flex', 
          flexWrap: 'wrap', 
          gap: '4px', 
          marginBottom: '6px',
          minHeight: '24px'
        }}>
          {selectedEvents.map((eventCode) => {
            const eventsToUse = allEvents || events;
            const event = eventsToUse.find(ev => (ev.event || ev.eventName) === eventCode);
            const eventName = event ? (event.eventName || event.event) : eventCode;
            return (
              <span
                key={eventCode}
                className="badge"
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '4px',
                  fontSize: '11px',
                  padding: '4px 8px',
                  cursor: 'default',
                  backgroundColor: 'white',
                  color: 'black',
                  border: '1px solid #ced4da'
                }}
              >
                {eventName}
                <button
                  type="button"
                  className="btn-close"
                  style={{
                    fontSize: '10px',
                    padding: '0',
                    margin: '0',
                    width: '12px',
                    height: '12px',
                    filter: 'invert(1)'
                  }}
                  onClick={() => handleRemoveEvent(eventCode)}
                  aria-label="Remove"
                ></button>
              </span>
            );
          })}
        </div>
      )}
      
      {/* Dropdown pentru a adăuga evenimente noi */}
      <select
        className="form-select form-select-sm"
        onChange={handleAddEvent}
        defaultValue=""
        style={{
          width: '100%',
          padding: '6px 8px',
          fontSize: '13px',
          border: '1px solid #ced4da',
          borderRadius: '4px',
          backgroundColor: 'white'
        }}
      >
        <option value="">-- Adaugă Event --</option>
        {availableEvents.length > 0 ? (
          availableEvents.map((event, index) => (
            <option key={index} value={event.event || event.eventName}>
              {event.eventName || event.event}
            </option>
          ))
        ) : (
          <option value="" disabled>Toate evenimentele sunt selectate</option>
        )}
      </select>
    </div>
  );
};

export default EventDropdownCell;

