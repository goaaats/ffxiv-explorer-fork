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

uniform vec4 uHairColor;
uniform vec4 uHighlightColor;

uniform sampler2D uNormalTex;
uniform sampler2D uSpecularTex;
uniform sampler2D uColorSetTex;

uniform bool uHasNormal;
uniform bool uHasSpecular;
uniform bool uHasColorSet;

void main() {
	
	//Color Maps			  
	vec4 mapNormal;
	vec4 mapSpecular;
	vec4 diffuseColor;
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
	{	
        mapNormal = texture2D(uNormalTex, vTexCoord.st);
        if (mapNormal.a < 0.2)
        discard;
    }                
    if (uHasSpecular)
    	mapSpecular = texture2D(uSpecularTex, vTexCoord.st);
	if (uHasNormal && uHasColorSet)
	{
		table_color = texture2D(uColorSetTex, vec2(0.125, mapNormal.a));
        table_specular = texture2D(uColorSetTex, vec2(0.375, mapNormal.a));
        table_unknown1 = texture2D(uColorSetTex, vec2(0.625, mapNormal.a));
        table_unknown2 = texture2D(uColorSetTex, vec2(0.875, mapNormal.a));              
	}
	
	//Check specular map for highlights
	if (mapSpecular.a >= 0.5)
		diffuseColor = uHighlightColor;
	else
		diffuseColor = uHairColor;	
	
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
    if (uHasNormal && uHasColorSet){
    	diffuseColor = vec4(table_color.xyz * diffuseColor.xyz, 1.0);
    	diffuseColor = diffuseColor + (table_unknown1 * 0.5);
    }
        
    diffuseColor = diffuseColor * max(dot(normal,L),0.0);
    diffuseColor = clamp(diffuseColor, 0.0, 1.0);    

	//Specular
	float specular = 1.0;
	if (uHasSpecular)
	{		
		float specular = pow( max(dot(R, E), 0.0), 1.0);
		specular = mapSpecular.r * mapSpecular.a * specular;						
	}	

	//Final color
	if (uHasNormal && uHasColorSet)
		gl_FragColor = vec4(diffuseColor.xyz, 1.0) + (specularColor * specular);
	else
    	gl_FragColor = vec4(diffuseColor.xyz, 1.0) * specular;
}


