#version 150
precision highp float;

uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjMatrix;

uniform int uNumBones;
uniform mat4 uBones[252];

attribute vec4 aPosition;
attribute vec4 aNormal;
attribute vec4 aTexCoord;
attribute vec4 aColor;
attribute vec4 aBiTangent;

attribute vec4 aBlendWeight;
attribute vec4 aBlendIndex;

varying vec4 vPosition;
varying vec4 vNormal;
varying vec4 vTexCoord;
varying vec4 vColor;

varying mat4 vTBNMatrix;	
varying vec3 vLightDir;
varying vec3 vEyeVec;

void main(void) {		

	vec4 transformedPosition = vec4(0.0);
    vec3 transformedNormal = vec3(0.0);

	vec4 curIndex = aBlendIndex;
    vec4 curWeight = aBlendWeight;
	vec4 pos = aPosition;
	//pos.z *= -1;
	//int fakeMatrix[10] = int[](0, 1, 2, 3, 4, 5 , 6, 7, 8,9);	
	//int fakeMatrix[10] = int[](1, 1, 1, 1, 1, 1 , 1, 1,1, 1);

    for (int i = 0; i < 4; i++)
    {
    	
        mat4 m44;
        
        m44 = uBones[4];      
        
        // transform the offset by bone i
        transformedPosition += m44 * pos;

        mat3 m33 = mat3(m44[0].xyz,
                        m44[1].xyz,
                        m44[2].xyz);

        // transform normal by bone i
        transformedNormal += m33 * aNormal.xyz * curWeight.x;

        // shift over the index/weight variables, this moves the index and 
        // weight for the current bone into the .x component of the index 
        // and weight variables
        curIndex = curIndex.yzwx;
        curWeight = curWeight.yzwx;
    }

	vPosition = vec4((uViewMatrix*uModelMatrix) * aPosition);
	vTexCoord = aTexCoord;	
		
	vNormal = vec4(normalize(aNormal.xyz), aNormal.a);
	vec4 biTangent = (aBiTangent * 2.0 / 255.0) - 1.0;
	biTangent = normalize(biTangent);
	vec3 tangent =  biTangent.a * cross(biTangent.xyz, transformedNormal.xyz);

	vTBNMatrix = mat4(
		vec4(tangent.x, biTangent.x, transformedNormal.x,0.0),
		vec4(tangent.y, biTangent.y, transformedNormal.y,0.0),
		vec4(tangent.z, biTangent.z, transformedNormal.z,0.0),
		vec4(0.0, 0.0, 0.0, 1.0)
        );
	
	vLightDir =  (inverse(uViewMatrix * uModelMatrix) * vec4(0.0,0.0,5.0,1.0)).xyz;
	vEyeVec = vec3((inverse(uViewMatrix * uModelMatrix) * vec4(0.0,0.0,5.0,1.0)).xyz);
	vColor = aColor;	

    gl_Position = uProjMatrix * uViewMatrix * uModelMatrix * transformedPosition;
}
