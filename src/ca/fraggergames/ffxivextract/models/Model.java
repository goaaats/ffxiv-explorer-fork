package ca.fraggergames.ffxivextract.models;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.media.opengl.GL3;
import javax.media.opengl.GL3bc;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.helpers.ImageDecoding.ImageDecodingException;
import ca.fraggergames.ffxivextract.helpers.GLHelper;
import ca.fraggergames.ffxivextract.helpers.HavokNative;
import ca.fraggergames.ffxivextract.helpers.Utils;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;
import ca.fraggergames.ffxivextract.models.directx.DX9VertexElement;
import ca.fraggergames.ffxivextract.shaders.DefaultShader;
import ca.fraggergames.ffxivextract.shaders.HairShader;
import ca.fraggergames.ffxivextract.shaders.IrisShader;
import ca.fraggergames.ffxivextract.shaders.Shader;
import ca.fraggergames.ffxivextract.shaders.SimpleShader;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

import com.jogamp.common.nio.Buffers;

public class Model {
			
	//Used to find other files
	String modelPath;
	SqPack_IndexFile currentIndex;
		
	//Model Info
	private IMC_File imcFile;
	private DX9VertexElement vertexElements[][];
	private String stringArray[];
	private short numAtrStrings, numBoneStrings, numMaterialStrings, numShpStrings;		
	
	private Material materials[];
	private LoDSubModel lodModels[] = new LoDSubModel[3];
		
	private ByteBuffer boneMatrixBuffer;
	private int numBones = -1;
	
	private String[] boneStrings;
	
	//Skeleton/Animation
	private SKLB_File skelFile;
	private PAP_File animFile;
	private float animSpeed = 1f/60f;
	
	private SimpleShader simpleShader;
	
	private boolean isVRAMLoaded = false;
	
	public Model(byte[] data)
	{
		this(null, null, data);
	}
	
