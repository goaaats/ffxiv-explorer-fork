package ca.fraggergames.ffxivextract.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.Strings;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JTextField;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class SettingsWindow extends JDialog {
	private JTextField txtDatPath;

	public SettingsWindow(JFrame parent) {
		super(parent, ModalityType.APPLICATION_MODAL);
		this.setTitle(Strings.DIALOG_TITLE_SETTINGS);
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("FFXIV Location"));
		getContentPane().add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.rowHeights = new int[]{23, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);		
		
		txtDatPath = new JTextField();
		//textField.setText("Point to your MACRO.DAT file");
		txtDatPath.setPreferredSize(new Dimension(200, 20));
		
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.weightx = 1.0;
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 0;
		panel.add(txtDatPath, gbc_textField);
		
		JButton btnBrowse = new JButton("Browse");
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.anchor = GridBagConstraints.NORTHWEST;
		gbc_button.gridx = 1;
		gbc_button.gridy = 0;
		panel.add(btnBrowse, gbc_button);
		
		btnBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setPath();
			}
		});
	
		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				saveSettings();	
			}
		});
		
		panel_1.add(btnSave);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SettingsWindow.this.dispose();
			}
		});
		
		panel_1.add(btnCancel);
		
		GridBagConstraints gbc_label = new GridBagConstraints();		
	
		pack();
	}

	public void setPath() {
		JFileChooser fileChooser = new JFileChooser();

		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int retunval = fileChooser.showOpenDialog(SettingsWindow.this);

		if (retunval == JFileChooser.APPROVE_OPTION) {
			try {
				txtDatPath.setText(fileChooser.getSelectedFile()
						.getCanonicalPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	private void saveSettings()
	{
		Preferences prefs = Preferences
				.userNodeForPackage(ca.fraggergames.ffxivextract.Main.class);
		prefs.put(Constants.PREF_DAT_PATH, txtDatPath.getText());		
		Constants.datPath = txtDatPath.getText();
		dispose();
	}
}
