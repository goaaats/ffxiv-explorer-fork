#version 150
precision highp float;

varying vec2 vTexCoord;

uniform sampler2D uInTex;

uniform vec2 uTexelSize;
uniform int uBlurDirection;
uniform int uBlurRadius;

void main() {	
	
	vec4 sum = vec4(0.0);
 
 	if (uBlurDirection == 0){
 		float blurSize = uTexelSize.x * uBlurRadius; //1 texel * num texel radius
		sum += texture2D(uInTex, vec2(vTexCoord.x - 4.0*blurSize, vTexCoord.y)) * 0.05;
		sum += texture2D(uInTex, vec2(vTexCoord.x - 3.0*blurSize, vTexCoord.y)) * 0.09;
		sum += texture2D(uInTex, vec2(vTexCoord.x - 2.0*blurSize, vTexCoord.y)) * 0.12;
		sum += texture2D(uInTex, vec2(vTexCoord.x - blurSize, vTexCoord.y)) * 0.15;
		sum += texture2D(uInTex, vec2(vTexCoord.x, vTexCoord.y)) * 0.16;
		sum += texture2D(uInTex, vec2(vTexCoord.x + blurSize, vTexCoord.y)) * 0.15;
		sum += texture2D(uInTex, vec2(vTexCoord.x + 2.0*blurSize, vTexCoord.y)) * 0.12;
		sum += texture2D(uInTex, vec2(vTexCoord.x + 3.0*blurSize, vTexCoord.y)) * 0.09;
		sum += texture2D(uInTex, vec2(vTexCoord.x + 4.0*blurSize, vTexCoord.y)) * 0.05;
	}
	else{
		float blurSize = uTexelSize.y * uBlurRadius; //1 texel * num texel radius
		sum += texture2D(uInTex, vec2(vTexCoord.x, vTexCoord.y - 4.0*blurSize)) * 0.05;
	    sum += texture2D(uInTex, vec2(vTexCoord.x, vTexCoord.y - 3.0*blurSize)) * 0.09;
	    sum += texture2D(uInTex, vec2(vTexCoord.x, vTexCoord.y - 2.0*blurSize)) * 0.12;
	    sum += texture2D(uInTex, vec2(vTexCoord.x, vTexCoord.y - blurSize)) * 0.15;
	    sum += texture2D(uInTex, vec2(vTexCoord.x, vTexCoord.y)) * 0.16;
	    sum += texture2D(uInTex, vec2(vTexCoord.x, vTexCoord.y + blurSize)) * 0.15;
	    sum += texture2D(uInTex, vec2(vTexCoord.x, vTexCoord.y + 2.0*blurSize)) * 0.12;
	    sum += texture2D(uInTex, vec2(vTexCoord.x, vTexCoord.y + 3.0*blurSize)) * 0.09;
	    sum += texture2D(uInTex, vec2(vTexCoord.x, vTexCoord.y + 4.0*blurSize)) * 0.05;
	}
	 
    gl_FragColor = clamp(sum, 0.0, 1.0);    
    
}


