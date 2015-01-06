package ca.fraggergames.ffxivextract.gui.components;

import java.awt.BorderLayout;
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

import ca.fraggergames.ffxivextract.models.Model;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.Animator;

public class OpenGL_View extends JPanel {

	Animator animator;
	ModelRenderer renderer;
	
	private boolean leftMouseDown = false;
	private boolean rightMouseDown = false;
	
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
		
		ShortBuffer verts;
		ShortBuffer indices;   
		ShortBuffer normals;
		
		public ModelRenderer(Model model) {
			this.model = model;			
			
			verts = Buffers.newDirectShortBuffer(model.verts);			
			indices = Buffers.newDirectShortBuffer(model.indices);
			normals = Buffers.newDirectShortBuffer(model.normals);
		}

		public void zoom(int notches) {
			zoom += notches;
			//System.out.println("Zooming to: " + zoom);
		}
		
		public void rotate(float x, float y)
		{
			angleX += x * 1.0f;
			angleY += y * 1.0f;
			//System.out.println("Rotating to: " + angleX + ",  " + angleY);
		}
		
		public void pan(float x, float y)
		{
			panX += x * 0.05f;
			panY += -y * 0.05f;
			//System.out.println("Panning to: " + panX + ",  " + panY);
		}

		@Override
		public void display(GLAutoDrawable drawable) {
			verts.position(0);
			indices.position(0);
			normals.position(0);
			
			GL2 gl = drawable.getGL().getGL2(); 
		    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT); 
		    gl.glLoadIdentity(); 
		 
		    gl.glTranslatef(panX, panY, zoom);
		    gl.glRotatef(angleX, 0, 1, 0);
		    gl.glRotatef(angleY, 1, 0, 0);	
		    gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		    gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		    gl.glVertexPointer(4, GL2.GL_HALF_FLOAT, 0, verts);
		    gl.glNormalPointer(GL2.GL_HALF_FLOAT, 0, normals);
		    gl.glDrawElements(GL2.GL_TRIANGLES, model.indices.length, GL2.GL_UNSIGNED_SHORT, indices);
		    gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		    gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
				    
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
