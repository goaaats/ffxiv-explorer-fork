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
			buffer.getShort();
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
			if (encodeByte != 0x00) //Decode if need to
				xorDecode(vorbisHeader, encodeByte);
			
			//Read in the rest of the music
			byte[] oggData = new byte[dataLength];
			buffer.get(oggData);
						
			byte[] dataFile = new byte[vorbisHeaderSize + dataLength];
			System.arraycopy(vorbisHeader, 0, dataFile, 0, vorbisHeaderSize);
			System.arraycopy(oggData, 0, dataFile, vorbisHeaderSize, oggData.length);
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
