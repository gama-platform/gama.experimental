/*******************************************************************************************************
 *
 * GamaRange.java, in espacedev.gaml.extensions.genstar, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package espacedev.gaml.extensions.genstar.type;

import java.util.Random;

import core.util.random.GenstarRandom;
import gama.core.common.interfaces.IValue;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.runtime.IScope;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;

/**
 * The Class GamaRange.
 */
@vars ({ @variable (
		name = "min_value",
		type = IType.FLOAT,
		doc = @doc ("The lower bound of the range.")),
		@variable (
				name = "max_value",
				type = IType.FLOAT,
				doc = @doc ("The upper bound of the range.")) })
public class GamaRange implements IValue {

	/** The min. */
	Number min;

	/** The max. */
	Number max;

	/**
	 * Instantiates a new gama range.
	 *
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 */
	public GamaRange(final Number min, final Number max) {
		this.min = min;
		this.max = max;
	}

	/**
	 * Gets the min.
	 *
	 * @return the min
	 */
	@getter ("min_value")
	public Number getMin() { return min.doubleValue(); }

	/**
	 * Gets the max.
	 *
	 * @return the max
	 */
	@getter ("max_value")
	public Number getMax() { return max.doubleValue(); }

	@Override
	public JsonValue serializeToJson(Json json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String stringValue(final IScope scope) {
		return serializeToJson(Json.getNew()).asString();
	}

	@Override
	public String toString() {
		return min + "->" + max;
	}

	@Override
	public IValue copy(final IScope scope) {
		return new GamaRange(min, max);
	}

	/**
	 * Cast.
	 *
	 * @param scope
	 *            the scope
	 * @param type
	 *            the type
	 * @return the object
	 */
	@SuppressWarnings ("rawtypes")
	public Object cast(final IScope scope, final IType type) {
		return type == null ? this : switch (type.id()) {
			case IType.INT -> intValue();
			case IType.FLOAT -> floatValue();
			case IType.STRING -> stringValue(scope);
			default -> this;
		};
	}

	/**
	 * Float value.
	 *
	 * @return the double
	 */
	// à raffiner ...
	private double floatValue() {
		Random random = GenstarRandom.getInstance();
		return (max.doubleValue() - min.doubleValue() + 1) * random.nextDouble() + min.doubleValue();
	}

	/**
	 * Int value.
	 *
	 * @return the int
	 */
	private int intValue() {
		return (int) floatValue();
	}
}
