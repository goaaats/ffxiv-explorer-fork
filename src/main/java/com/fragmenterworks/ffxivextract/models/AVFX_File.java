package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.helpers.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class AVFX_File extends Game_File {

	public int fileSize;
	public ArrayList<AVFX_Packet> packets = new ArrayList<AVFX_Packet>();
		
	public AVFX_File(byte[] data, ByteOrder endian) {
		super(endian);
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(endian);
		
		bb.getInt(); //Signature
		fileSize = bb.getInt(); //File Size
		
		while(bb.hasRemaining())
			packets.add(new AVFX_Packet(bb));
	}
	
	class AVFX_Packet {
		byte[] tag = new byte[4];
		int dataSize;
		byte data[];
		
		public AVFX_Packet(ByteBuffer inBuff)
		{			
			inBuff.get(tag);		
			
			dataSize = inBuff.getInt();				
			
			//Datasizes for strings are all fucked up
			if (tag[0] == 0x78 && tag[1] == 0x65 && tag[2] == 0x54)
			{		
				int increment = 0;
				int curPos = inBuff.position();
				inBuff.position(curPos + dataSize);
				
				while (inBuff.hasRemaining() && inBuff.get() == 0x0)
					increment++;
				
				dataSize += increment;
				
				inBuff.position(curPos);				
			}
			//End this hack
			
			data = new byte[dataSize];
			inBuff.get(data);
			
		}
		
		@Override
		public String toString() {			
			
			String string = "";
			
			for (int i = 0; i < 4; i++)
				string += String.format("%02x, ", data[i]);
			
			return new StringBuffer(new String(tag)).reverse().toString().trim() + " : " + (data.length == 4 ? string : "");
		}
	}
	
	public void printOut() {
		for (AVFX_Packet ap : packets)
			Utils.getGlobalLogger().trace(ap);
	}
	
}
