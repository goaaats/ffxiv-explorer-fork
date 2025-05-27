package com.fragmenterworks.ffxivextract.gui.components;

import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.sqpack.index.IIndexUpdateListener;
import com.fragmenterworks.ffxivextract.models.sqpack.index.SqPackIndexFile;
import com.fragmenterworks.ffxivextract.models.sqpack.model.SqPackFile;
import com.fragmenterworks.ffxivextract.models.sqpack.model.SqPackFolder;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ExplorerPanel_View extends JScrollPane implements MouseListener, IIndexUpdateListener, TreeWillExpandListener {
    private boolean enableHashUpdate = true;

    private final JTree fileTree;
    private final RootFolder root = new RootFolder();

    PopupMenu contextMenu;

    public ExplorerPanel_View() {
        setBackground(Color.WHITE);
        fileTree = new JTree(root) {

        };

        var renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
                setTextNonSelectionColor(Color.BLACK);

                if (value instanceof SelfRenderable)
                    value = ((SelfRenderable) value).getNodeText();
                else
                    value = "No File Loaded";

                super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
                return this;
            }
        };
        var fileIcon = (Icon) UIManager.get("FileView.fileIcon");
        var folderIcon = (Icon) UIManager.get("FileView.directoryIcon");
        renderer.setLeafIcon(fileIcon);
        renderer.setOpenIcon(folderIcon);
        renderer.setClosedIcon(folderIcon);
        fileTree.setCellRenderer(renderer);

        fileTree.setShowsRootHandles(false);
        fileTree.addMouseListener(this);
        fileTree.addTreeWillExpandListener(this);

        contextMenu = new PopupMenu();
        MenuItem copyPath = new MenuItem("Copy full path");
        copyPath.addActionListener(e -> {
            TreePath[] selectedPaths = fileTree.getSelectionPaths();

            if (selectedPaths == null)
                return;

            var strings = new ArrayList<String>();
            for (var selectedPath : selectedPaths) {
                var selectedObject = selectedPath.getLastPathComponent();
                if (selectedObject instanceof SelfRenderable) {
                    var path = ((SelfRenderable) selectedObject).getFullPath();
                    if (path != null)
                        strings.add(path);
                }
            }

            StringSelection selection = new StringSelection(String.join("\n", strings));
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
        root.addIndex(index, (DefaultTreeModel) fileTree.getModel());
        if (root.getChildCount() == 1)
            fileTree.expandPath(new TreePath(((VirtualFolder) root.getChildAt(0)).getPath()));
    }

    public void fileClosed(SqPackIndexFile index) {
        root.removeIndex(index, (DefaultTreeModel) fileTree.getModel());
    }

    public void setEnableHashUpdate(boolean enableHashUpdate) {
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
    public void onIndexUpdate(SqPackIndexFile indexFile) {
        Utils.getGlobalLogger().debug("Index update");

        if (!enableHashUpdate) {
            Utils.getGlobalLogger().debug("Ignored");
            return;
        }

        root.removeIndex(indexFile, (DefaultTreeModel) fileTree.getModel());
        root.addIndex(indexFile, (DefaultTreeModel) fileTree.getModel());
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) {
        var item = event.getPath().getLastPathComponent();
        if (item instanceof VirtualFolder) {
            var folder = (VirtualFolder) item;
            folder.populate();
            if (folder.shouldAutoExpandChildren()) {
                for (var i = 0; i < folder.getChildCount(); i++) {
                    final var child = folder.getChildAt(i);
                    if (child instanceof UnknownVirtualFolder)
                        continue;
                    if (child instanceof VirtualFolder)
                        SwingUtilities.invokeLater(() -> fileTree.expandPath(new TreePath(((VirtualFolder) child).getPath())));
                }
            }
        }
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) {

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

    public ArrayList<SqPackIndexFile> getAllIndexFiles() {
        return root.getAllIndexFiles();
    }

    public ArrayList<SqPackIndexFile> getSelectedIndexFiles() {
        var selectedIndices = new HashSet<SqPackIndexFile>();
        TreePath[] selectedPaths = fileTree.getSelectionPaths();

        if (selectedPaths == null)
            return new ArrayList<>(selectedIndices);

        for (TreePath tp : selectedPaths) {
            var obj = tp.getLastPathComponent();
            while (obj != null) {
                if (!(obj instanceof DefaultMutableTreeNode))
                    break;

                var node = (DefaultMutableTreeNode) obj;
                obj = node.getParent();

                if (!(node instanceof VirtualFolder))
                    continue;

                var folder = (VirtualFolder) node;
                var indexFile = folder.getIndexFile();
                if (indexFile != null)
                    selectedIndices.add(indexFile);
            }
        }
        return new ArrayList<>(selectedIndices);
    }

    public int getSelectionCount() {
        return fileTree.getSelectionCount();
    }

    public SqPackFile getSelectedSingleFile() {
        var selectedPaths = fileTree.getSelectionPaths();
        if (selectedPaths == null || selectedPaths.length != 1)
            return null;
        if (selectedPaths[0].getLastPathComponent() instanceof VirtualFile)
            return ((VirtualFile) selectedPaths[0].getLastPathComponent()).getFile();
        return null;
    }

    public ArrayList<SqPackFile> getSelectedFiles() {
        ArrayList<TreeNode> remainingNodes;
        {
            var selectedPaths = fileTree.getSelectionPaths();
            if (selectedPaths == null)
                return new ArrayList<>();

            var tmp = Arrays.stream(selectedPaths)
                    .map(x -> (TreeNode) x.getLastPathComponent())
                    .collect(Collectors.toList());
            remainingNodes = tmp instanceof ArrayList<?>
                    ? (ArrayList<TreeNode>) tmp
                    : new ArrayList<>(tmp);
        }

        var files = new ArrayList<SqPackFile>();

        while (!remainingNodes.isEmpty()) {
            var node = remainingNodes.get(remainingNodes.size() - 1);
            remainingNodes.remove(remainingNodes.size() - 1);

            if (node instanceof VirtualFile) {
                var file = (VirtualFile) node;
                files.add(file.getFile());
            } else {
                if (node instanceof VirtualFolder) {
                    var folder = (VirtualFolder) node;
                    folder.populate();
                }

                var childCount = node.getChildCount();
                remainingNodes.ensureCapacity(remainingNodes.size() + childCount);
                for (var i = 0; i < childCount; i++)
                    remainingNodes.add(node.getChildAt(i));
            }
        }

        return files;
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

    private interface SelfRenderable {
        String getNodeText();

        String getFullPath();
    }

    private static class RootFolder extends DefaultMutableTreeNode implements SelfRenderable {
        public void addIndex(SqPackIndexFile indexFile, DefaultTreeModel model) {
            var indexNode = new VirtualFolder(indexFile.getPath(), Paths.get(indexFile.getPath()).getFileName().toString());
            indexNode.setUserObject(indexFile);

            var children = new ArrayList<VirtualFolder>(getChildCount());
            for (var i = 0; i < getChildCount(); i++)
                children.add((VirtualFolder) getChildAt(i));

            var pos = Collections.binarySearch(children, indexNode);
            if (pos >= 0)
                return;  // don't re-open what's already opened
            pos = ~pos;

            var unknownFolder = new UnknownVirtualFolder();

            for (var folder : indexFile.getFolders()) {
                var name = folder.getName();
                if (name.startsWith("~"))
                    unknownFolder.getOrCreateSubfolder(name).setUserObject(folder);
                else
                    indexNode.getOrCreateSubfolder(name).setUserObject(folder);
            }

            for (var file : indexFile.getFiles()) {
                // If a file has a parent, then it will be lazy-expanded later on.
                if (file.getParent() != null)
                    continue;

                var name = file.getName();
                if (name.startsWith("~")) {
                    unknownFolder.addFile(file);
                    continue;
                }

                var off = name.lastIndexOf('/');
                if (off == -1) {
                    unknownFolder.addFile(file);
                    continue;
                }

                var path = name.substring(0, off);
                unknownFolder.getOrCreateSubfolder(path).addFile(file);
            }

            if (!unknownFolder.subfolders.isEmpty() || !unknownFolder.files.isEmpty())
                indexNode.subfolders.put(unknownFolder.name, unknownFolder);

            insert(indexNode, pos);
            model.nodesWereInserted(this, new int[]{pos});
        }

        public void removeIndex(SqPackIndexFile indexFile, DefaultTreeModel model) {
            var removedIndices = new ArrayList<Integer>();
            var removedObjects = new ArrayList<>();
            for (var i = getChildCount() - 1; i >= 0; --i) {
                if (((VirtualFolder) getChildAt(i)).getUserObject() == indexFile) {
                    removedObjects.add(getChildAt(i));
                    removedIndices.add(i);
                    remove(i);
                }
            }
            model.nodesWereRemoved(this,
                    removedIndices.stream().mapToInt(Integer::intValue).toArray(),
                    removedObjects.toArray());
        }

        @Override
        public String getNodeText() {
            if (getChildCount() == 0)
                return "No file loaded";
            else if (getChildCount() == 1)
                return "1 file loaded";
            else
                return getChildCount() + " files opened";
        }

        @Override
        public String getFullPath() {
            return null;
        }

        public ArrayList<SqPackIndexFile> getAllIndexFiles() {
            var children = new ArrayList<SqPackIndexFile>(getChildCount());
            for (var i = 0; i < getChildCount(); i++)
                children.add(((VirtualFolder) getChildAt(i)).getIndexFile());
            return children;
        }
    }

    private static class VirtualFolder extends DefaultMutableTreeNode implements SelfRenderable, Comparable<VirtualFolder> {
        public final String name;
        public final String displayName;
        public HashMap<String, VirtualFolder> subfolders = new HashMap<>();
        public ArrayList<VirtualFile> files = new ArrayList<>();

        private boolean _populated = false;

        public VirtualFolder(String name, String displayName) {
            super(null, false);
            this.name = name;
            this.displayName = displayName;
        }

        public SqPackFolder getFolder() {
            return userObject instanceof SqPackFolder ? (SqPackFolder) userObject : null;
        }

        public SqPackIndexFile getIndexFile() {
            return userObject instanceof SqPackIndexFile ? (SqPackIndexFile) userObject : null;
        }

        public VirtualFolder getOrCreateSubfolder(String path) {
            var sepOffset = path.indexOf('/');
            var name = sepOffset == -1 ? path : path.substring(0, sepOffset);
            var subfolder = subfolders.get(name);
            if (subfolder == null)
                subfolders.put(name, subfolder = new VirtualFolder(name, name));
            if (sepOffset != -1)
                subfolder = subfolder.getOrCreateSubfolder(path.substring(sepOffset + 1));
            setAllowsChildren(true);
            return subfolder;
        }

        public void addFile(SqPackFile file) {
            files.add(new VirtualFile(file));
            setAllowsChildren(true);
        }

        public void populate() {
            if (_populated)
                return;

            var sortedSubfolders = new ArrayList<>(subfolders.values());
            Collections.sort(sortedSubfolders);
            for (var subfolder : sortedSubfolders) {
                add(subfolder);

                var hasChildren = subfolder.getAllowsChildren();
                if (!hasChildren)
                    hasChildren = !subfolder.files.isEmpty();
                if (!hasChildren)
                    hasChildren = !subfolder.subfolders.isEmpty();
                if (!hasChildren) {
                    var realFolder = subfolder.getFolder();
                    if (realFolder != null)
                        hasChildren = !realFolder.getFiles().isEmpty();
                }
                subfolder.setAllowsChildren(hasChildren);
            }

            var folder = getFolder();
            if (folder != null) {
                files.ensureCapacity(files.size() + folder.getFiles().size());
                for (var file : folder.getFiles())
                    files.add(new VirtualFile(file));
            }

            Collections.sort(files);
            for (var file : files)
                add(file);

            _populated = true;
        }

        public boolean shouldAutoExpandChildren() {
            var count = subfolders.size() + files.size();
            if (count < 2)
                return true;
            if (count > 2)
                return false;
            return subfolders.values().stream().anyMatch(x -> x instanceof UnknownVirtualFolder);
        }

        @Override
        public int compareTo(VirtualFolder o) {
            return name.compareTo(o.name);
        }

        @Override
        public boolean isLeaf() {
            return subfolders.isEmpty() && files.isEmpty() && (getFolder() == null || getFolder().getFiles().isEmpty());
        }

        @Override
        public String getNodeText() {
            return displayName;
        }

        @Override
        public String getFullPath() {
            if (getParent() instanceof VirtualFolder) {
                var parentPath = ((VirtualFolder) getParent()).getFullPath();
                if (parentPath == null)
                    return name;
                return parentPath + "/" + name;
            }

            return name;
        }
    }

    private static class UnknownVirtualFolder extends VirtualFolder {
        public UnknownVirtualFolder() {
            super("?", "<unknown>");
        }

        @Override
        public String getFullPath() {
            if (getParent() instanceof VirtualFolder)
                return ((VirtualFolder) getParent()).getFullPath();

            return name;
        }
    }

    private static class VirtualFile extends DefaultMutableTreeNode implements SelfRenderable, Comparable<VirtualFile> {
        public VirtualFile(SqPackFile file) {
            super(file, false);
        }

        public SqPackFile getFile() {
            return (SqPackFile) userObject;
        }

        @Override
        public int compareTo(VirtualFile o) {
            return getFile().getName().compareTo(o.getFile().getName());
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public String getNodeText() {
            return getFile().getName();
        }

        @Override
        public String getFullPath() {
            var name = getFile().getName();

            if (getParent() instanceof VirtualFolder) {
                var parentPath = ((VirtualFolder) getParent()).getFullPath();
                if (parentPath == null)
                    return name;
                return parentPath + "/" + name;
            }

            return name;
        }
    }
}

