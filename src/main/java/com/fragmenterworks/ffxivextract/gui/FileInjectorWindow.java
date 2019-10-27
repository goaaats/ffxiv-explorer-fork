package com.fragmenterworks.ffxivextract.gui;

import com.fragmenterworks.ffxivextract.Strings;
import com.fragmenterworks.ffxivextract.helpers.DatBuilder;
import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;
import com.fragmenterworks.ffxivextract.helpers.LERandomAccessFile;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

class FileInjectorWindow extends JFrame {

    //FILE I/O
    private File lastOpenedFile;
    private File backup = null;
    private File edittingIndexFile = null;
    private SqPack_IndexFile editMusicFile, originalMusicFile;
    private SqPack_File[] editedFiles;
    private final Hashtable<Integer, Integer> originalPositionTable = new Hashtable<Integer, Integer>(); //Fucking hack, but this is my fix if we want alphabetical sort
    private ByteOrder workingEndian;

    //CUSTOM MUSIC STUFF
    private int currentDatIndex;
    private String customDatPath;
    private ArrayList<String> customPaths = new ArrayList<String>();
    private ArrayList<Long> customIndexes = new ArrayList<Long>();
    private boolean datWasGenerated = false;

    //GUI
    private final JPanel pnlBackup;
    private final JPanel pnlSwapper;
    private final JTextField txtDatPath;
    private final JLabel lblBackup;
    private final JButton btnBackup;
    private final JButton btnRestore;
    private JLabel txtSetTo;
    private final JLabel lblOriginal;
    private final JLabel lblSetId;
    private final JList lstOriginal = new JList();
    private final JList lstSet = new JList();
    private final JButton btnSwap;
    private final JButton btnRevert;
    private final JPanel pnlCustomMusic;
    private final JList lstCustomMusic;
    private final JButton btnAdd;
    private final JButton btnGenerateDat;
    private final JButton btnRemove;
    private final JLabel lblGenerateMessage;

