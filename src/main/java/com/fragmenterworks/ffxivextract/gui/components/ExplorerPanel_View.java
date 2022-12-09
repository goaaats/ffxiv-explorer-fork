package com.fragmenterworks.ffxivextract.gui.components;

import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.sqpack.index.IIndexUpdateListener;
import com.fragmenterworks.ffxivextract.models.sqpack.index.SqPackIndexFile;
import com.fragmenterworks.ffxivextract.models.sqpack.model.SqPackFile;
import com.fragmenterworks.ffxivextract.models.sqpack.model.SqPackFolder;
import com.fragmenterworks.ffxivextract.paths.CrcResult;
import com.fragmenterworks.ffxivextract.paths.database.IHashUpdateListener;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;

public class ExplorerPanel_View extends JScrollPane implements MouseListener, IIndexUpdateListener {

    private SqPackIndexFile currentIndex;
    private boolean enableHashUpdate = true;

    private final JTree fileTree;
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("No File Loaded");

    PopupMenu contextMenu;

    public ExplorerPanel_View() {
        setBackground(Color.WHITE);
        fileTree = new JTree(root) {

        };

        fileTree.setCellRenderer(new TreeRenderer());
        fileTree.setShowsRootHandles(false);
        fileTree.addMouseListener(this);

        contextMenu = new PopupMenu();
        MenuItem copyPath = new MenuItem("Copy full path");
        copyPath.addActionListener(e -> {
            String path = "";
            TreePath[] selectedPaths = fileTree.getSelectionPaths();

            if (selectedPaths == null)
                return;

            TreePath tp = selectedPaths[0];
            Object obj = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();

            if (obj == null)
                return;
            else if (obj instanceof SqPackFolder)
                path = ((SqPackFolder) obj).getName();
            else if (obj instanceof SqPackFile) {
                path = ((SqPackFile) obj).getFullPath();
            }
            StringSelection selection = new StringSelection(path);
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            clip.setContents(selection, selection);
        });
        contextMenu.add(copyPath);
        this.add(contextMenu);
        this.getViewport().add(fileTree);
    }

    public void addTreeSelectionListener(TreeSelectionListener l) {
        fileTree.addTreeSelectionListener(l);
    }

    public void fileOpened(SqPackIndexFile index) {
        currentIndex = index;
        currentIndex.addListener(this);
        load();
        ((DefaultTreeModel) fileTree.getModel()).reload();
    }

    public void fileClosed() {
        currentIndex.removeListener(this);
        currentIndex = null;
        unload();
        ((DefaultTreeModel) fileTree.getModel()).reload();
    }

    public void load() {
        for (var folder : currentIndex.getFolders()) {
            DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder);
            for (var file : folder.getFiles())
                folderNode.add(new DefaultMutableTreeNode(file));
            root.add(folderNode);
        }
        for (var file : currentIndex.getFiles()) {
            if (file.getParent() == null)
                root.add(new DefaultMutableTreeNode(file));
        }
    }

    private void unload() {
        root.removeAllChildren();
    }

    public void setEnableHashUpdate(boolean enableHashUpdate)
    {
        this.enableHashUpdate = enableHashUpdate;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            int row = fileTree.getClosestRowForLocation(e.getX(), e.getY());
            fileTree.setSelectionRow(row);
            contextMenu.show(fileTree, e.getX(), e.getY());
        }
    }

    @Override
    public void onIndexUpdate() {
        Utils.getGlobalLogger().debug("Index update");

        if (!enableHashUpdate) {
            Utils.getGlobalLogger().debug("Ignored");
            return;
        }

        unload();
        load();
        ((DefaultTreeModel) fileTree.getModel()).reload();
    }

    private class TreeRenderer extends DefaultTreeCellRenderer {

        private final Icon fileIcon = (Icon) UIManager.get("FileView.fileIcon");
        private final Icon folderIcon = (Icon) UIManager.get("FileView.directoryIcon");

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            setTextNonSelectionColor(Color.BLACK);

            if (node.getUserObject() instanceof SqPackFolder) //FOLDER
            {
                SqPackFolder folder = (SqPackFolder) node.getUserObject();

                value = folder.getName();

                setOpenIcon(getDefaultOpenIcon());
                setClosedIcon(getDefaultClosedIcon());
            } else if (node.getUserObject() instanceof SqPackFile) //FILE
            {
                SqPackFile file = (SqPackFile) node.getUserObject();
	        	
	        	/*if (currentCompareFile != null && currentCompareFile.isNewFile(file.getId()))
	        		setTextNonSelectionColor(new Color(0,150,0));
	        	else
	        		setTextNonSelectionColor(Color.BLACK);
	        	*/
                value = file.getName();

                setLeafIcon(fileIcon);
                setLeafIcon(fileIcon);
            } else //ROOT
            {
                if (tree.getModel().getChildCount(node) == 0)
                    value = "No File Loaded";
                else
                    value = "Dat File";
                setOpenIcon(folderIcon);
                setClosedIcon(folderIcon);
            }

            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
            return this;
        }
    }

    public boolean isOnlyFolder() {
        TreePath[] selectedPaths = fileTree.getSelectionPaths();

        if (selectedPaths == null)
            return true;

        if (selectedPaths.length != 1)
            return false;

        Object obj = ((DefaultMutableTreeNode) selectedPaths[0].getLastPathComponent()).getUserObject();

        return (obj instanceof SqPackFolder);
    }

    public ArrayList<SqPackFile> getSelectedFiles() {
        ArrayList<SqPackFile> selectedFiles = new ArrayList<>();
        TreePath[] selectedPaths = fileTree.getSelectionPaths();

        if (selectedPaths == null)
            return selectedFiles;

        for (TreePath tp : selectedPaths) {
            Object obj = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();
            if (obj == null)
                continue;
            if (obj instanceof SqPackFolder) {
                int children = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getChildCount();
                for (int i = 0; i < children; i++) {
                    SqPackFile file = (SqPackFile) ((DefaultMutableTreeNode) ((DefaultMutableTreeNode) tp.getLastPathComponent()).getChildAt(i)).getUserObject();
                    if (!selectedFiles.contains(file))
                        selectedFiles.add(file);
                }
            }
            if (obj instanceof SqPackFile)
                selectedFiles.add((SqPackFile) obj);
        }

        return selectedFiles;
    }

    public void select(long offset) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) fileTree.getModel().getRoot();
        Enumeration<TreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            var node = (DefaultMutableTreeNode) e.nextElement();
            if (node.getUserObject() instanceof SqPackFile) {
                SqPackFile file = (SqPackFile) node.getUserObject();
                if (offset == file.getElement().getOffset()) {
                    fileTree.setSelectionPath(new TreePath(node.getPath()));
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}

