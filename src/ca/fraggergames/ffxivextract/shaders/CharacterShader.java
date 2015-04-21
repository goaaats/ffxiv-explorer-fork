package ca.fraggergames.ffxivextract.shaders;

import java.io.IOException;

import javax.media.opengl.GL3;

public class CharacterShader extends Shader {
		
	public int boneMatrixLocation;
	
	public CharacterShader(GL3 gl)
			throws IOException {
		super(gl, "/res/shaders/model_vert_boned.glsl", "/res/shaders/character_frag.glsl", true);		
		//super(gl, MinifiedShaders.model_vert_glsl, MinifiedShaders.character_frag_glsl, false);

		boneMatrixLocation = gl.glGetUniformLocation(shaderProgram, "uBoneMatrix");
	}

	public void setBoneMatrix(GL3 gl, float[][] boneMatrix)
	{
		//gl.glUniformMatrix4fv(boneMatrixLocation, 1, boneMatrix, 0);		
	}
}
