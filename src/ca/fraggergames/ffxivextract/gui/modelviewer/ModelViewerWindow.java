package ca.fraggergames.ffxivextract.gui.modelviewer;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

public class ModelViewerWindow extends JFrame {
	public ModelViewerWindow() {
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
	}

}
