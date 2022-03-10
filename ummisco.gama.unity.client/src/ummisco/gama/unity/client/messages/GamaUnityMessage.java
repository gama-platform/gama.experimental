/*********************************************************************************************
 *
 * 'GamaMessage.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.unity.client.messages;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.util.StringUtils;
import msi.gama.extensions.messaging.GamaMessage;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import ummisco.gama.unity.client.skills.IUnitySkill;

/**
 * The Class GamaUnityMessage.
 *
 * @author youcef sklab
 */

@vars ({ @variable (
		name = GamaUnityMessage.SENDER,
		type = IType.NONE,
		doc = { @doc ("Returns the sender that has sent this message") }),
		@variable (
				name = GamaUnityMessage.UNREAD,
				type = IType.BOOL,
				init = IKeyword.TRUE,
				doc = { @doc ("Returns whether this message is unread or not") }),
		@variable (
				name = GamaUnityMessage.RECEPTION_TIMESTAMP,
				type = IType.INT,
				doc = { @doc ("Returns the reception time stamp of this message (I.e. at what cycle it has been received)") }),
		@variable (
				name = GamaUnityMessage.EMISSION_TIMESTAMP,
				type = IType.INT,
				doc = { @doc ("Returns the emission time stamp of this message (I.e. at what cycle it has been emitted)") }),

		@variable (
				name = GamaUnityMessage.ACTION,
				type = IType.STRING,
				doc = { @doc ("Returns the emission time stamp of this message (I.e. at what cycle it has been emitted)") }),
		@variable (
				name = GamaUnityMessage.OBJECT,
				type = IType.STRING,
				doc = { @doc ("Returns the emission time stamp of this message (I.e. at what cycle it has been emitted)") }),
		@variable (
				name = GamaUnityMessage.ATTRIBUTE,
				type = IType.MAP,
				doc = { @doc ("Returns the emission time stamp of this message (I.e. at what cycle it has been emitted)") }),
		@variable (
				name = GamaUnityMessage.TOPIC,
				type = IType.STRING,
				doc = { @doc ("Returns the topic of this message (I.e. topic Speed to change a unity GameObject speed.)") }) })
public class GamaUnityMessage extends GamaMessage {

	public final static String CONTENTS = IUnitySkill.MSG_CONTENTS;
	public final static String UNREAD = IUnitySkill.MSG_UNREAD;
	public final static String EMISSION_TIMESTAMP = IUnitySkill.MSG_EMISSION_TIMESTAMP;
	public final static String RECEPTION_TIMESTAMP = IUnitySkill.MSG_RECEPTION_TIMESTAMP;
	public final static String SENDER = IUnitySkill.MSG_SENDER;
	public final static String RECEIVERS = IUnitySkill.MSG_RECEIVERS;

	public final static String ACTION = IUnitySkill.MSG_UNITY_ACTION;
	public final static String OBJECT = IUnitySkill.MSG_UNITY_OBJECT;
	public final static String ATTRIBUTE = IUnitySkill.MSG_UNITY_ATTRIBUTE;
	public final static String TOPIC = IUnitySkill.MSG_UNITY_TOPIC;

	protected Object unityAction;
	protected Object unityObject;
	protected Object unityAttribute;
	protected Object unityTopic;

	public GamaUnityMessage(final IScope scope, final Object sender, final Object receivers, final Object unityAction,
			final Object unityObject, final Object unityAttribute, final Object topic, final Object content)
			throws GamaRuntimeException {
		super(scope, sender, receivers, content);
		setUnityAction(unityAction);
		setUnityObject(unityObject);
		setUnityAttribute(unityAttribute);
		setUnityTopic(unityTopic);
	}

	@getter (GamaUnityMessage.ACTION)
	public Object getUnityAction() {
		return unityAction;
	}

	@setter (GamaUnityMessage.ACTION)
	public void setUnityAction(final Object unityAction) {
		this.unityAction = unityAction;
	}

	@getter (GamaUnityMessage.OBJECT)
	public Object getUnityObject() {
		return unityObject;
	}

	@setter (GamaUnityMessage.OBJECT)
	public void setUnityObject(final Object unityObject) {
		this.unityObject = unityObject;
	}

	@getter (GamaUnityMessage.ATTRIBUTE)
	public Object getUnityAttribute() {
		return unityAttribute;
	}

	@setter (GamaUnityMessage.ATTRIBUTE)
	public void setUnityAttribute(final Object unityAttribute) {

		this.unityAttribute = unityAttribute;
	}

	@getter (GamaUnityMessage.TOPIC)
	public Object getUnityTopic() {
		return unityTopic;
	}

	@setter (GamaUnityMessage.TOPIC)
	public void setUnityTopic(final Object unityTopic) {
		this.unityTopic = unityTopic;
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return StringUtils.toGaml(contents, includingBuiltIn);
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "message[sender: " + getSender() + "; Action: " + getUnityAction() + "; content: " + getContents(scope)
				+ "; content" + "]";
	}

	@Override
	public GamaUnityMessage copy(final IScope scope) throws GamaRuntimeException {
		return new GamaUnityMessage(scope, getSender(), getReceivers(), getUnityAction(), getUnityObject(),
				getUnityAttribute(), getUnityTopic(), getContents(scope));
	}

	/**
	 * Method getType()
	 *
	 * @see msi.gama.common.interfaces.ITyped#getType()
	 */
	@Override
	public IType<?> getGamlType() {
		return Types.get(IType.MESSAGE);
	}

	@Override
	public void hasBeenReceived(final IScope scope) {
		// receptionTimeStamp = scope.getClock().getCycle();

	}

}
