package ca.fraggergames.ffxivextract.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.fraggergames.ffxivextract.Constants;

public class AboutWindow extends JFrame {

	JPanel aboutWindow = new JPanel();
	JPanel container = new JPanel();
	JLabel appname = new JLabel(Constants.APPNAME);
	JLabel author = new JLabel("By Magis Luagis on Excalibur");
	JLabel version = new JLabel("Version: " + Constants.VERSION);
	JLabel gitcommit = new JLabel("Git Commit: " + Constants.COMMIT.substring(0, 5));
	JLabel lol = new JLabel("(Sorry, this was last minute)");
	
	JLabel magisImage = new JLabel();
	
	public AboutWindow() {
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(480, 300);
		this.setTitle("About " + Constants.APPNAME);
		
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		
		ClassLoader cldr = this.getClass().getClassLoader();
		URL imageURL = getClass().getResource("/res/magis.jpeg");
		ImageIcon image = new ImageIcon(imageURL);
		magisImage.setIcon(image);
				
		container.add(appname);
		container.add(author);
		container.add(version);
		container.add(gitcommit);		
		container.add(lol);
		
		aboutWindow.setLayout(new BorderLayout());
		aboutWindow.add(container, BorderLayout.LINE_START);
		aboutWindow.add(magisImage, BorderLayout.LINE_END);
		getContentPane().add(aboutWindow);
	}
	
}

