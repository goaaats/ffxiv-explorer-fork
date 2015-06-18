package ca.fraggergames.ffxivextract.gui.modelviewer;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.Strings;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;

public class ModelViewerWindow extends JFrame {
	
	JFrame parent;
	Loading_Dialog dialog;
	String sqPackPath;
	SqPack_IndexFile exdIndexFile, modelIndexFile, buildingIndexFile;
	JTabbedPane tabbedPane;
	public ModelViewerWindow(JFrame parent, String sqPackPath) {
		
		this.setTitle(Strings.DIALOG_TITLE_MODELVIEWER);
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
		setSize(800, 600);				
		
		this.parent = parent;
		this.sqPackPath = sqPackPath;
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		       
	}

	public SqPack_IndexFile getExdIndexFile()
	{
		return exdIndexFile;
	}
	
	public SqPack_IndexFile getModelIndexFile()
	{
		return modelIndexFile;
	}
	
	public SqPack_IndexFile getBuildingIndexFile()
	{
		return buildingIndexFile;
	}
	
	public String getSqpackPath() {
		return sqPackPath;
	}

	public void beginLoad()
	{
		OpenIndexTask task = new OpenIndexTask();				 
		dialog = new Loading_Dialog(ModelViewerWindow.this, 4);		
		task.execute();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
	
	class OpenIndexTask extends SwingWorker<Void, Void>{
		
		public OpenIndexTask() {	
					
		}

		@Override
		protected Void doInBackground() throws Exception {
				
			
			try {
					dialog.nextFile(0, "..\\game\\sqpack\\ffxiv\\0a0000.win32.index");
		        	exdIndexFile = new SqPack_IndexFile(getSqpackPath() + "\\game\\sqpack\\ffxiv\\0a0000.win32.index", true);
		        	dialog.nextFile(1, "..\\game\\sqpack\\ffxiv\\040000.win32.index");
					modelIndexFile = new SqPack_IndexFile(getSqpackPath() + "\\game\\sqpack\\ffxiv\\040000.win32.index", true);
		        	dialog.nextFile(2, "..\\game\\sqpack\\ffxiv\\010000.win32.index");
					buildingIndexFile = new SqPack_IndexFile(getSqpackPath() + "\\game\\sqpack\\ffxiv\\010000.win32.index", true);
					dialog.nextFile(3, "Setting up lists...");
					tabbedPane.add("Monsters", new ModelViewerMonsters(ModelViewerWindow.this, modelIndexFile));
					tabbedPane.add("Items", new ModelViewerItems(ModelViewerWindow.this, modelIndexFile));
					tabbedPane.add("Furniture", new ModelViewerFurniture(ModelViewerWindow.this, buildingIndexFile));
				} catch (IOException e1) {
					e1.printStackTrace();
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
