package ummisco.gama.unity.messages;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IMap;
import msi.gaml.types.Types;
import ummisco.gama.unity.skills.IUnitySkill;

@XStreamAlias("ummisco.gama.unity.messages.littosimMessage")
public class littosimMessage {

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_UNREAD)
	public boolean unread;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_SENDER)
	public String sender;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_RECEIVERS)
	public String receivers;

	// @XStreamImplicit(itemFieldName = IUnitySkill.CONTENTS)
	// public Object contents;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_TYPE)
	public int type;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_NAME)
	public String name;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_X)
	public double x;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_Y)
	public double y;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_Z)
	public double z;

	@XStreamImplicit(itemFieldName = IUnitySkill.MSG_EMISSION_TIMESTAMP)
	public String emissionTimeStamp;

	public littosimMessage() {

	}

	public Map<String, String> getMapMsg() {
		//GamaMap<String, String> msg = GamaMapFactory.create(Types.STRING, Types.LIST);
		IMap<String, String> msg = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);		
		msg.put(IUnitySkill.MSG_UNREAD, "false");
		msg.put(IUnitySkill.MSG_SENDER, sender);
		msg.put(IUnitySkill.MSG_RECEIVERS, receivers);
		msg.put(IUnitySkill.MSG_TYPE, Integer.toString(type));
		msg.put(IUnitySkill.MSG_NAME, name);
		msg.put(IUnitySkill.MSG_X, Double.toString(x));
		msg.put(IUnitySkill.MSG_Y, Double.toString(y));
		msg.put(IUnitySkill.MSG_Z, Double.toString(z));
		msg.put(IUnitySkill.MSG_EMISSION_TIMESTAMP, emissionTimeStamp);
		return msg;
	}

}
