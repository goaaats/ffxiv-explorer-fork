package com.fragmenterworks.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.fragmenterworks.ffxivextract.models.directx.D3DXShader_ConstantTable;

public class SHPK_File {

	public final static int SHADERTYPE_VERTEX = 0;
	public final static int SHADERTYPE_PIXEL = 1;
	
	byte data[];
	
	int fileLength;
	String directXVersion;
	int shaderDataOffset;
	int parameterOffset;
	int numVertexShaders;
	int numPixelShaders;
	int numConstants, numSamplers, numX, numY;
	
	ParameterInfo[] paramInfo;
	
	ArrayList<ShaderHeader> shaderHeaders = new ArrayList<ShaderHeader>();
	
	public SHPK_File(String path) throws IOException{
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		loadSHPK(data);
	}

	public SHPK_File(byte[] data) throws IOException {
		loadSHPK(data);
	}

	private void loadSHPK(byte[] data) throws IOException {
		
		this.data = data;
		
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		//Check Signatures
		if (bb.getInt() != 0x6B506853)
			throw new IOException("Not a SHPK file");
		
		bb.getInt();
		
		byte dxStringBuffer[] = new byte[4];
		bb.get(dxStringBuffer);
		directXVersion = new String(dxStringBuffer);
		
		fileLength = bb.getInt();
		shaderDataOffset = bb.getInt();
		parameterOffset = bb.getInt();
		numVertexShaders = bb.getInt();
		numPixelShaders = bb.getInt();
		
		bb.getInt();
		int someNum = bb.getInt();
		
		numConstants = bb.getInt();
		numSamplers = bb.getInt();
		 		
		bb.getInt(); //Count?
		bb.getInt(); //Count?
		bb.getInt(); //Count?
		bb.getInt(); //Offsets?
		bb.getInt(); //Offsets?
		bb.getInt(); //Offsets?
		
		//Read in shader headers
		for (int i = 0; i < numVertexShaders; ++i) {
			ShaderHeader header = new ShaderHeader(SHADERTYPE_VERTEX, bb);
			shaderHeaders.add(header);						
        }
        for (int i = 0; i < numPixelShaders; ++i) {
            ShaderHeader header = new ShaderHeader(SHADERTYPE_PIXEL, bb);
            shaderHeaders.add(header);
        }
        
        bb.position(bb.position()+(someNum*8));
        
        //Read in parameter info for the pack
  		paramInfo = new ParameterInfo[numConstants + numSamplers];
  		for (int i = 0; i < paramInfo.length; i++)
  			paramInfo[i] = new ParameterInfo(bb);	
  		
  		//Read in strings for headers
  		for (int i = 0; i < shaderHeaders.size(); i++)
  		{
  			ShaderHeader header = shaderHeaders.get(i);
  			for (int j = 0; j < header.paramInfo.length; j++)
  			{
  				byte buff[] = new byte[header.paramInfo[j].stringSize];
  				bb.position(parameterOffset + header.paramInfo[j].stringOffset);
  				bb.get(buff);
  				header.paramInfo[j].parameterName = new String(buff);  				
  			}
  		}
  		
  		//Read in strings for shaderpack
  		for (int j = 0; j < paramInfo.length; j++)
		{
			byte buff[] = new byte[paramInfo[j].stringSize];
			bb.position(parameterOffset + paramInfo[j].stringOffset);
			bb.get(buff);
			paramInfo[j].parameterName = new String(buff);  				
		}
	}
	
	public D3DXShader_ConstantTable getConstantTable(int shaderIndex)
	{
		if (directXVersion.equals("DX9\0"))
			return D3DXShader_ConstantTable.getConstantTable(getShaderBytecode(shaderIndex));
		else
			return null;
	}
	
	public int getShaderType(int shaderIndex)
	{
		return shaderHeaders.get(shaderIndex).type;
	}	
	
	public byte[] getShaderBytecode(int shaderIndex)
	{
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		ShaderHeader header = shaderHeaders.get(shaderIndex);		
		
		byte shaderBytecode[] = new byte[header.shaderBytecodeSize - (header.type == SHADERTYPE_VERTEX ? 4 : 0)];
		bb.position(shaderDataOffset + header.shaderBytecodeOffset  + (header.type == SHADERTYPE_VERTEX ? 4 : 0));
		bb.get(shaderBytecode);
		return shaderBytecode;
	}

	public int getNumVertShaders() {
		return numVertexShaders;
	}
	
	public int getNumPixelShaders() {
		return numPixelShaders;
	}
	
	public ShaderHeader getShaderHeader(int i)
	{
		return shaderHeaders.get(i);
	}
}
