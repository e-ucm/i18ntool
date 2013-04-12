/**
 * Metaphase Editor - WYSIWYG HTML Editor Component
 * Copyright (C) 2010  Rudolf Visagie
 * Full text of license can be found in com/metaphaseeditor/LICENSE.txt
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * The author can be contacted at metaphase.editor@gmail.com.
 */

package com.metaphaseeditor;

import com.metaphaseeditor.action.AddAttributesAction;
import com.metaphaseeditor.action.ClearFormattingAction;
import com.metaphaseeditor.action.DecreaseIndentAction;
import com.metaphaseeditor.action.FindReplaceAction;
import com.metaphaseeditor.action.FormatAction;
import com.metaphaseeditor.action.IncreaseIndentAction;
import com.metaphaseeditor.action.InsertHtmlAction;
import com.metaphaseeditor.action.InsertTextAction;
import com.metaphaseeditor.action.RemoveAttributesAction;
import com.metaphaseeditor.action.UnlinkAction;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *
 * @author Rudolf Visagie
 */
public class MetaphaseEditorPanel extends javax.swing.JPanel {

    private JTextArea htmlTextArea;
    private boolean htmlSourceMode = false;    
    private SpecialCharacterDialog specialCharacterDialog = new SpecialCharacterDialog(null, true);
    private Hashtable<Object, Action> editorKitActions;
    private SpellCheckDictionaryVersion spellCheckDictionaryVersion = SpellCheckDictionaryVersion.LIBERAL_US;
    private String customDictionaryFilename = null;

    /** Listener for the edits on the current document. */
    protected UndoableEditListener undoHandler = new UndoHandler();

    /** UndoManager that we add edits to. */
    protected UndoManager undo = new UndoManager();

    private UndoAction undoAction = new UndoAction();
    private RedoAction redoAction = new RedoAction();
    private HTMLEditorKit.CutAction cutAction = new HTMLEditorKit.CutAction();
    private HTMLEditorKit.CopyAction copyAction = new HTMLEditorKit.CopyAction();
    private HTMLEditorKit.PasteAction pasteAction = new HTMLEditorKit.PasteAction();
    private FindReplaceAction findReplaceAction;

    private HTMLEditorKit editorKit = new HTMLEditorKit();

    private JPopupMenu contextMenu;

    private List<ContextMenuListener> contextMenuListeners = new ArrayList<ContextMenuListener>();
    private List<EditorMouseMotionListener> editorMouseMotionListeners = new ArrayList<EditorMouseMotionListener>();

    private enum ParagraphFormat {
        PARAGRAPH_FORMAT("Format", null),
        NORMAL("Normal", Tag.P),
        HEADING1("Heading 1", Tag.H1),
        HEADING2("Heading 2", Tag.H2),
        HEADING3("Heading 3", Tag.H3),
        HEADING4("Heading 4", Tag.H4),
        HEADING5("Heading 5", Tag.H5),
        HEADING6("Heading 6", Tag.H6),
        FORMATTED("Formatted", Tag.PRE),
        ADDRESS("Address", Tag.ADDRESS);

        private String text;
        private Tag tag;

        ParagraphFormat(String text, Tag tag) {
            this.text = text;
            this.tag = tag;
        }

