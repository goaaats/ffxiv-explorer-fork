#version 150
precision highp float;

varying vec2 vTexCoord;

uniform float uGlowIntensity;

uniform sampler2D uInTex1;
uniform sampler2D uInTex2;

mediump vec4 temp1;

void main()
{
	temp1  = texture2D(uInTex1,vTexCoord);
	temp1 += uGlowIntensity*texture2D(uInTex2,vTexCoord);

	gl_FragColor = temp1;
}
