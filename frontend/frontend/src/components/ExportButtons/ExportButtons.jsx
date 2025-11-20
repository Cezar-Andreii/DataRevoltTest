import React, { useState } from 'react';
import { useTagging } from '../../context/TaggingContext';
import ExportDataModal from '../ExportDataModal/ExportDataModal';

const ExportButtons = () => {
  const { taggingRows, exportGoogleSheet, exportCSV, exportJSON, exportGTMJSON, resetRows, loading } = useTagging();
  const [exporting, setExporting] = useState(false);
  const [showExportModal, setShowExportModal] = useState(false);
  const [exportFormat, setExportFormat] = useState('csv');

  const handleExportGoogleSheet = async () => {
    if (taggingRows.length === 0) {
      alert('Nu există date pentru export. Generează mai întâi un tagging plan.');
      return;
    }

    // Deschide modalul cu datele pentru copiere manuală
    setExportFormat('csv');
    setShowExportModal(true);
  };

  const handleExportGoogleSheetWithAPI = async () => {
    if (taggingRows.length === 0) {
      alert('Nu există date pentru export. Generează mai întâi un tagging plan.');
      return;
    }

    setExporting(true);
    try {
      const response = await exportGoogleSheet();
      if (response.success && response.url) {
        window.open(response.url, '_blank');
      } else {
        alert(response.message || 'Eroare la exportul Google Sheets');
      }
    } catch (error) {
      alert('Eroare la exportul Google Sheets: ' + error.message);
    } finally {
      setExporting(false);
    }
  };

  const handleExportCSV = async () => {
    if (taggingRows.length === 0) {
      alert('Nu există date pentru export. Generează mai întâi un tagging plan.');
      return;
    }

    setExporting(true);
    try {
      await exportCSV();
    } catch (error) {
      alert('Eroare la exportul CSV: ' + error.message);
    } finally {
      setExporting(false);
    }
  };

  const handleExportJSON = async () => {
    if (taggingRows.length === 0) {
      alert('Nu există date pentru export. Generează mai întâi un tagging plan.');
      return;
    }

    setExporting(true);
    try {
      const response = await exportJSON();
      if (response.success) {
        // Creează și descarcă fișierul JSON
        const dataStr = JSON.stringify(response.data, null, 2);
        const dataBlob = new Blob([dataStr], { type: 'application/json' });
        const url = window.URL.createObjectURL(dataBlob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'tagging-plan.json');
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
      }
    } catch (error) {
      alert('Eroare la exportul JSON: ' + error.message);
    } finally {
      setExporting(false);
    }
  };

  const handleExportGTMJSON = async () => {
    if (taggingRows.length === 0) {
      alert('Nu există date pentru export. Generează mai întâi un tagging plan.');
      return;
    }

    setExporting(true);
    try {
      const response = await exportGTMJSON();
      if (response.success) {
        // Creează și descarcă fișierul GTM JSON
        const dataStr = JSON.stringify(response.data, null, 2);
        const dataBlob = new Blob([dataStr], { type: 'application/json' });
        const url = window.URL.createObjectURL(dataBlob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'gtm-container.json');
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
      } else {
        alert(response.message || 'Eroare la exportul GTM JSON');
      }
    } catch (error) {
      alert('Eroare la exportul GTM JSON: ' + error.message);
    } finally {
      setExporting(false);
    }
  };

  const handleReset = async () => {
    if (taggingRows.length === 0) {
      alert('Tabelul este deja gol.');
      return;
    }

    if (window.confirm('Ești sigur că vrei să resetezi tabelul? Toate datele vor fi șterse.')) {
      try {
        await resetRows();
        alert('Tabelul a fost resetat cu succes.');
      } catch (error) {
        alert('Eroare la resetarea tabelului: ' + error.message);
      }
    }
  };

  const isDisabled = taggingRows.length === 0 || loading || exporting;

  return (
    <>
      <div className="d-flex align-items-center gap-2 flex-wrap">
        <button
          type="button"
          className="btn btn-success btn-sm"
          onClick={handleExportGoogleSheet}
          disabled={isDisabled}
          title="Creează Google Sheet fără API (copiere manuală)"
        >
          📊 Creează Google Sheet (fără API)
        </button>
        
        <button
          type="button"
          className="btn btn-outline-success btn-sm"
          onClick={handleExportGoogleSheetWithAPI}
          disabled={isDisabled || exporting}
          title="Creează Google Sheet cu API (dacă e configurat)"
        >
          {exporting ? (
            <>
              <span className="spinner-border spinner-border-sm me-1" role="status"></span>
              Exporting...
            </>
          ) : (
            <>
              🔗 Google Sheet (cu API)
            </>
          )}
        </button>
      
      <button
        type="button"
        className="btn btn-outline-success btn-sm"
        onClick={handleExportCSV}
        disabled={isDisabled}
        title="Descarcă CSV"
      >
        📥 Download CSV
      </button>
      
      <button
        type="button"
        className="btn btn-outline-info btn-sm"
        onClick={handleExportJSON}
        disabled={isDisabled}
        title="Export JSON"
      >
        📄 Export JSON
      </button>
      
      <button
        type="button"
        className="btn btn-outline-warning btn-sm"
        onClick={handleExportGTMJSON}
        disabled={isDisabled || exporting}
        title="Export GTM JSON (Google Tag Manager)"
      >
        {exporting ? (
          <>
            <span className="spinner-border spinner-border-sm me-1" role="status"></span>
            Exporting...
          </>
        ) : (
          <>
            🏷️ Export GTM JSON
          </>
        )}
      </button>
      
      <button
        type="button"
        className="btn btn-danger btn-sm"
        onClick={handleReset}
        disabled={isDisabled}
        title="Resetează tabelul"
      >
        🗑️ Reset Table
      </button>
      </div>
      
      <ExportDataModal
        show={showExportModal}
        onClose={() => setShowExportModal(false)}
        data={taggingRows}
        format={exportFormat}
      />
    </>
  );
};

export default ExportButtons;

