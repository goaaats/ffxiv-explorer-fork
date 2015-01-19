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
	vec3 tangent = cross(normal, biTangent);
	vec3 mapNormal = texture2D(uNormalTex, vTexCoord.st).xyz;
	mapNormal = 2.0 * mapNormal - vec3(1.0, 1.0, 1.0);	
	mat3 TBN = mat3(tangent, biTangent, normal);
	vec3 newNormal = TBN * mapNormal;
	newNormal = normalize(newNormal);
	return newNormal;
}

void main() {
	
	//Color Maps
	vec4 mapDiffuse = texture2D(uDiffuseTex, vTexCoord.st);    
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

	//Check for Transparent	
	if (uHasNormal) {
        mapNormal = texture2D(uNormalTex, vTexCoord.st);

        // Alpha testing
        if (mapNormal.b < 0.5) {
            discard;
        }
    }
        
	vec3 L = normalize(vLightDir);
    vec3 E = normalize(vEyeVec);
    vec3 R = reflect(-L, vNormal.xyz);

	//Compute ColorSet Map
	if (uHasNormal && uHasColorSet)
	{
		table_color = texture2D(uColorSetTex, vec2(0.125, mapNormal.a));
        table_specular = texture2D(uColorSetTex, vec2(0.375, mapNormal.a));
        table_unknown1 = texture2D(uColorSetTex, vec2(0.625, mapNormal.a));
        table_unknown2 = texture2D(uColorSetTex, vec2(0.875, mapNormal.a));              
	}
    
    //Compute Normal Map
	if (uHasNormal)	
		normal.xyz = calculateNormalMap();
    
    //Diffuse
    mapDiffuse = mapDiffuse * max(dot(vNormal.xyz,L),0.0);
    mapDiffuse = vec4(table_color.xyz * mapDiffuse.xyz, 1.0);
    mapDiffuse = clamp(mapDiffuse, 0.0, 1.0);         

	//Specular
	if (uHasSpecular)
	{
		mapSpecular = texture2D(uSpecularTex, vTexCoord.st);
		float specular = pow( max(dot(R, -E), 0.0), 2.0);
		specularColor = vec4(1.0, 1.0, 1.0, 1.0) * mapSpecular.r * mapSpecular.a * specular;
		specularColor = table_specular * specularColor;
	}	

    gl_FragColor = mapDiffuse + specularColor;
}


