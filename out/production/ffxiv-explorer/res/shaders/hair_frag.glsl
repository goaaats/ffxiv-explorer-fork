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

uniform vec4 uHairColor;
uniform vec4 uHighlightColor;

uniform sampler2D uNormalTex;
uniform sampler2D uSpecularTex;
uniform sampler2D uColorSetTex;

uniform bool uHasNormal;
uniform bool uHasSpecular;
uniform bool uHasColorSet;

const vec3 lightPos = vec3(1.0,1.0,1.0);
const vec3 ambientColor = vec3(0.1, 0.1, 0.1);
const vec3 diffuseColor = vec3(0.7, 0.7, 0.7);
const vec3 specColor = vec3(1.0, 1.0, 1.0);

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
        mapNormal = texture2D(uNormalTex, vTexCoord.st);             
    if (uHasSpecular)
    	mapSpecular = texture2D(uSpecularTex, vTexCoord.st);		
	
    //Compute Normal Map
	if (uHasNormal)	
	{
		vec4 normal_raw = mapNormal;
        normal = normalize(((normal_raw * 2.0 - 1.0) * vTBNMatrix).xyz);
	}
        
	vec3 L = normalize(vLightDir);
    vec3 E = normalize(vEyeVec);
    vec3 R = reflect(-L, normal);	
    vec3 H = normalize(L+E);   
    
    //Diffuse            
    diffuseColor = mix(uHairColor, uHighlightColor, mapSpecular.a) * mapSpecular.r; 
    diffuseColor = clamp(diffuseColor, 0.0, 1.0);    
		
   	float lambertian = max(dot(L,normal), 0.0);
	float specular = 0.0;
 
	if(lambertian > 0.0) { 
		// this is blinn phong
		float specAngle = max(dot(H, normal), 0.0);
		specular = pow(specAngle, 128.0);
		
		//Fresnel approximation
		float F0 = 0.028;
		float exp = pow(max(0.0, 1-dot(H, E)), 5.0);
	 	float fresnel = exp+F0*(1.0-exp);
		specular *= fresnel;
	}
    
    float rimShading = smoothstep(0.6, 1.0, (1.0 - max(dot(E, normal), 0.0)));    

	gl_FragColor = vec4(ambientColor +
                      lambertian * diffuseColor.xyz +
                      rimShading * diffuseColor.xyz +
                      specular * specColor, mapNormal.a);
}


