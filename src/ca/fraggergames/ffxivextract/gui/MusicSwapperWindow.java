package ca.fraggergames.ffxivextract.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.JList;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.AbstractListModel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;

import javax.swing.ListSelectionModel;

import ca.fraggergames.ffxivextract.Strings;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_DataSegment;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;

public class MusicSwapperWindow extends JFrame {

	//FILE I/O
	private File lastOpenedFile;
	File backup = null;
	File edittingIndexFile = null;
	SqPack_IndexFile editMusicFile, originalMusicFile;
	SqPack_File[] editedFiles;
	
	//GUI
	private JPanel panel_2;
	private JPanel pnlSwapper;
	private JPanel contentPane;
	private JTextField txtDatPath;
	private JLabel lblBackup;
	private JButton btnBackup, btnRestore;
	private JLabel txtSetTo;
	private JLabel lblOriginal, lblSetId;
	private JList<String> lstOriginal = new JList<String>();
	private JList<String> lstSet = new JList<String>();
	private JButton btnSwap, btnRevert;
	
	public MusicSwapperWindow() {
		this.setTitle(Strings.DIALOG_TITLE_MUSICSWAPPER);
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, Strings.MUSICSWAPPER_FRAMETITLE_ARCHIVE, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		txtDatPath = new JTextField();
		txtDatPath.setEditable(false);
		txtDatPath.setText(Strings.MUSICSWAPPER_DEFAULTPATHTEXT);
		panel_1.add(txtDatPath, BorderLayout.CENTER);
		
		JButton btnBrowse = new JButton(Strings.BUTTONNAMES_BROWSE);
		panel_1.add(btnBrowse, BorderLayout.EAST);
		
		panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Backup", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		lblBackup = new JLabel("No backup found");
		panel_2.add(lblBackup, BorderLayout.CENTER);
		lblBackup.setHorizontalAlignment(SwingConstants.LEFT);
		
		JPanel panel_3 = new JPanel();
		panel_3.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_2.add(panel_3, BorderLayout.EAST);
		panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		btnBackup = new JButton("Backup");
		panel_3.add(btnBackup);
		
		btnRestore = new JButton("Restore");
		btnBackup.setEnabled(false);
		btnRestore.setEnabled(false);
		panel_2.setEnabled(false);
		lblBackup.setEnabled(false);
		panel_3.add(btnRestore);
		
		pnlSwapper = new JPanel();
		pnlSwapper.setBorder(new TitledBorder(null, Strings.MUSICSWAPPER_FRAMETITLE_SWAPPING, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(pnlSwapper);
		pnlSwapper.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_4 = new JPanel();
		pnlSwapper.add(panel_4, BorderLayout.CENTER);
		panel_4.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new EmptyBorder(0, 0, 0, 3));
		panel_4.add(panel_6);
		panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.Y_AXIS));
		
		lblOriginal = new JLabel(Strings.MUSICSWAPPER_ORIGINALID);
		panel_6.add(lblOriginal);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_6.add(scrollPane);
			
