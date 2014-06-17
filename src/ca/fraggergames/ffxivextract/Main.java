package ca.fraggergames.ffxivextract;

import java.io.IOException;
import java.rmi.server.UID;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.models.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_Folder;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;

public class Main {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileManagerWindow fileMan = new FileManagerWindow("FFXIV 2.0 Data Explorer");
		
	}

}
