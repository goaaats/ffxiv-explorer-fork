package com.fragmenterworks.ffxivextract.models;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.graph.geom.Vertex;

public class Mesh{
		
	final public int numBuffers;
	public ByteBuffer vertBuffers[];
	public ByteBuffer indexBuffer;
	
	final public int[] vertexBufferOffsets;
	final public int[] vertexSizes;
	final public int indexOffset;		
	final public int numVerts, numIndex;
	
	final public int materialNumber;
	
	final public int vertElementIndex;
	
	public Mesh(int vertCount, int indexCount, int meshNum, int[] vertexBufferOffsets,
			int indexBufferOffset, int[] vertexSizes, int numBuffers, int vertElementIndex) {
		this.numVerts = vertCount;
		this.numIndex = indexCount;
		this.vertexBufferOffsets = vertexBufferOffsets;
		this.indexOffset = indexBufferOffset;		
		this.vertElementIndex = vertElementIndex;
		this.vertexSizes = vertexSizes;
		this.numBuffers = numBuffers;
		this.materialNumber = meshNum;				
		
		this.vertBuffers = new ByteBuffer[numBuffers];
		
		for (int i = 0; i < numBuffers; i++)
			this.vertBuffers[i] = Buffers.newDirectByteBuffer(numVerts * vertexSizes[i]);
		this.indexBuffer = Buffers.newDirectByteBuffer(numIndex * 2);
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
		bb.position(lodIndexOffset);		
		bbTemp = bb.duplicate ();    	
    	bbTemp.limit (bbTemp.position() + (2 * numIndex));
    	indexBuffer.put (bbTemp);
	
	}

	public int getVertexElementIndex() {
		return vertElementIndex;
	}
}
