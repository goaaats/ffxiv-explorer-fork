package com.fragmenterworks.ffxivextract.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class IndeterminateLoadingDialog extends JDialog {

	public IndeterminateLoadingDialog(JFrame parent) {
		super(parent, "Loading...", ModalityType.APPLICATION_MODAL);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

		JLabel txtCurrentFile = new JLabel("Processing path list...");

		JProgressBar fileProgress = new JProgressBar();
		fileProgress.setIndeterminate(true);

		txtCurrentFile.setBorder(new EmptyBorder(5, 0, 0, 0));
		fileProgress.setBorder(new EmptyBorder(0, 0, 5, 0));
		contentPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

		contentPanel.add(txtCurrentFile);
		contentPanel.add(fileProgress);

		getContentPane().add(contentPanel);

		pack();
		setSize(500, 80);
		setResizable(false);
	}
}
