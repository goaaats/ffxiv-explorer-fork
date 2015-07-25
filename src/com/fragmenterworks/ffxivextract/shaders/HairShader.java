package com.fragmenterworks.ffxivextract.shaders;

import java.io.IOException;

import javax.media.opengl.GL3;

public class HairShader extends Shader {
	
	private int hairColorLocation;
	private int highlightColorLocation;
	
	public HairShader(GL3 gl)
			throws IOException {
		//super(gl, "/res/shaders/model_vert.glsl", "/res/shaders/hair_frag.glsl", true);	
		super(gl, MinifiedShaders.model_vert_glsl, MinifiedShaders.hair_frag_glsl, false);		
		
		hairColorLocation = gl.glGetUniformLocation(shaderProgram, "uHairColor");
		highlightColorLocation = gl.glGetUniformLocation(shaderProgram, "uHighlightColor");
	}

	public void setHairColor(GL3 gl, float hairColor[], float highlightColor[])
	{
		gl.glUniform4fv(hairColorLocation, 1, hairColor, 0);
		gl.glUniform4fv(highlightColorLocation, 1, highlightColor, 0);
	}
}
