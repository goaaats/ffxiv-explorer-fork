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

	int currentCategory = 3;
	
	OpenGL_View view3D;
	JList lstMonsters;	
	
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
		
		JComboBox cmbBodyStyle = new JComboBox();
		panel_5.add(cmbBodyStyle);
		
		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new EmptyBorder(2, 1, 2, 1));
		panel_4.add(panel_6);
		panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));
		
		JComboBox cmbCategory = new JComboBox();
		panel_6.add(cmbCategory);
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		
		lstMonsters = new JList();
		
		scrollPane.setViewportView(lstMonsters);

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
        
        lstMonsters.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent event) {				
								
				
				if (event.getValueIsAdjusting() || lstMonsters.getModel().getSize() == 0)
					return;				
				
				int selected = lstMonsters.getSelectedIndex();
				
				String modelPath = null;
				byte[] modelData = null;				
				
				ModelItemEntry currentItem = entries[currentCategory].get(selected);
				
				try {
					
					switch (currentCategory)
					{
					case 13:											
					case 2:
						modelPath = String.format("chara/weapon/w%04d/obj/body/b%04d/model/w%04b%04d.mdl", currentItem.id, currentItem.model, currentItem.id, currentItem.model);
						break;
					case 3:
						modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_met.mdl", currentItem.id, currentItem.character, currentItem.id);	
						break;
					case 4:					
						modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_top.mdl", currentItem.id, currentItem.character, currentItem.id);
						break;
					case 5:
						modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_glv.mdl", currentItem.id, currentItem.character, currentItem.id);
						break;
					case 7:
						modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_dwn.mdl", currentItem.id, currentItem.character, currentItem.id);
						break;
					case 8:
						modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_met.mdl", currentItem.id, currentItem.character, currentItem.id);
						break;	
					case 9:
						modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_ear.mdl", currentItem.id, currentItem.character, currentItem.id);
						break;
					case 10:
						modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_nek.mdl", currentItem.id, currentItem.character, currentItem.id);
						break;
					case 11:
						modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_wrs.mdl", currentItem.id, currentItem.character, currentItem.id);
						break;
					case 12:
						modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_rir.mdl", currentItem.id, currentItem.character, currentItem.id);
						break;					
					}
					
					modelData = modelIndexFile.extractFile(modelPath);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if (modelData != null)
				{
					Model model = new Model(modelPath,modelIndexFile,modelData);
					model.loadMaterials(currentItem.varient == 0 ? 1 : currentItem.varient);
					renderer.setModel(model);
				}
			}
		});		
        
        panel_3.add( glcanvas, BorderLayout.CENTER);
                
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
				entries[slot].add(new ModelItemEntry((String)view.getTable().getValueAt(i, 4), Integer.parseInt(model1Split[0].trim()), Integer.parseInt(model1Split[2].trim()), Integer.parseInt(model1Split[1].trim()), slot, 101));			
			}
		}		
				
		lstMonsters.setModel(new AbstractListModel() {			
			public int getSize() {
				return entries[currentCategory].size();
			}
			public String getElementAt(int index) {
				return entries[currentCategory].get(index).name;
			}
		});
				
	}
	
}
