package ca.fraggergames.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PAP_File {

	public PAP_File(String path) throws IOException{
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		loadPAP(data);
	}

	public PAP_File(byte[] data) throws IOException {
		loadPAP(data);
	}

	private void loadPAP(byte[] data) {
		
	}
	
}
