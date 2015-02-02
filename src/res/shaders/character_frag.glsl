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
	if (uHasNormal && uHasColorSet)
	{
		table_color = texture2D(uColorSetTex, vec2(0.125, mapNormal.a));
        table_specular = texture2D(uColorSetTex, vec2(0.375, mapNormal.a));
        table_unknown1 = texture2D(uColorSetTex, vec2(0.625, mapNormal.a));
        table_unknown2 = texture2D(uColorSetTex, vec2(0.875, mapNormal.a));              
	}

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
    vec3 R = normalize(2 * dot(L, normal) * normal - L);	
    vec3 H = normalize(L+E);   
    
    float rimShading = 1.0 - max(dot(E, normal), 0.0); 
    
    if (uHasNormal && uHasColorSet){
    	mapDiffuse = vec4(table_color.xyz * mapDiffuse.xyz, 1.0);
    	mapDiffuse = vec4(table_unknown1.xyz + mapDiffuse.xyz, 1.0);
    }
    
    //Diffuse    
    mapDiffuse.xyz = mapDiffuse.xyz * max(dot(normal,L),0.0);
    mapDiffuse = clamp(mapDiffuse, 0.0, 1.0);    

	//Specular
	if (uHasSpecular)
	{
		//Fresnel approximation
		float F0 = 0.7;
		float exp = pow(max(0, 1-dot(H, E)), 5);
	 	float fresnel = exp+F0*(1.0-exp);
	
		//Specular calculation and map
		mapSpecular = texture2D(uSpecularTex, vTexCoord.st);
		float specular = pow( max(dot(R, E), 0.0), table_specular.a);
		specular = mapSpecular.g * mapSpecular.b * specular;
		specular *= fresnel;
		
		if (uHasNormal && uHasColorSet)
			specularColor = table_specular  * mapSpecular.r * mapSpecular.a *  specular;
		else
			specularColor = vec4(1.0,1.0,1.0,1.0) * mapSpecular.r * mapSpecular.a * specular;
	}	

    gl_FragColor = clamp(mapDiffuse + specularColor,0.0,1.0);
    gl_FragColor.a = 1.0;
}


