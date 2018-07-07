package com.fragmenterworks.ffxivextract.gui.modelviewer;

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

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.Strings;
import com.fragmenterworks.ffxivextract.gui.components.EXDF_View;
import com.fragmenterworks.ffxivextract.models.EXHF_File;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;

public class ModelViewerWindow extends JFrame {
	
	JFrame parent;
	Loading_Dialog dialog;
	String sqPackPath;
	SqPack_IndexFile exdIndexFile, modelIndexFile, buildingIndexFile;
	JTabbedPane tabbedPane;
	EXDF_View itemView;
	
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
					EXHF_File exhfFile = new EXHF_File(exdIndexFile.extractFile("exd/item.exh"));
					itemView = new EXDF_View(exdIndexFile, "exd/item.exh", exhfFile);
					
					tabbedPane.add("Monsters", new ModelViewerMonsters(ModelViewerWindow.this, modelIndexFile));
					tabbedPane.add("Items", new ModelViewerItems(ModelViewerWindow.this, modelIndexFile, itemView));
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
