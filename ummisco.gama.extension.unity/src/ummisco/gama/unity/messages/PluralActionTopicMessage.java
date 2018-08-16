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
 * The Class PluralActionTopicMessage.
 *
 * @author youcef sklab
 */

@vars({ @variable(name = PluralActionTopicMessage.OBJECT_NAME, type = IType.STRING, doc = {
		@doc("Returns the concerned unity game object name") }),
		@variable(name = PluralActionTopicMessage.METHOD_NAME, type = IType.STRING, doc = {
			@doc("Returns the unity game object method name") }),
		@variable(name = PluralActionTopicMessage.ATTRIBUTES_LIST, type = IType.MAP, doc = {
				@doc("Returns the attribtes list of the message") }), })
public class PluralActionTopicMessage extends GamaMessage {

	public final static String OBJECT_NAME = "objectName";
	public final static String METHOD_NAME = "methodName";
	public final static String ATTRIBUTES_LIST = "attributes";

	protected Object objectName;
	protected Object methodName;
	protected Object attributes;

	public PluralActionTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName, final Object methodName,
			final Object attributes, final Object content) throws GamaRuntimeException {
		super(scope, sender, receivers, content);

		setObjectName(objectName);
		setMethodName(methodName);
		setAttributesList(attributes);
	}

	public PluralActionTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName, final Object methodName,
			final Object attributes) throws GamaRuntimeException {
		super(scope, sender, receivers, "content not set");

		setObjectName(objectName);
		setMethodName(methodName);
		setAttributesList(attributes);
	}

	@getter(PluralActionTopicMessage.OBJECT_NAME)
	public Object getObjectName() {
		return objectName;
	}

	@setter(PluralActionTopicMessage.OBJECT_NAME)
	public void setObjectName(final Object objectName) {
		this.objectName = objectName;
	}
	
	@getter(PluralActionTopicMessage.METHOD_NAME)
	public Object getMethodName() {
		return methodName;
	}

	@setter(PluralActionTopicMessage.METHOD_NAME)
	public void setMethodName(final Object methodName) {
		this.methodName = methodName;
	}

	@getter(PluralActionTopicMessage.ATTRIBUTES_LIST)
	public Object getAttributesList() {
		return attributes;
	}

	@setter(PluralActionTopicMessage.ATTRIBUTES_LIST)
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
	public PluralActionTopicMessage copy(final IScope scope) throws GamaRuntimeException {
		return new PluralActionTopicMessage(scope, getSender(), getReceivers(), getObjectName(), getMethodName(), (Map<?, ?>) getAttributesList(),
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