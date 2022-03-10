package ummisco.gama.unity.client.messages;

import java.util.HashMap;
import java.util.Map;

import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import ummisco.gama.unity.client.skills.IUnitySkill;

public class UIActionMessage {
	
	public String topic;
	public long messageTime;
	public int messageNumber;
	public String elementId;
	public Object actionCode;
	public String content;
	
	public UIActionMessage()
	{
		SetDefault();			
	}
	
	public UIActionMessage(String _elementId, int _actionCode)
	{
		this ();
		SetElementId(_elementId);
		SetActionCode(_actionCode);
		SetContent(" ");
	}

	public UIActionMessage(String _elementId, float _actionCode)
	{
		this();
		SetElementId(_elementId);
		SetActionCode(_actionCode);
		SetContent(" ");
	}

	public UIActionMessage(String _elementId, int _actionCode, String _topic) 
	{
		this(_elementId, _actionCode);
		SetTopic(_topic);
	}

	public UIActionMessage(String _elementId, float _actionCode, String _topic)
	{
		this(_elementId, _actionCode);
		SetTopic(_topic);
	}

	public UIActionMessage(String _elementId, int _actionCode, String _topic, String _content)
	{
		this(_elementId, _actionCode, _topic);
		SetContent(_content);
	}

	private void SetDefault()
	{
		messageNumber++;
		messageTime = System.currentTimeMillis();
		topic = IUnitySkill.TOPIC_MAIN;
	}

	public void SetElementId(String _elementId)
	{
		this.elementId = _elementId;
	}

	public void SetActionCode(int _actionCode)
	{
		this.actionCode = _actionCode;
	}

	public void SetActionCode(float _actionCode)
	{
		this.actionCode = _actionCode;
	}

	public void SetContent(String _content)
	{
		this.content = _content;
	}

	public void SetTopic(String _topic)
	{
		this.topic = _topic;
	}

	public GamaMap<String, String> ToHashMap() {
		GamaMap<String, String> MapMsg = (GamaMap) GamaMapFactory.create() ;

		MapMsg.put("topic", topic);
		MapMsg.put("messageTime", String.valueOf(messageTime));
		MapMsg.put("messageNumber",String.valueOf(messageNumber));
		MapMsg.put("elementId",elementId);
		MapMsg.put("actionCode", String.valueOf(actionCode));
		MapMsg.put("content",content);
		
		return MapMsg;
	}

}
