package ca.fraggergames.ffxivextract.shaders;

import java.io.IOException;

import javax.media.opengl.GL3;

public class SkinShader extends Shader {
		
	public SkinShader(GL3 gl)
			throws IOException {
		super(gl, "/res/shaders/skin_vert.glsl", "/res/shaders/skin_frag.glsl");		
	}

}
