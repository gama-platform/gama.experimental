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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

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
 * The Class PositionTopicMessage.
 *
 * @author youcef sklab
 */

@vars({ @variable(name = PropertyTopicMessage.OBJECT_NAME, type = IType.STRING, doc = {
		@doc("Returns the concerned unity game object name") }),
		@variable(name = PropertyTopicMessage.PROPERTY_NAME, type = IType.STRING, doc = {
		@doc("Returns the property name the message") }),
		@variable(name = PropertyTopicMessage.VALUE, type = IType.NONE, doc = {
		@doc("Returns the property value the message") }) })
public class PropertyTopicMessage extends GamaMessage {

	public final static String OBJECT_NAME = IUnitySkill.MSG_OBJECT_NAME;
	public final static String PROPERTY_NAME = IUnitySkill.MSG_PROPERTY_NAME;
	public final static String VALUE_TYPE = IUnitySkill.MSG_VALUE_TYPE;
	public final static String VALUE = IUnitySkill.MSG_VALUE;

	protected Object objectName;
	protected Object propertyName;
	protected Object valueType;
	protected Object value;

	public PropertyTopicMessage(final IScope scope, final Object sender, final Object receivers,
			final Object objectName, final Object propertyName, final Object value, final Object content)
			throws GamaRuntimeException {
		super(scope, sender, receivers, content);

		setObjectName(objectName);
		setPropertyName(propertyName);
		setValueType(value.getClass().getSimpleName());
		setValue(value);

	}

	public PropertyTopicMessage(final IScope scope, final Object sender, final Object receivers,
			final Object objectName, final Object propertyName, final Object value) throws GamaRuntimeException {
		super(scope, sender, receivers, "content not set");

		setObjectName(objectName);
		setPropertyName(propertyName);
		setValueType(value.getClass().getSimpleName());
		setValue(value);
	}

	@getter(PropertyTopicMessage.OBJECT_NAME)
	public Object getObjectName() {
		return objectName;
	}

	@setter(PropertyTopicMessage.OBJECT_NAME)
	public void setObjectName(final Object objectName) {
		this.objectName = objectName;
	}

	@getter(PropertyTopicMessage.PROPERTY_NAME)
	public Object getPropertyName() {
		return propertyName;
	}

	@setter(PropertyTopicMessage.PROPERTY_NAME)
	public void setPropertyName(final Object propertyName) {
		this.propertyName = propertyName;
	}

	@getter(PropertyTopicMessage.VALUE_TYPE)
	public Object getValueType() {
		return valueType;
	}

	@setter(PropertyTopicMessage.VALUE_TYPE)
	public void setValueType(final Object valueType) {
		this.valueType = valueType;
	}

	@getter(PropertyTopicMessage.VALUE)
	public Object getValue() {
		return value;
	}

	@setter(PropertyTopicMessage.VALUE)
	public void setValue(final Object value) {
		this.value = value;
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
	public PropertyTopicMessage copy(final IScope scope) throws GamaRuntimeException {
		return new PropertyTopicMessage(scope, getSender(), getReceivers(), getObjectName(), getPropertyName(),
				getValue(), getContents(scope));
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
