package ca.fraggergames.ffxivextract;

import javax.swing.UIManager;

import ca.fraggergames.ffxivextract.gui.FileManagerWindow;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
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
			SCD_File file = new SCD_File("C:\\Users\\Filip\\Desktop\\windy_meadows.dat");
			LERandomAccessFile out = new LERandomAccessFile("C:\\Users\\Filip\\Desktop\\windy_meadows.ogg", "rw");
			out.write(file.getData());
			out.close();
		}
		catch (Exception e)
		{}
	}

}
