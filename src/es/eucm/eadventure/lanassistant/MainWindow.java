package es.eucm.eadventure.lanassistant;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 * The main window of the system, where the keys are changed, added, parsed from the code, etc.
 * 
 * @author Eugenio Marchiori
 *
 */
public class MainWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2564008632191697757L;

	/**
	 * Button used to load files or set the project folder
	 */
	private JButton loadFilesButton;
	
	/**
	 * Button used to update the content of the files
	 */
	private JButton updateFilesButton;
	
	/**
	 * Button used to parse the code for new keys
	 */
	private JButton parseCodeButton;
	
	/**
	 * Button used to save the changes to the keys
	 */
	private JButton saveChangesButton;
		
	/**
	 * Button used to add a new language
	 */
	private JButton addNewLanguageButton;
	
	private JButton removeUnusedButton;
	
	/**
	 * Button used to add a new key
	 */
	private JButton addNewKeyButton;
	
	/**
	 * Table model for the table with the keys and values
	 */
	private LangTableModel langTableModel;
	
	/**
	 * Text field used to search the keys and values
	 */
	private JTextField searchTextField;
	
	/**
	 * String with the project path
	 */
	private String projectFolder = null;
	
	/**
	 * Manager of the editor's keys and languages
	 */
	private LangManager editorLangManager;
	
	/**
	 * Manager of the engine's keys and languages
	 */
	private LangManager engineLangManager;
	
	private LangManager weevLangManager;
	
	/**
	 * Combo used to switch between editor and engine
	 */
	private JComboBox partComboBox;
	
	/**
	 * The table where keys and values are shown
	 */
	private LangTable table;
	
	/**
	 * Combo boxes used to set the language shown in each column
	 */
	private JComboBox[] languageComboBox = new JComboBox[2];
	
	/**
	 * Default window constructor
	 */
	public MainWindow() {
	    addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	        	int option = JOptionPane.showConfirmDialog(MainWindow.this, "Save changes?", "Save changes", JOptionPane.YES_NO_OPTION);
	        	if (option == JOptionPane.YES_OPTION) {
	    			editorLangManager.saveChanges();
	    			engineLangManager.saveChanges();
	    			if (LangAssistant.INCLUDE_WEEV)
	    				weevLangManager.saveChanges();
	        	}
	        	System.exit(0);
	        }
	      });
		
		JPanel buttonPanel = new JPanel();
		loadFilesButton = new JButton("Load proyect folder");
		loadFilesButton.addActionListener(new LoadFilesActionListener());
		updateFilesButton = new JButton("Update proyect files");
		updateFilesButton.addActionListener(new UpdateFilesActionListener());
		JButton editHelp = new JButton("Edit help");
		editHelp.addActionListener(new EditHelpActionListener());
		parseCodeButton = new JButton("Parse source code");
		parseCodeButton.addActionListener(new ParseCodeActionListener());
		saveChangesButton = new JButton("Save changes");
		saveChangesButton.addActionListener(new SaveChangesActionListener());
		removeUnusedButton = new JButton("Remove unused");
		removeUnusedButton.addActionListener(new RemoveUnusedActionListener());
		partComboBox = new JComboBox(new String[] {"Engine", "Editor", "WEEV"});
		partComboBox.addActionListener(new PartComboActionListener());
		
		languageComboBox[0] = new JComboBox(new String[] {"es_ES", "en_EN"});
		languageComboBox[1] = new JComboBox(new String[] {"es_ES", "en_EN"});
		languageComboBox[0].addActionListener(new LanguageActionListener(0));
		languageComboBox[1].addActionListener(new LanguageActionListener(1));
		
		searchTextField = new JTextField(30);
		searchTextField.getDocument().addDocumentListener(new SearchDocumentListener());
		
		buttonPanel.add(loadFilesButton);
		buttonPanel.add(editHelp);
		if (!LangAssistant.EXTERNAL) {
			buttonPanel.add(updateFilesButton);
			buttonPanel.add(parseCodeButton);
			buttonPanel.add(removeUnusedButton);
		}
		buttonPanel.add(saveChangesButton);
		
		JPanel comboPanel = new JPanel();
		comboPanel.add(partComboBox);
		comboPanel.add(languageComboBox[0]);
		comboPanel.add(languageComboBox[1]);
		comboPanel.add(new JLabel("Search:"));
		comboPanel.add(searchTextField);
		
		this.setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(buttonPanel, BorderLayout.NORTH);
		topPanel.add(comboPanel, BorderLayout.SOUTH);
		this.add(topPanel, BorderLayout.NORTH);
	
		langTableModel = new LangTableModel();
		table = new LangTable(langTableModel);

		JScrollPane scrollPane = new JScrollPane(table);
		
		this.add(scrollPane, BorderLayout.CENTER);
		
		addNewLanguageButton = new JButton("Add new language");
		addNewKeyButton = new JButton("Add new key");
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(addNewLanguageButton);
		addNewLanguageButton.addActionListener(new AddNewLanguageActionListener());
		if (!LangAssistant.EXTERNAL)
			bottomPanel.add(addNewKeyButton);
		addNewKeyButton.addActionListener(new AddNewKeyActionListener());
		this.add(bottomPanel, BorderLayout.SOUTH);
		
		this.setSize(850, 700);
		this.setVisible(true);
		loadProyectFolder();
	}
	
	
	/**
	 * Class that implements the "Load Files" action listener
	 * 
	 * @author Eugenio Marchiori
	 *
	 */
	private class LoadFilesActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			loadProyectFolder();
		}
	}

	/**
	 * Class that implements the "Update Files" action listener
	 * 
	 * @author Eugenio Marchiori
	 *
	 */
	private class UpdateFilesActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			loadFileContents(true);
		}
	}

	private class EditHelpActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			new HelpEditor(projectFolder);
		}
	}

	/**
	 * Class that implements the "Parse code" action listener
	 * 
	 * @author Eugenio Marchiori
	 *
	 */
	private class ParseCodeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			CodeParser.parseCode(projectFolder + File.separator + "src/es/eucm/eadventure/editor", editorLangManager);
			CodeParser.parseCode(projectFolder + File.separator + "src/es/eucm/eadventure/engine", engineLangManager);
			if (LangAssistant.INCLUDE_WEEV)
				CodeParser.parseCode(projectFolder + File.separator + "src/es/eucm/eadventure/weev" , weevLangManager);
			langTableModel.fireTableDataChanged();
		}
	}

	/**
	 * Class that implements the "Save changes" action listener
	 * 
	 * @author Eugenio Marchiori
	 *
	 */
	private class SaveChangesActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			editorLangManager.saveChanges();
			engineLangManager.saveChanges();
			if (LangAssistant.INCLUDE_WEEV)
				weevLangManager.saveChanges();
			if (!LangAssistant.EXTERNAL)
				JOptionPane.showMessageDialog(MainWindow.this, "Remember to refresh the language folders in Eclipse", "Changes saved", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private class SearchDocumentListener implements DocumentListener {

		public void changedUpdate(DocumentEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			updateSearch(e);
		}

		public void removeUpdate(DocumentEvent e) {
			updateSearch(e);
		}
		
		private void updateSearch(DocumentEvent e) {
			try {
				if (partComboBox.getSelectedIndex() == 0)
					engineLangManager.search(e.getDocument().getText(0, e.getDocument().getLength()));
				else if (partComboBox.getSelectedIndex() == 1)
					editorLangManager.search(e.getDocument().getText(0, e.getDocument().getLength()));
				else
					weevLangManager.search(e.getDocument().getText(0, e.getDocument().getLength()));
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
			langTableModel.fireTableDataChanged();
		}
	}

	private class AddNewLanguageActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int confirm = JOptionPane.showConfirmDialog(MainWindow.this, "Are you sure?", "Add new language", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				String newLanguage = JOptionPane.showInputDialog(MainWindow.this, "New language", "Add new language", JOptionPane.QUESTION_MESSAGE);
				
				boolean added = editorLangManager.addLanguage(newLanguage, "comment" + newLanguage);
				if (added && partComboBox.getSelectedIndex() == 1) {
					Object selected = languageComboBox[0].getSelectedItem();
					languageComboBox[0].setModel(new DefaultComboBoxModel(editorLangManager.getLangIDs()));
					languageComboBox[0].setSelectedItem(selected);
					selected = languageComboBox[1].getSelectedItem();
					languageComboBox[1].setModel(new DefaultComboBoxModel(editorLangManager.getLangIDs()));
					languageComboBox[1].setSelectedItem(selected);
				}
				
				added = engineLangManager.addLanguage(newLanguage, "comment" + newLanguage);
				if (added && partComboBox.getSelectedIndex() == 0) {
					Object selected = languageComboBox[0].getSelectedItem();
					languageComboBox[0].setModel(new DefaultComboBoxModel(engineLangManager.getLangIDs()));
					languageComboBox[0].setSelectedItem(selected);
					selected = languageComboBox[1].getSelectedItem();
					languageComboBox[1].setModel(new DefaultComboBoxModel(engineLangManager.getLangIDs()));
					languageComboBox[1].setSelectedItem(selected);
				}
				
				if (LangAssistant.INCLUDE_WEEV) {
					added = weevLangManager.addLanguage(newLanguage, "comment" + newLanguage);
					if (added && partComboBox.getSelectedIndex() == 2) {
						Object selected = languageComboBox[0].getSelectedItem();
						languageComboBox[0].setModel(new DefaultComboBoxModel(weevLangManager.getLangIDs()));
						languageComboBox[0].setSelectedItem(selected);
						selected = languageComboBox[1].getSelectedItem();
						languageComboBox[1].setModel(new DefaultComboBoxModel(weevLangManager.getLangIDs()));
						languageComboBox[1].setSelectedItem(selected);
					}
				}

			}
		}
	}

	private class AddNewKeyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String newKey = JOptionPane.showInputDialog(MainWindow.this, "New key", "Add new key", JOptionPane.QUESTION_MESSAGE);
			int row = 0;
			if (partComboBox.getSelectedIndex() == 1)
				row = editorLangManager.addKey(newKey);
			if (partComboBox.getSelectedIndex() == 0)
				row = engineLangManager.addKey(newKey);
			if (partComboBox.getSelectedIndex() == 2)
				row = weevLangManager.addKey(newKey);
			langTableModel.fireTableDataChanged();
			table.changeSelection(row, 0, true, true);
		}
	}

	private class PartComboActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (partComboBox.getSelectedIndex() == 1) {
				langTableModel.setLangManager(editorLangManager);
				languageComboBox[0].setModel(new DefaultComboBoxModel(editorLangManager.getLangIDs()));
				languageComboBox[1].setModel(new DefaultComboBoxModel(editorLangManager.getLangIDs()));
			} else if (partComboBox.getSelectedIndex() == 0 ){
				langTableModel.setLangManager(engineLangManager);
				languageComboBox[0].setModel(new DefaultComboBoxModel(engineLangManager.getLangIDs()));
				languageComboBox[1].setModel(new DefaultComboBoxModel(engineLangManager.getLangIDs()));
			} else {
				langTableModel.setLangManager(weevLangManager);
				languageComboBox[0].setModel(new DefaultComboBoxModel(weevLangManager.getLangIDs()));
				languageComboBox[1].setModel(new DefaultComboBoxModel(weevLangManager.getLangIDs()));
			}
			languageComboBox[0].setSelectedIndex(0);
			languageComboBox[1].setSelectedIndex(1);
		}
	}

	private class RemoveUnusedActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int count = UnusedKeyRemover.unusedKeyRemover(projectFolder + File.separator + "src/es/eucm/eadventure", editorLangManager);
			count += UnusedKeyRemover.unusedKeyRemover(projectFolder + File.separator + "src/es/eucm/eadventure", engineLangManager);
			if (LangAssistant.INCLUDE_WEEV)
				count += UnusedKeyRemover.unusedKeyRemover(projectFolder + File.separator + "src/es/eucm/eadventure/weev", weevLangManager);
