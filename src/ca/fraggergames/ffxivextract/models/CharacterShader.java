package ca.fraggergames.ffxivextract.models;

import java.io.IOException;

import javax.media.opengl.GL3;

public class CharacterShader extends Shader {
		
	public CharacterShader(GL3 gl)
			throws IOException {
		super(gl, "/res/shaders/character_vert.glsl", "/res/shaders/character_frag.glsl");		
	}

}
