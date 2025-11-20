/**
 * Funcții helper pentru logica de tagging
 */

/**
 * Găsește numele evenimentului părinte pentru un parametru
 * @param {number} paramRowIndex - Index-ul rândului de parametru
 * @param {Array} rows - Lista de rânduri
 * @returns {string|null} Numele evenimentului părinte sau null
 */
export const findParentEventName = (paramRowIndex, rows) => {
  // Căutăm înapoi până găsim un eveniment (rând cu eventName completat)
  for (let i = paramRowIndex - 1; i >= 0; i--) {
    const row = rows[i];
    if (row.eventName && row.eventName.trim() !== '') {
      return row.eventName;
    }
  }
  return null;
};

/**
 * Verifică dacă un parametru aparține unui eveniment specific
 * @param {Object} paramRow - Rândul de parametru
 * @param {string} eventName - Numele evenimentului
 * @param {Array} rows - Lista de rânduri
 * @returns {boolean} True dacă parametrul aparține evenimentului
 */
export const belongsToEvent = (paramRow, eventName, rows) => {
  const paramIndex = rows.indexOf(paramRow);
  if (paramIndex === -1) return false;

  // Căutăm înapoi până găsim un eveniment
  for (let i = paramIndex - 1; i >= 0; i--) {
    const row = rows[i];
    if (row.eventName && row.eventName.trim() !== '') {
      return row.eventName === eventName;
    }
  }

  return false;
};

/**
 * Obține parametrii oficiali pentru un eveniment specific
 * @param {string} eventName - Numele evenimentului
 * @returns {Array<string>} Lista de parametri oficiali
 */
export const getOfficialParametersForEvent = (eventName) => {
  const parameters = [];

  switch (eventName) {
    case 'view_item_list':
    case 'view_item':
    case 'select_item':
    case 'add_to_cart':
    case 'view_cart':
    case 'add_to_wishlist':
    case 'view_promotion':
    case 'select_promotion':
      parameters.push('event', 'currency', 'value', 'items');
      break;
    case 'begin_checkout':
    case 'add_shipping_info':
    case 'add_payment_info':
      parameters.push('event', 'currency', 'value', 'items', 'coupon');
      break;
    case 'purchase':
      parameters.push('event', 'currency', 'value', 'customer_type', 'transaction_id', 'coupon', 'shipping', 'tax', 'items');
      break;
    case 'search':
      parameters.push('event', 'search_term');
      break;
    case 'login':
      parameters.push('event', 'method');
      break;
    default:
      // Parametri de bază pentru evenimente necunoscute
      parameters.push('event', 'currency', 'value');
      break;
  }

  return parameters;
};

/**
 * Obține label-ul proprietății pentru un parametru
 * @param {string} paramName - Numele parametrului
 * @returns {string} Label-ul proprietății (Metric sau Dimension)
 */
export const getPropertyLabel = (paramName) => {
  switch (paramName) {
    case 'value':
    case 'shipping':
    case 'tax':
      return 'Metric';
    case 'event':
    case 'coupon':
    case 'transaction_id':
    case 'currency':
    case 'customer_type':
    case 'affiliation':
    case 'items':
    case 'search_term':
    case 'method':
      return 'Dimension';
    default:
      return 'Dimension';
  }
};

/**
 * Obține tipul de date pentru un parametru
 * @param {string} paramName - Numele parametrului
 * @returns {string} Tipul de date (String, Numeric, List of Objects)
 */
export const getDataType = (paramName) => {
  switch (paramName) {
    case 'value':
    case 'shipping':
    case 'tax':
      return 'Numeric';
    case 'items':
      return 'List of Objects';
    case 'event':
    case 'coupon':
    case 'transaction_id':
    case 'currency':
    case 'affiliation':
    case 'search_term':
    case 'method':
      return 'String';
    default:
      return 'String';
  }
};

/**
 * Verifică dacă un rând este un rând de eveniment
 * @param {Object} row - Rândul de verificat
 * @returns {boolean} True dacă este rând de eveniment
 */
export const isEventRow = (row) => {
  return row.eventName && row.eventName.trim() !== '';
};

export default {
  findParentEventName,
  belongsToEvent,
  getOfficialParametersForEvent,
  getPropertyLabel,
  getDataType,
  isEventRow
};

