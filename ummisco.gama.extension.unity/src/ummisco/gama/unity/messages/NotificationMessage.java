package ummisco.gama.unity.messages;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("ummisco.gama.unity.messages.NotificationMessage")
public class NotificationMessage {

	@XStreamImplicit(itemFieldName = "unread")
	public boolean unread;

	@XStreamImplicit(itemFieldName = "sender")
	public String sender;

	@XStreamImplicit(itemFieldName = "receivers")
	public String receivers;

	@XStreamImplicit(itemFieldName = "contents")
	public String contents;

	@XStreamImplicit(itemFieldName = "emissionTimeStamp")
	public String emissionTimeStamp;

	@XStreamImplicit(itemFieldName = "notificationId")
	public String notificationId;

	// public String docRoot = "ummisco.gama.unity.messages.NotificationMessage";

	public NotificationMessage() {

	}

}
