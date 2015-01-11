package ca.fraggergames.ffxivextract.models;

import java.nio.ByteBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.graph.geom.Vertex;

public class Mesh{
	
	public ByteBuffer vertBuffer;
	public ByteBuffer indexBuffer;
	
	public int vertOffset, indexOffset;		
	public int numVerts, numIndex;
	
	public int[] boneWeight;
	public int[] boneIndex;
	
	public Mesh(int vertCount, int indexCount, int vertexBufferOffset,
			int indexBufferOffset, int vertexSize) {
		this.numVerts = vertCount;
		this.numIndex = indexCount;
		this.vertOffset = vertexBufferOffset;
		this.indexOffset = indexBufferOffset;
		
		this.vertBuffer = Buffers.newDirectByteBuffer(numVerts * (16+24));
		this.indexBuffer = Buffers.newDirectByteBuffer(numIndex * 2);
	}

	public void loadMeshes(ByteBuffer bb, int lodVertexOffset, int lodIndexOffset){
		
    	boneWeight = new int[numVerts];
    	boneIndex = new int[numVerts];
    	    	
    	bb.position(lodVertexOffset +vertOffset);
    	
    	byte buffer[] = new byte[8];
    	
		for (int i = 0; i < numVerts; i++)
		{			
			bb.get(buffer);
			vertBuffer.put(buffer);
			boneWeight[i] = bb.getInt();
			boneIndex[i] = bb.getInt();
		}								
		
		//Normals, Binormals, Colors, and Tex Coords
		for (int i = 0; i < numVerts; i++)
		{
			//Normal			
			vertBuffer.putShort(bb.getShort());
			vertBuffer.putShort(bb.getShort());
			vertBuffer.putShort(bb.getShort());
			vertBuffer.putShort(bb.getShort());
			
			//Binormal
			vertBuffer.put(bb.get());
			vertBuffer.put(bb.get());
			vertBuffer.put(bb.get());
			vertBuffer.put(bb.get());
			
			//Color
			vertBuffer.put(bb.get());
			vertBuffer.put(bb.get());
			vertBuffer.put(bb.get());
			vertBuffer.put(bb.get());
			
			//Tex Coords
			vertBuffer.putShort(bb.getShort());
			vertBuffer.putShort(bb.getShort());
			vertBuffer.putShort(bb.getShort());
			vertBuffer.putShort(bb.getShort());
		}		
		
		//Index Table
		bb.position(lodIndexOffset);
		for (int i = 0; i < numIndex; i++)
		{
			short index =bb.getShort();
			
			indexBuffer.putShort(index);
			
			if (index > numVerts)
				System.out.println(String.format("FUCK, INVALID VERT: %x @ %x", index, i));
		}
	}
}
