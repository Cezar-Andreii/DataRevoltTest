import React, { useState, useEffect } from 'react';

const UserDataModal = ({ show, onClose, onConfirm, selectedParams: initialSelectedParams = [] }) => {
  const [selectedParams, setSelectedParams] = useState(initialSelectedParams);
  const [customParam, setCustomParam] = useState('');

  useEffect(() => {
    if (show) {
      setSelectedParams(initialSelectedParams);
    }
  }, [show, initialSelectedParams]);

  const availableParams = [
    'userID',
    'email',
    'md5',
    'sha256',
    'firstPurchase',
    'lastPurchase',
    'accountAge',
    'Orders',
    'OrdersValue',
    'OrdersCanceled',
    'catOrdered',
    'catWishlisted',
    'lastPaymentType',
    'lastShippingMethod',
    'newsletter_subscriber',
    'lastPurchasedProducts',
    'lastProductsCompare',
    'savedCard',
    'savedAddress',
    'client_type'
  ];

  const handleParamToggle = (param) => {
    setSelectedParams(prev => {
      if (prev.includes(param)) {
        return prev.filter(p => p !== param);
      } else {
        return [...prev, param];
      }
    });
  };

  const handleAddCustomParam = () => {
    if (customParam.trim() && !selectedParams.includes(customParam.trim())) {
      setSelectedParams(prev => [...prev, customParam.trim()]);
      setCustomParam('');
    }
  };

  const handleConfirm = () => {
    console.log('=== UserDataModal: handleConfirm ===');
    console.log('Selected Params:', selectedParams);
    onConfirm(selectedParams);
    onClose();
  };

  if (!show) return null;

  return (
    <div
      className="modal show"
      style={{ display: 'block', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={onClose}
    >
      <div className="modal-dialog modal-lg" onClick={(e) => e.stopPropagation()}>
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Selectează Parametrii User Data</h5>
            <button
              type="button"
              className="btn-close"
              onClick={onClose}
            ></button>
          </div>
          <div className="modal-body">
            <p className="text-muted">
              Selectează parametrii care vor fi incluși în obiectul userData pentru evenimentul userData.
            </p>
            
            <div
              className="parameter-checkboxes"
              style={{
                border: '1px solid #e0e0e0',
                borderRadius: '8px',
                padding: '15px',
                maxHeight: '400px',
                overflowY: 'auto',
                display: 'grid',
                gridTemplateColumns: 'repeat(2, 1fr)',
                gap: '10px'
              }}
            >
              {availableParams.map((param) => (
                <div key={param} className="form-check">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id={`param_${param}`}
                    checked={selectedParams.includes(param)}
                    onChange={() => handleParamToggle(param)}
                  />
                  <label className="form-check-label" htmlFor={`param_${param}`}>
                    {param}
                  </label>
                </div>
              ))}
            </div>
            <div className="mt-3">
              <label className="form-label">Adaugă Parametru Custom:</label>
              <div className="input-group">
                <input
                  type="text"
                  className="form-control"
                  value={customParam}
                  onChange={(e) => setCustomParam(e.target.value)}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter') {
                      handleAddCustomParam();
                    }
                  }}
                  placeholder="Nume parametru custom"
                />
                <button
                  className="btn btn-outline-secondary"
                  type="button"
                  onClick={handleAddCustomParam}
                >
                  Adaugă
                </button>
              </div>
            </div>
            {selectedParams.length > 0 && (
              <div className="mt-3">
                <strong>Parametri selectați ({selectedParams.length}):</strong>
                <div className="mt-2">
                  {selectedParams.map((param, index) => (
                    <span
                      key={index}
                      className="badge bg-primary me-1 mb-1"
                      style={{ cursor: 'pointer' }}
                      onClick={() => handleParamToggle(param)}
                    >
                      {param} ×
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onClose}
            >
              Anulează
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleConfirm}
            >
              Confirmă și Generează
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserDataModal;

