package com.fragmenterworks.ffxivextract.shaders;

import com.jogamp.opengl.GL3;

import java.io.IOException;

public class CharacterShader extends Shader {

    public CharacterShader(GL3 gl) throws IOException {
        //super(gl, "/res/shaders/model_vert_boned.glsl", "/res/shaders/character_frag.glsl", true);
        super(gl, MinifiedShaders.model_vert_boned_glsl, MinifiedShaders.character_frag_glsl, false);
    }

}
