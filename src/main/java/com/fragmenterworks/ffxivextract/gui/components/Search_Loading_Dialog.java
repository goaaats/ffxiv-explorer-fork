package com.fragmenterworks.ffxivextract.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

class Search_Loading_Dialog extends JDialog {

    private final JLabel txtCurrentFolder;
    private final JLabel txtCurrentFile;
    private final JProgressBar folderProgress;
    private final JProgressBar fileProgress;

    private final int numFolders;
    private int numFiles;

    public boolean isCancelled = false;

    public Search_Loading_Dialog(JFrame parent, int numFolders) {
        super(parent);
        this.numFolders = numFolders;
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

        txtCurrentFolder = new JLabel("No File");
        txtCurrentFile = new JLabel("No Block");

        folderProgress = new JProgressBar();
        fileProgress = new JProgressBar();

        //Margins
        txtCurrentFolder.setBorder(new EmptyBorder(5, 0, 0, 0));
        txtCurrentFile.setBorder(new EmptyBorder(10, 0, 0, 0));
        folderProgress.setBorder(new EmptyBorder(0, 0, 5, 0));
        fileProgress.setBorder(new EmptyBorder(0, 0, 5, 0));
        contentPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        contentPanel.add(txtCurrentFolder);
        contentPanel.add(folderProgress);
        contentPanel.add(txtCurrentFile);
        contentPanel.add(fileProgress);

        folderProgress.setMaximum(numFolders);

        getContentPane().add(contentPanel);

        pack();
        setSize(500, 130);
        setResizable(false);
    }

    public void nextFolder(int curFolder, String foldername) {
        folderProgress.setValue(curFolder);
        fileProgress.setValue(0);
        txtCurrentFolder.setText(foldername + " (" + curFolder + "/" + numFolders + ")");
    }

    public void nextFile(int currFile, String filename) {
        fileProgress.setValue(currFile);
        txtCurrentFile.setText(filename + " (" + currFile + "/" + numFiles + ")");
    }

    public void setMaxBlocks(int blockCount) {
        numFiles = blockCount;
    }
}
