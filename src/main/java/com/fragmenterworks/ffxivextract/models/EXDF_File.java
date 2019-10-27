package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.helpers.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EXDF_File extends Game_File {

	private byte data[];
	private EXDF_Offset entryOffsets[];
	
	public EXDF_File(byte[] data) throws IOException {
		super(ByteOrder.BIG_ENDIAN);
		loadEXDF(data);
	}

	public EXDF_File(String path) throws IOException, FileNotFoundException {
		super(ByteOrder.BIG_ENDIAN);
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

			if (magic != 0x45584446 || version != 2) {
				Utils.getGlobalLogger().error("EXDF magic was incorrect.");
				Utils.getGlobalLogger().debug("Magic was {}", String.format("0x%08X", magic));
				return;
			}

			buffer.getShort();
			
			int offsetTableSize = buffer.getInt();
			//int dataSectionSize = buffer.getInt();
			
			buffer.position(0x20);

			entryOffsets = new EXDF_Offset[offsetTableSize/8];
			for (int i = 0; i < offsetTableSize/8; i++)			
				entryOffsets[i] = new EXDF_Offset(buffer.getInt(), buffer.getInt());

		} 		
		catch (BufferUnderflowException underflowException) {} 
		catch (BufferOverflowException overflowException) {}
	}
	
	public EXDF_Entry getEntry(int index)
	{
		return new EXDF_Entry(data, entryOffsets[index].index, entryOffsets[index].offset);	
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

		int index;
		int offset;
		private byte dataChunk[];
		
		public EXDF_Entry(byte[] data, int index, int offset) {
			this.index = index;
			this.offset = offset;
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
		
		public int[] getQuad(short offset) {
			ByteBuffer buffer = ByteBuffer.wrap(dataChunk);
			buffer.position(offset);
			
			int quad[] = new int[4];
			quad[0] = buffer.getShort();
			quad[1] = buffer.getShort();
			quad[2] = buffer.getShort();
			quad[3] = buffer.getShort();
			
			return quad;
		}		
		
		public boolean getByteBool(int datatype, int offset)
		{
			ByteBuffer buffer = ByteBuffer.wrap(dataChunk);
			buffer.position(offset);
			int val = buffer.get();
			int shift = (datatype - 0x19);
			int i = 1 << shift;
			val &= i;
			return (val & i) == i;
		}
		
		public int getInt(int offset)
		{
			ByteBuffer buffer = ByteBuffer.wrap(dataChunk);
			buffer.position(offset);
			return buffer.getInt();
		}
		
		public float getFloat(short offset) {
			ByteBuffer buffer = ByteBuffer.wrap(dataChunk);
			buffer.position(offset);
			return buffer.getFloat();
		}

		public byte[] getString(int datasetChunkSize, short offset) {
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
			
			return stringBytes;
		}

		public boolean getBoolean(short offset) {
			byte b = getByte(offset);
			return b == 1;
		}		
		
		public int getIndex() {
			return index;
		}

		public boolean getByteBool(short offset2) {
			return false;
		}

	}

	public EXDF_Offset[] getIndexOffsetTable()
	{
		return entryOffsets;
	}
	
	public int getNumEntries() {
		return entryOffsets.length;
	}
}
