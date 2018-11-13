package ummisco.gama.unity.messages;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gaml.types.Types;

@XStreamAlias("ummisco.gama.unity.messages.littosimMessage")
public class littosimMessage {

	@XStreamImplicit(itemFieldName = "unread")
	public boolean unread;

	@XStreamImplicit(itemFieldName = "sender")
	public String sender;

	@XStreamImplicit(itemFieldName = "receivers")
	public String receivers;
	
	//@XStreamImplicit(itemFieldName = "contents")
	//public Object contents;
	
	@XStreamImplicit(itemFieldName = "type")
	public int type;
	
	@XStreamImplicit(itemFieldName = "name")
	public String name;
	
	@XStreamImplicit(itemFieldName = "x")
	public double x;
	
	@XStreamImplicit(itemFieldName = "y")
	public double y;
	
	@XStreamImplicit(itemFieldName = "z")
	public double z;

	@XStreamImplicit(itemFieldName = "emissionTimeStamp")
	public String emissionTimeStamp;

	public littosimMessage() {

	}
	
	public Map<String, String> getMapMsg(){
		GamaMap<String, String> msg = GamaMapFactory.create(Types.STRING, Types.LIST);
		msg.put("unread", "false");
		msg.put("sender", sender);
		msg.put("receivers", receivers);
		msg.put("type", Integer.toString(type));
		msg.put("name", name);
		msg.put("x", Double.toString(x));
		msg.put("y", Double.toString(y));
		msg.put("z", Double.toString(z));
		msg.put("emissionTimeStamp", emissionTimeStamp);
		return msg;
	}

}
