import axios from 'axios';

// Creează instanța axios cu configurație de bază
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor pentru request-uri (opțional - pentru logging)
api.interceptors.request.use(
  (config) => {
    console.log(`Making ${config.method.toUpperCase()} request to ${config.url}`);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor pentru response-uri (opțional - pentru error handling global)
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

// Funcții pentru API calls

/**
 * Obține datele inițiale (evenimente, parametri, taggingRows)
 */
export const getInitialData = async () => {
  const response = await api.get('/tagging/api/init');
  return response.data;
};

/**
 * Generează tagging plan
 * @param {Object} data - { selectedEvents: [], selectedItems: [], selectedPlatforms: [] }
 */
export const generateTaggingPlan = async (data) => {
  console.log('API: Sending request to /tagging/api/generate');
  console.log('API: Request data:', JSON.stringify(data, null, 2));
  try {
    const response = await api.post('/tagging/api/generate', data);
    console.log('API: Response received:', response.data);
    return response.data;
  } catch (error) {
    console.error('API: Error in generateTaggingPlan:', error);
    console.error('API: Error response:', error.response?.data);
    throw error;
  }
};

/**
 * Generează cod JavaScript
 * @param {Object} data - { eventName: string, parameters: [], items: [] }
 */
export const generateCode = async (data) => {
  const response = await api.post('/tagging/api/generate-code', data);
  return response.data;
};

/**
 * Actualizează un rând complet
 * @param {Object} data - UpdateRowRequest
 */
export const updateTaggingRow = async (data) => {
  const response = await api.put('/tagging/api/update', data);
  return response.data;
};

/**
 * Șterge un rând
 * @param {number} rowIndex - Index-ul rândului de șters
 */
export const deleteTaggingRow = async (rowIndex) => {
  const response = await api.delete(`/tagging/api/delete/${rowIndex}`);
  return response.data;
};

/**
 * Actualizează o celulă
 * @param {Object} data - { rowIndex: number, field: string, value: string }
 */
export const updateCell = async (data) => {
  const response = await api.patch('/tagging/api/update-cell', data);
  return response.data;
};

/**
 * Adaugă un rând custom
 * @param {Object} data - AddCustomRowRequest
 */
export const addCustomRow = async (data) => {
  const response = await api.post('/tagging/api/add-custom-row', data);
  return response.data;
};

/**
 * Resetează tabelul
 */
export const resetTable = async () => {
  const response = await api.post('/tagging/api/reset');
  return response.data;
};

/**
 * Exportă la Google Sheets
 */
export const exportGoogleSheet = async () => {
  const response = await api.get('/tagging/api/export-google-sheet');
  return response.data;
};

/**
 * Exportă CSV (download direct)
 */
export const exportCSV = async () => {
  const response = await api.get('/tagging/export-csv', {
    responseType: 'blob',
  });
  
  // Creează link de download
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', 'tagging-plan.csv');
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
  
  return { success: true, message: 'CSV downloaded successfully' };
};

/**
 * Exportă JSON
 */
export const exportJSON = async () => {
  const response = await api.get('/tagging/api/export-json');
  return response.data;
};

/**
 * Exportă GTM JSON (Google Tag Manager format)
 */
export const exportGTMJSON = async () => {
  const response = await api.get('/tagging/api/export-gtm-json');
  return response.data;
};

export default api;

