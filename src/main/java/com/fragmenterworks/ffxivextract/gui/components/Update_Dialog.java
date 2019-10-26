package com.fragmenterworks.ffxivextract.gui.components;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.fragmenterworks.ffxivextract.helpers.VersionUpdater.VersionCheckObject;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.storage.HashDatabase;

public class Update_Dialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextArea txtPatchInfo;
	
	private final ActionListener listener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("openweb"))
			{
				try {
                    Desktop.getDesktop().browse(new URI(Constants.URL_WEBSITE));
                } catch (IOException ex) {
                    //It looks like there's a problem
                } catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if (e.getActionCommand().equals("close"))
			{
				Update_Dialog.this.dispose();
			}
		}
	};
	
	public Update_Dialog(VersionCheckObject checkObj) {
		setBounds(100, 100, 600, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		setTitle("New Update Found");
		
		URL imageURL = getClass().getResource("/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		setIconImage(image.getImage());
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				txtPatchInfo = new JTextArea();
				scrollPane.setViewportView(txtPatchInfo);
				txtPatchInfo.setEditable(false);
				txtPatchInfo.setLineWrap(true);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnOpenSite = new JButton("Open Website");
				btnOpenSite.setActionCommand("openweb");
				btnOpenSite.addActionListener(listener);
				buttonPane.add(btnOpenSite);
			}
			{
				JButton btnClose = new JButton("Close");
				btnClose.setActionCommand("close");
				btnClose.addActionListener(listener);
				buttonPane.add(btnClose);
				getRootPane().setDefaultButton(btnClose);
			}
		}
		
		setupMessage(checkObj);
	}

	private void setupMessage(VersionCheckObject checkObj) {
		
		StringBuilder msgBuilder = new StringBuilder();
		
		//Setup App Text
		if(Constants.APP_VERSION_CODE < checkObj.currentAppVer)
		{
			msgBuilder.append("===New App Version!===\r\n");
			msgBuilder.append("Updated On: " + checkObj.appUpdateDate + "\r\n");
			msgBuilder.append("Update Info: " + checkObj.patchDesc + "\r\n");
			msgBuilder.append("\r\n");
		}
		
		//Setup HashDB Text
		if(HashDatabase.getHashDBVersion() < checkObj.currentDbVer)
		{
			msgBuilder.append("===New Hash Database!===\r\n");
			msgBuilder.append("Updated On: " + checkObj.dbUpdateDate + "\r\n");
			msgBuilder.append("\r\n");
		}				
		
		msgBuilder.append("Get it at "+ Constants.URL_WEBSITE +".\r\n");
		
		txtPatchInfo.setText(msgBuilder.toString());
	}
	
}
