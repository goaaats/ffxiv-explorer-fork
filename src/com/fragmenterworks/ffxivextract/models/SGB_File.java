package com.fragmenterworks.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder; 

public class SGB_File {
	
	public String entryName;
	public String modelName;
	public String collisionName;
	
	public SGB_File(String path) throws IOException{
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		loadSGB(data);
	}

	public SGB_File(byte[] data) throws IOException {
		loadSGB(data);
	}
	
	private void loadSGB(byte[] data) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		int signature = bb.getInt();
		int filesize = bb.getInt();
		bb.getInt();
		int signature2 = bb.getInt();
		
		if (signature != 0x31424753 || signature2 != 0x314e4353)
			return;
		
		
	}
	
}
