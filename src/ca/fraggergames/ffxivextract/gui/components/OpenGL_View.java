package ca.fraggergames.ffxivextract.gui.components;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;

import javax.media.opengl.GL;
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
import ca.fraggergames.ffxivextract.helpers.ImageDecoding.ImageDecodingException;
import ca.fraggergames.ffxivextract.models.DefaultShader;
import ca.fraggergames.ffxivextract.models.HairShader;
import ca.fraggergames.ffxivextract.models.IrisShader;
import ca.fraggergames.ffxivextract.models.Material;
import ca.fraggergames.ffxivextract.models.Mesh;
import ca.fraggergames.ffxivextract.models.Model;
import ca.fraggergames.ffxivextract.models.Shader;
import ca.fraggergames.ffxivextract.models.Texture_File;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.FlowLayout;
import javax.swing.DefaultComboBoxModel;

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
			          lblMeshes.setText("Num Meshes: " + model.getMeshes(currentLoD).length);
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
        
        add( glcanvas, BorderLayout.CENTER);
	}
	
	class ModelRenderer implements GLEventListener{

		private Model model, model2;
		private GLU glu;
		private float zoom = -7;
		private float panX = 0;
		private float panY = 0;
		private float angleX = 0;
		private float angleY = 0;
		
		DefaultShader defaultShader;
		
		private int[] textureIds;		
		
		//Matrices
		float[] modelMatrix = new float[16];
		float[] viewMatrix = new float[16];
		float[] projMatrix = new float[16];
		
		public ModelRenderer(Model model, Model model2) {
			this.model = model;
			this.model2 = model2;
			textureIds = new int[model.getNumMaterials() * 4];
		}

		public void resetMaterial() {
			loaded = false;
		}

		public void zoom(int notches) {
			zoom += notches * 0.25f;
		}
		
		public void rotate(float x, float y)
		{
			angleX += x * 1.0f;
			angleY += y * 1.0f;
		}
		
		public void pan(float x, float y)
		{
			panX += x * 0.05f;
			panY += -y * 0.05f;
		}

		boolean loaded = false;		
		
		@Override
		public void display(GLAutoDrawable drawable) {
			GL3bc gl = drawable.getGL().getGL3bc();
			
			if (!loaded)
			{
				model.loadToVRAM(gl);
			//	model2.loadToVRAM(gl);
				loaded = true;
			}
			
		    gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT); 		  
		    
		    Matrix.setIdentityM(viewMatrix, 0);
		    Matrix.setIdentityM(modelMatrix, 0);
		    Matrix.translateM(modelMatrix, 0, panX, panY, zoom);
		    Matrix.rotateM(modelMatrix, 0, angleX, 0, 1, 0);
		    Matrix.rotateM(modelMatrix, 0, angleY, 1, 0, 0);		     		   		    		    		    
		    
		    model.render(defaultShader, viewMatrix, modelMatrix, projMatrix, gl, currentLoD);
		    if (model2 != null)
		    	model2.render(defaultShader, viewMatrix, modelMatrix, projMatrix, gl, currentLoD);
		}

		@Override
		public void dispose(GLAutoDrawable drawable) {
		}

		@Override
		public void init(GLAutoDrawable drawable) {
			GL3 gl = drawable.getGL().getGL3();      // get the OpenGL graphics context
		      glu = new GLU();                         // get GL Utilities
		      gl.glClearColor(0.3f, 0.3f, 0.3f, 0.0f); // set background (clear) color
		      gl.glClearDepth(1.0f);      // set clear depth value to farthest
		      gl.glEnable(GL3.GL_DEPTH_TEST); // enables depth testing
		      gl.glDepthFunc(GL3.GL_LEQUAL);  // the type of depth test to do
		      gl.glEnable(GL3.GL_BLEND);
		      gl.glEnable(GL3.GL_CULL_FACE);
		      gl.glBlendFunc (GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);	
		      gl.glEnable(GL3.GL_TEXTURE_2D);
		    		      
		      try {
				defaultShader = new DefaultShader(gl);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height){
				GL3bc gl = drawable.getGL().getGL3bc();  // get the OpenGL 2 graphics context
			 
		      if (height == 0) height = 1;   // prevent divide by zero
		      float aspect = (float)width / height;
		 
		      gl.glViewport(0, 0, width, height);
		 
		      Matrix.setIdentityM(projMatrix, 0);
		      Matrix.perspectiveM(projMatrix, 0, 45.0f, aspect, 0.1f, 100.0f);		     
		      Matrix.setIdentityM(modelMatrix, 0);
		      Matrix.setIdentityM(viewMatrix, 0);
		}
		
	}
	
}
