#version 110

varying vec4 vPosition;
varying vec4 vNormal;
varying vec4 vTexCoord;
varying vec4 vColor;

uniform sampler2D uDiffuseTex;
uniform sampler2D uNormalTex;
uniform sampler2D uSpecularTex;
uniform sampler2D uColorSetTex;

uniform bool uHasDiffuse;
uniform bool uHasNormal;
uniform bool uHasSpecular;
uniform bool uHasColorSet;


void main() {
	
	vec4 normal_color;
	
	if (uHasNormal) {
        normal_color = texture2D(uNormalTex, vTextureCoord.xy);

        // Alpha testing
        if (normal_color.b < 0.5) {
            discard;
        }
    }

    gl_FragColor = texture2D(diffuse, vTexCoord.st);    
}