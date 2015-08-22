package notepadextended;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

// Base operations inspired from http://codereview.stackexchange.com/questions/51175/simple-text-editor-class
// and http://forum.codecall.net/topic/49721-simple-text-editor/
// Actions learned from Oracle's java.awt.Action example.

public class Javapad{

    public static void main(String[] args) {
        new Javapad();
    }

    private JMenuBar menuBar;
    private JMenu file, edit, format;
    private JMenuItem fileNew, fileOpen, fileSave, fileSaveAs, fileExit;
    private JMenuItem editCopy, editPaste, editCut, editSelectAll, editUndo, editRedo;
    private JMenuItem formatFont, formatFontSize;
    private JFrame window;
    private JTextArea textArea;
    private JScrollPane scrollPane;
    private Border textBorder; // none so far
    private Font textFont;
    private JToolBar toolBar; // needs to be implemented
    private boolean opened;
    private boolean saved;
    private boolean changed;
    private File openedFile;
    private UndoManager uManager;

    public Javapad() {
        createTextArea();
        createUndoManager();
        createWindow();
    }

    public JFrame createWindow() {
        window = new JFrame("New File");
        window.setVisible(true);
        window.setJMenuBar(createMenuBar());
        window.add(scrollPane, BorderLayout.CENTER);
        window.pack();
        window.setLocationRelativeTo(null);
        return window;
    }

    public JMenuBar createMenuBar() {
        menuBar = new JMenuBar();
        menuBar.add(createFile());
        menuBar.add(createEdit());
        menuBar.add(createFormat());

        return menuBar;
    }

    public JTextArea createTextArea() {
        textArea = new JTextArea(30, 50);
        textArea.setEditable(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(textBorder,
                BorderFactory.createEmptyBorder(2, 5, 0, 0)));

        textFont = new Font("Lucida Console", 0, 12);
        textArea.setFont(textFont);

        scrollPane = new JScrollPane(textArea);

        return textArea;
    }

