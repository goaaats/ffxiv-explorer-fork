package ca.fraggergames.ffxivextract.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.media.opengl.GL3;

import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

public class Material {
	
	String materialPath;
	
	//Data
	short numPaths, numMaps, numColorSets, numUnknown;	
	String stringArray[];
	
	Texture_File diffuse, normal, specular;
	Texture_File colorSet;
	private boolean shaderReady = false;
	
	Shader shader;
	
	//Rendering
	int textureIds[] = new int[4];
	
	//Constructor grabs info about material
	public Material(byte[] data) {
		this(null, null, data);
	}	
	
	//Constructor grabs info and texture files
	public Material(String folderPath, SqPack_IndexFile currentIndex, byte[] data) {
	
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.getInt();
		int fileSize = bb.getInt();
		short stringSectionSize = bb.getShort();
		bb.getShort();
		numPaths = bb.get();
		numMaps = bb.get();
		numColorSets = bb.get();
		numUnknown = bb.get();		
		
		//Load Strings
		stringArray = new String[numPaths + numMaps + numColorSets + 1];		
		byte stringBuffer[] = new byte[stringSectionSize];
		bb.position(bb.position() + (4 * (numPaths + numMaps + numColorSets)));
		bb.get(stringBuffer);		
		
		int stringCounter = 0;
		int start=0, end=0;
		for (int i = 0; i < stringBuffer.length; i++)
		{			
			if (stringBuffer[i] == 0)
			{
				if (stringCounter >= stringArray.length)
					break;
				stringArray[stringCounter] = new String(stringBuffer, start, end-start);
				start = end+1;
				stringCounter++;				
			}
			end++;
		}
		
		//Load Textures
		if (folderPath != null && currentIndex != null)
		{
			for (int i = 0; i < numPaths; i++)
			{				
				String s = stringArray[i];
				String folderName = s.substring(0, s.lastIndexOf("/"));
				int hash1 = HashDatabase.computeCRC(folderName.getBytes(), 0, folderName.getBytes().length);			
				
				for (SqPack_Folder f : currentIndex.getPackFolders())
				{			
					if (f.getId() == hash1)
					{
						
								String fileString = s.substring(s.lastIndexOf("/")+1, s.length());
								int hash2 = HashDatabase.computeCRC(fileString.getBytes(), 0, fileString.getBytes().length);
								for (SqPack_File file : f.getFiles())
								{
									if (file.id == hash2)
									{
										HashDatabase.addPathToDB(s);																			
										
										try {
											byte extracted[] = currentIndex.extractFile(file.dataoffset, null);
											if (extracted == null)
												continue;
											
											if (fileString.endsWith("_d.tex") || fileString.contains("catchlight"))
												diffuse = new Texture_File(extracted);
											else if (fileString.endsWith("_n.tex"))
												normal = new Texture_File(extracted);
											else if (fileString.endsWith("_s.tex"))
												specular = new Texture_File(extracted);
											else if (fileString.endsWith("_m.tex"))
												diffuse = new Texture_File(extracted);
											else
												colorSet = new Texture_File(extracted);
											
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
	}
	
	public void loadShader(GL3 gl)
	{
		//Load Shader
		try {
			if (stringArray[stringArray.length-1].equals("character.shpk"))		
				shader = new CharacterShader(gl);
			else if (stringArray[stringArray.length-1].equals("hair.shpk"))		
				shader = new HairShader(gl);
			else if (stringArray[stringArray.length-1].equals("iris.shpk"))		
				shader = new IrisShader(gl);
			else if (stringArray[stringArray.length-1].equals("skin.shpk"))		
				shader = new SkinShader(gl);
			else 		
				shader = new DefaultShader(gl);
		} catch (IOException e) {				
			e.printStackTrace();
		}
		
		shaderReady = true;
	}
	
	public Shader getShader()
	{
		return shader;
	}
	
	public boolean isShaderReady()
	{
		return shaderReady;
	}
	
	public Texture_File getDiffuseMapTexture(){
		return diffuse;
	}
	
	public Texture_File getNormalMapTexture(){
		return normal;
	}

	public Texture_File getSpecularMapTexture(){
		return specular;
	}
	
	public Texture_File getColorSetTexture(){
		return colorSet;
	}

	public int[] getGLTextureIds() {
		return textureIds;
	}
	
}
