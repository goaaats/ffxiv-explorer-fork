package com.fragmenterworks.ffxivextract.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.Strings;

public class AboutWindow extends JFrame {

	JPanel aboutWindow = new JPanel();
	JPanel container = new JPanel();
	FancyJLabel appname = new FancyJLabel(Constants.APPNAME);	
	JLabel author = new FancyJLabel("By Ioncannon");
	JLabel version = new FancyJLabel(Strings.ABOUTDIALOG_VERSION + " " + Constants.VERSION);
	JLabel gitcommit = new FancyJLabel(Strings.ABOUTDIALOG_GITVERSION + " " + Constants.COMMIT.substring(0, 10));
	JLabel website = new FancyJLabel("<html><a href=\"\">"+Constants.URL_WEBSITE+"</a></html>");
	
	JLabel meImage = new JLabel();
	
	private int easterEggActivate = 0;
	
	Font titleFont;
	Font standardFont;
	
	JFrame parent;
	
	public AboutWindow(JFrame parent) {
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(480, 700);
		this.setTitle(Strings.DIALOG_TITLE_ABOUT + " " + Constants.APPNAME);
		
		this.parent = parent;
		
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	
		titleFont = new Font("Helvetica", Font.BOLD, 20);
		standardFont = new Font("Helvetica", Font.PLAIN, 14);
		
		appname.setFont(titleFont);
		author.setFont(standardFont);
		version.setFont(standardFont);
		gitcommit.setFont(standardFont);
		website.setFont(standardFont);
		
		ImageIcon image = new ImageIcon(getClass().getResource("/res/me.png"));
		meImage.setIcon(image);				
			
		website.setCursor(new Cursor(Cursor.HAND_CURSOR));
		goWebsite(website);
		
		container.add(appname);
		container.add(author);
		container.add(version);
		container.add(gitcommit);
		container.add(website);	
		
		container.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel centerPanel = new JPanel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 1;
		centerPanel.setLayout(new GridBagLayout());		
		centerPanel.add(container, gbc);
		
		aboutWindow.setLayout(new BorderLayout());		
		aboutWindow.add(centerPanel, BorderLayout.LINE_START);
		aboutWindow.add(meImage, BorderLayout.LINE_END);
		getContentPane().add(aboutWindow);		
		
		ImageIcon icon = new ImageIcon(getClass().getResource("/res/frameicon.png"));
		
		this.setIconImage(icon.getImage());
		this.pack();
		this.setSize(getWidth(), getHeight()-10);
		this.setResizable(false);
		
		//Easter Egg :)
		meImage.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
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
				easterEggActivate++;
				
				if (easterEggActivate >= 5)
				{
					try {
						Constants.setUIFont(new FontUIResource(Font.createFont(Font.TRUETYPE_FONT, getClass().getResource("/res/cache").openStream()).deriveFont(13.5f)));
						
						titleFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResource("/res/cache").openStream()).deriveFont(20.0f);
						standardFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResource("/res/cache").openStream()).deriveFont(14.0f);
						
						appname.setFont(titleFont);
						author.setFont(standardFont);
						version.setFont(standardFont);
						gitcommit.setFont(standardFont);
						
						SwingUtilities.updateComponentTreeUI( AboutWindow.this.parent );
						
					} catch (FontFormatException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void goWebsite(JLabel website) {
        website.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(Constants.URL_WEBSITE));
                } catch (IOException ex) {
                    //It looks like there's a problem
                } catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
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

