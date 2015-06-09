package ca.fraggergames.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class SHPK_File {

	public final static int SHADERTYPE_VERTEX = 0;
	public final static int SHADERTYPE_PIXEL = 1;
	
	int fileLength;
	int shaderDataOffset;
	int parameterOffset;
	int numVertexShaders;
	int numPixelShaders;
	int numScalarParameters;
	int numResourceParameters;
	
	ArrayList<ShaderHeader> shaderHeaders = new ArrayList<SHPK_File.ShaderHeader>();
	
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
		
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		//Check Signatures
		if (bb.getInt() != 0x6B506853)
			throw new IOException("Not a SHPK file");
		if (bb.getInt() != 0x00395844)
			throw new IOException("Not a DX9 shader pack");
		
		fileLength = bb.getInt();
		shaderDataOffset = bb.getInt();
		parameterOffset = bb.getInt();
		numVertexShaders = bb.getInt();
		numPixelShaders = bb.getInt();
		
		bb.getInt();
		bb.getInt();
		
		numScalarParameters = bb.getInt();
		numResourceParameters = bb.getInt();
		 		
		bb.position(0x48);
			
		for (int i = 0; i < numVertexShaders; ++i) {
			ShaderHeader header = new ShaderHeader(bb, 0, SHADERTYPE_VERTEX);
			shaderHeaders.add(header);
        }
        for (int i = 0; i < numPixelShaders; ++i) {
            ShaderHeader header = new ShaderHeader(bb, 0, SHADERTYPE_PIXEL);
            shaderHeaders.add(header);
        }
        
	}
	
	public static class ShaderHeader
	{
		private int type;
		private int dataOffset;
		private int dataSize;
		
		public ShaderHeader(ByteBuffer bb, int offset, int type){

			this.type = type;
			dataOffset = bb.getInt();
			dataSize = bb.getInt();
			
			bb.getShort();
			bb.getShort();
		}
	}
	
}
