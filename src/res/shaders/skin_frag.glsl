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
	vec4 mapDiffuse = vColor;		   
	vec4 mapNormal;
	vec4 mapSpecular;
	vec4 specularColor;	
	
	//Color Sets
	vec4 table_color;
	vec4 table_specular;
	vec4 table_unknown1;
	vec4 table_unknown2;

	//Other
	vec3 normal = vNormal.xyz;

	//Texture Maps
	if (uHasNormal) 
        mapNormal = texture2D(uNormalTex, vTexCoord.st);
	if (uHasDiffuse)
    	mapDiffuse = texture2D(uDiffuseTex, vTexCoord.st);

	//Check for Transparent	
	if (uHasNormal) {
        if (mapNormal.b < 0.5)
            discard;        
    }  
        
    //Compute Normal Map
	if (uHasNormal)	
	{
		vec4 normal_raw = mapNormal;
        normal = normalize(((normal_raw * 2.0 - 1.0) * vTBNMatrix).xyz);
	}
        
	vec3 L = normalize(vLightDir);
    vec3 E = normalize(vEyeVec);
    vec3 R = reflect(L, normal);	
    vec3 H = normalize(L+E); 
      
    if (uHasNormal && uHasColorSet){
    	mapDiffuse = vec4(table_color.xyz * mapDiffuse.xyz, 1.0);
    	mapDiffuse = mapDiffuse + (table_unknown1 * 0.5);
    }
    
    float rimShading = 1.0 - max(dot(E, normal), 0.0);     
    
    //Diffuse
    mapDiffuse.xyz = mapDiffuse.xyz * max(dot(normal,L),0.0);
    mapDiffuse = clamp(mapDiffuse, 0.0, 1.0);    

	//Specular
	float specular = 1.0;
	if (uHasSpecular)
	{
		mapSpecular = texture2D(uSpecularTex, vTexCoord.st);
		specular = pow( max(dot(R, -E), 0.0), mapSpecular.g);
		specular = specular;
		
		//Fresnel approximation
		float F0 = 0.028;
		float exp = pow(max(0, 1-dot(H, E)), 5);
	 	float fresnel = exp+F0*(1.0-exp);
	 	
	 	specular *= fresnel;
		
		specularColor = vec4(1.0,1.0,1.0,1.0) * specular;
	}	

    gl_FragColor = mapDiffuse + specularColor;
}


