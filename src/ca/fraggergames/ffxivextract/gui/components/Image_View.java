package ca.fraggergames.ffxivextract.gui.components;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import javax.swing.border.TitledBorder;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.AbstractListModel;

import ca.fraggergames.ffxivextract.helpers.ImageDecoding.ImageDecodingException;
import ca.fraggergames.ffxivextract.models.Texture_File;

import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Image_View extends JPanel {

	NavigableImagePanel imgPreviewCanvas;

	public Image_View(Texture_File texture) {
		setLayout(new BorderLayout(0, 0));
		
		JPanel pnlTexInfo = new JPanel();
		add(pnlTexInfo, BorderLayout.NORTH);
		pnlTexInfo.setBorder(new TitledBorder(null, "Texture Info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlTexInfo.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_1 = new JScrollPane();
		pnlTexInfo.add(scrollPane_1, BorderLayout.NORTH);
		
		JList list = new JList();
		list.setAutoscrolls(false);
		list.setEnabled(false);
		
		final String[] values = new String[] {"Compression Type: " + texture.getCompressionTypeString(), "Width: " + texture.uncompressedWidth, "Height: " + texture.uncompressedHeight};
		
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
		pnlTexPreview.setLayout(new BoxLayout(pnlTexPreview, BoxLayout.X_AXIS));
		
		JScrollPane scrollPane = new JScrollPane();
		pnlTexPreview.add(scrollPane);		
		
		try {
			BufferedImage preview = texture.decode(0, null);
			imgPreviewCanvas = new NavigableImagePanel(preview);
			if (texture.compressionType == 96)
				imgPreviewCanvas.setHighQualityRenderingEnabled(false);
			scrollPane.setViewportView(imgPreviewCanvas);
		} catch (ImageDecodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
