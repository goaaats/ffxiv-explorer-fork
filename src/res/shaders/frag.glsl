#version 110
precision highp float;

varying vec4 vPosition;
varying vec4 vNormal;
varying vec4 vTexCoord;
varying vec4 vColor;
varying vec4 vBiTangent;
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

vec3 calculateNormalMap()
{
	vec3 normal = vNormal.xyz;
	vec3 biTangent = vBiTangent.xyz;
	vec3 tangent = cross(biTangent, normal);
	vec3 mapNormal = texture2D(uNormalTex, vTexCoord.st).xyz;
	mapNormal = 2.0 * mapNormal - vec3(1.0, 1.0, 1.0);	
	mat3 TBN = mat3(tangent, biTangent, normal);
	vec3 newNormal = TBN * mapNormal;
	newNormal = normalize(newNormal);
	return newNormal;
}

void main() {
	
	//Color Maps
	vec4 mapDiffuse = vColor;		   
	vec4 mapNormal;
	vec4 mapSpecular;
	vec4 specularColor;
	
	mapDiffuse = vec4(1.0, 1.0, 1.0, 1.0);
	
	//Color Sets
	vec4 table_color;
	vec4 table_specular;
	vec4 table_unknown1;
	vec4 table_unknown2;

	//Other
	vec3 normal = vNormal.xyz;

	//Check for Transparent	
	if (uHasNormal) {
        mapNormal = texture2D(uNormalTex, vTexCoord.st);

        // Alpha testing
        if (mapNormal.b < 0.5) {
            discard;
        }
    }
        
    if (uHasDiffuse)
    	mapDiffuse = mapDiffuse * texture2D(uDiffuseTex, vTexCoord.st);
        
    //Compute Normal Map
	//if (uHasNormal)	
		normal.xyz = calculateNormalMap();
        
	vec3 L = normalize(vLightDir);
    vec3 E = normalize(vEyeVec);
    vec3 R = reflect(-L, normal);

	//Compute ColorSet Map
	if (uHasNormal && uHasColorSet)
	{
		table_color = texture2D(uColorSetTex, vec2(0.125, mapNormal.a));
        table_specular = texture2D(uColorSetTex, vec2(0.375, mapNormal.a));
        table_unknown1 = texture2D(uColorSetTex, vec2(0.625, mapNormal.a));
        table_unknown2 = texture2D(uColorSetTex, vec2(0.875, mapNormal.a));              
	}
        
    
    //Diffuse
    mapDiffuse = mapDiffuse * max(dot(normal,L),0.0);
    
    if (uHasNormal && uHasColorSet){
    	mapDiffuse = vec4(table_color.xyz * mapDiffuse.xyz, 1.0);
    	mapDiffuse = mapDiffuse + (table_unknown1 * 0.5);
    }
    
    mapDiffuse = clamp(mapDiffuse, 0.0, 1.0);    

	//Specular
	if (uHasSpecular)
	{
		mapSpecular = texture2D(uSpecularTex, vTexCoord.st);
		float specular = pow( max(dot(R, E), 0.0), 1.0);
		specular = mapSpecular.r * mapSpecular.a * specular;
		
		if (uHasNormal && uHasColorSet)
			specularColor = table_specular * specular;
		
		//specularColor = table_specular * specularColor;
	}	

    gl_FragColor = mapDiffuse + specularColor;
}


