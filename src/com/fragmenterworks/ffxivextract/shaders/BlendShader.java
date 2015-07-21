package com.fragmenterworks.ffxivextract.shaders;

import java.io.IOException;

import javax.media.opengl.GL3;
import javax.media.opengl.GL3bc;

public class BlendShader extends Shader {
	
	int tex1Location, tex2Location, intensityLocation;
	
	public BlendShader(GL3 gl)
			throws IOException {
		super(gl, "/res/shaders/fbout_vert.glsl", "/res/shaders/blend_frag.glsl", true);	
		//super(gl, MinifiedShaders.fbout_vert_glsl, MinifiedShaders.blur_frag_glsl, false);
		
		tex1Location = gl.glGetUniformLocation(shaderProgram, "uInTex1");
		tex2Location = gl.glGetUniformLocation(shaderProgram, "uInTex2");
		intensityLocation = gl.glGetUniformLocation(shaderProgram, "uGlowIntensity");
	}

	public void setUniforms(GL3bc gl, int texId1, int texId2, float glowIntensity) {
		
		gl.glUniform1i(tex1Location, 0);
    	gl.glUniform1i(tex2Location, 1);
    	
    	gl.glActiveTexture(GL3.GL_TEXTURE0);
    	gl.glBindTexture(GL3.GL_TEXTURE_2D, texId1);
    	
    	gl.glActiveTexture(GL3.GL_TEXTURE1);
    	gl.glBindTexture(GL3.GL_TEXTURE_2D, texId2);    
    	
    	gl.glUniform1f(intensityLocation, glowIntensity);
		
	}

}
