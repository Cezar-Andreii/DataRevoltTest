import React, { useState } from 'react';
import { generateJavaScriptCode } from '../../services/codeGenerator';

const CodeGenerator = ({ eventName, parameters = [], items = [] }) => {
  const [code, setCode] = useState('');
  const [copied, setCopied] = useState(false);

  React.useEffect(() => {
    if (eventName) {
      const generatedCode = generateJavaScriptCode(eventName, parameters, items);
      setCode(generatedCode);
    } else {
      setCode('');
    }
  }, [eventName, parameters, items]);

  const handleCopy = () => {
    navigator.clipboard.writeText(code).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }).catch(err => {
      alert('Eroare la copiere: ' + err.message);
    });
  };

  if (!eventName) {
    return (
      <div className="card">
        <div className="card-body">
          <p className="text-muted">Selectează un eveniment pentru a genera codul JavaScript.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="card">
      <div className="card-header d-flex justify-content-between align-items-center">
        <h5>Cod JavaScript Generat</h5>
        <button
          type="button"
          className="btn btn-primary btn-sm"
          onClick={handleCopy}
        >
          {copied ? '✅ Copiat!' : '📋 Copy to Clipboard'}
        </button>
      </div>
      <div className="card-body">
        <pre
          style={{
            backgroundColor: '#f5f5f5',
            padding: '15px',
            borderRadius: '8px',
            overflow: 'auto',
            fontSize: '12px',
            fontFamily: 'monospace',
            lineHeight: '1.5',
            maxHeight: '400px',
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word'
          }}
        >
          {code || 'Generând cod...'}
        </pre>
      </div>
    </div>
  );
};

export default CodeGenerator;

