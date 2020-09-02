package com.fragmenterworks.ffxivextract.shaders;

class MinifiedShaders {

    static final String model_vert_glsl = "#version 150\n" + "precision highp float;\n"
            + "uniform mat4 uModelMatrix,uViewMatrix,uProjMatrix;"
            + "attribute vec4 aPosition,aNormal,aTexCoord,aColor,aBiTangent;"
            + "varying vec4 vPosition,vNormal,vTexCoord,vColor;" + "varying mat4 vTBNMatrix;"
            + "varying vec3 vLightDir,vEyeVec;" + "void main()" + "{"
            + "vPosition=vec4(uViewMatrix*uModelMatrix*aPosition);" + "vTexCoord=aTexCoord;"
            + "vNormal=vec4(normalize(aNormal.xyz),aNormal.w);" + "vec4 v=aBiTangent*2./255.-1.;" + "v=normalize(v);"
            + "vec3 l=v.w*cross(v.xyz,vNormal.xyz);"
            + "vTBNMatrix=mat4(vec4(l.x,v.x,vNormal.x,0.),vec4(l.y,v.y,vNormal.y,0.),vec4(l.z,v.z,vNormal.z,0.),vec4(0.,0.,0.,1.));"
            + "vLightDir=(inverse(uViewMatrix*uModelMatrix)*vec4(0.,0.,5.,1.)).xyz;"
            + "vEyeVec=vec3((inverse(uViewMatrix*uModelMatrix)*vec4(0.,0.,5.,1.)).xyz);" + "vColor=aColor;"
            + "gl_Position=uProjMatrix*uViewMatrix*uModelMatrix*aPosition;" + "}";

    static final String model_vert_boned_glsl = "#version 150\n" + "precision highp float;\n"
            + "uniform mat4 uModelMatrix,uViewMatrix,uProjMatrix;" + "uniform int uNumBones;"
            + "uniform mat4 uBones[252];" + "attribute vec4 aPosition,aNormal,aTexCoord,aColor,aBiTangent;"
            + "attribute ivec4 aBlendWeight,aBlendIndex;" + "varying vec4 vPosition,vNormal,vTexCoord,vColor;"
            + "varying mat4 vTBNMatrix;" + "varying vec3 vLightDir,vEyeVec;" + "void main()" + "{" + "vec4 v=vec4(0.);"
            + "vec3 a=vec3(0.);" + "if(uNumBones>0)" + "{" + "ivec4 u=aBlendIndex,l=aBlendWeight;" + "vec4 x=aPosition;"
            + "for(int m=0;m<4;m++)" + "{" + "mat4 i=uBones[u.x];" + "v+=i*x*(l.x/255.);"
            + "mat3 n=mat3(i[0].xyz,i[1].xyz,i[2].xyz);" + "a+=n*aNormal.xyz*(l.x/255.);" + "u=u.yzwx;" + "l=l.yzwx;"
            + "}" + "}" + "else" + " v=aPosition,a=aNormal.xyz;" + "vPosition=vec4(uViewMatrix*uModelMatrix*aPosition);"
            + "vTexCoord=aTexCoord;" + "vNormal=vec4(normalize(aNormal.xyz),aNormal.w);"
            + "vec4 u=aBiTangent*2./255.-1.;" + "u=normalize(u);" + "vec3 l=u.w*cross(u.xyz,a.xyz);"
            + "vTBNMatrix=mat4(vec4(l.x,u.x,a.x,0.),vec4(l.y,u.y,a.y,0.),vec4(l.z,u.z,a.z,0.),vec4(0.,0.,0.,1.));"
            + "vLightDir=(inverse(uViewMatrix*uModelMatrix)*vec4(0.,0.,5.,1.)).xyz;"
            + "vEyeVec=vec3((inverse(uViewMatrix*uModelMatrix)*vec4(0.,0.,5.,1.)).xyz);" + "vColor=aColor;"
            + "gl_Position=uProjMatrix*uViewMatrix*uModelMatrix*v;" + "}";