	public Model(String modelPath, SqPack_IndexFile index, byte[] data) throws BufferOverflowException, BufferUnderflowException
	{						
		this.modelPath = modelPath;
		this.currentIndex = index; 
		
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		int numTotalMeshes = bb.getShort();				
		
		//Count DirectX Vertex Elements
		vertexElements = new DX9VertexElement[numTotalMeshes][];		
		for (int i = 0; i < numTotalMeshes; i++){
			bb.position(0x44 + (0x88 * i));
			int count = 0;
			while (true)
			{				
				byte stream = bb.get();				
				if (stream == -1)
					break;
				count++;
				bb.position(bb.position()+7);
			}
			vertexElements[i] = new DX9VertexElement[count];
		}
		//Load DirextX Vertex Elements
		for (int i = 0; i < numTotalMeshes; i++){
			bb.position(0x44 + (0x88 * i));
			for (int j = 0; j < vertexElements[i].length; j++)
			{
				vertexElements[i][j] = new DX9VertexElement(bb.get(), bb.get(), bb.get(), bb.get());
				bb.position(bb.position()+0x4);
			}
		}
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

		boneStrings = new String[numBoneStrings];
		System.arraycopy(stringArray, numAtrStrings, boneStrings, 0, numBoneStrings);
		
		materials = new Material[numMaterialStrings];
		
		if (Constants.DEBUG){
			System.out.println("Atr Strings: " + numAtrStrings);
			System.out.println("Material Strings: " + numMaterialStrings);
			System.out.println("Anim Things: " + numBoneStrings);
		}
		
		imcFile = loadImcFile();		
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
		
		int vertElementNumber = 0;
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
	        	
	        	meshList[j] = new Mesh(vertCount, indexCount, meshNum, vertexBufferOffset, indexBufferOffset, sizeInfo, vertElementNumber);
	        	
	        	vertElementNumber++;
	        	
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
        
        for (int i = 0; i < lodModels.length; i++) {
        	lodModels[i].loadMeshes(bb);
        }
        
        //Skeletons and Animations
        if (!Constants.HAVOK_ENABLED)
        { 
        	numBones = -1;
        	return;
        }
        
        if (modelPath != null)
        {
        	String modelPathSplit[] = modelPath.split("/");
        	String skeletonPath = null, animationPath = null;        	        	        	
        	
        	if (modelPathSplit[1].equals("monster"))
        	{
        		skeletonPath = String.format("chara/monster/%s/skeleton/base/b0001/skl_%sb0001.sklb", modelPathSplit[2], modelPathSplit[2]);
        		animationPath = String.format("chara/monster/%s/animation/a0001/bt_common/resident/monster.pap", modelPathSplit[2]);
        	} 
        	else if (modelPathSplit[1].equals("human"))
        	{
        		skeletonPath = String.format("chara/human/%s/skeleton/%s/%s/skl_%s%s.sklb", modelPathSplit[2], modelPathSplit[4], modelPathSplit[5], modelPathSplit[2], modelPathSplit[5]);
        		animationPath = String.format("chara/human/%s/animation/%s/resident/face.pap", modelPathSplit[2], modelPathSplit[5]);
        	}  
        	        		       
        	if (skeletonPath == null || animationPath == null)
        	{
        		skeletonPath = "!/!";
        		animationPath = "!/!";
        	}
        	
        	
	        skelFile = null;
			try {				
				byte sklbData[] = currentIndex.extractFile(skeletonPath);
				if (sklbData != null)
					skelFile = new SKLB_File(sklbData);
			} catch (FileNotFoundException e) {
				System.out.println("Skel Not Found");
			} catch (IOException e) {
				System.out.println("Skel Not Found");
			}
	        animFile = null;
			try {
				byte animData[] = currentIndex.extractFile(animationPath);
				if (animData != null)
					animFile = new PAP_File(animData);
			} catch (FileNotFoundException e) {
				System.out.println("Anim Not Found");
			} catch (IOException e) {
				System.out.println("Anim Not Found");
			}		
			
			if (animFile != null && skelFile != null){
		        ByteBuffer skelBuffer = ByteBuffer.allocateDirect(skelFile.getHavokData().length);
		        skelBuffer.order(ByteOrder.nativeOrder());
		        skelBuffer.put(skelFile.getHavokData());        
		        ByteBuffer animBuffer = ByteBuffer.allocateDirect(animFile.getHavokData().length);
		        skelBuffer.order(ByteOrder.nativeOrder());
		        animBuffer.put(animFile.getHavokData());
		        skelBuffer.position(0);
		        animBuffer.position(0);							        
		        
		        //Incase Havok doesn't work
		        try{		        	
		        	HavokNative.startHavok();
		        }
		        catch (UnsatisfiedLinkError e)
		        { 
		        	numBones = -1;
		        	return;
		        }
		        
			    if (HavokNative.loadSkeleton(skelBuffer, skelFile.getHavokData().length) && (HavokNative.loadAnimation(animBuffer, animFile.getHavokData().length)))
				{
					if (HavokNative.setAnimation(0) == -1)
					{
						HavokNative.setAnimation(0);
						System.out.println("Invalid Animation");
					}
					numBones = boneStrings.length;		
					System.out.println("There are:" + numBones + " bones.");
					boneMatrixBuffer = ByteBuffer.allocateDirect(4 * 16 * numBones);
					boneMatrixBuffer.order(ByteOrder.nativeOrder());						
				}
				else{
					numBones = -1;
					HavokNative.endHavok();
				}
			}
			else
			{
				numBones = -1;
				try{
					HavokNative.endHavok();
				}
				 catch (UnsatisfiedLinkError e)
			        { 
					 
			        }
			}
        }
	}
	
