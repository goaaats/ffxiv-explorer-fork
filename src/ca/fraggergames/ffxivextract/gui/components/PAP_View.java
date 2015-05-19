package ca.fraggergames.ffxivextract.gui.components;

import java.awt.BorderLayout;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import ca.fraggergames.ffxivextract.models.PAP_File;

public class PAP_View extends JPanel {

	PAP_File currentPAP;
	JList lstAnimationNames;
	
	public PAP_View(PAP_File file) {
		setBorder(new TitledBorder(null, "Animations", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		currentPAP = file;
		
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		lstAnimationNames = new JList();
		scrollPane.setViewportView(lstAnimationNames);
		lstAnimationNames.setModel(new AbstractListModel() {
			
			public int getSize() {
				return currentPAP.getAnimationNames().length;
			}
			public Object getElementAt(int index) {
				return currentPAP.getAnimationNames()[index];
			}
		});
		
		
		
	}
	
}
