#version 150
precision highp float;

varying vec2 vTexCoord;

uniform sampler2D uInTex1;
uniform sampler2D uInTex2;

mediump vec4 temp1;

void main()
{
	float u_glowIntensity = 2.0f;
	temp1  = 1.0*texture2D(uInTex1,vTexCoord);
	temp1 += u_glowIntensity*texture2D(uInTex2,vTexCoord);

	gl_FragColor = temp1;
}
