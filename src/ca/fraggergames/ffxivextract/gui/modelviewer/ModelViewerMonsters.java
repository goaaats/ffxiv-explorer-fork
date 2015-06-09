package ca.fraggergames.ffxivextract.gui.modelviewer;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import ca.fraggergames.ffxivextract.helpers.SparseArray;
import ca.fraggergames.ffxivextract.helpers.Utils;
import ca.fraggergames.ffxivextract.models.EXHF_File;
import ca.fraggergames.ffxivextract.models.Model;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;

public class ModelViewerMonsters extends JPanel {
	
	ModelViewerWindow parent;

	ModelCharaEntry[] entries;
	
	OpenGL_View view3D;
	JList lstMonsters;	
	
	FPSAnimator animator;
	
	ModelRenderer renderer;
	
	private boolean leftMouseDown = false;
	private boolean rightMouseDown = false;
	
	private int lastOriginX, lastOriginY;
	private int lastX, lastY;	
	
	SqPack_IndexFile modelIndexFile;	
	
	SparseArray<String> names = new SparseArray<String>();
	
	public ModelViewerMonsters(ModelViewerWindow parent, SqPack_IndexFile modelIndex) {
		
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
			loadMonsters();
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
				try {
					
					switch (entries[selected].type)
					{
					case 2:
						EquipableRender demihuman = new EquipableRender();
						
						switch(entries[selected].id)
						{
						case 1: //Chocobo													
							demihuman.setModel(EquipableRender.DWN, modelIndexFile, "chara/demihuman/d0001/obj/equipment/e0001/model/d0001e0001_dwn.mdl", 0);
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d0001/obj/equipment/e0001/model/d0001e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.SHO, modelIndexFile, "chara/demihuman/d0001/obj/equipment/e0001/model/d0001e0001_sho.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d0001/obj/equipment/e0001/model/d0001e0001_top.mdl", 0);							
							break;
						case 2: //Magitek													
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d0002/obj/equipment/e0001/model/d0002e0001_top.mdl", 0);											
							break;
						case 1001: //Amalja													
							demihuman.setModel(EquipableRender.DWN, modelIndexFile, "chara/demihuman/d1001/obj/equipment/e0001/model/d1001e0001_dwn.mdl", 0);
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1001/obj/equipment/e0001/model/d1001e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.SHO, modelIndexFile, "chara/demihuman/d1001/obj/equipment/e0001/model/d1001e0001_sho.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1001/obj/equipment/e0001/model/d1001e0001_top.mdl", 0);
							demihuman.setModel(EquipableRender.GLV, modelIndexFile, "chara/demihuman/d1001/obj/equipment/e0001/model/d1001e0001_glv.mdl", 0);
							break;
						case 1002: //Ixali													
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1002/obj/equipment/e0001/model/d1002e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.SHO, modelIndexFile, "chara/demihuman/d1002/obj/equipment/e0001/model/d1002e0001_sho.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1002/obj/equipment/e0001/model/d1002e0001_top.mdl", 0);
							demihuman.setModel(EquipableRender.GLV, modelIndexFile, "chara/demihuman/d1002/obj/equipment/e0001/model/d1002e0001_glv.mdl", 0);
							break;
						case 1003: //Kobold												
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1003/obj/equipment/e0001/model/d1003e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1003/obj/equipment/e0001/model/d1003e0001_top.mdl", 0);							
							break;
						case 1004: //Goblin													
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1004/obj/equipment/e0001/model/d1004e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1004/obj/equipment/e0001/model/d1004e0001_top.mdl", 0);							
							break;
						case 1005: //Sylph													
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1005/obj/equipment/e0001/model/d1005e0001_top.mdl", 0);							
							break;
						case 1006: //Moogle													
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1006/obj/equipment/e0001/model/d1006e0001_top.mdl", 0);							
							break;
						case 1007: //Sahagin													
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1007/obj/equipment/e0001/model/d1007e0001_top.mdl", 0);							
							break;
						case 1008: //Mamoolja													
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1008/obj/equipment/e0001/model/d1008e0001_top.mdl", 0);							
							break;
						case 1009: //Gigas 1													
							demihuman.setModel(EquipableRender.DWN, modelIndexFile, "chara/demihuman/d1009/obj/equipment/e0001/model/d1009e0001_dwn.mdl", 0);
							demihuman.setModel(EquipableRender.GLV, modelIndexFile, "chara/demihuman/d1009/obj/equipment/e0001/model/d1009e0001_glv.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1009/obj/equipment/e0001/model/d1009e0001_top.mdl", 0);							
							break;
						case 1010: //Gigas 2													
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1010/obj/equipment/e0001/model/d1010e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1010/obj/equipment/e0001/model/d1010e0001_top.mdl", 0);							
							break;
						case 1011: //Gigas 3													
							demihuman.setModel(EquipableRender.DWN, modelIndexFile, "chara/demihuman/d1011/obj/equipment/e0001/model/d1011e0001_dwn.mdl", 0);
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1011/obj/equipment/e0001/model/d1011e0001_met.mdl", 0);							
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1011/obj/equipment/e0001/model/d1011e0001_top.mdl", 0);							
							break;
						case 1012: //Quijrn													
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1012/obj/equipment/e0001/model/d1012e0001_top.mdl", 0);							
							break;
						case 1013: //Moogle Postman													
							demihuman.setModel(EquipableRender.DWN, modelIndexFile, "chara/demihuman/d1013/obj/equipment/e0001/model/d1013e0001_dwn.mdl", 0);
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1013/obj/equipment/e0001/model/d1013e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1013/obj/equipment/e0001/model/d1013e0001_top.mdl", 0);							
							break;
						case 1014: //Lamia													
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1014/obj/equipment/e0001/model/d1014e0001_top.mdl", 0);							
							break;
						case 1015: //Skel													
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1015/obj/equipment/e0001/model/d1015e0001_top.mdl", 0);							
							break;
						case 1016: //Succubus													
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1016/obj/equipment/e0001/model/d1016e0001_top.mdl", 0);							
							break;
						case 1017: //Demon
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1017/obj/equipment/e0001/model/d1017e0001_top.mdl", 0);							
							break;
						case 1018: //Emperor													
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1018/obj/equipment/e0001/model/d1018e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1018/obj/equipment/e0001/model/d1018e0001_top.mdl", 0);							
							break;
						case 1019: //Archbishop											
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1019/obj/equipment/e0001/model/d1019e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1019/obj/equipment/e0001/model/d1019e0001_top.mdl", 0);							
							break;
						case 1020: //											
							//demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1020/obj/equipment/e0001/model/d1020e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.DWN, modelIndexFile, "chara/demihuman/d1020/obj/equipment/e0001/model/d1020e0001_dwn.mdl", 0);							
							break;
						case 1021: //											
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1021/obj/equipment/e0001/model/d1021e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1022/obj/equipment/e0001/model/d1021e0001_top.mdl", 0);							
							break;
						case 1022: //											
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1022/obj/equipment/e0001/model/d1022e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1022/obj/equipment/e0001/model/d1022e0001_top.mdl", 0);							
							break;
						case 1101: //											
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1101/obj/equipment/e0001/model/d1101e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1101/obj/equipment/e0001/model/d1101e0001_top.mdl", 0);							
							break;
						case 1201: //											
							demihuman.setModel(EquipableRender.MET, modelIndexFile, "chara/demihuman/d1201/obj/equipment/e0001/model/d1201e0001_met.mdl", 0);
							demihuman.setModel(EquipableRender.TOP, modelIndexFile, "chara/demihuman/d1201/obj/equipment/e0001/model/d1201e0001_top.mdl", 0);							
							break;
						}		
												
