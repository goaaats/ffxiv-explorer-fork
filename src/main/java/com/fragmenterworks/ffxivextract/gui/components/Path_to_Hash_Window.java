package com.fragmenterworks.ffxivextract.gui.components;

import java.awt.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.fragmenterworks.ffxivextract.Strings;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.sqpack.index.SqPackIndexFile;
import com.fragmenterworks.ffxivextract.paths.Crc32;
import com.fragmenterworks.ffxivextract.paths.PathUtils;
import com.fragmenterworks.ffxivextract.paths.database.HashDatabase;

public class Path_to_Hash_Window extends JFrame {

    private JPanel contentPane;
    private JTextField edtFullPath;
    private JTextArea txtOutput;
    private JPanel panel_2;
    private JPanel bottomPane;
    private JButton btnCalculate;
    private JButton btnClose;
    private JCheckBox autoCommitCheckBox;
    private JLabel indexLabel;
    private JScrollPane scrollPane;

    SqPackIndexFile currentIndex;

    public Path_to_Hash_Window(SqPackIndexFile currentIndex) {

        setTitle(Strings.PATHTOHASH_TITLE);
        URL imageURL = getClass().getResource("/frameicon.png");
        ImageIcon image = new ImageIcon(imageURL);
        this.setIconImage(image.getImage());
        setBounds(100, 100, 510, 220);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel outputPanel = new JPanel();
        contentPane.add(outputPanel, BorderLayout.CENTER);
        outputPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.X_AXIS));
        txtOutput = new JTextArea(Strings.PATHTOHASH_INTRO);
        txtOutput.setRows(2);
        txtOutput.setEditable(false);
        outputPanel.add(txtOutput);

        panel_2 = new JPanel();
        panel_2.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JLabel pathLabel = new JLabel(Strings.PATHTOHASH_PATH);
        panel.add(pathLabel);

        edtFullPath = new JTextField();
        edtFullPath.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calcHash();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calcHash();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calcHash();
            }
        });
        edtFullPath.setColumns(10);
        panel.add(edtFullPath);
        panel_2.add(panel);

        contentPane.add(panel_2, BorderLayout.NORTH);

        // Set up bottom pane (index label, auto-commit checkbox, calculate button, close button)
        bottomPane = new JPanel();

        indexLabel = new JLabel();
        if (currentIndex == null)
            indexLabel.setText("Current index: None");
        else
            indexLabel.setText(String.format("Current index: %06x", currentIndex.getIndexId()));
        bottomPane.add(indexLabel);

        autoCommitCheckBox = new JCheckBox(Strings.PATHTOHASH_AUTO_COMMIT);
        autoCommitCheckBox.setSelected(true);
        bottomPane.add(autoCommitCheckBox);

        btnCalculate = new JButton(Strings.PATHTOHASH_BUTTON_COMMIT);
        btnCalculate.addActionListener(e -> commit());
        bottomPane.add(btnCalculate);

        btnClose = new JButton(Strings.PATHTOHASH_BUTTON_CLOSE);
        btnClose.addActionListener(e -> Path_to_Hash_Window.this.dispose());
        bottomPane.add(btnClose);

        contentPane.add(bottomPane, BorderLayout.SOUTH);

        setIndex(currentIndex);
    }

    private void commit() {
        var result = JOptionPane.showConfirmDialog(this, Strings.PATHTOHASH_BUTTON_FORCE_WARNING, "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.CANCEL_OPTION) {
            return;
        }

        String path = edtFullPath.getText();

        boolean hashResult = HashDatabase.addPath(path);
        JOptionPane.showMessageDialog(this,
                "The path was " + (hashResult ? "" : "not ") + "new and was added to the database.",
                "Path Hash Window",
                hashResult ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    private void calcHash() {
        String path = edtFullPath.getText();

        if (!path.contains("/"))
        {
            var hash = Crc32.compute(path);
            txtOutput.setText(Strings.PATHTOHASH_FILE_HASH + String.format("0x%08X (%s)", hash, hash & 0xFFFFFFFFL));
            return;
        }

        path = path.trim();

        var hashes = PathUtils.computeHashesWithLower(path);

        Border border = BorderFactory.createLineBorder(Color.RED, 2);

        try {
            if (currentIndex != null && currentIndex.fileExists(path)) {
                HashDatabase.addPath(edtFullPath.getText());
                border = BorderFactory.createLineBorder(Color.GREEN, 2);
            }
        } catch (Exception e) {
            Utils.getGlobalLogger().error("", e);
        }

        txtOutput.setBorder(border);

        txtOutput.setText(
                Strings.PATHTOHASH_FOLDER_HASH + String.format("0x%08X (%s)", hashes.folderHash, hashes.folderHash & 0xFFFFFFFFL) + "\n"+
                Strings.PATHTOHASH_FILE_HASH + String.format("0x%08X (%s)", hashes.fileHash, hashes.fileHash & 0xFFFFFFFFL) + "\n" +
                Strings.PATHTOHASH_FULL_HASH + String.format("0x%08X (%s)", hashes.fullHash, hashes.fullHash & 0xFFFFFFFFL));
    }

    public void setIndex(SqPackIndexFile currentIndex) {
        this.currentIndex = currentIndex;
        if (currentIndex == null)
            indexLabel.setText("Current index: None");
        else
            indexLabel.setText(String.format("Current index: %06x", currentIndex.getIndexId()));
    }

}
