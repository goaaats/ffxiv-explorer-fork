package ca.fraggergames.ffxivextract.shaders;

import java.io.IOException;

import javax.media.opengl.GL3;

public class BlurShader extends Shader {

	int texLocation, dirLocation, amountLocation, scaleLocation, strengthLocation, texelSizeLocation;
	
	public BlurShader(GL3 gl)
			throws IOException {
		super(gl, "/res/shaders/fbout_vert.glsl", "/res/shaders/blur_frag.glsl", true);	
		//super(gl, MinifiedShaders.fbout_vert_glsl, MinifiedShaders.blur_frag_glsl, false);
		
		texLocation = gl.glGetUniformLocation(shaderProgram, "uInTex");
		texelSizeLocation = gl.glGetUniformLocation(shaderProgram, "uTexelSize");
		dirLocation = gl.glGetUniformLocation(shaderProgram, "uBlurDirection");
		amountLocation = gl.glGetUniformLocation(shaderProgram, "uBlurAmount");
		scaleLocation = gl.glGetUniformLocation(shaderProgram, "uBlurScale");
		strengthLocation = gl.glGetUniformLocation(shaderProgram, "uBlurStrength");
	}

	public void setUniforms(GL3 gl, int texId, int direction, int amount, float scale, float strength, int width, int height) {
		gl.glUniform1i(texLocation, 0);    	
    	gl.glActiveTexture(GL3.GL_TEXTURE0);
    	gl.glBindTexture(GL3.GL_TEXTURE_2D, texId);
    	
    	gl.glUniform2f(texelSizeLocation, width != 0.0f ? 1.0f/(float)width : 0.0f, height != 0.0f ? 1.0f/(float)height : 0.0f);
    	gl.glUniform1i(dirLocation, direction);
    	gl.glUniform1i(amountLocation, 7);    	
    	gl.glUniform1f(scaleLocation, 1.5f);    	    
    	gl.glUniform1f(strengthLocation, 0.9f);   
	}

}
