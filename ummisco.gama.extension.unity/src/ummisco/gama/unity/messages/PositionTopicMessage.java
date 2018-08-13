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
 * The Class PositionTopicMessage.
 *
 * @author youcef sklab
 */

@vars({ @variable(name = PositionTopicMessage.OBJECT_NAME, type = IType.STRING, doc = {
		@doc("Returns the concerned unity game object name") }),
		@variable(name = PositionTopicMessage.POSITION, type = IType.MAP, doc = {
				@doc("Returns the attribtes list of the message") }), })
public class PositionTopicMessage extends GamaMessage {

	public final static String OBJECT_NAME = "objectName";
	public final static String POSITION = "position";

	protected Object objectName;
	protected Object position;

	public PositionTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName, final Object position,
			final Object content) throws GamaRuntimeException {
		super(scope, sender, receivers, content);

		setObjectName(objectName);
		setPosition(position);
	}

	public PositionTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName, 
			final Object position) throws GamaRuntimeException {
		super(scope, sender, receivers, "content not set");

		setObjectName(objectName);
		setPosition(position);
	}

	@getter(PositionTopicMessage.OBJECT_NAME)
	public Object getObjectName() {
		return objectName;
	}

	@setter(PositionTopicMessage.OBJECT_NAME)
	public void setObjectName(final Object objectName) {
		this.objectName = objectName;
	}
	

	@getter(PositionTopicMessage.POSITION)
	public Object getPosition() {
		return position;
	}

	@setter(PositionTopicMessage.POSITION)
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
	public PositionTopicMessage copy(final IScope scope) throws GamaRuntimeException {
		return new PositionTopicMessage(scope, getSender(), getReceivers(), getObjectName(), (Map<?, ?>) getPosition(),
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
