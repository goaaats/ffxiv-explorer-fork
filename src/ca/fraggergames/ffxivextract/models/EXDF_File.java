package ca.fraggergames.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class EXDF_File {

	public EXDF_Offset[] offsets;
	public EXDF_StringEntry[] strings;

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

	private void loadEXDF(byte[] data) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
		buffer.put(data);
		buffer.rewind();

		// EXDF Header
		int signature = buffer.getInt(); 
		if (signature != 0x45584446) // Should be EXDF or 0x45584446
			throw new IOException("Not a EXDF");
		int version = buffer.getInt(); //I assume, always 0x00200000
		int offsetSize = buffer.getInt();
		int dataSize = buffer.getInt();
		
		buffer.rewind();
		buffer.position(0x20);

		// Offsets
		offsets = new EXDF_Offset[offsetSize / 0x08];
		for (int i = 0; i < offsets.length; i++) {
			int x = buffer.getInt();
			int offset = buffer.getInt();
			offsets[i] = new EXDF_Offset(offset, x);
		}

		// Data
		strings = new EXDF_StringEntry[offsetSize / 0x08];
		for (int i = 0; i < offsets.length; i++)
		{
			buffer.rewind();
			buffer.position(offsets[i].offset);

			String stringName;
			String stringValue;
			
			int entrySize = buffer.getInt();
						
			buffer.getShort(); //Skip 2 bytes (marker?
			buffer.getInt(); //Will be null
			int nameSize = buffer.getInt();
			
			int valueSize = entrySize - nameSize - 0x0A;
			
			//Get Name
			if (nameSize > 0)
			{
				byte[] string1 = new byte[nameSize];
				buffer.get(string1);
				stringName = new String(string1);
			}
			else
				stringName = "";
			
			//Get Value
			if (valueSize > 0){
			byte[] string2 = new byte[valueSize];
				buffer.get(string2);
				stringValue = new String(string2);
			}			
			else
				stringValue = "";
			
			strings[i] = new EXDF_StringEntry(stringName, stringValue);
			System.out.println(strings[i].name + ": " + strings[i].value);
		}
		
	}	

}
