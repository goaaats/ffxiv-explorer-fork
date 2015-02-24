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
uniform sampler2D uMaskTex;

uniform bool uHasDiffuse;
uniform bool uHasMask;
uniform bool uHasNormal;
uniform bool uHasSpecular;
uniform bool uHasColorSet;

vec3 lightPos = vec3(1.0,1.0,1.0);
vec3 ambientColor = vec3(0.0, 0.0, 0.0);
vec3 diffuseColor = vec3(255.0/255.0, 255.0/255.0, 255.0/255.0);
vec3 specColor = vec3(1.0, 1.0, 1.0);

void main() {
	
	//Color Maps
	vec4 mapDiffuse = vec4(1.0,1.0,1.0,1.0);		
	vec4 mapNormal;
	vec4 mapSpecular;
	vec4 mapMask;
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
		normalize(mapNormal);
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
    vec3 R = normalize(2.0 * dot(L, normal) * normal - L);	
    vec3 H = normalize(L+E);       
    
    //Color
    vec3 color = table_color.xyz;       	
    if (uHasDiffuse && uHasColorSet){    
   		color = table_color.xyz;
   		color += table_unknown1.xyz; 
    	//mapDiffuse = vec4(mapDiffuse.xyz * color,1.0);    
    } 
	if (uHasMask && uHasNormal && uHasColorSet){		
		color = mix(table_color.xyz, table_color.xyz+table_specular.xyz, mapMask.g);
    	mapDiffuse = vec4(mapDiffuse.xyz * color * mapMask.x, 1.0);
    }
    
    if (uHasDiffuse && uHasColorSet)
    {
    	color = (table_color+table_specular).xyz;		
    	mapDiffuse = vec4(mapDiffuse.xyz * color, 1.0);
    }
    
   // if (table_unknown1.a > 0.1)
    	//mapDiffuse.rgb = table_unknown1.rgb;
    
    //mapDiffuse.r = 0.0;
    //mapDiffuse.g = 0.0;
    //mapDiffuse.b = 0.0;
    
    //Diffuse           	
    float lambertian = max(dot(L,normal), 0.0);
    float invertedLambertian = 1.0 -max(dot(L,normal), 0.0);
    
    if (uHasMask)
    	invertedLambertian *= mapMask.b;
    else
    	invertedLambertian = 0.0;
        
	vec3 specular = vec3(0.0,0.0,0.0);
 
	if(lambertian > 0.0 && uHasSpecular) {
		// this is blinn phong
		specColor = mapSpecular.rgb * table_specular.rgb;
		float specAngle = max(dot(H, normal), 0.0);			
		
		if (uHasSpecular)
		{
			specular = vec3(pow(specAngle, mapSpecular.r*64.0),pow(specAngle, mapSpecular.g*74.0),pow(specAngle, mapSpecular.b*32.0));						
		}
		
		//Fresnel approximation
		float F0 = 0.028;
		float exp = pow(max(0.0, 1.0-dot(H, E)), 5.0);
	 	float fresnel = exp+F0*(1.0-exp);
		//specular *= fresnel;				
	}      
	
	float rimShading = 0.0;
	if (uHasMask)
    	rimShading = mapMask.b * (1.0 - max(dot(E, normal), 0.0));    

	gl_FragColor = vec4(
                      /*rimShading * vec3(1.0,1.0,1.0) +*/
                      specColor * specular +                     
                      diffuseColor * lambertian * mapDiffuse.xyz, 1.0);

}


