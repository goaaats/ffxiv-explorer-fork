package ca.fraggergames.ffxivextract.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;
import ca.fraggergames.ffxivextract.storage.CompareFile;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

@SuppressWarnings("serial")
public class ExplorerPanel_View extends JScrollPane {

	JTree fileTree;
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("No File Loaded");
	JScrollPane scroller;

	CompareFile currentCompareFile;
	
	public ExplorerPanel_View() {
		setBackground(Color.WHITE);
		fileTree = new JTree(root) {
			
			public boolean getScrollableTracksViewportWidth() {
				return getPreferredSize().width < getParent().getWidth();
			}
			public boolean getScrollableTracksViewportHeight() {
				return getPreferredSize().height < getParent().getHeight();
			}
		};
		
		fileTree.setCellRenderer(new TreeRenderer());
		fileTree.setShowsRootHandles(false);
		
		this.getViewport().add(fileTree);		
	}

	public void addTreeSelectionListener(TreeSelectionListener l)
	{
		fileTree.addTreeSelectionListener(l);
	}
	
	public void fileOpened(SqPack_IndexFile index, CompareFile compareFile) {		
		
		currentCompareFile = compareFile;
		
		if (index.hasNoFolders())
		{
			SqPack_Folder fakefolder = index.getPackFolders()[0];
			
			for (int j = 0; j < fakefolder.getFiles().length; j++)
			{
				Arrays.sort(fakefolder.getFiles(), fileComparator);
				root.add(new DefaultMutableTreeNode(fakefolder.getFiles()[j]));	
			}
			
		}
		else
		{
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
	        }
	        else if (node.getUserObject() instanceof SqPack_File) //FILE
	        {
	        	SqPack_File file = (SqPack_File) node.getUserObject();
	        	
	        	if (currentCompareFile != null && currentCompareFile.isNewFile(file.getId()))
	        		setTextNonSelectionColor(new Color(0,150,0));
	        	else
	        		setTextNonSelectionColor(Color.BLACK);
	        	
	        	value = file.getName();
	        	
	        	setLeafIcon(fileIcon);
	        	setLeafIcon(fileIcon);
	        }
	        else //ROOT
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
	
	public boolean folderOnlySelected()
	{
		ArrayList<SqPack_File> selectedFiles = new ArrayList<SqPack_File>();
		TreePath[] selectedPaths = fileTree.getSelectionPaths();
		
		if (selectedPaths == null)
			return false;
		
		return selectedPaths.length == 1 && ((DefaultMutableTreeNode) selectedPaths[0].getLastPathComponent()).getUserObject() instanceof SqPack_Folder;
	}
	
	public ArrayList<SqPack_File> getSelectedFiles()
	{
		ArrayList<SqPack_File> selectedFiles = new ArrayList<SqPack_File>();
		TreePath[] selectedPaths = fileTree.getSelectionPaths();
		
		if (selectedPaths == null)
			return selectedFiles;
		
		for (TreePath tp : selectedPaths)
		{
			Object obj = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();
			if (obj == null)
				continue;
			if (obj instanceof SqPack_Folder)
			{
				int children = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getChildCount();
				for (int i = 0; i < children; i++)
				{
					SqPack_File file = (SqPack_File) ((DefaultMutableTreeNode)((DefaultMutableTreeNode) tp.getLastPathComponent()).getChildAt(i)).getUserObject();
					if (!selectedFiles.contains(file))
						selectedFiles.add(file);
				}
			}
			if (obj instanceof SqPack_File)
				selectedFiles.add((SqPack_File)obj);
		}
		
		return selectedFiles;
	}
	
	public void select(long offset)
	{		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) fileTree.getModel().getRoot();		
		Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
	    while (e.hasMoreElements()) {
	        DefaultMutableTreeNode node = e.nextElement();
	        if (node.getUserObject() instanceof SqPack_File)
	        {
	        	SqPack_File file = (SqPack_File)node.getUserObject();
		        if (offset == file.getOffset()) {
		            fileTree.setSelectionPath(new TreePath(node.getPath()));
		        }
	        }
	    }
	}
	
	Comparator<SqPack_Folder> folderComparator =  new Comparator<SqPack_Folder>() {
		
		@Override
		public int compare(SqPack_Folder o1, SqPack_Folder o2) {
		
			return o1.getName().compareTo(o2.getName());
		}
	};
	
	Comparator<SqPack_File> fileComparator = new Comparator<SqPack_File>() {
		
		@Override
		public int compare(SqPack_File o1, SqPack_File o2) {
			
			return o1.getName().compareTo(o2.getName());
		}
	};
}

