package ca.fraggergames.ffxivextract.gui.modelviewer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ca.fraggergames.ffxivextract.Strings;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.net.URL;

public class ModelViewerWindow extends JFrame {
	
	public ModelViewerWindow() {
		
		this.setTitle(Strings.DIALOG_TITLE_SEARCH);
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
	}

	public String getSqpackPath() {
		return "";
	}

}