    static final String character_frag_glsl = "#version 150\n" + "precision highp float;\n"
            + "varying vec4 vPosition,vNormal,vTexCoord,vColor,vBiTangent;" + "varying mat4 vTBNMatrix;"
            + "varying vec3 vLightDir,vEyeVec;"
            + "uniform sampler2D uDiffuseTex,uNormalTex,uSpecularTex,uColorSetTex,uMaskTex;"
            + "uniform bool uHasDiffuse,uHasMask,uHasNormal,uHasSpecular,uHasColorSet,uIsGlow;"
            + "vec3 v=vec3(1.,1.,1.),u=vec3(1.,1.,1.),z=vec3(1.,1.,1.),d=vec3(1.,1.,1.);" + "void main()" + "{"
            + "vec4 v=vec4(1.,1.,1.,1.),u,x,z,r,l,y,t,p;" + "vec3 i=vNormal.xyz;" + "if(uHasNormal)"
            + "u=texture2D(uNormalTex,vTexCoord.xy);" + "if(uHasDiffuse)" + "v=texture2D(uDiffuseTex,vTexCoord.xy);"
            + "if(uHasMask)" + "z=texture2D(uMaskTex,vTexCoord.xy);" + "if(uHasSpecular)"
            + "x=texture2D(uSpecularTex,vTexCoord.xy);" + "if(uHasNormal&&uHasColorSet)"
            + "normalize(u),l=texture2D(uColorSetTex,vec2(.125,u.w)),y=texture2D(uColorSetTex,vec2(.375,u.w)),t=texture2D(uColorSetTex,vec2(.625,u.w)),p=texture2D(uColorSetTex,vec2(.875,u.w));"
            + "if(uHasNormal)" + "{" + "if(u.z<.5)" + "discard;" + "}" + "if(uHasNormal)" + "{" + "vec4 m=u;"
            + "i=normalize(((m*2.-1.)*vTBNMatrix).xyz);" + "}"
            + "vec3 m=normalize(vLightDir),w=normalize(vEyeVec),e=normalize(2.*dot(m,i)*i-m),n=normalize(m+w);"
            + "if(uIsGlow)" + "{" + "if(t.x==0.&&t.y==0.&&t.z==0.)" + "discard;" + "else"
            + " gl_FragColor=vec4(t.xyz,1.);" + "return;" + "}" + "vec3 s=v.xyz,c;" + "if(uHasDiffuse&&uHasColorSet)"
            + "s=v.xyz*l.xyz;" + "v.xyz=s;" + "if(uHasMask&&uHasNormal&&uHasColorSet)"
            + "s=mix(l.xyz,l.xyz+y.xyz,z.y),v=vec4(v.xyz*s*z.x,1.);" + "v.xyz+=t.xyz;" + "float g=max(dot(m,i),0.);"
            + "vec3 f=vec3(0.,0.,0.);" + "if(g>0.&&uHasSpecular)" + "{" + "float k=max(dot(n,i),0.);"
            + "f.x=pow(k,8.)*y.x;" + "f.y=pow(k,8.)*y.y;" + "f.z=pow(k,8.)*y.z;" + "if(uHasSpecular)" + "d=x.xyz;" + "}"
            + "else" + " if(g>0.&&uHasMask)" + "{" + "float k=max(dot(n,i),0.);"
            + "f=vec3(pow(k,8.)*z.z,pow(k,8.)*z.z,pow(k,8.)*z.z);" + "}"
            + "float k=.5,o=max(0,1-dot(w,m)),a=pow(o,5),T=k+(1-k)*a;" + "f*=T;" + "gl_FragColor=vec4(f*d+g*v.xyz,1.);"
            + "}";

    static final String default_frag_glsl = "#version 150\n" + "precision highp float;\n"
            + "varying vec4 vPosition,vNormal,vTexCoord,vColor,vBiTangent;" + "varying mat4 vTBNMatrix;"
            + "varying vec3 vLightDir,vEyeVec;" + "uniform sampler2D uDiffuseTex,uNormalTex,uSpecularTex,uColorSetTex;"
            + "uniform bool uHasDiffuse,uHasNormal,uHasSpecular,uHasColorSet;" + "void main()" + "{"
            + "vec4 u=vec4(1.,1.,1.,1.),x,v,y;" + "vec3 r=normalize(vLightDir),e=normalize(vEyeVec);"
            + "u.xyz=u.xyz*max(dot(vNormal.xyz,r),0.);" + "u=clamp(u,0.,1.);" + "gl_FragColor=vec4(u.xyz,1.);" + "}";

