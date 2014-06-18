package ca.fraggergames.ffxivextract.gui;

import javax.swing.JFrame;

import ca.fraggergames.ffxivextract.Constants;

public class AboutWindow extends JFrame {

	public AboutWindow() {
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(480, 300);
		this.setTitle("About " + Constants.APPNAME);
	}
	
}
