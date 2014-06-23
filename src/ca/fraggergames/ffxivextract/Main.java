package ca.fraggergames.ffxivextract;

import javax.swing.UIManager;

import ca.fraggergames.ffxivextract.gui.FileManagerWindow;
import ca.fraggergames.ffxivextract.models.SCD_File;

public class Main {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileManagerWindow fileMan = new FileManagerWindow(Constants.APPNAME);
		fileMan.setVisible(true);		
		
		try{
			new SCD_File("F:\\Coding\\workspace3\\FFXIV_Extractor\\src\\res\\0E587EC7.scd");
		}
		catch (Exception e)
		{}
	}

}
