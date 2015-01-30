#version 110
precision highp float;

varying vec4 vPosition;
varying vec4 vNormal;
varying vec4 vTexCoord;
varying vec4 vColor;
varying vec4 vBiTangent;

varying mat4 vTBNMatrix;
varying vec3 vLightDir;
varying vec3 vEyeVec;

uniform vec4 uEyeColor;

uniform sampler2D uNormalTex;
uniform sampler2D uSpecularTex;
uniform sampler2D uCatchLightTex;

uniform bool uHasNormal;
uniform bool uHasSpecular;
uniform bool uHasCatchLight;

void main() {
	
	//Color Maps			  
	vec4 mapNormal;
	vec4 mapSpecular;
	vec4 mapCatchLight;
	vec4 specularColor;	

	//Other
	vec4 finalColor;
	vec3 normal = vNormal.xyz;
	
	//Texture Maps
	if (uHasNormal) 
        mapNormal = texture2D(uNormalTex, vTexCoord.st);        
    if (uHasSpecular)
    	mapSpecular = texture2D(uSpecularTex, vTexCoord.st);
	//if (uHasCatchLight)	
		mapCatchLight = texture2D(uCatchLightTex, vTexCoord.st);     
	
    //Compute Normal Map
	if (uHasNormal)	
	{
		vec4 normal_raw = mapNormal;
        normal = normalize(((normal_raw * 2.0 - 1.0) * vTBNMatrix).xyz);
	}
        
	vec3 L = normalize(vLightDir);
    vec3 E = normalize(vEyeVec);
    vec3 R = reflect(-L, normal);	
        
    finalColor = vec4((uEyeColor.xyz * mapSpecular.x), 1.0);
    finalColor = finalColor * max(dot(normal,L),0.0);        
    finalColor = clamp(finalColor, 0.0, 1.0); 

	//Specular
	float specular = pow(max(dot(R, E), 0.0), mapSpecular.y);							

	//Final color
	gl_FragColor = finalColor + (mapCatchLight * specular);
	gl_FragColor.a = 1.0;
}


