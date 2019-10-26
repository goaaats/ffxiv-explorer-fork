package com.fragmenterworks.ffxivextract.models;

import java.nio.ByteBuffer;

public class BoneList {

	final public short boneList[] = new short[0x40];
	final public int boneCount;
	
	public BoneList(ByteBuffer bb)
	{
		for (int i = 0; i < boneList.length;i++)
			boneList[i] = bb.getShort();
		boneCount = bb.getInt();
	}
	
}
