import React, { useState, useEffect } from 'react';

const ItemsArrayModal = ({ show, onClose, onConfirm, selectedItems: initialSelectedItems = [], selectedPlatforms: initialSelectedPlatforms = [] }) => {
  const [selectedItems, setSelectedItems] = useState(initialSelectedItems);
  const [selectedPlatforms, setSelectedPlatforms] = useState(initialSelectedPlatforms);
  const [customItem, setCustomItem] = useState('');

  useEffect(() => {
    if (show) {
      setSelectedItems(initialSelectedItems);
      setSelectedPlatforms(initialSelectedPlatforms);
    }
  }, [show, initialSelectedItems, initialSelectedPlatforms]);

  const availableItems = [
    'item_id',
    'item_name',
    'item_category',
    'item_category2',
    'item_category3',
    'item_brand',
    'item_variant',
    'item_list_id',
    'item_list_name',
    'price',
    'quantity',
    'discount',
    'affiliation',
    'coupon',
    'index'
  ];

  const handleItemToggle = (item) => {
    setSelectedItems(prev => {
      if (prev.includes(item)) {
        return prev.filter(i => i !== item);
      } else {
        return [...prev, item];
      }
    });
  };

  const handleAddCustomItem = () => {
    if (customItem.trim() && !selectedItems.includes(customItem.trim())) {
      setSelectedItems(prev => [...prev, customItem.trim()]);
      setCustomItem('');
    }
  };

  const handlePlatformToggle = (platform) => {
    setSelectedPlatforms(prev => {
      if (prev.includes(platform)) {
        return prev.filter(p => p !== platform);
      } else {
        return [...prev, platform];
      }
    });
  };

  const handleConfirm = () => {
    console.log('=== ItemsArrayModal: handleConfirm ===');
    console.log('Selected Items:', selectedItems);
    console.log('Selected Platforms:', selectedPlatforms);
    console.log('Selected Platforms type:', typeof selectedPlatforms);
    console.log('Selected Platforms is array:', Array.isArray(selectedPlatforms));
    if (selectedPlatforms && selectedPlatforms.length > 0) {
      selectedPlatforms.forEach((p, i) => {
        console.log(`Platform[${i}]: '${p}' (type: ${typeof p})`);
      });
    }
    onConfirm(selectedItems, selectedPlatforms);
    onClose();
  };

  const platforms = ['WEB', 'Android', 'iOS'];

  if (!show) return null;

  return (
    <div
      className="modal show"
      style={{ display: 'block', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={onClose}
    >
      <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Selectează Item-uri pentru Array</h5>
            <button
              type="button"
              className="btn-close"
              onClick={onClose}
            ></button>
          </div>
          <div className="modal-body">
            <p className="text-muted">
              Selectează item-urile care vor fi incluse în array-ul de items pentru evenimentele selectate.
            </p>
            
            {/* Platform Selection */}
            <div className="mb-4">
              <label className="form-label fw-bold">Selectează Platforma:</label>
              <div className="d-flex gap-3">
                {platforms.map((platform) => (
                  <div key={platform} className="form-check">
                    <input
                      className="form-check-input"
                      type="checkbox"
                      id={`platform_${platform}`}
                      checked={selectedPlatforms.includes(platform)}
                      onChange={() => handlePlatformToggle(platform)}
                    />
                    <label className="form-check-label" htmlFor={`platform_${platform}`}>
                      {platform}
                    </label>
                  </div>
                ))}
              </div>
              {selectedPlatforms.length > 0 && (
                <div className="mt-2">
                  <small className="text-muted">
                    Platforme selectate: {selectedPlatforms.join(', ')}
                  </small>
                </div>
              )}
            </div>

            <div
              className="parameter-checkboxes"
              style={{
                border: '1px solid #e0e0e0',
                borderRadius: '8px',
                padding: '15px',
                maxHeight: '300px',
                overflowY: 'auto'
              }}
            >
              {availableItems.map((item) => (
                <div key={item} className="form-check">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id={`item_${item}`}
                    checked={selectedItems.includes(item)}
                    onChange={() => handleItemToggle(item)}
                  />
                  <label className="form-check-label" htmlFor={`item_${item}`}>
                    {item}
                  </label>
                </div>
              ))}
            </div>
            <div className="mt-3">
              <label className="form-label">Adaugă Item Custom:</label>
              <div className="input-group">
                <input
                  type="text"
                  className="form-control"
                  value={customItem}
                  onChange={(e) => setCustomItem(e.target.value)}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter') {
                      handleAddCustomItem();
                    }
                  }}
                  placeholder="Nume item custom"
                />
                <button
                  className="btn btn-outline-secondary"
                  type="button"
                  onClick={handleAddCustomItem}
                >
                  Adaugă
                </button>
              </div>
            </div>
            {selectedItems.length > 0 && (
              <div className="mt-3">
                <strong>Item-uri selectate ({selectedItems.length}):</strong>
                <div className="mt-2">
                  {selectedItems.map((item, index) => (
                    <span
                      key={index}
                      className="badge bg-primary me-1 mb-1"
                      style={{ cursor: 'pointer' }}
                      onClick={() => handleItemToggle(item)}
                    >
                      {item} ×
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

export default ItemsArrayModal;

