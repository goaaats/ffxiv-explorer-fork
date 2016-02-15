package com.fragmenterworks.ffxivextract.gui.components;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.fragmenterworks.ffxivextract.models.CMP_File;

public class CMP_View extends JPanel {

	CMP_File currentCMP;
	JLabel canvas = new JLabel();
	
	public CMP_View(CMP_File file) {
		setBorder(new TitledBorder(null, "CMP File", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		currentCMP = file;
		
		setLayout(new BorderLayout(0, 0));
				
		add(canvas, BorderLayout.CENTER);
		
		int w = 8;
		int h = 5580;
		
		int type = BufferedImage.TYPE_INT_ARGB;

		BufferedImage image = new BufferedImage(30, h, type);

		int drawY = 0;
		
		boolean pushX = false;
	    for(int y = 0; y < h; y++) {
	    	for(int x = 0; x < w; x++) {
		    	if (((y * w) + x) < file.getColors().size())
		    	{		    	
		    		image.setRGB(x, y, file.getColors().get((y * w) + x));
		    	}
		    }
		}
		
		try {
		    File outputfile = new File("saved.png");
		    ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
		}
		
		Image scaled = image.getScaledInstance(100, 500, Image.SCALE_DEFAULT);
		
		canvas.setIcon(new ImageIcon(image));
		
	}
	
}
