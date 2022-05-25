package ummisco.gama.unity.client.wox.serial;

import java.util.HashMap;

import ummisco.gama.unity.client.messages.UIActionMessage;

public class MappingDictionary {
	
	
	
	
	
	public static HashMap<String, String> GetUIActionMessageDic(String unityPath) {
		HashMap<String, String> UIActionMessageDic = new HashMap<String, String>();
		UIActionMessageDic.put(unityPath, UIActionMessage.class.getName());
		return UIActionMessageDic;
	}

}
