package es.eucm.eadventure.lanassistant;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class UnusedKeysFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6488526137140509326L;

	public UnusedKeysFrame(List<String> keys, final LangManager langManager) {
		setLayout(new BorderLayout());
		
		JTable table = new JTable();
		final List<String> keysToRemove = new ArrayList<String>();
		for (String key : keys)
			keysToRemove.add(key);
		table.setModel(new UnusedKeysTableModel(keys, keysToRemove));
		table.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
		table.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer());
		add(new JScrollPane(table), BorderLayout.CENTER);
		setSize(400, 400);
		
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (String key: keysToRemove) {
					langManager.removeKey(key);
				}
				setVisible(false);
			}
		});
		add(ok, BorderLayout.SOUTH);
		this.setVisible(true);
	}
	
	private class UnusedKeysTableModel extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2428387004637676769L;

		private List<String> keys;
		
		private List<String> keysToRemove;
		
		public UnusedKeysTableModel(List<String> keys, List<String> keysToRemove) {
			this.keys = keys;
			this.keysToRemove = keysToRemove;
		}
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 1)
				return Boolean.class;
			return String.class;
		}
		public int getColumnCount() {
			return 2;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "Key";
			}
			if (columnIndex == 1) {
				return "remove?";
			}
			return "";
		}

		public int getRowCount() {
			return keys.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return keys.get(rowIndex);
			}
			if (columnIndex == 1) {
				return new Boolean(keysToRemove.contains(keys.get(rowIndex)));
			}
			return null;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				Boolean b = (Boolean) aValue;
				if (b.booleanValue())
					keysToRemove.add(keys.get(rowIndex));
				else
					keysToRemove.remove(keys.get(rowIndex));
			}
		}
		
	}
	
}
