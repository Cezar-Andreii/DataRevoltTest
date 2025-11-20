import React, { useState } from 'react';
import { useTagging } from '../../context/TaggingContext';

const CustomEventModal = ({ show, onClose }) => {
  const { addRow, loading } = useTagging();
  const [formData, setFormData] = useState({
    eventName: '',
    eventCategory: '',
    eventDescription: '',
    eventLocation: '',
    propertyGroup: 'Analytics',
    propertyLabel: 'Dimension',
    propertyName: 'event',
    propertyDefinition: 'The event name.',
    dataType: 'String',
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
    
    // Verifică că cel puțin Event Name și Property Name sunt completate
    if (!formData.eventName.trim() || !formData.propertyName.trim()) {
      alert('Te rugăm să completezi cel puțin Event Name și Property Name!');
      return;
    }

    try {
      await addRow(formData);
      alert('Event custom adăugat cu succes!');
      // Resetează formularul
      setFormData({
        eventName: '',
        eventCategory: '',
        eventDescription: '',
        eventLocation: '',
        propertyGroup: 'Analytics',
        propertyLabel: 'Dimension',
        propertyName: 'event',
        propertyDefinition: 'The event name.',
        dataType: 'String',
        possibleValues: '',
        codeExamples: '',
        dataLayerStatus: 'yes',
        statusGA4: 'yes'
      });
      onClose();
    } catch (error) {
      alert('Eroare la adăugarea eventului: ' + error.message);
    }
  };

  if (!show) return null;

  return (
    <div
      className="modal show"
      style={{ display: 'block', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={onClose}
    >
      <div className="modal-dialog modal-xl" onClick={(e) => e.stopPropagation()}>
        <div className="modal-content">
          <div className="modal-header bg-primary text-white">
            <h5 className="modal-title">
              ➕ Creează Event Custom
            </h5>
            <button
              type="button"
              className="btn-close btn-close-white"
              onClick={onClose}
            ></button>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              <div className="alert alert-info">
                <strong>Completează toate câmpurile pentru a crea un event custom în tabel.</strong>
                <p className="mb-0">Eventul va fi adăugat în tabel după confirmare.</p>
              </div>

              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label fw-bold">
                    Event Name <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.eventName}
                    onChange={(e) => handleChange('eventName', e.target.value)}
                    placeholder="ex: purchase, view_item, add_to_cart"
                    required
                  />
                  <small className="text-muted">Any meaningful action a user takes within your product</small>
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label fw-bold">Event Category</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.eventCategory}
                    onChange={(e) => handleChange('eventCategory', e.target.value)}
                    placeholder="ex: Ecommerce, Engagement, etc."
                  />
                  <small className="text-muted">General category of the event</small>
                </div>
              </div>

              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label fw-bold">Event Description</label>
                  <textarea
                    className="form-control"
                    rows="2"
                    value={formData.eventDescription}
                    onChange={(e) => handleChange('eventDescription', e.target.value)}
                    placeholder="Description of the event being performed"
                  />
                  <small className="text-muted">Description of the event (for any given end user to understand)</small>
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label fw-bold">Event Location</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.eventLocation}
                    onChange={(e) => handleChange('eventLocation', e.target.value)}
                    placeholder="ex: /checkout, /product, etc."
                  />
                  <small className="text-muted">The pages where the event can trigger</small>
                </div>
              </div>

              <hr className="my-3" />

              <div className="row">
                <div className="col-md-4 mb-3">
                  <label className="form-label fw-bold">Property Group</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.propertyGroup}
                    onChange={(e) => handleChange('propertyGroup', e.target.value)}
                    placeholder="ex: Analytics, Custom"
                  />
                  <small className="text-muted">Analytics or Custom Property</small>
                </div>
                <div className="col-md-4 mb-3">
                  <label className="form-label fw-bold">Property Label</label>
                  <select
                    className="form-select"
                    value={formData.propertyLabel}
                    onChange={(e) => handleChange('propertyLabel', e.target.value)}
                  >
                    <option value="Metric">Metric</option>
                    <option value="Dimension">Dimension</option>
                  </select>
                  <small className="text-muted">Metric or Dimension</small>
                </div>
                <div className="col-md-4 mb-3">
                  <label className="form-label fw-bold">
                    Property Name <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.propertyName}
                    onChange={(e) => handleChange('propertyName', e.target.value)}
                    placeholder="ex: event, value, currency"
                    required
                  />
                  <small className="text-muted">Any rich metadata associated to the event</small>
                </div>
              </div>

              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label fw-bold">Property Definition</label>
                  <textarea
                    className="form-control"
                    rows="2"
                    value={formData.propertyDefinition}
                    onChange={(e) => handleChange('propertyDefinition', e.target.value)}
                    placeholder="Description of the property associated with the event"
                  />
                  <small className="text-muted">Description of the property (for any given end user to understand)</small>
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label fw-bold">Data Type</label>
                  <select
                    className="form-select"
                    value={formData.dataType}
                    onChange={(e) => handleChange('dataType', e.target.value)}
                  >
                    <option value="String">String</option>
                    <option value="Numeric">Numeric</option>
                    <option value="Boolean">Boolean</option>
                    <option value="List of Objects">List of Objects</option>
                  </select>
                  <small className="text-muted">One of six possible data types</small>
                </div>
              </div>

              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label fw-bold">Possible Values</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.possibleValues}
                    onChange={(e) => handleChange('possibleValues', e.target.value)}
                    placeholder="Sample value of the property"
                  />
                  <small className="text-muted">Sample value of the property or all possible values</small>
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label fw-bold">Code Examples</label>
                  <textarea
                    className="form-control font-monospace"
                    rows="3"
                    value={formData.codeExamples}
                    onChange={(e) => handleChange('codeExamples', e.target.value)}
                    placeholder="JavaScript code example"
                    style={{ fontFamily: 'monospace', fontSize: '12px' }}
                  />
                  <small className="text-muted">Examples of how the codes should look like</small>
                </div>
              </div>

              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label fw-bold">DATA LAYER STATUS</label>
                  <select
                    className="form-select"
                    value={formData.dataLayerStatus}
                    onChange={(e) => handleChange('dataLayerStatus', e.target.value)}
                  >
                    <option value="yes">yes</option>
                    <option value="no">no</option>
                    <option value="pending">pending</option>
                  </select>
                  <small className="text-muted">Whether the event/property has been implemented or not</small>
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label fw-bold">STATUS GA4</label>
                  <select
                    className="form-select"
                    value={formData.statusGA4}
                    onChange={(e) => handleChange('statusGA4', e.target.value)}
                  >
                    <option value="yes">yes</option>
                    <option value="no">no</option>
                    <option value="pending">pending</option>
                  </select>
                  <small className="text-muted">Whether the event/property has been implemented or not</small>
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
                className="btn btn-success"
                disabled={loading}
              >
                {loading ? 'Se adaugă...' : 'Adaugă Event în Tabel'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CustomEventModal;

