package com.fragmenterworks.ffxivextract.gui.modelviewer;

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

import com.fragmenterworks.ffxivextract.Constants;
import com.jogamp.opengl.util.FPSAnimator;

import com.fragmenterworks.ffxivextract.gui.components.EXDF_View;
import com.fragmenterworks.ffxivextract.gui.components.ModelRenderer;
import com.fragmenterworks.ffxivextract.gui.components.OpenGL_View;
import com.fragmenterworks.ffxivextract.helpers.SparseArray;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.EXHF_File;
import com.fragmenterworks.ffxivextract.models.Model;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.border.EmptyBorder;
import javax.swing.JSpinner;
import javax.swing.JButton;

import java.awt.Component;

public class ModelViewerCharacter extends JPanel {
	
	ModelViewerWindow parent;
	
	ArrayList<ModelItemEntry> entries[] = new ArrayList[22];
	
	SparseArray<String> slots = new SparseArray<String>();
	
	SparseArray<String> charIds = new SparseArray<String>();

	//UI
	OpenGL_View view3D;
	FPSAnimator animator;
	
	//Builder	
	private int currentBody = 0;
	private int currentFace = 0;
	private int currentFaceOptions = 0;
	private int currentHair = 0;
	private float[] currentHairColor = Constants.defaultHairColor;
	private float[] currentHairHighlightsColor = Constants.defaultHairColor;
	private float[] currentEyeColor = Constants.defaultEyeColor;
	
	private ModelItemEntry currentWeap1Item = null;
	private ModelItemEntry currentWeap2Item = null;
	private ModelItemEntry currentHeadItem = null;
	private ModelItemEntry currentBodyItem = null;
	private ModelItemEntry currentBeltItem = null;
	private ModelItemEntry currentHandsItem = null;
	private ModelItemEntry currentPantsItem = null;
	private ModelItemEntry currentFeetItem = null;
	private ModelItemEntry currentNeckItem = null;
	private ModelItemEntry currentBracletItem = null;
	private ModelItemEntry currentRing1Item = null;
	private ModelItemEntry currentRing2Item = null;
	
	//Render Stuff
	private ModelRenderer renderer;
		
	private boolean leftMouseDown = false;
	private boolean rightMouseDown = false;
	
	private int lastOriginX, lastOriginY;
	private int lastX, lastY;	
	
	SqPack_IndexFile modelIndexFile;
	
	public ModelViewerCharacter(ModelViewerWindow parent, SqPack_IndexFile modelIndex) {
		
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
		
		JPanel panel_9 = new JPanel();
		add(panel_9, BorderLayout.WEST);
		panel_9.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel_9.add(scrollPane);
		
		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_5 = new JPanel();
		panel.add(panel_5, BorderLayout.NORTH);
		panel_5.setBorder(new TitledBorder(null, "Appearance", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.Y_AXIS));
		
		JComboBox cmbRace = new JComboBox();
		cmbRace.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_5.add(cmbRace);
		
		JPanel panel_4 = new JPanel();
		panel_4.setAlignmentX(Component.LEFT_ALIGNMENT);
		FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		panel_5.add(panel_4);
		
		JLabel lblFace = new JLabel("Face");
		panel_4.add(lblFace);
		
		JSpinner spnFace = new JSpinner();
		panel_4.add(spnFace);
		
		JPanel panel_7 = new JPanel();
		panel_7.setAlignmentX(Component.LEFT_ALIGNMENT);
		FlowLayout flowLayout = (FlowLayout) panel_7.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_5.add(panel_7);
		
		JLabel lblHair = new JLabel("Hair ");
		panel_7.add(lblHair);
		
		JSpinner spnHair = new JSpinner();
		panel_7.add(spnHair);
		
		JPanel panel_8 = new JPanel();
		panel_8.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_5.add(panel_8);
		panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.Y_AXIS));
		
		JButton btnFaceOptions = new JButton("Face Options");
		btnFaceOptions.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_8.add(btnFaceOptions);
		
		JButton btnColorOptions = new JButton("Color Options");
		btnColorOptions.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_8.add(btnColorOptions);
		
		JPanel panel_6 = new JPanel();
		panel.add(panel_6, BorderLayout.CENTER);
		panel_6.setBorder(new TitledBorder(null, "Equipment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.Y_AXIS));
		
		JButton btnMainhand = new JButton("Main Hand");
		panel_6.add(btnMainhand);
		
		JButton btnOffhand = new JButton("Off hand");
		panel_6.add(btnOffhand);
		
		JButton btnHead = new JButton("Head");
		panel_6.add(btnHead);
		
		JButton btnBody = new JButton("Body");
		panel_6.add(btnBody);
		
		JButton btnBelt = new JButton("Belt");
		panel_6.add(btnBelt);
		
		JButton btnHands = new JButton("Hands");
		panel_6.add(btnHands);
		
		JButton btnLegs = new JButton("Legs");
		panel_6.add(btnLegs);
		
		JButton btnFeet = new JButton("Feet");
		panel_6.add(btnFeet);

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
        
        
       // panel_3.add( glcanvas, BorderLayout.CENTER);
                
	}	
	
	
}
