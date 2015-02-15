package ca.fraggergames.ffxivextract.models;

import java.io.IOException;

import javax.media.opengl.GL3;

public class BlurShader extends Shader {

	int texLocation, dirLocation, radiusLocation, resolutionLocation;
	
	public BlurShader(GL3 gl)
			throws IOException {
		super(gl, "/res/shaders/fbout_vert.glsl", "/res/shaders/blur_frag.glsl");
		
		texLocation = gl.glGetUniformLocation(shaderProgram, "uInTex");
		dirLocation = gl.glGetUniformLocation(shaderProgram, "uDir");
		radiusLocation = gl.glGetUniformLocation(shaderProgram, "uRadius");
		resolutionLocation = gl.glGetUniformLocation(shaderProgram, "uResolution");
	}

	public void setUniforms(GL3 gl, int texId, float dirX, float dirY, float radius, float width, float height) {
		gl.glUniform1i(texLocation, 0);    	
    	gl.glActiveTexture(GL3.GL_TEXTURE0);
    	gl.glBindTexture(GL3.GL_TEXTURE_2D, texId);
    	
    	gl.glUniform2f(dirLocation, dirX, dirY);
    	gl.glUniform1f(radiusLocation, radius);    	
    	gl.glUniform2f(resolutionLocation, width, height);    	    	
	}

}
