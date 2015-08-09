package com.fragmenterworks.ffxivextract.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.Strings;

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
		panel.setBorder(new TitledBorder(null, "General Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.rowHeights = new int[]{23, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0};
		gbl_panel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);		
		
		JPanel panel_4 = new JPanel();
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.insets = new Insets(0, 0, 5, 5);
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 0;
		panel.add(panel_4, gbc_panel_4);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.LINE_AXIS));
		
		JLabel lblNewLabel = new JLabel("FFXIV Path");
		panel_4.add(lblNewLabel);
		
		JPanel panel_3 = new JPanel();
		panel_4.add(panel_3);
		
		txtDatPath = new JTextField();		
		panel_3.add(txtDatPath);
		txtDatPath.setText(Constants.datPath);
		txtDatPath.setPreferredSize(new Dimension(200, 20));
		
		JButton btnBrowse = new JButton("Browse");
		panel_3.add(btnBrowse);
		
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
		
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();		
	
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
				.userNodeForPackage(com.fragmenterworks.ffxivextract.Main.class);
		prefs.put(Constants.PREF_DAT_PATH, txtDatPath.getText());		
		Constants.datPath = txtDatPath.getText();
		dispose();
	}
}
