package ca.fraggergames.ffxivextract.models;

import java.io.IOException;

import javax.media.opengl.GL3;

public class IrisShader extends Shader {
	
	private int usesColorSetLocation;
	private int eyeCatchTexLocation;
	private int eyeColorLocation;
	
	public IrisShader(GL3 gl)
			throws IOException {
		super(gl, "/res/shaders/iris_vert.glsl", "/res/shaders/iris_frag.glsl");
		
		usesColorSetLocation = gl.glGetUniformLocation(shaderProgram, "uHasCatchLightTex");
		eyeCatchTexLocation = gl.glGetUniformLocation(shaderProgram, "uCatchLightTex");
		eyeColorLocation = gl.glGetUniformLocation(shaderProgram, "uEyeColor");		
	}
	
	public void setEyeCatchTexture(GL3 gl, Material mat)
	{		
		gl.glUniform1i(usesColorSetLocation, 1);
    	gl.glUniform1i(eyeCatchTexLocation, 4);			    	
    	gl.glActiveTexture(GL3.GL_TEXTURE4);
    	gl.glBindTexture(GL3.GL_TEXTURE_2D, mat.getGLTextureIds()[0]);
	}
	
	public void setEyeColor(GL3 gl, float eyeColor[])
	{
		gl.glUniform4fv(eyeColorLocation, 1, eyeColor, 0);
	}
	
}
