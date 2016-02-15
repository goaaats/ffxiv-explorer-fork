package com.fragmenterworks.ffxivextract.gui.modelviewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.fragmenterworks.ffxivextract.storage.HashDatabase;
import com.jogamp.opengl.util.FPSAnimator;

import com.fragmenterworks.ffxivextract.gui.components.EXDF_View;
import com.fragmenterworks.ffxivextract.gui.components.ModelRenderer;
import com.fragmenterworks.ffxivextract.gui.components.OpenGL_View;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.EXHF_File;
import com.fragmenterworks.ffxivextract.models.Model;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;

public class ModelViewerFurniture extends JPanel {
	
	public static final int INDEX_ITEM_NAME = 10;
	public static final int INDEX_HOUSINGFURNITURE_ITEMID = 7;
	public static final int INDEX_HOUSINGFURNITURE_TYPEID = 2;
	public static final int INDEX_HOUSINGFURNITURE_MODELNUMBER = 1; 
	public static final int INDEX_FURNITURETYPE_NAME = 1;
		
	ModelViewerWindow parent;

	ArrayList<ModelFurnitureEntry> entries = new ArrayList<ModelFurnitureEntry>();
	
	OpenGL_View view3D;
	JList lstFurniture;	
	
	JLabel txtPath;
	JButton btnResetCamera;
	JCheckBox chkGlowToggle;
	
	FPSAnimator animator;
	
	ModelRenderer renderer;
	
	private boolean leftMouseDown = false;
	private boolean rightMouseDown = false;
	
	private int currentLoD = 0;
	private int lastOriginX, lastOriginY;
	private int lastX, lastY;	
	
	SqPack_IndexFile modelIndexFile;
	
