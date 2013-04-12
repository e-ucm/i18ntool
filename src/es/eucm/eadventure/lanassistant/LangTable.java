package es.eucm.eadventure.lanassistant;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;


public class LangTable extends JTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5304991346926030396L;
	
	private LangTableModel langTableModel;

	public LangTable(LangTableModel langTableModel) {
		super(langTableModel);
		this.langTableModel = langTableModel;
		getColumnModel().setColumnSelectionAllowed(false);
		setDragEnabled(false);
		getSelectionModel( ).setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		getColumnModel().getColumn(0).setCellRenderer(new KeyCellRenderer());
		getColumnModel().getColumn(1).setCellRenderer(new JLabelCellRenderer());
		getColumnModel().getColumn(2).setCellRenderer(new JLabelCellRenderer());
		
		
		setRowHeight( 20 );
		getSelectionModel( ).addListSelectionListener( new ListSelectionListener( ) {
            public void valueChanged( ListSelectionEvent arg0 ) {
            	setRowHeight( 20 );
            	setRowHeight( getSelectedRow( ), 44 );
            }
        } );
	}
	
	private class JLabelCellRenderer implements TableCellRenderer {

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JLabel label = new JLabel();
			if (value != null && value instanceof String && !value.equals("")) {
				label.setText((String) value);
				if (isSelected) {
					label.setOpaque(true);
					label.setBackground(table.getSelectionBackground());
				}
				return label;
			}
			label.setOpaque(true);
			label.setBackground(Color.RED);
			return label;
		}
		
	}

	private class KeyCellRenderer implements TableCellRenderer {

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JLabel label = new JLabel();
			if (value != null && value instanceof String) {
				label.setText((String) value);
				if (isSelected) {
					label.setOpaque(true);
					label.setBackground(table.getSelectionBackground());
				} else if (langTableModel.isModifiedKey(row)) {
					label.setOpaque(true);
					label.setBackground(Color.GREEN);
				}
				return label;
			}
			label.setOpaque(true);
			label.setBackground(Color.RED);
			return label;
		}
		
	}

}
