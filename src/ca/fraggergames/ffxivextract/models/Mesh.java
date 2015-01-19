package ca.fraggergames.ffxivextract.models;

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
	
	public int[] boneWeight;
	public int[] boneIndex;
	
	public Mesh(int vertCount, int indexCount, int meshNum, int vertexBufferOffset,
			int indexBufferOffset, int sizeinfo) {
		this.numVerts = vertCount;
		this.numIndex = indexCount;
		this.vertOffset = vertexBufferOffset;
		this.indexOffset = indexBufferOffset;		
		
		this.vertexSize = (sizeinfo >> 8*0) & 0xFF;
		this.auxVertexSize = (sizeinfo >> 8*1) & 0xFF;
		this.unknownSize = (sizeinfo >> 8*2) & 0xFF;
		this.indexSize = (sizeinfo >> 8*3) & 0xFF;		
		
		this.vertBuffer = Buffers.newDirectByteBuffer(numVerts * (vertexSize+auxVertexSize));
		this.indexBuffer = Buffers.newDirectByteBuffer(numIndex * indexSize);
	}

	public void loadMeshes(ByteBuffer bb, int lodVertexOffset, int lodIndexOffset){
		
    	boneWeight = new int[numVerts];
    	boneIndex = new int[numVerts];
    	    	
    	bb.position(lodVertexOffset +vertOffset);
    	
    	byte buffer[] = new byte[8];
    	
		for (int i = 0; i < numVerts; i++)
		{			
			
			if (vertexSize == 0x10)
			{
				bb.get(buffer);
				vertBuffer.put(buffer);
			}			
			else if (vertexSize == 0x14)
			{
				bb.get(buffer);
				vertBuffer.put(buffer);
				int x = bb.getInt();
				vertBuffer.putInt(x);
			}
			else if (vertexSize == 0x8)
			{
				bb.get(buffer);
				vertBuffer.put(buffer);
			}
			
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
		bb.position(lodIndexOffset + (indexOffset * 2));
		for (int i = 0; i < numIndex; i++)
			indexBuffer.putShort(bb.getShort());
	}
}
