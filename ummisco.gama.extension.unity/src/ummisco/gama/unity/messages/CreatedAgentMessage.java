package ummisco.gama.unity.messages;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("ummisco.gama.unity.messages.CreatedAgentMessage")
//@XStreamAlias("msi.gama.extensions.messaging.GamaMessage")
public class CreatedAgentMessage {

	@XStreamImplicit(itemFieldName = "unread")
	public boolean unread;

	@XStreamImplicit(itemFieldName = "sender")
	public String sender;

	@XStreamImplicit(itemFieldName = "receivers")
	public String receivers;
	
	//@XStreamImplicit(itemFieldName = "contents")
	//public Object contents;
	
	@XStreamImplicit(itemFieldName = "name")
	public String name;
	
	@XStreamImplicit(itemFieldName = "type")
	public int type;
	
	@XStreamImplicit(itemFieldName = "x")
	public double x;
	
	@XStreamImplicit(itemFieldName = "y")
	public double y;
	
	@XStreamImplicit(itemFieldName = "z")
	public double z;

	@XStreamImplicit(itemFieldName = "emissionTimeStamp")
	public String emissionTimeStamp;

	public CreatedAgentMessage() {

	}

}
