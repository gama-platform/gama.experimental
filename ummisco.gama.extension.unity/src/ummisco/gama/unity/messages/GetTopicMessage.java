/*********************************************************************************************
 *
 * 'GamaMessage.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.unity.messages;

import java.util.Map;

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

/**
 * The Class GetTopicMessage.
 *
 * @author youcef sklab
 */

@vars({ @variable(name = GetTopicMessage.OBJECT_NAME, type = IType.STRING, doc = {
		@doc("Returns the concerned unity game object name") }),
		@variable(name = GetTopicMessage.ATTRIBUTE, type = IType.STRING, doc = {
				@doc("Returns the attribte of the message") }), })
public class GetTopicMessage extends GamaMessage {

	public final static String OBJECT_NAME = "objectName";
	public final static String ATTRIBUTE = "attribute";

	protected Object objectName;
	protected Object attribute;

	public GetTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName,
			final Object attribute, final Object content) throws GamaRuntimeException {
		super(scope, sender, receivers, content);

		setObjectName(objectName);
		setAttribute(attribute);
	}

	public GetTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName,
			final Object attribute) throws GamaRuntimeException {
		super(scope, sender, receivers, "content not set");

		setObjectName(objectName);
		setAttribute(attribute);
	}

	@getter(GetTopicMessage.OBJECT_NAME)
	public Object getObjectName() {
		return objectName;
	}

	@setter(GetTopicMessage.OBJECT_NAME)
	public void setObjectName(final Object objectName) {
		this.objectName = objectName;
	}
	

	@getter(GetTopicMessage.ATTRIBUTE)
	public Object getAttribute() {
		return attribute;
	}

	@setter(GetTopicMessage.ATTRIBUTE)
	public void setAttribute(final Object attribute) {
		this.attribute = attribute;
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
	public GetTopicMessage copy(final IScope scope) throws GamaRuntimeException {
		return new GetTopicMessage(scope, getSender(), getReceivers(), getObjectName(), getAttribute(),
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

	public void hasBeenReceived(final IScope scope) {
		// receptionTimeStamp = scope.getClock().getCycle();

	}

}