        public Tag getTag() {
            return tag;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private enum FontItem {
        FONT("Font", null),
        ARIAL("Arial", "Arial"),
        COMIC_SANS_MS("Comic Sans MS", "Comic Sans MS"),
        COURIER_NEW("Courier New", "Courier New"),
        GEORGIA("Georgia", "Georgia"),
        LUCINDA_SANS_UNICODE("Lucinda Sans Unicode", "Lucinda Sans Unicode"),
        TAHOMA("Tahoma", "Tahoma"),
        TIMES_NEW_ROMAN("Times New Roman", "Times New Roman"),
        TREBUCHET_MS("Trebuchet MS", "Trebuchet MS"),
        VERDANA("Verdana", "Verdana");

        private String text;
        private String fontName;

        FontItem(String text, String fontName) {
            this.text = text;
            this.fontName = fontName;
        }

        public String getText() {
            return text;
        }

        public String getFontName() {
            return fontName;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private enum FontSize {
        FONT_SIZE("Size", -1),
        SIZE8("8", 8),
        SIZE9("9", 9),
        SIZE10("10", 10),
        SIZE11("11", 11),
        SIZE12("12", 12),
        SIZE14("14", 14),
        SIZE18("18", 18),
        SIZE20("20", 20),
        SIZE22("22", 22),
        SIZE24("24", 24),
        SIZE26("26", 26),
        SIZE28("28", 28),
        SIZE36("36", 36),
        SIZE48("48", 48),
        SIZE72("72", 72);

        private String text;
        private int size;

        FontSize(String text, int size) {
            this.text = text;
            this.size = size;
        }

        public String getText() {
            return text;
        }

        public int getSize() {
            return size;
        }

        @Override
        public String toString() {
            return text;
        }
    }
    
    /** Creates new form MetaphaseEditorPanel */
    public MetaphaseEditorPanel() {        
        initComponents();

        createEditorKitActionTable();
        
        htmlTextArea = new JTextArea();
        htmlTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        htmlTextPane.setContentType("text/html");

        findReplaceAction = new FindReplaceAction("Find/Replace", htmlTextPane);

        cutButton.setAction(cutAction);
        cutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/cut.png")));
        cutButton.setText("");
        cutButton.setToolTipText("Cut");

        copyButton.setAction(copyAction);
        copyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/copy.png")));
        copyButton.setText("");
        copyButton.setToolTipText("Copy");

        pasteButton.setAction(pasteAction);
        pasteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/paste.png")));
        pasteButton.setText("");
        pasteButton.setToolTipText("Paste");

        undoButton.setAction(undoAction);
        undoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/undo.png")));
        undoButton.setText("");
        undoButton.setToolTipText("Undo");

        redoButton.setAction(redoAction);
        redoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/redo.png")));
        redoButton.setText("");
        redoButton.setToolTipText("Redo");

        findButton.setAction(findReplaceAction);
        findButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/find.png")));
        findButton.setText("");
        findButton.setToolTipText("Find");

        replaceButton.setAction(findReplaceAction);
        replaceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/replace.png")));
        replaceButton.setText("");
        replaceButton.setToolTipText("Replace");

        clearFormattingButton.setAction(new ClearFormattingAction(this, "Remove Format"));
        clearFormattingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/removeformat.png")));
        clearFormattingButton.setText("");
        clearFormattingButton.setToolTipText("Remove Format");

        boldButton.setAction(new HTMLEditorKit.BoldAction());
        boldButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/bold.png")));
        boldButton.setText("");
        boldButton.setToolTipText("Bold");

        italicButton.setAction(new HTMLEditorKit.ItalicAction());
        italicButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/italic.png")));
        italicButton.setText("");
        italicButton.setToolTipText("Italic");

        underlineButton.setAction(new HTMLEditorKit.UnderlineAction());
        underlineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/underline.png")));
        underlineButton.setText("");
        underlineButton.setToolTipText("Underline");

        strikethroughButton.setAction(new StrikeThroughAction());
        strikethroughButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/strikethrough.png")));
        strikethroughButton.setText("");
        strikethroughButton.setToolTipText("Strike Through");

        subscriptButton.setAction(new SubscriptAction());
        subscriptButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/subscript.png")));
        subscriptButton.setText("");
        subscriptButton.setToolTipText("Subscript");

        superscriptButton.setAction(new SuperscriptAction());
        superscriptButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/superscript.png")));
        superscriptButton.setText("");
        superscriptButton.setToolTipText("Superscript");

        //TODO: change increase and decrease indent to add inner <li> when inside bulleted or numbered list
        increaseIndentButton.setAction(new IncreaseIndentAction("Increase Indent", this));
        increaseIndentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/incindent.png")));
        increaseIndentButton.setText("");
        increaseIndentButton.setToolTipText("Increase Indent");

        decreaseIndentButton.setAction(new DecreaseIndentAction("Decrease Indent", this));
        decreaseIndentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/decindent.png")));
        decreaseIndentButton.setText("");
        decreaseIndentButton.setToolTipText("Decrease Indent");

        blockQuoteButton.setAction(new FormatAction(this, "Block Quote", Tag.BLOCKQUOTE));
        blockQuoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/blockquote.png")));
        blockQuoteButton.setText("");
        blockQuoteButton.setToolTipText("Block Quote");

        leftJustifyButton.setAction(new HTMLEditorKit.AlignmentAction("Left Align",StyleConstants.ALIGN_LEFT));
        leftJustifyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/leftjustify.png")));
        leftJustifyButton.setText("");
        leftJustifyButton.setToolTipText("Left Justify");

        centerJustifyButton.setAction(new HTMLEditorKit.AlignmentAction("Center Align",StyleConstants.ALIGN_CENTER));
        centerJustifyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/centerjustify.png")));
        centerJustifyButton.setText("");
        centerJustifyButton.setToolTipText("Center Justify");

        rightJustifyButton.setAction(new HTMLEditorKit.AlignmentAction("Left Align",StyleConstants.ALIGN_RIGHT));
        rightJustifyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/rightjustify.png")));
        rightJustifyButton.setText("");
        rightJustifyButton.setToolTipText("Right Justify");

        blockJustifyButton.setAction(new HTMLEditorKit.AlignmentAction("Justified Align",StyleConstants.ALIGN_JUSTIFIED));
        blockJustifyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/blockjustify.png")));
        blockJustifyButton.setText("");
        blockJustifyButton.setToolTipText("Block Justify");

        unlinkButton.setAction(new UnlinkAction(this, "Unlink"));
        unlinkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/unlink.png")));
        unlinkButton.setText("");
        unlinkButton.setToolTipText("Unlink");

        //TODO: horizontal rule - doesn't insert correctly if within anything other than P, ie. TD or H1
        insertHorizontalLineButton.setAction(new HTMLEditorKit.InsertHTMLTextAction("Insert Horizontal Line", "<hr/>", Tag.P, Tag.HR, Tag.BODY, Tag.HR));
        insertHorizontalLineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/horizontalline.png")));
        insertHorizontalLineButton.setText("");
        insertHorizontalLineButton.setToolTipText("Insert Horizontal Line");

        paragraphFormatComboBox.setRenderer(new ParagraphFormatListCellRenderer());
        paragraphFormatComboBox.removeAllItems();
        ParagraphFormat[] paragraphFormats = ParagraphFormat.values();
        for (int i=0; i<paragraphFormats.length; i++) {
            paragraphFormatComboBox.addItem(paragraphFormats[i]);
        }

        fontComboBox.setRenderer(new FontListCellRenderer());
        fontComboBox.removeAllItems();
        FontItem[] fontItems = FontItem.values();
        for (int i=0; i<fontItems.length; i++) {
            fontComboBox.addItem(fontItems[i]);
        }

        fontSizeComboBox.setRenderer(new FontSizeListCellRenderer());
        fontSizeComboBox.removeAllItems();
        FontSize[] fontSizes = FontSize.values();
        for (int i=0; i<fontSizes.length; i++) {
            fontSizeComboBox.addItem(fontSizes[i]);
        }

        setToolbarFocusActionListener(this);

        htmlTextPane.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        htmlTextPane.getActionMap().put("Undo", undoAction);

        htmlTextPane.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
        htmlTextPane.getActionMap().put("Redo", redoAction);

        htmlTextPane.getInputMap().put(KeyStroke.getKeyStroke("control F"), "Find");
        htmlTextPane.getActionMap().put("Find", findReplaceAction);

        htmlTextPane.getInputMap().put(KeyStroke.getKeyStroke("control R"), "Replace");
        htmlTextPane.getActionMap().put("Replace", findReplaceAction);

        contextMenu = new JPopupMenu();
        JMenuItem cutMenuItem = new JMenuItem();
        cutMenuItem.setAction(cutAction);
        cutMenuItem.setText("Cut");
        cutMenuItem.setMnemonic('C');
        cutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/cut.png")));
        JMenuItem copyMenuItem = new JMenuItem();
        copyMenuItem.setAction(copyAction);
        copyMenuItem.setText("Copy");
        copyMenuItem.setMnemonic('o');
        copyMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/copy.png")));
        JMenuItem pasteMenuItem = new JMenuItem();
        pasteMenuItem.setAction(pasteAction);
        pasteMenuItem.setText("Paste");
        pasteMenuItem.setMnemonic('P');
        pasteMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/paste.png")));
        contextMenu.add(cutMenuItem);
        contextMenu.add(copyMenuItem);
        contextMenu.add(pasteMenuItem);

        htmlTextPane.addMouseMotionListener(new DefaultEditorMouseMotionListener());
        htmlTextPane.setEditorKit(editorKit);

	startNewDocument();

        initSpellChecker();
    }

    // The following two methods allow us to find an
    // action provided by the editor kit by its name.
    private void createEditorKitActionTable() {
        editorKitActions = new Hashtable<Object, Action>();
        Action[] actionsArray = editorKit.getActions();
        for (int i = 0; i < actionsArray.length; i++) {
            Action a = actionsArray[i];
            editorKitActions.put(a.getValue(Action.NAME), a);
        }
    }

    private Action getEditorKitActionByName(String name) {
        return editorKitActions.get(name);
    }

    protected void resetUndoManager() {
        undo.discardAllEdits();
        undoAction.update();
        redoAction.update();
    }

    public void startNewDocument(){
            Document oldDoc = htmlTextPane.getDocument();
            if(oldDoc != null)
                    oldDoc.removeUndoableEditListener(undoHandler);
            htmlDocument = (HTMLDocument)editorKit.createDefaultDocument();
            htmlTextPane.setDocument(htmlDocument);
            htmlTextPane.getDocument().addUndoableEditListener(undoHandler);
            resetUndoManager();
            //TODO: check if necessary
            htmlDocument.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
            htmlDocument.setPreservesUnknownTags(false);            
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        htmlDocument = new javax.swing.text.html.HTMLDocument();
        toolbarPanel = new javax.swing.JPanel();
        editPanel = new javax.swing.JPanel();
        cutButton = new javax.swing.JButton();
        copyButton = new javax.swing.JButton();
        pasteAsTextButton = new javax.swing.JButton();
        pasteButton = new javax.swing.JButton();
        pagePanel = new javax.swing.JPanel();
        saveButton = new javax.swing.JButton();
        newButton = new javax.swing.JButton();
        previewButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        toolsPanel = new javax.swing.JPanel();
        printButton = new javax.swing.JButton();
        spellcheckButton = new javax.swing.JButton();
        undoRedoPanel = new javax.swing.JPanel();
        undoButton = new javax.swing.JButton();
        redoButton = new javax.swing.JButton();
        searchPanel = new javax.swing.JPanel();
        findButton = new javax.swing.JButton();
        replaceButton = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        selectAllButton = new javax.swing.JButton();
        clearFormattingButton = new javax.swing.JButton();
        textEffectPanel = new javax.swing.JPanel();
        boldButton = new javax.swing.JButton();
        italicButton = new javax.swing.JButton();
        strikethroughButton = new javax.swing.JButton();
        underlineButton = new javax.swing.JButton();
        subSuperScriptPanel = new javax.swing.JPanel();
        subscriptButton = new javax.swing.JButton();
        superscriptButton = new javax.swing.JButton();
        listPanel = new javax.swing.JPanel();
        insertRemoveNumberedListButton = new javax.swing.JButton();
        insertRemoveBulletedListButton = new javax.swing.JButton();
        blockPanel = new javax.swing.JPanel();
        decreaseIndentButton = new javax.swing.JButton();
        increaseIndentButton = new javax.swing.JButton();
        createDivButton = new javax.swing.JButton();
        blockQuoteButton = new javax.swing.JButton();
        createParagraphButton = new javax.swing.JButton();
        justificationPanel = new javax.swing.JPanel();
        leftJustifyButton = new javax.swing.JButton();
        centerJustifyButton = new javax.swing.JButton();
        blockJustifyButton = new javax.swing.JButton();
        rightJustifyButton = new javax.swing.JButton();
        miscPanel = new javax.swing.JPanel();
        insertTableButton = new javax.swing.JButton();
        insertHorizontalLineButton = new javax.swing.JButton();
        insertSpecialCharButton = new javax.swing.JButton();
        insertImage = new javax.swing.JButton();
        paragraphFormatComboBox = new javax.swing.JComboBox();
        fontComboBox = new javax.swing.JComboBox();
        fontSizeComboBox = new javax.swing.JComboBox();
        colorPanel = new javax.swing.JPanel();
        textColorButton = new javax.swing.JButton();
        backgroundColorButton = new javax.swing.JButton();
        aboutPanel = new javax.swing.JPanel();
        aboutButton = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        sourceButton = new javax.swing.JButton();
        linkPanel = new javax.swing.JPanel();
        linkButton = new javax.swing.JButton();
        unlinkButton = new javax.swing.JButton();
        anchorButton = new javax.swing.JButton();
        mainScrollPane = new javax.swing.JScrollPane();
        htmlTextPane = new javax.swing.JTextPane();

        editPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        cutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/cut.png"))); // NOI18N
        cutButton.setToolTipText("Cut");

        copyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/copy.png"))); // NOI18N
        copyButton.setToolTipText("Copy");

        pasteAsTextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/paste_as_text.png"))); // NOI18N
        pasteAsTextButton.setToolTipText("Paste as plain text");
        pasteAsTextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteAsTextButtonActionPerformed(evt);
            }
        });

        pasteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/paste.png"))); // NOI18N
        pasteButton.setToolTipText("Paste");

        javax.swing.GroupLayout editPanelLayout = new javax.swing.GroupLayout(editPanel);
        editPanel.setLayout(editPanelLayout);
        editPanelLayout.setHorizontalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editPanelLayout.createSequentialGroup()
                .addComponent(cutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(copyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pasteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pasteAsTextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        editPanelLayout.setVerticalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editPanelLayout.createSequentialGroup()
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(copyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pasteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pasteAsTextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pagePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/save.png"))); // NOI18N
        saveButton.setToolTipText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        newButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/newpage.png"))); // NOI18N
        newButton.setToolTipText("New");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        previewButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/preview.png"))); // NOI18N
        previewButton.setToolTipText("Preview");
        previewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewButtonActionPerformed(evt);
            }
        });

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/open.png"))); // NOI18N
        openButton.setToolTipText("Open");
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pagePanelLayout = new javax.swing.GroupLayout(pagePanel);
        pagePanel.setLayout(pagePanelLayout);
        pagePanelLayout.setHorizontalGroup(
            pagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pagePanelLayout.createSequentialGroup()
                .addComponent(openButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pagePanelLayout.setVerticalGroup(
            pagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pagePanelLayout.createSequentialGroup()
                .addGroup(pagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(openButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(previewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        toolsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        printButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/print.png"))); // NOI18N
        printButton.setToolTipText("Print");
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        spellcheckButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/spellcheck.png"))); // NOI18N
        spellcheckButton.setToolTipText("Check Spelling");
        spellcheckButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spellcheckButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout toolsPanelLayout = new javax.swing.GroupLayout(toolsPanel);
        toolsPanel.setLayout(toolsPanelLayout);
        toolsPanelLayout.setHorizontalGroup(
            toolsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolsPanelLayout.createSequentialGroup()
                .addComponent(printButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spellcheckButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        toolsPanelLayout.setVerticalGroup(
            toolsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(printButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(spellcheckButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        undoRedoPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        undoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/undo.png"))); // NOI18N
        undoButton.setToolTipText("Undo");

        redoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/redo.png"))); // NOI18N
        redoButton.setToolTipText("Redo");

        javax.swing.GroupLayout undoRedoPanelLayout = new javax.swing.GroupLayout(undoRedoPanel);
        undoRedoPanel.setLayout(undoRedoPanelLayout);
        undoRedoPanelLayout.setHorizontalGroup(
            undoRedoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(undoRedoPanelLayout.createSequentialGroup()
                .addComponent(undoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(redoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        undoRedoPanelLayout.setVerticalGroup(
            undoRedoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(undoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(redoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        searchPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        findButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/find.png"))); // NOI18N
        findButton.setToolTipText("Find");

        replaceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/replace.png"))); // NOI18N
        replaceButton.setToolTipText("Replace");

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addComponent(findButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(replaceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(findButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(replaceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        selectAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/selectall.png"))); // NOI18N
        selectAllButton.setToolTipText("Select All");
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });

        clearFormattingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/removeformat.png"))); // NOI18N
        clearFormattingButton.setToolTipText("Remove Format");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(selectAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearFormattingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(selectAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(clearFormattingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        textEffectPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        textEffectPanel.setPreferredSize(new java.awt.Dimension(122, 29));

        boldButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/bold.png"))); // NOI18N
        boldButton.setToolTipText("Bold");

        italicButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/italic.png"))); // NOI18N
        italicButton.setToolTipText("Italic");

        strikethroughButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/strikethrough.png"))); // NOI18N
        strikethroughButton.setToolTipText("Strike Through");

        underlineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/underline.png"))); // NOI18N
        underlineButton.setToolTipText("Underline");

        javax.swing.GroupLayout textEffectPanelLayout = new javax.swing.GroupLayout(textEffectPanel);
        textEffectPanel.setLayout(textEffectPanelLayout);
        textEffectPanelLayout.setHorizontalGroup(
            textEffectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textEffectPanelLayout.createSequentialGroup()
                .addComponent(boldButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(italicButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(underlineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(strikethroughButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        textEffectPanelLayout.setVerticalGroup(
            textEffectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textEffectPanelLayout.createSequentialGroup()
                .addGroup(textEffectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(boldButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(italicButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(underlineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(strikethroughButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        subSuperScriptPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        subscriptButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/subscript.png"))); // NOI18N
        subscriptButton.setToolTipText("Subscript");

        superscriptButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/superscript.png"))); // NOI18N
        superscriptButton.setToolTipText("Superscript");

        javax.swing.GroupLayout subSuperScriptPanelLayout = new javax.swing.GroupLayout(subSuperScriptPanel);
        subSuperScriptPanel.setLayout(subSuperScriptPanelLayout);
        subSuperScriptPanelLayout.setHorizontalGroup(
            subSuperScriptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(subSuperScriptPanelLayout.createSequentialGroup()
                .addComponent(subscriptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(superscriptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        subSuperScriptPanelLayout.setVerticalGroup(
            subSuperScriptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(subSuperScriptPanelLayout.createSequentialGroup()
                .addGroup(subSuperScriptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(subscriptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(superscriptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(1, Short.MAX_VALUE))
        );

        listPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        insertRemoveNumberedListButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/numberlist.png"))); // NOI18N
        insertRemoveNumberedListButton.setToolTipText("Insert/Remove Numbered List");
        insertRemoveNumberedListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertRemoveNumberedListButtonActionPerformed(evt);
            }
        });

        insertRemoveBulletedListButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/bulletlist.png"))); // NOI18N
        insertRemoveBulletedListButton.setToolTipText("Insert/Remove Bulleted List");
        insertRemoveBulletedListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertRemoveBulletedListButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout listPanelLayout = new javax.swing.GroupLayout(listPanel);
        listPanel.setLayout(listPanelLayout);
        listPanelLayout.setHorizontalGroup(
            listPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(listPanelLayout.createSequentialGroup()
                .addComponent(insertRemoveNumberedListButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(insertRemoveBulletedListButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        listPanelLayout.setVerticalGroup(
            listPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(insertRemoveNumberedListButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(insertRemoveBulletedListButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        blockPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        blockPanel.setPreferredSize(new java.awt.Dimension(122, 29));

        decreaseIndentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/decindent.png"))); // NOI18N
        decreaseIndentButton.setToolTipText("Decrease Indent");

        increaseIndentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/incindent.png"))); // NOI18N
        increaseIndentButton.setToolTipText("Increase Indent");

        createDivButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/creatediv.png"))); // NOI18N
        createDivButton.setToolTipText("Create Div Container");
        createDivButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createDivButtonActionPerformed(evt);
            }
        });

        blockQuoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/blockquote.png"))); // NOI18N
        blockQuoteButton.setToolTipText("Block Quote");

        createParagraphButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/createparagraph.png"))); // NOI18N
        createParagraphButton.setToolTipText("Create Paragraph");
        createParagraphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createParagraphButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout blockPanelLayout = new javax.swing.GroupLayout(blockPanel);
        blockPanel.setLayout(blockPanelLayout);
        blockPanelLayout.setHorizontalGroup(
            blockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(blockPanelLayout.createSequentialGroup()
                .addComponent(decreaseIndentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(increaseIndentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(blockQuoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createDivButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createParagraphButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        blockPanelLayout.setVerticalGroup(
            blockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(blockPanelLayout.createSequentialGroup()
                .addGroup(blockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(decreaseIndentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(increaseIndentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blockQuoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createDivButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createParagraphButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        justificationPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        justificationPanel.setPreferredSize(new java.awt.Dimension(122, 29));

        leftJustifyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/leftjustify.png"))); // NOI18N
        leftJustifyButton.setToolTipText("Left Justify");

        centerJustifyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/centerjustify.png"))); // NOI18N
        centerJustifyButton.setToolTipText("Center Justify");

        blockJustifyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/blockjustify.png"))); // NOI18N
        blockJustifyButton.setToolTipText("Block Justify");

        rightJustifyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/rightjustify.png"))); // NOI18N
        rightJustifyButton.setToolTipText("Right Justify");

        javax.swing.GroupLayout justificationPanelLayout = new javax.swing.GroupLayout(justificationPanel);
        justificationPanel.setLayout(justificationPanelLayout);
        justificationPanelLayout.setHorizontalGroup(
            justificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(justificationPanelLayout.createSequentialGroup()
                .addComponent(leftJustifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(centerJustifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rightJustifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(blockJustifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        justificationPanelLayout.setVerticalGroup(
            justificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(justificationPanelLayout.createSequentialGroup()
                .addGroup(justificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(leftJustifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(centerJustifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightJustifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blockJustifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        miscPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        miscPanel.setPreferredSize(new java.awt.Dimension(122, 29));

        insertTableButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/table.png"))); // NOI18N
        insertTableButton.setToolTipText("Table");
        insertTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertTableButtonActionPerformed(evt);
            }
        });

        insertHorizontalLineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/horizontalline.png"))); // NOI18N
        insertHorizontalLineButton.setToolTipText("Insert Horizontal Line");
        insertHorizontalLineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertHorizontalLineButtonActionPerformed(evt);
            }
        });

        insertSpecialCharButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/symbol.png"))); // NOI18N
        insertSpecialCharButton.setToolTipText("Insert Special Character");
        insertSpecialCharButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSpecialCharButtonActionPerformed(evt);
            }
        });

        insertImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/image.png"))); // NOI18N
        insertImage.setToolTipText("Image");
        insertImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertImageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout miscPanelLayout = new javax.swing.GroupLayout(miscPanel);
        miscPanel.setLayout(miscPanelLayout);
        miscPanelLayout.setHorizontalGroup(
            miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(miscPanelLayout.createSequentialGroup()
                .addComponent(insertImage, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(insertTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(insertHorizontalLineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(insertSpecialCharButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        miscPanelLayout.setVerticalGroup(
            miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(miscPanelLayout.createSequentialGroup()
                .addGroup(miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(insertImage, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(insertTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(insertHorizontalLineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(insertSpecialCharButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(1, Short.MAX_VALUE))
        );

        paragraphFormatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Heading 1", "Heading 2", "Heading 3", "Heading 4", "Heading 5", "Heading 6", "Formatted", "Address", "Normal (DIV)" }));
        paragraphFormatComboBox.setToolTipText("Paragraph Format");
        paragraphFormatComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paragraphFormatComboBoxActionPerformed(evt);
            }
        });

        fontComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Arial", "Comic Sans MS", "Courier New", "Georgia", "Lucinda Sans Unicode", "Tahoma", "Times New Roman", "Trebuchet MS", "Verdana" }));
        fontComboBox.setToolTipText("Font");
        fontComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontComboBoxActionPerformed(evt);
            }
        });

        fontSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72" }));
        fontSizeComboBox.setToolTipText("Font Size");
        fontSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeComboBoxActionPerformed(evt);
            }
        });

        colorPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        textColorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/textcolor.png"))); // NOI18N
        textColorButton.setToolTipText("Text Color");
        textColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textColorButtonActionPerformed(evt);
            }
        });

        backgroundColorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/backgroundcolor.png"))); // NOI18N
        backgroundColorButton.setToolTipText("Background Color");
        backgroundColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundColorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout colorPanelLayout = new javax.swing.GroupLayout(colorPanel);
        colorPanel.setLayout(colorPanelLayout);
        colorPanelLayout.setHorizontalGroup(
            colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colorPanelLayout.createSequentialGroup()
                .addComponent(textColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(backgroundColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        colorPanelLayout.setVerticalGroup(
            colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(textColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(backgroundColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        aboutPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        aboutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/about.png"))); // NOI18N
        aboutButton.setToolTipText("About Metaphase Editor");
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout aboutPanelLayout = new javax.swing.GroupLayout(aboutPanel);
        aboutPanel.setLayout(aboutPanelLayout);
        aboutPanelLayout.setHorizontalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(aboutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        aboutPanelLayout.setVerticalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(aboutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel17.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        sourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/source.png"))); // NOI18N
        sourceButton.setText("Source");
        sourceButton.setToolTipText("Source");
        sourceButton.setPreferredSize(new java.awt.Dimension(87, 24));
        sourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addComponent(sourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addComponent(sourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        linkPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        linkPanel.setPreferredSize(new java.awt.Dimension(91, 29));

        linkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/link.png"))); // NOI18N
        linkButton.setToolTipText("Link");
        linkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkButtonActionPerformed(evt);
            }
        });

        unlinkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/unlink.png"))); // NOI18N
        unlinkButton.setToolTipText("Unlink");

        anchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metaphaseeditor/icons/anchor.png"))); // NOI18N
        anchorButton.setToolTipText("Anchor");
        anchorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anchorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout linkPanelLayout = new javax.swing.GroupLayout(linkPanel);
        linkPanel.setLayout(linkPanelLayout);
        linkPanelLayout.setHorizontalGroup(
            linkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(linkPanelLayout.createSequentialGroup()
                .addComponent(linkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unlinkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(anchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        linkPanelLayout.setVerticalGroup(
            linkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(linkPanelLayout.createSequentialGroup()
                .addGroup(linkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(linkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(anchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(unlinkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout toolbarPanelLayout = new javax.swing.GroupLayout(toolbarPanel);
        toolbarPanel.setLayout(toolbarPanelLayout);
        toolbarPanelLayout.setHorizontalGroup(
            toolbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolbarPanelLayout.createSequentialGroup()
                .addGroup(toolbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(toolbarPanelLayout.createSequentialGroup()
                        .addComponent(paragraphFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fontComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fontSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(colorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(aboutPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(toolbarPanelLayout.createSequentialGroup()
                        .addGroup(toolbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, toolbarPanelLayout.createSequentialGroup()
                                .addComponent(textEffectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(subSuperScriptPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(listPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(blockPanel, 0, 153, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, toolbarPanelLayout.createSequentialGroup()
                                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(pagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(editPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(toolsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(10, 10, 10)
                        .addGroup(toolbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(toolbarPanelLayout.createSequentialGroup()
                                .addComponent(undoRedoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toolbarPanelLayout.createSequentialGroup()
                                .addComponent(justificationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(linkPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(miscPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(114, 114, 114))
        );
        toolbarPanelLayout.setVerticalGroup(
            toolbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolbarPanelLayout.createSequentialGroup()
                .addGroup(toolbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pagePanel, 0, 30, Short.MAX_VALUE)
                    .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editPanel, 0, 30, Short.MAX_VALUE)
                    .addComponent(toolsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(undoRedoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(toolbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(blockPanel, 0, 30, Short.MAX_VALUE)
                    .addGroup(toolbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(miscPanel, javax.swing.GroupLayout.Alignment.LEADING, 0, 30, Short.MAX_VALUE)
                        .addComponent(listPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                        .addComponent(subSuperScriptPanel, javax.swing.GroupLayout.Alignment.LEADING, 0, 30, Short.MAX_VALUE)
                        .addComponent(textEffectPanel, javax.swing.GroupLayout.Alignment.LEADING, 0, 30, Short.MAX_VALUE)
                        .addComponent(justificationPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(linkPanel, javax.swing.GroupLayout.Alignment.LEADING, 0, 30, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(toolbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(toolbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(paragraphFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(fontComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(fontSizeComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE))
                    .addComponent(colorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(aboutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        htmlTextPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                htmlTextPaneMouseClicked(evt);
            }
        });
        htmlTextPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                htmlTextPaneKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                htmlTextPaneKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                htmlTextPaneKeyTyped(evt);
            }
        });
        mainScrollPane.setViewportView(htmlTextPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbarPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 904, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        try {
            htmlTextPane.print();
        } catch (PrinterException e) {
            throw new MetaphaseEditorException(e.getMessage(), e);
        }
    }//GEN-LAST:event_printButtonActionPerformed

    public JTextPane getHtmlTextPane() {
        return htmlTextPane;
    }

    public void setEditorToolTipText(String string) {
        htmlTextPane.setToolTipText(string);
    }

    public void addEditorMouseMotionListener(EditorMouseMotionListener editorMouseMotionListener) {
        editorMouseMotionListeners.add(editorMouseMotionListener);
    }

    public void removeEditorMouseMotionListener(EditorMouseMotionListener editorMouseMotionListener) {
        editorMouseMotionListeners.remove(editorMouseMotionListener);
    }

    public AttributeSet getSelectedParagraphAttributes() {
        int start = htmlTextPane.getSelectionStart();        

        Element element = htmlDocument.getParagraphElement(start);
        MutableAttributeSet attributes = new SimpleAttributeSet(element.getAttributes());

        Element charElement = htmlDocument.getCharacterElement(start);
        if (charElement != null) {
            Element impliedParagraph = charElement.getParentElement();
            if (impliedParagraph != null) {
                Element listElement = impliedParagraph.getParentElement();
                if (listElement.getName().equals("li")) {
                    // re-add the existing attributes to the list item
                    AttributeSet listElementAttrs = listElement.getAttributes();
                    Enumeration currentAttrEnum = listElementAttrs.getAttributeNames();
                    while (currentAttrEnum.hasMoreElements()) {
                        Object attrName = currentAttrEnum.nextElement();
                        Object attrValue = listElement.getAttributes().getAttribute(attrName);
                        if ((attrName instanceof String || attrName instanceof HTML.Attribute) && attrValue instanceof String) {
                            attributes.addAttribute(attrName, attrValue);
                        }
                    }
                }
            }
        }

        return attributes;
    }

    public void addAttributesToSelectedParagraph(Map<String, String> attributes) {
        new AddAttributesAction(this, "Add Attributes To Selected Paragraph", attributes).actionPerformed(null);
    }

    public void removeAttributesFromSelectedParagraph(String[] attributeNames) {
        new RemoveAttributesAction(this, "Remove Attributes From Selected Paragraph", attributeNames).actionPerformed(null);
    }

    public String getDocument() {
        return htmlTextPane.getText();
    }

    public void setDocument(String value) {
        try {
            StringReader reader = new StringReader(value);
            Document oldDoc = htmlTextPane.getDocument();
            if(oldDoc != null)
                oldDoc.removeUndoableEditListener(undoHandler);
            htmlDocument = (HTMLDocument)editorKit.createDefaultDocument();
            editorKit.read(reader, htmlDocument, 0);
            htmlDocument.addUndoableEditListener(undoHandler);
            htmlTextPane.setDocument(htmlDocument);
            resetUndoManager();
        } catch (BadLocationException e) {
            throw new MetaphaseEditorException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MetaphaseEditorException(e.getMessage(), e);
        }
    }

    public JPopupMenu getContextMenu() {
        return contextMenu;
    }

    public void addStyleSheetRule(String rule) {
        StyleSheet styleSheet = editorKit.getStyleSheet();
        styleSheet.addRule(rule);
    }

    public void refreshAfterAction() {
        int pos = htmlTextPane.getCaretPosition();
        htmlTextPane.setText(htmlTextPane.getText());
        htmlTextPane.validate();
        try {
            htmlTextPane.setCaretPosition(pos);
        } catch (IllegalArgumentException e) {
            // swallow the exception
            // seems like a bug in the JTextPane component
            // only happens occasionally when pasting text at the end of a document
            System.err.println(e.getMessage());
        }
    }

    private void insertRemoveNumberedListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertRemoveNumberedListButtonActionPerformed
        new HTMLEditorKit.InsertHTMLTextAction("Insert Bulleted List", "<ol><li></li></ol>", Tag.BODY, Tag.OL).actionPerformed(evt);
    }//GEN-LAST:event_insertRemoveNumberedListButtonActionPerformed

    private void textColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textColorButtonActionPerformed
        Color color = JColorChooser.showDialog(null, "Text Color", null);
        if (color != null) {
            new StyledEditorKit.ForegroundAction("Color",color).actionPerformed(evt);
        }
    }//GEN-LAST:event_textColorButtonActionPerformed

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        htmlTextPane.selectAll();
    }//GEN-LAST:event_selectAllButtonActionPerformed

    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
        AboutDialog aboutDialog = new AboutDialog(null, true);
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_aboutButtonActionPerformed

    private void backgroundColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundColorButtonActionPerformed
        Color color = JColorChooser.showDialog(null, "Text Color", null);
        if (color != null) {
            new BackgroundColorAction(color).actionPerformed(evt);
        }
    }//GEN-LAST:event_backgroundColorButtonActionPerformed

    private void sourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceButtonActionPerformed
        if (htmlSourceMode) {
            htmlTextPane.setText(htmlTextArea.getText());
            mainScrollPane.setViewportView(htmlTextPane);
            htmlSourceMode = false;

            setToolbarComponentEnable(this, true);
        } else {
            htmlTextArea.setText(htmlTextPane.getText());
            mainScrollPane.setViewportView(htmlTextArea);
            htmlSourceMode = true;

            setToolbarComponentEnable(this, false);
        }
    }//GEN-LAST:event_sourceButtonActionPerformed

    private void insertHorizontalLineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertHorizontalLineButtonActionPerformed
        
    }//GEN-LAST:event_insertHorizontalLineButtonActionPerformed

    private void insertSpecialCharButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSpecialCharButtonActionPerformed
        String specialChars = specialCharacterDialog.showDialog();
        if (specialChars != null) {
            new InsertTextAction(this, "Insert Special Character", specialChars).actionPerformed(evt);
        }
    }//GEN-LAST:event_insertSpecialCharButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        try {
            File current = new File(".");
            JFileChooser chooser = new JFileChooser(current);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new HTMLFileFilter());
            for (;;) {
                int approval = chooser.showSaveDialog(this);
                if (approval == JFileChooser.APPROVE_OPTION){
                    File newFile = chooser.getSelectedFile();
                    if (newFile.exists()){
                        String message = newFile.getAbsolutePath()
                            + " already exists. \n"
                            + "Do you want to replace it?";
                        int option = JOptionPane.showConfirmDialog(this, message, "Save", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            File currentFile = newFile;
                            FileWriter fw = new FileWriter(currentFile);
                            fw.write(htmlTextPane.getText());
                            fw.close();
                            break;
                        } else if (option == JOptionPane.NO_OPTION) {
                            continue;
                        } else if (option == JOptionPane.CANCEL_OPTION) {
                            break;
                        }
                    } else {
                        File currentFile = new File(newFile.getAbsolutePath());
                        FileWriter fw = new FileWriter(currentFile);
                        fw.write(htmlTextPane.getText());
                        fw.close();
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (FileNotFoundException e){
            throw new MetaphaseEditorException(e.getMessage(), e);
        } catch(IOException e){
            throw new MetaphaseEditorException(e.getMessage(), e);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to erase all the current content and start a new document?", "New", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            startNewDocument();

            if (htmlSourceMode) {
                htmlTextArea.setText(htmlTextPane.getText());
            }
        }
    }//GEN-LAST:event_newButtonActionPerformed

    private void paragraphFormatComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paragraphFormatComboBoxActionPerformed
        ParagraphFormat paragraphFormat = (ParagraphFormat)paragraphFormatComboBox.getSelectedItem();
        if (paragraphFormat != null && paragraphFormat.getTag() != null) {
            new FormatAction(this, "Paragraph Format", paragraphFormat.getTag()).actionPerformed(evt);
        }
        if (paragraphFormatComboBox.getItemCount() > 0) {
            paragraphFormatComboBox.setSelectedIndex(0);
        }
    }//GEN-LAST:event_paragraphFormatComboBoxActionPerformed

    private void fontComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontComboBoxActionPerformed
        FontItem fontItem = (FontItem)fontComboBox.getSelectedItem();
        if (fontItem != null && fontItem.getFontName() != null) {
            new HTMLEditorKit.FontFamilyAction(fontItem.getText(), fontItem.getFontName()).actionPerformed(evt);
        }
        if (fontComboBox.getItemCount() > 0) {
            fontComboBox.setSelectedIndex(0);
        }
    }//GEN-LAST:event_fontComboBoxActionPerformed

    private void fontSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeComboBoxActionPerformed
        FontSize fontSize = (FontSize)fontSizeComboBox.getSelectedItem();
        if (fontSize != null && fontSize.getSize() != -1) {
            new HTMLEditorKit.FontSizeAction(fontSize.getText(), fontSize.getSize()).actionPerformed(evt);
        }
        if (fontSizeComboBox.getItemCount() > 0) {
            fontSizeComboBox.setSelectedIndex(0);
        }
    }//GEN-LAST:event_fontSizeComboBoxActionPerformed

    private void previewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewButtonActionPerformed
        try {
            if (htmlSourceMode) {
                htmlTextPane.setText(htmlTextArea.getText());
            }
            File tempFile = File.createTempFile("metaphaseeditorpreview",".html");
            tempFile.deleteOnExit();
            FileWriter fw = new FileWriter(tempFile);
            fw.write(htmlTextPane.getText());
            fw.close();            

            Desktop.getDesktop().browse(tempFile.toURI());
        } catch (IOException e) {
            throw new MetaphaseEditorException(e.getMessage(), e);
        }
    }//GEN-LAST:event_previewButtonActionPerformed

    private void insertTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertTableButtonActionPerformed
        TableDialog tableDialog = new TableDialog(null, true);
        String tableHtml = tableDialog.showDialog();
        if (tableHtml != null) {
            try {
                editorKit.insertHTML(htmlDocument, htmlTextPane.getCaretPosition(), tableHtml, 0, 0, Tag.TABLE);
                refreshAfterAction();
            } catch (BadLocationException e) {
                throw new MetaphaseEditorException(e.getMessage(), e);
            } catch (IOException e) {
                throw new MetaphaseEditorException(e.getMessage(), e);
            }
        }
    }//GEN-LAST:event_insertTableButtonActionPerformed

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
       try{
            File current = new File(".");
            JFileChooser chooser = new JFileChooser(current);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new HTMLFileFilter());
            int approval = chooser.showOpenDialog(this);
            if (approval == JFileChooser.APPROVE_OPTION){
                File currentFile = chooser.getSelectedFile();
                if (currentFile.exists()) {
                    FileReader fr = new FileReader(currentFile);
                    Document oldDoc = htmlTextPane.getDocument();
                    if(oldDoc != null)
                        oldDoc.removeUndoableEditListener(undoHandler);
                    htmlDocument = (HTMLDocument)editorKit.createDefaultDocument();
                    htmlDocument.putProperty("IgnoreCharsetDirective", new Boolean(true));
                    editorKit.read(fr, htmlDocument, 0);
                    htmlDocument.addUndoableEditListener(undoHandler);
                    htmlTextPane.setDocument(htmlDocument);
                    resetUndoManager();
                } else {
                    JOptionPane.showMessageDialog(null, "The selected file does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch(BadLocationException e){
            throw new MetaphaseEditorException(e.getMessage(), e);
        } catch(FileNotFoundException e){
            throw new MetaphaseEditorException(e.getMessage(), e);
        } catch(IOException e){
            throw new MetaphaseEditorException(e.getMessage(), e);
        }
    }//GEN-LAST:event_openButtonActionPerformed

    private void pasteAsTextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteAsTextButtonActionPerformed
	Clipboard clipboard = getToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(null);
        if(transferable !=null) {
            try {
                String plainText = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                plainText = plainText.replaceAll("\\r\\n", "<br/>");
                plainText = plainText.replaceAll("\\n", "<br/>");
                plainText = plainText.replaceAll("\\r", "<br/>");
                new InsertHtmlAction(this, "Paste as Text", "<p>" + plainText + "</p>", Tag.P).actionPerformed(null);
            } catch (UnsupportedFlavorException e) {
                throw new MetaphaseEditorException(e.getMessage(), e);
            } catch (IOException e) {
                throw new MetaphaseEditorException(e.getMessage(), e);
            }
        }
    }//GEN-LAST:event_pasteAsTextButtonActionPerformed

    private void htmlTextPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_htmlTextPaneMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {
            for (int i=0; i<contextMenuListeners.size(); i++) {
                contextMenuListeners.get(i).beforeContextMenuPopup();
            }
            contextMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_htmlTextPaneMouseClicked

    private void anchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anchorButtonActionPerformed
        AnchorDialog anchorDialog = new AnchorDialog(null, true);
        String anchorHtml = anchorDialog.showDialog();
        if (anchorHtml != null) {
            new InsertHtmlAction(this, "Anchor", anchorHtml, Tag.A).actionPerformed(evt);
        }
    }//GEN-LAST:event_anchorButtonActionPerformed

    private void insertImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertImageActionPerformed
        try {
            ImageDialog imageDialog = new ImageDialog(null, true);
            String html = imageDialog.showDialog();
            if (html != null) {
                if (imageDialog.isLink()) {
                    editorKit.insertHTML(htmlDocument, htmlTextPane.getCaretPosition(), html, 0, 0, Tag.A);
                } else {
                    editorKit.insertHTML(htmlDocument, htmlTextPane.getCaretPosition(), html, 0, 0, Tag.IMG);
                }
                refreshAfterAction();
            }
        } catch (BadLocationException e) {
            throw new MetaphaseEditorException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MetaphaseEditorException(e.getMessage(), e);
        }
    }//GEN-LAST:event_insertImageActionPerformed

    private void insertRemoveBulletedListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertRemoveBulletedListButtonActionPerformed
        new HTMLEditorKit.InsertHTMLTextAction("Insert Bulleted List", "<ul><li></li></ul>", Tag.BODY, Tag.UL).actionPerformed(evt);
    }//GEN-LAST:event_insertRemoveBulletedListButtonActionPerformed

    private void htmlTextPaneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_htmlTextPaneKeyPressed
    }//GEN-LAST:event_htmlTextPaneKeyPressed

    private void htmlTextPaneKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_htmlTextPaneKeyReleased
        
    }//GEN-LAST:event_htmlTextPaneKeyReleased

    private void htmlTextPaneKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_htmlTextPaneKeyTyped
        if (evt.getKeyChar() == 10) {
            // TODO: currently this inserts two list items, fix this. PS it's not because of the two actions below, they will only insert when encountering either a UL or OL
            new HTMLEditorKit.InsertHTMLTextAction("Insert List Item", "<li></li>", Tag.UL, Tag.LI).actionPerformed(null);
            new HTMLEditorKit.InsertHTMLTextAction("Insert List Item", "<li></li>", Tag.OL, Tag.LI).actionPerformed(null);
        }
    }//GEN-LAST:event_htmlTextPaneKeyTyped

    private void createDivButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createDivButtonActionPerformed
        DivDialog divDialog = new DivDialog(null, true);
        String divHtml = divDialog.showDialog();
        if (divHtml != null) {
            try {
                editorKit.insertHTML(htmlDocument, htmlTextPane.getCaretPosition(), divHtml, 0, 0, Tag.DIV);
                refreshAfterAction();
            } catch (BadLocationException e) {
                throw new MetaphaseEditorException(e.getMessage(), e);
            } catch (IOException e) {
                throw new MetaphaseEditorException(e.getMessage(), e);
            }
        }
    }//GEN-LAST:event_createDivButtonActionPerformed

    private void linkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkButtonActionPerformed
        LinkDialog linkDialog = new LinkDialog(null, true);
        String html = linkDialog.showDialog();
        if (html != null) {
            new InsertHtmlAction(this, "Anchor", html, Tag.A).actionPerformed(evt);            
        }
    }//GEN-LAST:event_linkButtonActionPerformed

    private void spellcheckButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spellcheckButtonActionPerformed
        //JOptionPane.showMessageDialog(null, "The spelling checker functionality is currently unavailable.");        
        Thread thread = new Thread() {
            public void run() {
                try {
                    JOptionPane.showMessageDialog(null, "The spelling check is complete.", "Check Spelling", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    throw new MetaphaseEditorException(ex.getMessage(), ex);
                }
            }
        };
        thread.start();
    }//GEN-LAST:event_spellcheckButtonActionPerformed

    private void createParagraphButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createParagraphButtonActionPerformed
        new InsertHtmlAction(this, "Paragraph", "<p>TODO: modify paragraph contents</p>", Tag.P).actionPerformed(evt);
    }//GEN-LAST:event_createParagraphButtonActionPerformed

    private void setToolbarFocusActionListener(JComponent component) {
        Component[] vComponents = component.getComponents();
        for (int i=0; i<vComponents.length; i++) {
            if (vComponents[i] instanceof JButton) {
                JButton button = (JButton)vComponents[i];
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        htmlTextPane.requestFocus();
                    }
                });
            } else if (vComponents[i] instanceof JComboBox) {
                JComboBox comboBox = (JComboBox)vComponents[i];
                comboBox.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        htmlTextPane.requestFocus();
                    }
                });
            } else if (vComponents[i] instanceof JPanel) {
                JPanel panel = (JPanel)vComponents[i];
                setToolbarFocusActionListener(panel);
            }
        }
    }

    private void setToolbarComponentEnable(JComponent component, boolean enabled) {
        Component[] vComponents = component.getComponents();
        for (int i=0; i<vComponents.length; i++) {
            if (vComponents[i] == sourceButton || vComponents[i] == newButton || vComponents[i] == previewButton || vComponents[i] == aboutButton) {
                return;
            } else if (vComponents[i] instanceof JButton) {
                JButton button = (JButton)vComponents[i];
                button.setEnabled(enabled);
            } else if (vComponents[i] instanceof JComboBox) {
                JComboBox comboBox = (JComboBox)vComponents[i];
                comboBox.setEnabled(enabled);
            } else if (vComponents[i] instanceof JPanel) {
                JPanel panel = (JPanel)vComponents[i];
                setToolbarComponentEnable(panel, enabled);
            }
        }
    }

    public void addContextMenuListener(ContextMenuListener contextMenuListener) {
        contextMenuListeners.add(contextMenuListener);
    }

    public void removeContextMenuListener(ContextMenuListener contextMenuListener) {
        contextMenuListeners.remove(contextMenuListener);
    }

    public void initSpellChecker() {
        try {
            ZipInputStream zipInputStream = null;
            InputStream inputStream = null;
            if (spellCheckDictionaryVersion == SpellCheckDictionaryVersion.CUSTOM) {
                if (customDictionaryFilename == null) {
                    throw new MetaphaseEditorException("The dictionary version has been set to CUSTOM but no custom dictionary file name has been specified.");
                }
                inputStream = new FileInputStream(customDictionaryFilename);
            } else {
                inputStream = this.getClass().getResourceAsStream(spellCheckDictionaryVersion.getFilename());
            }
            zipInputStream = new ZipInputStream(inputStream);
            zipInputStream.getNextEntry();
        } catch (FileNotFoundException e) {
            throw new MetaphaseEditorException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MetaphaseEditorException(e.getMessage(), e);
        }
    }

    public void setCustomDictionaryFilename(String customDictionaryFilename) {
        this.customDictionaryFilename = customDictionaryFilename;
    }

    public String getCustomDictionaryFilename() {
        return customDictionaryFilename;
    }

    public void setDictionaryVersion(SpellCheckDictionaryVersion spellCheckDictionaryVersion) {
        this.spellCheckDictionaryVersion = spellCheckDictionaryVersion;

        initSpellChecker();
    }

    public SpellCheckDictionaryVersion getDictionaryVersion() {
        return spellCheckDictionaryVersion;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutButton;
    private javax.swing.JPanel aboutPanel;
    private javax.swing.JButton anchorButton;
    private javax.swing.JButton backgroundColorButton;
    private javax.swing.JButton blockJustifyButton;
    private javax.swing.JPanel blockPanel;
    private javax.swing.JButton blockQuoteButton;
    private javax.swing.JButton boldButton;
    private javax.swing.JButton centerJustifyButton;
    private javax.swing.JButton clearFormattingButton;
    private javax.swing.JPanel colorPanel;
    private javax.swing.JButton copyButton;
    private javax.swing.JButton createDivButton;
    private javax.swing.JButton createParagraphButton;
    private javax.swing.JButton cutButton;
    private javax.swing.JButton decreaseIndentButton;
    private javax.swing.JPanel editPanel;
    private javax.swing.JButton findButton;
    private javax.swing.JComboBox fontComboBox;
    private javax.swing.JComboBox fontSizeComboBox;
    private javax.swing.text.html.HTMLDocument htmlDocument;
    private javax.swing.JTextPane htmlTextPane;
    private javax.swing.JButton increaseIndentButton;
    private javax.swing.JButton insertHorizontalLineButton;
    private javax.swing.JButton insertImage;
    private javax.swing.JButton insertRemoveBulletedListButton;
    private javax.swing.JButton insertRemoveNumberedListButton;
    private javax.swing.JButton insertSpecialCharButton;
    private javax.swing.JButton insertTableButton;
    private javax.swing.JButton italicButton;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel justificationPanel;
    private javax.swing.JButton leftJustifyButton;
    private javax.swing.JButton linkButton;
    private javax.swing.JPanel linkPanel;
    private javax.swing.JPanel listPanel;
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JPanel miscPanel;
    private javax.swing.JButton newButton;
    private javax.swing.JButton openButton;
    private javax.swing.JPanel pagePanel;
    private javax.swing.JComboBox paragraphFormatComboBox;
    private javax.swing.JButton pasteAsTextButton;
    private javax.swing.JButton pasteButton;
    private javax.swing.JButton previewButton;
    private javax.swing.JButton printButton;
    private javax.swing.JButton redoButton;
    private javax.swing.JButton replaceButton;
    private javax.swing.JButton rightJustifyButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JButton sourceButton;
    private javax.swing.JButton spellcheckButton;
    private javax.swing.JButton strikethroughButton;
    private javax.swing.JPanel subSuperScriptPanel;
    private javax.swing.JButton subscriptButton;
    private javax.swing.JButton superscriptButton;
    private javax.swing.JButton textColorButton;
    private javax.swing.JPanel textEffectPanel;
    private javax.swing.JPanel toolbarPanel;
    private javax.swing.JPanel toolsPanel;
    private javax.swing.JButton underlineButton;
    private javax.swing.JButton undoButton;
    private javax.swing.JPanel undoRedoPanel;
    private javax.swing.JButton unlinkButton;
    // End of variables declaration//GEN-END:variables

    class SubscriptAction extends StyledEditorKit.StyledTextAction {
        public SubscriptAction() {
            super(StyleConstants.Subscript.toString());
        }

        public void actionPerformed(ActionEvent ae)
        {
            JEditorPane editor = getEditor(ae);
            if (editor != null) {
                boolean subscript = (StyleConstants.isSubscript(getStyledEditorKit(editor).getInputAttributes())) ? false : true;
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setSubscript(sas, subscript);
                setCharacterAttributes(editor, sas, false);
            }
        }
    }

    class SuperscriptAction extends StyledEditorKit.StyledTextAction
    {
        public SuperscriptAction() {
            super(StyleConstants.Superscript.toString());
        }

        public void actionPerformed(ActionEvent ae) {
            JEditorPane editor = getEditor(ae);
            if (editor != null) {
                StyledEditorKit kit = getStyledEditorKit(editor);
                boolean superscript = (StyleConstants.isSuperscript(kit.getInputAttributes())) ? false : true;
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setSuperscript(sas, superscript);
                setCharacterAttributes(editor, sas, false);
            }
        }
    }

    class StrikeThroughAction extends StyledEditorKit.StyledTextAction
    {
        public StrikeThroughAction() {
            super(StyleConstants.StrikeThrough.toString());
        }

        public void actionPerformed(ActionEvent ae) {
            JEditorPane editor = getEditor(ae);
            if (editor != null) {
                StyledEditorKit kit = getStyledEditorKit(editor);
                boolean strikeThrough = (StyleConstants.isStrikeThrough(kit.getInputAttributes())) ? false : true;
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setStrikeThrough(sas, strikeThrough);
                setCharacterAttributes(editor, sas, false);
            }
        }
    }

    class BackgroundColorAction extends StyledEditorKit.StyledTextAction
    {
        private Color color;
        public BackgroundColorAction(Color color) {
            super(StyleConstants.StrikeThrough.toString());
            this.color = color;
        }

        public void actionPerformed(ActionEvent ae) {
            JEditorPane editor = getEditor(ae);
            if (editor != null) {
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setBackground(sas, color);
                setCharacterAttributes(editor, sas, false);
            }
        }
    }

    
    class UndoHandler implements UndoableEditListener {
        /**
         * Messaged when the Document has created an edit, the edit is
         * added to <code>undo</code>, an instance of UndoManager.
         */
        public void undoableEditHappened(UndoableEditEvent e) {
            undo.addEdit(e.getEdit());
            undoAction.update();
            redoAction.update();
        }
    }

    class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            try {
                undo.undo();
            } catch (CannotUndoException e) {
                throw new MetaphaseEditorException(e.getMessage(), e);
            }
            update();
            redoAction.update();
        }

        protected void update() {
            if(undo.canUndo()) {
                setEnabled(true);
            }else {
                setEnabled(false);
            }
        }
    }

    class RedoAction extends AbstractAction {

        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            try {
                undo.redo();
            } catch (CannotRedoException e) {
                throw new MetaphaseEditorException(e.getMessage(), e);
            }
            update();
            undoAction.update();
        }

        protected void update() {
            if(undo.canRedo()) {
                setEnabled(true);
            }else {
                setEnabled(false);
            }
        }
    }

    class HTMLFileFilter extends javax.swing.filechooser.FileFilter{
        public boolean accept(File f){
            return ((f.isDirectory()) ||(f.getName().toLowerCase().indexOf(".htm") > 0));
        }

        public String getDescription(){
            return "html";
        }
    }

    class ParagraphFormatListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (index == 0) {
                component.setEnabled(false);
            }
            ParagraphFormat paragraphFormat = (ParagraphFormat)value;
            if (paragraphFormat.getTag() != null) {
                JLabel label = (JLabel)component;
                label.setText("<html><" + paragraphFormat.getTag().toString() + ">" + label.getText() + "</" + paragraphFormat.getTag().toString() + ">");
            }
            return component;
        }        
    }

    class FontListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (index == 0) {
                component.setEnabled(false);
            }
            FontItem fontItem = (FontItem)value;
            if (fontItem.getFontName() != null) {
                Font currentFont = component.getFont();
                component.setFont(new Font(fontItem.getFontName(), currentFont.getStyle(), currentFont.getSize()));
            }
            return component;
        }
    }

    class FontSizeListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);            
            if (index == 0) {
                component.setEnabled(false);
            }
            FontSize fontSize = (FontSize)value;
            if (fontSize.getSize() != -1) {
                Font currentFont = component.getFont();
                component.setFont(new Font(currentFont.getName(), currentFont.getStyle(), fontSize.getSize()));
            }
            return component;
        }
    }

    class DefaultEditorMouseMotionListener implements MouseMotionListener {

        public void mouseDragged(MouseEvent me) {
            int pos = htmlTextPane.viewToModel(me.getPoint());

            if (pos >= 0) {
                Element element = htmlDocument.getParagraphElement(pos);
                MutableAttributeSet attributes = new SimpleAttributeSet(element.getAttributes());

                EditorMouseEvent editorMouseEvent = new EditorMouseEvent();
                editorMouseEvent.setNearestParagraphAttributes(attributes);
                for (int i=0; i<editorMouseMotionListeners.size(); i++) {
                    editorMouseMotionListeners.get(i).mouseDragged(editorMouseEvent);
                }
            }
        }

        public void mouseMoved(MouseEvent me) {
            int pos = htmlTextPane.viewToModel(me.getPoint());

            if (pos >= 0) {
                Element element = htmlDocument.getParagraphElement(pos);
                MutableAttributeSet attributes = new SimpleAttributeSet(element.getAttributes());

                EditorMouseEvent editorMouseEvent = new EditorMouseEvent();
                editorMouseEvent.setNearestParagraphAttributes(attributes);
                for (int i=0; i<editorMouseMotionListeners.size(); i++) {
                    editorMouseMotionListeners.get(i).mouseMoved(editorMouseEvent);
                }
            }
        }
    }

	public void openFile(File file) {
		try {
        if (file.exists()) {
            FileReader fr;
				fr = new FileReader(file);
            Document oldDoc = htmlTextPane.getDocument();
            if(oldDoc != null)
                oldDoc.removeUndoableEditListener(undoHandler);
            htmlDocument = (HTMLDocument)editorKit.createDefaultDocument();
            htmlDocument.putProperty("IgnoreCharsetDirective", new Boolean(true));
            editorKit.read(fr, htmlDocument, 0);
            htmlDocument.addUndoableEditListener(undoHandler);
            htmlTextPane.setDocument(htmlDocument);
            
            htmlTextPane.setPage(file.toURI().toURL());
            htmlDocument = (HTMLDocument) htmlTextPane.getDocument();
            resetUndoManager();
        } else {
            JOptionPane.showMessageDialog(null, "The selected file does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void save(File newFile) {
        try {
        	if (htmlSourceMode) {
	            htmlTextPane.setText(htmlTextArea.getText());
	            mainScrollPane.setViewportView(htmlTextPane);
	            htmlSourceMode = false;
	            setToolbarComponentEnable(this, true);
        	}
                File currentFile = newFile;
                FileWriter fw = new FileWriter(currentFile);
                fw.write(htmlTextPane.getText());
                fw.close();
        } catch (FileNotFoundException e){
            throw new MetaphaseEditorException(e.getMessage(), e);
        } catch(IOException e){
            throw new MetaphaseEditorException(e.getMessage(), e);
        }
	}
}
