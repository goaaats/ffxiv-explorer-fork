package ca.fraggergames.ffxivextract.helpers;

public class ShaderIdHelper {

	private static SparseArray<String> idToName = null;
	
	private static void initArray(){
		
		idToName = new SparseArray<String>();				

		idToName.put(0x115306be,"g_SamplerDiffuse");
		idToName.put(0x2b99e025,"g_SamplerSpecular");
		idToName.put(0x0c5ec1f1,"g_SamplerNormal");
		idToName.put(0x2005679f,"g_SamplerTable");
		idToName.put(0x8a4e82b6,"g_SamplerMask");
		idToName.put(0x0237cb94,"g_SamplerDecal");
		
		//Character Shader
		idToName.put(0xebbb29bd,"g_SamplerGBuffer");
		idToName.put(0x565f8fd8,"g_SamplerIndex");
		idToName.put(0x23d0f850,"g_SamplerLightDiffuse");
		idToName.put(0x6c19aca4,"g_SamplerLightSpecular");
		idToName.put(0x87f6474d,"g_SamplerReflection");
		idToName.put(0x29156a85,"g_SamplerTileDiffuse");
		idToName.put(0x32667bd7,"g_SamplerOcclusion");
		
		//BG Shader
		idToName.put(0x1e6fef9c,"g_SamplerColorMap0");
		idToName.put(0x6968df0a,"g_SamplerColorMap1");
		idToName.put(0x9f467267,"g_SamplerDither");
		idToName.put(0xaab4d9e9,"g_SamplerNormalMap0");
		idToName.put(0xddb3e97f,"g_SamplerNormalMap1");
		idToName.put(0x1bbc2f12,"g_SamplerSpecularMap0");
		idToName.put(0x6cbb1f84,"g_SamplerSpecularMap1");
		
		//Hair Shader
		idToName.put(0x9f467267,"g_SamplerDither");
		idToName.put(0xebbb29bd,"g_SamplerGBuffer");
		idToName.put(0x23d0f850,"g_SamplerLightDiffuse");
		idToName.put(0x6c19aca4,"g_SamplerLightSpecular");
				
		idToName.put(0x32667bd7,"g_SamplerOcclusion");
		idToName.put(0x87f6474d,"g_SamplerReflection");		
		
		//Iris Shader
		idToName.put(0xfea0f3d2,"g_SamplerCatchlight");
		
	}
	public static String getName(int id)
	{
		if (idToName == null)
			initArray();
		
		return idToName.get(id, null);
	}
}
