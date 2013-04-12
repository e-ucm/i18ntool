package es.eucm.eadventure.lanassistant;

import javax.swing.table.AbstractTableModel;

public class LangTableModel extends AbstractTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 856087427642263016L;

	private LangManager langManager;
	
	private String[] lang = {"es_ES", "en_EN"};
	
	public void setLangManager(LangManager langManager) {
		this.langManager = langManager;
		this.fireTableDataChanged();
	}
	
	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		if (langManager == null)
			return 0;
		return langManager.getKeyCount();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0)
			return langManager.getKey(rowIndex);
		if (columnIndex == 1 || columnIndex == 2)
			return langManager.getValue(langManager.getKey(rowIndex), lang[columnIndex - 1]);
		return null;
	}

    @Override
    public void setValueAt( Object value, int rowIndex, int columnIndex ) {
    	if (columnIndex > 0)
    	langManager.setKeyValue(langManager.getKey(rowIndex), lang[columnIndex - 1], (String) value);
    }

    public boolean isCellEditable( int row, int column ) {
        return true;
    }

	public void setLang(int lang2, String selectedItem) {
		lang[lang2] = selectedItem;
		this.fireTableDataChanged();
	}
	
	public boolean isModifiedKey(int rowIndex) {
		return langManager.isModifiedKey(langManager.getKey(rowIndex));
	}

}
