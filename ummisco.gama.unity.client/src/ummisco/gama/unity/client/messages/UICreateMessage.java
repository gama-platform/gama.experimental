package ummisco.gama.unity.client.messages;

import java.util.HashMap;
import java.util.Map;

import msi.gama.runtime.IScope;

public class UICreateMessage {
	
	public String topic;
	public String uiType;
	public String parent;
	public String uiId;
	public float x;
	public float y;
	public float z;
	public float height;
	public float width;
	public String label;
	
	public int redColor;
	public int greenColor;
	public int blueColor;
	public int alphaColor;
	
	public String content_text;
	
	public Map<String, String> option_action = new HashMap<String, String>();
	
	public int size;
	public int state;
	
	public UICreateMessage(final IScope scope, String topic, String uiType, String parent, String uiId, float x, float y, float z,
			float height, float width, String label, 
			int redColor, int greenColor, int blueColor, int alphaColor,
			String content_text,
			Map<String, String> option_action, int size, int state) {
		super();
		this.topic = topic;
		this.uiType = uiType;
		this.parent = parent;
		this.uiId = uiId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.height = height;
		this.width = width;
		this.label = label;
		this.redColor = redColor;
		this.greenColor = greenColor;
		this.blueColor = blueColor;
		this.alphaColor = alphaColor;
		this.content_text = content_text;
		this.option_action = option_action;
		this.size = size;
		this.state = state;
	}

	public UICreateMessage(final IScope scope, String topic, String uiType, String uiId, float x, float y, float z, String label,
			Object actionCode) {
		super();
		this.topic = topic;
		this.uiType = uiType;
		this.uiId = uiId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.label = label;
		
		this.redColor = 0;
		this.greenColor = 0;
		this.blueColor = 0;
		this.alphaColor = 0;
		
		this.parent = null;
		this.height = 0;
		this.width = 0;
		this.content_text = null;
		this.option_action = null;
		this.size = 1;
		this.state = 1;
	}
		
	
}
