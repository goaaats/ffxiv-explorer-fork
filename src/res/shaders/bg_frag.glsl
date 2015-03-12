#version 150
precision highp float;

varying vec4 vPosition;
varying vec4 vNormal;
varying vec4 vTexCoord;
varying vec4 vColor;
varying vec4 vBiTangent;

varying mat4 vTBNMatrix;
varying vec3 vLightDir;
varying vec3 vEyeVec;

uniform sampler2D uDiffuseTex;
uniform sampler2D uNormalTex;
uniform sampler2D uSpecularTex;
uniform sampler2D uColorSetTex;

uniform bool uHasDiffuse;
uniform bool uHasNormal;
uniform bool uHasSpecular;
uniform bool uHasColorSet;

void main() {
	
	//Color Maps
	vec4 mapDiffuse = vec4(1.0,1.0,1.0,1.0);		   
	vec4 mapNormal;
	vec4 mapSpecular;
	vec4 specularColor;
  
  	//Texture Maps
	/*if (uHasNormal) 
        mapNormal = texture2D(uNormalTex, vTexCoord.st);
	if (uHasDiffuse)
    	mapDiffuse = texture2D(uDiffuseTex, vTexCoord.st);
    if (uHasSpecular)
    	mapSpecular = texture2D(uSpecularTex, vTexCoord.st);
  */
   if (uHasDiffuse)
  	mapDiffuse = texture2D(uDiffuseTex, vTexCoord.st);  
  
	vec3 L = normalize(vLightDir);
    vec3 E = normalize(vEyeVec);
    //vec4 R = reflect(-L, vNormal);	
            
    //Diffuse
    mapDiffuse.xyz = mapDiffuse.xyz * max(dot(vNormal.xyz,L),0.0);
    mapDiffuse = clamp(mapDiffuse, 0.0, 1.0);    

	//Specular
	//float specular = pow( max(dot(R, E), 0.0), 1.0);

    gl_FragColor = vec4(mapDiffuse.xyz,1.0);
}


