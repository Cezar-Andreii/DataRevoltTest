import React, { createContext, useContext, useState, useCallback } from 'react';
import * as api from '../services/api';

const TaggingContext = createContext();

export const useTagging = () => {
  const context = useContext(TaggingContext);
  if (!context) {
    throw new Error('useTagging must be used within a TaggingProvider');
  }
  return context;
};

export const TaggingProvider = ({ children }) => {
  const [taggingRows, setTaggingRows] = useState([]);
  const [events, setEvents] = useState([]);
  const [parameters, setParameters] = useState([]);
  const [selectedEvents, setSelectedEvents] = useState([]);
  const [selectedPlatform, setSelectedPlatform] = useState('WEB'); // Platforma selectată pentru filtrare
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Încarcă datele inițiale
  const loadInitialData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getInitialData();
      setEvents(data.events || []);
      setParameters(data.parameters || []);
      setTaggingRows(data.taggingRows || []);
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  // Generează tagging plan
  const generateTaggingPlan = useCallback(async (selectedEvents, selectedItems, selectedPlatforms) => {
    setLoading(true);
    setError(null);
    try {
      const requestData = {
        selectedEvents,
        selectedItems: selectedItems || [],
        selectedPlatforms: selectedPlatforms || []
      };
      console.log('=== FRONTEND: Generating Tagging Plan ===');
      console.log('Request data:', requestData);
      console.log('Selected Platforms:', selectedPlatforms);
      const response = await api.generateTaggingPlan(requestData);
      if (response.success) {
        setTaggingRows(response.taggingRows || []);
        // Reîncarcă datele pentru a obține versiunea actualizată
        await loadInitialData();
      }
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [loadInitialData]);

  // Adaugă un rând
  const addRow = useCallback(async (rowData) => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.addCustomRow(rowData);
      if (response.success) {
        await loadInitialData(); // Reîncarcă datele
      }
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [loadInitialData]);

  // Actualizează un rând
  const updateRow = useCallback(async (rowIndex, rowData) => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.updateTaggingRow({
        rowIndex,
        ...rowData
      });
      if (response.success) {
        await loadInitialData(); // Reîncarcă datele
      }
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [loadInitialData]);

  // Actualizează o celulă (fără loading pentru a nu întrerupe editarea)
  const updateCell = useCallback(async (rowIndex, field, value) => {
    setError(null);
    try {
      const response = await api.updateCell({
        rowIndex,
        field,
        value
      });
      if (response.success) {
        // Actualizează local fără să reîmprospăteze toate datele
        setTaggingRows(prev => {
          const newRows = [...prev];
          if (newRows[rowIndex]) {
            newRows[rowIndex] = { ...newRows[rowIndex], [field]: value };
          }
          return newRows;
        });
      }
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    }
  }, []);

  // Șterge un rând
  const deleteRow = useCallback(async (rowIndex) => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.deleteTaggingRow(rowIndex);
      if (response.success) {
        await loadInitialData(); // Reîncarcă datele
      }
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [loadInitialData]);

  // Resetează tabelul
  const resetRows = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.resetTable();
      if (response.success) {
        setTaggingRows([]);
      }
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  // Export funcții
  const exportGoogleSheet = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.exportGoogleSheet();
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const exportCSV = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.exportCSV();
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const exportJSON = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.exportJSON();
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const exportGTMJSON = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.exportGTMJSON();
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  // Filtrează evenimentele în funcție de platforma selectată
  // Momentan returnează toate evenimentele (va fi adaptat mai târziu)
  const filteredEvents = events; // TODO: filtrare pe baza platformei

  const value = {
    // State
    taggingRows,
    events: filteredEvents,
    allEvents: events, // Toate evenimentele (nefiltrate)
    parameters,
    selectedEvents,
    selectedPlatform,
    loading,
    error,
    
    // Setters
    setTaggingRows,
    setSelectedEvents,
    setSelectedPlatform,
    
    // Actions
    loadInitialData,
    generateTaggingPlan,
    addRow,
    updateRow,
    updateCell,
    deleteRow,
    resetRows,
    exportGoogleSheet,
    exportCSV,
    exportJSON,
    exportGTMJSON,
  };

  return (
    <TaggingContext.Provider value={value}>
      {children}
    </TaggingContext.Provider>
  );
};

export default TaggingContext;