	private IMC_File loadImcFile()
	{
		if (modelPath == null || modelPath.contains("null") || !modelPath.contains("chara"))
			return null;
		
		String incFolderPath = String.format("%s", modelPath.substring(0, modelPath.indexOf("model")-1));								
		String fileString = incFolderPath.substring(incFolderPath.lastIndexOf("/")+1) + ".imc";										
		String imcPath = incFolderPath + "/" + fileString;
		try {
			byte[] data = currentIndex.extractFile(imcPath);
			
			if (data == null)
				return null;
			
			System.out.println("Adding Entry: " + imcPath);
			HashDatabase.addPathToDB(imcPath, currentIndex.getIndexName());			

			return new IMC_File(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;										
					
	}
	
	public void loadMaterials(int variantNumber)
	{		
		if (modelPath == null || modelPath.contains("null") || (!modelPath.contains("chara") && !modelPath.contains("bg") && !modelPath.contains("bgcommon")))
			return;
		
		String split[] = modelPath.split("/");		
		
		String materialFolderPath = null;
		
		if (!stringArray[numAtrStrings+numBoneStrings].startsWith("/") && !stringArray[numAtrStrings+numBoneStrings].contains("chara"))
			materialFolderPath = stringArray[numAtrStrings+numBoneStrings].substring(0, stringArray[numAtrStrings+numBoneStrings].lastIndexOf("/"));
		else if ((modelPath.contains("face") || modelPath.contains("hsair"))){
			if (variantNumber == -1 || imcFile == null)
				materialFolderPath = String.format("%smaterial", modelPath.substring(0, modelPath.indexOf("model")));
			else
				materialFolderPath = String.format("%smaterial/v%04d", modelPath.substring(0, modelPath.indexOf("model")), variantNumber);
		}		
		else// if (imcFile != null && imcFile.getVarianceInfo(variantNumber-1) != null)
			materialFolderPath = String.format("%smaterial/v%04d", modelPath.substring(0, modelPath.indexOf("model")), variantNumber);
		//else
		//	materialFolderPath = String.format("%smaterial/v%04d", modelPath.substring(0, modelPath.indexOf("model")), 1);
		
		//imcFile.getVarianceInfo(variantNumber-1).materialNumber
		
		//HACK HERE
		int bodyMaterialSpot = -1;
		
		if (materialFolderPath.contains("body") && materialFolderPath.contains("human"))
		{
			bodyMaterialSpot = 0;
		}
		else
		{
			for (int i = 0; i < numMaterialStrings; i++)
			{
				String fileString = null;
				
				try{
				if (stringArray[numAtrStrings+numBoneStrings+i].startsWith("/"))
					fileString = stringArray[numAtrStrings+numBoneStrings+i].substring(1);
				else
					fileString = stringArray[numAtrStrings+numBoneStrings+i].substring(stringArray[numAtrStrings+numBoneStrings+i].lastIndexOf("/")+1);
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					System.out.println("Num Materials was a LIE!");
					break;
				}
				
				//HACK HERE
				if (fileString.matches(Utils.getRegexpFromFormatString("mt_c%04db%04d_%s.mtrl")))
				{
					bodyMaterialSpot = i;
					continue;
				}
				
				//Fix the body model it's pointing to
				if (materialFolderPath.contains("/obj/body/b"))
				{
					String splitFileString[] = fileString.split("_");
					String weaponIdString = splitFileString[1].substring(0, 5);
					String weaponBodyString = splitFileString[1].substring(5);
					String splitString[] = materialFolderPath.split("/");
					materialFolderPath = splitString[0] + "/" + splitString[1] + "/" + weaponIdString + "/" + splitString[3] + "/" + splitString[4] + "/" + weaponBodyString + "/" + splitString[6] + "/" + splitString[7];
				}
				
				try {
					byte materialData[] = currentIndex.extractFile(materialFolderPath, fileString);
					
					if (materialData != null)
					{
						materials[i] = new Material(materialFolderPath, currentIndex, materialData);
						
						System.out.println("Adding Entry: " + materialFolderPath +"/"+ fileString);
						HashDatabase.addPathToDB(materialFolderPath +"/"+ fileString, "040000");
						
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
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
			
			System.out.println("Adding Entry: " + materialFolderPath);
			HashDatabase.addPathToDB(materialFolderPath, "040000");
			
			try {
				materials[bodyMaterialSpot] = new Material(materialFolderPath, currentIndex, currentIndex.extractFile(materialFolderPath, s));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
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
		if (imcFile == null)
			return -1;
		else
			return imcFile.getNumVariances();
	}

	public void render(DefaultShader defaultShader, float[] viewMatrix, float[] modelMatrix,
			float[] projMatrix, GL3bc gl, int currentLoD, boolean isGlow) {
		
		if (simpleShader == null)
			try {
				simpleShader = new SimpleShader(gl);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		

		if (numBones != -1){
	    	boneMatrixBuffer.position(0);
	    	HavokNative.getBonesWithNames(boneMatrixBuffer, boneStrings);
		}
		
		for (int i = 0; i < getNumMesh(currentLoD); i++){
	    	
			
	    	Mesh mesh = getMeshes(currentLoD)[i];
	    	Material material = getMaterial(mesh.materialNumber);		    	
	    	Shader shader = material == null || !material.isShaderReady() ? defaultShader : material.getShader();
	    	
	    	gl.glUseProgram(shader.getShaderProgramID());

	    	if (numBones != -1)
	    		boneMatrixBuffer.position(0);
	    	mesh.vertBuffer.position(0);
	    	mesh.indexBuffer.position(0);	    	
	    	
	    	for (int e = 0; e < vertexElements[mesh.getVertexElementIndex()].length; e++)
	    	{
	    		DX9VertexElement element = vertexElements[mesh.getVertexElementIndex()][e];
	    		
	    		int components = GLHelper.getComponents(element.datatype);
	    		int datatype = GLHelper.getDatatype(element.datatype);	    				
	    		int size = 0;
	    		
	    		//Set offset and size of buffer
	    		ByteBuffer origin = mesh.vertBuffer.duplicate();	
	    		if (element.stream == 0)	    	
	    		{
	    			origin.position(element.offset);
	    			size = mesh.vertexSize;
	    		}
	    		else	    
	    		{
	    			origin.position(mesh.numVerts*mesh.vertexSize + element.offset);
	    			size = mesh.auxVertexSize;
	    		}
	    		
	    		//Set Pointer
	    		switch (element.usage)
	    		{
	    		case 0://Position
	    			gl.glVertexAttribPointer(shader.getAttribPosition(), components, datatype, false, size, origin);
	    			break;
	    		case 1://Blend Weights	    
	    			gl.glVertexAttribIPointer(shader.getAttribBlendWeight(), components, datatype, size, origin);
	    			break;
	    		case 2://Blend Indices
	    			gl.glVertexAttribIPointer(shader.getAttribBlendIndex(), components, datatype, size, origin);
	    			break;
	    		case 3://Normal
	    			gl.glVertexAttribPointer(shader.getAttribNormal(), components, datatype, false, size, origin);
	    			break;
	    		case 4://Tex Coord
	    			gl.glVertexAttribPointer(shader.getAttribTexCoord(), components, datatype, false, size, origin);
	    			break;
	    		case 6://Tangent
	    			gl.glVertexAttribPointer(shader.getAttribBiTangent(), components, datatype, false, size, origin);
	    			break;
	    		case 7://Color
	    			gl.glVertexAttribPointer(shader.getAttribColor(), components, datatype, false, size, origin);
	    			break;
	    		}
	    		
	    	}
	    	
	    	shader.setTextures(gl, material);
	    	shader.setMatrix(gl, modelMatrix, viewMatrix, projMatrix);
	    	boolean f = shader.isGlowPass(gl, isGlow);
	    	if (isGlow && !f)
	    		return;
		    	
	    	if (shader instanceof HairShader)
	    		((HairShader)shader).setHairColor(gl, Constants.defaultHairColor, Constants.defaultHighlightColor);
	    	else if (shader instanceof IrisShader)
	    		((IrisShader)shader).setEyeColor(gl, Constants.defaultEyeColor);		    	
	    		    	
	    	//Upload Bone Matrix	    	
	    	if (numBones != -1)
	    	{	
	    		shader.setBoneMatrix(gl, numBones, boneMatrixBuffer);
	    		
	    	}
	    	
	    	//Draw	    	
	    	shader.enableAttribs(gl);
		    gl.glDrawElements(GL3.GL_TRIANGLES, mesh.numIndex, GL3.GL_UNSIGNED_SHORT, mesh.indexBuffer);		 		    
		    shader.disableAttribs(gl);			  		    		   

		    //Draw Skeleton
		    /*
		    gl.glDisable(GL3.GL_DEPTH_TEST);
		    if (simpleShader != null && numBones != -1){
			    gl.glPointSize(5f);
		    	simpleShader.enableAttribs(gl);
		    	boneMatrixBuffer.position(4*12);
			    gl.glUseProgram(simpleShader.getShaderProgramID());
			    simpleShader.setMatrix(gl, modelMatrix, viewMatrix, projMatrix);
			    gl.glVertexAttribPointer(simpleShader.getAttribPosition(), 3, GL3.GL_FLOAT, false, 16 * 4, boneMatrixBuffer);
			    gl.glDrawArrays(GL3.GL_POINTS, 0, HavokNative.getNumBones()-1);
			    simpleShader.disableAttribs(gl);	
		    }
		    gl.glEnable(GL3.GL_DEPTH_TEST);
		    */
		    
		   
		}
		
		 //Advance Animation		
	    if (numBones != -1)
	    {
	    	HavokNative.stepAnimation(animSpeed);
	    	HavokNative.debugRenderBones();
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
				boolean isBytes = false;
				byte byteData[] = null;
				
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
					if (tex == null)
					{
						isBytes = true;
						byteData = m.getColorSetData();
					}
					break;
				case 4: 
					tex = m.getMaskTexture();
					break;
				}
				
				if (tex == null && byteData == null)
					continue;						
				
		        //Load into VRAM
		        gl.glBindTexture(GL3.GL_TEXTURE_2D, m.getGLTextureIds()[j]);
				gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
				gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
				gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, j == 3 ? GL3.GL_NEAREST : GL3.GL_LINEAR);
				gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, j == 3 ? GL3.GL_NEAREST : GL3.GL_LINEAR);
				
				/*
				//Anisotropic Filtering
				float[] ansio = new float[1];
				gl.glGetFloatv(GL3.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, ansio,0);
				gl.glTexParameterf(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAX_ANISOTROPY_EXT, ansio[0]);
				*/											
				
				if (isBytes)
				{
					ByteBuffer colorTable = Buffers.newDirectByteBuffer(byteData);
					colorTable.position(0);
					colorTable.order(ByteOrder.LITTLE_ENDIAN);
					gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA, 4, 16, 0, GL3.GL_RGBA, GL3.GL_HALF_FLOAT, colorTable);
				}
				else
				{
					ByteBuffer dxtBB = Buffers.newDirectByteBuffer(tex.data);
					dxtBB.position(tex.mipmapOffsets[0]);
					dxtBB.order(ByteOrder.LITTLE_ENDIAN);
					
					switch(tex.compressionType){
					case 0x3420: 
						gl.glCompressedTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT, tex.uncompressedWidth, tex.uncompressedHeight, 0, tex.mipmapOffsets[1]-tex.mipmapOffsets[0], dxtBB);
						break;
					case 0x3430: 
						gl.glCompressedTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT, tex.uncompressedWidth, tex.uncompressedHeight, 0, tex.mipmapOffsets[1]-tex.mipmapOffsets[0], dxtBB);
						break;
					case 0x3431: 
						gl.glCompressedTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT, tex.uncompressedWidth, tex.uncompressedHeight, 0, tex.mipmapOffsets[1]-tex.mipmapOffsets[0], dxtBB);
						break;
					case 0x2460:
						gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA, tex.uncompressedWidth, tex.uncompressedHeight, 0, GL3.GL_RGBA, GL3.GL_HALF_FLOAT, dxtBB);
						break;
					default:
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
						gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA, img.getWidth(), img.getHeight(), 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, buffer);
					}				
				}
				//if (j != 3)
				//	gl.glGenerateMipmap(GL3.GL_TEXTURE_2D);
				
				gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
				
				m.loadShader(gl);
			}					
		}
		
		isVRAMLoaded = true;
	}

	public DX9VertexElement[] getDX9Struct(int lodLevel, int i) {
		return vertexElements[lodModels[lodLevel].meshList[i].getVertexElementIndex()];
	}
	
	public int getNumAnimations()
	{
		if (animFile == null)
			return 0;
		else
			return animFile.getNumAnimations();
	}
	
	public String getAnimationName(int index)
	{
		if (animFile == null)
			return null;
		else
			return animFile.getAnimationName(index);
	}

	public void setCurrentAnimation(int selectedIndex) {
		
		if (numBones == -1)
			return;
		
		if (HavokNative.setAnimation(selectedIndex) == -1)
		{
			HavokNative.setAnimation(0);
			System.out.println("Invalid Animation");
		}		
				
	}

	public void setAnimationSpeed(int speed) {
		animSpeed = 1/(float)speed;
	}
	
	public boolean isVRAMLoaded()
	{
		return isVRAMLoaded;
	}

	public void resetVRAM() {
		isVRAMLoaded = false;
	}
	
}