		lstOriginal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstOriginal.setModel(new DefaultListModel<String>());
		lstOriginal.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent event) {				
				
				if (lstOriginal.getSelectedIndex() == -1)
				{
					lstSet.clearSelection();
					return;
				}
				
				if (event.getValueIsAdjusting() ||lstSet.getModel().getSize() == 0)
					return;				
				txtSetTo.setText(String.format(Strings.MUSICSWAPPER_CURRENTOFFSET, editedFiles[lstOriginal.getSelectedIndex()].getOffset() & 0xFFFFFFFF));
				if (editedFiles[lstOriginal.getSelectedIndex()].getOffset() != originalMusicFile.getPackFolders()[0].getFiles()[lstOriginal.getSelectedIndex()].dataoffset)
					txtSetTo.setForeground(Color.RED);
				else
					txtSetTo.setForeground(Color.decode("#006400"));
			
			}
		});		
		
		scrollPane.setViewportView(lstOriginal);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new EmptyBorder(0, 3, 0, 0));
		panel_4.add(panel_5);
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.Y_AXIS));
		
		lblSetId = new JLabel(Strings.MUSICSWAPPER_TOID);
		panel_5.add(lblSetId);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_5.add(scrollPane_1);
		
		lstSet.setModel(new DefaultListModel<String>());
		lstSet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane_1.setViewportView(lstSet);
		
		JPanel panel_8 = new JPanel();
		panel_8.setBorder(new EmptyBorder(5, 0, 0, 0));
		pnlSwapper.add(panel_8, BorderLayout.SOUTH);
		panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.Y_AXIS));
		
		txtSetTo = new JLabel(Strings.MUSICSWAPPER_CURRENTSETTO);
		panel_8.add(txtSetTo);
		
		JPanel panel_9 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_9.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel_9.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_8.add(panel_9);
		
		btnSwap = new JButton(Strings.BUTTONNAMES_SET);
		btnRevert = new JButton(Strings.BUTTONNAMES_REVERT);
		btnSwap.setHorizontalAlignment(SwingConstants.LEFT);
		panel_9.add(btnSwap);
		
		panel_9.add(btnRevert);
		
		//SETUP
		
		btnBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setPath();
			}
		});
		
		btnSwap.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				swapMusic(lstOriginal.getSelectedIndex(),
						lstSet.getSelectedIndex());
			}
		});
		btnRevert.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				swapMusic(lstOriginal.getSelectedIndex(),
						lstOriginal.getSelectedIndex());
			}
		});
		
		btnBackup.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					createBackup();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		btnRestore.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					restoreFromBackup();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
				
		pack();
		setSwapperEnabled(false);
		
		JOptionPane.showMessageDialog(this,
				Strings.MSG_MUSICSWAPPER,
			    Strings.MSG_MUSICSWAPPER_TITLE,
			    JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void setPath() {
		JFileChooser fileChooser = new JFileChooser(lastOpenedFile);

		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		FileFilter filter = new FileFilter() {

			@Override
			public String getDescription() {
				return Strings.FILETYPE_FFXIV_MUSICINDEX;
			}

			@Override
			public boolean accept(File f) {
				return f.getName().equals("0c0000.win32.index")
						|| f.isDirectory();
			}
		};
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);

		int retunval = fileChooser.showOpenDialog(MusicSwapperWindow.this);

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

	public void loadFile(File file) throws Exception {
		setSwapperEnabled(true);
		SqPack_File[] originalFiles;

		((DefaultListModel<String>)lstOriginal.getModel()).clear();
		((DefaultListModel<String>)lstSet.getModel()).clear();
		
		panel_2.setEnabled(true);
		lblBackup.setEnabled(true);
		
		// Check if we got a backup
		backup = new File(file.getParentFile().getAbsoluteFile(),
				"0c0000.win32.index.bak");
		if (backup.exists()) {
			System.out.println("Backup found, checking file.");
			// Should hash here, but for now just check file counts
			editMusicFile = new SqPack_IndexFile(file.getCanonicalPath());
			originalMusicFile = new SqPack_IndexFile(backup.getCanonicalPath());
			originalFiles = originalMusicFile.getPackFolders()[0].getFiles();
			editedFiles = editMusicFile.getPackFolders()[0].getFiles();

			// Throw and remake backup
			if (originalFiles.length != editedFiles.length) {
				System.out
						.println("File mismatch, there was an update... remaking backup");
				backup.delete();
				copyFile(file, backup);
				originalMusicFile = new SqPack_IndexFile(
						backup.getCanonicalPath());
				originalFiles = originalMusicFile.getPackFolders()[0]
						.getFiles();
			}
			System.out.println("File is good.");
			lblBackup.setText("Backup exists. Remember to restore before patching.");
			btnBackup.setEnabled(false);
			btnRestore.setEnabled(true);
		} else {
			// Create backup
			copyFile(file, backup);
			editMusicFile = new SqPack_IndexFile(file.getCanonicalPath());
			originalMusicFile = new SqPack_IndexFile(backup.getCanonicalPath());
			originalFiles = originalMusicFile.getPackFolders()[0].getFiles();
			editedFiles = editMusicFile.getPackFolders()[0].getFiles();
			lblBackup.setText("Backup was auto generated. Remember to restore before patching.");
			btnBackup.setEnabled(false);
			btnRestore.setEnabled(true);
		}

		// We are good, load up the index
		loadDropDown(lstOriginal, originalFiles, 0);
		loadDropDown(lstSet, originalFiles, 0);

		edittingIndexFile = file;

		//Init this since the list listener doesn't fire
		txtSetTo.setText(String.format(Strings.MUSICSWAPPER_CURRENTOFFSET, editedFiles[lstOriginal.getSelectedIndex()].getOffset() & 0xFFFFFFFF));
		if (editedFiles[lstOriginal.getSelectedIndex()].getOffset() != originalMusicFile.getPackFolders()[0].getFiles()[lstOriginal.getSelectedIndex()].dataoffset)
			txtSetTo.setForeground(Color.RED);
		else
			txtSetTo.setForeground(Color.decode("#006400"));
	}

	private void loadDropDown(JList<String> list, SqPack_File[] files,
			int selectedSpot) {
		DefaultListModel<String> listModel = (DefaultListModel<String>)list.getModel();
		
		for (int i = 0; i < files.length; i++)
		{
			String fileName = files[i].getName();
			
			if (fileName !=null)
				listModel.addElement(String.format("%s (%08X)", fileName, files[i].getOffset() & 0xFFFFFFFF));
			else
				listModel.addElement(String.format("%08X (%08X)", files[i].id & 0xFFFFFFFF, files[i].getOffset() & 0xFFFFFFFF));
		}
			
		list.setSelectedIndex(0);
	}

	private void createBackup() throws IOException
	{
		// Create backup
		System.out.println("Creating backup.");
		copyFile(edittingIndexFile, backup);
		editMusicFile = new SqPack_IndexFile(edittingIndexFile.getCanonicalPath());
		originalMusicFile = new SqPack_IndexFile(backup.getCanonicalPath());
		SqPack_File originalFiles[] = originalMusicFile.getPackFolders()[0].getFiles();
		editedFiles = editMusicFile.getPackFolders()[0].getFiles();
		
		loadDropDown(lstOriginal, originalFiles, 0);
		loadDropDown(lstSet, originalFiles, 0);
		
		lblBackup.setText("Backup exists. Remember to restore before patching.");
		btnBackup.setEnabled(false);
		btnRestore.setEnabled(true);
		
		setSwapperEnabled(true);
			
		//Init this since the list listener doesn't fire
		txtSetTo.setText(String.format(Strings.MUSICSWAPPER_CURRENTOFFSET, editedFiles[lstOriginal.getSelectedIndex()].getOffset() & 0xFFFFFFFF));
		if (editedFiles[lstOriginal.getSelectedIndex()].getOffset() != originalMusicFile.getPackFolders()[0].getFiles()[lstOriginal.getSelectedIndex()].dataoffset)
			txtSetTo.setForeground(Color.RED);
		else
			txtSetTo.setForeground(Color.decode("#006400"));
	}
	
	private void restoreFromBackup() throws IOException
	{
		// Create backup
		System.out.println("Restoring...");
		
		edittingIndexFile.delete();
		backup.renameTo(edittingIndexFile);
		
		lblBackup.setText("Backup does not exist.");
		btnBackup.setEnabled(true);
		btnRestore.setEnabled(false);
		
		setSwapperEnabled(false);
	}
	
	private void setSwapperEnabled(boolean isEnabled) {
		
		lstOriginal.clearSelection();
		lstSet.clearSelection();
		
		if (!isEnabled){			
			((DefaultListModel<String>)lstOriginal.getModel()).clear();
			((DefaultListModel<String>)lstSet.getModel()).clear();
			txtSetTo.setText(Strings.MUSICSWAPPER_CURRENTSETTO);
			txtSetTo.setForeground(Color.decode("#000000"));
		}
		btnSwap.setEnabled(isEnabled);
		btnRevert.setEnabled(isEnabled);
		lblOriginal.setEnabled(isEnabled);
		lstOriginal.setEnabled(isEnabled);
		lblSetId.setEnabled(isEnabled);
		txtSetTo.setEnabled(isEnabled);
		lstSet.setEnabled(isEnabled);
		pnlSwapper.setEnabled(isEnabled);
		
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	private void swapMusic(int which, int to) {
		
		if (which == -1 || to == -1)
			return;
		
		SqPack_File toBeChanged = originalMusicFile.getPackFolders()[0].getFiles()[which];
		SqPack_File toThisFile = originalMusicFile.getPackFolders()[0].getFiles()[to];
		editedFiles[which] = new SqPack_File(toBeChanged.getId(), toBeChanged.getId2(),
				toThisFile.getOffset());
		
		txtSetTo.setText(String.format(Strings.MUSICSWAPPER_CURRENTOFFSET, toThisFile.getOffset() & 0xFFFFFFFF));
		if (toBeChanged.getOffset() != toThisFile.getOffset())
			txtSetTo.setForeground(Color.RED);
		else
			txtSetTo.setForeground(Color.decode("#006400"));
		try {
			LERandomAccessFile ref = new LERandomAccessFile(
					edittingIndexFile.getCanonicalPath(), "rw");

			ref.seek(SqPack_IndexFile.checkSqPackHeader(ref));
			
			int segHeaderLengthres = ref.readInt();				
			
			//Read it in
			int firstVal = ref.readInt();			
			int offset = ref.readInt();
			int size = ref.readInt();
			
			ref.seek(offset);
			for (int i = 0; i < size; i++)
			{	
				if (i == which)
				{
					ref.skipBytes(8);
					ref.writeInt((int)editedFiles[which].dataoffset);					
				}
				else
					ref.skipBytes(16);									
			}
						
			ref.close();
			
			System.out.println("Data changed");
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(MusicSwapperWindow.this,
					Strings.ERROR_CANNOT_OPEN_INDEX,
				    Strings.DIALOG_TITLE_ERROR,
				    JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(MusicSwapperWindow.this,
					Strings.ERROR_EDITIO,
				    Strings.DIALOG_TITLE_ERROR,
				    JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

}
