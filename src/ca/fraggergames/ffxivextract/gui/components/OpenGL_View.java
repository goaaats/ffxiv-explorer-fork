package ca.fraggergames.ffxivextract.gui.components;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JPanel;

import ca.fraggergames.ffxivextract.models.Mesh;
import ca.fraggergames.ffxivextract.models.Model;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.Animator;
import javax.swing.border.TitledBorder;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;

public class OpenGL_View extends JPanel {

	JComboBox cmbLodChooser;
	
	Animator animator;
	ModelRenderer renderer;
	
	private boolean leftMouseDown = false;
	private boolean rightMouseDown = false;
	
	private int currentLoD = 0;
	private int lastOriginX, lastOriginY;
	private int lastX, lastY;
	
	public OpenGL_View(Model model) {
		GLProfile glProfile = GLProfile.getDefault();
		GLCapabilities glcapabilities = new GLCapabilities( glProfile );
        final GLCanvas glcanvas = new GLCanvas( glcapabilities );
        renderer = new ModelRenderer(model);
        glcanvas.addGLEventListener(renderer);
        animator = new Animator(glcanvas);
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
        
        JLabel lbl1 = new JLabel("Detail Level:");
        panel_1.add(lbl1);
        
        cmbLodChooser = new JComboBox();
        panel_1.add(cmbLodChooser);
        cmbLodChooser.addItem("0");
        cmbLodChooser.addItem("1");
        cmbLodChooser.addItem("2");
        
        cmbLodChooser.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
			          currentLoD = Integer.parseInt((String)e.getItem());
			    }
			}
		});
        
        
        JPanel panel_2 = new JPanel();
        panel.add(panel_2, BorderLayout.CENTER);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
        
        JLabel lblVertices = new JLabel("Vertices:");
        panel_2.add(lblVertices);
        
        JLabel lblIndices = new JLabel("Indices:");
        panel_2.add(lblIndices);
        
        JLabel lblMeshes = new JLabel("Meshes:");
        panel_2.add(lblMeshes);
        add( glcanvas, BorderLayout.CENTER);
	}
	
	class ModelRenderer implements GLEventListener{

		private Model model;
		private GLU glu;
		private float zoom = -7;
		private float panX = 0;
		private float panY = 0;
		private float angleX = 0;
		private float angleY = 0;
		
		public ModelRenderer(Model model) {
			this.model = model;					
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

		@Override
		public void display(GLAutoDrawable drawable) {
			GL2 gl = drawable.getGL().getGL2(); 
		    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT); 
		    gl.glLoadIdentity(); 
			 		    
		    gl.glTranslatef(panX, panY, zoom);
		    gl.glRotatef(angleX, 0, 1, 0);
		    gl.glRotatef(angleY, 1, 0, 0);
		    
		    for (int i = 0; i < model.getNumMesh(currentLoD); i++){
		    	
		    	Mesh mesh = model.getMeshes(currentLoD)[i]; 
		    	
		    	mesh.vertBuffer.position(0);
		    	mesh.indexBuffer.position(0);
			    gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
			    gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
			    
			    if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)
			    	gl.glVertexPointer(4, GL2.GL_HALF_FLOAT, 0, mesh.vertBuffer);
			    else if (mesh.vertexSize == 0x14)
			    	gl.glVertexPointer(3, GL2.GL_FLOAT, 0, mesh.vertBuffer);
			    
			    ByteBuffer otherData = mesh.vertBuffer.duplicate();
			    
			    if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)
			    	otherData.position(mesh.numVerts*8);
			    else
			    	otherData.position(mesh.numVerts*12);
			    	
			    gl.glNormalPointer(GL2.GL_HALF_FLOAT, 24, otherData);
			    gl.glDrawElements(GL2.GL_TRIANGLES, mesh.numIndex, GL2.GL_UNSIGNED_SHORT, mesh.indexBuffer);
			    gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
			    gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
			    
			}
		}

		@Override
		public void dispose(GLAutoDrawable drawable) {
		}

		@Override
		public void init(GLAutoDrawable drawable) {
			GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
		      glu = new GLU();                         // get GL Utilities
		      gl.glClearColor(0.3f, 0.3f, 0.3f, 0.0f); // set background (clear) color
		      gl.glClearDepth(1.0f);      // set clear depth value to farthest
		      gl.glEnable(GL3.GL_DEPTH_TEST); // enables depth testing
		      gl.glDepthFunc(GL3.GL_LEQUAL);  // the type of depth test to do
		      gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL3.GL_NICEST); // best perspective correction
		      gl.glShadeModel(GL2.GL_SMOOTH); // blends colors nicely, and smoothes out lighting
		      
		      //Delete this scrub shit later
		      gl.glEnable(GL2.GL_LIGHTING);
		      gl.glEnable(GL2.GL_LIGHT0);
		      
		      float light_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		      float light_diffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		      float light_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		      float light_position[] = { 1.0f, 1.0f, 1.0f, 0.0f };		    

		      gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light_ambient,0);
		      gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light_diffuse,0);
		      gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light_specular,0);
		      gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light_position,0);
		}

		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height){
			GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
			 
		      if (height == 0) height = 1;   // prevent divide by zero
		      float aspect = (float)width / height;
		 
		      // Set the view port (display area) to cover the entire window
		      gl.glViewport(0, 0, width, height);
		 
		      // Setup perspective projection, with aspect ratio matches viewport
		      gl.glMatrixMode(GL2.GL_PROJECTION);  // choose projection matrix
		      gl.glLoadIdentity();             // reset projection matrix
		      glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar
		 
		      // Enable the model-view transform
		      gl.glMatrixMode(GL2.GL_MODELVIEW);
		      gl.glLoadIdentity(); // reset
		}
		
	}
	
}
