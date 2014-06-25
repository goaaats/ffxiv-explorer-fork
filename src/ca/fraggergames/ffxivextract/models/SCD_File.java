package ca.fraggergames.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SCD_File {
	
	SCD_Sound_Info soundInfo;
	private byte[] oggVorbisFile;
	
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
			int sizeOfSCDHeader = buffer.getShort();
			int fullFileLength = buffer.getInt();
			
			//Offset Header
			buffer.rewind();
			buffer.position(sizeOfSCDHeader);
			
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
			
			int oggDataLength = buffer.getInt();
			int numChannels = buffer.getInt();
			int frequency = buffer.getInt();
			int dataType = buffer.getInt();
			int loopStart = buffer.getInt();
			int loopEnd = buffer.getInt();			
			int firstFramePosition = buffer.getInt(); //Add to after header for first frame
			buffer.getInt();
			
			soundInfo = new SCD_Sound_Info(numChannels, frequency, dataType, loopStart, loopEnd);
			
			//Seek Table Header			
			buffer.getShort();
			int encodeByte = buffer.getShort();
			buffer.getInt();
			buffer.getInt();
			buffer.getInt();
			int seekTableSize = buffer.getInt();
			int vorbisHeaderSize = buffer.getInt();
			buffer.getInt();
			
			//Vorbis Header + Data
			buffer.rewind();
			buffer.position(metaHeaderOffset + 0x40 + seekTableSize);
			
			//Read in Vorbis header, decode if
			byte[] vorbisHeader = new byte[vorbisHeaderSize];
			buffer.get(vorbisHeader);
			if (encodeByte != 0x00) //Decode if need to
				xorDecode(vorbisHeader, encodeByte);
			
			//Read in the rest of the music
			byte[] oggData = new byte[oggDataLength];
			buffer.get(oggData);
						
			oggVorbisFile = new byte[vorbisHeaderSize + oggDataLength];
			System.arraycopy(vorbisHeader, 0, oggVorbisFile, 0, vorbisHeaderSize);
			System.arraycopy(oggData, 0, oggVorbisFile, vorbisHeaderSize, oggData.length);
		}
		catch (BufferUnderflowException underflowException) {} 
		catch (BufferOverflowException overflowException) {}
		
	}

	private void xorDecode(byte[] vorbisHeader, int encodeByte) {
		for (int i = 0; i < vorbisHeader.length; i++)
			vorbisHeader[i] ^= encodeByte;
	}
	
	public byte[] getData(){
		return oggVorbisFile;
	}
	
	public SCD_Sound_Info getSoundInfo()
	{
		return soundInfo;
	}
	
	public static class SCD_Sound_Info{
		public final int numChannels; //1: Mono, 2: Stereo
		public final int frequency;
		public final int dataType; //0x0C: MS-ADPCM, 0x06: OGG
		public final int loopStart;
		public final int loopEnd;
		
		public SCD_Sound_Info(int numChannels, int frequency, int dataType, int loopStart, int loopEnd)
		{
			this.numChannels = numChannels;
			this.frequency = frequency;
			this.dataType = dataType;
			this.loopStart = loopStart;
			this.loopEnd = loopEnd;
		}
	}	
}
