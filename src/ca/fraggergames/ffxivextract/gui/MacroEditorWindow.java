package ca.fraggergames.ffxivextract.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.BoxLayout;
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
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;

import ca.fraggergames.ffxivextract.models.Macro_File;

public class MacroEditorWindow extends JFrame {

	// FILE IO
	private File lastOpenedFile = null;
	private Macro_File currentMacro_File;
	
	private JPanel contentPane;	

	// GUI
	private JTextField txtDatPath;
	private JComboBox drpMacroChooser;
	private JComboBox drpIconChooser;
	private JTextField txtMacroName;
	private JTextArea txtMacroBody;
	private JLabel txtLineCounter;
	
	private JLabel lblMacroChooserLabel;
	private JLabel lblIconChooserLabel;
	private JLabel lblMacroNameLabel;
	
	private JButton btnSave, btnBrowse;
	
	/**
	 * Create the frame.
	 */
	public MacroEditorWindow() {
		
		this.setTitle("Macro Editor (EXPERIMENTAL)");
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
				
		setBounds(100, 100, 600, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel_4 = new JPanel();
		contentPane.add(panel_4, BorderLayout.NORTH);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.Y_AXIS));
		
		JPanel pnlFileSelect = new JPanel();
		panel_4.add(pnlFileSelect);
		pnlFileSelect.setBorder(BorderFactory
						.createTitledBorder("Macro File"));
		GridBagLayout gbl_pnlFileSelect = new GridBagLayout();
		gbl_pnlFileSelect.columnWidths = new int[]{91, 200, 67, 0};
		gbl_pnlFileSelect.rowHeights = new int[]{23, 0};
		gbl_pnlFileSelect.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlFileSelect.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlFileSelect.setLayout(gbl_pnlFileSelect);
		
		JLabel label = new JLabel("Path to macro file: ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 0, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		pnlFileSelect.add(label, gbc_label);
		
		txtDatPath = new JTextField();
		txtDatPath.setText("Point to your MACRO.DAT file");
		txtDatPath.setPreferredSize(new Dimension(200, 20));
		txtDatPath.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.weightx = 1.0;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		pnlFileSelect.add(txtDatPath, gbc_textField);
		
		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setPath();
			}
		});
		
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.anchor = GridBagConstraints.NORTHWEST;
		gbc_button.gridx = 2;
		gbc_button.gridy = 0;
		pnlFileSelect.add(btnBrowse, gbc_button);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(null, "Macro Editor", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(panel_5, BorderLayout.CENTER);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3 = new JPanel();
		panel_5.add(panel_3, BorderLayout.NORTH);
		FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		
		JPanel panel = new JPanel();
		panel_3.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		lblMacroChooserLabel = new JLabel("Macro: ");
		panel_1.add(lblMacroChooserLabel);
		lblMacroChooserLabel.setEnabled(false);
		
		drpMacroChooser = new JComboBox();
		panel_1.add(drpMacroChooser);
		drpMacroChooser.setEnabled(false);
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
		
		lblIconChooserLabel = new JLabel("Icon:    ");
		panel_2.add(lblIconChooserLabel);
		lblIconChooserLabel.setEnabled(false);
		
		drpIconChooser = new JComboBox();
		panel_2.add(drpIconChooser);
		drpIconChooser.setEnabled(false);
		
		JPanel panel_6 = new JPanel();
		panel_5.add(panel_6, BorderLayout.CENTER);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_7 = new JPanel();
		panel_6.add(panel_7, BorderLayout.NORTH);
		panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.Y_AXIS));
		
		lblMacroNameLabel = new JLabel("Macro Name:");
		panel_7.add(lblMacroNameLabel);
		
		txtMacroName = new JTextField();		
		txtMacroName.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_7.add(txtMacroName);
		
		JPanel panel_8 = new JPanel();
		panel_6.add(panel_8, BorderLayout.CENTER);
		panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel = new JLabel("Macro Body:");
		panel_8.add(lblNewLabel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_8.add(scrollPane);
		
		txtMacroBody = new JTextArea();
		scrollPane.setViewportView(txtMacroBody);
		txtMacroBody.setRows(15);
		txtMacroBody.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel panel_9 = new JPanel();
		panel_9.setBorder(new EmptyBorder(0, 0, 0, 2));
		FlowLayout flowLayout_1 = (FlowLayout) panel_9.getLayout();
		flowLayout_1.setAlignment(FlowLayout.RIGHT);
		contentPane.add(panel_9, BorderLayout.SOUTH);
		
		btnSave = new JButton("Save");
		panel_9.add(btnSave);
		
		DefaultStyledDocument doc = new DefaultStyledDocument();
		doc.setDocumentFilter(new DocumentSizeFilter(txtMacroBody, Macro_File.MAX_LINES, Macro_File.MAX_LENGTH));
		txtMacroBody.setDocument(doc);
		
		JPanel panel_10 = new JPanel();
		panel_6.add(panel_10, BorderLayout.SOUTH);
		panel_10.setLayout(new BorderLayout(0, 0));
		
		txtLineCounter = new JLabel("-/15");
		panel_10.add(txtLineCounter, BorderLayout.EAST);
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
		lblMacroChooserLabel.setEnabled(isEnabled);
		lblIconChooserLabel.setEnabled(isEnabled);
		lblMacroNameLabel.setEnabled(isEnabled);
		
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
	                             String text, AttributeSet a)
	        throws BadLocationException {
	    
	    	int actRow = area.getLineOfOffset(offs);
	    	int rowBeginn = area.getLineStartOffset(actRow);
	    	int rowEnd = area.getLineEndOffset(actRow);	      	
	    	
	    	//Count newlines
	    	int newlineCount = text.length() - text.replace("\n", "").length();
	    	
	    	if (text.contains("\n") && newlineCount + area.getLineCount() > maxLines)
	    		return;
	    	
	    	super.insertString(fb, offs, text, a);	
   	
	    	txtLineCounter.setText(area.getLineCount() + "/" + maxLines);
	    }    
		 @Override
		public void replace(FilterBypass fb, int offset, int length,
				String text, AttributeSet attrs) throws BadLocationException {		
			 	int actRow = area.getLineOfOffset(offset);
		    	int rowBegin = area.getLineStartOffset(actRow);
		    	int rowEnd = area.getLineEndOffset(actRow);
		      	
		    	//Count newlines
		    	int newlineCount = text.length() - text.replace("\n", "").length();
		    	
		    	if (text.contains("\n") && newlineCount + area.getLineCount() > maxLines)
		    		return;
		    	
		    	if (area.getLineCount() < maxLines && text.equals("\n"))
		    	{
		    		super.replace(fb, offset, length, text, attrs);		    		
		    	}	    	
		    	
		    	try {
					if ((area.getText(rowBegin, rowEnd-rowBegin)+ text).getBytes("UTF-8").length <= 0xB5 && !text.equals("\n")) {
						super.replace(fb, offset, length, text, attrs);	
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	    	
		    	txtLineCounter.setText(area.getLineCount() + "/" + maxLines);
		}
		 
		 @Override
		public void remove(FilterBypass fb, int offset, int length)
				throws BadLocationException {			
			super.remove(fb, offset, length);
			txtLineCounter.setText(area.getLineCount() + "/" + maxLines);
		}
	}
	
}
