package ca.fraggergames.ffxivextract.gui;

import java.awt.BorderLayout;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ca.fraggergames.ffxivextract.Strings;
import ca.fraggergames.ffxivextract.helpers.Utils;

public class SCDConverterWindow extends JFrame {

	final int SCD_HEADER_SIZE = 0x540;
	
	JPanel contentPane;
	
	public SCDConverterWindow() {
		this.setTitle(Strings.DIALOG_TITLE_SCDCONVERTER);
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
	}
	
	private void convert(String oggPath) throws IOException
	{	
		byte[] ogg = Utils.readContentIntoByteArray(new File(oggPath));
		
		float volume = 1.0f;
		int numChannels = 2;
		int sampleRate = 14200;
		int loopStart = 0;
		int loopEnd = oggPath.length();
				
		//Create Header
		byte[] header = createSCDHeader(ogg.length, volume, numChannels, sampleRate, loopStart, loopEnd);
		
		//Write out scd
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(oggPath + ".scd"));
		out.write(header);
		out.write(ogg);
		out.close();
	}
	
	private byte[] createSCDHeader(int oggLength, float volume, int numChannels, int sampleRate, int loopStart, int loopEnd)
	{
		//Load scd template scd header
		InputStream inStream = getClass().getResourceAsStream("/res/scd_header.bin");
		byte scdHeader[] = new byte[SCD_HEADER_SIZE];
		try {
			inStream.read(scdHeader);
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		//Edit the parts needed
		ByteBuffer bb = ByteBuffer.wrap(scdHeader);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(0x10);
		bb.putInt(scdHeader.length + oggLength);
		bb.position(0x1B0);
		bb.putInt(oggLength);
		
		bb.position(0xA8);
		bb.putFloat(volume);
		bb.position(0x1B4);
		bb.putInt(numChannels);
		bb.position(0x1B8);
		bb.putInt(sampleRate);
		bb.position(0x1C0);
		bb.putInt(loopStart);
		bb.position(0x1C4);
		bb.putInt(loopEnd);
				
		return scdHeader;
	}
	
	
	
}
