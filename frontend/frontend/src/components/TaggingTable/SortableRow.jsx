import React, { useMemo } from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import EditableCell from './EditableCell';
import EventDropdownCell from './EventDropdownCell';
import PlatformBadge from './PlatformBadge';

const SortableRow = ({ 
  row, 
  rowIndex, 
  columns, 
  showEventColumn,
  isEventRow,
  isEventRowForDropdown,
  events,
  allEvents,
  editingCell,
  setEditingCell,
  handleCellUpdate,
  handleEventSelect,
  handleDelete
}) => {
  // Creează un ID unic care include index-ul pentru a evita duplicate
  const uniqueId = useMemo(() => {
    const eventName = row.eventName || 'param';
    const propertyName = row.propertyName || `param-${rowIndex}`;
    const platform = row.eventCategory || 'default';
    return `${eventName}-${propertyName}-${platform}-${rowIndex}`;
  }, [row.eventName, row.propertyName, row.eventCategory, rowIndex]);

  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ 
    id: uniqueId
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <tr
      ref={setNodeRef}
      style={style}
      id={`row-${rowIndex}`}
      className={isEventRow(row) ? 'table-primary' : ''}
    >
      <td 
        {...attributes}
        {...listeners}
        style={{ width: '30px', cursor: 'grab', textAlign: 'center' }}
        className="user-select-none"
      >
        <span>🟰</span>
      </td>
      {columns.map((col) => (
        <td key={col.key}>
          {col.key === 'eventCategory' ? (
            <PlatformBadge platform={row[col.key]} />
          ) : (
            <EditableCell
              value={row[col.key] || ''}
              rowIndex={rowIndex}
              field={col.key}
              isEditing={editingCell === `${rowIndex}-${col.key}`}
              onEdit={() => setEditingCell(`${rowIndex}-${col.key}`)}
              onCancel={() => setEditingCell(null)}
              onSave={(value) => handleCellUpdate(rowIndex, col.key, value)}
              isEventRow={isEventRow(row)}
            />
          )}
        </td>
      ))}
      {showEventColumn && (
        <td>
          {isEventRowForDropdown(rowIndex) ? (
            <EventDropdownCell
              rowIndex={rowIndex}
              events={events}
              allEvents={allEvents}
              onEventSelect={handleEventSelect}
              currentValue={row.selectedEvents || row.selectedEvent || []}
            />
          ) : (
            <div style={{ padding: '6px 8px', color: '#6c757d', fontSize: '13px' }}>
              —
            </div>
          )}
        </td>
      )}
      <td>
        <button
          className="btn btn-sm btn-danger"
          onClick={() => handleDelete(rowIndex)}
          title="Șterge rând"
        >
          🗑️
        </button>
      </td>
    </tr>
  );
};

export default SortableRow;

