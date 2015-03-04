package ca.fraggergames.ffxivextract.gui.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.nio.FloatBuffer;

import javax.media.opengl.GL3;
import javax.media.opengl.GL3bc;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ca.fraggergames.ffxivextract.helpers.Matrix;
import ca.fraggergames.ffxivextract.models.Mesh;
import ca.fraggergames.ffxivextract.models.Model;
import ca.fraggergames.ffxivextract.shaders.BlurShader;
import ca.fraggergames.ffxivextract.shaders.DefaultShader;
import ca.fraggergames.ffxivextract.shaders.FXAAShader;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.FPSAnimator;

public class OpenGL_View extends JPanel {

	//UI
	JLabel lblVertices, lblIndices, lblMeshes;
	JComboBox cmbLodChooser, cmbVariantChooser;
	
	FPSAnimator animator;
	ModelRenderer renderer;
	JLabel lbl1;	
	
	private boolean leftMouseDown = false;
	private boolean rightMouseDown = false;
	
	private int currentLoD = 0;
	private int lastOriginX, lastOriginY;
	private int lastX, lastY;		
	
	public OpenGL_View(final Model model, Model model2) {
		GLProfile glProfile = GLProfile.getDefault();
		GLCapabilities glcapabilities = new GLCapabilities( glProfile );
        final GLCanvas glcanvas = new GLCanvas( glcapabilities );
        renderer = new ModelRenderer(model, model2);
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
        setLayout(new BorderLayout(0, 0));
        
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Model Info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(panel, BorderLayout.NORTH);
        panel.setLayout(new BorderLayout(0, 0));
        
        JPanel panel_1 = new JPanel();
        panel.add(panel_1, BorderLayout.EAST);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
        
        JPanel panel_3 = new JPanel();
        FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
        flowLayout_1.setAlignment(FlowLayout.RIGHT);
        panel_1.add(panel_3);
        
        lbl1 = new JLabel("Detail Level:");
        panel_3.add(lbl1);
        
        cmbLodChooser = new JComboBox();
        panel_3.add(cmbLodChooser);
        cmbLodChooser.addItem("0");
        cmbLodChooser.addItem("1");
        cmbLodChooser.addItem("2");
        cmbLodChooser.setLightWeightPopupEnabled(false);
        
        cmbLodChooser.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
		          currentLoD = Integer.parseInt((String)e.getItem());
		          String vertList = "Vertices: ";
		          String indicesList = "Indices: ";
		          for (Mesh m : model.getMeshes(currentLoD))
		          {
		        	  vertList+= "("+m.numVerts+") ";
		        	  indicesList+= "("+m.numIndex+") ";
		          }		        		          
		          
		          lblVertices.setText("Vertices: " + vertList);
		          lblIndices.setText("Indices: " + indicesList);
		          lblMeshes.setText("Num Meshes: " + model.getMeshes(currentLoD).length);
		          renderer.setLoD(currentLoD);	
			    }
			}
		});
                
        JPanel panel_4 = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel_4.getLayout();
        flowLayout.setAlignment(FlowLayout.RIGHT);
        panel_1.add(panel_4);
        
        JLabel lblVariant = new JLabel("Variant:");
        panel_4.add(lblVariant);
        
        cmbVariantChooser = new JComboBox();
        cmbVariantChooser.setLightWeightPopupEnabled(false);
        
        int variantChooserModel[] = new int[model.getNumVariants() == -1 ? 0 : model.getNumVariants()];
        for (int i = 0; i < variantChooserModel.length; i++)
        	cmbVariantChooser.addItem("" + (i+1));
        
        cmbVariantChooser.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
			          model.loadMaterials(Integer.parseInt((String)e.getItem()));
			          renderer.resetMaterial();
			    }
			}
		});
        
        panel_4.add(cmbVariantChooser);
        
        JPanel panel_5 = new JPanel();
        panel.add(panel_5, BorderLayout.CENTER);
        panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));
        
        
        JPanel panel_2 = new JPanel();
        panel_5.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
        
        lblVertices = new JLabel("Vertices:");
        panel_2.add(lblVertices);
        
        lblIndices = new JLabel("Indices:");
        panel_2.add(lblIndices);
        
        lblMeshes = new JLabel("Num Meshes: " + model.getMeshes(currentLoD).length);
        panel_2.add(lblMeshes);
        
        String vertList = "Vertices: ";
        String indicesList = "Indices: ";
        for (Mesh m : model.getMeshes(currentLoD))
        {
      	  vertList+= "("+m.numVerts+") ";
      	  indicesList+= "("+m.numIndex+") ";
        }		        		          
        
        lblVertices.setText("Vertices: " + vertList);
        lblIndices.setText("Indices: " + indicesList);
        lblMeshes.setText("Num Meshes: " + model.getMeshes(currentLoD).length);
        
        add( glcanvas, BorderLayout.CENTER);
	}
		
}
