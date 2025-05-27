package com.fragmenterworks.ffxivextract.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Loading_Dialog extends JDialog {
    private final int SCREEN_UPDATE_INTERVAL = 100; // ms

    private final JLabel txtCurrentFile;
    private final JLabel txtCurrentBlock;
    private final JProgressBar fileProgress;
    private final JProgressBar blockProgress;

    private final int numFiles;
    private int numBlocks;

    private long nextScreenUpdate;

    public boolean isCancelled = false;

    public Loading_Dialog(JFrame parent, int numFiles) {
        super(parent, ModalityType.APPLICATION_MODAL);
        this.numFiles = numFiles;
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

        txtCurrentFile = new JLabel("No File");
        txtCurrentBlock = new JLabel("No Block");

        fileProgress = new JProgressBar();
        blockProgress = new JProgressBar();

        //Margins
        txtCurrentFile.setBorder(new EmptyBorder(5, 0, 0, 0));
        txtCurrentBlock.setBorder(new EmptyBorder(10, 0, 0, 0));
        fileProgress.setBorder(new EmptyBorder(0, 0, 5, 0));
        blockProgress.setBorder(new EmptyBorder(0, 0, 5, 0));
        contentPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        contentPanel.add(txtCurrentFile);
        contentPanel.add(fileProgress);
        contentPanel.add(txtCurrentBlock);
        contentPanel.add(blockProgress);

        fileProgress.setMaximum(numFiles);

        getContentPane().add(contentPanel);

        pack();
        setSize(500, 130);
        setResizable(false);
    }

    public void nextFile(int curFile, String filename) {
        fileProgress.setValue(curFile);
        blockProgress.setValue(0);
        txtCurrentFile.setText(filename + " (" + curFile + "/" + numFiles + ")");
        nextScreenUpdate = 0;
    }

    public void nextBlock(int currentBlock) {
        var now = System.currentTimeMillis();
        if (now < nextScreenUpdate)
            return;
        
        nextScreenUpdate = now + SCREEN_UPDATE_INTERVAL;
        blockProgress.setValue(currentBlock);
        txtCurrentBlock.setText("Decompressing block " + currentBlock + " of " + numBlocks);
    }

    public void setMaxBlocks(int blockCount) {
        numBlocks = blockCount;
    }
}
