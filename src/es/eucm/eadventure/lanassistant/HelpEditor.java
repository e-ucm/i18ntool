package es.eucm.eadventure.lanassistant;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.metaphaseeditor.MetaphaseEditor;

public class HelpEditor extends JFrame {

	private String projectFolder;
	
	private List<String> languages;
	
	private JTable table;
	
	private JComboBox langCombo;
	
	private MetaphaseEditor editor;
	
	private int selected = -1;
	
	public HelpEditor(String projectFolder) {
		this.projectFolder = projectFolder;
		File helpFolder = new File(projectFolder + "/help");
		languages = new ArrayList<String>();
		for (String f: helpFolder.list()) {
			if (!f.equals(".svn") && !f.equals("common_img") && !f.equals("help.css"))
				languages.add(f);
		}
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		langCombo = new JComboBox(languages.toArray(new String[]{}));
		langCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTableContents();
			}
		});
		leftPanel.add(langCombo, BorderLayout.NORTH);
		
		
		table = new JTable();
		leftPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (table.getSelectedRow() != -1 && selected != table.getSelectedRow()) {
					editor.save();
					editor.openFile(((File) table.getModel().getValueAt(table.getSelectedRow(), 0)));
					selected = table.getSelectedRow();
				}
			}
		});
		
		setTableContents();
		
		setLayout(new BorderLayout());
		editor = new MetaphaseEditor();
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, editor), BorderLayout.CENTER);
		setSize(800, 600);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
	}
	

	private void setTableContents() {
		File file = new File(projectFolder + "/help/" + langCombo.getSelectedItem());
		List<File> files = getFiles(file.listFiles());
		DefaultTableModel dtm = new DefaultTableModel();
		table.setModel(dtm);
		dtm.setColumnCount(1);
		for (File f : files) {
			dtm.addRow(new Object[]{f});
			System.out.println(f.getAbsolutePath());
		}
		table.getColumnModel().getColumn(0).setCellEditor((TableCellEditor) new FileRenderer());
		table.getColumnModel().getColumn(0).setCellRenderer(new FileRenderer());
	}

	private static class FileRenderer implements TableCellEditor, TableCellRenderer  {

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (value == null)
				return new JLabel("null");
			JLabel label = new JLabel(((File) value).getName());
			if (isSelected) {
				label.setOpaque(true);
				label.setBackground(table.getSelectionBackground());
			}
			return label;
		}

		public boolean isCellEditable(EventObject anEvent) {
			return false;
		}

		public Object getCellEditorValue() {
			return null;
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			// TODO Auto-generated method stub
			return null;
		}

		public void addCellEditorListener(CellEditorListener l) {
			// TODO Auto-generated method stub
			
		}

		public void cancelCellEditing() {
			// TODO Auto-generated method stub
			
		}

		public void removeCellEditorListener(CellEditorListener l) {
			// TODO Auto-generated method stub
			
		}

		public boolean shouldSelectCell(EventObject anEvent) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean stopCellEditing() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	private List<File> getFiles(File[] files) {
		List<File> result = new ArrayList<File>();
		for (File s : files) {
			if (s.isDirectory()) {
				result.addAll(getFiles(s.listFiles()));
			} else if (s.getAbsolutePath().endsWith("html")) {
				result.add(s);
			}
		}
		return result;
	}
		
}