						renderer.setModels(demihuman.getModels());
						
						break;
					case 3:
						modelPath = String.format("chara/monster/m%04d/obj/body/b%04d/model/m%04db%04d.mdl", entries[selected].id, entries[selected].model, entries[selected].id, entries[selected].model);
						modelData = modelIndexFile.extractFile(modelPath);
						if (modelData != null)
						{
							Model model = new Model(modelPath,modelIndexFile,modelData);
							model.loadMaterials(entries[selected].varient);
							renderer.setModel(model);
						}
						break;
					}						
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		});		
        
        panel_3.add( glcanvas, BorderLayout.CENTER);
                
        loadAndParseNames("./monsters.lst");
        

	}

	private void loadMonsters() throws FileNotFoundException, IOException
	{
		SqPack_IndexFile indexFile = new SqPack_IndexFile(parent.getSqpackPath() + "0a0000.win32.index", true);
		EXHF_File exhfFile = new EXHF_File(indexFile.extractFile("exd/modelchara.exh"));
		EXDF_View view = new EXDF_View(indexFile, "exd/modelchara.exh", exhfFile);
		
		entries = new ModelCharaEntry[view.getTable().getRowCount()];
		
		for (int i = 0; i < view.getTable().getRowCount(); i++){
			entries[i] = new ModelCharaEntry((Integer)view.getTable().getValueAt(i, 1), (Integer)view.getTable().getValueAt(i, 4), (Integer)view.getTable().getValueAt(i, 5), (Integer)view.getTable().getValueAt(i, 3));
		}
				
		lstMonsters.setModel(new AbstractListModel() {			
			public int getSize() {
				return entries.length;
			}
			public String getElementAt(int index) {				
				return names.get(index, "Monster " + index);
			}
		});
				
	}
	
	private void loadAndParseNames(String path)
	{
		try{
			BufferedReader br = new BufferedReader(new FileReader(path));
		    for(String line; (line = br.readLine()) != null; ) {
		    	//Skip comments and whitespace
		        if (line.startsWith("#") || line.isEmpty() || line.equals(""))
		        	continue;		        
		        if (line.contains(":"))
		        {
		        	String split[] = line.split(":", 2);
		        	if (split.length != 2)
		        		continue;
		        	
		        	if (split[1].isEmpty())
		        		continue;
		        	names.put(Integer.parseInt(split[0]), split[1]);
		        }
		    } 
		}
		catch (IOException e){
			
		}
	}
	
}
