package ca.fraggergames.ffxivextract.models;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.graph.geom.Vertex;

public class Mesh{
	
	public ByteBuffer vertBuffer;
	public ByteBuffer indexBuffer;
	
	public int vertOffset, indexOffset;		
	public int numVerts, numIndex;
	
	public int vertexSize, auxVertexSize, unknownSize, indexSize;
	
	public int materialNumber;
	
	public Mesh(int vertCount, int indexCount, int meshNum, int vertexBufferOffset,
			int indexBufferOffset, int sizeinfo) {
		this.numVerts = vertCount;
		this.numIndex = indexCount;
		this.vertOffset = vertexBufferOffset;
		this.indexOffset = indexBufferOffset;		
		
		this.materialNumber = meshNum;
		
		this.vertexSize = (sizeinfo >> 8*0) & 0xFF;
		this.auxVertexSize = (sizeinfo >> 8*1) & 0xFF;
		this.unknownSize = (sizeinfo >> 8*2) & 0xFF;
		this.indexSize = (sizeinfo >> 8*3) & 0xFF;		
		
		this.vertBuffer = Buffers.newDirectByteBuffer(numVerts * (vertexSize+auxVertexSize));
		this.indexBuffer = Buffers.newDirectByteBuffer(numIndex * indexSize);
	}

	public void loadMeshes(ByteBuffer bb, int lodVertexOffset, int lodIndexOffset) throws BufferOverflowException, BufferUnderflowException{
		    	    	
    	//Vert Table
    	bb.position(lodVertexOffset +vertOffset);
    	ByteBuffer bbTemp = bb.duplicate ();    	
    	bbTemp.limit (bbTemp.position() + ((auxVertexSize + vertexSize) * numVerts));
    	vertBuffer.put (bbTemp);
    
		//Index Table
		bb.position(lodIndexOffset + (indexOffset * 2));		
		bbTemp = bb.duplicate ();    	
    	bbTemp.limit (bbTemp.position() + (indexSize * numIndex));
    	indexBuffer.put (bbTemp);
	
	}
}
