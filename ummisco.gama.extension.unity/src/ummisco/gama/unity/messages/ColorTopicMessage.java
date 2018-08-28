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
 * The Class SetTopicMessage.
 *
 * @author youcef sklab
 */

@vars({ @variable(name = ColorTopicMessage.OBJECT_NAME, type = IType.STRING, doc = {
		@doc("Returns the concerned unity game object name") }),
		@variable(name = ColorTopicMessage.RED, type = IType.INT, doc = {
				@doc("Returns the red component") }), 
		@variable(name = ColorTopicMessage.GREEN, type = IType.INT, doc = {
				@doc("Returns the green component") }), 
		@variable(name = ColorTopicMessage.BLUE, type = IType.INT, doc = {
				@doc("Returns the blue component") }), })
public class ColorTopicMessage extends GamaMessage {

	public final static String OBJECT_NAME = "objectName";
	public final static String RED = "red";
	public final static String GREEN = "green";
	public final static String BLUE = "blue";

	protected Object objectName;
	protected Object red;
	protected Object green;
	protected Object blue;

	public ColorTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName,
			final Object red, final Object green, final Object blue, final Object content) throws GamaRuntimeException {
		super(scope, sender, receivers, content);

		setObjectName(objectName);
		setRed(red);
		setGreen(green);
		setBlue(blue);
	}

	public ColorTopicMessage(final IScope scope, final Object sender, final Object receivers, final Object objectName,
			final Object red, final Object green, final Object blue) throws GamaRuntimeException {
		super(scope, sender, receivers, "content not set");

		setObjectName(objectName);
		setRed(red);
		setGreen(green);
		setBlue(blue);
	}

	@getter(ColorTopicMessage.OBJECT_NAME)
	public Object getObjectName() {
		return objectName;
	}

	@setter(ColorTopicMessage.OBJECT_NAME)
	public void setObjectName(final Object objectName) {
		this.objectName = objectName;
	}
	

	@getter(ColorTopicMessage.RED)
	public Object getRed() {
		return red;
	}

	@setter(ColorTopicMessage.RED)
	public void setRed(final Object red) {
		this.red = red;
	}
	
	
	@getter(ColorTopicMessage.GREEN)
	public Object getGreen() {
		return green;
	}

	@setter(ColorTopicMessage.GREEN)
	public void setGreen(final Object green) {
		this.green = green;
	}
	
	
	@getter(ColorTopicMessage.BLUE)
	public Object getBlue() {
		return blue;
	}

	@setter(ColorTopicMessage.BLUE)
	public void setBlue(final Object blue) {
		this.blue  = blue;
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
	public ColorTopicMessage copy(final IScope scope) throws GamaRuntimeException {
		return new ColorTopicMessage(scope, getSender(), getReceivers(), getObjectName(), getRed(), getGreen(), getBlue(), 
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
