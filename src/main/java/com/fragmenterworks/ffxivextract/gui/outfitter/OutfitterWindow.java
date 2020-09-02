package com.fragmenterworks.ffxivextract.gui.outfitter;

import com.fragmenterworks.ffxivextract.Strings;
import com.fragmenterworks.ffxivextract.gui.components.EXDF_View;
import com.fragmenterworks.ffxivextract.gui.modelviewer.Loading_Dialog;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.EXHF_File;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;

public class OutfitterWindow extends JFrame {

    private final JFrame parent;
    private Loading_Dialog dialog;
    private final String sqPackPath;
    private SqPack_IndexFile exdIndexFile;
    private SqPack_IndexFile modelIndexFile;
    private EXDF_View itemView;

    public OutfitterWindow(JFrame parent, String sqPackPath) {

        this.setTitle(Strings.DIALOG_TITLE_OUTFITTER);
        URL imageURL = getClass().getResource("/frameicon.png");
        ImageIcon image = new ImageIcon(imageURL);
        this.setIconImage(image.getImage());
        setSize(800, 600);

        this.parent = parent;
        this.sqPackPath = sqPackPath;

    }

    public SqPack_IndexFile getExdIndexFile() {
        return exdIndexFile;
    }

    public SqPack_IndexFile getModelIndexFile() {
        return modelIndexFile;
    }

    private String getSqpackPath() {
        return sqPackPath;
    }

    public void beginLoad() {
        JOptionPane.showMessageDialog(this,
                Strings.MSG_OUTFITTER,
                Strings.MSG_OUTFITTER_TITLE,
                JOptionPane.INFORMATION_MESSAGE);

        OpenIndexTask task = new OpenIndexTask();
        dialog = new Loading_Dialog(OutfitterWindow.this, 3);
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
                dialog.nextFile(2, "Loading initial models...");
                EXHF_File exhfFile = new EXHF_File(exdIndexFile.extractFile("exd/item.exh"));
                itemView = new EXDF_View(exdIndexFile, "exd/item.exh", exhfFile);
                getContentPane().add(new Outfitter(modelIndexFile, itemView));
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

            OutfitterWindow.this.setLocationRelativeTo(parent);
            OutfitterWindow.this.setVisible(true);
        }
    }

}
