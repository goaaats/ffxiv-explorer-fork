package com.fragmenterworks.ffxivextract.models;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.fragmenterworks.ffxivextract.Constants;
import com.jogamp.common.nio.Buffers;
import com.jogamp.graph.geom.Vertex;

public class Mesh{
		
	final public int numBuffers;
	public ByteBuffer vertBuffers[];
	public ByteBuffer indexBuffer;
	
	final public int[] vertexBufferOffsets;
	final public int[] vertexSizes;
	final public int indexBufferOffset;		
	final public int numVerts, numIndex;
	
	final public int partTableOffset, partTableCount;
	
	final public int boneListIndex;
	
	final public int materialNumber;
	
	final public int vertElementIndex;
	
	public Mesh(ByteBuffer bb, int elementIndex)
	{
		numVerts = bb.getInt();
		numIndex = bb.getInt();	    	        
    	
    	materialNumber = bb.getShort();
    	partTableOffset = bb.getShort();
    	partTableCount = bb.getShort();
    	boneListIndex = bb.getShort();
    	
    	/*if (i == 0){
    	System.out.println(String.format("0x%04x", partOffset));
    	System.out.println(partCount);
    	System.out.println(boneListIndex);
    	}*/
    	
    	indexBufferOffset = bb.getInt();
    	
    	//Seems FFXIV already stores the offset of the aux buffer (and others). DOH! Learned from Saint Coinach...
    	vertexBufferOffsets = new int[3];
    	for (int x = 0; x < vertexBufferOffsets.length; x++)
    		vertexBufferOffsets[x] = bb.getInt();        	
    		     
    	vertexSizes = new int[3];
    	for (int x = 0; x < vertexSizes.length; x++)
    		vertexSizes[x] = bb.get() & 0xFF;	      
    	
    	numBuffers = bb.get() & 0xFF;
    	
    	vertElementIndex = elementIndex;
    	
    	vertBuffers = new ByteBuffer[numBuffers];
		
		for (int i = 0; i < numBuffers; i++)
			vertBuffers[i] = Buffers.newDirectByteBuffer(numVerts * vertexSizes[i]);
		indexBuffer = Buffers.newDirectByteBuffer(numIndex * 2);
    	
		System.out.println("Num Parts: " + partTableCount);
		
    	if (Constants.DEBUG)
    	{
        	System.out.println("numVerts: " + numVerts);
        	System.out.println("numIndex: " + numIndex);	   
        	
        	System.out.println("vertOffset: " + vertexBufferOffsets[0]);
        	System.out.println("indexOffset: " + indexBufferOffset);
    	}
	}

	public void loadMeshes(ByteBuffer bb, int lodVertexOffset, int lodIndexOffset) throws BufferOverflowException, BufferUnderflowException{
		    	    
		ByteBuffer bbTemp;
		
    	//Vert Table
		for (int i = 0; i < numBuffers; i++){
	    	bb.position(lodVertexOffset + vertexBufferOffsets[i]);	
	    	bbTemp = bb.duplicate();
	    	bbTemp.limit (bbTemp.position() + ((vertexSizes[i] * numVerts)));
	    	vertBuffers[i].put(bbTemp);
		}
		//Index Table
		bb.position(lodIndexOffset + (indexBufferOffset*2));		
		bbTemp = bb.duplicate ();    	
    	bbTemp.limit (bbTemp.position() + (2 * numIndex));
    	indexBuffer.put (bbTemp);
	
	}

	public int getVertexElementIndex() {
		return vertElementIndex;
	}
}
