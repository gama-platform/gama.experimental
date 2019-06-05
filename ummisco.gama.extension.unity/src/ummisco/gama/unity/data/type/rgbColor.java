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
package ummisco.gama.unity.data.type;

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
 * The Class rgbColor.
 *
 * 
 */

@vars({ @variable(name = rgbColor.RED, type = IType.INT, doc = { @doc(" ") }),
		@variable(name = rgbColor.GREEN, type = IType.INT),
		@variable(name = rgbColor.BLUE, type = IType.INT, doc = { @doc(" ") }) })
public class rgbColor {

	public final static String RED = IUnitySkill.MSG_RED;
	public final static String GREEN = IUnitySkill.MSG_GREEN;
	public final static String BLUE = IUnitySkill.MSG_BLUE;

	protected Object red;
	protected Object green;
	protected Object blue;

	public rgbColor(final Object red, final Object green, final Object blue) throws GamaRuntimeException {

		setRed(red);
		setGreen(green);
		setBlue(blue);

	}

	@getter(rgbColor.RED)
	public Object getRed() {
		return red;
	}

	@setter(rgbColor.RED)
	public void setRed(final Object red) {
		this.red = red;
	}

	@getter(rgbColor.GREEN)
	public Object getGreen() {
		return green;
	}

	@setter(rgbColor.GREEN)
	public void setGreen(final Object green) {
		this.green = green;
	}

	@getter(rgbColor.BLUE)
	public Object getBlue() {
		return blue;
	}

	@setter(rgbColor.BLUE)
	public void setBlue(final Object blue) {
		this.blue = blue;
	}

}
