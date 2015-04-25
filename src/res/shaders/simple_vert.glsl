#version 150
precision highp float;

uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjMatrix;

attribute vec4 aPosition;

void main(void) {		

    gl_Position = uProjMatrix * uViewMatrix * uModelMatrix * aPosition;
}
