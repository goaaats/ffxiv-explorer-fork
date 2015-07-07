package ca.fraggergames.ffxivextract.models.directx;

import java.nio.ByteBuffer;

public class D3DXShader_StructMemberInfo {

	final public String Name;
	final public D3DXShader_TypeInfo TypeInfo;
	
	protected D3DXShader_StructMemberInfo(ByteBuffer bb)
	{
		int nameOffset = bb.getInt();
		int typeInfoOffset = bb.getInt();
		
		int lastPost = bb.position();
		
		//Load in the strings that creator/target offsets point to
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
		
		//Load typeinfo
		bb.position(typeInfoOffset);
		TypeInfo = new D3DXShader_TypeInfo(bb);
		
		bb.position(lastPost);
	}
	
}
