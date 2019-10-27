package com.fragmenterworks.ffxivextract.shaders;

import com.jogamp.opengl.GL3;

import java.io.IOException;

public class DefaultShader extends Shader {

	public DefaultShader(GL3 gl) throws IOException {
		//super(gl, "/res/shaders/model_vert.glsl", "/res/shaders/default_frag.glsl", true);	
		super(gl, MinifiedShaders.model_vert_glsl, MinifiedShaders.default_frag_glsl, false);		
	}

}
