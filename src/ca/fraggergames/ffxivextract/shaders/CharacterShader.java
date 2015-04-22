package ca.fraggergames.ffxivextract.shaders;

import java.io.IOException;

import javax.media.opengl.GL3;

public class CharacterShader extends Shader {
		
	public CharacterShader(GL3 gl)
			throws IOException {
		super(gl, "/res/shaders/model_vert.glsl", "/res/shaders/character_frag.glsl", true);		
		//super(gl, MinifiedShaders.model_vert_glsl, MinifiedShaders.character_frag_glsl, false);
		
	}
	
}
