package com.fragmenterworks.ffxivextract.models;

import java.nio.ByteBuffer;
import java.util.ArrayList;

//Based off of Rogueadyn's finds

public class MeshPart {
	
	final public int indexOffset;
	final public int indexCount;
	final public int attributes;	
	final public short boneReferenceOffset; 
	final public short boneReferenceCount; 
	
	public ArrayList<Long> attributeMasks = new ArrayList<Long>();
	
    public MeshPart(ByteBuffer bb)
    {
    	indexOffset = bb.getInt();
    	indexCount = bb.getInt();
    	attributes = bb.getInt();
    	boneReferenceOffset = bb.getShort();
    	boneReferenceCount = bb.getShort();
    }
    
}
