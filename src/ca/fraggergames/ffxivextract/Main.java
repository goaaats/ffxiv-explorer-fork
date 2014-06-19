package ca.fraggergames.ffxivextract;

import javax.swing.UIManager;

import ca.fraggergames.ffxivextract.gui.FileManagerWindow;

public class Main {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileManagerWindow fileMan = new FileManagerWindow(Constants.APPNAME);
		fileMan.setVisible(true);		
	}

}
