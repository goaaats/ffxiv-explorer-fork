#version 110

varying vec4 vPosition;
varying vec4 vNormal;
varying vec4 vTexCoord;
varying vec4 vColor;

uniform sampler2D diffuse;
uniform sampler2D normal;
uniform sampler2D specular;
uniform sampler2D colorSet;

void main() {
    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);    
}