	public ModelViewerFurniture(ModelViewerWindow parent, SqPack_IndexFile modelIndex) {
		
		this.parent = parent;
		this.modelIndexFile = modelIndex;
		
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
		
		JPanel panelInfo_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelInfo_1.getLayout();
		flowLayout.setVgap(1);
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_2.add(panelInfo_1);
		
		JLabel lblBleh = new JLabel("Path: ");
		panelInfo_1.add(lblBleh);
					
		txtPath = new JLabel("-");
		panelInfo_1.add(txtPath);
		
		JPanel panelInfo_3 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panelInfo_3.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		panel_2.add(panelInfo_3);
		
		btnResetCamera = new JButton("Reset Camera");
		panelInfo_3.add(btnResetCamera);
		
		chkGlowToggle = new JCheckBox("Glow Shader", true);
		panelInfo_3.add(chkGlowToggle);
			
		chkGlowToggle.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				renderer.toggleGlow(chkGlowToggle.isSelected());
			}
		});
		
		btnResetCamera.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				renderer.resetCamera();
			}
		});
		
		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.WEST);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.WEST);
		
		lstFurniture = new JList();
		
		scrollPane.setViewportView(lstFurniture);

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
			if (!loadFurniture())
			{
				removeAll();
				JLabel errorLabel = new JLabel("There was an error loading the furniture list.");
				add(errorLabel);
				return;
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        lstFurniture.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent event) {				
								
				
				if (event.getValueIsAdjusting() || lstFurniture.getModel().getSize() == 0)
					return;				
				
				int selected = lstFurniture.getSelectedIndex();
				
				if (selected == -1)
					return;
				
				String modelPath = null;
				byte[] modelData = null;
				try {
					
					if (entries.get(selected).modelType == ModelFurnitureEntry.TYPE_FURNITURE)
						modelPath = String.format("bgcommon/hou/indoor/general/%04d/bgparts/fun_b0_m%04d.mdl", entries.get(selected).model, entries.get(selected).model);
					else if (entries.get(selected).modelType == ModelFurnitureEntry.TYPE_YARDOBJECT)
						modelPath = String.format("bgcommon/hou/outdoor/general/%04d/bgparts/gar_b0_m%04d.mdl", entries.get(selected).model, entries.get(selected).model);
					
					modelData = modelIndexFile.extractFile(modelPath);
					
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (modelData != null)
				{
					System.out.println("Adding Entry: " + modelPath);
					HashDatabase.addPathToDB(modelPath, "040000");
					Model model = new Model(modelPath,modelIndexFile,modelData);
					renderer.setModel(model);
				}
				
				txtPath.setText(modelPath);
			}
		});		
        
        panel_3.add( glcanvas, BorderLayout.CENTER);
                
	}

	private boolean loadFurniture() throws FileNotFoundException, IOException
	{
		SqPack_IndexFile indexFile = parent.getExdIndexFile();
		EXHF_File exhfFileHousingFurniture = new EXHF_File(indexFile.extractFile("exd/housingfurniture.exh"));
		EXHF_File exhfFileHousingYardObject = new EXHF_File(indexFile.extractFile("exd/housingyardobject.exh"));
		EXHF_File exhfFileItem = new EXHF_File(indexFile.extractFile("exd/item.exh"));
		EXHF_File exhfFileHousingCategory = new EXHF_File(indexFile.extractFile("exd/housingitemcategory.exh"));		
		
		EXDF_View view1 = new EXDF_View(indexFile, "exd/housingfurniture.exh", exhfFileHousingFurniture);
		EXDF_View view2 = new EXDF_View(indexFile, "exd/item.exh", exhfFileItem);
		EXDF_View view3 = new EXDF_View(indexFile, "exd/housingitemcategory.exh", exhfFileHousingCategory);
		EXDF_View view4 = new EXDF_View(indexFile, "exd/housingyardobject.exh", exhfFileHousingYardObject);
		
		try{
			for (int i = 0; i < view1.getTable().getRowCount(); i++){
							
				long itemId = (Long) view1.getTable().getValueAt(i, INDEX_HOUSINGFURNITURE_ITEMID);
				int modelNumber = (Integer)view1.getTable().getValueAt(i, INDEX_HOUSINGFURNITURE_MODELNUMBER);
				int furnitureType = (Integer)view1.getTable().getValueAt(i, INDEX_HOUSINGFURNITURE_TYPEID);			
				
				String name = (String) view2.getTable().getValueAt((int)itemId, INDEX_ITEM_NAME);
				
				if (itemId == 0)
					name = "Unknown";
				
				if (name.isEmpty())
					name = "Placeholder?";
				
				if (modelNumber == 0)
					continue;
				
				String furnitureTypeName = (String) view3.getTable().getValueAt(furnitureType, INDEX_FURNITURETYPE_NAME);
				
				entries.add(new ModelFurnitureEntry(ModelFurnitureEntry.TYPE_FURNITURE, i, name, modelNumber, furnitureTypeName));
			}
			for (int i = 0; i < view4.getTable().getRowCount(); i++){
				
				long itemId = (Long) view4.getTable().getValueAt(i, INDEX_HOUSINGFURNITURE_ITEMID);
				int modelNumber = (Integer)view4.getTable().getValueAt(i, INDEX_HOUSINGFURNITURE_MODELNUMBER);
				int furnitureType = (Integer)view4.getTable().getValueAt(i, INDEX_HOUSINGFURNITURE_TYPEID);			
				
				String name = (String) view2.getTable().getValueAt((int)itemId, INDEX_ITEM_NAME);
				
				if (itemId == 0)
					name = "Unknown";
				
				if (name.isEmpty())
					name = "Placeholder?";
				
				if (modelNumber == 0)
					continue;
				
				String furnitureTypeName = (String) view3.getTable().getValueAt(furnitureType, INDEX_FURNITURETYPE_NAME);
				
				entries.add(new ModelFurnitureEntry(ModelFurnitureEntry.TYPE_YARDOBJECT, i, name, modelNumber, furnitureTypeName));
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			return false;			
		}
	
		lstFurniture.setModel(new AbstractListModel() {			
			public int getSize() {
				return entries.size();
			}
			public String getElementAt(int index) {
				return entries.get(index).name + (entries.get(index).type.isEmpty() ? "" : "("+ entries.get(index).type+")");
			}
		});
			
		return true;
	}
	
}
