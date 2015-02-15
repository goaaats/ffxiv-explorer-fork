#version 150
precision highp float;

varying vec2 vTexCoord;

uniform sampler2D uInTex;

uniform vec2 uResolution;
uniform float uRadius;
uniform vec2 uDir;

void main() {	

	vec2 fragCoord = vTexCoord;

   //this will be our RGBA sum
    vec4 sum = vec4(0.0);

    //the amount to blur, i.e. how far off center to sample from 
    //1.0 -> blur by one pixel
    //2.0 -> blur by two pixels, efragCoord.
    float blurX = uRadius/uResolution.x; 
	float blurY = uRadius/uResolution.y;

    //the uDirection of our blur
    //(1.0, 0.0) -> x-axis blur
    //(0.0, 1.0) -> y-axis blur
    float hstep = uDir.x;
    float vstep = uDir.y;

    //apply blurring, using a 9-tap filter with predefined gaussian weights

    sum += texture2D(uInTex, vec2(fragCoord.x - 4.0*blurX*hstep, fragCoord.y - 4.0*blurY*vstep)) * 0.0162162162;
    sum += texture2D(uInTex, vec2(fragCoord.x - 3.0*blurX*hstep, fragCoord.y - 3.0*blurY*vstep)) * 0.0540540541;
    sum += texture2D(uInTex, vec2(fragCoord.x - 2.0*blurX*hstep, fragCoord.y - 2.0*blurY*vstep)) * 0.1216216216;
    sum += texture2D(uInTex, vec2(fragCoord.x - 1.0*blurX*hstep, fragCoord.y - 1.0*blurY*vstep)) * 0.1945945946;

    sum += texture2D(uInTex, vec2(fragCoord.x, fragCoord.y)) * 0.2270270270;

    sum += texture2D(uInTex, vec2(fragCoord.x + 1.0*blurX*hstep, fragCoord.y + 1.0*blurY*vstep)) * 0.1945945946;
    sum += texture2D(uInTex, vec2(fragCoord.x + 2.0*blurX*hstep, fragCoord.y + 2.0*blurY*vstep)) * 0.1216216216;
    sum += texture2D(uInTex, vec2(fragCoord.x + 3.0*blurX*hstep, fragCoord.y + 3.0*blurY*vstep)) * 0.0540540541;
    sum += texture2D(uInTex, vec2(fragCoord.x + 4.0*blurX*hstep, fragCoord.y + 4.0*blurY*vstep)) * 0.0162162162;


	gl_FragColor = vec4(sum.rgb, 1.0);
}


