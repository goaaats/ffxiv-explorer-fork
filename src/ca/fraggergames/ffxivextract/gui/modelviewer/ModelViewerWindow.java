package ca.fraggergames.ffxivextract.gui.modelviewer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ca.fraggergames.ffxivextract.Strings;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.io.IOException;
import java.net.URL;

public class ModelViewerWindow extends JFrame {
	
	SqPack_IndexFile modelIndexFile, buildingIndexFile;
	
	public ModelViewerWindow() {
		
		this.setTitle(Strings.DIALOG_TITLE_MODELVIEWER);
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
		setSize(800, 600);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
        try {
			modelIndexFile = new SqPack_IndexFile(getSqpackPath() + "040000.win32.index", true);
			buildingIndexFile = new SqPack_IndexFile(getSqpackPath() + "010000.win32.index", true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//tabbedPane.add("Character Builder", new ModelViewerCharacter(this, modelIndexFile));
		tabbedPane.add("Items", new ModelViewerItems(this, modelIndexFile));
		tabbedPane.add("Monsters", new ModelViewerMonsters(this, modelIndexFile));
		tabbedPane.add("Furniture", new ModelViewerFurniture(this, buildingIndexFile));
	}

	public String getSqpackPath() {
		return "E:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\";
	}

}
