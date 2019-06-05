package ummisco.gama.unity.messages;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import ummisco.gama.unity.skills.IUnitySkill;

@XStreamAlias(IUnitySkill.CLASS_NOTIFICATION_MESSAGE)
public class NotificationMessage {

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_UNREAD)
	public boolean unread;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_SENDER)
	public String sender;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_RECEIVERS)
	public String receivers;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_CONTENTS)
	public String contents;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_EMISSION_TIMESTAMP)
	public String emissionTimeStamp;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_NOTIFICATION_ID)
	public String notificationId;

	public NotificationMessage() {

	}

}
