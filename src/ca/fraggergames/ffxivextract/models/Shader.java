package ca.fraggergames.ffxivextract.models;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Scanner;

import javax.media.opengl.GL3;
import javax.media.opengl.GL3bc;
import javax.media.opengl.GLAutoDrawable;

import ca.fraggergames.ffxivextract.gui.components.OpenGL_View;
import ca.fraggergames.ffxivextract.helpers.Matrix;


public class Shader {

	protected int shaderProgram;
	private Hashtable<String, Object> uniforms = new Hashtable<String, Object>();
	
	private int modelLocation;
	private int viewLocation;
	private int projLocation;
	
	private int positionLocation;
	private int normalLocation;
	private int texCoordLocation;
	private int binormalLocation;
	private int colorLocation;
	
	private int diffuseTexLocation;
	private int normalTexLocation;
	private int specularTexLocation;
	private int colorSetTexLocation;
	
	private int usesDiffuseLocation;
	private int usesNormalLocation;
	private int usesSpecularLocation;
	private int usesColorSetLocation;	
	
	public Shader(GL3 gl, String vertPath, String fragPath) throws IOException
	{
		//Build shader
		String vsrc = readFromStream(OpenGL_View.class
				 .getResourceAsStream(vertPath)); 
		 
		String fsrc = readFromStream(OpenGL_View.class
				 .getResourceAsStream(fragPath)); 
		
		int vertShader = createShader(gl, GL3.GL_VERTEX_SHADER, vsrc);
		int fragShader = createShader(gl, GL3.GL_FRAGMENT_SHADER, fsrc);
		
		shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, vertShader);
		gl.glAttachShader(shaderProgram, fragShader);
		gl.glLinkProgram(shaderProgram);
		gl.glValidateProgram(shaderProgram);
		
		//Set Attrib Locations
		positionLocation = gl.glGetAttribLocation(shaderProgram, "aPosition");
		normalLocation = gl.glGetAttribLocation(shaderProgram, "aNormal");
		texCoordLocation = gl.glGetAttribLocation(shaderProgram, "aTexCoord");
		binormalLocation = gl.glGetAttribLocation(shaderProgram, "aBiTangent");
		colorLocation = gl.glGetAttribLocation(shaderProgram, "aColor");
		
		//Set Uniform Locations
		modelLocation = gl.glGetUniformLocation(shaderProgram, "uModelMatrix");
		viewLocation = gl.glGetUniformLocation(shaderProgram, "uViewMatrix");
		projLocation = gl.glGetUniformLocation(shaderProgram, "uProjMatrix");

		//Set Uniform Tex Locations
		diffuseTexLocation = gl.glGetUniformLocation(shaderProgram, "uDiffuseTex");
		normalTexLocation = gl.glGetUniformLocation(shaderProgram, "uNormalTex");
		specularTexLocation = gl.glGetUniformLocation(shaderProgram, "uSpecularTex");
		colorSetTexLocation = gl.glGetUniformLocation(shaderProgram, "uColorSetTex");
		
		usesDiffuseLocation = gl.glGetUniformLocation(shaderProgram, "uHasDiffuse");
		usesNormalLocation = gl.glGetUniformLocation(shaderProgram, "uHasNormal");
		usesSpecularLocation = gl.glGetUniformLocation(shaderProgram, "uHasSpecular");
		usesColorSetLocation = gl.glGetUniformLocation(shaderProgram, "uHasColorSet");
	}	
	
	private String readFromStream(InputStream ins) throws IOException {
		if (ins == null) {
			 throw new IOException("Could not read from stream.");
		}
		StringBuffer buffer = new StringBuffer();
	 	Scanner scanner = new Scanner(ins);
		try {
			while (scanner.hasNextLine()) {
				buffer.append(scanner.nextLine() + "\n");
			}
		} finally {
				scanner.close();
			}
		return buffer.toString(); 
	}

	private int createShader(GL3 gl, int shaderType, String source)
	{
		int shader = gl.glCreateShader(shaderType);
		gl.glShaderSource(shader, 1, new String[] {source}, (int[]) null, 0);
		gl.glCompileShader(shader);
		
		int[] compiled = new int[1];
		gl.glGetShaderiv(shader, GL3.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
           byte[] infoLog = new byte[1024];
           gl.glGetShaderInfoLog(shader, 1024, null, 0, infoLog, 0);
           System.out.println(new String(infoLog));
           gl.glDeleteShader(shader);
           shader = 0;
           return 0;
        }
		
        return shader;
	}	
	
	public void setTextures(GL3 gl, Material mat)
	{
		//Textures
    	if (mat != null){
    		
    		if (mat.getDiffuseMapTexture() != null)
    			gl.glUniform1i(usesDiffuseLocation, 1);
    		if (mat.getNormalMapTexture() != null)
    			gl.glUniform1i(usesNormalLocation, 1);
    		if (mat.getSpecularMapTexture() != null)
    			gl.glUniform1i(usesSpecularLocation, 1);
    		if (mat.getColorSetTexture() != null)
    			gl.glUniform1i(usesColorSetLocation, 1);
	    	
	    	gl.glUniform1i(diffuseTexLocation, 0);
	    	gl.glUniform1i(normalTexLocation, 1);
	    	gl.glUniform1i(specularTexLocation, 2);
	    	gl.glUniform1i(colorSetTexLocation, 3);
    			    	
	    	gl.glActiveTexture(GL3.GL_TEXTURE0);
	    	gl.glBindTexture(GL3.GL_TEXTURE_2D, mat.getGLTextureIds()[0]);
	    	gl.glActiveTexture(GL3.GL_TEXTURE1);
	    	gl.glBindTexture(GL3.GL_TEXTURE_2D, mat.getGLTextureIds()[1]);
	    	gl.glActiveTexture(GL3.GL_TEXTURE2);
	    	gl.glBindTexture(GL3.GL_TEXTURE_2D, mat.getGLTextureIds()[2]);
	    	gl.glActiveTexture(GL3.GL_TEXTURE3);
	    	gl.glBindTexture(GL3.GL_TEXTURE_2D, mat.getGLTextureIds()[3]);		    	
	    		    	    	
    	}
    	    	
	}
	
	public int getShaderProgramID()
	{
		return shaderProgram;
	}
	
	public void setMatrix(GL3 gl, float modelMatrix[], float viewMatrix[], float projMatrix[])
	{
    	gl.glUniformMatrix4fv(modelLocation, 1, false, modelMatrix, 0);
    	gl.glUniformMatrix4fv(viewLocation, 1, false, viewMatrix, 0);
    	gl.glUniformMatrix4fv(projLocation, 1, false, projMatrix, 0);	
	}
	
	public int getAttribPosition()
	{return positionLocation;}
	
	public int getAttribColor()
	{return colorLocation;}

	public int getAttribNormal()
	{return normalLocation;}

	public int getAttribBiTangent()
	{return binormalLocation;}
	
	public int getAttribTexCoord()
	{return texCoordLocation;}
	
}
