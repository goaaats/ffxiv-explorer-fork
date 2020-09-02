package com.fragmenterworks.ffxivextract.gui.modelviewer;

import com.fragmenterworks.ffxivextract.Strings;
import com.fragmenterworks.ffxivextract.gui.components.EXDF_View;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.EXHF_File;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class ModelViewerWindow extends JFrame {

    private final JFrame parent;
    private Loading_Dialog dialog;
    private final String sqPackPath;
    private SqPack_IndexFile exdIndexFile;
    private SqPack_IndexFile modelIndexFile;
    private SqPack_IndexFile buildingIndexFile;
    private final JTabbedPane tabbedPane;
    private EXDF_View itemView;

    public ModelViewerWindow(JFrame parent, String sqPackPath) {

        this.setTitle(Strings.DIALOG_TITLE_MODELVIEWER);
        URL imageURL = getClass().getResource("/frameicon.png");
        ImageIcon image = new ImageIcon(imageURL);
        this.setIconImage(image.getImage());
        setSize(800, 600);

        this.parent = parent;
        this.sqPackPath = sqPackPath;

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

    }

    public SqPack_IndexFile getExdIndexFile() {
        return exdIndexFile;
    }

    public SqPack_IndexFile getModelIndexFile() {
        return modelIndexFile;
    }

    public SqPack_IndexFile getBuildingIndexFile() {
        return buildingIndexFile;
    }

    private String getSqpackPath() {
        return sqPackPath;
    }

    public void beginLoad() {
        OpenIndexTask task = new OpenIndexTask();
        dialog = new Loading_Dialog(ModelViewerWindow.this, 4);
        task.execute();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    class OpenIndexTask extends SwingWorker<Void, Void> {

        OpenIndexTask() {

        }

        @Override
        protected Void doInBackground() {


            try {
                dialog.nextFile(0, "..\\game\\sqpack\\ffxiv\\0a0000.win32.index");
                exdIndexFile = new SqPack_IndexFile(getSqpackPath() + "\\game\\sqpack\\ffxiv\\0a0000.win32.index", true);
                dialog.nextFile(1, "..\\game\\sqpack\\ffxiv\\040000.win32.index");
                modelIndexFile = new SqPack_IndexFile(getSqpackPath() + "\\game\\sqpack\\ffxiv\\040000.win32.index", true);
                dialog.nextFile(2, "..\\game\\sqpack\\ffxiv\\010000.win32.index");
                buildingIndexFile = new SqPack_IndexFile(getSqpackPath() + "\\game\\sqpack\\ffxiv\\010000.win32.index", true);
                dialog.nextFile(3, "Setting up lists...");
                EXHF_File exhfFile = new EXHF_File(exdIndexFile.extractFile("exd/item.exh"));
                itemView = new EXDF_View(exdIndexFile, "exd/item.exh", exhfFile);

                tabbedPane.add("Monsters", new ModelViewerMonsters(ModelViewerWindow.this, modelIndexFile));
                tabbedPane.add("Items", new ModelViewerItems(ModelViewerWindow.this, modelIndexFile, itemView));
                tabbedPane.add("Furniture", new ModelViewerFurniture(ModelViewerWindow.this, buildingIndexFile));
            } catch (IOException e1) {
                Utils.getGlobalLogger().error(e1);
                getContentPane().removeAll();
                getContentPane().add(new JLabel("Error: Could not find game files. Is DAT path correct?"));
                return null;
            }
            return null;
        }

        @Override
        protected void done() {
            dialog.dispose();

            ModelViewerWindow.this.setLocationRelativeTo(parent);
            ModelViewerWindow.this.setVisible(true);
        }
    }

}
