package ummisco.gama.unity.client.messages;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import ummisco.gama.unity.client.skills.IUnitySkill;

@XStreamAlias(IUnitySkill.CLASS_REPLAY_MESSAGE)
public class ReplayMessage {

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_UNREAD)
	public boolean unread;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_SENDER)
	public String sender;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_RECEIVERS)
	public String receivers;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_CONTENTS)
	public String contents;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_FIELD_NAME)
	public String fieldName;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_FIELD_VALUE)
	public String fieldValue;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_EMISSION_TIMESTAMP)
	public String emissionTimeStamp;

	public ReplayMessage() {

	}

}
