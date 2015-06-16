package ca.fraggergames.ffxivextract.gui.modelviewer;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.Strings;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;

public class ModelViewerWindow extends JFrame {
	
	String sqPackPath;
	SqPack_IndexFile exdIndexFile, modelIndexFile, buildingIndexFile;
	
	public ModelViewerWindow(String sqPackPath) {
		
		this.setTitle(Strings.DIALOG_TITLE_MODELVIEWER);
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
		setSize(800, 600);				
		
		this.sqPackPath = sqPackPath;
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
        try {
        	exdIndexFile = new SqPack_IndexFile(getSqpackPath() + "\\game\\sqpack\\ffxiv\\0a0000.win32.index2", true);
			modelIndexFile = new SqPack_IndexFile(getSqpackPath() + "\\game\\sqpack\\ffxiv\\040000.win32.index2", true);
			buildingIndexFile = new SqPack_IndexFile(getSqpackPath() + "\\game\\sqpack\\ffxiv\\010000.win32.index2", true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//tabbedPane.add("Items", new ModelViewerItems(this, modelIndexFile));
		tabbedPane.add("Monsters", new ModelViewerMonsters(this, modelIndexFile));
		//tabbedPane.add("Furniture", new ModelViewerFurniture(this, buildingIndexFile));
		
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

}
