package ca.fraggergames.ffxivextract.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ca.fraggergames.ffxivextract.Constants;

public class AboutWindow extends JFrame {

	JPanel aboutWindow = new JPanel();
	JPanel container = new JPanel();
	FancyJLabel appname = new FancyJLabel(Constants.APPNAME);	
	JLabel author = new FancyJLabel("By Magis");
	JLabel version = new FancyJLabel("Version: " + Constants.VERSION);
	JLabel gitcommit = new FancyJLabel("Git Commit: " + Constants.COMMIT.substring(0, 10));
	
	JLabel magisImage = new JLabel();
	
	public AboutWindow() {
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(480, 700);
		this.setTitle("About " + Constants.APPNAME);
		
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	
		Font titleFont = new Font("Helvetica", Font.BOLD, 20);
		Font standardFont = new Font("Helvetica", Font.PLAIN, 14);
		
		appname.setFont(titleFont);
		author.setFont(standardFont);
		version.setFont(standardFont);
		gitcommit.setFont(standardFont);
		
		ImageIcon image = new ImageIcon(getClass().getResource("/res/me.png"));
		magisImage.setIcon(image);				
			
		container.add(appname);
		container.add(author);
		container.add(version);
		container.add(gitcommit);		
		
		container.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel centerPanel = new JPanel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 1;
		centerPanel.setLayout(new GridBagLayout());		
		centerPanel.add(container, gbc);
		
		aboutWindow.setLayout(new BorderLayout());		
		aboutWindow.add(centerPanel, BorderLayout.LINE_START);
		aboutWindow.add(magisImage, BorderLayout.LINE_END);
		getContentPane().add(aboutWindow);
		
		ImageIcon icon = new ImageIcon(getClass().getResource("/res/frameicon.png"));
		
		this.setIconImage(icon.getImage());
		this.pack();
		this.setSize(getWidth(), getHeight()-10);
		this.setResizable(false);
	}

	private class FancyJLabel extends JLabel{
	
		public FancyJLabel(String string)
		{
			super(string);
		}

            @Override
            public void paintComponent(Graphics g) {
                Graphics2D graphics2d = (Graphics2D) g;
                graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(g);
            }
        
	}
	
}

