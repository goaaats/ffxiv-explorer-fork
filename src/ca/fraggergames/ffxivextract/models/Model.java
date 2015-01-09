package ca.fraggergames.ffxivextract.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Model {
	
	private int numLoD0Meshes;
	private int numLoD1Meshes;
	private int numLoD2Meshes;
	
	private Mesh meshList[];
		
	public Model(byte[] data)
	{		
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		int numMeshes = bb.getInt();		
		
		meshList = new Mesh[numMeshes];
		
		//DirectX Structs
		bb.position(0x44 + (0x88 * numMeshes));
		
		//Strings
		int numStrings = bb.getInt();
		int stringBlockSize = bb.getInt();
		bb.position(bb.position() + stringBlockSize);

		bb.position(bb.position()+0x18);
		short numStructs = bb.getShort();
		bb.position(bb.position()+0x20);
		
		bb.position(bb.position()+(0x18 * numStructs));

		numLoD0Meshes = bb.getShort();
        		
        bb.position(bb.position()+0x28);
        
        int lod0VertBuffSize = bb.getInt();
        int lod0IndexBuffSize = bb.getInt();
        int lod0VertOffset = bb.getInt();
        int lod0IndexOffset = bb.getInt();             
        
        bb.position(bb.position()+(0x3c*2));
        
        //Load LoD 0 Mesh Info
        for (int i = 0; i < numLoD0Meshes; i++)
        {
        	int vertCount = bb.getInt();
        	int indexCount = bb.getInt();
        	
        	System.out.println("Mesh " + i + ", numVerts: " + vertCount);
        	System.out.println("Mesh " + i + ", numIndex: " + indexCount);
        	
        	bb.getInt();bb.getInt();
        	
        	int indexBufferOffset = bb.getInt();
        	int vertexBufferOffset = bb.getInt();
        	
        	bb.getInt();bb.getInt();        	
        	
        	int vertexSize = bb.get();        	
        	
        	bb.position(bb.position()+3);
        	
        	meshList[i] = new Mesh(vertCount, indexCount, vertexBufferOffset, indexBufferOffset, vertexSize);
        }
        
        //Load LoD 0 Meshes
        for (int i = 0; i < numLoD0Meshes; i++)                	
        	meshList[i].loadMeshes(bb, lod0VertOffset, lod0IndexOffset);        
	}
	
	public Mesh[] getMeshes()
	{
		return meshList;
	}

	public int getNumLOD0Meshes() {
		return numLoD0Meshes;
	}
	
}
