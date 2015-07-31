package com.fragmenterworks.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SGB_File {

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
		
	}
	
}
