package ca.fraggergames.ffxivextract.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;

import ca.fraggergames.ffxivextract.models.Macro_File;

@SuppressWarnings("serial")
public class MacroEditorWindow extends JFrame {

	// FILE IO
	File lastOpenedFile = null;
	Macro_File currentMacro_File;
	
	// UI
	JPanel pnlMainDatFile, pnlMacroEditor;
	JLabel txtDatLabel;
	JTextField txtDatPath;
	JButton btnBrowse;	
	
	JLabel txtMacroChooserLabel = new JLabel("Macro: ");
	JLabel txtIconChooserLabel = new JLabel("Icon: ");
	JLabel txtMacroNameLabel = new JLabel("Name: ");
	
	JButton btnImport = new JButton("Import");
	JButton btnExport = new JButton("Export");
	JButton btnSave = new JButton("Save");
	
	JComboBox drpMacroChooser;
	JComboBox drpIconChooser;
	JTextField txtMacroName;
	JTextArea txtMacroBody;

	public MacroEditorWindow() {
		this.setTitle("Macro Editor (EXPERIMENTAL)");
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());

		// PATH SETUP
		pnlMainDatFile = new JPanel();
		pnlMainDatFile.setBorder(BorderFactory
				.createTitledBorder("Macro File"));
		txtDatLabel = new JLabel("Path to macro file: ");
		txtDatPath = new JTextField();
		txtDatPath.setEditable(false);
		txtDatPath.setText("Point to your MACRO.DAT file");
		txtDatPath.setPreferredSize(new Dimension(200, txtDatPath
				.getPreferredSize().height));

		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setPath();
			}
		});

		pnlMainDatFile.add(txtDatLabel);
		pnlMainDatFile.add(txtDatPath);
		pnlMainDatFile.add(btnBrowse);		

		// EDITOR
		pnlMacroEditor = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		pnlMacroEditor.setBorder(BorderFactory
				.createTitledBorder("Macro Editor"));	
		
		drpMacroChooser = new JComboBox();
		drpIconChooser = new JComboBox();
		txtMacroName = new JTextField();
		txtMacroBody = new JTextArea(Macro_File.MAX_LINES, Macro_File.MAX_LENGTH);
		JScrollPane scroll = new JScrollPane(txtMacroBody);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		DefaultStyledDocument doc = new DefaultStyledDocument();
		doc.setDocumentFilter(new DocumentSizeFilter(txtMacroBody, Macro_File.MAX_LINES, Macro_File.MAX_LENGTH));
		txtMacroBody.setDocument(doc);
		
		
		JPanel pnlDrp1 = new JPanel();
		pnlDrp1.add(txtMacroChooserLabel);
		pnlDrp1.add(drpMacroChooser);
		JPanel pnlDrp2 = new JPanel();
		pnlDrp2.add(txtIconChooserLabel);
		pnlDrp2.add(drpIconChooser);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		pnlMacroEditor.add(pnlDrp1,gbc);
		gbc.gridx = 0;		
		gbc.gridy = 1;
		pnlMacroEditor.add(pnlDrp2, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 5, 0);
				
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;		
		pnlMacroEditor.add(txtMacroName,gbc);
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.weighty =1;
		pnlMacroEditor.add(scroll,gbc);
		
		// BUTTONS
		JPanel pnlButtons = new JPanel();
		pnlButtons.add(btnSave);
		pnlButtons.add(btnImport);
		pnlButtons.add(btnExport);
		
		// ROOT
		gbc = new GridBagConstraints();
		JPanel pnlRoot = new JPanel(new GridBagLayout());	
		pnlRoot.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.weightx = 1;
		gbc.weighty = 0;
		pnlRoot.add(pnlMainDatFile,gbc);
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		pnlRoot.add(pnlMacroEditor,gbc);
		gbc.gridy = 2;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.SOUTH;
		pnlRoot.add(pnlButtons,gbc);
	
		getContentPane().add(pnlRoot);
		setSize(500, 600);
		
		setEditorEnabled(false);
	}

	public void setPath() {
		JFileChooser fileChooser = new JFileChooser(lastOpenedFile);

		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		FileFilter filter = new FileFilter() {

			@Override
			public String getDescription() {
				return "FFXIV Macro File (MACRO.DAT)";
			}

			@Override
			public boolean accept(File f) {
				return f.getName().equals("MACRO.DAT")
						|| f.isDirectory();
			}
		};
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);

		int retunval = fileChooser.showOpenDialog(MacroEditorWindow.this);

		if (retunval == JFileChooser.APPROVE_OPTION) {
			try {
				txtDatPath.setText(fileChooser.getSelectedFile()
						.getCanonicalPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				loadFile(fileChooser.getSelectedFile());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void loadFile(File file) throws FileNotFoundException, IOException {
		currentMacro_File = new Macro_File(file.getCanonicalPath());
		setEditorEnabled(true);
		
		//Setup Drop Downs
		for (int i = 0; i < Macro_File.MAX_MACROS; i++)
			drpMacroChooser.addItem(""+i);
		
		drpMacroChooser.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				int state = itemEvent.getStateChange();
				if (state == ItemEvent.SELECTED)
				{
					txtMacroName.setText(currentMacro_File.entries[drpMacroChooser.getSelectedIndex()].title);
					txtMacroBody.setText("");
					
					//Precheck
					int end = 0;
					for (int i = 0; i <currentMacro_File.entries[drpMacroChooser.getSelectedIndex()].lines.length; i++)
					{
						if (!currentMacro_File.entries[drpMacroChooser.getSelectedIndex()].lines[i].isEmpty())
							end = i;
					}
					
					for (int i = 0; i <= end; i++)
					{
						txtMacroBody.append(currentMacro_File.entries[drpMacroChooser.getSelectedIndex()].lines[i] + (i+1 >= currentMacro_File.entries[drpMacroChooser.getSelectedIndex()].lines.length ? "" : "\n"));
						System.out.println(currentMacro_File.entries[drpMacroChooser.getSelectedIndex()].lines[i]);
					}
				}				
			}
		});
		txtMacroName.setText(currentMacro_File.entries[drpMacroChooser.getSelectedIndex()].title);
		txtMacroBody.setText("");
		for (int i = 0; i <currentMacro_File.entries[drpMacroChooser.getSelectedIndex()].lines.length; i++)
			txtMacroBody.append(currentMacro_File.entries[drpMacroChooser.getSelectedIndex()].lines[i] + (i+1 >= currentMacro_File.entries[drpMacroChooser.getSelectedIndex()].lines.length ? "" : "\n"));
	}

	private void setEditorEnabled(boolean isEnabled) {
		txtMacroChooserLabel.setEnabled(isEnabled);
		txtIconChooserLabel.setEnabled(isEnabled);
		txtMacroNameLabel.setEnabled(isEnabled);
		
		btnImport.setEnabled(isEnabled);
		btnExport.setEnabled(isEnabled);
		btnSave.setEnabled(isEnabled);
		
		drpMacroChooser.setEnabled(isEnabled);
		drpIconChooser.setEnabled(isEnabled);
		txtMacroName.setEnabled(isEnabled);
		txtMacroBody.setEnabled(isEnabled);
	}

	public class DocumentSizeFilter extends DocumentFilter {
	    
	    JTextArea area;
	    boolean DEBUG = false;
	    int maxLines, maxLength;
	    String EOL = "\n";
	    
	    public DocumentSizeFilter(JTextArea area, int maxLines, int maxLength) {
	    	this.area = area;
	        this.maxLines = maxLines;
	        this.maxLength = maxLength;
	    }
	 
	    public void insertString(FilterBypass fb, int offs,
	                             String str, AttributeSet a)
	        throws BadLocationException {
	    	int actRow = area.getLineOfOffset(offs);
	    	int rowBeginn = area.getLineStartOffset(actRow);
	    	int rowEnd = area.getLineEndOffset(actRow);
	      	int referenceValue = 0;
	      	
	    	if (str.length() > 1) {
	    		referenceValue = (rowEnd + str.length()) - rowBeginn;
	    	} else {
	    		referenceValue = rowEnd - rowBeginn;
	    	}
	    	
	    	//if (referenceValue < maxLines) {
	    		super.insertString(fb, offs, str, a);	
	    //	}    	
	    	
	    }    
	 @Override
	public void replace(FilterBypass fb, int offset, int length,
			String text, AttributeSet attrs) throws BadLocationException {
		 int actRow = area.getLineOfOffset(offset);
	    	int rowBeginn = area.getLineStartOffset(actRow);
	    	int rowEnd = area.getLineEndOffset(actRow);
	      	int referenceValue = 0;
	    	if (text.length() > 1) {
	    		referenceValue = (rowEnd + text.length()) - rowBeginn;
	    	} else {
	    		referenceValue = rowEnd - rowBeginn;
	    	}	 
	    	
	    	if (area.getLineCount() < maxLines && text.equals("\n"))
	    	{
	    		super.replace(fb, offset, length, text, attrs);		    		
	    	}	    	
	    	
	    	try {
				if (area.getText(rowBeginn, rowEnd).getBytes("UTF-8").length <= 0xB5 && !text.equals("\n")) {
					super.replace(fb, offset, length, text, attrs);	
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	    	
		
	}
	}
}
