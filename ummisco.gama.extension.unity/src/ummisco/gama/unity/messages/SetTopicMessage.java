/*********************************************************************************************
 *
 * 'GamaMessage.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.unity.messages;

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
import ummisco.gama.unity.skills.IUnitySkill;

/**
 * The Class SetTopicMessage.
 *
 * @author youcef sklab
 */

@vars ({ @variable (
		name = SetTopicMessage.OBJECT_NAME,
		type = IType.STRING,
		doc = { @doc ("Returns the concerned unity game object name") }),
		@variable (
				name = SetTopicMessage.ATTRIBUTES_LIST,
				type = IType.MAP,
				doc = { @doc ("Returns the attribtes list of the message") }), })
public class SetTopicMessage extends GamaMessage {

	public final static String OBJECT_NAME = IUnitySkill.MSG_OBJECT_NAME;
	public final static String ATTRIBUTES_LIST = IUnitySkill.MSG_ATTRIBUTES;

	protected Object objectName;
	protected Object attributes;

	public SetTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName,
			final Object attributes, final Object content) throws GamaRuntimeException {
		super(scope, sender, receivers, content);

		setObjectName(objectName);
		setAttributesList(attributes);
	}

	public SetTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName,
			final Object attributes) throws GamaRuntimeException {
		super(scope, sender, receivers, "content not set");

		setObjectName(objectName);
		setAttributesList(attributes);
	}

	@getter (SetTopicMessage.OBJECT_NAME)
	public Object getObjectName() {
		return objectName;
	}

	@setter (SetTopicMessage.OBJECT_NAME)
	public void setObjectName(final Object objectName) {
		this.objectName = objectName;
	}

	@getter (SetTopicMessage.ATTRIBUTES_LIST)
	public Object getAttributesList() {
		return attributes;
	}

	@setter (SetTopicMessage.ATTRIBUTES_LIST)
	public void setAttributesList(final Object attributesList) {
		this.attributes = attributesList;
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return StringUtils.toGaml(contents, includingBuiltIn);
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "message[sender: " + getSender() + "; Object Name: " + getObjectName() + "; content: "
				+ getContents(scope) + "; content" + "]";
	}

	@Override
	public SetTopicMessage copy(final IScope scope) throws GamaRuntimeException {
		return new SetTopicMessage(scope, getSender(), getReceivers(), getObjectName(), getAttributesList(),
				getContents(scope));
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
