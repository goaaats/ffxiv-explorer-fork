package ca.fraggergames.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SCD_File {

	private int fileLength;
	private int numChannels; //1: Mono, 2: Stereo
	private int frequency;
	private int dataType; //0x0C: MS-ADPCM, 0x06: OGG
	
	
	public SCD_File(String path) throws IOException{
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		loadSCD(data);
	}

	public SCD_File(byte[] data) throws IOException {
		loadSCD(data);
	}
	
	private void loadSCD(byte[] data) throws IOException {
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.put(data);
		buffer.rewind();

		try {
			
			//Check Signature
			int sig1 = buffer.getInt();
			int sig2 = buffer.getInt();			
			if (sig1 != 0x42444553 && sig2 != 0x46435353) //SEDBSSCF in hex
				throw new IOException("Not a valid SCD file");
			
			//Check Version
			int version = buffer.getInt();			
			if (version != 3)
				throw new IOException("Only version 3 SCD files supported");
			
			buffer.rewind();
			buffer.position(0xE);
			int sizeOfHeader1 = buffer.getShort();
			int fullFileLength = buffer.getInt();
			
			//Offset Header
			buffer.rewind();
			buffer.position(sizeOfHeader1);
			
			int val1 = buffer.getShort();
			int val2 = buffer.getShort();
			int numFiles = buffer.getShort();
			int val3 = buffer.getShort();
			int address1 = buffer.getInt();
			int metaHeaderOffset_Offset = buffer.getInt();
			
			//Meta Header			
			buffer.rewind();
			buffer.position(metaHeaderOffset_Offset);
			int metaHeaderOffset = buffer.getInt();
			buffer.rewind();
			buffer.position(metaHeaderOffset);
			
			int fileDataLength = buffer.getInt();
			numChannels = buffer.getInt();
			frequency = buffer.getInt();
			dataType = buffer.getInt();
			buffer.getInt();
			buffer.getInt();
			int firstFramePosition = buffer.getInt();
			
			
		}
		catch (BufferUnderflowException underflowException) {} 
		catch (BufferOverflowException overflowException) {}
		
	}
}
