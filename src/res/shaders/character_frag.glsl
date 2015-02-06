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
uniform sampler2D uMaskTex;

uniform bool uHasDiffuse;
uniform bool uHasMask;
uniform bool uHasNormal;
uniform bool uHasSpecular;
uniform bool uHasColorSet;

vec3 lightPos = vec3(1.0,1.0,1.0);
vec3 ambientColor = vec3(0.0, 0.0, 0.0);
vec3 diffuseColor = vec3(0.3, 0.3, 0.3 );
vec3 specColor = vec3(1.0, 1.0, 1.0);

void main() {
	
	//Color Maps
	vec4 mapDiffuse = vColor;		
	vec4 mapMask;
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
    if (uHasMask)
    	mapMask = texture2D(uMaskTex, vTexCoord.st);
    if (uHasSpecular)
    	mapSpecular = texture2D(uSpecularTex, vTexCoord.st);
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
    vec3 H = normalize(L+E);       
    
    //Color
    vec3 color = table_color.xyz + table_specular.xyz;
    color = mix(color, table_unknown1.xyz, table_unknown1.a); 
    
    //Diffuse         
    if (uHasDiffuse && uHasNormal && uHasColorSet){            
    	mapDiffuse = vec4(color * mapDiffuse.xyz,1.0);		
    } 
	else if (uHasMask && uHasNormal && uHasColorSet){	
    	mapDiffuse = vec4(color * mapMask.x, 1.0); //Red Channel Masks Color    	    	
    }    
                  
    mapDiffuse = clamp(mapDiffuse, 0.0, 1.0);    
		
	//Specular
	specColor = table_specular.xyz;
    float lambertian = max(dot(L,normal), 0.0);
	float specular = 0.0; 
	if(lambertian > 0.0) {
		// this is blinn phong
		float specAngle = max(dot(H, normal), 0.0);
		
		if (uHasDiffuse){
			specular = pow(specAngle, mapSpecular.z*255.0);
			specular = mapSpecular.g * mapSpecular.b * specular;
		}
		else if (uHasMask)
			specular = mapSpecular.y;
		
		//Fresnel approximation
		float F0 = 0.028;
		float exp = pow(max(0.0, 1.0-dot(H, E)), 5.0);
	 	float fresnel = exp+F0*(1.0-exp);
	//	specular *= fresnel;				
	}
    
    float rimShading = smoothstep(0.8, 1.0, (1.0 - max(dot(E, normal), 0.0)));    

	vec3 calcedSpecular = specular * specColor;
		
	mapDiffuse *= 0.9;
		
	gl_FragColor = vec4(ambientColor +                      
                      lambertian * mapDiffuse.xyz +
                      calcedSpecular, 1.0);
}

