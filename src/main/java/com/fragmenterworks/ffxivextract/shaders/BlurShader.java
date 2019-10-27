package com.fragmenterworks.ffxivextract.shaders;

import com.jogamp.opengl.GL3;

import java.io.IOException;

public class BlurShader extends Shader {

	int texLocation, texelSizeLocation, dirLocation, radiusLocation;
	
	public BlurShader(GL3 gl) throws IOException {
		//super(gl, "/res/shaders/fbout_vert.glsl", "/res/shaders/blur_frag.glsl", true);	
		super(gl, MinifiedShaders.fbout_vert_glsl, MinifiedShaders.blur_frag_glsl, false);
		
		texLocation = gl.glGetUniformLocation(shaderProgram, "uInTex");
		texelSizeLocation = gl.glGetUniformLocation(shaderProgram, "uTexelSize");
		dirLocation = gl.glGetUniformLocation(shaderProgram, "uBlurDirection");
		radiusLocation = gl.glGetUniformLocation(shaderProgram, "uBlurRadius");
	}

	public void setUniforms(GL3 gl, int texId, int direction, int radius, int width, int height) {
		gl.glUniform1i(texLocation, 0);    	
    	gl.glActiveTexture(GL3.GL_TEXTURE0);
    	gl.glBindTexture(GL3.GL_TEXTURE_2D, texId);
    	
    	gl.glUniform2f(texelSizeLocation, width != 0.0f ? 1.0f/(float)width : 0.0f, height != 0.0f ? 1.0f/(float)height : 0.0f);
    	gl.glUniform1i(dirLocation, direction);
    	gl.glUniform1i(radiusLocation, 1);    	  
	}

}
