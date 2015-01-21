package ca.fraggergames.ffxivextract.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

public class Model {
	
	//Used to find other files
	String modelPath;
	SqPack_IndexFile currentIndex;
	
	private String stringArray[];
	private short numAtrStrings, numBoneStrings, numMaterialStrings, numShpStrings;	
	
	private int numVariants = -1;
	
	private Material materials[];
	private LoDSubModel lodModels[] = new LoDSubModel[3];
		
	public Model(byte[] data)
	{
		this(null, null, data);
	}
	
	public Model(String modelPath, SqPack_IndexFile index, byte[] data)
	{		
		this.modelPath = modelPath;
		this.currentIndex = index; 
		
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		int numTotalMeshes = bb.getShort();				
		
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
		numAtrStrings = bb.getShort();		
		bb.getShort();				
		numMaterialStrings = bb.getShort( );
		numBoneStrings = bb.getShort();						
		numShpStrings = bb.getShort();		

		materials = new Material[numMaterialStrings];
		
		if (Constants.DEBUG){
			System.out.println("Atr Strings: " + numAtrStrings);
			System.out.println("Material Strings: " + numMaterialStrings);
			System.out.println("Anim Things: " + numBoneStrings);
		}
		
		numVariants = loadNumberOfVariants();		
		loadMaterials(1);
		
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
	        	
	        	short meshNum = bb.getShort();
	        	bb.getShort();
	        	bb.getInt();
	        	
	        	int indexBufferOffset = bb.getInt();
	        	int vertexBufferOffset = bb.getInt();
	        	
	        	bb.getInt();bb.getInt();        	
	        		        	        	        	
	        	int sizeInfo = bb.getInt();
	        	
	        	meshList[j] = new Mesh(vertCount, indexCount, meshNum, vertexBufferOffset, indexBufferOffset, sizeInfo);
	        	
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
	
	private short loadNumberOfVariants()
	{
		if (modelPath == null || modelPath.contains("null"))
			return -1;
		
		String incFolderPath = String.format("%s", modelPath.substring(0, modelPath.indexOf("model")-1));			
		
		int hash1 = HashDatabase.computeCRC(incFolderPath.getBytes(), 0, incFolderPath.getBytes().length);			
		
		for (SqPack_Folder f : currentIndex.getPackFolders())
		{			
			if (f.getId() == hash1)
			{
					for (int i = 0; i < numMaterialStrings; i++)
					{
						String fileString = incFolderPath.substring(incFolderPath.lastIndexOf("/")+1) + ".imc";										
							
						int hash2 = HashDatabase.computeCRC(fileString.getBytes(), 0, fileString.getBytes().length);
						for (SqPack_File file : f.getFiles())
						{
							if (file.id == hash2)
							{
								HashDatabase.addPathToDB(incFolderPath+"/"+fileString);
								
								try {
									byte[] data = currentIndex.extractFile(file.getOffset(), null);
									ByteBuffer bb = ByteBuffer.wrap(data);
									bb.order(ByteOrder.LITTLE_ENDIAN);
									return bb.getShort();
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}															
							}
						}
					}
			}
		}
		return -1;
	}
	
	public void loadMaterials(int variant)
	{		
		if (modelPath == null || modelPath.contains("null"))
			return;
		
		String split[] = modelPath.split("/");		
		
		String materialFolderPath = null;
		
		if ((modelPath.contains("face") || modelPath.contains("hair"))){
			if (variant == 1)
				materialFolderPath = String.format("%smaterial", modelPath.substring(0, modelPath.indexOf("model")));
			else
				materialFolderPath = String.format("%smaterial/v%04d", modelPath.substring(0, modelPath.indexOf("model")), variant-1);
		}
		else
			materialFolderPath = String.format("%smaterial/v%04d", modelPath.substring(0, modelPath.indexOf("model")), variant);
		
		int hash1 = HashDatabase.computeCRC(materialFolderPath.getBytes(), 0, materialFolderPath.getBytes().length);			
		
		for (SqPack_Folder f : currentIndex.getPackFolders())
		{			
			if (f.getId() == hash1)
			{
					for (int i = 0; i < numMaterialStrings; i++)
					{
						String fileString = null;
						
						if (stringArray[numAtrStrings+numBoneStrings+i].startsWith("/"))
							fileString = stringArray[numAtrStrings+numBoneStrings+i].substring(1);
						else
							fileString = stringArray[numAtrStrings+numBoneStrings+i].substring(stringArray[numAtrStrings+numBoneStrings+i].lastIndexOf("/")+1);
							
						int hash2 = HashDatabase.computeCRC(fileString.getBytes(), 0, fileString.getBytes().length);
						for (SqPack_File file : f.getFiles())
						{
							if (file.id == hash2)
							{
								try {
									HashDatabase.addPathToDB(materialFolderPath+"/"+fileString);
									materials[i] = new Material(materialFolderPath, currentIndex, currentIndex.extractFile(file.dataoffset, null));
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
								break;
							}
						}
					}
			}
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
	
	public Material getMaterial(int index)
	{
		return materials[index];
	}

	public int getNumMesh(int lodLevel) {
		return lodModels[lodLevel].numMeshes;
	}

	public int getNumMaterials() {
		return modelPath.contains("null") ? 0 : materials.length;
	}
	
	public int getNumVariants()
	{
		return numVariants;
	}
	
}