    private UndoManager createUndoManager() {
        uManager = new UndoManager() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                super.undoableEditHappened(e);
                editUndo.setEnabled(true);
                if (!changed) {
                    window.setTitle("*" + window.getTitle());
                    changed = true;
                }
            }
        };
        textArea.getDocument().addUndoableEditListener(uManager);
        return uManager;
    }

    public JMenu createFile() {
        file = new JMenu("File");
        
        fileNew = new JMenuItem(new ActionNew());
        fileNew.setIcon(null);
        
        fileOpen = new JMenuItem(new ActionOpen());
        fileOpen.setIcon(null);
        
        fileSave = new JMenuItem(new ActionSave());
        fileSave.setIcon(null);
        
        fileSaveAs = new JMenuItem(new ActionSaveAs());
        fileSaveAs.setIcon(null);
        
        fileExit = new JMenuItem(new ActionExit());
        fileExit.setIcon(null);
        
        file.add(fileNew);
        file.add(fileOpen);
        file.add(fileSave);
        file.add(fileSaveAs);
        file.add(fileExit);
        return file;
    }

    public JMenu createEdit() {
        edit = new JMenu("Edit");
        
        editUndo = new JMenuItem(new ActionUndo());
        editUndo.setIcon(null);    
        editUndo.setEnabled(false);
        
        editRedo = new JMenuItem(new ActionRedo());
        editRedo.setIcon(null);
        editRedo.setEnabled(false);
        
        editSelectAll = new JMenuItem(new ActionSelectAll());
        editSelectAll.setIcon(null);
        
        ActionMap m = textArea.getActionMap();
        editCopy = new JMenuItem(m.get(DefaultEditorKit.cutAction));
        editCut = new JMenuItem(m.get(DefaultEditorKit.copyAction));
        editPaste = new JMenuItem(m.get(DefaultEditorKit.pasteAction));
        
        edit.add(editUndo);
        edit.add(editRedo);
        edit.add(editSelectAll);
        edit.add(editCopy);
        edit.add(editCut);
        edit.add(editPaste);
        return edit;
    }
    
    public JMenu createFormat() {
        format = new JMenu("Format");
        
        formatFont = new JMenuItem(new ActionFont());
        formatFont.setIcon(null);
        
        formatFontSize = new JMenuItem(new ActionFontSize());
        formatFontSize.setIcon(null);
        
        format.add(formatFont);
        format.add(formatFontSize);
        
        return format;
    }

    private void saveFile(File filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(textArea.getText());
            writer.close();
            saved = true;
            changed = false;
            window.setTitle(filename.getName());
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    private void quickSave(File filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(textArea.getText());
            writer.close();
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    private void openingFiles(File filename) {
        try {
            openedFile = filename;
            FileReader reader = new FileReader(filename);
            textArea.read(reader, null);
            opened = true;
            window.setTitle(filename.getName());

        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    public class ActionNew extends AbstractAction {
        public ActionNew() {
            super("New");
        }

        public void actionPerformed(ActionEvent e) {
            new Javapad();
        }
    }
    
    public class ActionOpen extends AbstractAction {
        public ActionOpen() {
            super("Open");
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser openChooser = new JFileChooser();
            openChooser.showOpenDialog(null);
            File file = openChooser.getSelectedFile();
            openingFiles(file);
        }
    }
    
    public class ActionSave extends AbstractAction {
        public ActionSave() {
            super("Save");
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser saveChooser = new JFileChooser();
            File filename = saveChooser.getSelectedFile();
            if (opened == false && saved == false) {
                saveChooser.showSaveDialog(null);
                int confirmationResult;
                if (filename.exists()) {
                    confirmationResult = JOptionPane.showConfirmDialog(
                            fileSave, "Replace existing file?");
                    if (confirmationResult == JOptionPane.YES_OPTION) {
                        saveFile(filename);
                    }
                } else {
                    saveFile(filename);
                }
            } else {
                quickSave(openedFile);
            }
        }
    }
    
    public class ActionSaveAs extends AbstractAction {
        public ActionSaveAs() {
            super("SaveAs");
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser saveAs = new JFileChooser();
            saveAs.showSaveDialog(null);
            File filename = saveAs.getSelectedFile();
            int confirmationResult;
            if (filename.exists()) {
                confirmationResult = JOptionPane.showConfirmDialog(fileSaveAs,
                        "Replace existing file?");
                if (confirmationResult == JOptionPane.YES_OPTION) {
                    saveFile(filename);
                }
            } else {
                saveFile(filename);
            }
        }
    }
    
    public class ActionExit extends AbstractAction {
        public ActionExit() {
            super("Exit");
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
    
    public class ActionUndo extends AbstractAction {
        public ActionUndo() {
            super("Undo");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                uManager.undo();
                if (!uManager.canUndo()) {
                    editUndo.setEnabled(false);
                }
                editRedo.setEnabled(true);
            } catch (CannotUndoException cu) {
                cu.printStackTrace();
            }
        }
    }
    
    public class ActionRedo extends AbstractAction {
        public ActionRedo() {
            super("Redo");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                uManager.redo();
                if (!uManager.canRedo()) {
                    editRedo.setEnabled(false);
                }
                editUndo.setEnabled(true);
            } catch (CannotUndoException cur) {
                cur.printStackTrace();
            }
        }
    }
    
    public class ActionSelectAll extends AbstractAction {
        public ActionSelectAll() {
            super("Select All");
        }

        public void actionPerformed(ActionEvent e) {
            textArea.selectAll();
        }
    }
    
    
    
    public class ActionFont extends AbstractAction {
        public ActionFont() {
            super("Font");
        }

        public void actionPerformed(ActionEvent e) {
            String changedFont = JOptionPane.showInputDialog("Input a font");
            changeFont(changedFont);
        }
    }
    
    public class ActionFontSize extends AbstractAction {
        public ActionFontSize() {
            super("Font Size");
        }

        public void actionPerformed(ActionEvent e) {
            String changedFontSize = JOptionPane.showInputDialog("Input a font size");
            try {
                changeFontSize(Integer.parseInt(changedFontSize));  
            } catch (NumberFormatException n) {
                JOptionPane.showMessageDialog(window, "Not a valid font size");
                n.printStackTrace();
            }
        }
    }
    
    public JTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(JTextArea text) {
        textArea = text;
    }
    
    public void changeFont(String f) {
        textFont = new Font("f", 0, 12);
        textArea.setFont(textFont);
    }
    
    public void changeFontSize(int size) {
        textFont = new Font(textFont.getFontName(), 0, size);
        textArea.setFont(textFont);
    }
}
