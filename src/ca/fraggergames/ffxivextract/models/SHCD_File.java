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
	private ShaderHeader shaderHeader;
	
	private byte[] shaderBytecode;	
	
	private int shaderStartBytecodeOffset;
	private int shaderStringBlockoffset;
	
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
		shaderStartBytecodeOffset = bb.getInt();
		shaderStringBlockoffset = bb.getInt();
		
		//Read in shader header
		shaderHeader = new ShaderHeader(shaderType, bb);		    						
	
		//Set the param strings
		for (int i = 0; i < shaderHeader.paramInfo.length; i++)
		{
			bb.position(shaderStringBlockoffset + shaderHeader.paramInfo[i].stringOffset);
			byte buffer[] = new byte[shaderHeader.paramInfo[i].stringSize];
			bb.get(buffer);
			shaderHeader.paramInfo[i].parameterName = new String(buffer);
		}
	
		//Read in bytecode
		bb.position(shaderStartBytecodeOffset + shaderHeader.shaderBytecodeOffset);
		
		//Read in ? if vertex shader
		if (shaderType == SHADERTYPE_VERTEX)
			bb.getInt();
		
		shaderBytecode = new byte[shaderHeader.shaderBytecodeSize - (shaderType == SHADERTYPE_VERTEX?4 : 0)];
		bb.get(shaderBytecode);
		
		//Constant Table in bytecode IF DX9
		if (directXVersion.equals("DX9\0"))
			constantTable = D3DXShader_ConstantTable.getConstantTable(shaderBytecode);
		
		if (Constants.DEBUG && constantTable != null)
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
	
}
