package com.fragmenterworks.ffxivextract.shaders;

import com.jogamp.opengl.GL3;

import java.io.IOException;

public class SimpleShader extends Shader {

	public SimpleShader(GL3 gl)
			throws IOException {
		//super(gl, "/res/shaders/simple_vert.glsl", "/res/shaders/simple_frag.glsl", true);	
		super(gl, MinifiedShaders.simple_vert_glsl, MinifiedShaders.simple_frag_glsl, false);		
	}

}
