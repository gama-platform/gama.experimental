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
 * The Class CreateTopicMessage.
 *
 * @author youcef sklab
 */

@vars({ @variable(name = CreateTopicMessage.OBJECT_NAME, type = IType.STRING, doc = {
		@doc("Returns the concerned unity game object name") }),
		@variable(name = CreateTopicMessage.TYPE, type = IType.STRING, doc = {
				@doc("Returns the type of the object to create") }),
		@variable(name = CreateTopicMessage.COLOR, type = IType.COLOR, doc = {
				@doc("Returns the color of the object to create") }),
		@variable(name = CreateTopicMessage.POSITION, type = IType.POINT, doc = {
				@doc("Returns the position of the object to create") }), })
public class CreateTopicMessage extends GamaMessage {

	public final static String OBJECT_NAME = IUnitySkill.MSG_OBJECT_NAME;
	public final static String TYPE = IUnitySkill.MSG_TYPE;
	public final static String COLOR = IUnitySkill.MSG_COLOR;
	public final static String POSITION = IUnitySkill.MSG_POSITION;

	protected Object objectName;
	protected Object type;
	protected Object color;
	protected Object position;

	public CreateTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName,
			final Object type, final Object color, final Object position, final Object content)
			throws GamaRuntimeException {
		super(scope, sender, receivers, content);

		setObjectName(objectName);
		setType(type);
		setColor(color);
		setPosition(position);
	}

	public CreateTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName,
			final Object type, final Object color, final Object position) throws GamaRuntimeException {
		super(scope, sender, receivers, "content not set");

		setObjectName(objectName);
		setType(type);
		setColor(color);
		setPosition(position);
	}

	@getter(CreateTopicMessage.OBJECT_NAME)
	public Object getObjectName() {
		return objectName;
	}

	@setter(CreateTopicMessage.OBJECT_NAME)
	public void setObjectName(final Object objectName) {
		this.objectName = objectName;
	}

	@getter(CreateTopicMessage.TYPE)
	public Object getType() {
		return type;
	}

	@setter(CreateTopicMessage.TYPE)
	public void setType(final Object type) {
		this.type = type;
	}

	@getter(CreateTopicMessage.COLOR)
	public Object getColor() {
		return color;
	}

	@setter(CreateTopicMessage.COLOR)
	public void setColor(final Object color) {
		this.color = color;
	}

	@getter(CreateTopicMessage.POSITION)
	public Object getPosition() {
		return position;
	}

	@setter(CreateTopicMessage.POSITION)
	public void setPosition(final Object position) {
		this.position = position;
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
	public CreateTopicMessage copy(final IScope scope) throws GamaRuntimeException {
		return new CreateTopicMessage(scope, getSender(), getReceivers(), getObjectName(), getType(), getColor(),
				getPosition(), getContents(scope));
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
