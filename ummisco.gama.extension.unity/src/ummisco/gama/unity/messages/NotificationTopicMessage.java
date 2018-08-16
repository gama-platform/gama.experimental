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
 * The Class CreateTopicMessage.
 *
 * @author youcef sklab
 */

@vars({ @variable(name = NotificationTopicMessage.OBJECT_NAME, type = IType.STRING, doc = {
		@doc("Returns the concerned unity game object name") }),
		@variable(name = NotificationTopicMessage.FIELD_TYPE, type = IType.STRING, doc = {
				@doc("Returns the observed fielsd's type (a field or a property") }),
		@variable(name = NotificationTopicMessage.FIELD_NAME, type = IType.STRING, doc = {
				@doc("Returns the observed field's name") }),
		@variable(name = NotificationTopicMessage.FIELD_VALUE, type = IType.STRING, doc = {
				@doc("Returns the field value, to be compared with") }),
		@variable(name = NotificationTopicMessage.FIELD_OPERATOR, type = IType.STRING, doc = {
				@doc("Returns the comparaison operator") }),
		})

public class NotificationTopicMessage extends GamaMessage {

	public final static String OBJECT_NAME = "objectName";
	public final static String FIELD_TYPE = "type";
	public final static String FIELD_NAME = "fieldName";
	public final static String FIELD_VALUE = "value";
	public final static String FIELD_OPERATOR = "operator";

	protected Object objectName;
	protected Object type;
	protected Object fieldName;
	protected Object value;
	protected Object operator;

	public NotificationTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName,
			final Object type, final Object fieldName, final Object value, final Object operator, final Object content) throws GamaRuntimeException {
		super(scope, sender, receivers, content);

		setObjectName(objectName);
		setType(type);
		setFieldName(fieldName);
		setValue(value);
		setOperator(operator);
	}

	public NotificationTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName, 
			final Object type, final Object fieldName, final Object value, final Object operator) throws GamaRuntimeException {
		super(scope, sender, receivers, "content not set");

		setObjectName(objectName);
		setType(type);
		setFieldName(fieldName);
		setValue(value);
		setOperator(operator);
	}

	@getter(NotificationTopicMessage.OBJECT_NAME)
	public Object getObjectName() {
		return objectName;
	}

	@setter(NotificationTopicMessage.OBJECT_NAME)
	public void setObjectName(final Object objectName) {
		this.objectName = objectName;
	}
	

	@getter(NotificationTopicMessage.FIELD_TYPE)
	public Object getType() {
		return type;
	}

	@setter(NotificationTopicMessage.FIELD_TYPE)
	public void setType(final Object type) {
		this.type = type;
	}
	
	
	
	
	@getter(NotificationTopicMessage.FIELD_NAME)
	public Object getFieldName() {
		return fieldName;
	}

	@setter(NotificationTopicMessage.FIELD_NAME)
	public void setFieldName(final Object fieldName) {
		this.fieldName = fieldName;
	}
	
	@getter(NotificationTopicMessage.FIELD_VALUE)
	public Object getValue() {
		return value;
	}

	@setter(NotificationTopicMessage.FIELD_VALUE)
	public void setValue(final Object value) {
		this.value = value;
	}
	
	@getter(NotificationTopicMessage.FIELD_OPERATOR)
	public Object getOperator() {
		return operator;
	}

	@setter(NotificationTopicMessage.FIELD_OPERATOR)
	public void setOperator(final Object operator) {
		this.operator = operator;
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
	public NotificationTopicMessage copy(final IScope scope) throws GamaRuntimeException {
		return new NotificationTopicMessage(scope, getSender(), getReceivers(), getObjectName(), getType(), getFieldName(), getValue(), getOperator(),
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
