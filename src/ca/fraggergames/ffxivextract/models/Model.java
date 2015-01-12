package ca.fraggergames.ffxivextract.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ca.fraggergames.ffxivextract.Constants;

public class Model {
	
	private LoDSubModel lodModels[] = new LoDSubModel[3];
		
	public Model(byte[] data)
	{		
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		int numTotalMeshes = bb.getInt();				
		
		//DirectX Structs
		bb.position(0x44 + (0x88 * numTotalMeshes));
		
		//Strings
		int numStrings = bb.getInt();
		int stringBlockSize = bb.getInt();
		bb.position(bb.position() + stringBlockSize);

		//Stuff
		bb.position(bb.position()+0x18);
		short numStructs = bb.getShort();
		bb.position(bb.position()+0x1e);
				
		bb.position(bb.position()+(0x20 * numStructs));

		if (Constants.DEBUG)
			System.out.println("LoD Header Info");
		//LOD Headers		
		for (int i = 0; i < lodModels.length; i++)
		{
			if (Constants.DEBUG)
				System.out.println("-----LoD Level " + i + "-----");
			lodModels[i] = LoDSubModel.loadInfo(bb);
		}
        //Load Mesh Info
		for (int i = 0; i < lodModels.length; i++)
		{
			if (Constants.DEBUG)
				System.out.println(String.format("-----LoD %d-----", i));
			
			Mesh meshList[] = new Mesh[lodModels[i].numMeshes];
			for (int j = 0; j < lodModels[i].numMeshes; j++)
			{								
				int vertCount = bb.getInt();
	        	int indexCount = bb.getInt();	    	        
	        	
	        	bb.getInt();bb.getInt();
	        	
	        	int indexBufferOffset = bb.getInt();
	        	int vertexBufferOffset = bb.getInt();
	        	
	        	bb.getInt();bb.getInt();        	
	        		        	        	        	
	        	int sizeInfo = bb.getInt();
	        	
	        	meshList[j] = new Mesh(vertCount, indexCount, vertexBufferOffset, indexBufferOffset, sizeInfo);
	        	
	        	lodModels[i].setMeshList(meshList);
	        	
	        	if (Constants.DEBUG)
	        	{
		        	System.out.println("Mesh " + j + ", numVerts: " + vertCount);
		        	System.out.println("Mesh " + j + ", numIndex: " + indexCount);	   
		        	
		        	System.out.println("Mesh " + j + ", vertOffset: " + vertexBufferOffset);
		        	System.out.println("Mesh " + j + ", indexOffset: " + indexBufferOffset);
	        	}
	        	
			}
		}     
        
        //Load LoD 0 Meshes
        for (int i = 0; i < lodModels.length; i++){
        	lodModels[i].loadMeshes(bb);
        }
        	       
	}
	
	public Mesh[] getMeshes(int lodLevel)
	{
		return lodModels[lodLevel].meshList;
	}

	public int getNumLOD0Meshes(int lodLevel) {
		return lodModels[lodLevel].meshList.length;
	}
	
	public int getLodLevels()
	{
		return lodModels.length;
	}

	public int getNumMesh(int lodLevel) {
		return lodModels[lodLevel].numMeshes;
	}
	
}
