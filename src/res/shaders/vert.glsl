#version 110
precision highp float;

uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjMatrix;

attribute vec4 aPosition;
attribute vec4 aNormal;
attribute vec4 aTexCoord;
attribute vec4 aColor;
attribute vec4 aBiTangent;

varying vec4 vPosition;
varying vec4 vNormal;
varying vec4 vTexCoord;
varying vec4 vColor;
varying vec4 vBiTangent;
varying vec3 vLightDir;
varying vec3 vEyeVec;

void main(void) {
	vLightDir = vec3(vec3(1.0,1.0,1.0)-vec3(uViewMatrix * uModelMatrix * aPosition));
	vEyeVec = -vec3(uViewMatrix * uModelMatrix * aPosition);
	
	vPosition = vec4((uViewMatrix*uModelMatrix) * aPosition);
	vTexCoord = aTexCoord;
	vNormal = vec4(normalize(aNormal.xyz), aNormal.a);	
	vColor = aColor;	
	vBiTangent = aBiTangent;

    gl_Position = uProjMatrix * uViewMatrix * uModelMatrix * aPosition;
}
