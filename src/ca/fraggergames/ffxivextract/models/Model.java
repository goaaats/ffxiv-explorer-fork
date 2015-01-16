package ca.fraggergames.ffxivextract.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ca.fraggergames.ffxivextract.Constants;

public class Model {
	
	private String stringArray[];
	private short numAtrStrings, numAnimStrings, numMaterialStrings;	
	
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
		
		stringArray = new String[numStrings];
		byte stringBuffer[] = new byte[stringBlockSize];
		bb.get(stringBuffer);		
		
		int stringCounter = 0;
		int start=0, end=0;
		for (int i = 0; i < stringBuffer.length; i++)
		{			
			if (stringBuffer[i] == 0)
			{
				if (stringCounter >= numStrings)
					break;
				stringArray[stringCounter] = new String(stringBuffer, start, end-start);
				start = end+1;
				stringCounter++;				
			}
			end++;
		}

		if (Constants.DEBUG)
		{
			System.out.println("-----Strings-----");
			for(String s : stringArray)
				System.out.println(s);
		}
		
		//Counts
		bb.getInt();
		bb.getShort();		
		bb.getShort();		
		numAtrStrings = bb.getShort();		
		bb.getShort();				
		numMaterialStrings = bb.getShort();
		numAnimStrings = bb.getShort();						
		bb.getShort();		

		if (Constants.DEBUG){
			System.out.println("Atr Strings: " + numAtrStrings);
			System.out.println("Material Strings: " + numMaterialStrings);
			System.out.println("Anim Things: " + numAnimStrings);
		}
		
		loadMaterials();
		
		//Skip Stuff
		bb.position(bb.position()+0x8);
		short numStructs = bb.getShort();
		bb.position(bb.position()+0x1e);
				
		bb.position(bb.position()+(0x20 * numStructs));		
		
		//LOD Headers		
		if (Constants.DEBUG)
			System.out.println("-----LoD Header Info-----");
		for (int i = 0; i < lodModels.length; i++)
		{
			if (Constants.DEBUG)
				System.out.println(String.format("LoD Level %d:", i));
			lodModels[i] = LoDSubModel.loadInfo(bb);
		}
        //Load Mesh Info
		if (Constants.DEBUG)
			System.out.println("-----LoD Mesh Info-----");
		for (int i = 0; i < lodModels.length; i++)
		{
			if (Constants.DEBUG)
				System.out.println(String.format("LoD %d:", i));
			
			Mesh meshList[] = new Mesh[lodModels[i].numMeshes];
			for (int j = 0; j < lodModels[i].numMeshes; j++)
			{		
				if (Constants.DEBUG)
					System.out.println(String.format("Mesh %d:", j));
				
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
		        	System.out.println("numVerts: " + vertCount);
		        	System.out.println("numIndex: " + indexCount);	   
		        	
		        	System.out.println("vertOffset: " + vertexBufferOffset);
		        	System.out.println("indexOffset: " + indexBufferOffset);
	        	}
	        	
			}
		}     
        
        //Load LoD 0 Meshes
        for (int i = 0; i < lodModels.length; i++){
        	lodModels[i].loadMeshes(bb);
        }
        	       
	}
	
	private void loadMaterials()
	{
		
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
