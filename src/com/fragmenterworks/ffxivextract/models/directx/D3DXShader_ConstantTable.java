package com.fragmenterworks.ffxivextract.models.directx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class D3DXShader_ConstantTable {

	final public int Size;
	final public String Creator;
	final public int Version;
	final public int Constants;
	final public int ConstantInfo;
	final public int Flags;
	final public String Target;
	
	final public D3DXShader_ConstantInfo constantInfo[];
	
	public static D3DXShader_ConstantTable getConstantTable(byte shaderByteCode[])
	{
		//Find CTAB
		ByteBuffer bb = ByteBuffer.wrap(shaderByteCode);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		byte commentData[];
		
		while (true)
		{
			if (bb.remaining() <= 4)
				return null;
			
			int in = bb.getInt();
			
			//Found
			if (in == 0x42415443)
			{			
				bb.position(bb.position()-6);
				int ctabSize = bb.getShort() * 4;
				commentData = new byte[ctabSize];
				bb.position(bb.position()+4);
				bb.get(commentData);
				break;
			}
			
		}		
		
		return new D3DXShader_ConstantTable(commentData);
	}
	
	private D3DXShader_ConstantTable(byte ctabSection[]) {
		ByteBuffer bb = ByteBuffer.wrap(ctabSection);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		Size = bb.getInt();
		int creatorOffset = bb.getInt(); 
		Version = bb.getInt();
		Constants = bb.getInt();
		ConstantInfo = bb.getInt();
		Flags = bb.getInt();
		int targetOffset = bb.getInt();
		
		//Load in constant info
		constantInfo = new D3DXShader_ConstantInfo[Constants];
		for (int i = 0; i < constantInfo.length;i++)		
			constantInfo[i] = new D3DXShader_ConstantInfo(bb);
		
		//Load in the strings that creator/target offsets point to
		StringBuilder sb = new StringBuilder();
		bb.position(creatorOffset);
		while(true)
		{
			char in = (char)bb.get();
			
			if (in == 0)
				break;
			else
				sb.append(in);
		}
		Creator = sb.toString();
		
		sb.setLength(0);
		bb.position(targetOffset);
		while(true)
		{
			char in = (char)bb.get();
			
			if (in == 0)
				break;
			else
				sb.append(in);
		}
		
		Target = sb.toString();
				
	}
	
}
