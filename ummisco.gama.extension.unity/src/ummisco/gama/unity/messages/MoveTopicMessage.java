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
 * The Class MoveTopicMessage.
 *
 * @author youcef sklab
 */

@vars ({ @variable (
		name = MoveTopicMessage.OBJECT_NAME,
		type = IType.STRING,
		doc = { @doc ("Returns the concerned unity game object name") }),
		@variable (
				name = MoveTopicMessage.POSITION,
				type = IType.MAP,
				doc = { @doc ("Returns the attribtes list of the message") }),
		@variable (
				name = MoveTopicMessage.SPEED,
				type = IType.FLOAT,
				doc = { @doc ("Returns the object mouvment speed") }),
		@variable (
				name = MoveTopicMessage.SMOOTH_MOVE,
				type = IType.BOOL,
				doc = { @doc ("Return if the object moving is free or stric. whether the object will be moved with force (so, it may not stop at the target position) or not.") }), })
public class MoveTopicMessage extends GamaMessage {

	public final static String OBJECT_NAME = IUnitySkill.MSG_OBJECT_NAME;
	public final static String POSITION = IUnitySkill.MSG_POSITION;
	public final static String SPEED = IUnitySkill.MSG_SPEED;
	public final static String SMOOTH_MOVE = IUnitySkill.MSG_SMOOTH_MOVE;

	protected Object objectName;
	protected Object position;
	protected Object speed;
	protected Object smoothMove;

	public MoveTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName,
			final Object position, final Object speed, final Object smoothMove, final Object content)
			throws GamaRuntimeException {
		super(scope, sender, receivers, content);

		setObjectName(objectName);
		setPosition(position);
		setSpeed(speed);
		setSmoothMove(smoothMove);
	}

	public MoveTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName,
			final Object position, final Object speed, final Object smoothMove) throws GamaRuntimeException {
		super(scope, sender, receivers, "content not set");

		setObjectName(objectName);
		setPosition(position);
		setSpeed(speed);
		setSmoothMove(smoothMove);
	}

	@getter (MoveTopicMessage.OBJECT_NAME)
	public Object getObjectName() {
		return objectName;
	}

	@setter (MoveTopicMessage.OBJECT_NAME)
	public void setObjectName(final Object objectName) {
		this.objectName = objectName;
	}

	@getter (MoveTopicMessage.POSITION)
	public Object getPosition() {
		return position;
	}

	@setter (MoveTopicMessage.POSITION)
	public void setPosition(final Object position) {
		this.position = position;
	}

	@getter (MoveTopicMessage.SPEED)
	public Object getSpeed() {
		return speed;
	}

	@setter (MoveTopicMessage.SPEED)
	public void setSpeed(final Object speed) {
		this.speed = speed;
	}

	@getter (MoveTopicMessage.SMOOTH_MOVE)
	public Object getSmoothMove() {
		return smoothMove;
	}

	@setter (MoveTopicMessage.SMOOTH_MOVE)
	public void setSmoothMove(final Object smoothMove) {
		this.smoothMove = smoothMove;
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
	public MoveTopicMessage copy(final IScope scope) throws GamaRuntimeException {
		return new MoveTopicMessage(scope, getSender(), getReceivers(), getObjectName(), getPosition(), getSpeed(),
				getSmoothMove(), getContents(scope));
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
