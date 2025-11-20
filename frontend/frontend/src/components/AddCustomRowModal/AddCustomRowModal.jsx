import React, { useState } from 'react';
import { useTagging } from '../../context/TaggingContext';

const AddCustomRowModal = ({ show, onClose }) => {
  const { addRow, loading } = useTagging();
  const [formData, setFormData] = useState({
    eventName: '',
    eventCategory: '',
    eventDescription: '',
    eventLocation: '',
    propertyGroup: '',
    propertyLabel: '',
    propertyName: '',
    propertyDefinition: '',
    dataType: '',
    possibleValues: '',
    codeExamples: '',
    dataLayerStatus: 'yes',
    statusGA4: 'yes'
  });

  const handleChange = (field, value) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Verifică dacă există cel puțin un câmp completat
    const hasContent = formData.eventName.trim() !== '' ||
                      formData.eventCategory.trim() !== '' ||
                      formData.eventDescription.trim() !== '' ||
                      formData.propertyName.trim() !== '';

    if (!hasContent) {
      alert('Te rugăm să completezi cel puțin un câmp!');
      return;
    }

    try {
      await addRow(formData);
      alert('Rând custom adăugat cu succes!');
      // Resetează formularul
      setFormData({
        eventName: '',
        eventCategory: '',
        eventDescription: '',
        eventLocation: '',
        propertyGroup: '',
        propertyLabel: '',
        propertyName: '',
        propertyDefinition: '',
        dataType: '',
        possibleValues: '',
        codeExamples: '',
        dataLayerStatus: 'yes',
        statusGA4: 'yes'
      });
      onClose();
    } catch (error) {
      alert('Eroare la adăugarea rândului: ' + error.message);
    }
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
            <h5 className="modal-title">Adaugă Rând Custom</h5>
            <button
              type="button"
              className="btn-close"
              onClick={onClose}
            ></button>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label">Event Name</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.eventName}
                    onChange={(e) => handleChange('eventName', e.target.value)}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Event Category</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.eventCategory}
                    onChange={(e) => handleChange('eventCategory', e.target.value)}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Event Description</label>
                  <textarea
                    className="form-control"
                    rows="2"
                    value={formData.eventDescription}
                    onChange={(e) => handleChange('eventDescription', e.target.value)}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Event Location</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.eventLocation}
                    onChange={(e) => handleChange('eventLocation', e.target.value)}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Property Group</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.propertyGroup}
                    onChange={(e) => handleChange('propertyGroup', e.target.value)}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Property Label</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.propertyLabel}
                    onChange={(e) => handleChange('propertyLabel', e.target.value)}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Property Name</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.propertyName}
                    onChange={(e) => handleChange('propertyName', e.target.value)}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Property Definition</label>
                  <textarea
                    className="form-control"
                    rows="2"
                    value={formData.propertyDefinition}
                    onChange={(e) => handleChange('propertyDefinition', e.target.value)}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Data Type</label>
                  <select
                    className="form-select"
                    value={formData.dataType}
                    onChange={(e) => handleChange('dataType', e.target.value)}
                  >
                    <option value="">Selectează...</option>
                    <option value="String">String</option>
                    <option value="Numeric">Numeric</option>
                    <option value="Boolean">Boolean</option>
                    <option value="List of Objects">List of Objects</option>
                  </select>
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Possible Values</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.possibleValues}
                    onChange={(e) => handleChange('possibleValues', e.target.value)}
                  />
                </div>
                <div className="col-12 mb-3">
                  <label className="form-label">Code Examples</label>
                  <textarea
                    className="form-control"
                    rows="4"
                    value={formData.codeExamples}
                    onChange={(e) => handleChange('codeExamples', e.target.value)}
                    style={{ fontFamily: 'monospace', fontSize: '12px' }}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Data Layer Status</label>
                  <select
                    className="form-select"
                    value={formData.dataLayerStatus}
                    onChange={(e) => handleChange('dataLayerStatus', e.target.value)}
                  >
                    <option value="yes">Yes</option>
                    <option value="no">No</option>
                  </select>
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Status GA4</label>
                  <select
                    className="form-select"
                    value={formData.statusGA4}
                    onChange={(e) => handleChange('statusGA4', e.target.value)}
                  >
                    <option value="yes">Yes</option>
                    <option value="no">No</option>
                  </select>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={onClose}
                disabled={loading}
              >
                Anulează
              </button>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={loading}
              >
                {loading ? 'Se adaugă...' : 'Adaugă Rând'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AddCustomRowModal;

