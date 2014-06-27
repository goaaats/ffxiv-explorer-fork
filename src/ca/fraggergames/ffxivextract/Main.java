package ca.fraggergames.ffxivextract;

import javax.swing.UIManager;

import ca.fraggergames.ffxivextract.gui.FileManagerWindow;

public class Main {

	public static void main(String[] args) {
		
		//Debug Mode
		if (args.length >= 2 && (args[1].equals("-debug") || args[1].equals("-d")))
		{
			System.out.println("Debug Mode ON");
			Constants.DEBUG = true;
		}
		
		//Set to Windows Look & Feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Open the File Manager Window
		FileManagerWindow fileMan = new FileManagerWindow(Constants.APPNAME);
		fileMan.setVisible(true);				
	}

}
