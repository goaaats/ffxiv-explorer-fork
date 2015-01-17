package ca.fraggergames.ffxivextract.models;

public class Material {
	
	String materialPath;
	Texture_File diffusem, normal, specular;
	Texture_File colorSet1, colorSet2;
	
	public Material(byte[] data) {
		this(null, data);
	}
	
	public Material(String materialPath, byte[] data) {
		this.materialPath = materialPath;
	}
	
}
