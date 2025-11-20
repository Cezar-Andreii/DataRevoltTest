import React, { useState, useEffect, useRef } from 'react';

const EditableCell = ({
  value,
  rowIndex,
  field,
  isEditing,
  onEdit,
  onCancel,
  onSave,
  isEventRow
}) => {
  const [editValue, setEditValue] = useState(value);
  const inputRef = useRef(null);

  useEffect(() => {
    setEditValue(value);
  }, [value]);

  useEffect(() => {
    if (isEditing && inputRef.current) {
      inputRef.current.focus();
      inputRef.current.select();
    }
  }, [isEditing]);

  const handleDoubleClick = () => {
    if (!isEditing) {
      onEdit();
    }
  };

  const handleBlur = () => {
    if (editValue !== value) {
      onSave(editValue);
    } else {
      onCancel();
    }
  };

  const handleKeyPress = (e) => {
    // Pentru textarea, Enter nu salvează (permite newline)
    if (isTextarea) {
      if (e.key === 'Escape') {
        setEditValue(value);
        onCancel();
      }
      // Ctrl+Enter sau Cmd+Enter salvează pentru textarea
      if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
        if (editValue !== value) {
          onSave(editValue);
        } else {
          onCancel();
        }
      }
    } else {
      // Pentru input, Enter salvează
      if (e.key === 'Enter') {
        if (editValue !== value) {
          onSave(editValue);
        } else {
          onCancel();
        }
      } else if (e.key === 'Escape') {
        setEditValue(value);
        onCancel();
      }
    }
  };

  const displayValue = value || '';
  const isEmpty = !value || value.trim() === '';

  const isCodeExamples = field === 'codeExamples';
  const isTextarea = isCodeExamples || field === 'eventDescription' || field === 'propertyDefinition';

  if (isEditing) {
    if (isTextarea) {
      return (
        <textarea
          ref={inputRef}
          className="form-control form-control-sm"
          value={editValue}
          onChange={(e) => setEditValue(e.target.value)}
          onBlur={handleBlur}
          onKeyDown={handleKeyPress}
          rows={isCodeExamples ? 3 : 2}
          style={{
            width: '100%',
            padding: '6px 8px',
            fontSize: isCodeExamples ? '12px' : '13px',
            fontFamily: isCodeExamples ? 'monospace' : 'inherit',
            border: '2px solid #4285f4',
            borderRadius: '4px',
            resize: 'vertical'
          }}
        />
      );
    }
    
    return (
      <input
        ref={inputRef}
        type="text"
        className="form-control form-control-sm"
        value={editValue}
        onChange={(e) => setEditValue(e.target.value)}
        onBlur={handleBlur}
        onKeyDown={handleKeyPress}
        style={{
          width: '100%',
          padding: '6px 8px',
          fontSize: '13px',
          border: '2px solid #4285f4',
          borderRadius: '4px'
        }}
      />
    );
  }

  const displayStyle = {
    padding: '6px 8px',
    fontSize: isCodeExamples ? '11px' : '13px',
    cursor: 'text',
    minHeight: isTextarea ? (isCodeExamples ? '60px' : '50px') : '32px',
    border: '1px solid transparent',
    borderRadius: '4px',
    backgroundColor: isEmpty && !isEventRow ? 'transparent' : 'white',
    color: isEmpty && !isEventRow ? '#6c757d' : '#212529',
    fontStyle: isEmpty && !isEventRow ? 'italic' : 'normal',
    borderStyle: isEmpty && !isEventRow ? 'dashed' : 'solid',
    borderColor: isEmpty && !isEventRow ? '#dee2e6' : 'transparent',
    fontFamily: isCodeExamples ? 'monospace' : 'inherit',
    whiteSpace: isCodeExamples ? 'pre-wrap' : 'normal',
    overflow: isCodeExamples ? 'auto' : 'hidden',
    textOverflow: isCodeExamples ? 'unset' : 'ellipsis'
  };

  return (
    <div
      className={`editable-cell ${isEmpty && !isEventRow ? 'bg-light' : ''}`}
      onDoubleClick={handleDoubleClick}
      style={displayStyle}
      title="Dublu-click pentru editare"
    >
      {displayValue || (isEmpty && !isEventRow ? '' : '')}
    </div>
  );
};

export default EditableCell;

