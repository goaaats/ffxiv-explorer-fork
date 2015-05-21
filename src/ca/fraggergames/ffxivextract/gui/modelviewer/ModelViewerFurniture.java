package ca.fraggergames.ffxivextract.gui.modelviewer;

import java.awt.BorderLayout;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jogamp.opengl.util.FPSAnimator;

import ca.fraggergames.ffxivextract.gui.components.EXDF_View;
import ca.fraggergames.ffxivextract.gui.components.ModelRenderer;
import ca.fraggergames.ffxivextract.gui.components.OpenGL_View;
import ca.fraggergames.ffxivextract.helpers.Utils;
import ca.fraggergames.ffxivextract.models.EXHF_File;
import ca.fraggergames.ffxivextract.models.Model;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;

public class ModelViewerFurniture extends JPanel {
	
	ModelViewerWindow parent;

	ArrayList<ModelFurnitureEntry> entries = new ArrayList<ModelFurnitureEntry>();
	
	OpenGL_View view3D;
	JList lstFurniture;	
	
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
		panel_1.add(panel_2, BorderLayout.NORTH);
		
		JLabel lblInfoAndControls = new JLabel("Info and controls go here");
		panel_2.add(lblInfoAndControls);
		
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
			loadFurniture();
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
				
				String modelPath = null;
				byte[] modelData = null;
				try {
					
					modelPath = String.format("bgcommon/hou/indoor/general/%04d/bgparts/fun_b0_m%04d.mdl", entries.get(selected).model, entries.get(selected).model);
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
					Model model = new Model(modelPath,modelIndexFile,modelData);
					renderer.setModel(model);
				}
			}
		});		
        
        panel_3.add( glcanvas, BorderLayout.CENTER);
                
	}

	private void loadFurniture() throws FileNotFoundException, IOException
	{
		SqPack_IndexFile indexFile = new SqPack_IndexFile(parent.getSqpackPath() + "0a0000.win32.index", true);
		EXHF_File exhfFileHousingFurniture = new EXHF_File(indexFile.extractFile("exd/housingfurniture.exh"));
		EXHF_File exhfFileItem = new EXHF_File(indexFile.extractFile("exd/item.exh"));
		EXHF_File exhfFileHousingCategory = new EXHF_File(indexFile.extractFile("exd/housingitemcategory.exh"));
		
		EXDF_View view1 = new EXDF_View(indexFile, "exd/housingfurniture.exh", exhfFileHousingFurniture);
		EXDF_View view2 = new EXDF_View(indexFile, "exd/item.exh", exhfFileItem);
		EXDF_View view3 = new EXDF_View(indexFile, "exd/housingitemcategory.exh", exhfFileHousingCategory);		
		
		for (int i = 0; i < view1.getTable().getRowCount(); i++){
						
			long itemId = (Long) view1.getTable().getValueAt(i, 3);
			int modelNumber = (Integer)view1.getTable().getValueAt(i, 4);
			int furnitureType = (Integer)view1.getTable().getValueAt(i, 5);			
			
			String name = (String) view2.getTable().getValueAt((int)itemId, 4);
			
			if (itemId == 0)
				name = "Unknown";
			
			if (name.isEmpty())
				name = "Placeholder?";
			
			if (modelNumber == 0)
				continue;
			
			String furnitureTypeName = (String) view3.getTable().getValueAt(furnitureType, 1);
			
			entries.add(new ModelFurnitureEntry(i, name, modelNumber, furnitureTypeName));
		}
				
		lstFurniture.setModel(new AbstractListModel() {			
			public int getSize() {
				return entries.size();
			}
			public String getElementAt(int index) {
				return entries.get(index).name + (entries.get(index).type.isEmpty() ? "" : "("+ entries.get(index).type+")");
			}
		});
				
	}
	
}
