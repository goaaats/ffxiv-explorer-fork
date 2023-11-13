package com.fragmenterworks.ffxivextract.gui.modelviewer;

import com.fragmenterworks.ffxivextract.Strings;
import com.fragmenterworks.ffxivextract.gui.components.EXDF_View;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.EXHF_File;
import com.fragmenterworks.ffxivextract.models.sqpack.index.SqPackIndexFile;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ModelViewerWindow extends JFrame {

    private final JFrame parent;
    private Loading_Dialog dialog;
    private final String sqPackPath;
    private SqPackIndexFile exdIndexFile;
    private SqPackIndexFile modelIndexFile;
    private SqPackIndexFile buildingIndexFile;
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

    public SqPackIndexFile getExdIndexFile() {
        return exdIndexFile;
    }

    public SqPackIndexFile getModelIndexFile() {
        return modelIndexFile;
    }

    public SqPackIndexFile getBuildingIndexFile() {
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
                dialog.nextFile(0, "0a0000");
                exdIndexFile = SqPackIndexFile.read(
                    getSqpackPath() + File.separator +
                            "game" + File.separator + "sqpack" + File.separator + "ffxiv" + File.separator + "0a0000.win32.index"
                );
                dialog.nextFile(1, "040000");
                modelIndexFile = exdIndexFile.getIndexForIdFromSameRepo(0x040000);
                dialog.nextFile(2, "010000");
                buildingIndexFile = exdIndexFile.getIndexForIdFromSameRepo(0x010000);
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
