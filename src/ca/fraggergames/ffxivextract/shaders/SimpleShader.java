package ca.fraggergames.ffxivextract.shaders;

import java.io.IOException;

import javax.media.opengl.GL3;

public class SimpleShader extends Shader {

	public SimpleShader(GL3 gl)
			throws IOException {
		super(gl, "/res/shaders/simple_vert.glsl", "/res/shaders/simple_frag.glsl", true);	
		//super(gl, MinifiedShaders.model_vert_glsl, MinifiedShaders.default_frag_glsl, false);		
	}

}