    static final String fbout_vert_glsl = "#version 150\n" + "precision highp float;\n"
            + "attribute vec4 aPosition;" + "varying vec2 vTexCoord;" + "void main()" + "{"
            + "vTexCoord=(aPosition*.5+.5).xy,gl_Position=vec4(aPosition.xy,0.,1.);" + "}";

    static final String fxaa_frag_glsl = "#version 150\n" + "precision highp float;\n" + "varying vec2 vTexCoord;"
            + "uniform vec2 uSize;" + "uniform sampler2D uInTex;\n" + "#define FXAA_REDUCE_MIN (1.0/128.0)\n"
            + "#define FXAA_REDUCE_MUL (1.0/8.0)\n" + "#define FXAA_SPAN_MAX 8.0\n" + "vec3 D(vec2 v,sampler2D u)" + "{"
            + "vec3 r;" + "vec2 y=vec2(1./uSize.x,1./uSize.y);"
            + "vec3 x=texture2D(u,v+vec2(-1.,-1.)*y).xyz,z=texture2D(u,v+vec2(1.,-1.)*y).xyz,m=texture2D(u,v+vec2(-1.,1.)*y).xyz,e=texture2D(u,v+vec2(1.,1.)*y).xyz,a=texture2D(u,v).xyz,X=vec3(.299,.587,.114);"
            + "float t=dot(x,X),d=dot(z,X),F=dot(m,X),n=dot(e,X),l=dot(a,X),s=min(l,min(min(t,d),min(F,n))),c=max(l,max(max(t,d),max(F,n)));"
            + "vec2 f;" + "f.x=-(t+d-(F+n));" + "f.y=t+F-(d+n);"
            + "float D=max((t+d+F+n)*(.25*FXAA_REDUCE_MUL),FXAA_REDUCE_MIN),N=1./(min(abs(f.x),abs(f.y))+D);"
            + "f=min(vec2(FXAA_SPAN_MAX,FXAA_SPAN_MAX),max(vec2(-FXAA_SPAN_MAX,-FXAA_SPAN_MAX),f*N))*y;"
            + "vec3 b=.5*(texture2D(u,v+f*(1./3.-.5)).xyz+texture2D(u,v+f*(2./3.-.5)).xyz),g=b*.5+.25*(texture2D(u,v+f*-.5).xyz+texture2D(u,v+f*.5).xyz);"
            + "float A=dot(g,X);" + "if(A<s||A>c)" + "r=b;" + "else" + " r=g;" + "return r;" + "}" + "void main()" + "{"
            + "gl_FragColor=texture2D(uInTex,vTexCoord);" + "}";

    static final String hair_frag_glsl = "#version 150\n" + "precision highp float;\n"
            + "varying vec4 vPosition,vNormal,vTexCoord,vColor,vBiTangent;" + "varying mat4 vTBNMatrix;"
            + "varying vec3 vLightDir,vEyeVec;" + "uniform vec4 uHairColor,uHighlightColor;"
            + "uniform sampler2D uNormalTex,uSpecularTex,uColorSetTex;"
            + "uniform bool uHasNormal,uHasSpecular,uHasColorSet;"
            + "const vec3 v=vec3(1.,1.,1.),u=vec3(.1,.1,.1),r=vec3(.7,.7,.7),x=vec3(1.,1.,1.);" + "void main()" + "{"
            + "vec4 v,r,y,z,X,l,m,d;" + "vec3 f=vNormal.xyz;" + "if(uHasNormal)"
            + "v=texture2D(uNormalTex,vTexCoord.xy);" + "if(uHasSpecular)" + "r=texture2D(uSpecularTex,vTexCoord.xy);"
            + "if(uHasNormal)" + "{" + "vec4 e=v;" + "f=normalize(((e*2.-1.)*vTBNMatrix).xyz);" + "}"
            + "vec3 F=normalize(vLightDir),n=normalize(vEyeVec),e=reflect(-F,f),t=normalize(F+n);"
            + "y=mix(uHairColor,uHighlightColor,r.w)*r.x;" + "y=clamp(y,0.,1.);" + "float a=max(dot(F,f),0.),g=0.;"
            + "if(a>0.)" + "{" + "float s=max(dot(t,f),0.);" + "g=pow(s,128.);"
            + "float b=.028,c=pow(max(0.,1-dot(t,n)),5.),N=c+b*(1.-c);" + "g*=N;" + "}"
            + "float b=smoothstep(.6,1.,1.-max(dot(n,f),0.));" + "gl_FragColor=vec4(u+a*y.xyz+b*y.xyz+g*x,v.w);" + "}";

