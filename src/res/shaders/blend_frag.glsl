#version 150
precision highp float;

varying vec2 vTexCoord;

uniform sampler2D uInTex1;
uniform sampler2D uInTex2;

void main() {	

	vec4 tex1 = texture2D(uInTex1, vTexCoord);
    vec4 tex2 = texture2D(uInTex2, vTexCoord);
       
    gl_FragColor = min(tex1 + tex2, 1.0);
}


