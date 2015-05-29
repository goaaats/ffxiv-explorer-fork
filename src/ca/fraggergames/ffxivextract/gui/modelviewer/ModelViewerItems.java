package ca.fraggergames.ffxivextract.gui.modelviewer;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jogamp.opengl.util.FPSAnimator;

import ca.fraggergames.ffxivextract.gui.components.EXDF_View;
import ca.fraggergames.ffxivextract.gui.components.ModelRenderer;
import ca.fraggergames.ffxivextract.gui.components.OpenGL_View;
import ca.fraggergames.ffxivextract.helpers.SparseArray;
import ca.fraggergames.ffxivextract.helpers.Utils;
import ca.fraggergames.ffxivextract.models.EXHF_File;
import ca.fraggergames.ffxivextract.models.Model;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.border.EmptyBorder;

public class ModelViewerItems extends JPanel {
	
	ModelViewerWindow parent;
	
	ArrayList<ModelItemEntry> entries[] = new ArrayList[22];
	
	SparseArray<String> slots = new SparseArray<String>();
	
	SparseArray<String> charIds = new SparseArray<String>();

	int currentBody = -1;
	int currentCategory = -1;
	
	OpenGL_View view3D;
	JList lstItems;	
	JComboBox cmbBodyStyle;
	JComboBox cmbCategory;
	FPSAnimator animator;
	
	ModelRenderer renderer;
	
	private boolean leftMouseDown = false;
	private boolean rightMouseDown = false;
	
	private int currentLoD = 0;
	private int lastOriginX, lastOriginY;
	private int lastX, lastY;	
	
	SqPack_IndexFile modelIndexFile;
	
