package ca.fraggergames.ffxivextract.helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ca.fraggergames.ffxivextract.models.Mesh;
import ca.fraggergames.ffxivextract.models.Model;

public class WavefrontObjectWriter {
	
	public static void writeObj(String path, Model model) throws IOException
	{		
		if (path.contains(".mdl"))
			path=path.replace(".mdl", ".obj");
		else if (!path.contains(".obj"))
			path+=".obj";
		
		for (int i = 0; i < model.getNumMesh(0); i++)
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(path.replace(".obj", "_"+i+".obj")));
		
			out.write("#FFXIV Model\r\n\r\n");
				
			out.write("mtllib " + path.replace(".obj", ".mtl").substring(path.lastIndexOf("\\")+1) + "\r\n");
			
			out.write("usemtl mesh"+i+"\r\n");
			
			writeVerts(model.getMeshes(0)[i], out);
			writeTexCoords(model.getMeshes(0)[i], out);
			writeNormals(model.getMeshes(0)[i], out);
			writeIndices(model.getMeshes(0)[i], out);
			
			out.close();
		}
				
		writeMtl(path, model);
		
	}
	
	public static void writeMtl(String path, Model model) throws IOException
	{		
		if (path.contains(".obj"))
			path=path.replace(".obj", ".mtl");
		else if (!path.contains(".mtl"))
			path+=".mtl";
		
		BufferedWriter out = new BufferedWriter(new FileWriter(path));
		
		out.write("#FFXIV Material\r\n");
		
		for (int i = 0; i < model.getNumMesh(0); i++)
		{		
			out.write("newmtl mesh" + i + "\r\n");
			out.write("illum 2\r\n");
			out.write("Ka 0.9882 0.9882 0.9882\r\n");
			out.write("Kd 0.9882 0.9882 0.9882\r\n");
			out.write("Ks 0.0000 0.0000 0.0000\r\n");			
			out.write("map_Kd "+ path.replace(".mtl", "_d.tga").substring(path.lastIndexOf("\\")+1) +"\r\n");			
			out.write("map_bump "+ path.replace(".mtl", "_n.tga").substring(path.lastIndexOf("\\")+1) +"\r\n");
			out.write("\r\n");
		}
		
		out.close();
	}
	
	private static void writeVerts(Mesh mesh, BufferedWriter out) throws IOException {
		out.write("#Verts\r\n");
		
		ByteBuffer vertBuffer = mesh.vertBuffer;
		vertBuffer.order(ByteOrder.LITTLE_ENDIAN);
		vertBuffer.position(0);
		for (int i = 0; i < mesh.numVerts; i++)
		{
			if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)
				vertBuffer.position(i*8);
			else
				vertBuffer.position(i*12);
			
			if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)			
				out.write(String.format("v %f %f %f \r\n", Utils.convertHalfToFloat(vertBuffer.getShort()), Utils.convertHalfToFloat(vertBuffer.getShort()), Utils.convertHalfToFloat(vertBuffer.getShort())));			
			else if (mesh.vertexSize == 0x14)			
				out.write(String.format("v %f %f %f \r\n", vertBuffer.getFloat(), vertBuffer.getFloat(), vertBuffer.getFloat()));
			
		}
		
		out.write("\r\n");
	}

	private static void writeNormals(Mesh mesh, BufferedWriter out) throws IOException {
		out.write("#Normals\r\n");
		
		ByteBuffer vertBuffer = mesh.vertBuffer;
		vertBuffer.order(ByteOrder.LITTLE_ENDIAN);
		if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)
			vertBuffer.position(mesh.numVerts*8);
	    else
	    	vertBuffer.position(mesh.numVerts*12);
		for (int i = 0; i < mesh.numVerts; i++)
		{
			if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)
				vertBuffer.position((mesh.numVerts*8) + (i*24));
		    else
		    	vertBuffer.position((mesh.numVerts*12) + (i*24));
				
			out.write(String.format("vn %f %f %f \r\n", Utils.convertHalfToFloat(vertBuffer.getShort()), Utils.convertHalfToFloat(vertBuffer.getShort()), Utils.convertHalfToFloat(vertBuffer.getShort())));
		}		
		
		out.write("\r\n");
	}

	private static void writeTexCoords(Mesh mesh, BufferedWriter out) throws IOException {
		out.write("#Tex Coords\r\n");
		
		ByteBuffer vertBuffer = mesh.vertBuffer;
		vertBuffer.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < mesh.numVerts; i++)
		{
			if (mesh.vertexSize == 0x10 || mesh.vertexSize == 0x8)
				vertBuffer.position((mesh.numVerts*8) + (i*24) + 16);
		    else
		    	vertBuffer.position((mesh.numVerts*12) + (i*24) + 16);
				
			out.write(String.format("vt %f %f \r\n", Utils.convertHalfToFloat(vertBuffer.getShort()), Utils.convertHalfToFloat(vertBuffer.getShort())));
		}		
		
		out.write("\r\n");
	}

	private static void writeIndices(Mesh mesh, BufferedWriter out) throws IOException {		
		out.write("#Indices\r\n");
		
		ByteBuffer indexBuffer = mesh.indexBuffer;
		indexBuffer.position(0);
		indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < mesh.numIndex; i+=3)
		{
			int ind1 = indexBuffer.getShort()+1;
			int ind2 = indexBuffer.getShort()+1;
			int ind3 = indexBuffer.getShort()+1;
			out.write(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d \r\n", ind1, ind1, ind1, ind2, ind2, ind2, ind3, ind3, ind3));
		}
		
		out.write("\r\n");
	}
	
}
