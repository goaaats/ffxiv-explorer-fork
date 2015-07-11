package ca.fraggergames.ffxivextract.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.media.opengl.GL3;

import ca.fraggergames.ffxivextract.helpers.Utils;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;
import ca.fraggergames.ffxivextract.shaders.BGShader;
import ca.fraggergames.ffxivextract.shaders.CharacterShader;
import ca.fraggergames.ffxivextract.shaders.DefaultShader;
import ca.fraggergames.ffxivextract.shaders.HairShader;
import ca.fraggergames.ffxivextract.shaders.IrisShader;
import ca.fraggergames.ffxivextract.shaders.Shader;
import ca.fraggergames.ffxivextract.shaders.SkinShader;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

public class Material {
	
	String materialPath;	
	
	//Data
	int fileSize;
	int colorTableSize;
	int stringSectionSize;
	int shaderStringOffset;
	short numPaths, numMaps, numColorSets, numUnknown;	
	
	String stringArray[];
	
	Texture_File diffuse, mask, normal, specular;
	Texture_File colorSet;
	
	byte[] colorSetData;
	
	private boolean shaderReady = false;	
	
	Shader shader;
	
	//Rendering
	int textureIds[] = new int[5];
	
	//Constructor grabs info about material
	public Material(byte[] data) {
		this(null, null, data);
	}	
	
	//Constructor grabs info and texture files
	public Material(String folderPath, SqPack_IndexFile currentIndex, byte[] data) {
	
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.getInt();
		fileSize = bb.getShort();
		colorTableSize = bb.getShort();
		stringSectionSize = bb.getShort();
		shaderStringOffset = bb.getShort();
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
								
				if (!s.contains("/"))
				{
					System.out.println("Can't load: " + s);
					continue;
				}
				
				String folderName = s.substring(0, s.lastIndexOf("/"));										
				String fileString = s.substring(s.lastIndexOf("/")+1, s.length());
			
				System.out.println("Adding Entry: " + s);
				HashDatabase.addPathToDB(s, currentIndex.getIndexName());																			
				
				try {
					byte extracted[] = currentIndex.extractFile(folderName, fileString);
					if (extracted == null)
						continue;
					
					if ((fileString.endsWith("_d.tex") || fileString.contains("catchlight")) && diffuse == null)
						diffuse = new Texture_File(extracted);
					else if (fileString.endsWith("_n.tex") && normal == null)
						normal = new Texture_File(extracted);
					else if (fileString.endsWith("_s.tex") && specular == null)
						specular = new Texture_File(extracted);
					else if (fileString.endsWith("_m.tex"))
						mask = new Texture_File(extracted);
					else
						colorSet = new Texture_File(extracted);
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();				
				}
				
			}
		}	
		
		//This is for the new material file setup. ALso bgcolorchange doesn't have a table color but can't find where it says that.
		if (colorSet == null && colorTableSize >= 512)
		{			
			bb.position(16 + (4 * (numPaths + numMaps + numColorSets)) + stringBuffer.length);
			if (bb.getInt() == 0x0)
				return;
			colorSetData = new byte[512];
			bb.get(colorSetData);
		}
	}
	
	public void loadShader(GL3 gl)
	{
		//Weird case
		if (stringArray[stringArray.length-1] == null)
		{
			int x = stringArray.length-1;
			do
			{
				x--;
				stringArray[stringArray.length-1] = stringArray[x];				
			}while (stringArray[x] == null && x >= 0);
		}
		
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
			else if (stringArray[stringArray.length-1].equals("bg.shpk"))		
				shader = new BGShader(gl);
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
	
	public Texture_File getMaskTexture() {
		return mask;
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

	public byte[] getColorSetData(){
		return colorSetData;
	}
	
	public int[] getGLTextureIds() {
		return textureIds;
	}
	
	
}
