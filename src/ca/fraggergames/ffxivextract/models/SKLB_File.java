package ca.fraggergames.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SKLB_File {

	private byte havokData[];
	
	public SKLB_File(String path) throws IOException{
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		loadSKLB(data);
	}

	public SKLB_File(byte[] data) throws IOException {
		loadSKLB(data);
	}

	private void loadSKLB(byte[] data) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		//HEADER
		if (bb.getInt() != 0x736b6c62)
			throw new IOException("Not a SKLB");
		
		bb.getInt();
		int offsetToSkelSection = bb.getShort();
		int offsetToHavokFile = bb.getShort();
		bb.getShort();
		bb.getShort();		
		bb.getInt();
		bb.getInt();
		
		//SKEL INFO SECTION		
		
		//HAVOK FILE
		bb.position(offsetToHavokFile);
		havokData = new byte[bb.limit()-offsetToHavokFile];
		bb.get(havokData);
		
	}	
	
	public byte[] getHavokData()
	{
		return havokData;
	}
	
}
