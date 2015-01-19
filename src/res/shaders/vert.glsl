#version 110
precision highp float;

uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjMatrix;

attribute vec4 aPosition;
attribute vec4 aNormal;
attribute vec4 aTexCoord;
attribute vec4 aColor;

varying vec4 vPosition;
varying vec4 vNormal;
varying vec4 vTexCoord;
varying vec4 vColor;

void main(void) {
	vPosition = aPosition;
	vTexCoord = aTexCoord;
	vNormal = vec4(normalize(aNormal.xyz), aNormal.a);	
	vColor = aColor;	

    gl_Position = uProjMatrix * uViewMatrix * uModelMatrix * aPosition;
}
