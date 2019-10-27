package com.fragmenterworks.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.fragmenterworks.ffxivextract.Constants;

import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.directx.D3DXShader_ConstantTable;

public class SHCD_File extends Game_File {

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
	
	public SHCD_File(String path, ByteOrder endian) throws IOException{
		super(endian);
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		loadSHPK(data);
	}

	public SHCD_File(byte[] data, ByteOrder endian) throws IOException {
		super(endian);
		loadSHPK(data);
	}

	private void loadSHPK(byte[] data) throws IOException {
		
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(endian);

		int magic = bb.getInt();

		//Check Signatures
		if (magic != 0x64436853) {
			Utils.getGlobalLogger().error("SHCD magic was incorrect.");
			Utils.getGlobalLogger().debug("Magic was {}", String.format("0x%08X", magic));
			return;
		}
		
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

		if (constantTable != null)
		{
			StringBuilder s = new StringBuilder();

			for (int i = 0; i < constantTable.constantInfo.length; i++) {
				s.append(" ");
				s.append(constantTable.constantInfo[i].TypeInfo.Columns);
				s.append("x");
				s.append(constantTable.constantInfo[i].TypeInfo.Rows);
				s.append(" Index: ");
				s.append(constantTable.constantInfo[i].RegisterIndex);
				s.append(" Count: ");
				s.append(constantTable.constantInfo[i].RegisterCount);

				if (constantTable.constantInfo[i].TypeInfo.StructMembers != 0) {
					s.append("Struct!\n");
					for (int j = 0; j < constantTable.constantInfo[i].TypeInfo.StructMemberInfo.length; j++) {
						s.append("  => ");
						s.append(constantTable.constantInfo[i].TypeInfo.StructMemberInfo[j].Name);
					}
				}
			}

			Utils.getGlobalLogger().trace("SHCD info:\nCreator: {}\nTarget: {}\nConstants: {}",
											constantTable.Creator, constantTable.Target, s.toString());
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
	
	public ShaderHeader getShaderHeader()
	{
		return shaderHeader;
	}
}
