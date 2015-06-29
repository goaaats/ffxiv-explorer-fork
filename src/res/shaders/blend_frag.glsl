#version 150
precision highp float;

varying vec2 vTexCoord;

uniform sampler2D uInTex1;
uniform sampler2D uInTex2;

void main() {	

	vec4 src = texture2D(uInTex1, vTexCoord);
    vec4 dst = texture2D(uInTex2, vTexCoord);
       
  //  gl_FragColor = min(tex1 + tex2, 1.0);
    //gl_FragColor = tex1;
    
    gl_FragColor = clamp((src + dst) - (src * dst), 0.0, 1.0);
	gl_FragColor.w = 1.0;
}


