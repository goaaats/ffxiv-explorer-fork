package ca.fraggergames.ffxivextract.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.Strings;
import ca.fraggergames.ffxivextract.models.SqPack_DatFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;

@SuppressWarnings("serial")
public class SearchWindow extends JFrame {

	JMenuBar menu = new JMenuBar();

	// FILE IO
	SqPack_IndexFile currentIndexFile;
	SqPack_DatFile currentDatFile;
	ISearchComplete searchCallback;

	// UI
	JPanel pnlSearchString;
	JPanel pnlSearchBytes;
	JRadioButton rbtnSearchString, rbtnSearchBytes;
	JLabel txtSearchLabel, txtSearchLabel2;
	JTextField txtStringToSearch, txtBytesToSearch;
	JButton btnBrowse;
	ButtonGroup searchGroup = new ButtonGroup();

	JPanel pnlButtons;
	JButton btnSearch, btnClose;

	// SAVED SEARCH
	String lastString;
	int lastFolder = 0;
	int lastFile = 0;
	
	public SearchWindow(SqPack_IndexFile currentIndexFile,
			SqPack_DatFile currentDatFile, ISearchComplete searchCallback) {
		this.setTitle(Strings.DIALOG_TITLE_SEARCH);
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
		this.searchCallback = searchCallback;
		
		this.currentIndexFile = currentIndexFile;
		this.currentDatFile = currentDatFile;

		// String search
		pnlSearchString = new JPanel(new GridBagLayout());
		txtSearchLabel = new JLabel(Strings.SEARCH_FRAMETITLE_BYSTRING);
		txtStringToSearch = new JTextField();
		txtStringToSearch.setPreferredSize(new Dimension(200, txtSearchLabel
				.getPreferredSize().height));
		rbtnSearchString = new JRadioButton(Strings.SEARCH_FRAMETITLE_BYSTRING);

		pnlSearchString.add(rbtnSearchString);
		pnlSearchString.add(txtStringToSearch);

		// Byte search
		pnlSearchBytes = new JPanel(new GridBagLayout());
		txtSearchLabel2 = new JLabel(Strings.SEARCH_FRAMETITLE_BYBYTES);
		txtBytesToSearch = new JTextField();
		txtBytesToSearch.setPreferredSize(new Dimension(200, txtSearchLabel
				.getPreferredSize().height));
		rbtnSearchBytes = new JRadioButton(Strings.SEARCH_FRAMETITLE_BYBYTES);

		pnlSearchBytes.add(rbtnSearchBytes);
		pnlSearchBytes.add(txtBytesToSearch);

		// Buttons
		pnlButtons = new JPanel();
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
		btnSearch = new JButton(Strings.BUTTONNAMES_SEARCH);
		btnClose = new JButton(Strings.BUTTONNAMES_CLOSE);
		pnlButtons.add(btnSearch);
		pnlButtons.add(btnClose);

		// ROOT
		JPanel pnlRoot = new JPanel();
		pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
		pnlRoot.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		pnlRoot.add(pnlSearchString);
		pnlRoot.add(pnlSearchBytes);
		pnlRoot.add(pnlButtons);

		rbtnSearchString.setActionCommand("string");
		rbtnSearchBytes.setActionCommand("bytes");
		searchGroup.add(rbtnSearchString);
		searchGroup.add(rbtnSearchBytes);
		searchGroup.setSelected(rbtnSearchString.getModel(), true);
		txtBytesToSearch.setEnabled(false);

		rbtnSearchString.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (rbtnSearchString.isSelected()) {
					txtStringToSearch.setEnabled(true);
					txtBytesToSearch.setEnabled(false);
				}
			}
		});
		rbtnSearchBytes.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (rbtnSearchBytes.isSelected()) {
					txtStringToSearch.setEnabled(false);
					txtBytesToSearch.setEnabled(true);
				}
			}
		});
		btnSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (searchGroup.getSelection().getActionCommand()
						.equals("string")) {
					doStringSearch(txtStringToSearch.getText());
				} else if (searchGroup.getSelection().getActionCommand()
						.equals("bytes")) {
					doBytesSearch(txtBytesToSearch.getText());
				}
			}
		});
		btnClose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SearchWindow.this.dispose();
			}
		});

		getContentPane().add(pnlRoot);
		pack();
	}

	private void doStringSearch(String string) {
		lastString = string;
		for (int i = lastFolder; i < currentIndexFile.getPackFolders().length; i++) {
			SqPack_Folder f = currentIndexFile.getPackFolders()[i];
			for (int j = lastFile; j < f.getFiles().length; j++) {
				SqPack_File fi = f.getFiles()[j];
				byte[] data;
				try {
					data = currentDatFile.extractFile(fi.dataoffset, null);
					if (data == null)
						continue;
					
					boolean breakOutOfFile = false;
					for (int i2 = 0; i2 < data.length - string.length(); i2++) {
						for (int j2 = 0; j2 < string.length(); j2++) {
							if (data[i2 + j2] == string.charAt(j2)) {
								if (j2 == string.length() - 1) {
									
									if (Constants.DEBUG){
									System.out.println(String.format("Folder: %08X",
											f.getId() & 0xFFFFFFFF));
									System.out.println(String.format("File: %08X",
											fi.getId() & 0xFFFFFFFF));
									System.out.println("---");
									}
									Object[] options = { "Continue",
											"Open", "Stop Search" };
									
									String folder= null, file = null;
									
									if (Constants.hashDatabase != null && Constants.hashDatabase.getFolder(f.getId()) != null)
										folder = Constants.hashDatabase.getFolder(f.getId());
									else
										folder = String.format("%08X", f.getId() & 0xFFFFFFFF);
									
									if (Constants.hashDatabase != null && Constants.hashDatabase.getFileName(fi.getId()) != null)
										file = Constants.hashDatabase.getFileName(fi.getId());
									else
										file = String.format("%08X", fi.getId() & 0xFFFFFFFF);
									
									int n = JOptionPane
											.showOptionDialog(
													this,
													"Found result in folder: " + folder + ", file: " + file,
													"Searching through DAT...",
													JOptionPane.YES_NO_CANCEL_OPTION,
													JOptionPane.QUESTION_MESSAGE,
													null, options, options[2]);
									switch (n)
									{
									case 0:
										breakOutOfFile = true;
										break;
									case 1:
										lastFolder = i+1;
										lastFile = j+1;
										searchCallback.onSearchChosen(fi.getOffset());
										this.setVisible(false);										
										return;
									case 2:
										searchCallback.onSearchChosen(-1);
										this.dispose();
										return;
									}

									
								} else
									continue;
							} else
								break;
						}
						if (breakOutOfFile)
							break;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		JOptionPane.showMessageDialog(SearchWindow.this,
				"Search completed.",
			    "Search Finished",
			    JOptionPane.QUESTION_MESSAGE);
		searchCallback.onSearchChosen(-1);
		this.dispose();
	}

	private void doBytesSearch(String bytes) {

	}
	
	public void searchAgain() {		
		doStringSearch(lastString);
	}
	
	public interface ISearchComplete{
		public void onSearchChosen(long offset);		
	}

	
}
