package ca.fraggergames.ffxivextract.gui.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import ca.fraggergames.ffxivextract.helpers.ImageDecoding.ImageDecodingException;
import ca.fraggergames.ffxivextract.models.Texture_File;

public class Image_View extends JPanel {

	Texture_File currentTexture;
	NavigableImagePanel imgPreviewCanvas;

	public Image_View(Texture_File texture) {
		currentTexture = texture;
		setLayout(new BorderLayout(0, 0));
		
		JPanel pnlTexInfo = new JPanel();
		add(pnlTexInfo, BorderLayout.NORTH);
		pnlTexInfo.setBorder(new TitledBorder(null, "Texture Info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlTexInfo.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_1 = new JScrollPane();
		pnlTexInfo.add(scrollPane_1, BorderLayout.CENTER);
		
		JList list = new JList();
		list.setAutoscrolls(false);
		list.setEnabled(false);
		
		final String[] values = new String[] {"Compression Type: " + texture.getCompressionTypeString(), "Width: " + texture.uncompressedWidth, "Height: " + texture.uncompressedHeight, "Number of MipMaps: " + texture.numMipMaps};
		
		list.setModel(new AbstractListModel() {
			
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		scrollPane_1.setViewportView(list);
		
		JPanel pnlTexPreview = new JPanel();
		pnlTexPreview.setBorder(new TitledBorder(null, "Texture Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(pnlTexPreview, BorderLayout.CENTER);
		pnlTexPreview.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		pnlTexPreview.add(scrollPane);				
		imgPreviewCanvas = new NavigableImagePanel();
		scrollPane.setViewportView(imgPreviewCanvas);
		
		setImage(0);
	}
	
	private void setImage(int index)
	{
		try {
			BufferedImage preview = currentTexture.decode(index, null);
			imgPreviewCanvas.setImage(preview);
			if (currentTexture.compressionType == 0x2460)
				imgPreviewCanvas.setHighQualityRenderingEnabled(false);			
		} catch (ImageDecodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
