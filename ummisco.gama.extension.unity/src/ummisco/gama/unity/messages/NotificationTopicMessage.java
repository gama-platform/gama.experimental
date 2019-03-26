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
import ummisco.gama.unity.skills.IUnitySkill;

/**
 * The Class NotificationTopicMessage.
 *
 * @author youcef sklab
 */

@vars({ @variable(name = NotificationTopicMessage.NOTIFICATION_ID, type = IType.STRING, doc = {
		@doc("Returns the notificaionId") }),
		@variable(name = NotificationTopicMessage.OBJECT_NAME, type = IType.STRING, doc = {
				@doc("Returns the concerned unity game object name") }),
		@variable(name = NotificationTopicMessage.FIELD_TYPE, type = IType.STRING, doc = {
				@doc("Returns the observed fielsd's type (a field or a property") }),
		@variable(name = NotificationTopicMessage.FIELD_NAME, type = IType.STRING, doc = {
				@doc("Returns the observed field's name") }),
		@variable(name = NotificationTopicMessage.FIELD_VALUE, type = IType.STRING, doc = {
				@doc("Returns the field value, to be compared with") }),
		@variable(name = NotificationTopicMessage.FIELD_OPERATOR, type = IType.STRING, doc = {
				@doc("Returns the comparaison operator") }), })
public class NotificationTopicMessage extends GamaMessage {

	public final static String NOTIFICATION_ID = IUnitySkill.MSG_NOTIFICATION_ID;
	public final static String OBJECT_NAME = IUnitySkill.MSG_OBJECT_NAME;
	public final static String FIELD_TYPE = IUnitySkill.MSG_FIELD_TYPE;
	public final static String FIELD_NAME = IUnitySkill.MSG_FIELD_NAME;
	public final static String FIELD_VALUE = IUnitySkill.MSG_FIELD_VALUE;
	public final static String FIELD_OPERATOR = IUnitySkill.MSG_FIELD_OPERATOR;

	protected Object notificationId;
	protected Object objectName;
	protected Object fieldType;
	protected Object fieldName;
	protected Object fieldValue;
	protected Object fieldOperator;

	public NotificationTopicMessage(final IScope scope, final Object sender, final Object receivers,
			final Object notificationId, final Object objectName, final Object fieldType, final Object fieldName,
			final Object fieldValue, final Object fieldOperator, final Object content) throws GamaRuntimeException {
		super(scope, sender, receivers, content);

		setNotificationId(notificationId);
		setObjectName(objectName);
		setFieldType(fieldType);
		setFieldName(fieldName);
		setFieldValue(fieldValue);
		setFieldOperator(fieldOperator);
	}

	public NotificationTopicMessage(final IScope scope, final Object sender, final Object receivers,
			final Object notificationId, final Object objectName, final Object fieldType, final Object fieldName,
			final Object fieldValue, final Object fieldOperator) throws GamaRuntimeException {
		super(scope, sender, receivers, "content not set");

		setNotificationId(notificationId);
		setObjectName(objectName);
		setFieldType(fieldType);
		setFieldName(fieldName);
		setFieldValue(fieldValue);
		setFieldOperator(fieldOperator);
	}

	@getter(NotificationTopicMessage.NOTIFICATION_ID)
	public Object getNotificationId() {
		return notificationId;
	}

	@setter(NotificationTopicMessage.NOTIFICATION_ID)
	public void setNotificationId(final Object notificationId) {
		this.notificationId = notificationId;
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
	public Object getFieldType() {
		return fieldType;
	}

	@setter(NotificationTopicMessage.FIELD_TYPE)
	public void setFieldType(final Object fieldType) {
		this.fieldType = fieldType;
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
	public Object getFieldValue() {
		return fieldValue;
	}

	@setter(NotificationTopicMessage.FIELD_VALUE)
	public void setFieldValue(final Object fieldValue) {
		this.fieldValue = fieldValue;
	}

	@getter(NotificationTopicMessage.FIELD_OPERATOR)
	public Object getFieldOperator() {
		return fieldOperator;
	}

	@setter(NotificationTopicMessage.FIELD_OPERATOR)
	public void setFieldOperator(final Object fieldOperator) {
		this.fieldOperator = fieldOperator;
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
		return new NotificationTopicMessage(scope, getSender(), getReceivers(), getNotificationId(), getObjectName(),
				getFieldType(), getFieldName(), getFieldValue(), getFieldOperator(), getContents(scope));
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