    static final String iris_frag_glsl = "#version 150\n" + "precision highp float;\n"
            + "varying vec4 vPosition,vNormal,vTexCoord,vColor,vBiTangent;" + "varying mat4 vTBNMatrix;"
            + "varying vec3 vLightDir,vEyeVec;" + "uniform vec4 uEyeColor;"
            + "uniform sampler2D uNormalTex,uSpecularTex,uCatchLightTex;"
            + "uniform bool uHasNormal,uHasSpecular,uHasCatchLight;" + "void main()" + "{" + "vec4 v,u,x,r,t;"
            + "vec3 y=vNormal.xyz;" + "if(uHasNormal)" + "v=texture2D(uNormalTex,vTexCoord.xy);" + "if(uHasSpecular)"
            + "u=texture2D(uSpecularTex,vTexCoord.xy);" + "x=texture2D(uCatchLightTex,vTexCoord.xy);" + "if(uHasNormal)"
            + "{" + "vec4 e=v;" + "y=normalize(((e*2.-1.)*vTBNMatrix).xyz);" + "}"
            + "vec3 m=normalize(vLightDir),n=normalize(vEyeVec),d=reflect(-m,y),e=normalize(m+n);"
            + "t=vec4(uEyeColor.xyz*u.x,1.);" + "t=t*max(dot(y,m),0.);" + "t=clamp(t,0.,1.);"
            + "float f=.5,X=pow(max(0.,1.-dot(e,n)),5.),c=X+f*(1.-X),g=pow(max(dot(d,n),0.),u.y);" + "g*=c;"
            + "gl_FragColor=t+x*g;" + "gl_FragColor.w=1.;" + "}";

    static final String skin_frag_glsl = "#version 150\n" + "precision highp float;\n"
            + "varying vec4 vPosition,vNormal,vTexCoord,vColor,vBiTangent;" + "varying mat4 vTBNMatrix;"
            + "varying vec3 vLightDir,vEyeVec;" + "uniform sampler2D uDiffuseTex,uNormalTex,uSpecularTex,uColorSetTex;"
            + "uniform bool uHasDiffuse,uHasNormal,uHasSpecular,uHasColorSet;"
            + "const vec3 v=vec3(1.,1.,1.),u=vec3(.1,.1,.1),r=vec3(.7,.7,.7),x=vec3(1.,1.,1.);" + "void main()" + "{"
            + "vec4 v=vColor,d,r,l,e,n,o,t;" + "vec3 i=vNormal.xyz;" + "if(uHasNormal)"
            + "d=texture2D(uNormalTex,vTexCoord.xy);" + "if(uHasDiffuse)" + "v=texture2D(uDiffuseTex,vTexCoord.xy);"
            + "if(uHasSpecular)" + "r=texture2D(uSpecularTex,vTexCoord.xy);" + "if(uHasNormal)" + "{" + "if(d.z<.5)"
            + "discard;" + "}" + "if(uHasNormal)" + "{" + "vec4 m=d;" + "i=normalize(((m*2.-1.)*vTBNMatrix).xyz);" + "}"
            + "vec3 m=normalize(vLightDir),y=normalize(vEyeVec),a=reflect(m,i),f=normalize(m+y);"
            + "float s=max(dot(m,i),0.),c=0.;" + "if(s>0.)" + "{" + "float b=max(dot(f,i),0.);" + "c=pow(b,128.);"
            + "float T=.028,z=pow(max(0.,1.-dot(f,y)),5.),p=z+T*(1.-z);" + "c*=p;" + "}"
            + "float b=smoothstep(.6,1.,1.-max(dot(y,i),0.));" + "gl_FragColor=vec4(u+s*v.xyz+c*x,1.);" + "}";

