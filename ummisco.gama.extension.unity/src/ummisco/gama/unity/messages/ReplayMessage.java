package ummisco.gama.unity.messages;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("ummisco.gama.unity.messages.ReplayMessage")
public class ReplayMessage {

	@XStreamImplicit(itemFieldName = "unread")
	public boolean unread;

	@XStreamImplicit(itemFieldName = "sender")
	public String sender;

	@XStreamImplicit(itemFieldName = "receivers")
	public String receivers;

	@XStreamImplicit(itemFieldName = "contents")
	public String contents;

	
	@XStreamImplicit(itemFieldName = "fieldName")
	public String fieldName;
	
	@XStreamImplicit(itemFieldName = "fieldValue")
	public String fieldValue;
	
	@XStreamImplicit(itemFieldName = "emissionTimeStamp")
	public String emissionTimeStamp;


	// public String docRoot = "ummisco.gama.unity.messages.NotificationMessage";

	public ReplayMessage() {

	}

}
