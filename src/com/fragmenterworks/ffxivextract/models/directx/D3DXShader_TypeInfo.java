package com.fragmenterworks.ffxivextract.models.directx;

import java.nio.ByteBuffer;

public class D3DXShader_TypeInfo {

	public final short Class;
	public final short Type;
	public final short Rows;
	public final short Columns;
	public final short Elements;
	public final short StructMembers;
	
	public final D3DXShader_StructMemberInfo StructMemberInfo[];
	
	protected D3DXShader_TypeInfo(ByteBuffer bb)
	{
		Class = bb.getShort();
		Type = bb.getShort();
		Rows = bb.getShort();
		Columns = bb.getShort();
		Elements = bb.getShort();
		StructMembers = bb.getShort();				
		int structMemberInfoOffset = bb.getInt();
		StructMemberInfo = new D3DXShader_StructMemberInfo[StructMembers];
		
		int oldPos = bb.position();
		bb.position(structMemberInfoOffset);
		for (int i = 0; i<StructMembers;i++)
			StructMemberInfo[i] = new D3DXShader_StructMemberInfo(bb);
		bb.position(oldPos);		
	}
	
}