    public FileInjectorWindow() {
        this.setTitle(Strings.DIALOG_TITLE_FILEINJECT);
        URL imageURL = getClass().getResource("/frameicon.png");
        ImageIcon image = new ImageIcon(imageURL);
        this.setIconImage(image.getImage());

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.NORTH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel pnlMusicArchive = new JPanel();
        pnlMusicArchive.setBorder(new TitledBorder(null, Strings.FILEINJECT_FRAMETITLE_ARCHIVE, TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(pnlMusicArchive);
        pnlMusicArchive.setLayout(new BorderLayout(0, 0));

        txtDatPath = new JTextField();
        txtDatPath.setEditable(false);
        txtDatPath.setText(Strings.FILEINJECT_DEFAULTPATHTEXT);
        pnlMusicArchive.add(txtDatPath, BorderLayout.CENTER);

        JButton btnBrowse = new JButton(Strings.BUTTONNAMES_BROWSE);
        pnlMusicArchive.add(btnBrowse, BorderLayout.EAST);

        pnlBackup = new JPanel();
        pnlBackup.setBorder(new TitledBorder(null, "Backup", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(pnlBackup);
        pnlBackup.setLayout(new BorderLayout(0, 0));

        lblBackup = new JLabel("No backup found");
        pnlBackup.add(lblBackup, BorderLayout.CENTER);
        lblBackup.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel panel_3 = new JPanel();
        panel_3.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlBackup.add(panel_3, BorderLayout.EAST);
        panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        btnBackup = new JButton("Backup");
        panel_3.add(btnBackup);

        btnRestore = new JButton("Restore");
        btnBackup.setEnabled(false);
        btnRestore.setEnabled(false);
        pnlBackup.setEnabled(false);
        lblBackup.setEnabled(false);
        panel_3.add(btnRestore);

        pnlCustomMusic = new JPanel();
        pnlCustomMusic.setBorder(new TitledBorder(null, "Custom Files <Advance>", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(pnlCustomMusic);
        pnlCustomMusic.setLayout(new BorderLayout(0, 0));

        JPanel panel_1 = new JPanel();
        pnlCustomMusic.add(panel_1, BorderLayout.NORTH);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        JScrollPane scrollPane_2 = new JScrollPane();
        panel_1.add(scrollPane_2);

        lstCustomMusic = new JList();
        lstCustomMusic.setVisibleRowCount(5);
        lstCustomMusic.setModel(new DefaultListModel());
        scrollPane_2.setViewportView(lstCustomMusic);

        JPanel panel_7 = new JPanel();
        panel_7.setBorder(new EmptyBorder(5, 5, 5, 5));
        pnlCustomMusic.add(panel_7, BorderLayout.WEST);
        panel_7.setLayout(new BorderLayout(0, 0));

        lblGenerateMessage = new JLabel("Generating a new custom dat may break already set indices. \r\nPlease go through the music list and reset anything flagged red.");
        lblGenerateMessage.setHorizontalAlignment(SwingConstants.CENTER);
        panel_7.add(lblGenerateMessage);

        JPanel panel_2 = new JPanel();
        FlowLayout flowLayout_1 = (FlowLayout) panel_2.getLayout();
        flowLayout_1.setAlignment(FlowLayout.RIGHT);
        pnlCustomMusic.add(panel_2, BorderLayout.SOUTH);

        btnAdd = new JButton("Add");
        panel_2.add(btnAdd);

        btnRemove = new JButton("Remove");
        panel_2.add(btnRemove);

        btnGenerateDat = new JButton("Generate Dat");
        panel_2.add(btnGenerateDat);

        JButton btnOgg2Scd = new JButton("Ogg2Scd Converter");
        panel_2.add(btnOgg2Scd);

        btnOgg2Scd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SCDConverterWindow converter = new SCDConverterWindow();
                converter.setVisible(true);
            }
        });

        pnlSwapper = new JPanel();
        pnlSwapper.setBorder(new TitledBorder(null, Strings.MUSICSWAPPER_FRAMETITLE_SWAPPING, TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPane.add(pnlSwapper);
        pnlSwapper.setLayout(new BorderLayout(0, 0));

        JPanel panel_4 = new JPanel();
        pnlSwapper.add(panel_4, BorderLayout.CENTER);
        panel_4.setLayout(new GridLayout(0, 2, 0, 0));

        JPanel panel_6 = new JPanel();
        panel_6.setBorder(new EmptyBorder(0, 0, 0, 3));
        panel_4.add(panel_6);
        panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.Y_AXIS));

        lblOriginal = new JLabel(Strings.MUSICSWAPPER_ORIGINALID);
        panel_6.add(lblOriginal);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel_6.add(scrollPane);

        lstOriginal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstOriginal.setModel(new DefaultListModel());
        lstOriginal.setCellRenderer(new SwapperCellRenderer());
        lstOriginal.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent event) {

                if (lstOriginal.getSelectedIndex() == -1) {
                    lstSet.clearSelection();
                    return;
                }

                if (event.getValueIsAdjusting() || lstSet.getModel().getSize() == 0)
                    return;
                txtSetTo.setText(String.format(Strings.MUSICSWAPPER_CURRENTOFFSET, editedFiles[lstOriginal.getSelectedIndex()].getOffset()));
                if (editedFiles[lstOriginal.getSelectedIndex()].getOffset() != originalMusicFile.getPackFolders()[0].getFiles()[lstOriginal.getSelectedIndex()].dataoffset)
                    txtSetTo.setForeground(Color.RED);
                else
                    txtSetTo.setForeground(Color.decode("#006400"));

            }
        });

        scrollPane.setViewportView(lstOriginal);

        JPanel panel_5 = new JPanel();
        panel_5.setBorder(new EmptyBorder(0, 3, 0, 0));
        panel_4.add(panel_5);
        panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.Y_AXIS));

        lblSetId = new JLabel(Strings.MUSICSWAPPER_TOID);
        panel_5.add(lblSetId);

        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel_5.add(scrollPane_1);

        lstSet.setModel(new DefaultListModel());
        lstSet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane_1.setViewportView(lstSet);

        JPanel panel_8 = new JPanel();
        panel_8.setBorder(new EmptyBorder(5, 0, 0, 0));
        pnlSwapper.add(panel_8, BorderLayout.SOUTH);
        panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.Y_AXIS));

        txtSetTo = new JLabel(Strings.MUSICSWAPPER_CURRENTSETTO);
        panel_8.add(txtSetTo);

        JPanel panel_9 = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel_9.getLayout();
        flowLayout.setAlignment(FlowLayout.RIGHT);
        panel_9.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel_8.add(panel_9);

        JButton btnGoto = new JButton(Strings.BUTTONNAMES_GOTOFILE);
        btnSwap = new JButton(Strings.BUTTONNAMES_SET);
        btnRevert = new JButton(Strings.BUTTONNAMES_REVERT);
        btnSwap.setHorizontalAlignment(SwingConstants.LEFT);

        panel_9.add(btnGoto);
        panel_9.add(btnSwap);

        panel_9.add(btnRevert);

        //SETUP

        btnBrowse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                setPath();
            }
        });

        btnSwap.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (lstSet.getModel().getElementAt(lstSet.getSelectedIndex()).equals("-----------"))
                    return;

                swapMusic(lstOriginal.getSelectedIndex(),
                        lstSet.getSelectedIndex());
            }
        });
        btnRevert.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                swapMusic(lstOriginal.getSelectedIndex(),
                        lstOriginal.getSelectedIndex() + (customIndexes.size() == 0 ? 0 : lstCustomMusic.getModel().getSize() + 1));
            }
        });

        btnBackup.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    createBackup();
                } catch (IOException e1) {
                    Utils.getGlobalLogger().error(e1);
                }
            }
        });

        btnRestore.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                restoreFromBackup();
            }
        });

        btnAdd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fileChooser = new JFileChooser(lastOpenedFile);

                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                int retunval = fileChooser.showOpenDialog(FileInjectorWindow.this);

                if (retunval == JFileChooser.APPROVE_OPTION) {

                    for (int i = 0; i < fileChooser.getSelectedFiles().length; i++)
                        ((DefaultListModel) lstCustomMusic.getModel()).addElement(fileChooser.getSelectedFiles()[i].getAbsolutePath());
                }

                btnGenerateDat.setEnabled(true);
            }
        });

        btnRemove.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                int selected = lstCustomMusic.getSelectedIndex();
                if (selected >= 0)
                    ((DefaultListModel) lstCustomMusic.getModel()).remove(selected);

                btnGenerateDat.setEnabled(true);
            }
        });

        btnGenerateDat.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                if (customIndexes.size() != 0) {
                    for (int i = 0; i < customIndexes.size(); i++)
                        ((DefaultListModel) lstSet.getModel()).removeElementAt(0);
                }

                if (((DefaultListModel) lstSet.getModel()).get(0).equals("-----------"))
                    ((DefaultListModel) lstSet.getModel()).removeElementAt(0);

                customPaths.clear();
                customIndexes.clear();

                String lastLoaded = "";
                try {
                    //Generate DAT
                    customDatPath = String.format("%s\\%s.win32.dat%d", edittingIndexFile.getParent(), originalMusicFile.getName(), currentDatIndex);
                    File datlstfile = new File(customDatPath + ".lst");
                    datlstfile.delete();

                    DatBuilder builder = new DatBuilder(currentDatIndex, customDatPath, workingEndian);
                    for (int i = 0; i < lstCustomMusic.getModel().getSize(); i++) {
                        lastLoaded = (String) lstCustomMusic.getModel().getElementAt(i);
                        customPaths.add((String) lstCustomMusic.getModel().getElementAt(i));
                        customIndexes.add(builder.addFile((String) lstCustomMusic.getModel().getElementAt(i)));
                    }
                    builder.finish();
                } catch (FileNotFoundException e) {
                    JOptionPane.showMessageDialog(FileInjectorWindow.this,
                            lastLoaded + " is missing. Dat generation was aborted. Please recreate the custom dat file, as some custom indexes may be invalid.\nDo not set any custom songs until done, as this may corrupt the index file and require a restore.",
                            Strings.DIALOG_TITLE_ERROR,
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(FileInjectorWindow.this,
                            "Write Error",
                            "There was an error writing to the modded index file.",
                            JOptionPane.ERROR_MESSAGE);
                    Utils.getGlobalLogger().error(e);
                    return;
                }
                try {
                    //Edit Index
                    EARandomAccessFile output = new EARandomAccessFile(edittingIndexFile, "rw", workingEndian);
                    output.seek(0x450);
                    output.writeInt(currentDatIndex + 1);
                    output.close();
                } catch (FileNotFoundException e) {
                    JOptionPane.showMessageDialog(FileInjectorWindow.this,
                            Strings.ERROR_CANNOT_OPEN_INDEX,
                            Strings.DIALOG_TITLE_ERROR,
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(FileInjectorWindow.this,
                            "Write Error",
                            "There was an error writing to the modded index file.",
                            JOptionPane.ERROR_MESSAGE);
                    Utils.getGlobalLogger().error(e);
                    return;
                }


                saveCustomDatIndexList();

                //Put new songs into list
                ((DefaultListModel) lstSet.getModel()).add(0, "-----------");
                for (int i = lstCustomMusic.getModel().getSize() - 1; i >= 0; i--)
                    ((DefaultListModel) lstSet.getModel()).add(0, ((DefaultListModel) lstCustomMusic.getModel()).elementAt(i));

                btnGenerateDat.setEnabled(false);
                datWasGenerated = true;

                lstOriginal.repaint();
            }
        });

        btnGoto.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String name = JOptionPane.showInputDialog(FileInjectorWindow.this, "What to search for?");

                int startIndex = lstOriginal.getSelectedIndex() + 1;

                for (int i = startIndex; i < lstOriginal.getModel().getSize(); i++) {
                    if (((String) lstOriginal.getModel().getElementAt(i)).toLowerCase().contains(name.toLowerCase())) {
                        lstOriginal.setSelectedIndex(i);
                        break;
                    }
                }

                lstOriginal.ensureIndexIsVisible(lstOriginal.getSelectedIndex());
                lstOriginal.repaint();
            }
        });

        pack();
        setSwapperEnabled(false);

        JOptionPane.showMessageDialog(this,
                Strings.MSG_FILEINJECT,
                Strings.MSG_FILEINJECT_TITLE,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void setPath() {
        JFileChooser fileChooser = new JFileChooser("C:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv");

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        FileFilter filter = new FileFilter() {

            @Override
            public String getDescription() {
                return "sqpack index file";
            }

            @Override
            public boolean accept(File f) {
                return f.getName().contains("index")
                        || f.isDirectory();
            }
        };
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int retunval = fileChooser.showOpenDialog(FileInjectorWindow.this);

        if (retunval == JFileChooser.APPROVE_OPTION) {
            try {
                txtDatPath.setText(fileChooser.getSelectedFile()
                        .getCanonicalPath());
            } catch (IOException e) {
                Utils.getGlobalLogger().error(e);
            }
            try {
                loadFile(fileChooser.getSelectedFile());
            } catch (Exception e) {
                Utils.getGlobalLogger().error(e);
            }
        }
    }

    private void loadFile(File file) throws Exception {
        setSwapperEnabled(true);
        SqPack_File[] originalFiles;

        ((DefaultListModel) lstOriginal.getModel()).clear();
        ((DefaultListModel) lstSet.getModel()).clear();

        datWasGenerated = false;
        ((DefaultListModel) lstCustomMusic.getModel()).clear();
        customPaths.clear();
        customIndexes.clear();

        pnlBackup.setEnabled(true);
        lblBackup.setEnabled(true);

        // Check if we got a backup
        backup = new File(file.getParentFile().getAbsoluteFile(),
                file.getName() + ".bak");
        if (backup.exists()) {
            Utils.getGlobalLogger().info("Backup found, checking file.");
            btnRestore.setEnabled(true);
            lblBackup.setText("Backup exists. Remember to restore before patching.");
        } else {
            // Create backup
            copyFile(file, backup);

            lblBackup.setText("Backup was auto generated. Remember to restore before patching.");
        }

        // Should hash here, but for now just check file counts
        originalMusicFile = new SqPack_IndexFile(backup.getCanonicalPath(), true);
        editMusicFile = new SqPack_IndexFile(file.getCanonicalPath(), true);

        boolean sort = false;
        int reply = JOptionPane.showConfirmDialog(null, "Do you want to sort the lists? This could take a while for big dats.", "", JOptionPane.YES_NO_OPTION);
        sort = reply == JOptionPane.YES_OPTION;

        if (sort) {
            Arrays.sort(editMusicFile.getPackFolders()[0].getFiles(), new Comparator<SqPack_File>() {
                @Override
                public int compare(SqPack_File o1, SqPack_File o2) {
//                    return String.format("%08X (%08X)", o1.getId() & 0xFFFFFFFF, o1.getOffset() & 0xFFFFFFFF).compareTo(
//                            String.format("%08X (%08X)", o2.getId() & 0xFFFFFFFF, o2.getOffset() & 0xFFFFFFFF)
                    return o1.getName2().compareTo(o2.getName2());
                }
            });
        }

        SqPack_File[] files = originalMusicFile.getPackFolders()[0].getFiles();
        for (int i = 0; i < files.length; i++)
            originalPositionTable.put(files[i].id, i);

        if (sort) {
            Arrays.sort(originalMusicFile.getPackFolders()[0].getFiles(), new Comparator<SqPack_File>() {
                @Override
                public int compare(SqPack_File o1, SqPack_File o2) {
                    return o1.getName2().compareTo(o2.getName2());
                }
            });
        }

        originalFiles = originalMusicFile.getPackFolders()[0].getFiles();
        editedFiles = editMusicFile.getPackFolders()[0].getFiles();

		/*
		// Throw and remake backup
		if (originalFiles.length != editedFiles.length) {
			Utils.getGlobalLogger().info("File mismatch, there was an update... remaking backup");
			backup.delete();
			copyFile(file, backup);
			originalMusicFile = new SqPack_IndexFile(
					backup.getCanonicalPath(), true);
			originalFiles = originalMusicFile.getPackFolders()[0]
					.getFiles();
		}
		*/

        Utils.getGlobalLogger().info("File is good.");
        btnBackup.setEnabled(false);
        btnRestore.setEnabled(true);

        // Set Current Index
        EARandomAccessFile input = new EARandomAccessFile(backup, "r", workingEndian);
        input.seek(0x450);
        currentDatIndex = input.readInt();
        input.close();

        // We are good, load up the index
        loadDropDown(lstOriginal, originalFiles, 0);
        loadDropDown(lstSet, originalFiles, 0);

        edittingIndexFile = file;

        loadCustomDatIndexList();

        //Init this since the list listener doesn't fire
        txtSetTo.setText(String.format(Strings.MUSICSWAPPER_CURRENTOFFSET, editedFiles[lstOriginal.getSelectedIndex()].getOffset()));
        if (editedFiles[lstOriginal.getSelectedIndex()].getOffset() != originalMusicFile.getPackFolders()[0].getFiles()[lstOriginal.getSelectedIndex()].dataoffset)
            txtSetTo.setForeground(Color.RED);
        else
            txtSetTo.setForeground(Color.decode("#006400"));
    }

    private void loadDropDown(JList list, SqPack_File[] files,
                              int selectedSpot) {
        DefaultListModel listModel = (DefaultListModel) list.getModel();

        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName2();

            if (fileName != null)
                listModel.addElement(String.format("%s (%08X)", fileName, files[i].getOffset()));
            else
                listModel.addElement(String.format("%08X (%08X)", files[i].id, files[i].getOffset()));
        }

        list.setSelectedIndex(0);
    }

    private void createBackup() throws IOException {
        // Create backup
        Utils.getGlobalLogger().info("Creating backup.");
        copyFile(edittingIndexFile, backup);
        editMusicFile = new SqPack_IndexFile(edittingIndexFile.getCanonicalPath(), true);
        originalMusicFile = new SqPack_IndexFile(backup.getCanonicalPath(), true);
        SqPack_File[] originalFiles = originalMusicFile.getPackFolders()[0].getFiles();
        editedFiles = editMusicFile.getPackFolders()[0].getFiles();

        Arrays.sort(editMusicFile.getPackFolders()[0].getFiles(), new Comparator<SqPack_File>() {

            @Override
            public int compare(SqPack_File o1, SqPack_File o2) {
                return o1.getName2().compareTo(o2.getName2());
            }
        });

        Arrays.sort(originalMusicFile.getPackFolders()[0].getFiles(), new Comparator<SqPack_File>() {

            @Override
            public int compare(SqPack_File o1, SqPack_File o2) {
                return o1.getName2().compareTo(o2.getName2());
            }
        });

        loadDropDown(lstOriginal, originalFiles, 0);
        loadDropDown(lstSet, originalFiles, 0);

        lblBackup.setText("Backup exists. Remember to restore before patching.");
        btnBackup.setEnabled(false);
        btnRestore.setEnabled(true);

        setSwapperEnabled(true);

        //Init this since the list listener doesn't fire
        txtSetTo.setText(String.format(Strings.MUSICSWAPPER_CURRENTOFFSET, editedFiles[0].getOffset()));
        if (editedFiles[0].getOffset() != originalMusicFile.getPackFolders()[0].getFiles()[0].dataoffset)
            txtSetTo.setForeground(Color.RED);
        else
            txtSetTo.setForeground(Color.decode("#006400"));
    }

    private void restoreFromBackup() {
        // Create backup
        Utils.getGlobalLogger().info("Restoring...");

        if (!edittingIndexFile.delete()) {
            JOptionPane.showMessageDialog(FileInjectorWindow.this,
                    Strings.ERROR_CANNOT_OPEN_INDEX,
                    Strings.DIALOG_TITLE_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (customDatPath != null) {
            File generatedDatFile = new File(customDatPath);
            if (generatedDatFile.exists())
                generatedDatFile.delete();
            File generatedDatFileList = new File(customDatPath + ".lst");
            if (generatedDatFileList.exists())
                generatedDatFileList.delete();
        }
        backup.renameTo(edittingIndexFile);

        lblBackup.setText("Backup does not exist.");
        btnBackup.setEnabled(true);
        btnRestore.setEnabled(false);

        setSwapperEnabled(false);

        datWasGenerated = false;
        ((DefaultListModel) lstCustomMusic.getModel()).clear();
        customPaths.clear();
        customIndexes.clear();
    }

    private void setSwapperEnabled(boolean isEnabled) {

        lstOriginal.clearSelection();
        lstSet.clearSelection();

        if (!isEnabled) {
            ((DefaultListModel) lstOriginal.getModel()).clear();
            ((DefaultListModel) lstSet.getModel()).clear();
            txtSetTo.setText(Strings.MUSICSWAPPER_CURRENTSETTO);
            txtSetTo.setForeground(Color.decode("#000000"));
        }

        lblGenerateMessage.setEnabled(isEnabled);
        pnlCustomMusic.setEnabled(isEnabled);
        lstCustomMusic.setEnabled(isEnabled);
        btnAdd.setEnabled(isEnabled);
        btnRemove.setEnabled(isEnabled);
        btnGenerateDat.setEnabled(isEnabled);

        btnSwap.setEnabled(isEnabled);
        btnRevert.setEnabled(isEnabled);
        lblOriginal.setEnabled(isEnabled);
        lstOriginal.setEnabled(isEnabled);
        lblSetId.setEnabled(isEnabled);
        txtSetTo.setEnabled(isEnabled);
        lstSet.setEnabled(isEnabled);
        pnlSwapper.setEnabled(isEnabled);

    }

    private static void copyFile(File sourceFile, File destFile)
            throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private void swapMusic(int which, int to) {

        if (which == -1 || to == -1)
            return;

        long tooffset = 0;

        SqPack_File toBeChanged = originalMusicFile.getPackFolders()[0].getFiles()[which];

        //This is the index of the file, not the alphaed
        int fileIndex = originalPositionTable.get(toBeChanged.id);

        if (lstCustomMusic.getModel().getSize() == 0) {
            SqPack_File toThisFile = originalMusicFile.getPackFolders()[0].getFiles()[to - lstCustomMusic.getModel().getSize()];
            editedFiles[which] = new SqPack_File(toBeChanged.getId(), toBeChanged.getId2(),
                    toThisFile.getOffset(), true);
            tooffset = toThisFile.getOffset();
        } else {
            if (to >= lstCustomMusic.getModel().getSize() + 1) {
                SqPack_File toThisFile = originalMusicFile.getPackFolders()[0].getFiles()[to - (lstCustomMusic.getModel().getSize() + 1)];
                editedFiles[which] = new SqPack_File(toBeChanged.getId(), toBeChanged.getId2(),
                        toThisFile.getOffset(), true);
                tooffset = toThisFile.getOffset();
            } else {
                editedFiles[which] = new SqPack_File(toBeChanged.getId(), toBeChanged.getId2(),
                        customIndexes.get(to), true);
                tooffset = customIndexes.get(to);
            }
        }

        try {
            // This segment copied from SqPack_IndexFile
            LERandomAccessFile lref = new LERandomAccessFile(edittingIndexFile.getCanonicalPath(), "rw");
            RandomAccessFile bref = new RandomAccessFile(edittingIndexFile.getCanonicalPath(), "rw");

            byte[] buffer = new byte[6];
            byte[] bigBuffer = new byte[6];

            lref.readFully(buffer, 0, 6);
            bref.readFully(bigBuffer, 0, 6);

            if (buffer[0] == 'S' && buffer[1] == 'q' && buffer[2] == 'P'
                    && buffer[3] == 'a' && buffer[4] == 'c' && buffer[5] == 'k') {
                workingEndian = ByteOrder.LITTLE_ENDIAN;
            } else if (bigBuffer[0] == 'S' && bigBuffer[1] == 'q' && bigBuffer[2] == 'P'
                    && bigBuffer[3] == 'a' && bigBuffer[4] == 'c' && bigBuffer[5] == 'k') {
                workingEndian = ByteOrder.BIG_ENDIAN;
            } else {
                lref.close();
                bref.close();
                throw new IOException("Not a SqPack file");
            }

            lref.seek(0x0c);
            bref.seek(0x0c);

            int headerLength;
            if (workingEndian == ByteOrder.LITTLE_ENDIAN)
                headerLength = lref.readInt();
            else
                headerLength = bref.readInt();

            EARandomAccessFile ref = new EARandomAccessFile(edittingIndexFile.getCanonicalPath(), "rw", workingEndian);
            ref.seek(headerLength);
            int segHeaderLengthres = ref.readInt();

            //Read it in
            int firstVal = ref.readInt();
            int offset = ref.readInt();
            int size = ref.readInt();

            ref.seek(offset);
            for (int i = 0; i < size; i++) {
                if (i == fileIndex) {
                    ref.skipBytes(8);
                    ref.writeInt((int) tooffset);
                    break;
                } else
                    ref.skipBytes(16);
            }

            ref.close();

            txtSetTo.setText(String.format(Strings.MUSICSWAPPER_CURRENTOFFSET, tooffset));
            if (toBeChanged.getOffset() != tooffset)
                txtSetTo.setForeground(Color.RED);
            else
                txtSetTo.setForeground(Color.decode("#006400"));

            Utils.getGlobalLogger().info("Data changed!");
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(FileInjectorWindow.this,
                    Strings.ERROR_CANNOT_OPEN_INDEX,
                    Strings.DIALOG_TITLE_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            Utils.getGlobalLogger().error(e);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(FileInjectorWindow.this,
                    Strings.ERROR_EDITIO,
                    Strings.DIALOG_TITLE_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            Utils.getGlobalLogger().error(e);
        }
    }

    private void loadCustomDatIndexList() {
        CustomDatPOJO toLoad = null;
        Gson gson = new Gson();

        try {

            String json = null;
            BufferedReader br = new BufferedReader(new FileReader(edittingIndexFile.getParent() + "\\" + originalMusicFile.getName().replace(".index", ".dat") + currentDatIndex + ".lst"));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\r\n");
                    line = br.readLine();
                }
                json = sb.toString();
            } finally {
                br.close();
            }
            toLoad = gson.fromJson(json, CustomDatPOJO.class);
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            Utils.getGlobalLogger().error(e);
        }

        customDatPath = toLoad.datPath;
        customPaths = toLoad.musicPaths;
        customIndexes = toLoad.musicOffsets;

        for (int i = 0; i < customPaths.size(); i++)
            ((DefaultListModel) lstCustomMusic.getModel()).addElement(customPaths.get(i));

        //Put new songs into list
        ((DefaultListModel) lstSet.getModel()).add(0, "-----------");
        for (int i = lstCustomMusic.getModel().getSize() - 1; i >= 0; i--)
            ((DefaultListModel) lstSet.getModel()).add(0, ((DefaultListModel) lstCustomMusic.getModel()).elementAt(i));

        btnGenerateDat.setEnabled(false);
        datWasGenerated = true;

        lstOriginal.repaint();
    }

    private void saveCustomDatIndexList() {
        CustomDatPOJO toSave = new CustomDatPOJO();
        toSave.datPath = customDatPath;
        toSave.musicPaths = customPaths;
        toSave.musicOffsets = customIndexes;
        Gson gson = new Gson();
        String json = gson.toJson(toSave);
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(customDatPath + ".lst");
            fileOut.write(json.getBytes());
            fileOut.close();
        } catch (IOException e) {
            Utils.getGlobalLogger().error(e);
        }
    }

    class SwapperCellRenderer extends DefaultListCellRenderer {

        SwapperCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            setText(value.toString());

            Color background;
            Color foreground;

            int lastVal = (int) ((editedFiles[index].dataoffset) & 0xF);

            boolean flagAsInvalid = false;
            if (!customIndexes.contains(editedFiles[index].dataoffset) && lastVal == (currentDatIndex + 1))
                flagAsInvalid = true;

            if (isSelected) {
                if (flagAsInvalid) {
                    Component defaultComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setForeground(Color.RED);
                    setBackground(defaultComponent.getBackground());
                } else
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            } else {
                if (flagAsInvalid) {
                    setBackground(Color.RED);
                    setForeground(Color.WHITE);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }

            return this;
        }
    }

    private class CustomDatPOJO {
        String datPath;
        ArrayList musicPaths;
        ArrayList<Long> musicOffsets;
    }
}