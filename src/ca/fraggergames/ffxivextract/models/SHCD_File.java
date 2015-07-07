package ca.fraggergames.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.models.directx.D3DXShader_ConstantTable;

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
	
	private D3DXShader_ConstantTable constantTable;
	
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
		
		//Set the param strings
		//for (int i = 0; i < paramInfo.length; i++)
	
		//Read in bytecode
		bb.position(shaderBytecodeOffset);
		
		//Read in ? if vertex shader
		if (shaderType == SHADERTYPE_VERTEX)
			bb.getInt();
		
		shaderBytecode = new byte[shaderBytecodeSize - (shaderType == SHADERTYPE_VERTEX?4 : 0)];
		bb.get(shaderBytecode);
		
		//Constant Table in bytecode
		constantTable = D3DXShader_ConstantTable.getConstantTable(shaderBytecode);
		
		if (Constants.DEBUG)
		{
			System.out.println(constantTable.Creator);
			System.out.println(constantTable.Target);
			System.out.println("Constants:");
			for (int i = 0; i < constantTable.constantInfo.length; i++)
			{
				System.out.println(constantTable.constantInfo[i].Name + " " + constantTable.constantInfo[i].TypeInfo.Columns + "x" +constantTable.constantInfo[i].TypeInfo.Rows + " Index: " + constantTable.constantInfo[i].RegisterIndex + " Count: " + constantTable.constantInfo[i].RegisterCount);
				
				if (constantTable.constantInfo[i].TypeInfo.StructMembers != 0)
				{
					System.out.println("Struct!:");
					for (int j = 0; j < constantTable.constantInfo[i].TypeInfo.StructMemberInfo.length; j++)
						System.out.println("=>" + constantTable.constantInfo[i].TypeInfo.StructMemberInfo[j].Name);
				}
			}
		}
	}
	
	public D3DXShader_ConstantTable getConstantTable()
	{
		return constantTable;
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
