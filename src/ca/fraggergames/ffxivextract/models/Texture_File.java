package ca.fraggergames.ffxivextract.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Texture_File {

	public int compressionType;
	
	public int numMipMaps;
	
	public int width;
	public int height;
	
	public int dataStart;
	
	public int compressedWidth;
	public int compressedHeight;
		
	public Texture_File(byte data[]) {
		
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.getInt(); //Uknown
		compressionType = bb.get();
		numMipMaps = bb.get();
		bb.getShort();
		width = bb.getShort();
		height = bb.getShort();
		dataStart = bb.getInt();
		compressedWidth = bb.getInt();
		compressedHeight = bb.getInt();
	}

}
