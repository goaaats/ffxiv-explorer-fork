package com.fragmenterworks.ffxivextract.models.directx;

import java.nio.ByteBuffer;

public class D3DXShader_ConstantInfo {

	final public String Name;
	final public int RegisterSet;
	final public int RegisterIndex;
	final public int RegisterCount;
	final public int Reserved;
	final public D3DXShader_TypeInfo TypeInfo;
	final public int DefaultValue;
	
	protected D3DXShader_ConstantInfo(ByteBuffer bb)
	{
		int nameOffset = bb.getInt();
		RegisterSet = bb.getShort();
		RegisterIndex = bb.getShort();
		RegisterCount = bb.getShort();
		Reserved = bb.getShort();
		int typeInfoOffset = bb.getInt();
		DefaultValue = bb.getInt();
		
		int lastPos = bb.position();
		
		//Load in name string
		StringBuilder sb = new StringBuilder();
		bb.position(nameOffset);
		while(true)
		{
			char in = (char)bb.get();
			
			if (in == 0)
				break;
			else
				sb.append(in);
		}
		Name = sb.toString();
		
		//Load in type info
		bb.position(typeInfoOffset);
		TypeInfo = new D3DXShader_TypeInfo(bb);		
		bb.position(lastPos);
	}

}