//			langTableModel.fireTableDataChanged();
			//JOptionPane.showMessageDialog(MainWindow.this, "The key removal process has finished", "Removed " + count + " keys", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private class LanguageActionListener implements ActionListener {
		int lang = 0;
		public LanguageActionListener(int lang) {
			this.lang = lang;
		}
		public void actionPerformed(ActionEvent e) {
			langTableModel.setLang(lang, (String) languageComboBox[lang].getSelectedItem());
		}
	}
	
	
	private void loadProyectFolder() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int retValue = fc.showOpenDialog(MainWindow.this);
		
		if (retValue == JFileChooser.APPROVE_OPTION) {
			boolean isProyectFolder = true;
			File selected = fc.getSelectedFile();
			isProyectFolder = isProyectFolder && new File(selected.getAbsolutePath() + File.separator + "i18n/editor").exists();
			isProyectFolder = isProyectFolder && new File(selected.getAbsolutePath() + File.separator + "i18n/engine").exists();
			if (!LangAssistant.EXTERNAL)
				isProyectFolder = isProyectFolder && new File(selected.getAbsolutePath() + File.separator + "src").exists();

			if (isProyectFolder) {
				projectFolder = selected.getAbsolutePath();
				loadFileContents(false);
			} else
				JOptionPane.showMessageDialog(MainWindow.this, "The folder isn't the <e-Adventure> proyect folder", "Wrong folder", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void loadFileContents(boolean update) {
		if (projectFolder != null) {
			String editorFiles = projectFolder + File.separator + "i18n" + File.separator + "editor";
			String engineFiles = projectFolder + File.separator + "i18n" + File.separator + "engine";
			String weevFiles = projectFolder + File.separator + "i18n" + File.separator + "weev";
			if (!update || editorLangManager == null || engineLangManager == null) { 
				editorLangManager = new LangManager(editorFiles);
				engineLangManager = new LangManager(engineFiles);
				if (LangAssistant.INCLUDE_WEEV)
					weevLangManager = new LangManager(weevFiles);
			} else {
				editorLangManager.update(editorFiles);
				engineLangManager.update(engineFiles);
				if (LangAssistant.INCLUDE_WEEV)
					weevLangManager.update(weevFiles);
			}
			
			if (partComboBox.getSelectedIndex() == 1) {
				langTableModel.setLangManager(editorLangManager);
				languageComboBox[0].setModel(new DefaultComboBoxModel(editorLangManager.getLangIDs()));
				languageComboBox[1].setModel(new DefaultComboBoxModel(editorLangManager.getLangIDs()));
			} else if (partComboBox.getSelectedIndex() == 0){
				langTableModel.setLangManager(engineLangManager);
				languageComboBox[0].setModel(new DefaultComboBoxModel(engineLangManager.getLangIDs()));
				languageComboBox[1].setModel(new DefaultComboBoxModel(engineLangManager.getLangIDs()));
			} else if (partComboBox.getSelectedIndex() == 2) {
				langTableModel.setLangManager(weevLangManager);
				languageComboBox[0].setModel(new DefaultComboBoxModel(weevLangManager.getLangIDs()));
				languageComboBox[1].setModel(new DefaultComboBoxModel(weevLangManager.getLangIDs()));
			}
			languageComboBox[0].setSelectedIndex(0);
			languageComboBox[1].setSelectedIndex(1);
		}
	}

}
