package ca.fraggergames.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.mysql.jdbc.util.Base64Decoder;

public class SCD_File {
		
	final static int[] XORTABLE = { 0x003A, 0x0032, 0x0032, 0x0032, 0x0003, 0x007E, 0x0012,
		0x00F7, 0x00B2, 0x00E2, 0x00A2, 0x0067, 0x0032, 0x0032, 0x0022, 0x0032, 0x0032, 0x0052,
		0x0016, 0x001B, 0x003C, 0x00A1, 0x0054, 0x007B, 0x001B, 0x0097, 0x00A6, 0x0093, 0x001A,
		0x004B, 0x00AA, 0x00A6, 0x007A, 0x007B, 0x001B, 0x0097, 0x00A6, 0x00F7, 0x0002, 0x00BB,
		0x00AA, 0x00A6, 0x00BB, 0x00F7, 0x002A, 0x0051, 0x00BE, 0x0003, 0x00F4, 0x002A, 0x0051,
		0x00BE, 0x0003, 0x00F4, 0x002A, 0x0051, 0x00BE, 0x0012, 0x0006, 0x0056, 0x0027, 0x0032,
		0x0032, 0x0036, 0x0032, 0x00B2, 0x001A, 0x003B, 0x00BC, 0x0091, 0x00D4, 0x007B, 0x0058,
		0x00FC, 0x000B, 0x0055, 0x002A, 0x0015, 0x00BC, 0x0040, 0x0092, 0x000B, 0x005B, 0x007C,
		0x000A, 0x0095, 0x0012, 0x0035, 0x00B8, 0x0063, 0x00D2, 0x000B, 0x003B, 0x00F0, 0x00C7,
		0x0014, 0x0051, 0x005C, 0x0094, 0x0086, 0x0094, 0x0059, 0x005C, 0x00FC, 0x001B, 0x0017,
		0x003A, 0x003F, 0x006B, 0x0037, 0x0032, 0x0032, 0x0030, 0x0032, 0x0072, 0x007A, 0x0013,
		0x00B7, 0x0026, 0x0060, 0x007A, 0x0013, 0x00B7, 0x0026, 0x0050, 0x00BA, 0x0013, 0x00B4,
		0x002A, 0x0050, 0x00BA, 0x0013, 0x00B5, 0x002E, 0x0040, 0x00FA, 0x0013, 0x0095, 0x00AE,
		0x0040, 0x0038, 0x0018, 0x009A, 0x0092, 0x00B0, 0x0038, 0x0000, 0x00FA, 0x0012, 0x00B1,
		0x007E, 0x0000, 0x00DB, 0x0096, 0x00A1, 0x007C, 0x0008, 0x00DB, 0x009A, 0x0091, 0x00BC,
		0x0008, 0x00D8, 0x001A, 0x0086, 0x00E2, 0x0070, 0x0039, 0x001F, 0x0086, 0x00E0, 0x0078,
		0x007E, 0x0003, 0x00E7, 0x0064, 0x0051, 0x009C, 0x008F, 0x0034, 0x006F, 0x004E, 0x0041,
		0x00FC, 0x000B, 0x00D5, 0x00AE, 0x0041, 0x00FC, 0x000B, 0x00D5, 0x00AE, 0x0041, 0x00FC,
		0x003B, 0x0070, 0x0071, 0x0064, 0x0033, 0x0032, 0x0012, 0x0032, 0x0032, 0x0036, 0x0070,
		0x0034, 0x002B, 0x0056, 0x0022, 0x0070, 0x003A, 0x0013, 0x00B7, 0x0026, 0x0060, 0x00BA,
		0x001B, 0x0094, 0x00AA, 0x0040, 0x0038, 0x0000, 0x00FA, 0x00B2, 0x00E2, 0x00A2, 0x0067,
		0x0032, 0x0032, 0x0012, 0x0032, 0x00B2, 0x0032, 0x0032, 0x0032, 0x0032, 0x0075, 0x00A3,
		0x0026, 0x007B, 0x0083, 0x0026, 0x00F9, 0x0083, 0x002E, 0x00FF, 0x00E3, 0x0016, 0x007D,
		0x00C0, 0x001E, 0x0063, 0x0021, 0x0007, 0x00E3, 0x0001 };
	
	SCD_Sound_Info soundInfo;
	private int soundEntryOffsets[];
	private byte[] scdFile;
	
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
		
		scdFile = data;
		
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
			
