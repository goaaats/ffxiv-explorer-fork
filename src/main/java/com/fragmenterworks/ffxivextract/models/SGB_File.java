package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.helpers.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder; 

public class SGB_File extends Game_File {
	
	public String entryName;
	public String modelName;
	public String collisionName;
	
	public SGB_File(String path, ByteOrder endian) throws IOException{
		super(endian);
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		loadSGB(data);
	}

	public SGB_File(byte[] data, ByteOrder endian) throws IOException {
		super(endian);
		loadSGB(data);
	}
	
	private void loadSGB(byte[] data) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(endian);
		int signature = bb.getInt();
		int filesize = bb.getInt();
		bb.getInt();
		int signature2 = bb.getInt();

		if (signature != 0x31424753) {
			Utils.getGlobalLogger().error("SGB1 magic was incorrect.");
			Utils.getGlobalLogger().debug("Magic was {}", String.format("0x%08X", signature));
			return;
		} else if (signature2 != 0x314E4353) {
			Utils.getGlobalLogger().error("SCN1 magic was incorrect.");
			Utils.getGlobalLogger().debug("Magic was {}", String.format("0x%08X", signature2));
			return;
		}
	}
}
