import React, { useState } from 'react';
import { useTagging } from '../../context/TaggingContext';
import ExportButtons from '../ExportButtons/ExportButtons';
import AddCustomRowModal from '../AddCustomRowModal/AddCustomRowModal';
import SortableRow from './SortableRow';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';

const TaggingTable = () => {
  const { taggingRows, setTaggingRows, updateCell, deleteRow, loading, events, allEvents, selectedPlatform, setSelectedPlatform } = useTagging();
  const [editingCell, setEditingCell] = useState(null);
  const [showAddCustomRowModal, setShowAddCustomRowModal] = useState(false);
  const [showEventColumn, setShowEventColumn] = useState(false);
  const [sortConfig, setSortConfig] = useState({ 
    key: null,
    direction: null
  });

  // Filtrează rândurile în funcție de platforma selectată
  // Dacă este selectată o platformă, arată doar rândurile pentru acea platformă
  // Dacă nu este selectată nicio platformă, arată toate rândurile
  const filteredTaggingRows = React.useMemo(() => {
    if (!selectedPlatform || selectedPlatform === '') {
      return taggingRows;
    }
    
    // Filtrează strict după platformă
    // Exclude rândurile vechi care au "Ecommerce" sau "" ca eventCategory
    const validPlatforms = ['WEB', 'Android', 'iOS'];
    const filtered = taggingRows.filter(row => {
      const rowPlatform = (row.eventCategory || '').trim();
      const selectedPlatformTrimmed = (selectedPlatform || '').trim();
      
      // Exclude rândurile vechi care nu au platformă validă
      if (!validPlatforms.includes(rowPlatform) && rowPlatform !== '') {
        return false;
      }
      
      // Doar rândurile cu platforma exactă selectată
      const matches = rowPlatform === selectedPlatformTrimmed && rowPlatform !== '';
      
      return matches;
    });
    
    return filtered;
  }, [taggingRows, selectedPlatform]);

  // Sortare
  const sortedRows = [...filteredTaggingRows].sort((a, b) => {
    if (!sortConfig.key) return 0;
    
    const aValue = a[sortConfig.key] || '';
    const bValue = b[sortConfig.key] || '';
    
    if (aValue < bValue) return sortConfig.direction === 'asc' ? -1 : 1;
    if (aValue > bValue) return sortConfig.direction === 'asc' ? 1 : -1;
    return 0;
  });

  // Găsește index-ul real în taggingRows pentru un rând din sortedRows
  // Folosește contextul evenimentului părinte pentru a identifica corect parametrii
  const findRealIndex = (sortedIndex) => {
    if (sortedIndex >= sortedRows.length) return -1;
    const row = sortedRows[sortedIndex];
    
    // Dacă rândul are eventName, este un rând de eveniment - găsește-l direct
    if (row.eventName && row.eventName.trim() !== '') {
      return taggingRows.findIndex(r => 
        r.eventName === row.eventName && 
        r.eventCategory === row.eventCategory &&
        r.propertyName === row.propertyName &&
        r.propertyName === 'event' // Rândurile de eveniment au propertyName = 'event'
      );
    }
    
    // Pentru parametri, trebuie să găsim evenimentul părinte
    // Căutăm înapoi în sortedRows pentru a găsi evenimentul părinte
    let parentEventName = null;
    for (let i = sortedIndex - 1; i >= 0; i--) {
      const prevRow = sortedRows[i];
      if (prevRow.eventName && prevRow.eventName.trim() !== '' && 
          prevRow.eventCategory === row.eventCategory) {
        parentEventName = prevRow.eventName;
        break;
      }
    }
    
    // Dacă nu găsim evenimentul părinte, încercăm să găsim rândul direct
    if (!parentEventName) {
      return taggingRows.findIndex(r => 
        r.eventName === row.eventName && 
        r.eventCategory === row.eventCategory &&
        r.propertyName === row.propertyName
      );
    }
    
    // Găsește rândul în taggingRows folosind evenimentul părinte și parametrul
    // Trebuie să găsim primul rând care se potrivește după evenimentul părinte
    const parentEventIndex = taggingRows.findIndex(r => 
      r.eventName === parentEventName && 
      r.eventCategory === row.eventCategory &&
      r.propertyName === 'event'
    );
    
    if (parentEventIndex === -1) return -1;
    
    // Căutăm parametrul după evenimentul părinte
    for (let i = parentEventIndex + 1; i < taggingRows.length; i++) {
      const r = taggingRows[i];
      // Dacă găsim un alt eveniment (nu parametru), ne oprim
      if (r.eventName && r.eventName.trim() !== '' && r.eventName !== parentEventName) {
        break;
      }
      // Verifică dacă este rândul căutat
      if (r.propertyName === row.propertyName && 
          r.eventCategory === row.eventCategory &&
          (!r.eventName || r.eventName.trim() === '')) {
        // Verifică și alte câmpuri pentru a fi sigur
        if (r.propertyLabel === row.propertyLabel || 
            r.propertyDefinition === row.propertyDefinition) {
          return i;
        }
      }
    }
    
    // Fallback: caută direct
    return taggingRows.findIndex(r => 
      r.eventName === row.eventName && 
      r.eventCategory === row.eventCategory &&
      r.propertyName === row.propertyName
    );
  };

  // Funcție pentru sortare
  const handleSort = (columnKey) => {
    setSortConfig(prev => {
      if (prev.key === columnKey) {
        if (prev.direction === 'asc') {
          return { key: columnKey, direction: 'desc' };
        } else {
          return { key: null, direction: null };
        }
      } else {
        return { key: columnKey, direction: 'asc' };
      }
    });
  };

  // Senzori pentru drag & drop
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  // Funcție pentru drag & drop
  const handleDragEnd = (event) => {
    const { active, over } = event;
    
    if (!over || active.id === over.id) return;
    
    const oldIndex = sortedRows.findIndex((row, idx) => {
      const eventName = row.eventName || 'param';
      const propertyName = row.propertyName || `param-${idx}`;
      const platform = row.eventCategory || 'default';
      const rowKey = `${eventName}-${propertyName}-${platform}-${idx}`;
      return rowKey === active.id;
    });
    
    const newIndex = sortedRows.findIndex((row, idx) => {
      const eventName = row.eventName || 'param';
      const propertyName = row.propertyName || `param-${idx}`;
      const platform = row.eventCategory || 'default';
      const rowKey = `${eventName}-${propertyName}-${platform}-${idx}`;
      return rowKey === over.id;
    });
    
    if (oldIndex === -1 || newIndex === -1) return;
    
    // Găsește index-urile reale în taggingRows folosind findRealIndex
    const sourceRealIndex = findRealIndex(oldIndex);
    const destRealIndex = findRealIndex(newIndex);
    
    if (sourceRealIndex === -1 || destRealIndex === -1) {
      console.error('Could not find real indices for drag & drop');
      return;
    }
    
    // Reordonează local
    const newRows = Array.from(taggingRows);
    const [removed] = newRows.splice(sourceRealIndex, 1);
    newRows.splice(destRealIndex, 0, removed);
    
    // Actualizează state-ul
    setTaggingRows(newRows);
  };

  const columns = [
    { key: 'eventName', label: 'Event Name', width: '8%' },
    { key: 'eventCategory', label: 'Platform', width: '5%' },
    { key: 'eventDescription', label: 'Event Description', width: '12%' },
    { key: 'eventLocation', label: 'Event Location', width: '8%' },
    { key: 'propertyGroup', label: 'Property Group', width: '8%' },
    { key: 'propertyLabel', label: 'Property Label', width: '8%' },
    { key: 'propertyName', label: 'Property Name', width: '8%' },
    { key: 'propertyDefinition', label: 'Property Definition', width: '15%' },
    { key: 'dataType', label: 'Data Type', width: '8%' },
    { key: 'possibleValues', label: 'Possible Values', width: '10%' },
    { key: 'codeExamples', label: 'Code Examples', width: '15%' },
    { key: 'dataLayerStatus', label: 'DATA LAYER STATUS', width: '8%' },
    { key: 'statusGA4', label: 'STATUS GA4', width: '8%' },
  ];

  const handleCellUpdate = async (rowIndex, field, value) => {
    try {
      const realIndex = findRealIndex(rowIndex);
      if (realIndex === -1) {
        console.error('Could not find real index for row:', rowIndex);
        return;
      }
      await updateCell(realIndex, field, value);
      setEditingCell(null);
    } catch (error) {
      console.error('Error updating cell:', error);
      alert('Eroare la actualizarea celulei: ' + error.message);
    }
  };

  const handleDelete = async (rowIndex) => {
    if (window.confirm('Ești sigur că vrei să ștergi acest rând?')) {
      try {
        const realIndex = findRealIndex(rowIndex);
        if (realIndex === -1) {
          console.error('Could not find real index for row:', rowIndex);
          return;
        }
        await deleteRow(realIndex);
      } catch (error) {
        console.error('Error deleting row:', error);
        alert('Eroare la ștergerea rândului: ' + error.message);
      }
    }
  };

  const handleEventSelect = async (rowIndex, selectedEventCodes, selectedEventObjects) => {
    const realIndex = findRealIndex(rowIndex);
    if (realIndex === -1) {
      console.error('Could not find real index for row:', rowIndex);
      return;
    }

    // Actualizează local fără să reîmprospăteze toate datele
    setTaggingRows(prev => {
      const newRows = [...prev];
      if (newRows[realIndex]) {
        newRows[realIndex] = { 
          ...newRows[realIndex], 
          selectedEvents: selectedEventCodes 
        };
      }
      return newRows;
    });

    // Când se selectează evenimente, completează prima coloană "DATA LAYER STATUS" cu "yes"
    try {
      await updateCell(realIndex, 'dataLayerStatus', 'yes');
      // Opțional: poți actualiza și eventName dacă e gol și există cel puțin un eveniment selectat
      if (!taggingRows[realIndex]?.eventName && selectedEventObjects.length > 0) {
        const firstEventName = selectedEventObjects[0].name;
        await updateCell(realIndex, 'eventName', firstEventName);
      }
    } catch (error) {
      console.error('Error updating dataLayerStatus:', error);
    }
  };

  const isEventRow = (row) => {
    return row.eventName && row.eventName.trim() !== '';
  };

  // Verifică dacă este rândul unui eveniment (nu parametru)
  // Dropdown-ul apare doar pe rândurile unde eventName este completat
  const isEventRowForDropdown = (rowIndex) => {
    if (rowIndex >= sortedRows.length) return false;
    const row = sortedRows[rowIndex];
    return isEventRow(row);
  };

  if (loading && taggingRows.length === 0) {
    return (
      <div className="card">
        <div className="card-body text-center">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      </div>
    );
  }


  return (
    <div className="card">
      <div className="card-header d-flex justify-content-between align-items-center">
        <div className="d-flex align-items-center gap-3">
          <h5 className="mb-0">Tagging Plan Sheet</h5>
          <div className="d-flex align-items-center gap-2">
            <label className="form-label mb-0" style={{ fontSize: '14px' }}>
              Platformă:
            </label>
            <select
              className="form-select form-select-sm"
              value={selectedPlatform}
              onChange={(e) => setSelectedPlatform(e.target.value)}
              style={{ width: 'auto', minWidth: '120px' }}
            >
              <option value="WEB">WEB</option>
              <option value="Android">Android</option>
              <option value="iOS">iOS</option>
            </select>
          </div>
        </div>
        <div className="d-flex align-items-center gap-3">
          <div className="form-check">
            <input
              className="form-check-input"
              type="checkbox"
              id="showEventColumn"
              checked={showEventColumn}
              onChange={(e) => setShowEventColumn(e.target.checked)}
            />
            <label className="form-check-label" htmlFor="showEventColumn">
              Afișează coloană Event Selection
            </label>
          </div>
          <span className="badge bg-info">
            Total rânduri: {filteredTaggingRows.length}
          </span>
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={() => setShowAddCustomRowModal(true)}
          >
            ➕ Add Custom Row
          </button>
          <ExportButtons />
        </div>
      </div>
      <div className="card-body p-0">
        <div className="table-responsive" style={{ maxHeight: '70vh', overflowY: 'auto' }}>
          <table className="table table-bordered table-hover mb-0" id="taggingTable">
            <thead className="table-dark sticky-top">
              <tr>
                <th style={{ width: '30px', cursor: 'default' }}>🟰</th>
                {columns.map((col) => (
                  <th 
                    key={col.key} 
                    style={{ width: col.width, cursor: 'pointer' }}
                    onClick={() => handleSort(col.key)}
                    className="user-select-none"
                  >
                    <div className="d-flex align-items-center justify-content-between">
                      <span>{col.label}</span>
                      {sortConfig.key === col.key && (
                        <span className="ms-2" style={{ fontSize: '0.8em' }}>
                          {sortConfig.direction === 'asc' ? '↑' : '↓'}
                        </span>
                      )}
                    </div>
                  </th>
                ))}
                {showEventColumn && (
                  <th style={{ width: '10%' }}>Datalayer Status</th>
                )}
                <th style={{ width: '5%' }}>Actions</th>
              </tr>
            </thead>
            <DndContext
              sensors={sensors}
              collisionDetection={closestCenter}
              onDragEnd={handleDragEnd}
            >
              <SortableContext
                items={sortedRows.map((row, idx) => {
                  // Creează un ID unic care include toate informațiile relevante
                  const eventName = row.eventName || 'param';
                  const propertyName = row.propertyName || `param-${idx}`;
                  const platform = row.eventCategory || 'default';
                  const uniqueId = `${eventName}-${propertyName}-${platform}-${idx}`;
                  return uniqueId;
                })}
                strategy={verticalListSortingStrategy}
              >
                <tbody>
                  {sortedRows.length === 0 ? (
                    <tr>
                      <td colSpan={columns.length + (showEventColumn ? 1 : 0) + 2} className="text-center" style={{ padding: '40px' }}>
                        <p className="text-muted">Nu există date. Generează un tagging plan pentru a începe.</p>
                      </td>
                    </tr>
                  ) : (
                    sortedRows.map((row, rowIndex) => {
                      // Creează un key unic care include index-ul pentru a evita duplicate
                      const eventName = row.eventName || 'param';
                      const propertyName = row.propertyName || `param-${rowIndex}`;
                      const platform = row.eventCategory || 'default';
                      const uniqueKey = `${eventName}-${propertyName}-${platform}-${rowIndex}`;
                      
                      return (
                        <SortableRow
                          key={uniqueKey}
                          row={row}
                          rowIndex={rowIndex}
                        columns={columns}
                        showEventColumn={showEventColumn}
                        isEventRow={isEventRow}
                        isEventRowForDropdown={isEventRowForDropdown}
                        events={events}
                        allEvents={allEvents}
                        editingCell={editingCell}
                        setEditingCell={setEditingCell}
                        handleCellUpdate={handleCellUpdate}
                        handleEventSelect={handleEventSelect}
                        handleDelete={handleDelete}
                      />
                      );
                    })
                  )}
                </tbody>
              </SortableContext>
            </DndContext>
          </table>
        </div>
      </div>
      <AddCustomRowModal
        show={showAddCustomRowModal}
        onClose={() => setShowAddCustomRowModal(false)}
      />
    </div>
  );
};

export default TaggingTable;

