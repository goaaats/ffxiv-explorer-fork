package ca.fraggergames.ffxivextract.models;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.media.opengl.GL3;
import javax.media.opengl.GL3bc;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.helpers.ImageDecoding.ImageDecodingException;
import ca.fraggergames.ffxivextract.helpers.Utils;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

import com.jogamp.common.nio.Buffers;

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
		bb.getShort();
		bb.getShort();

		materials = new Material[numMaterialStrings];
		
		if (Constants.DEBUG){
			System.out.println("Atr Strings: " + numAtrStrings);
			System.out.println("Material Strings: " + numMaterialStrings);
			System.out.println("Anim Things: " + numBoneStrings);
		}
		
		numVariants = loadNumberOfVariants();		
		loadMaterials(1);
		
		//Skip Stuff
		bb.position(bb.position()+0x4);
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
		
		if ((modelPath.contains("face") || modelPath.contains("hsair"))){
			if (variant == 1)
				materialFolderPath = String.format("%smaterial", modelPath.substring(0, modelPath.indexOf("model")));
			else
				materialFolderPath = String.format("%smaterial/v%04d", modelPath.substring(0, modelPath.indexOf("model")), variant-1);
		}		
		else
			materialFolderPath = String.format("%smaterial/v%04d", modelPath.substring(0, modelPath.indexOf("model")), variant);
		
		int hash1 = HashDatabase.computeCRC(materialFolderPath.getBytes(), 0, materialFolderPath.getBytes().length);			
		
		//HACK HERE
		int bodyMaterialSpot = -1;
		
		if (materialFolderPath.contains("body") && materialFolderPath.contains("human"))
		{
			bodyMaterialSpot = 0;
		}
		else
		{
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
								
							//HACK HERE
							if (fileString.matches(Utils.getRegexpFromFormatString("mt_c%04db%04d_%s.mtrl")))
							{
								bodyMaterialSpot = i;
								continue;
							}
							
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
					break;
				}
			}	
		}
		
		//If there was a body material, grab it HACK HERE
		if (bodyMaterialSpot != -1)
		{	
			String s = stringArray[numAtrStrings+numBoneStrings+bodyMaterialSpot].substring(1);
			String s1 = s.replace("mt_c", "").substring(0, 9);					
			int chara = Integer.parseInt(s1.substring(0, 4));
			int body = Integer.parseInt(s1.substring(5, 9));
			materialFolderPath = String.format("chara/human/c%04d/obj/body/b%04d/material",chara,body);
			hash1 = HashDatabase.computeCRC(materialFolderPath.getBytes(), 0, materialFolderPath.getBytes().length);
			
			for (SqPack_Folder f : currentIndex.getPackFolders())
			{			
				if (f.getId() == hash1)
				{
						int hash2 = HashDatabase.computeCRC(s.getBytes(), 0, s.getBytes().length);
						for (SqPack_File file : f.getFiles())
						{
							if (file.id == hash2)
							{
								try {
									HashDatabase.addPathToDB(materialFolderPath+"/"+s);
									materials[bodyMaterialSpot] = new Material(materialFolderPath, currentIndex, currentIndex.extractFile(file.dataoffset, null));
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
		return index < materials.length ? materials[index] : null;
	}

	public int getNumMesh(int lodLevel) {
		return lodModels[lodLevel].numMeshes;
	}

	public int getNumMaterials() {
		return modelPath == null || modelPath.contains("null") ? 0 : materials.length;
	}
	
	public int getNumVariants()
	{
		return numVariants;
	}

	public void render(DefaultShader defaultShader, float[] viewMatrix, float[] modelMatrix,
			float[] projMatrix, GL3bc gl, int currentLoD) {
		for (int i = 0; i < getNumMesh(currentLoD); i++){
	    	
	    	Mesh mesh = getMeshes(currentLoD)[i];
	    	Material material = getMaterial(mesh.materialNumber);		    	
	    	Shader shader = material == null || !material.isShaderReady() ? defaultShader : material.getShader();
	    	
	    	gl.glUseProgram(shader.getShaderProgramID());
	    	
	    	mesh.vertBuffer.position(0);
	    	mesh.indexBuffer.position(0);
	    	
	    	//Position
	    	if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)
	    		gl.glVertexAttribPointer(shader.getAttribPosition(), 4, GL3.GL_HALF_FLOAT, false, 0, mesh.vertBuffer);
		    else if (mesh.vertexSize == 0x14)
		    	gl.glVertexAttribPointer(shader.getAttribPosition(), 3, GL3.GL_FLOAT, false, 0, mesh.vertBuffer);
	    	
	    	//Normal
	    	ByteBuffer normalData = mesh.vertBuffer.duplicate();			    
		    if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)
		    	normalData.position(mesh.numVerts*8);
		    else
		    	normalData.position(mesh.numVerts*12);		    	
	    	gl.glVertexAttribPointer(shader.getAttribNormal(), 4, GL3.GL_HALF_FLOAT, false, 24, normalData);
	    	
	    	//Tex Coord
	    	ByteBuffer texData = mesh.vertBuffer.duplicate();			    
		    if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)
		    	texData.position((mesh.numVerts*8) + 16);
		    else
		    	texData.position((mesh.numVerts*12)+ 16);		
	    	gl.glVertexAttribPointer(shader.getAttribTexCoord(), 4, GL3.GL_HALF_FLOAT, false, 24, texData);
	    	
	    	//BiNormal
	    	ByteBuffer binormalData = mesh.vertBuffer.duplicate();			    
		    if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)
		    	binormalData.position(mesh.numVerts*8+8);
		    else
		    	binormalData.position(mesh.numVerts*12+8);		    	
	    	gl.glVertexAttribPointer(shader.getAttribBiTangent(), 4, GL3.GL_UNSIGNED_BYTE, false, 24, binormalData);
	    	
	    	//Color
	    	ByteBuffer colorData = mesh.vertBuffer.duplicate();			    
		    if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)
		    	colorData.position((mesh.numVerts*8) + 12);
		    else
		    	colorData.position((mesh.numVerts*12)+ 12);	
	    	gl.glVertexAttribPointer(shader.getAttribColor(), 4, GL3.GL_UNSIGNED_BYTE, false, 24, colorData);
	    	
	    	shader.setTextures(gl, material);
	    	shader.setMatrix(gl, modelMatrix, viewMatrix, projMatrix);
		    	
	    	if (shader instanceof HairShader)
	    		((HairShader)shader).setHairColor(gl, Constants.defaultHairColor, Constants.defaultHighlightColor);
	    	else if (shader instanceof IrisShader)
	    		((IrisShader)shader).setEyeColor(gl, Constants.defaultEyeColor);		    	
	    	
	    	//Draw
	    	shader.enableAttribs(gl);
		    gl.glDrawElements(GL3.GL_TRIANGLES, mesh.numIndex, GL3.GL_UNSIGNED_SHORT, mesh.indexBuffer);			    
		    shader.disableAttribs(gl);			  
		    
		}
	}

	public void loadToVRAM(GL3bc gl) {
		for (int i = 0; i < getNumMaterials(); i++){
			
			if (getMaterial(i) == null)
				break;
			
			gl.glGenTextures(5, getMaterial(i).getGLTextureIds(),0);												
			Material m = getMaterial(i);
			
			for (int j = 0; j < 5; j++){
				
				Texture_File tex = null;
				
				switch(j)
				{
				case 0: 
					tex = m.getDiffuseMapTexture();
					break;
				case 1: 
					tex = m.getNormalMapTexture();
					break;
				case 2: 
					tex = m.getSpecularMapTexture();
					break;
				case 3: 
					tex = m.getColorSetTexture();
					break;
				case 4: 
					tex = m.getMaskTexture();
					break;
				}
				
				if (tex == null)
					continue;
				
				BufferedImage img = null;
				try {
					img = tex.decode(0, null);
				} catch (ImageDecodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}								
				
				int[] pixels = new int[img.getWidth() * img.getHeight()];
				img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());				
				
				ByteBuffer buffer = Buffers.newDirectByteBuffer(img.getWidth() * img.getHeight() * 4);
				
				//Fucking Java Trash
				for(int y = 0; y < img.getHeight(); y++){
		            for(int x = 0; x < img.getWidth(); x++){
		                int pixel = pixels[y * img.getWidth() + x];
		                buffer.put((byte) ((pixel >> 16) & 0xFF));     
		                buffer.put((byte) ((pixel >> 8) & 0xFF));      
		                buffer.put((byte) (pixel & 0xFF));               
		                buffer.put((byte) ((pixel >> 24) & 0xFF));
		            }
		        }				
		        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS
				buffer.position(0);
				
		        //Load into VRAM
		        gl.glBindTexture(GL3.GL_TEXTURE_2D, m.getGLTextureIds()[j]);
				gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_MIRRORED_REPEAT);
				gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_MIRRORED_REPEAT);
				gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, j == 3 ? GL3.GL_NEAREST : GL3.GL_LINEAR);
				gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, j == 3 ? GL3.GL_NEAREST : GL3.GL_LINEAR);
				
				gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA, img.getWidth(), img.getHeight(), 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, buffer);						
				gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
				
				m.loadShader(gl);
			}					
		}
	}
	
}