			int sizeofTable0 = buffer.getShort();
			int sizeofTable1 = buffer.getShort();
			int sizeofSoundTable = buffer.getShort();
			int val3 = buffer.getShort();
			int table0Offset = buffer.getInt();
			int soundEntryOffsetTable_Offset = buffer.getInt();
			int table1Offset = buffer.getInt();
			buffer.getInt();
			int unknownOffset1 = buffer.getInt();
			
			//Load Sound Entry Offsets
			soundEntryOffsets = new int[sizeofSoundTable];
			buffer.rewind();
			buffer.position(soundEntryOffsetTable_Offset);
			for (int i = 0; i < sizeofSoundTable; i++)
				soundEntryOffsets[i] = buffer.getInt();
						
		}
		catch (BufferUnderflowException underflowException) {} 
		catch (BufferOverflowException overflowException) {}
		
	}	

	private void xorDecode(byte[] vorbisHeader, int encodeByte) {
		for (int i = 0; i < vorbisHeader.length; i++)
			vorbisHeader[i] ^= encodeByte;
	}
	
	private void xorDecodeFromTable(byte[] dataFile, int dataLength) {
		int byte1 = dataLength & 0xFF & 0x7F;
		int byte2 = byte1 & 0x3F;
		for (int i = 0; i < dataFile.length; i++)
		{			
			int xorByte = XORTABLE[(byte2 + i) & 0xFF];
			xorByte &= 0xFF;
			xorByte ^= (dataFile[i]&0xFF);
			xorByte ^= byte1;
			dataFile[i] = (byte) xorByte;
		}
	}
	
	public byte[] getRawData(){		
		return scdFile;
	}
	
	public SCD_Sound_Info getSoundInfo(int index)
	{
		if (index > soundEntryOffsets.length-1)
			return null;
		
		//Get to Sound Entry Header	
		ByteBuffer buffer = ByteBuffer.wrap(scdFile);
		buffer.order(ByteOrder.LITTLE_ENDIAN);		
		buffer.position(soundEntryOffsets[index]);
		
		//Read in Entry header
		int dataLength = buffer.getInt();
		
		//Exception for placeholders
		if (dataLength == 0)
			return null;
		
		int numChannels = buffer.getInt();
		int frequency = buffer.getInt();
		int dataType = buffer.getInt();
		int loopStart = buffer.getInt();
		int loopEnd = buffer.getInt();			
		int firstFramePosition = buffer.getInt(); //Add to after header for first frame
		int numAuxChunks = buffer.getShort();
		buffer.getShort();
		
		SCD_Sound_Info soundInfo = new SCD_Sound_Info(dataLength, numChannels, frequency, dataType, loopStart, loopEnd, firstFramePosition);
		
		return soundInfo;
	}
	
	public byte[] getConverted(int index)
	{
		if (index > soundEntryOffsets.length-1)
			return null;
		
		//Get to Sound Entry Header	
		ByteBuffer buffer = ByteBuffer.wrap(scdFile);
		buffer.order(ByteOrder.LITTLE_ENDIAN);		
		buffer.position(soundEntryOffsets[index]);
		
		//Read in Entry header
		int dataLength = buffer.getInt();
		
		//Exception for placeholders
		if (dataLength == 0)
			return null;
		
		int numChannels = buffer.getInt();
		int frequency = buffer.getInt();
		int dataType = buffer.getInt();
		int loopStart = buffer.getInt();
		int loopEnd = buffer.getInt();			
		int firstFramePosition = buffer.getInt(); //Add to after header for first frame
		int numAuxChunks = buffer.getShort();
		buffer.getShort();

		//Skip any aux chunks
		int chunkStartPos = buffer.position();
		int chunkEndPos = chunkStartPos;
		for (int i = 0; i < numAuxChunks; i++)
		{
			buffer.getInt();
			chunkEndPos += buffer.getInt();
			buffer.position(chunkEndPos);
		}
		
		if (dataType == 0x06){
			//Seek Table Header			
			int encodeMode = buffer.getShort();
			int encodeByte = buffer.getShort();
			buffer.getInt();
			buffer.getInt();
			buffer.getInt();
			int seekTableSize = buffer.getInt();
			int vorbisHeaderSize = buffer.getInt();
			buffer.getInt();
			//1c6c
			//Vorbis Header + Data
			buffer.position(chunkEndPos + 0x20 + seekTableSize);
			
			//Read in Vorbis header, decode if
			byte[] vorbisHeader = new byte[vorbisHeaderSize];
			buffer.get(vorbisHeader);
			
			if (encodeMode == 0x2002)
			{
				if (encodeByte != 0x00) //Decode if need to
					xorDecode(vorbisHeader, encodeByte);
			}
			
			//Read in the rest of the music
			byte[] oggData = new byte[dataLength];
			buffer.get(oggData);
						
			byte[] dataFile = new byte[vorbisHeaderSize + dataLength];
			System.arraycopy(vorbisHeader, 0, dataFile, 0, vorbisHeaderSize);
			System.arraycopy(oggData, 0, dataFile, vorbisHeaderSize, oggData.length);
			
			if (encodeMode == 0x2003)
				xorDecodeFromTable(dataFile, dataLength);
			
			return dataFile;
		}
		else if (dataType == 0x0C)
		{
			byte waveHeader[] = new byte[16];
			byte data[] = new byte[dataLength];
			buffer.get(waveHeader);
			buffer.position(chunkStartPos+firstFramePosition);
			buffer.get(data);
			
			byte waveFile[] = new byte[8+36+data.length];
			ByteBuffer out = ByteBuffer.wrap(waveFile);
			out.order(ByteOrder.LITTLE_ENDIAN);
			out.putInt(0x46464952); //"RIFF"
			out.putInt(36 + data.length);
			out.putInt(0x45564157); //"WAVE"
			out.putInt(0x20746D66); //"fmt "
			out.putInt(waveHeader.length);
			out.put(waveHeader);
			out.putInt(0x61746164); //"data"
			out.putInt(data.length);
			
			out.put(data);

			return waveFile;					
		}
		else
			return null;
	}

	public static class SCD_Sound_Info{
		public final int numChannels; //1: Mono, 2: Stereo
		public final int frequency;
		public final int dataType; //0x0C: MS-ADPCM, 0x06: OGG
		public final int loopStart;
		public final int loopEnd;
		public final int firstFrame;
		public final int fileSize;
		
		public SCD_Sound_Info(int fileSize, int numChannels, int frequency, int dataType, int loopStart, int loopEnd, int firstFrame)
		{
			this.fileSize = fileSize;
			this.numChannels = numChannels;
			this.frequency = frequency;
			this.dataType = dataType;
			this.loopStart = loopStart;
			this.loopEnd = loopEnd;
			this.firstFrame = firstFrame;
		}
	}

	public int getNumEntries() {
		return soundEntryOffsets.length;
	}

	public byte[] getADPCMData(int index) {
		//Get to Sound Entry Header	
		ByteBuffer buffer = ByteBuffer.wrap(scdFile);
		buffer.order(ByteOrder.LITTLE_ENDIAN);		
		buffer.position(soundEntryOffsets[index]);
		
		//Read in Entry header
		int dataLength = buffer.getInt();
		
		//Exception for placeholders
		if (dataLength == 0)
			return null;
		
		int numChannels = buffer.getInt();
		int frequency = buffer.getInt();
		int dataType = buffer.getInt();
		int loopStart = buffer.getInt();
		int loopEnd = buffer.getInt();			
		int firstFramePosition = buffer.getInt(); //Add to after header for first frame		
		int numAuxChunks = buffer.getShort();
		buffer.getShort();
		
		//Skip any aux chunks
		int chunkStartPos = buffer.position();
		int chunkEndPos = chunkStartPos;
		for (int i = 0; i < numAuxChunks; i++)
		{
			buffer.getInt();
			chunkEndPos += buffer.getInt();
			buffer.position(chunkEndPos);
		}
		
		byte data[] = new byte[dataLength];
		buffer.position(chunkStartPos+firstFramePosition);
		buffer.get(data);
		return data;
	}

	public byte[] getADPCMHeader(int index) {
		//Get to Sound Entry Header	
		ByteBuffer buffer = ByteBuffer.wrap(scdFile);
		buffer.order(ByteOrder.LITTLE_ENDIAN);		
		buffer.position(soundEntryOffsets[index]);
		
		//Read in Entry header
		int dataLength = buffer.getInt();
		
		//Exception for placeholders
		if (dataLength == 0)
			return null;
		
		int numChannels = buffer.getInt();
		int frequency = buffer.getInt();
		int dataType = buffer.getInt();
		int loopStart = buffer.getInt();
		int loopEnd = buffer.getInt();			
		int firstFramePosition = buffer.getInt(); //Add to after header for first frame		
		int numAuxChunks = buffer.getShort();
		buffer.getShort();
		
		//Skip any aux chunks
		int chunkStartPos = buffer.position();
		int chunkEndPos = chunkStartPos;
		for (int i = 0; i < numAuxChunks; i++)
		{
			buffer.getInt();
			chunkEndPos += buffer.getInt();
			buffer.position(chunkEndPos);
		}		
		byte[] header = new byte[0x10];
		buffer.get(header);
		return header;
	}		
	
}
