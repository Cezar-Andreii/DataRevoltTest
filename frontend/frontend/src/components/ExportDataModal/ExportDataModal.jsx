import React, { useState } from 'react';

const ExportDataModal = ({ show, onClose, data, format = 'csv' }) => {
  const [copied, setCopied] = useState(false);

  const formatData = () => {
    if (format === 'csv') {
      // Generează CSV
      const headers = [
        'Event Name', 'Event Category', 'Event Description', 'Event Location',
        'Property Group', 'Property Label', 'Property Name', 'Property Definition',
        'Data Type', 'Possible Values', 'Code Examples', 'DATA LAYER STATUS', 'STATUS GA4'
      ];
      
      const rows = data.map(row => [
        row.eventName || '',
        row.eventCategory || '',
        row.eventDescription || '',
        row.eventLocation || '',
        row.propertyGroup || '',
        row.propertyLabel || '',
        row.propertyName || '',
        row.propertyDefinition || '',
        row.dataType || '',
        row.possibleValues || '',
        row.codeExamples || '',
        row.dataLayerStatus || '',
        row.statusGA4 || ''
      ].map(cell => {
        // Escape CSV
        const cellStr = String(cell || '');
        if (cellStr.includes(',') || cellStr.includes('"') || cellStr.includes('\n')) {
          return `"${cellStr.replace(/"/g, '""')}"`;
        }
        return cellStr;
      }));
      
      return [headers.join(','), ...rows.map(row => row.join(','))].join('\n');
    } else {
      return JSON.stringify(data, null, 2);
    }
  };

  const formattedData = formatData();

  const handleCopy = () => {
    navigator.clipboard.writeText(formattedData).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }).catch(err => {
      alert('Eroare la copiere: ' + err.message);
    });
  };

  const handleOpenGoogleSheets = () => {
    // Creează URL pentru Google Sheets
    const url = `https://docs.google.com/spreadsheets/create?usp=sharing`;
    window.open(url, '_blank');
    
    // Afișează instrucțiuni
    alert('Google Sheet deschis! După ce se încarcă:\n1. Selectează celula A1\n2. Click pe File → Import\n3. Alege "Paste" sau "Upload"\n4. Copiază datele din modal și lipește-le');
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
          <div className="modal-header">
            <h5 className="modal-title">
              {format === 'csv' ? 'Date pentru Google Sheets (CSV)' : 'Date JSON'}
            </h5>
            <button
              type="button"
              className="btn-close"
              onClick={onClose}
            ></button>
          </div>
          <div className="modal-body">
            <div className="alert alert-info">
              <strong>Instrucțiuni:</strong>
              <ol className="mb-0 mt-2">
                <li>Copiază datele de mai jos</li>
                <li>Deschide un Google Sheet nou</li>
                <li>Selectează celula A1</li>
                <li>Click pe <strong>File → Import</strong> sau <strong>Edit → Paste</strong></li>
                <li>Lipește datele copiate</li>
              </ol>
            </div>
            <div className="d-flex gap-2 mb-3">
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleCopy}
              >
                {copied ? '✅ Copiat!' : '📋 Copy to Clipboard'}
              </button>
              <button
                type="button"
                className="btn btn-success"
                onClick={handleOpenGoogleSheets}
              >
                📊 Deschide Google Sheets
              </button>
            </div>
            <textarea
              className="form-control"
              value={formattedData}
              readOnly
              rows={15}
              style={{
                fontFamily: 'monospace',
                fontSize: '12px',
                whiteSpace: 'pre',
                overflowWrap: 'normal',
                overflowX: 'auto'
              }}
              onClick={(e) => e.target.select()}
            />
          </div>
          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onClose}
            >
              Închide
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ExportDataModal;