	public ModelViewerItems(ModelViewerWindow parent, SqPack_IndexFile modelIndex) {
		
		this.parent = parent;
		this.modelIndexFile = modelIndex;
				
		//Fill the Equipment Slots
		slots.append(-1, "--Equipment Slot--");		
		slots.append(1, "One-Handed Weapon");
		slots.append(13, "Two-Handed Weapon");
		slots.append(2, "Offhand");
		slots.append(3, "Head");
		slots.append(4, "Body");
		slots.append(5, "Hands");
		slots.append(7, "Legs");
		slots.append(8, "Feet");
		slots.append(9, "Earings");
		slots.append(10, "Necklace");
		slots.append(11, "Wrists");
		slots.append(12, "Rings");
		
		slots.append(15, "Body + Head");
		slots.append(16, "All - Head");
		//slots.append(17, "Soulstone");
		slots.append(18, "Legs + Feet");
		slots.append(19, "All");
		slots.append(20, "Body + Hands");
		slots.append(21, "Body + Legs + Feet");
		
		slots.append(0, "Non-Equipment");
		
		//Fill the char ids
		charIds.append(-1, "--Body Style--");		
		charIds.append(1, "Midlander Male");
		charIds.append(2, "Midlander  Female");
		charIds.append(3, "Highlander Male");
		charIds.append(4, "Highlander Female");
		charIds.append(5, "Elezen Male");
		charIds.append(6, "Elezen Female");
		charIds.append(7, "Miqo'te Male");
		charIds.append(8, "Miqo'te Female");
		charIds.append(9, "Roegadyn Male");
		charIds.append(10, "Roegadyn  Female");
		charIds.append(11, "Lalafell Male");
		charIds.append(12, "Lalafell Female");
		
		for (int i = 0; i < entries.length; i++)
			entries[i] = new ArrayList<ModelItemEntry>();
		
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
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Items", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel, BorderLayout.WEST);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_4 = new JPanel();
		panel.add(panel_4, BorderLayout.NORTH);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.Y_AXIS));
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new EmptyBorder(2, 1, 0, 1));
		panel_4.add(panel_5);
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));
		
		cmbBodyStyle = new JComboBox();
		panel_5.add(cmbBodyStyle);
		
		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new EmptyBorder(2, 1, 2, 1));
		panel_4.add(panel_6);
		panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));
		
		cmbCategory = new JComboBox();
		panel_6.add(cmbCategory);
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		
		lstItems = new JList();
		
		scrollPane.setViewportView(lstItems);

		GLProfile glProfile = GLProfile.getDefault();
		GLCapabilities glcapabilities = new GLCapabilities( glProfile );
        final GLCanvas glcanvas = new GLCanvas( glcapabilities );
        renderer = new ModelRenderer();
        glcanvas.addGLEventListener(renderer);
        animator = new FPSAnimator(glcanvas, 30);
        animator.start();
        glcanvas.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if (leftMouseDown)
				{					
					renderer.pan((e.getX() - lastX), (e.getY() - lastY));
					lastX = e.getX();
					lastY = e.getY();
				}
				if (rightMouseDown)
				{
					renderer.rotate(e.getX() - lastX, e.getY() - lastY);
					lastX = e.getX();
					lastY = e.getY();
				}
			}
		});
        glcanvas.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1)				
					leftMouseDown = false;				
				if (e.getButton() == MouseEvent.BUTTON3)
					rightMouseDown = false;
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1)	
				{
					leftMouseDown = true;
					lastOriginX = e.getX();
					lastOriginY = e.getY();
					lastX = lastOriginX;
					lastY = lastOriginY;
				}
				if (e.getButton() == MouseEvent.BUTTON3)
				{
					rightMouseDown = true;
					lastOriginX = e.getX();
					lastOriginY = e.getY();
					lastX = lastOriginX;
					lastY = lastOriginY;
				}
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int notches = e.getWheelRotation();		
				renderer.zoom(-notches);				
			}
		});
        
        try {
        	loadItems();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}    
        
        for (int i = 0; i < charIds.size(); i++)
        	cmbBodyStyle.addItem(charIds.valueAt(i));
        
        cmbBodyStyle.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
		          int selected = cmbBodyStyle.getSelectedIndex();		          
		          currentBody = charIds.keyAt(selected);
		          
		          if (currentCategory != -1 && lstItems.getSelectedIndex() != 0)
		        	  loadModel(-1, lstItems.getSelectedIndex());
				}
			}
        });
        
        for (int i = 0; i < slots.size(); i++)
        	cmbCategory.addItem(slots.valueAt(i));
        
        cmbCategory.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
		          int selected = cmbCategory.getSelectedIndex();
		          
		          currentCategory = slots.keyAt(selected);
		          
		          ((ItemsListModel)lstItems.getModel()).refresh();
				}
			}
        });
        
        //Add all the slots        
        lstItems.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent event) {				
								
				
				if (event.getValueIsAdjusting() || lstItems.getModel().getSize() == 0 || currentCategory == -1)
					return;				
				
				int selected = lstItems.getSelectedIndex();
				
				loadModel(-1, selected);
			}
		});		
        
        panel_3.add( glcanvas, BorderLayout.CENTER);
                
	}
	
	private void loadModel(int charNumberOverride, int selected)
	{
		String modelPath = null;
		byte[] modelData = null;
		ModelItemEntry currentItem = entries[currentCategory].get(selected);
		
		int characterNumber = ((charNumberOverride == -1 ? currentBody * 100+ 01: charNumberOverride)); 
		
		try {
			
			switch (currentCategory)
			{
			case 13:		
			case 1:
			case 2:
				modelPath = String.format("chara/weapon/w%04d/obj/body/b%04d/model/w%04b%04d.mdl", currentItem.id, currentItem.model, currentItem.id, currentItem.model);
				break;
			case 3:
				modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_met.mdl", currentItem.id, characterNumber, currentItem.id);	
				break;
			case 4:					
				modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_top.mdl", currentItem.id, characterNumber, currentItem.id);
				break;
			case 5:
				modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_glv.mdl", currentItem.id, characterNumber, currentItem.id);
				break;
			case 7:
				modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_dwn.mdl", currentItem.id, characterNumber, currentItem.id);
				break;
			case 8:
				modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_sho.mdl", currentItem.id, characterNumber, currentItem.id);
				break;	
			case 9:
				modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_ear.mdl", currentItem.id, characterNumber, currentItem.id);
				break;
			case 10:
				modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_nek.mdl", currentItem.id, characterNumber, currentItem.id);
				break;
			case 11:
				modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_wrs.mdl", currentItem.id, characterNumber, currentItem.id);
				break;
			case 12:
				modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_rir.mdl", currentItem.id, characterNumber, currentItem.id);
				break;					
			}
			
			modelData = modelIndexFile.extractFile(modelPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (modelData == null && (characterNumber != 101 && characterNumber != 201))
		{
			System.out.println(String.format("Model for charId %04d not detected, falling back to %s Hyur model.", characterNumber, currentBody % 2 == 0 ? "female" : "male"));
			
			if (currentBody % 2 == 0)
				loadModel(201, selected);
			else
				loadModel(101, selected);
			
			
			return;			
		}
		
		if (modelData != null)
		{
			Model model = new Model(modelPath,modelIndexFile,modelData);
			model.loadMaterials(currentItem.varient == 0 ? 1 : currentItem.varient);
			renderer.setModel(model);
		}
				
	}
	
	private void loadItems() throws FileNotFoundException, IOException
	{
		SqPack_IndexFile indexFile = new SqPack_IndexFile(parent.getSqpackPath() + "0a0000.win32.index", true);
		EXHF_File exhfFile = new EXHF_File(indexFile.extractFile("exd/item.exh"));
		EXDF_View view = new EXDF_View(indexFile, "exd/item.exh", exhfFile);
		
		for (int i = 0; i < view.getTable().getRowCount(); i++){
			if (!((String)view.getTable().getValueAt(i, 11)).equals("0, 0, 0, 0"))
			{				
				String model1Split[] = ((String)view.getTable().getValueAt(i, 11)).split(",");
				String model2Split[] = ((String)view.getTable().getValueAt(i, 12)).split(",");
						
				int slot = (Integer) view.getTable().getValueAt(i, 48);
				entries[slot].add(new ModelItemEntry((String)view.getTable().getValueAt(i, 4), Integer.parseInt(model1Split[0].trim()), Integer.parseInt(model1Split[2].trim()), Integer.parseInt(model1Split[1].trim()), slot));			
			}
		}		
				
		lstItems.setModel(new ItemsListModel());
				
	}
	
	private int fallback(int characterCode)
	{
		switch (characterCode)
		{
			
		}
		
		return 101;
	}
	
	private void searchAndSelect(String input)
	{
		for (int i = 0; i < entries[currentCategory].size(); i++)
		{
			if (entries[currentCategory].get(i).name.equalsIgnoreCase(input))
			{
				lstItems.setSelectedIndex(i);
			}
		}
	}
	
	class ItemsListModel extends AbstractListModel
	{			
			public int getSize() {
				
				if (currentCategory == -1)
					return 0;
				
				return entries[currentCategory].size();
			}
			public String getElementAt(int index) {
				
				if (currentCategory == -1)
					return "";
				
				return entries[currentCategory].get(index).name;
			}
			
			public void refresh()
			{
				if (currentCategory == -1)
					fireContentsChanged(this, 0, 0);
				else
					fireContentsChanged(this, 0, entries[currentCategory].size());
			}
	}
	
}