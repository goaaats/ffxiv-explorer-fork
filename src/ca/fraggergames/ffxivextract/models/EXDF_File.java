package ca.fraggergames.ffxivextract.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharsetDecoder;

public class EXDF_File {

	private byte data[];
	private EXDF_Offset entryOffsets[];
	
	public EXDF_File(byte[] data) throws IOException {
		loadEXDF(data);
	}

	public EXDF_File(String path) throws IOException, FileNotFoundException {
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		loadEXDF(data);
	}

	private void loadEXDF(byte[] data) throws IOException {
		this.data = data;
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		try {
			
			int magic = buffer.getInt();
			int version = buffer.getShort();
			
			if (magic != 0x45584446 || version != 2)
				throw new IOException("Not a EXDF");
			
			buffer.getShort();
			
			int offsetTableSize = buffer.getInt();
			int dataSectionSize = buffer.getInt();
			
			buffer.position(0x20);
			
			//Load offsets			
			entryOffsets = new EXDF_Offset[offsetTableSize/8];
			for (int i = 0; i < offsetTableSize/8; i++)			
				entryOffsets[i] = new EXDF_Offset(buffer.getInt(), buffer.getInt());
		
			
		} 		
		catch (BufferUnderflowException underflowException) {} 
		catch (BufferOverflowException overflowException) {}
	}
	
	public EXDF_Entry getEntry(int index)
	{
		for (int i = 0; i < entryOffsets.length; i++)
		{
			if (index == entryOffsets[i].index)
				return new EXDF_Entry(data, entryOffsets[i].offset);
		}
		
		return null;
	}
	
	public static class EXDF_DataBlock{		
		private byte data[];
		
		public EXDF_DataBlock(byte[] dataBlock){
			this.data = dataBlock;
		}
		
		public byte[] getData(){
			return data;
		}
		
	}

	public static class EXDF_Offset{
		public final int index;
		public final int offset;
		
		public EXDF_Offset(int index, int offset)
		{
			this.index = index;
			this.offset = offset;
		}
	}
	
	public byte[] getRawData()
	{
		return data;
	}
	
	public static class EXDF_Entry{

		private byte dataChunk[];
		
		public EXDF_Entry(byte[] data, int offset) {
			ByteBuffer buffer = ByteBuffer.wrap(data);
			buffer.position(offset);
			int size = buffer.getInt();
			dataChunk = new byte[size];
			buffer.getShort();
			buffer.get(dataChunk);
		}
		
		public byte[] getRawData()
		{
			return dataChunk;
		}
		
		public byte getByte(int offset)
		{
			ByteBuffer buffer = ByteBuffer.wrap(dataChunk);
			buffer.position(offset);
			return buffer.get();
		}
		
		public short getShort(int offset)
		{
			ByteBuffer buffer = ByteBuffer.wrap(dataChunk);
			buffer.position(offset);
			return buffer.getShort();
		}
		
		public int getInt(int offset)
		{
			ByteBuffer buffer = ByteBuffer.wrap(dataChunk);
			buffer.position(offset);
			return buffer.getInt();
		}

		public String getString(int datasetChunkSize, short offset) {
			ByteBuffer buffer = ByteBuffer.wrap(dataChunk);
			buffer.position(offset);
			int stringOffset = buffer.getInt();
			buffer.position(datasetChunkSize + stringOffset);
			
			//Find the null terminator
			int nullTermPos = -1;
			while (true)
			{
				byte in = buffer.get();
				if (in == 0x00)
				{
					nullTermPos = buffer.position()-(datasetChunkSize+stringOffset);
					break;
				}
			}
			
			//Read in
			byte stringBytes[] = new byte[nullTermPos-1];
			buffer.position(datasetChunkSize + stringOffset);
			buffer.get(stringBytes);
			
			try {
				return new String(stringBytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			return "";
		}
	}
}
