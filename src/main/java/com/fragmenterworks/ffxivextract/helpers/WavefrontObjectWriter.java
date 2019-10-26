package com.fragmenterworks.ffxivextract.helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.fragmenterworks.ffxivextract.Constants;

import com.fragmenterworks.ffxivextract.models.Mesh;
import com.fragmenterworks.ffxivextract.models.Model;
import com.fragmenterworks.ffxivextract.models.directx.DX9VertexElement;

public class WavefrontObjectWriter {

	private static ByteOrder endian;

	public static void writeObj(String path, Model model, ByteOrder eEndian) throws IOException
	{
		endian = eEndian;

		if (path.contains(".mdl"))
			path=path.replace(".mdl", ".obj");
		else if (!path.contains(".obj"))
			path+=".obj";
		
		for (int i = 0; i < model.getNumMesh(0); i++)
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(path.replace(".obj", "_"+i+".obj")));
		
			out.write("#FFXIV Model\r\n#Exported using FFXIV Explorer by Ioncannon\r\n#Visit: "+Constants.URL_WEBSITE+"\r\n\r\n");
				
			//out.write("mtllib " + path.replace(".obj", ".mtl").substring(path.lastIndexOf("\\")+1) + "\r\n");
			
			//out.write("usemtl mesh"+i+"\r\n");
			
			DX9VertexElement[] elements = model.getDX9Struct(0, i);
			DX9VertexElement vertElement = null, texCoordElement = null, normalElement = null;
			
			for (DX9VertexElement e : elements)
			{
				switch (e.usage)
				{
				case 0:
					vertElement = e;
					break;				
				case 3:
					normalElement = e;
					break;
				case 4:
					texCoordElement = e;
					break;
				}
			}
			
			writeVerts(vertElement, model.getMeshes(0)[i], out);
			writeTexCoords(texCoordElement, model.getMeshes(0)[i], out);
			writeNormals(normalElement, model.getMeshes(0)[i], out);
			writeIndices(model.getMeshes(0)[i], out);
			
			out.close();
		}
				
		//writeMtl(path, model);
		
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
	
	private static void writeVerts(DX9VertexElement vertElement, Mesh mesh, BufferedWriter out) throws IOException {
		out.write("#Verts\r\n");
		
		ByteBuffer vertBuffer = mesh.vertBuffers[vertElement.stream];
		vertBuffer.order(endian);
		
		for (int i = 0; i < mesh.numVerts; i++)
		{			
			vertBuffer.position((i*mesh.vertexSizes[vertElement.stream]) + vertElement.offset);
		
			if (vertElement.datatype == 13 || vertElement.datatype == 14)
				out.write(String.format("v %f %f %f \r\n", Utils.convertHalfToFloat(vertBuffer.getShort()), Utils.convertHalfToFloat(vertBuffer.getShort()), Utils.convertHalfToFloat(vertBuffer.getShort())));			
			else if (vertElement.datatype == 2)			
				out.write(String.format("v %f %f %f \r\n", vertBuffer.getFloat(), vertBuffer.getFloat(), vertBuffer.getFloat()));			
		}
		
		out.write("\r\n");
	}

	private static void writeNormals(DX9VertexElement normalElement, Mesh mesh, BufferedWriter out) throws IOException {
		out.write("#Normals\r\n");
		
		ByteBuffer vertBuffer = mesh.vertBuffers[normalElement.stream];
		vertBuffer.order(endian);
		
		for (int i = 0; i < mesh.numVerts; i++)
		{
			vertBuffer.position((i*mesh.vertexSizes[normalElement.stream]) + normalElement.offset);		    		
			out.write(String.format("vn %f %f %f \r\n", Utils.convertHalfToFloat(vertBuffer.getShort()), Utils.convertHalfToFloat(vertBuffer.getShort()), Utils.convertHalfToFloat(vertBuffer.getShort())));
		}		
		
		out.write("\r\n");
	}

	private static void writeTexCoords(DX9VertexElement texCoordElement, Mesh mesh, BufferedWriter out) throws IOException {
		out.write("#Tex Coords\r\n");
		
		ByteBuffer vertBuffer = mesh.vertBuffers[texCoordElement.stream];
		vertBuffer.order(endian);
		
		for (int i = 0; i < mesh.numVerts; i++)
		{
			vertBuffer.position((i*mesh.vertexSizes[texCoordElement.stream]) + texCoordElement.offset);
			out.write(String.format("vt %f %f \r\n", Utils.convertHalfToFloat(vertBuffer.getShort()), Utils.convertHalfToFloat(vertBuffer.getShort())*-1));
		}		
		
		out.write("\r\n");
	}

	private static void writeIndices(Mesh mesh, BufferedWriter out) throws IOException {		
		out.write("#Indices\r\n");
		
		ByteBuffer indexBuffer = mesh.indexBuffer;
		indexBuffer.position(0);
		indexBuffer.order(endian);
		
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
