package com.fragmenterworks.ffxivextract.models;

import java.nio.ByteBuffer;

//Based off of Rogueadyn's finds

public class MeshPartHeader {
	
	final public int indexOffset;
	final public int indexCount;
	final public int attributes;
	final public short boneReferenceOffset; 
	final public short boneReferenceCount; 
	
    public MeshPartHeader(ByteBuffer bb)
    {
    	indexOffset = bb.getInt();
    	indexCount = bb.getInt();
    	attributes = bb.getInt();
    	boneReferenceOffset = bb.getShort();
    	boneReferenceCount = bb.getShort();
    }
    
}
