package ca.fraggergames.ffxivextract.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import ca.fraggergames.ffxivextract.Strings;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;

@SuppressWarnings("serial")
public class MusicSwapperWindow extends JFrame {

	JMenuBar menu = new JMenuBar();

	// FILE IO
	File lastOpenedFile = null;
	File edittingIndexFile = null;
	SqPack_IndexFile editMusicFile, originalMusicFile;

	SqPack_File[] editedFiles;

	// UI
	JPanel pnlMainDatFile;
	JLabel txtDatLabel;
	JTextField txtDatPath;
	JButton btnBrowse;	

	JPanel pnlSwapper;
	JLabel txtOriginal, txtSet;
	JComboBox drpOriginal;
	JComboBox drpSet;
	JLabel txtSetTo;
	JButton btnSwap, btnRevert;

	public MusicSwapperWindow() {
		this.setTitle(Strings.DIALOG_TITLE_MUSICSWAPPER);
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());

		// ARCHIVE PATH SETUP
		pnlMainDatFile = new JPanel(new GridBagLayout());
		pnlMainDatFile.setBorder(BorderFactory
				.createTitledBorder(Strings.MUSICSWAPPER_FRAMETITLE_ARCHIVE));
		txtDatLabel = new JLabel(Strings.MUSICSWAPPER_PATHTOFILE);
		txtDatPath = new JTextField();
		txtDatPath.setEditable(false);
		txtDatPath.setText(Strings.MUSICSWAPPER_DEFAULTPATHTEXT);
		txtDatPath.setPreferredSize(new Dimension(200, txtDatPath
				.getPreferredSize().height));

		btnBrowse = new JButton(Strings.BUTTONNAMES_BROWSE);
		btnBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setPath();
			}
		});

		pnlMainDatFile.add(txtDatLabel);
		pnlMainDatFile.add(txtDatPath);
		pnlMainDatFile.add(btnBrowse);

		// SWAPPER SETUP
		pnlSwapper = new JPanel(new GridBagLayout());
		pnlSwapper.setBorder(BorderFactory.createTitledBorder(Strings.MUSICSWAPPER_FRAMETITLE_SWAPPING));
		txtOriginal = new JLabel(Strings.MUSICSWAPPER_FROMID, SwingConstants.LEFT);

		txtSet = new JLabel(Strings.MUSICSWAPPER_TOID, SwingConstants.LEFT);
		txtOriginal = new JLabel(Strings.MUSICSWAPPER_ORIGINALID);
		drpOriginal = new JComboBox();
		drpOriginal.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				int state = itemEvent.getStateChange();
				if (state == ItemEvent.SELECTED && editMusicFile != null)
				{
					txtSetTo.setText(String.format(Strings.MUSICSWAPPER_CURRENTOFFSET, editedFiles[drpOriginal.getSelectedIndex()].getOffset() & 0xFFFFFFFF));
					if (editedFiles[drpOriginal.getSelectedIndex()].getOffset() != originalMusicFile.getPackFolders()[0].getFiles()[drpOriginal.getSelectedIndex()].dataoffset)
						txtSetTo.setForeground(Color.RED);
					else
						txtSetTo.setForeground(Color.decode("#006400"));
				}
			}
		});
		drpSet = new JComboBox();

		txtSetTo = new JLabel(Strings.MUSICSWAPPER_CURRENTSETTO);
		
		btnSwap = new JButton(Strings.BUTTONNAMES_SET);
		btnRevert = new JButton(Strings.BUTTONNAMES_REVERT);

		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.insets = new Insets(0, 0, 0, 5);
		pnlSwapper.add(txtOriginal, gc);
		gc.gridx = 2;
		gc.gridy = 0;
		gc.insets = new Insets(0, 5, 0, 0);
		pnlSwapper.add(txtSet, gc);
		gc.gridx = 0;
		gc.gridy = 1;
		gc.insets = new Insets(0, 0, 5, 5);
		pnlSwapper.add(drpOriginal, gc);
		gc.gridx = 2;
		gc.gridy = 1;
		gc.insets = new Insets(0, 5, 5, 0);
		pnlSwapper.add(drpSet, gc);
		gc.gridx = 0;
		gc.gridy = 3;
		gc.gridwidth = 2;
		gc.insets = new Insets(0, 0, 5, 0);
		pnlSwapper.add(txtSetTo, gc);
		gc.gridx = 0;
		gc.gridy = 4;
		gc.insets = new Insets(0, 0, 0, 5);
		gc.gridwidth = 2;
		pnlSwapper.add(btnSwap, gc);
		gc.gridx = 2;
		gc.insets = new Insets(0, 5, 0, 0);
		pnlSwapper.add(btnRevert, gc);

		// ROOT
		JPanel pnlRoot = new JPanel();
		pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
		pnlRoot.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		pnlRoot.add(pnlMainDatFile);
		pnlRoot.add(pnlSwapper);

		btnSwap.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				swapMusic(drpOriginal.getSelectedIndex(),
						drpSet.getSelectedIndex());
			}
		});
		btnRevert.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				swapMusic(drpOriginal.getSelectedIndex(),
						drpOriginal.getSelectedIndex());
			}
		});

		getContentPane().add(pnlRoot);
		pack();
		setSwapperEnabled(false);
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

		// Check if we got a backup
		File backup = new File(file.getParentFile().getAbsoluteFile(),
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
		} else {
			// Create backup
			System.out.println("No backup found, creating backup.");
			copyFile(file, backup);
			editMusicFile = new SqPack_IndexFile(file.getCanonicalPath());
			originalMusicFile = new SqPack_IndexFile(backup.getCanonicalPath());
			originalFiles = originalMusicFile.getPackFolders()[0].getFiles();
			editedFiles = editMusicFile.getPackFolders()[0].getFiles();
		}

		// We are good, load up the index
		loadDropDown(drpOriginal, originalFiles, 0);
		loadDropDown(drpSet, originalFiles, 0);

		edittingIndexFile = file;
	}

	private void loadDropDown(JComboBox dropdown, SqPack_File[] files,
			int selectedSpot) {
		dropdown.removeAllItems();
		for (int i = 0; i < files.length; i++)
			dropdown.addItem(String.format("%08X (%08X)", files[i].id & 0xFFFFFFFF, files[i].getOffset() & 0xFFFFFFFF));
		dropdown.setSelectedIndex(selectedSpot);
	}

	private void setSwapperEnabled(boolean isEnabled) {
		pnlSwapper.setEnabled(isEnabled);
		txtOriginal.setEnabled(isEnabled);
		drpOriginal.setEnabled(isEnabled);
		txtSet.setEnabled(isEnabled);
		txtSetTo.setEnabled(isEnabled);
		drpSet.setEnabled(isEnabled);
		btnSwap.setEnabled(isEnabled);
		btnRevert.setEnabled(isEnabled);
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
			ref.readInt();
			int firstVal = ref.readInt();
			if (firstVal == 0) // Fell into padding... we done here
				throw new IOException();
			int offset = ref.readInt();
			int size = ref.readInt()/0x10;
			
			ref.seek(offset);
			for (int i = 0; i < size; i++)
			{	
				if (i == which)
				{
					ref.skipBytes(8);
					ref.writeInt((int)editedFiles[which].dataoffset/0x8);					
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
