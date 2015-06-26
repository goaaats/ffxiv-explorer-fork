#version 150
precision highp float;

varying vec2 vTexCoord;

uniform sampler2D uInTex;

uniform vec2 uTexelSize;
uniform int uBlurDirection;
uniform int uBlurAmount;
uniform float uBlurScale;
uniform float uBlurStrength;

float Gaussian (float x, float deviation)
{
    return (1.0 / sqrt(2.0 * 3.141592 * deviation)) * exp(-((x * x) / (2.0 * deviation)));  
}

void main() {	

	 // Locals
    float halfBlur = float(uBlurAmount) * 0.5;
    vec4 colour = vec4(0.0);
    vec4 texColour = vec4(0.0);
    
    // Gaussian deviation
    float deviation = halfBlur * 0.35;
    deviation *= deviation;
    float strength = 1.0 - uBlurStrength;
    
    if ( uBlurDirection == 0 )
    {
        // Horizontal blur
        for (int i = 0; i < 10; ++i)
        {
            if ( i >= uBlurAmount )
                break;
            
            float offset = float(i) - halfBlur;
            texColour = texture2D(uInTex, vTexCoord + vec2(offset * uTexelSize.x * uBlurScale, 0.0)) * Gaussian(offset * strength, deviation);
            colour += texColour;
        }
    }
    else
    {
        // Vertical blur
        for (int i = 0; i < 10; ++i)
        {
            if ( i >= uBlurAmount )
                break;
            
            float offset = float(i) - halfBlur;
            texColour = texture2D(uInTex, vTexCoord + vec2(0.0, offset * uTexelSize.y * uBlurScale)) * Gaussian(offset * strength, deviation);
            colour += texColour;
        }
    }
    
    // Apply colour
    gl_FragColor = clamp(colour, 0.0, 1.0);
    
}


