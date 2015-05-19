package ca.fraggergames.ffxivextract.gui.modelviewer;

import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.fraggergames.ffxivextract.gui.components.EXDF_View;
import ca.fraggergames.ffxivextract.helpers.Utils;
import ca.fraggergames.ffxivextract.models.EXHF_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;

public class ModelViewMonsters extends JPanel {
	
	ModelViewerWindow parent;

	ModelCharaEntry[] entries;
	
	JList lstMonsters;	
	
	public ModelViewMonsters() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.NORTH);
		
		JLabel lblInfoAndControls = new JLabel("Info and controls go here");
		panel_2.add(lblInfoAndControls);
		
		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.WEST);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.WEST);
		
		lstMonsters = new JList();
		
		scrollPane.setViewportView(lstMonsters);
	}

	private void loadMonsters() throws FileNotFoundException, IOException
	{
		SqPack_IndexFile indexFile = new SqPack_IndexFile(parent.getSqpackPath() + "0a0000.win32.index");
		EXHF_File exhfFile = new EXHF_File(indexFile.extractFile(Utils.getOffset("exd/modelchara.exh", indexFile), null));
		EXDF_View view = new EXDF_View(indexFile, "", exhfFile);
		
		entries = new ModelCharaEntry[view.getTable().getRowCount()];
		
		for (int i = 0; i < view.getTable().getRowCount(); i++){
			entries[i] = new ModelCharaEntry((Integer)view.getTable().getValueAt(i, 1), (Integer)view.getTable().getValueAt(i, 2), (Integer)view.getTable().getValueAt(i, 3), (Integer)view.getTable().getValueAt(i, 4));
		}
				
		lstMonsters.setModel(new AbstractListModel() {			
			public int getSize() {
				return entries.length;
			}
			public String getElementAt(int index) {
				return "Monster: " + index;
			}
		});
				
	}
	
}
