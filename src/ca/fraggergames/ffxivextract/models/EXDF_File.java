package ca.fraggergames.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class EXDF_File {

	private byte data[];
	private byte entryOffsets[];
	
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
			
		} 
		
		catch (BufferUnderflowException underflowException) {} 
		catch (BufferOverflowException overflowException) {}
	}
	
	public EXDF_Entry getEntry(int index)
	{
		if (index >= entryOffsets.length)
			return null;
		
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
	
	public static class EXDF_Entry{
		
	}
}
