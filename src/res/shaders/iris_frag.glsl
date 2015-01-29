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
uniform bool uHasCatchLightTex;

void main() {
	
	//Color Maps			  
	vec4 mapNormal;
	vec4 mapSpecular;
	vec4 mapCatchLight;
	vec4 specularColor;	

	//Other
	vec3 normal = vNormal.xyz;
	
	//Texture Maps
	if (uHasNormal) 
        mapNormal = texture2D(uNormalTex, vTexCoord.st);        
    if (uHasSpecular)
    	mapSpecular = texture2D(uSpecularTex, vTexCoord.st);
	if (uHasCatchLightTex)	
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
    
    //Diffuse    
    if (uHasNormal){
    	mapCatchLight = vec4(mapCatchLight.xyz, 1.0);
    }
        
    mapCatchLight = mapCatchLight * max(dot(normal,L),0.0);
    mapCatchLight = clamp(mapCatchLight, 0.0, 1.0);    

	//Specular
	float specular = 1.0;
	if (uHasSpecular)
	{		
		float specular = pow( max(dot(R, E), 0.0), 1.0);
		specular = mapSpecular.r * mapSpecular.a * specular;						
	}	

	//Final color
	gl_FragColor = mapCatchLight * specular;
}


