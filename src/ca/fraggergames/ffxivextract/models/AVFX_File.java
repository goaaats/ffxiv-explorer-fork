package ca.fraggergames.ffxivextract.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class AVFX_File {

	public int fileSize;
	public ArrayList<AVFX_Packet> packets = new ArrayList<AVFX_Packet>();
		
	public AVFX_File(byte[] data)
	{
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		bb.getInt(); //Signature
		fileSize = bb.getInt(); //File Size
		
		while(bb.hasRemaining())
			packets.add(new AVFX_Packet(bb));
				
	}
	
	class AVFX_Packet{
		byte[] tag = new byte[4];
		int dataSize;
		byte data[];
		
		public AVFX_Packet(ByteBuffer inBuff)
		{
			inBuff.get(tag);
			dataSize = inBuff.getInt();
			data = new byte[dataSize];
			inBuff.get(data);
			
			printOut();
		}
		
		@Override
		public String toString() {			
			return new StringBuffer(new String(tag)).reverse().toString().trim();
		}		
		
	}
	
	public void printOut()
	{
		for (AVFX_Packet ap : packets)
			System.out.println(ap);
	}
	
}
