package notepadextended;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
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
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

//base operations inspired fromhttp://codereview.stackexchange.com/questions/51175/simple-text-editor-classand
//http://forum.codecall.net/topic/49721-simple-text-editor/

public class SynWrite implements ActionListener{

	public static void main (String[] args) {
		new SynWrite();
	}
	
	private JMenuBar menuBar;
	private JMenu file, edit;
	private JMenuItem fileNew, fileOpen, fileSave, fileSaveAs, fileExit;
	private JMenuItem editCopy, editPaste, editCut, editSelectAll, editUndo,
	editRedo;
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
	
	public SynWrite() {
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
		fileNew = new JMenuItem("New");
		fileNew.addActionListener(this);
		fileOpen = new JMenuItem("Open");
		fileOpen.addActionListener(this);
		fileSave = new JMenuItem("Save");
		fileSave.addActionListener(this);
		fileSaveAs = new JMenuItem("SaveAs");
		fileSaveAs.addActionListener(this);
		fileExit = new JMenuItem("Exit");
		fileExit.addActionListener(this);
		file.add(fileNew);
		file.add(fileOpen);
		file.add(fileSave);
		file.add(fileSaveAs);
		file.add(fileExit);
		return file;
	}
	
	public JMenu createEdit() {
		edit = new JMenu("Edit");
		editUndo = new JMenuItem("Undo");
		editUndo.addActionListener(this);
		editUndo.setEnabled(false);
		editRedo = new JMenuItem("Redo");
		editRedo.addActionListener(this);
		editUndo.setEnabled(false);
		editSelectAll = new JMenuItem("Select All");
		editSelectAll.addActionListener(this);
		editCopy = new JMenuItem("Copy");
		editCopy.addActionListener(this);
		editCut = new JMenuItem("Cut");
		editCut.addActionListener(this);
		editPaste = new JMenuItem("Paste");
		editPaste.addActionListener(this);
		edit.add(editUndo);
		edit.add(editRedo);
		edit.add(editSelectAll);
		edit.add(editCopy);
		edit.add(editCut);
		edit.add(editPaste);
		return edit;
	}
	
	private void saveFile(File filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new
					FileWriter(filename));
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
			BufferedWriter writer = new BufferedWriter(new
					FileWriter(filename));
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

	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == fileNew) {
			new SynWrite();
		} else if(event.getSource() == fileOpen) {
			JFileChooser openChooser = new JFileChooser();
			openChooser.showOpenDialog(null);
			File file = openChooser.getSelectedFile();
			openingFiles(file);
		} else if(event.getSource() == fileSave) {
			JFileChooser saveChooser = new JFileChooser();
			File filename = saveChooser.getSelectedFile();
			if(opened == false && saved == false) {
				saveChooser.showSaveDialog(null);
				int confirmationResult;
				if(filename.exists()) {
					confirmationResult =
							JOptionPane.showConfirmDialog(fileSave, "Replace existing file?");
					if(confirmationResult == JOptionPane.YES_OPTION) {
						saveFile(filename);
					}
				} else {
					saveFile(filename);
				}
			} else {
				quickSave(openedFile);
			}
		} else if(event.getSource() == fileSaveAs) {
			JFileChooser saveAs = new JFileChooser();
			saveAs.showSaveDialog(null);
			File filename = saveAs.getSelectedFile();
			int confirmationResult;
			if(filename.exists()) {
				confirmationResult =
						JOptionPane.showConfirmDialog(fileSaveAs, "Replace existing file?");
				if(confirmationResult == JOptionPane.YES_OPTION) {
					saveFile(filename);
				}
			} else {
				saveFile(filename);
			}
		} else if(event.getSource() == fileExit) {
			System.exit(0);
		} else if(event.getSource() == editUndo) {
			try {
				uManager.undo();
				if (!uManager.canUndo()) {
					editUndo.setEnabled(false);
				}
			} catch(CannotUndoException cu) {
				cu.printStackTrace();
			}
		} else if(event.getSource() == editRedo) {
			try {
				uManager.redo();
			} catch(CannotUndoException cur) {
				cur.printStackTrace();
			}
		} else if(event.getSource() == editSelectAll) {
			textArea.selectAll();
		}  else if(event.getSource() == editCopy) {
			textArea.copy();
		} else if(event.getSource() == editPaste) {
			textArea.paste();
		} else if(event.getSource() == editCut) {
			textArea.cut();
		}
	}

	public void setTextArea(JTextArea text) {
		textArea = text;
	}
}
