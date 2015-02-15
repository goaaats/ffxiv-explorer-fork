#version 150
precision highp float;

attribute vec4 aPosition;

varying vec2 vTexCoord;

void main(void) {			
	vTexCoord = (aPosition * 0.5 + 0.5).xy;
    gl_Position = vec4(aPosition.xy, 0.0, 1.0);
}
