package ca.fraggergames.ffxivextract.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import ca.fraggergames.ffxivextract.models.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_Folder;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;

@SuppressWarnings("serial")
public class ExplorerPanel extends JScrollPane {

	JTree fileTree;
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
	JScrollPane scroller;

	public ExplorerPanel() {
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

		this.getViewport().add(fileTree);		
	}

	public void addTreeSelectionListener(TreeSelectionListener l)
	{
		fileTree.addTreeSelectionListener(l);
	}
	
	public void fileOpened(SqPack_IndexFile index) {
		
		if (index.hasNoFolders())
		{
			SqPack_Folder fakefolder = index.getPackFolders()[0];
			
			for (int j = 0; j < fakefolder.getFiles().length; j++) 				
				root.add(new DefaultMutableTreeNode(fakefolder.getFiles()[j]));			
			
		}
		else
		{
			for (int i = 0; i < index.getPackFolders().length; i++) {
				SqPack_Folder folder = index.getPackFolders()[i];
		
				DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder);
		
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
	
	private static class TreeRenderer extends DefaultTreeCellRenderer {

	    private static final Icon fileIcon =
	        (Icon) UIManager.get("FileView.fileIcon");
	    private static final Icon folderIcon =
		        (Icon) UIManager.get("FileView.directoryIcon");
	    
	    @Override
	    public Component getTreeCellRendererComponent(JTree tree, Object value,
	        boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
	        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
	        
	        if (node.getUserObject() instanceof SqPack_Folder) //FOLDER
	        {
	        	SqPack_Folder folder = (SqPack_Folder) node.getUserObject();
	        	value = String.format("%08X", folder.getId() & 0xFFFFFFFF);
	        	setOpenIcon(getDefaultOpenIcon());
	            setClosedIcon(getDefaultClosedIcon());
	        }
	        else if (node.getUserObject() instanceof SqPack_File) //FILE
	        {
	        	SqPack_File file = (SqPack_File) node.getUserObject();
	        	value =  String.format("%X", file.getId() & 0xFFFFFFFF); 
	        	setLeafIcon(fileIcon);
	        	setLeafIcon(fileIcon);
	        }
	        else //ROOT
	        {
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
}
