package ca.fraggergames.ffxivextract.shaders;

import java.io.IOException;

import javax.media.opengl.GL3;

public class DefaultShader extends Shader {

	public DefaultShader(GL3 gl)
			throws IOException {
		super(gl, "/res/shaders/default_vert.glsl", "/res/shaders/default_frag.glsl");		
	}

}
