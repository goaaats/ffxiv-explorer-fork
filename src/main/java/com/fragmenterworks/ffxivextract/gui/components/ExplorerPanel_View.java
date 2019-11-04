package com.fragmenterworks.ffxivextract.gui.components;

import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;
import sun.awt.datatransfer.TransferableProxy;

import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;

@SuppressWarnings("serial")
public class ExplorerPanel_View extends JScrollPane implements MouseListener {

    private final JTree fileTree;
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("No File Loaded");
    JScrollPane scroller;

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
        copyPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = "";
                TreePath[] selectedPaths = fileTree.getSelectionPaths();

                if (selectedPaths == null)
                    return;

                TreePath tp = selectedPaths[0];
                Object obj = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();

                if (obj == null)
                    return;
                else if (obj instanceof SqPack_Folder)
                    path = ((SqPack_Folder) obj).getName();
                else if (obj instanceof SqPack_File) {
                    // It's messy but...
                    SqPack_Folder folder = (SqPack_Folder) ((DefaultMutableTreeNode) tp.getParentPath().getLastPathComponent()).getUserObject();
                    SqPack_File file = (SqPack_File) obj;

                    path = folder.getName() + "/" + file.getName();
                }
                StringSelection selection = new StringSelection(path);
                Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
                clip.setContents(selection, selection);
            }
        });
        contextMenu.add(copyPath);
        this.add(contextMenu);
        this.getViewport().add(fileTree);
    }

    public void addTreeSelectionListener(TreeSelectionListener l) {
        fileTree.addTreeSelectionListener(l);
    }

    public void fileOpened(SqPack_IndexFile index) {

        if (index.hasNoFolders()) {
            SqPack_Folder fakefolder = index.getPackFolders()[0];
            Arrays.sort(fakefolder.getFiles(), fileComparator);
            for (int j = 0; j < fakefolder.getFiles().length; j++)
                root.add(new DefaultMutableTreeNode(fakefolder.getFiles()[j]));
        } else {
            Arrays.sort(index.getPackFolders(), folderComparator);

            for (int i = 0; i < index.getPackFolders().length; i++) {
                SqPack_Folder folder = index.getPackFolders()[i];

                DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder);

                Arrays.sort(folder.getFiles(), fileComparator);

                for (int j = 0; j < folder.getFiles().length; j++)
                    folderNode.add(new DefaultMutableTreeNode(folder.getFiles()[j]));

                root.add(folderNode);
            }
        }

        ((DefaultTreeModel) fileTree.getModel()).reload();
    }

    public void fileClosed() {
        root.removeAllChildren();
        ((DefaultTreeModel) fileTree.getModel()).reload();
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
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private class TreeRenderer extends DefaultTreeCellRenderer {

        private final Icon fileIcon =
                (Icon) UIManager.get("FileView.fileIcon");
        private final Icon folderIcon =
                (Icon) UIManager.get("FileView.directoryIcon");

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            setTextNonSelectionColor(Color.BLACK);

            if (node.getUserObject() instanceof SqPack_Folder) //FOLDER
            {
                SqPack_Folder folder = (SqPack_Folder) node.getUserObject();

                value = folder.getName();

                setOpenIcon(getDefaultOpenIcon());
                setClosedIcon(getDefaultClosedIcon());
            } else if (node.getUserObject() instanceof SqPack_File) //FILE
            {
                SqPack_File file = (SqPack_File) node.getUserObject();
	        	
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

            super.getTreeCellRendererComponent(
                    tree, value, sel, exp, leaf, row, hasFocus);

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

        return (obj instanceof SqPack_Folder);
    }

    public ArrayList<SqPack_File> getSelectedFiles() {
        ArrayList<SqPack_File> selectedFiles = new ArrayList<SqPack_File>();
        TreePath[] selectedPaths = fileTree.getSelectionPaths();

        if (selectedPaths == null)
            return selectedFiles;

        for (TreePath tp : selectedPaths) {
            Object obj = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();
            if (obj == null)
                continue;
            if (obj instanceof SqPack_Folder) {
                int children = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getChildCount();
                for (int i = 0; i < children; i++) {
                    SqPack_File file = (SqPack_File) ((DefaultMutableTreeNode) ((DefaultMutableTreeNode) tp.getLastPathComponent()).getChildAt(i)).getUserObject();
                    if (!selectedFiles.contains(file))
                        selectedFiles.add(file);
                }
            }
            if (obj instanceof SqPack_File)
                selectedFiles.add((SqPack_File) obj);
        }

        return selectedFiles;
    }

    public void select(long offset) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) fileTree.getModel().getRoot();
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.getUserObject() instanceof SqPack_File) {
                SqPack_File file = (SqPack_File) node.getUserObject();
                if (offset == file.getOffset()) {
                    fileTree.setSelectionPath(new TreePath(node.getPath()));
                }
            }
        }
    }


    private final Comparator<SqPack_Folder> folderComparator = new Comparator<SqPack_Folder>() {

        @Override
        public int compare(SqPack_Folder o1, SqPack_Folder o2) {

            return o1.getName().compareTo(o2.getName());
        }
    };

    private final Comparator<SqPack_File> fileComparator = new Comparator<SqPack_File>() {

        @Override
        public int compare(SqPack_File o1, SqPack_File o2) {

            return o1.getName().compareTo(o2.getName());
        }
    };


}

