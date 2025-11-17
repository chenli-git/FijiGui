package org.stjude.swingui.boot.event;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for displaying recorded actions in a JTable
 */
public class RecorderTableModel extends AbstractTableModel {
    private final List<ActionRecord> records = new ArrayList<>();
    private final String[] columnNames = {"Channel", "Action", "Parameters"};

    public void addRecord(ActionRecord record) {
        records.add(record);
        int row = records.size() - 1;
        fireTableRowsInserted(row, row);
    }

    public void removeLastRecord() {
        int size = records.size();
        if (size > 0) {
            records.remove(size - 1);
            fireTableRowsDeleted(size - 1, size - 1);
        }
    }

    public void clear() {
        int size = records.size();
        if (size > 0) {
            records.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }

    public List<ActionRecord> getRecords() {
        return new ArrayList<>(records);
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= records.size()) {
            return null;
        }
        
        ActionRecord record = records.get(rowIndex);
        switch (columnIndex) {
            case 0: return record.getChannelLabel();
            case 1: return record.getActionId();
            case 2: return record.getParamsAsString();
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
