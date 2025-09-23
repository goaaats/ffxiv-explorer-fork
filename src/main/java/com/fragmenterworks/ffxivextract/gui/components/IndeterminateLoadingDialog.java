package com.fragmenterworks.ffxivextract.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class IndeterminateLoadingDialog extends JDialog {

    private final JProgressBar progressBar;
    private final JLabel progressTextLabel;
    private final JLabel stepTextLabel;

    private int max = 0;

    public IndeterminateLoadingDialog(JFrame parent) {
        super(parent, "Loading...", ModalityType.APPLICATION_MODAL);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        stepTextLabel = new JLabel("Downloading path list...");
        stepTextLabel.setHorizontalAlignment(SwingConstants.LEFT);

        progressTextLabel = new JLabel("");
        progressTextLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(400, 20));
        progressBar.setBorderPainted(false);

        // First row, left (label)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(stepTextLabel, gbc);

        // First row, right (progress text)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0;
        contentPanel.add(progressTextLabel, gbc);

        // Second row, progress bar (span both columns)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0);
        contentPanel.add(progressBar, gbc);

        getContentPane().add(contentPanel);

        pack();
        setSize(500, 130);
        setLocationRelativeTo(parent);
        setResizable(false);

        progressTextLabel.setText("");
    }

    public void setDeterminate(int max) {
        this.max = max;
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(max);
        progressBar.setValue(0);
        stepTextLabel.setText("Processing path list...");
        updateProgressText(0);
    }

    public void setProgress(int value) {
        progressBar.setValue(value);
        updateProgressText(value);
    }

    private void updateProgressText(int value) {
        if (max > 0) {
            progressTextLabel.setText(value + " / " + max);
        } else {
            progressTextLabel.setText("");
        }
    }
}
