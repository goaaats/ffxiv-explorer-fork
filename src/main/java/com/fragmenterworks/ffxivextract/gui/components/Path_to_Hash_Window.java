package com.fragmenterworks.ffxivextract.gui.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.fragmenterworks.ffxivextract.Strings;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;
import com.fragmenterworks.ffxivextract.storage.HashDatabase;

public class Path_to_Hash_Window extends JFrame {

    private JPanel contentPane;
    private JTextField edtFullPath;
    private JTextArea txtOutput;
    private JPanel panel_2;
    private JPanel panel_3;
    private JButton btnCalculate;
    private JButton btnClose;
    private JScrollPane scrollPane;

    SqPack_IndexFile currentIndex;

    public Path_to_Hash_Window(SqPack_IndexFile currentIndex) {

        this.currentIndex = currentIndex;

        setTitle(Strings.PATHTOHASH_TITLE);
        URL imageURL = getClass().getResource("/frameicon.png");
        ImageIcon image = new ImageIcon(imageURL);
        this.setIconImage(image.getImage());
        setBounds(100, 100, 510, 220);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel panel_1 = new JPanel();
        contentPane.add(panel_1, BorderLayout.CENTER);
        panel_1.setBorder(new EmptyBorder(5, 0, 0, 0));
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        scrollPane = new JScrollPane();
        panel_1.add(scrollPane);

        txtOutput = new JTextArea(Strings.PATHTOHASH_INTRO);
        scrollPane.setViewportView(txtOutput);
        txtOutput.setRows(2);
        txtOutput.setEditable(false);

        panel_2 = new JPanel();
        panel_2.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.add(panel_2, BorderLayout.NORTH);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel_2.add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JLabel lblNewLabel = new JLabel(Strings.PATHTOHASH_PATH);
        panel.add(lblNewLabel);

        edtFullPath = new JTextField();
        panel.add(edtFullPath);
        edtFullPath.setColumns(10);

        panel_3 = new JPanel();
        contentPane.add(panel_3, BorderLayout.SOUTH);

        btnCalculate = new JButton(Strings.PATHTOHASH_BUTTON_HASHTHIS);
        panel_3.add(btnCalculate);

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

        btnCalculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commit();
            }
        });

        btnClose = new JButton(Strings.PATHTOHASH_BUTTON_CLOSE);
        panel_3.add(btnClose);
        btnClose.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Path_to_Hash_Window.this.dispose();
            }
        });
    }

    private void commit() {
        boolean result = HashDatabase.addPathToDB(edtFullPath.getText(), currentIndex.getName());
        JOptionPane.showMessageDialog(this,
                "The path was " + (result ? "successfully" : "unsuccessfully") + "added to the database.",
                "File Open Error",
                result ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    private void calcHash() {
        String path = edtFullPath.getText();

        if (!path.contains("/"))
        {
            txtOutput.setText(Strings.PATHTOHASH_ERROR_INVALID);
            return;
        }

        path = path.trim();

        String folder = path.substring(0, path.lastIndexOf('/')).toLowerCase();
        String filename = path.substring(path.lastIndexOf('/') + 1);

        int folderHash = HashDatabase.computeCRC(folder.getBytes(), 0, folder.getBytes().length);
        int fileHash = HashDatabase.computeCRC(filename.getBytes(), 0, filename.getBytes().length);
        int fullHash = HashDatabase.computeCRC(path.getBytes(), 0, path.getBytes().length);

        String base = "";
        Border border = BorderFactory.createLineBorder(Color.RED, 2);

        try {
            if (currentIndex.extractFile(path) != null) {
                HashDatabase.addPathToDB(edtFullPath.getText(), currentIndex.getName());
                border = BorderFactory.createLineBorder(Color.GREEN, 2);
            }
        } catch (Exception e) {
            Utils.getGlobalLogger().error(e);
        }

        txtOutput.setBorder(border);

        txtOutput.setText(base +
                Strings.PATHTOHASH_FOLDER_HASH + String.format("0x%08X (%s)", folderHash, Long.toString(folderHash & 0xFFFFFFFFL)) + "\n"+
                Strings.PATHTOHASH_FILE_HASH + String.format("0x%08X (%s)", fileHash, Long.toString(fileHash & 0xFFFFFFFFL)) + "\n" +
                "Full hash: " + String.format("0x%08X (%s)", fullHash, Long.toString(fullHash & 0xFFFFFFFFL)));
    }

}
