package ca.fraggergames.ffxivextract.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Model {

	//int numVerts = 1981;
	//int numIndex = 6252;
	int numVerts = 2770;
	int numIndex = 8220;
	//int numVerts = 24;
	//int numIndex = 36;
	//int numVerts = 2743;
	//int numIndex = 6438;
	
	public short[] verts = new short[numVerts*4];
	public int[] boneWeight = new int[numVerts];
	public int[] boneIndex = new int[numVerts];
	
	public short[] normals = new short[numVerts*3];
	public byte[] biNormals = new byte[numVerts*4];
	public byte[] colors = new byte[numVerts*4];
	public short[] texCords = new short[numVerts*4];	
	
	public short indices[] = new short [numIndex];
	
	public Model(byte[] data)
	{
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < numVerts; i++)
		{			
			verts[i*4] = bb.getShort();
			verts[i*4+1] = bb.getShort();
			verts[i*4+2] = bb.getShort();
			verts[i*4+3] = bb.getShort();
			boneWeight[i] = bb.getInt();
			boneIndex[i] = bb.getInt();
		}					
		
		//Normals, Binormals, Colors, and Tex Coords
		for (int i = 0; i < numVerts; i++)
		{
			normals[i*3] = bb.getShort();
			normals[i*3+1] = bb.getShort();
			normals[i*3+2] = bb.getShort();
			bb.getShort();
			
			biNormals[i*4] = bb.get();
			biNormals[i*4+1] = bb.get();
			biNormals[i*4+2] = bb.get();
			biNormals[i*4+3] = bb.get();
			
			colors[i*4] = bb.get();
			colors[i*4+1] = bb.get();
			colors[i*4+2] = bb.get();
			colors[i*4+3] = bb.get();
			
			texCords[i*4] = bb.getShort();
			texCords[i*4+1] = bb.getShort();
			texCords[i*4+2] = bb.getShort();
			texCords[i*4+3] = bb.getShort();
		}		
		
		//Index Table
		for (int i = 0; i < numIndex; i++)
		{
			indices[i]=bb.getShort();
			
			if (indices[i] > verts.length/4)
				System.out.println(String.format("FUCK, INVALID VERT: %x @ %x", indices[i], i));
		}
	}
	
}