    static final String bg_frag_glsl = "#version 150\n" + "precision highp float;\n"
            + "varying vec4 vPosition,vNormal,vTexCoord,vColor,vBiTangent;" + "varying mat4 vTBNMatrix;"
            + "varying vec3 vLightDir,vEyeVec;" + "uniform sampler2D uDiffuseTex,uNormalTex,uSpecularTex,uColorSetTex;"
            + "uniform bool uHasDiffuse,uHasNormal,uHasSpecular,uHasColorSet;" + "void main()" + "{"
            + "vec4 v=vec4(1.,1.,1.,1.),u,x,t;" + "if(uHasDiffuse)" + "v=texture2D(uDiffuseTex,vTexCoord.xy);"
            + "if(uHasNormal)" + "u=texture2D(uNormalTex,vTexCoord.xy);"
            + "vec3 r=normalize(vLightDir),z=normalize(vEyeVec);" + "v.xyz=v.xyz*max(dot(vNormal.xyz,r),0.);"
            + "v=clamp(v,0.,1.);" + "gl_FragColor=vec4(v.xyz,1.);" + "}";

    static final String simple_vert_glsl = "#version 150\n" + "precision highp float;\n"
            + "uniform mat4 uModelMatrix,uViewMatrix,uProjMatrix;attribute vec4 aPosition;void main(){gl_Position=uProjMatrix*uViewMatrix*uModelMatrix*aPosition;}";

    static final String simple_frag_glsl = "#version 150\n" + "precision highp float;\n"
            + "void main(){gl_FragColor=vec4(1.,0.,0.,1.);}";

    static final String blend_frag_glsl = "#version 150\n" + "precision highp float;\n" + "varying vec2 vTexCoord;"
            + "uniform float uGlowIntensity;" + "uniform sampler2D uInTex1,uInTex2;" + "mediump vec4 u;" + "void main()"
            + "{" + "u=texture2D(uInTex1,vTexCoord),u+=uGlowIntensity*texture2D(uInTex2,vTexCoord),gl_FragColor=u;"
            + "}";

    static final String blur_frag_glsl = "#version 150\n" + "precision highp float;\n" + "varying vec2 vTexCoord;"
            + "uniform sampler2D uInTex;" + "uniform vec2 uTexelSize;" + "uniform int uBlurDirection,uBlurRadius;"
            + "void main()" + "{" + "vec4 x=vec4(0.);" + "if(uBlurDirection==0)" + "{"
            + "float u=uTexelSize.x*uBlurRadius;" + "x+=texture2D(uInTex,vec2(vTexCoord.x-4.*u,vTexCoord.y))*.05;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x-3.*u,vTexCoord.y))*.09;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x-2.*u,vTexCoord.y))*.12;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x-u,vTexCoord.y))*.15;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x,vTexCoord.y))*.16;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x+u,vTexCoord.y))*.15;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x+2.*u,vTexCoord.y))*.12;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x+3.*u,vTexCoord.y))*.09;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x+4.*u,vTexCoord.y))*.05;" + "}" + "else" + "{"
            + "float u=uTexelSize.y*uBlurRadius;" + "x+=texture2D(uInTex,vec2(vTexCoord.x,vTexCoord.y-4.*u))*.05;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x,vTexCoord.y-3.*u))*.09;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x,vTexCoord.y-2.*u))*.12;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x,vTexCoord.y-u))*.15;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x,vTexCoord.y))*.16;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x,vTexCoord.y+u))*.15;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x,vTexCoord.y+2.*u))*.12;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x,vTexCoord.y+3.*u))*.09;"
            + "x+=texture2D(uInTex,vec2(vTexCoord.x,vTexCoord.y+4.*u))*.05;" + "}" + "gl_FragColor=clamp(x,0.,1.);"
            + "}";

}
