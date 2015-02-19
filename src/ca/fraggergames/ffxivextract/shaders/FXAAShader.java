package ca.fraggergames.ffxivextract.shaders;

import java.io.IOException;

import javax.media.opengl.GL3;

public class FXAAShader extends Shader {

	int texLocation, sizeLocation;
	
	public FXAAShader(GL3 gl)
			throws IOException {
		super(gl, "/res/shaders/fbout_vert.glsl", "/res/shaders/fxaa_frag.glsl");
		
		texLocation = gl.glGetUniformLocation(shaderProgram, "uInTex");
		sizeLocation = gl.glGetUniformLocation(shaderProgram, "uSize");
	}
	
	public void setCanvasSize(GL3 gl, int width, int height)
	{
		gl.glUniform2f(sizeLocation, width, height);
	}

	public void setTexture(GL3 gl, int texId) {
		gl.glUniform1i(texLocation, 0);    	
    	gl.glActiveTexture(GL3.GL_TEXTURE0);
    	gl.glBindTexture(GL3.GL_TEXTURE_2D, texId);
	}

}
