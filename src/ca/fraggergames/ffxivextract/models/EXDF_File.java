package ca.fraggergames.ffxivextract.models;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EXDF_File {

	public EXDF_Offset[] offsets;
	
	public EXDF_File(byte[] data) throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
		buffer.put(data);
		buffer.rewind();
		
		//EXDF Header
		int signature = buffer.getInt(); //Should be EXDF or 0x45584446
		if (signature != 0)
			throw new IOException("Not a EXDF");
		buffer.get(); //Sig is 5 bytes so skip 1
		int version = buffer.getInt();
		int offsetSize = buffer.getInt(); 
		int dataSize = buffer.getInt();  
		//Skip to 0x1F
		
		//Offsets
		offsets = new EXDF_Offset[offsetSize/0x08];
		for (int i = 0; i < offsets.length; i++)
		{
			int x = buffer.getInt();
			int offset = buffer.getInt();			
			offsets[i] = new EXDF_Offset(offset, x);
		}
		
		//Data
		
	}	
	
	public EXDF_File(String path)
	{
		
	}

}
