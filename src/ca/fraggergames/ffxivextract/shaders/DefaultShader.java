package ca.fraggergames.ffxivextract.shaders;

import java.io.IOException;

import javax.media.opengl.GL3;

public class DefaultShader extends Shader {

	public DefaultShader(GL3 gl)
			throws IOException {
		//super(gl, "/res/shaders/model_vert.glsl", "/res/shaders/default_frag.glsl", true);	
		super(gl, MinifiedShaders.model_vert_glsl, MinifiedShaders.default_frag_glsl, false);		
	}

}
