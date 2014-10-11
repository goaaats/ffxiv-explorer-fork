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
				
				root.add(new DefaultMutableTreeNode(fakefolder.getFiles()[j]));	
			}
			
		}
		else
		{
			Arrays.sort(index.getPackFolders(), new Comparator<SqPack_Folder>() {
				
				@Override
				public int compare(SqPack_Folder o1, SqPack_Folder o2) {
					
					String o1Name = HashDatabase.getFolder(o1.getId());
					if (o1Name == null)
						o1Name = String.format("%X", o1.getId());
					
					String o2Name = HashDatabase.getFolder(o2.getId());
					if (o2Name == null)
						o2Name = String.format("%X", o2.getId());
					
					return o1Name.compareTo(o2Name);
				}
			});
			
			for (int i = 0; i < index.getPackFolders().length; i++) {
				SqPack_Folder folder = index.getPackFolders()[i];
		
				DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder);
		
				Arrays.sort(folder.getFiles(), new Comparator<SqPack_File>() {
		
					@Override
					public int compare(SqPack_File o1, SqPack_File o2) {
						
						String o1Name = HashDatabase.getFileName(o1.id);
						if (o1Name == null)
							o1Name = String.format("%X", o1.id);
						
						String o2Name = HashDatabase.getFileName(o2.id);
						if (o2Name == null)
							o2Name = String.format("%X", o2.id);
						
						return o1Name.compareTo(o2Name);
					}
				});
				
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
	        	
	        	String folderName = HashDatabase.getFolder(folder.getId());
	        	
	        	if (folderName != null)
	        		value = folderName;
	        	else
	        		value = String.format("%08X", folder.getId() & 0xFFFFFFFF);	     
	        		        	
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
	        	
	        	String fileName = HashDatabase.getFileName(file.getId());
	        	
	        	if (fileName != null)
	        		value = fileName;
	        	else
	        		value =  String.format("%08X", file.getId() & 0xFFFFFFFF); 
	        	
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
	
	public DefaultMutableTreeNode sort(DefaultMutableTreeNode node) {

	    //sort alphabetically
	    for(int i = 0; i < node.getChildCount() - 1; i++) {
	        DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
	        String nt;
	        
	        if (child.getUserObject() instanceof SqPack_Folder)
	        {
	        	SqPack_Folder folder = (SqPack_Folder) child.getUserObject();
	        	String folderName = HashDatabase.getFolder(folder.getId());
	        	
	        	if (folderName != null)
	        		nt = folderName;
	        	else
	        		nt = String.format("%08X", folder.getId() & 0xFFFFFFFF);	     	        	
	        }
	        else
	        {
	        	SqPack_File file = (SqPack_File) child.getUserObject();
	        	String fileName = HashDatabase.getFileName(file.getId());
	        	
	        	if (fileName != null)
	        		nt = fileName;
	        	else
	        		nt =  String.format("%08X", file.getId() & 0xFFFFFFFF); 
	        }
	        	

	        for(int j = i + 1; j <= node.getChildCount() - 1; j++) {
	            DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) node.getChildAt(j);
	            String np;
	           
	            if (prevNode.getUserObject() instanceof SqPack_Folder)
		        {
		        	SqPack_Folder folder = (SqPack_Folder) prevNode.getUserObject();
		        	String folderName = HashDatabase.getFolder(folder.getId());
		        	
		        	if (folderName != null)
		        		np = folderName;
		        	else
		        		np = String.format("%08X", folder.getId() & 0xFFFFFFFF);	
		        }
		        else
		        {
		        	SqPack_File file = (SqPack_File) prevNode.getUserObject();
		        	String fileName = HashDatabase.getFileName(file.getId());
		        	
		        	if (fileName != null)
		        		np = fileName;
		        	else
		        		np =  String.format("%08X", file.getId() & 0xFFFFFFFF); 
		        }
	            
	            if(nt.compareToIgnoreCase(np) > 0) {
	                node.insert(child, j);
	                node.insert(prevNode, i);
	            }
	        }
	        if(child.getChildCount() > 0) {
	            sort(child);
	        }
	    }	   

	    return node;

	}
}

