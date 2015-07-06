package ca.fraggergames.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class SHCD_File {

	public final static int SHADERTYPE_VERTEX = 0;
	public final static int SHADERTYPE_PIXEL = 1;
	
	private int fileLength;
	private int shaderType;	
	private String directXVersion;
	private int shaderBytecodeOffset;
	private int shaderBytecodeSize;
	private int shaderStringBlockoffset;
	private int numConstants, numSamplers, numX, numY;
	
	private byte[] shaderBytecode;	
	
	private ParameterInfo paramInfo[];
	
	public SHCD_File(String path) throws IOException{
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		loadSHPK(data);
	}

	public SHCD_File(byte[] data) throws IOException {
		loadSHPK(data);
	}

	private void loadSHPK(byte[] data) throws IOException {
		
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		//Check Signatures
		if (bb.getInt() != 0x64436853)
			throw new IOException("Not a ShCd file");
		
		//Read in header
		bb.getShort();
		bb.get();
		shaderType = bb.get();
		
		byte dxStringBuffer[] = new byte[4];
		bb.get(dxStringBuffer);
		directXVersion = new String(dxStringBuffer);
		
		fileLength = bb.getInt();
		shaderBytecodeOffset = bb.getInt();
		shaderStringBlockoffset = bb.getInt();
		bb.getInt();
		shaderBytecodeSize = bb.getInt();
		numConstants = bb.getShort();
		numSamplers = bb.getShort();
		numX = bb.getShort();
		numY = bb.getShort();
		    
		//Read in parameter info
		paramInfo = new ParameterInfo[numConstants + numSamplers + numX + numY];
		for (int i = 0; i < paramInfo.length; i++)
			paramInfo[i] = new ParameterInfo(bb);
			
		//Read in ? if vertex shader
		if (shaderType == SHADERTYPE_VERTEX)
			bb.getInt();
		
		//Set the param strings
		//for (int i = 0; i < paramInfo.length; i++)
	
		//Read in bytecode
		bb.position(shaderBytecodeOffset);
		shaderBytecode = new byte[shaderBytecodeSize];
		bb.get(shaderBytecode);
	}
	
	public int getShaderType()
	{
		return shaderType;
	}	
	
	public byte[] getShaderBytecode()
	{
		return shaderBytecode;
	}
	
	public static class ParameterInfo
	{
		final public int id;
		final public int stringOffset;
		final public int stringSize;
		final public int unknown1;
		final public int unknown2;
		
		public String parameterName = "";
		
		public ParameterInfo(ByteBuffer bb){

			id = bb.getInt();
			stringOffset = bb.getInt();
			stringSize = bb.getInt();
			unknown1 = bb.getShort();
			unknown2 = bb.getShort();
			
		}
	}
	
}
