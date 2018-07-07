package com.fragmenterworks.ffxivextract.gui.modelviewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.storage.HashDatabase;

import com.fragmenterworks.ffxivextract.gui.FileManagerWindow;
import com.fragmenterworks.ffxivextract.gui.SearchWindow;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;

import java.awt.Component;

public class Loading_Dialog extends JDialog {

	private JPanel contentPanel;
	private JLabel txtCurrentFile;
	private JProgressBar fileProgress;
	
	private int numFiles;
	private int numBlocks;
	
	public boolean isCancelled = false;
	
	public Loading_Dialog(JFrame parent, int numFiles) {	
		super(parent, ModalityType.APPLICATION_MODAL);
		setTitle("Loading...");
		this.numFiles = numFiles;		
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
		
		txtCurrentFile = new JLabel("No File");
		
		fileProgress = new JProgressBar();
		fileProgress.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		//Margins
		txtCurrentFile.setBorder(new EmptyBorder(5, 0, 0, 0));
		fileProgress.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPanel.setBorder(new EmptyBorder(10, 10, 0, 10));		
		
		contentPanel.add(txtCurrentFile);
		contentPanel.add(fileProgress);
		
		fileProgress.setMaximum(numFiles);
		
		getContentPane().add(contentPanel);
		
		pack();
		setSize(500,100);
		setResizable(false);
	}
	
	public void nextFile(int curFile, String filename)
	{
		fileProgress.setValue(curFile);
		txtCurrentFile.setText(filename + " (" + curFile + "/" + numFiles + ")");
	}
	
	
}
